# Base with CUDA for GPU workers. For CPU-only deployment swap to python:3.11-slim.
FROM nvidia/cuda:12.4.1-cudnn-runtime-ubuntu22.04

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=1

RUN apt-get update && apt-get install -y --no-install-recommends \
      python3.11 python3.11-venv python3-pip \
      build-essential git \
    && rm -rf /var/lib/apt/lists/*

RUN ln -sf /usr/bin/python3.11 /usr/bin/python && \
    python -m pip install --upgrade pip

WORKDIR /app

COPY pyproject.toml ./
RUN python -m pip install --upgrade pip setuptools wheel && \
    python -m pip install .

COPY . .

# The actual entrypoint is injected by compose / k8s: one of
#   housing-ml-gateway, housing-ml-stage1, housing-ml-stage2, housing-ml-stage3
CMD ["housing-ml-gateway"]
