"""Preference alignment with DPO.

Second stage of the App Store pipeline's training recipe: after SFT produces
an adapter that emits structurally-correct outputs, DPO tightens stylistic
and safety preferences using human-edited pairs.

Expected data format (JSONL):
    {"prompt": "...", "chosen": "...", "rejected": "..."}

`chosen` is the edited human-preferred output; `rejected` is the original
model draft.

Usage:
    python -m training.dpo_train \
        --stage insight \
        --base out/insight-adapter \
        --data training/data/insights.dpo.jsonl \
        --output out/insight-adapter-dpo
"""

from __future__ import annotations

import argparse
import logging
from pathlib import Path

import torch
from datasets import load_dataset
from peft import PeftModel
from transformers import AutoModelForCausalLM, AutoTokenizer
from trl import DPOConfig, DPOTrainer

from config import SETTINGS

logger = logging.getLogger(__name__)


def main() -> None:
    logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(name)s: %(message)s")

    parser = argparse.ArgumentParser()
    parser.add_argument("--stage", required=True, choices=["insight", "topic", "classify"])
    parser.add_argument("--base", required=True, help="Path to the SFT-produced adapter.")
    parser.add_argument("--data", required=True)
    parser.add_argument("--output", required=True)
    parser.add_argument("--epochs", type=float, default=1.0)
    parser.add_argument("--beta", type=float, default=0.1)
    parser.add_argument("--lr", type=float, default=5e-6)
    args = parser.parse_args()

    base_model_name = SETTINGS.model.base_model
    tokenizer = AutoTokenizer.from_pretrained(base_model_name)
    if tokenizer.pad_token_id is None:
        tokenizer.pad_token = tokenizer.eos_token

    model = AutoModelForCausalLM.from_pretrained(base_model_name, torch_dtype=torch.bfloat16)
    model = PeftModel.from_pretrained(model, args.base, is_trainable=True)

    reference = AutoModelForCausalLM.from_pretrained(base_model_name, torch_dtype=torch.bfloat16)
    reference = PeftModel.from_pretrained(reference, args.base)
    reference.eval()

    dataset = load_dataset("json", data_files=args.data, split="train")

    config = DPOConfig(
        output_dir=args.output,
        num_train_epochs=args.epochs,
        per_device_train_batch_size=2,
        gradient_accumulation_steps=8,
        learning_rate=args.lr,
        beta=args.beta,
        bf16=True,
        logging_steps=10,
        save_strategy="epoch",
        report_to=["none"],
    )

    trainer = DPOTrainer(
        model=model,
        ref_model=reference,
        args=config,
        train_dataset=dataset,
        tokenizer=tokenizer,
    )
    trainer.train()

    output = Path(args.output)
    output.mkdir(parents=True, exist_ok=True)
    trainer.model.save_pretrained(output)
    tokenizer.save_pretrained(output)
    logger.info("DPO adapter for stage=%s saved to %s", args.stage, output)


if __name__ == "__main__":
    main()
