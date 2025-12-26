#!/bin/bash
echo "📊 培训进度检查"
echo "=============="
cd "$(dirname "$0")/../.."

echo "当前代码质量状态:"
bash scripts/precise-quality-check.sh

echo ""
echo "质量趋势分析:"
bash scripts/quality-trend-analysis.sh

echo ""
echo "个人培训报告:"
echo "培训开始时间: $(date)"
echo "练习文件数: $(find training/*/practice -name "*.java" 2>/dev/null | wc -l)"
echo "报告文件数: $(find training/*/reports -name "*.txt" 2>/dev/null | wc -l)"
