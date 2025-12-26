#!/bin/bash

echo "🎓 IOE-DREAM 质量培训启动"
echo "========================="
echo "培训时间: $(date)"
echo "培训用户: $USER"
echo ""

# 检查培训环境
echo "🔍 检查培训环境..."
cd "$(dirname "$0")/../.."

if [ ! -f "scripts/precise-quality-check.sh" ]; then
    echo "❌ 质量检查脚本不存在，请确保在正确的项目目录中运行"
    exit 1
fi

echo "✅ 培训环境检查通过"
echo ""

# 显示当前质量状态
echo "📊 当前项目质量状态:"
echo "======================"
bash scripts/precise-quality-check.sh

echo ""
echo "📚 培训资源导航:"
echo "==============="
echo "1. 架构规范文档: ./CLAUDE.md"
echo "2. 培训指南: ./documentation/QUALITY_TRAINING_GUIDE.md"
echo "3. 质量总结: ./documentation/final-quality-gate-summary.md"
echo ""

echo "🧪 培训练习:"
echo "==========="
echo "1. 快速质量检查: ./quick-check.sh"
echo "2. 练习环境检查: ./practice-check.sh"
echo "3. 进度跟踪: ./progress-check.sh"
echo ""

echo "📝 建议的学习步骤:"
echo "================"
echo "1. 阅读CLAUDE.md文档，理解架构规范"
echo "2. 学习质量培训指南，了解培训内容"
echo "3. 运行质量检查脚本，理解检查结果"
echo "4. 完成练习题，实践质量修复"
echo "5. 定期检查进度，跟踪学习成果"
echo ""

echo "🚀 开始您的质量培训之旅！"
