# Training data

Each stage has its own JSONL file. Produce them with `training/data_templates.py`:

```python
from training.data_templates import stage1_sft
# build HouseInsight objects by hand / from a rater UI, then:
example = stage1_sft(request, insights)
# append `orjson.dumps(example)` + b"\n" to insights.jsonl
```

Expected files:

- `insights.jsonl` — stage 1 SFT (`{prompt, completion}`)
- `topics.jsonl` — stage 2 SFT
- `categories.jsonl` — stage 3 SFT
- `*.dpo.jsonl` — DPO refinement pairs `{prompt, chosen, rejected}`

Per the App Store paper, start with a large, diverse set of *reference*
outputs written by human experts, then collect DPO pairs by asking raters to
edit a model draft instead of writing from scratch. That scales better and
captures the network's house/realm conventions more accurately than
greenfield authoring.
