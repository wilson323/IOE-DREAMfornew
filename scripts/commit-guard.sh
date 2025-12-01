#!/bin/bash
# 提交守卫 - 强制性Git提交验证
# 用途: 任何git commit前必须通过此验证

set -e

echo "🛡️ 提交守卫验证 - 强制执行"
echo "时间: $(date)"
echo ""

# 检查是否在git仓库中
if [ ! -d ".git" ]; then
    echo "❌ 不在Git仓库中！"
    exit 1
fi

# 检查是否有待提交的文件
if [ -z "$(git status --porcelain)" ]; then
    echo "ℹ️ 没有文件待提交"
    exit 0
fi

echo ""
echo "检查1: 强制编译验证"
echo "执行: mvn clean compile -q"
if mvn clean compile -q; then
    echo "✅ 编译验证通过"
else
    echo "❌ 编译失败！禁止提交！"
    echo "请修复所有编译错误后再提交"
    exit 1
fi

echo ""
echo "检查2: 强制测试验证"
echo "执行: mvn test -q"
if mvn test -q; then
    echo "✅ 测试验证通过"
else
    echo "❌ 测试失败！禁止提交！"
    echo "请修复所有测试错误后再提交"
    exit 1
fi

echo ""
echo "检查3: Jakarta 规范守卫"
if bash scripts/jakarta-guard.sh; then
  echo "✅ Jakarta 规范守卫通过"
else
  echo "❌ Jakarta 规范守卫失败！禁止提交！"
  exit 1
fi

echo ""
echo "检查4: 提交信息验证"
COMMIT_MSG_FILE=".git/COMMIT_EDITMSG"
if [ -f "$COMMIT_MSG_FILE" ]; then
    commit_msg=$(cat "$COMMIT_MSG_FILE")
    # 检查是否包含必要信息
    if [[ ! "$commit_msg" =~ (feat|fix|docs|style|refactor|test|chore)[^[:space:]] ]]; then
        echo "❌ 提交信息格式不符合约定式提交规范！"
        echo "必须包含类型前缀: feat, fix, docs, style, refactor, test, chore"
        echo "当前提交信息: $commit_msg"
        exit 1
    fi
    echo "✅ 提交信息格式正确"
else
    echo "ℹ️ 未找到提交信息文件"
fi

echo ""
echo "检查5: 强制执行验证"
echo "执行: bash scripts/mandatory-verification.sh"
if bash scripts/mandatory-verification.sh; then
    echo "✅ 强制执行验证通过"
else
    echo "❌ 强制执行验证失败！禁止提交！"
    exit 1
fi

echo ""
echo "🎯 提交守卫验证通过"
echo "所有检查通过，可以安全提交"

# 生成提交守卫证明
COMMIT_GUARD_PROOF="commit-guard-passed-$(date +%Y%m%d-%H%M%S).proof"
cat > "$COMMIT_GUARD_PROOF" << EOF
提交守卫验证通过
时间: $(date)
项目: $(pwd)
状态: PASSED
允许: Git Commit
EOF
echo "📄 守卫证明: $COMMIT_GUARD_PROOF"

echo ""
echo "🛡️ 提交守卫验证完成 - Git操作已放行"