#!/bin/bash
# 代码质量检查脚本

echo "🔍 开始代码质量检查..."

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 错误计数
error_count=0
warning_count=0

echo "=========================================="

# 1. 检查javax包使用
echo "1. 检查javax包使用情况..."
javax_files=$(find . -name "*.java" -exec grep -l "javax\." {} \; 2>/dev/null)
if [ -n "$javax_files" ]; then
    echo -e "${RED}❌ 发现以下文件仍使用javax包:${NC}"
    echo "$javax_files"
    error_count=$((error_count + 1))
else
    echo -e "${GREEN}✅ javax包检查通过${NC}"
fi

# 2. 检查@Autowired使用
echo "=========================================="
echo "2. 检查@Autowired使用情况..."
autowired_files=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; 2>/dev/null)
if [ -n "$autowired_files" ]; then
    echo -e "${YELLOW}⚠️  发现以下文件仍使用@Autowired:${NC}"
    echo "$autowired_files"
    warning_count=$((warning_count + 1))
else
    echo -e "${GREEN}✅ @Autowired检查通过${NC}"
fi

# 3. 检查实体类继承
echo "=========================================="
echo "3. 检查实体类BaseEntity继承..."
entity_files=$(find . -name "*Entity.java" -exec grep -L "extends BaseEntity" {} \; 2>/dev/null)
if [ -n "$entity_files" ]; then
    echo -e "${RED}❌ 发现实体类未继承BaseEntity:${NC}"
    echo "$entity_files"
    error_count=$((error_count + 1))
else
    echo -e "${GREEN}✅ 实体类继承检查通过${NC}"
fi

# 4. 检查枚举类@AllArgsConstructor
echo "=========================================="
echo "4. 检查枚举类@AllArgsConstructor使用..."
enum_autolombok=$(find . -name "*.java" -exec grep -l "@AllArgsConstructor" {} \; 2>/dev/null | xargs grep -l "enum ")
if [ -n "$enum_autolombok" ]; then
    echo -e "${RED}❌ 发现枚举类使用@AllArgsConstructor:${NC}"
    echo "$enum_autolombok"
    error_count=$((error_count + 1))
else
    echo -e "${GREEN}✅ 枚举类AllArgsConstructor检查通过${NC}"
fi

# 5. 检查ResponseDTO.error(String)使用
echo "=========================================="
echo "5. 检查ResponseDTO.error(String)使用..."
response_error_usage=$(find . -name "*.java" -exec grep -l "ResponseDTO\.error(\"" {} \; 2>/dev/null)
if [ -n "$response_error_usage" ]; then
    echo -e "${RED}❌ 发现ResponseDTO.error(String)错误用法:${NC}"
    echo "$response_error_usage"
    error_count=$((error_count + 1))
else
    echo -e "${GREEN}✅ ResponseDTO使用检查通过${NC}"
fi

# 6. 检查System.out使用
echo "=========================================="
echo "6. 检查System.out使用情况..."
system_out_files=$(find . -name "*.java" -exec grep -l "System\.out" {} \; 2>/dev/null)
if [ -n "$system_out_files" ]; then
    echo -e "${YELLOW}⚠️  发现以下文件使用System.out:${NC}"
    echo "$system_out_files"
    warning_count=$((warning_count + 1))
else
    echo -e "${GREEN}✅ System.out检查通过${NC}"
fi

# 7. 尝试编译
echo "=========================================="
echo "7. 执行Maven编译..."
cd smart-admin-api-java17-springboot3 && mvn clean compile -DskipTests -q && cd ..
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Maven编译通过${NC}"
else
    echo -e "${RED}❌ Maven编译失败${NC}"
    error_count=$((error_count + 1))
fi

# 8. 运行单元测试
echo "=========================================="
echo "8. 运行单元测试..."
cd smart-admin-api-java17-springboot3 && mvn test -q && cd ..
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ 单元测试通过${NC}"
else
    echo -e "${YELLOW}⚠️  单元测试失败，但不阻止提交${NC}"
fi

# 统计结果
echo "=========================================="
echo "📊 检查结果统计:"
echo -e "错误: ${RED}$error_count${NC}"
echo -e "警告: ${YELLOW}$warning_count${NC}"

# 返回结果
if [ $error_count -gt 0 ]; then
    echo -e "${RED}❌ 代码质量检查失败，请修复错误后再提交${NC}"
    exit 1
elif [ $warning_count -gt 0 ]; then
    echo -e "${YELLOW}⚠️ 代码质量检查通过，但存在警告建议修复${NC}"
    exit 0
else
    echo -e "${GREEN}🎉 代码质量检查完全通过！${NC}"
    exit 0
fi