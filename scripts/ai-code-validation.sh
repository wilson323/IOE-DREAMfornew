#!/bin/bash

##############################################################################
# AI代码验证工具
# 
# 功能：验证AI生成的代码是否符合项目规范
# 用法：./scripts/ai-code-validation.sh
# 
# 检查层次：
# 1. 模板遵循度检查
# 2. 业务逻辑正确性检查
# 3. 测试覆盖检查
# 4. 文档完整性检查
##############################################################################

echo "🤖 AI代码生成验证工具"
echo "================================"

# 获取AI生成的文件（通过Git staged文件识别）
GENERATED_FILES=$(git diff --name-only --cached)

if [ -z "$GENERATED_FILES" ]; then
    echo "❌ 没有找到AI生成的文件（请先git add）"
    exit 1
fi

echo "📋 AI生成的文件清单："
echo "$GENERATED_FILES"
echo ""

ERROR_COUNT=0
WARNING_COUNT=0

##############################################################################
# 第1层检查：模板遵循度
##############################################################################
echo "🔍 第1层检查：模板遵循度"
echo "--------------------------------"

for file in $GENERATED_FILES; do
    # 只检查Java文件
    if [[ "$file" != *.java ]]; then
        continue
    fi
    
    # 检查Entity类
    if [[ "$file" == *"Entity.java" ]]; then
        echo "检查Entity: $file"
        
        # 必须有@Data注解
        if ! grep -q "@Data" "$file"; then
            echo "  ❌ 未使用@Data注解"
            ((ERROR_COUNT++))
        fi
        
        # 必须继承BaseEntity
        if ! grep -q "extends BaseEntity" "$file"; then
            echo "  ❌ 未继承BaseEntity"
            ((ERROR_COUNT++))
        fi
        
        # 不能重复定义审计字段
        if grep -q "private LocalDateTime createTime" "$file"; then
            echo "  ❌ 重复定义createTime字段（BaseEntity已包含）"
            ((ERROR_COUNT++))
        fi
        if grep -q "private LocalDateTime updateTime" "$file"; then
            echo "  ❌ 重复定义updateTime字段（BaseEntity已包含）"
            ((ERROR_COUNT++))
        fi
        if grep -q "private Long createUserId" "$file"; then
            echo "  ❌ 重复定义createUserId字段（BaseEntity已包含）"
            ((ERROR_COUNT++))
        fi
        if grep -q "private Integer deletedFlag" "$file"; then
            echo "  ❌ 重复定义deletedFlag字段（BaseEntity已包含）"
            ((ERROR_COUNT++))
        fi
    fi
    
    # 检查Controller类
    if [[ "$file" == *"Controller.java" ]]; then
        echo "检查Controller: $file"
        
        # 必须有@RestController注解
        if ! grep -q "@RestController" "$file"; then
            echo "  ❌ 未使用@RestController注解"
            ((ERROR_COUNT++))
        fi
        
        # 检查是否添加权限注解
        if ! grep -q "@SaCheckPermission" "$file"; then
            echo "  ⚠️  可能缺少@SaCheckPermission权限注解"
            ((WARNING_COUNT++))
        fi
        
        # 检查是否返回ResponseDTO
        if ! grep -q "ResponseDTO" "$file"; then
            echo "  ❌ Controller必须返回ResponseDTO"
            ((ERROR_COUNT++))
        fi
        
        # 检查是否直接注入DAO（违规）
        if grep -q "@Resource.*Dao" "$file"; then
            echo "  ❌ Controller禁止直接注入DAO层"
            ((ERROR_COUNT++))
        fi
        
        # 检查是否使用@Autowired（违规）
        if grep -q "@Autowired" "$file"; then
            echo "  ❌ 禁止使用@Autowired，请使用@Resource"
            ((ERROR_COUNT++))
        fi
    fi
    
    # 检查Service类
    if [[ "$file" == *"Service.java" ]]; then
        echo "检查Service: $file"
        
        # 必须有@Service注解
        if ! grep -q "@Service" "$file"; then
            echo "  ❌ 未使用@Service注解"
            ((ERROR_COUNT++))
        fi
        
        # 检查是否有事务注解
        if ! grep -q "@Transactional" "$file"; then
            echo "  ⚠️  Service可能缺少@Transactional事务注解"
            ((WARNING_COUNT++))
        fi
        
        # 检查是否使用@Autowired（违规）
        if grep -q "@Autowired" "$file"; then
            echo "  ❌ 禁止使用@Autowired，请使用@Resource"
            ((ERROR_COUNT++))
        fi
    fi
    
    # 检查DAO类
    if [[ "$file" == *"Dao.java" ]]; then
        echo "检查DAO: $file"
        
        # 必须有@Mapper注解
        if ! grep -q "@Mapper" "$file"; then
            echo "  ❌ 未使用@Mapper注解"
            ((ERROR_COUNT++))
        fi
        
        # 必须继承BaseMapper
        if ! grep -q "extends BaseMapper" "$file"; then
            echo "  ❌ 未继承BaseMapper"
            ((ERROR_COUNT++))
        fi
    fi
    
    # 通用检查：javax包使用
    if grep -q "import javax\." "$file"; then
        if ! grep -q "import javax.sql.DataSource" "$file"; then
            echo "  ❌ 使用了javax包，应使用jakarta包"
            ((ERROR_COUNT++))
        fi
    fi
    
    # 通用检查：System.out使用
    if grep -q "System\.out\.println" "$file"; then
        echo "  ❌ 禁止使用System.out.println，请使用SLF4J"
        ((ERROR_COUNT++))
    fi
done

##############################################################################
# 第2层检查：业务逻辑正确性
##############################################################################
echo ""
echo "🔍 第2层检查：业务逻辑正确性"
echo "--------------------------------"

for file in $GENERATED_FILES; do
    if [[ "$file" != *.java ]]; then
        continue
    fi
    
    # 检查Service类的业务逻辑
    if [[ "$file" == *"Service.java" ]]; then
        echo "检查业务逻辑: $file"
        
        # 检查是否有TODO标记（需要补充业务逻辑）
        todo_count=$(grep -c "// TODO" "$file" 2>/dev/null || echo "0")
        if [ "$todo_count" -gt 0 ]; then
            echo "  ⚠️  发现 $todo_count 处TODO标记，请补充完整业务逻辑"
            ((WARNING_COUNT++))
        fi
    fi
done

##############################################################################
# 第3层检查：测试覆盖
##############################################################################
echo ""
echo "🔍 第3层检查：测试覆盖"
echo "--------------------------------"

for file in $GENERATED_FILES; do
    if [[ "$file" == *.java && "$file" != *Test.java ]]; then
        # 检查是否有对应的测试文件
        test_file="${file%%.java}Test.java"
        if [ ! -f "$test_file" ]; then
            echo "⚠️  $file 缺少对应的测试文件"
            ((WARNING_COUNT++))
        fi
    fi
done

##############################################################################
# 第4层检查：文档完整性
##############################################################################
echo ""
echo "🔍 第4层检查：文档完整性"
echo "--------------------------------"

for file in $GENERATED_FILES; do
    if [[ "$file" != *.java ]]; then
        continue
    fi
    
    # 检查JavaDoc注释
    if ! grep -q "/\*\*" "$file"; then
        echo "⚠️  $file 可能缺少JavaDoc注释"
        ((WARNING_COUNT++))
    fi
    
    # 检查Controller的Swagger注解
    if [[ "$file" == *"Controller.java" ]]; then
        if ! grep -q "@Operation" "$file"; then
            echo "⚠️  $file 可能缺少Swagger @Operation注解"
            ((WARNING_COUNT++))
        fi
    fi
done

##############################################################################
# 总结报告
##############################################################################
echo ""
echo "================================"
echo "📊 AI代码验证结果："
echo "--------------------------------"
echo "错误: $ERROR_COUNT 个"
echo "警告: $WARNING_COUNT 个"
echo "================================"

if [ $ERROR_COUNT -eq 0 ]; then
    if [ $WARNING_COUNT -eq 0 ]; then
        echo "✅ AI代码验证完全通过！"
    else
        echo "⚠️  AI代码验证通过，但有 $WARNING_COUNT 个警告建议修复"
    fi
    exit 0
else
    echo "❌ AI代码验证失败，发现 $ERROR_COUNT 个错误，请修复后重新提交"
    echo ""
    echo "💡 修复建议："
    echo "  1. Entity类必须继承BaseEntity且不要重复定义审计字段"
    echo "  2. Controller必须使用@SaCheckPermission和返回ResponseDTO"
    echo "  3. 所有注入使用@Resource而非@Autowired"
    echo "  4. 使用jakarta.*包而非javax.*包"
    echo "  5. Service层必须添加@Transactional事务注解"
    exit 1
fi
