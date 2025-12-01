#!/bin/bash

echo "=================================================="
echo "📊 IOE-DREAM 项目代码质量全面检查报告"
echo "=================================================="
echo "检查时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo "检查版本: v1.0.0 (基于repowiki规范)"
echo ""

# 进入项目目录
cd smart-admin-api-java17-springboot3

# 1. 编译状态检查
echo "🔍 1. 编译状态检查"
echo "-------------------"

# 获取编译状态
mvn clean compile -DskipTests > compile-result.log 2>&1
COMPILE_SUCCESS=$?

if [ $COMPILE_SUCCESS -eq 0 ]; then
    echo "✅ 编译状态: 成功"
    ERROR_COUNT=0
else
    echo "❌ 编译状态: 失败"
    ERROR_COUNT=$(grep -c "ERROR" compile-result.log)
fi

echo "编译错误数量: $ERROR_COUNT"
WARNING_COUNT=$(grep -c "WARNING" compile-result.log)
echo "编译警告数量: $WARNING_COUNT"
echo ""

# 2. 编码规范检查
echo "📝 2. 编码规范检查"
echo "-------------------"

# UTF-8编码检查
echo "2.1 UTF-8编码合规性检查"
NON_UTF8_FILES=$(find . -name "*.java" -exec file {} \; | grep -v "UTF-8\|ASCII" | wc -l)
echo "  非UTF-8文件数量: $NON_UTF8_FILES"
if [ $NON_UTF8_FILES -eq 0 ]; then
    echo "  ✅ UTF-8编码合规"
else
    echo "  ❌ 发现 $NON_UTF8_FILES 个非UTF-8文件"
fi

# BOM标记检查
echo "2.2 BOM标记检查"
BOM_FILES=$(find . -name "*.java" -exec grep -l $'^\xEF\xBB\xBF' {} \; | wc -l)
echo "  含BOM标记文件数量: $BOM_FILES"
if [ $BOM_FILES -eq 0 ]; then
    echo "  ✅ 无BOM标记"
else
    echo "  ❌ 发现 $BOM_FILES 个含BOM标记文件"
fi

# 包名规范检查 (repowiki一级规范)
echo "2.3 包名规范检查 (repowiki一级规范)"
JAVAX_FILES=$(find . -name "*.java" -exec grep -l "javax\." {} \; | wc -l)
echo "  javax包使用数量: $JAVAX_FILES"
if [ $JAVAX_FILES -eq 0 ]; then
    echo "  ✅ 包名规范合规 (已迁移到jakarta)"
else
    echo "  ❌ 发现 $JAVAX_FILES 个文件使用javax包"
fi

# 依赖注入检查 (repowiki一级规范)
echo "2.4 依赖注入规范检查 (repowiki一级规范)"
AUTOWIRED_FILES=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l)
echo "  @Autowired使用数量: $AUTOWIRED_FILES"
if [ $AUTOWIRED_FILES -eq 0 ]; then
    echo "  ✅ 依赖注入规范合规 (使用@Resource)"
else
    echo "  ❌ 发现 $AUTOWIRED_FILES 个文件使用@Autowired"
fi

# 日志规范检查 (repowiki二级规范)
echo "2.5 日志规范检查 (repowiki二级规范)"
SYSOUT_FILES=$(find . -name "*.java" -exec grep -l "System\.out\.println" {} \; | wc -l)
echo "  System.out.println使用数量: $SYSOUT_FILES"
if [ $SYSOUT_FILES -eq 0 ]; then
    echo "  ✅ 日志规范合规 (使用SLF4J)"
else
    echo "  ⚠️ 发现 $SYSOUT_FILES 个文件使用System.out.println"
fi

echo ""

# 3. 架构合规性检查
echo "🏗️ 3. 架构合规性检查"
echo "--------------------"

# 四层架构检查 (Controller直接访问DAO)
echo "3.1 四层架构合规检查"
CONTROLLER_DIRECT_DAO=$(grep -r "@Resource.*Dao" --include="*Controller.java" . | wc -l)
echo "  Controller直接访问DAO数量: $CONTROLLER_DIRECT_DAO"
if [ $CONTROLLER_DIRECT_DAO -eq 0 ]; then
    echo "  ✅ 四层架构合规"
else
    echo "  ❌ 发现 $CONTROLLER_DIRECT_DAO 处架构违规"
fi

# Entity继承检查
echo "3.2 Entity继承BaseEntity检查"
ENTITY_WITHOUT_BASE=$(find . -name "*Entity.java" -exec grep -L "extends BaseEntity" {} \; | wc -l)
echo "  未继承BaseEntity的Entity数量: $ENTITY_WITHOUT_BASE"
if [ $ENTITY_WITHOUT_BASE -eq 0 ]; then
    echo "  ✅ Entity继承规范合规"
else
    echo "  ❌ 发现 $ENTITY_WITHOUT_BASE 个Entity未继承BaseEntity"
fi

# 权限注解检查
echo "3.3 权限注解检查"
CONTROLLER_METHODS=$(grep -r "@PostMapping\|@GetMapping\|@PutMapping\|@DeleteMapping" --include="*Controller.java" . | wc -l)
PERMISSION_METHODS=$(grep -r "@SaCheckPermission" --include="*Controller.java" . | wc -l)
echo "  Controller方法总数: $CONTROLLER_METHODS"
echo "  权限注解数量: $PERMISSION_METHODS"
PERMISSION_COVERAGE=$((PERMISSION_METHODS * 100 / CONTROLLER_METHODS))
echo "  权限注解覆盖率: $PERMISSION_COVERAGE%"
if [ $PERMISSION_COVERAGE -ge 80 ]; then
    echo "  ✅ 权限注解覆盖率达标 (≥80%)"
else
    echo "  ⚠️ 权限注解覆盖率偏低 (<80%)"
fi

echo ""

# 4. 代码复杂度检查
echo "📈 4. 代码复杂度检查"
echo "-------------------"

# 圈复杂度统计
TOTAL_COMPLEXITY=0
FILES_COUNT=0

find . -name "*.java" | while read file; do
    # 简单的圈复杂度估算：计算控制结构数量
    if_count=$(grep -c "\bif\b" "$file" 2>/dev/null || echo 0)
    for_count=$(grep -c "\bfor\b" "$file" 2>/dev/null || echo 0)
    while_count=$(grep -c "\bwhile\b" "$file" 2>/dev/null || echo 0)
    case_count=$(grep -c "\bcase\b" "$file" 2>/dev/null || echo 0)
    catch_count=$(grep -c "\bcatch\b" "$file" 2>/dev/null || echo 0)

    complexity=$((if_count + for_count + while_count + case_count + catch_count + 1))

    if [ $complexity -gt 10 ]; then
        echo "  高复杂度文件: $(basename "$file") (复杂度: $complexity)"
    fi
done

echo "✅ 代码复杂度检查完成"
echo ""

# 5. 模块质量评估
echo "📁 5. 模块质量评估"
echo "------------------"

# 各模块错误统计
ATTENDANCE_ERRORS=$(grep -c "/attendance/" compile-result.log 2>/dev/null || echo 0)
CONSUME_ERRORS=$(grep -c "/consume/" compile-result.log 2>/dev/null || echo 0)
ACCESS_ERRORS=$(grep -c "/access/" compile-result.log 2>/dev/null || echo 0)
VIDEO_ERRORS=$(grep -c "/video/" compile-result.log 2>/dev/null || echo 0)
SYSTEM_ERRORS=$(grep -c "/system/" compile-result.log 2>/dev/null || echo 0)
OA_ERRORS=$(grep -c "/oa/" compile-result.log 2>/dev/null || echo 0)
BIOMETRIC_ERRORS=$(grep -c "/biometric/" compile-result.log 2>/dev/null || echo 0)

echo "  考勤模块错误: $ATTENDANCE_ERRORS"
echo "  消费模块错误: $CONSUME_ERRORS"
echo "  门禁模块错误: $ACCESS_ERRORS"
echo "  视频模块错误: $VIDEO_ERRORS"
echo "  系统模块错误: $SYSTEM_ERRORS"
echo "  OA模块错误: $OA_ERRORS"
echo "  生物识别模块错误: $BIOMETRIC_ERRORS"

echo ""

# 6. 质量改进建议
echo "🛠️ 6. 质量改进建议"
echo "------------------"

if [ $ERROR_COUNT -gt 0 ]; then
    echo "🔴 高优先级修复建议:"

    if [ $SYSTEM_ERRORS -gt 20 ]; then
        echo "  - 系统模块错误较多 ($SYSTEM_ERRORS)，建议优先修复"
    fi

    if [ $JAVAX_FILES -gt 0 ]; then
        echo "  - 立即修复 javax 包使用问题 (违反repowiki一级规范)"
    fi

    if [ $AUTOWIRED_FILES -gt 0 ]; then
        echo "  - 立即修复 @Autowired 使用问题 (违反repowiki一级规范)"
    fi

    echo ""
fi

if [ $WARNING_COUNT -gt 10 ]; then
    echo "🟡 中优先级改进建议:"
    echo "  - 处理编译警告信息 ($WARNING_COUNT 个)"
    echo ""
fi

echo "🟢 低优先级优化建议:"
if [ $SYSOUT_FILES -gt 0 ]; then
    echo "  - 替换 System.out.println 为 SLF4J 日志"
fi

if [ $PERMISSION_COVERAGE -lt 80 ]; then
    echo "  - 提高权限注解覆盖率"
fi

echo ""

# 7. 质量评分
echo "📊 7. 综合质量评分"
echo "------------------"

# 计算质量评分 (满分100分)
SCORE=100

# 编译错误扣分
SCORE=$((SCORE - ERROR_COUNT * 2))

# 编码规范扣分
SCORE=$((SCORE - JAVAX_FILES * 5))
SCORE=$((SCORE - AUTOWIRED_FILES * 5))
SCORE=$((SCORE - SYSOUT_FILES))
SCORE=$((SCORE - NON_UTF8_FILES * 3))
SCORE=$((SCORE - BOM_FILES * 3))

# 架构违规扣分
SCORE=$((SCORE - CONTROLLER_DIRECT_DAO * 10))
SCORE=$((SCORE - ENTITY_WITHOUT_BASE * 5))

# 确保评分不低于0
if [ $SCORE -lt 0 ]; then
    SCORE=0
fi

echo "项目综合质量评分: $SCORE/100"

if [ $SCORE -ge 90 ]; then
    echo "质量等级: 优秀 ⭐⭐⭐⭐⭐"
elif [ $SCORE -ge 80 ]; then
    echo "质量等级: 良好 ⭐⭐⭐⭐"
elif [ $SCORE -ge 70 ]; then
    echo "质量等级: 中等 ⭐⭐⭐"
elif [ $SCORE -ge 60 ]; then
    echo "质量等级: 及格 ⭐⭐"
else
    echo "质量等级: 需要改进 ⭐"
fi

echo ""

# 8. 修复指南
echo "🔧 8. 立即修复指南"
echo "------------------"

echo "执行以下命令进行快速修复:"
echo ""
echo "# 1. 修复javax包问题"
echo "find . -name '*.java' -exec sed -i 's/javax\\./jakarta\\./g' {} \\;"
echo ""
echo "# 2. 修复@Autowired问题"
echo "find . -name '*.java' -exec sed -i 's/@Autowired/@Resource/g' {} \\;"
echo ""
echo "# 3. 修复System.out问题"
echo "find . -name '*.java' -exec sed -i 's/System\\.out\\.println/log.info/g' {} \\;"
echo ""

# 清理临时文件
rm -f compile-result.log

echo "=================================================="
echo "✅ 代码质量检查完成"
echo "=================================================="
echo "📋 生成时间: $(date)"
echo "📧 如有疑问，请联系开发团队"
echo "=================================================="