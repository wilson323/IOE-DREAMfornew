#!/bin/bash
# Git Pre-commit Hook - IOE-DREAM 合规性检查
# 安装方法: 复制此文件到 .git/hooks/pre-commit 并设置执行权限
# cp scripts/compliance-scan/pre-commit-hook-template.sh .git/hooks/pre-commit
# chmod +x .git/hooks/pre-commit

echo "🔍 执行代码合规性检查..."

# 获取暂存区的Java文件
STAGED_FILES=$(git diff --cached --name-only --diff-filter=ACM | grep "\.java$")

if [ -z "$STAGED_FILES" ]; then
    echo "✅ 没有Java文件变更，跳过检查"
    exit 0
fi

# 初始化违规标记
VIOLATIONS_FOUND=0

echo "检查文件:"
echo "$STAGED_FILES"
echo ""

# 检查@Repository违规
echo "🔍 检查 @Repository 注解..."
REPO_VIOLATIONS=$(echo "$STAGED_FILES" | xargs grep -l "@Repository" 2>/dev/null || true)
if [ ! -z "$REPO_VIOLATIONS" ]; then
    echo "❌ 发现 @Repository 注解违规:"
    echo "$REPO_VIOLATIONS"
    echo ""
    echo "修复建议: 使用 @Mapper 注解替代"
    echo "import org.apache.ibatis.annotations.Mapper;"
    echo ""
    VIOLATIONS_FOUND=1
fi

# 检查@Autowired违规
echo "🔍 检查 @Autowired 注解..."
AUTO_VIOLATIONS=$(echo "$STAGED_FILES" | xargs grep -l "@Autowired" 2>/dev/null || true)
if [ ! -z "$AUTO_VIOLATIONS" ]; then
    echo "❌ 发现 @Autowired 注解违规:"
    echo "$AUTO_VIOLATIONS"
    echo ""
    echo "修复建议: 使用 @Resource 注解替代"
    echo "import jakarta.annotation.Resource;"
    echo ""
    VIOLATIONS_FOUND=1
fi

# 检查Repository命名违规
echo "🔍 检查 Repository 命名..."
NAMING_VIOLATIONS=$(echo "$STAGED_FILES" | grep "Repository\.java$" | grep -v "RepositoryTest\.java$" || true)
if [ ! -z "$NAMING_VIOLATIONS" ]; then
    echo "❌ 发现 Repository 命名违规:"
    echo "$NAMING_VIOLATIONS"
    echo ""
    echo "修复建议: 使用 Dao 后缀替代 Repository"
    echo "例如: UserRepository.java -> UserDao.java"
    echo ""
    VIOLATIONS_FOUND=1
fi

# 检查javax包名违规
echo "🔍 检查 javax.* 包名..."
JAVAX_VIOLATIONS=$(echo "$STAGED_FILES" | xargs grep -l "import javax\." 2>/dev/null || true)
if [ ! -z "$JAVAX_VIOLATIONS" ]; then
    echo "⚠️  发现 javax.* 包名使用（建议使用 jakarta.*）:"
    echo "$JAVAX_VIOLATIONS"
    echo ""
    echo "修复建议: 使用 jakarta.* 包名替代"
    echo "例如: javax.annotation.Resource -> jakarta.annotation.Resource"
    echo ""
    # 注意：这是警告，不阻止提交
fi

# 如果发现违规，阻止提交
if [ $VIOLATIONS_FOUND -eq 1 ]; then
    echo ""
    echo "╔════════════════════════════════════════════════════════════════╗"
    echo "║  ❌ 代码合规性检查失败                                       ║"
    echo "║                                                                ║"
    echo "║  请修复以上违规后再提交                                     ║"
    echo "║  参考文档: CLAUDE.md                                         ║"
    echo "║  修复计划: COMPREHENSIVE_REMEDIATION_PLAN_2025-12-03.md     ║"
    echo "╚════════════════════════════════════════════════════════════════╝"
    echo ""
    exit 1
fi

echo "✅ 代码合规性检查通过"
exit 0

