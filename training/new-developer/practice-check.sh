#!/bin/bash
echo "🧪 练习环境质量检查"
echo "=================="
cd "$(dirname "$0")/../.."
bash scripts/comprehensive-quality-check.sh
