#!/bin/bash
# 快速全局日志规范验证脚本

echo "🚀 快速全局日志规范验证..."

# 统计关键指标
echo "📊 统计全局日志规范状态..."

TOTAL_FILES=$(find . -name "*.java" | wc -l)
SLF4J_IMPORTS=$(find . -name "*.java" -exec grep -l "import lombok\.extern\.slf4j\.Slf4j" {} \; 2>/dev/null | wc -l)
TRADITIONAL_LOGGER=$(find . -name "*.java" -exec grep -l "import org\.slf4j\.Logger" {} \; 2>/dev/null | wc -l)
TRADITIONAL_FACTORY=$(find . -name "*.java" -exec grep -l "import org\.slf4j\.LoggerFactory" {} \; 2>/dev/null | wc -l)
SLF4J_ANNOTATIONS=$(find . -name "*.java" -exec grep -l "@Slf4j" {} \; 2>/dev/null | wc -l)
DAO_SLF4J_ERRORS=$(find . -name "*Dao.java" -o -name "*DAO.java" | xargs grep -l "@Slf4j" 2>/dev/null | wc -l)

echo "📈 全局统计结果:"
echo "  📄 总Java文件数: $TOTAL_FILES"
echo "  ✅ @Slf4j import: $SLF4J_IMPORTS"
echo "  ✅ @Slf4j 注解: $SLF4J_ANNOTATIONS"
echo "  ❌ 传统Logger import: $TRADITIONAL_LOGGER"
echo "  ❌ 传统LoggerFactory import: $TRADITIONAL_FACTORY"
echo "  ❌ DAO接口@Slf4j错误: $DAO_SLF4J_ERRORS"

# 计算统一度
TOTAL_ISSUES=$((TRADITIONAL_LOGGER + TRADITIONAL_FACTORY + DAO_SLF4J_ERRORS))
if [ $TOTAL_ISSUES -eq 0 ]; then
    UNIFICATION=100
else
    UNIFICATION=$((100 - TOTAL_ISSUES))
fi

echo ""
echo "🎯 全局统一度: $UNIFICATION%"

if [ $UNIFICATION -eq 100 ]; then
    echo "🎉 🏆 全局日志规范已完全统一！"
    echo "✨ 所有文件都严格遵循 @Slf4j 规范！"
    echo "🚀 IOE-DREAM项目达到企业级A+日志标准！"
else
    echo "⚠️ 发现 $TOTAL_ISSUES 个问题需要处理:"
    if [ $TRADITIONAL_LOGGER -gt 0 ]; then
        echo "  - $TRADITIONAL_LOGGER 个文件仍使用传统Logger"
    fi
    if [ $TRADITIONAL_FACTORY -gt 0 ]; then
        echo "  - $TRADITIONAL_FACTORY 个文件仍使用LoggerFactory"
    fi
    if [ $DAO_SLF4J_ERRORS -gt 0 ]; then
        echo "  - $DAO_SLF4J_ERRORS 个DAO接口错误使用@Slf4j"
    fi
fi

# 生成快速验证报告
echo ""
echo "📋 生成快速验证报告..."

REPORT="QUICK_GLOBAL_LOGGING_REPORT-$(date +%Y%m%d_%H%M%S).md"

cat > "$REPORT" << EOF
# IOE-DREAM 快速全局日志规范验证报告

**验证时间**: $(date)
**验证范围**: 全局 $TOTAL_FILES 个Java文件

## 📊 验证结果

| 指标 | 数量 | 状态 |
|------|------|------|
| 总Java文件 | $TOTAL_FILES | 📊 |
| @Slf4j import | $SLF4J_IMPORTS | ✅ |
| @Slf4j 注解 | $SLF4J_ANNOTATIONS | ✅ |
| 传统Logger import | $TRADITIONAL_LOGGER | $(if [ $TRADITIONAL_LOGGER -eq 0 ]; then echo "✅"; else echo "❌"; fi) |
| 传统LoggerFactory | $TRADITIONAL_FACTORY | $(if [ $TRADITIONAL_FACTORY -eq 0 ]; then echo "✅"; else echo "❌"; fi) |
| DAO接口@Slf4j错误 | $DAO_SLF4J_ERRORS | $(if [ $DAO_SLF4J_ERRORS -eq 0 ]; then echo "✅"; else echo "❌"; fi) |

## 🎯 全局统一度

**统一度**: $UNIFICATION%

**质量等级**: $(if [ $UNIFICATION -eq 100 ]; then echo "🏆 企业级A+"; elif [ $UNIFICATION -ge 95 ]; then echo "⭐ A级"; else echo "⚠️ 需要改进"; fi)

## 📋 验证结论

$(if [ $UNIFICATION -eq 100 ]; then echo "🎉 **全局日志规范完全统一！**"; else echo "⚠️ **需要进一步修复以实现完全统一**"; fi)

- [x] @Slf4j规范普及率: $((SLF4J_IMPORTS * 100 / TOTAL_FILES))%
- [x] 传统Logger清理: $((100 - TRADITIONAL_LOGGER * 100 / TOTAL_FILES))%
- [x] DAO接口合规性: $((100 - DAO_SLF4J_ERRORS * 100 / TOTAL_FILES))%

---

**生成时间**: $(date)
**验证范围**: IOE-DREAM全局
**质量认证**: $(if [ $UNIFICATION -eq 100 ]; then echo "企业级A+ 🏆"; else echo "持续改进中 ⚡"; fi)
EOF

echo "✅ 快速验证报告已生成: $REPORT"

exit $([ $UNIFICATION -eq 100 ] && echo 0 || echo 1)