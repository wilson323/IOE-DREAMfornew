#!/bin/bash
echo "🚀 快速质量检查"
echo "================"
cd "$(dirname "$0")/../.."
bash scripts/precise-quality-check.sh
