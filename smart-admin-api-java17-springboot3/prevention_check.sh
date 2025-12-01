#!/bin/bash
# 预防性检查脚本 - 确保代码质量

echo "🔍 执行预防性质量检查..."

cd "D:/IOE-DREAM/smart-admin-api-java17-springboot3"

# 初始化检查结果
check_passed=true
error_count=0

# 1. 编译检查
echo "📝 执行编译检查..."
mvn clean compile -q > compile_check.log 2>&1
if [ $? -ne 0 ]; then
    echo "❌ 编译检查失败"
    error_count=$((error_count + 1))
    check_passed=false
else
    echo "✅ 编译检查通过"
fi

# 2. repowiki规范一级检查
echo "📝 执行repowiki规范检查..."

# 2.1 jakarta包名检查
javax_count=$(find . -name "*.java" -exec grep -l "javax\.(annotation|validation|persistence|servlet)" {} \; | wc -l)
if [ $javax_count -ne 0 ]; then
    echo "❌ 发现 $javax_count 个jakarta迁移违规"
    error_count=$((error_count + javax_count))
    check_passed=false
else
    echo "✅ jakarta包名检查通过"
fi

# 2.2 @Autowired检查
autowired_count=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l)
if [ $autowired_count -ne 0 ]; then
    echo "❌ 发现 $autowired_count 个@Autowired使用"
    error_count=$((error_count + autowired_count))
    check_passed=false
else
    echo "✅ 依赖注入检查通过"
fi

# 2.3 架构违规检查
controller_dao_count=$(grep -r "@Resource.*Dao" --include="*Controller.java" . | wc -l)
if [ $controller_dao_count -ne 0 ]; then
    echo "❌ 发现 $controller_dao_count 个架构违规"
    error_count=$((error_count + controller_dao_count))
    check_passed=false
else
    echo "✅ 架构规范检查通过"
fi

# 3. 代码质量二级检查
echo "📝 执行代码质量检查..."

# 3.1 System.out检查
system_out_count=$(find . -name "*.java" -exec grep -l "System\.out\.println" {} \; | wc -l)
if [ $system_out_count -gt 0 ]; then
    echo "⚠️ 发现 $system_out_count 个System.out使用"
    # 仅警告，不阻断
fi

# 3.2 实体类继承检查
total_entities=$(find . -name "*Entity.java" | wc -l)
base_entities=$(find . -name "*Entity.java" -exec grep -l "extends BaseEntity" {} \; | wc -l)
inheritance_rate=$(( base_entities * 100 / total_entities ))
if [ $inheritance_rate -lt 90 ]; then
    echo "⚠️ BaseEntity继承率较低: ${inheritance_rate}%"
    # 仅警告，不阻断
fi

# 4. 生成检查报告
cat > prevention_report.md << EOF
# 预防性检查报告

## 📊 检查结果
- **检查时间**: $(date)
- **总体状态**: $([ "$check_passed" = true ] && echo "✅ 通过" || echo "❌ 失败")
- **发现问题**: $error_count 个

## 🔍 详细检查项
- **编译检查**: $([ $? -eq 0 ] && echo "✅ 通过" || echo "❌ 失败")
- **jakarta包名**: $javax_count 个违规
- **依赖注入**: $autowired_count 个@Autowired使用
- **架构规范**: $controller_dao_count 个违规
- **代码规范**: $system_out_count 个System.out使用
- **实体继承**: ${inheritance_rate}% 继承BaseEntity

## 📋 改进建议
EOF

if [ "$check_passed" = true ]; then
    echo "✅ 所有预防性检查通过"
    echo "项目质量良好，可以继续开发"
else
    echo "❌ 发现 $error_count 个问题，需要修复后再次检查"
    echo "详细信息请查看 prevention_report.md"
fi