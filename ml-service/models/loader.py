"""Model loading + inference primitive.

One `LlmRunner` is shared by whichever pipeline stages happen to co-locate in
the current process. In production each stage runs in its own pod with its own
`HOUSING_ML_*_ADAPTER` env var, so only the relevant LoRA is hot-loaded.
"""

from __future__ import annotations

import logging
import os
import threading
from functools import lru_cache
from typing import Optional

# Torch + transformers imports deferred so mock mode needs zero ML deps.
if not os.environ.get("HOUSING_ML_MOCK"):
    import torch
    from transformers import AutoModelForCausalLM, AutoTokenizer
else:  # pragma: no cover
    torch = None  # type: ignore
    AutoModelForCausalLM = None  # type: ignore
    AutoTokenizer = None  # type: ignore

try:  # PEFT is optional at runtime — if no adapter path is given we skip it.
    from peft import PeftModel
except ImportError:  # pragma: no cover
    PeftModel = None  # type: ignore

from config import SETTINGS, ModelConfig

logger = logging.getLogger(__name__)


_DTYPES = {
    "bfloat16": torch.bfloat16,
    "float16": torch.float16,
    "float32": torch.float32,
}


class LlmRunner:
    """Thin wrapper around `model.generate`. Thread-safe for concurrent gens."""

    def __init__(self, cfg: ModelConfig, adapter_path: Optional[str]) -> None:
        self.cfg = cfg
        self.adapter_path = adapter_path
        self._lock = threading.Lock()

        dtype = _DTYPES.get(cfg.dtype, torch.bfloat16)
        quant_kwargs: dict = {}
        if cfg.load_in_4bit:
            from transformers import BitsAndBytesConfig

            quant_kwargs["quantization_config"] = BitsAndBytesConfig(
                load_in_4bit=True,
                bnb_4bit_compute_dtype=dtype,
                bnb_4bit_quant_type="nf4",
            )

        logger.info("loading base model %s (dtype=%s, 4bit=%s)", cfg.base_model, cfg.dtype, cfg.load_in_4bit)
        self.tokenizer = AutoTokenizer.from_pretrained(cfg.base_model)
        self.model = AutoModelForCausalLM.from_pretrained(
            cfg.base_model,
            torch_dtype=dtype,
            device_map=cfg.device,
            **quant_kwargs,
        )

        if adapter_path and PeftModel is not None:
            logger.info("attaching LoRA adapter %s", adapter_path)
            self.model = PeftModel.from_pretrained(self.model, adapter_path)

        self.model.eval()

    @torch.inference_mode()
    def generate(self, prompt: str, *, max_new_tokens: Optional[int] = None, temperature: Optional[float] = None) -> str:
        max_new = max_new_tokens if max_new_tokens is not None else self.cfg.max_new_tokens
        temp = temperature if temperature is not None else self.cfg.temperature

        with self._lock:
            inputs = self.tokenizer(prompt, return_tensors="pt").to(self.model.device)
            output = self.model.generate(
                **inputs,
                max_new_tokens=max_new,
                do_sample=temp > 0,
                temperature=max(temp, 1e-4),
                pad_token_id=self.tokenizer.eos_token_id,
            )
            generated = output[0, inputs["input_ids"].shape[-1]:]
            return self.tokenizer.decode(generated, skip_special_tokens=True).strip()


@lru_cache(maxsize=4)
def get_runner(adapter_env_var: str | None):
    """Cache runners by adapter path so multiple stages in one pod share base weights."""

    if os.environ.get("HOUSING_ML_MOCK"):
        from models.mock import MockLlmRunner
        return MockLlmRunner(adapter_env_var)

    cfg = SETTINGS.model
    adapter_path = None
    if adapter_env_var == "insight":
        adapter_path = cfg.insight_adapter
    elif adapter_env_var == "topic":
        adapter_path = cfg.topic_adapter
    elif adapter_env_var == "classify":
        adapter_path = cfg.classify_adapter

    return LlmRunner(cfg, adapter_path)
