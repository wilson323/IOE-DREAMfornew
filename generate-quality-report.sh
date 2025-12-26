#!/bin/bash
# IOE-DREAM 质量报告生成
echo "📄 IOE-DREAM 质量报告生成"
echo "======================="
bash scripts/quality-trend-analysis.sh
echo ""
echo "📋 查看所有报告:"
ls -la monitoring-reports/*.txt | tail -5
