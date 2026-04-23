"""Supervised fine-tuning of a small Gemma variant with LoRA adapters.

Produces one adapter per stage (insight / topic / classify) so each worker
loads only the adapter relevant to its stage. Mirrors the App Store paper:
"fine-tuned with LoRA adapters... distills each review into distinct,
atomic statements."

Usage:
    python -m training.sft_train \
        --stage insight \
        --data training/data/insights.jsonl \
        --output out/insight-adapter
"""

from __future__ import annotations

import argparse
import logging
from pathlib import Path

import torch
from datasets import load_dataset
from peft import LoraConfig, TaskType, get_peft_model
from transformers import (
    AutoModelForCausalLM,
    AutoTokenizer,
    DataCollatorForLanguageModeling,
    Trainer,
    TrainingArguments,
)

from config import SETTINGS

logger = logging.getLogger(__name__)


def build_lora_config() -> LoraConfig:
    # Low rank, reasonable alpha — tuned down for the 1B base. Bump both
    # proportionally if you swap to the 4B/12B variants.
    return LoraConfig(
        r=16,
        lora_alpha=32,
        lora_dropout=0.05,
        bias="none",
        task_type=TaskType.CAUSAL_LM,
        # Gemma attention modules. Update if swapping base family.
        target_modules=["q_proj", "k_proj", "v_proj", "o_proj"],
    )


def format_example(example: dict, tokenizer) -> dict:
    text = example["prompt"] + example["completion"] + tokenizer.eos_token
    encoded = tokenizer(text, truncation=True, max_length=2048)
    encoded["labels"] = encoded["input_ids"].copy()
    return encoded


def main() -> None:
    logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(name)s: %(message)s")

    parser = argparse.ArgumentParser()
    parser.add_argument("--stage", required=True, choices=["insight", "topic", "classify"])
    parser.add_argument("--data", required=True, help="JSONL with {prompt, completion} fields.")
    parser.add_argument("--output", required=True)
    parser.add_argument("--epochs", type=float, default=3.0)
    parser.add_argument("--batch-size", type=int, default=4)
    parser.add_argument("--grad-accum", type=int, default=4)
    parser.add_argument("--lr", type=float, default=2e-4)
    args = parser.parse_args()

    base = SETTINGS.model.base_model
    logger.info("loading base %s", base)
    tokenizer = AutoTokenizer.from_pretrained(base)
    if tokenizer.pad_token_id is None:
        tokenizer.pad_token = tokenizer.eos_token

    model = AutoModelForCausalLM.from_pretrained(base, torch_dtype=torch.bfloat16)
    model = get_peft_model(model, build_lora_config())
    model.print_trainable_parameters()

    raw = load_dataset("json", data_files=args.data, split="train")
    dataset = raw.map(
        lambda ex: format_example(ex, tokenizer),
        remove_columns=raw.column_names,
    )

    collator = DataCollatorForLanguageModeling(tokenizer=tokenizer, mlm=False)

    training_args = TrainingArguments(
        output_dir=args.output,
        num_train_epochs=args.epochs,
        per_device_train_batch_size=args.batch_size,
        gradient_accumulation_steps=args.grad_accum,
        learning_rate=args.lr,
        bf16=True,
        logging_steps=10,
        save_strategy="epoch",
        save_total_limit=2,
        report_to=["none"],
    )

    trainer = Trainer(
        model=model,
        args=training_args,
        train_dataset=dataset,
        data_collator=collator,
    )
    trainer.train()

    output = Path(args.output)
    output.mkdir(parents=True, exist_ok=True)
    trainer.model.save_pretrained(output)
    tokenizer.save_pretrained(output)
    logger.info("saved adapter for stage=%s to %s", args.stage, output)


if __name__ == "__main__":
    main()
