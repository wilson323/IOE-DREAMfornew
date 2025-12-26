#!/bin/bash
# IOE-DREAM 快速质量检查
echo "🚀 IOE-DREAM 快速质量检查"
echo "======================"
bash scripts/precise-quality-check.sh
echo ""
echo "📊 运行持续监控:"
bash scripts/continuous-monitoring.sh
