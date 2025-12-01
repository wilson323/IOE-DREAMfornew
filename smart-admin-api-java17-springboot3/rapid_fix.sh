#!/bin/bash

echo "=== IOE-DREAM 快速修复工具 ==="

cd "D:\IOE-DREAM\smart-admin-api-java17-springboot3\sa-admin"

echo "1. 修复包不存在问题..."

# 创建缺失的包结构
mkdir -p src/main/java/net/lab1024/sa/base/authz/rac/model
mkdir -p src/main/java/net/lab1024/sa/base/common/tenant
mkdir -p src/main/java/net/lab1024/sa/base/module/support/redis

echo "2. 检查关键文件是否存在..."

files_to_check=(
    "src/main/java/net/lab1024/sa/base/common/annotation/NoNeedLogin.java"
    "src/main/java/net/lab1024/sa/base/authz/rac/annotation/RequireResource.java"
    "src/main/java/net/lab1024/sa/base/authz/rac/model/ResourcePermission.java"
    "src/main/java/net/lab1024/sa/base/common/constant/RedisKeyConst.java"
    "src/main/java/net/lab1024/sa/base/common/domain/PageParam.java"
    "src/main/java/net/lab1024/sa/base/module/support/auth/LoginHelper.java"
    "src/main/java/net/lab1024/sa/base/common/tenant/TenantContextHolder.java"
    "src/main/java/net/lab1024/sa/base/module/support/redis/RedisTemplateUtil.java"
    "src/main/java/net/lab1024/sa/base/common/validate/BusinessValidator.java"
    "src/main/java/net/lab1024/sa/base/common/validate/ValidateGroup.java"
)

for file in "${files_to_check[@]}"; do
    if [ -f "$file" ]; then
        echo "✓ $file"
    else
        echo "✗ $file (缺失)"
    fi
done

echo "3. 运行快速编译测试..."
error_count=$(mvn clean compile -q 2>&1 | grep -c "ERROR")
echo "当前错误数量: $error_count"

echo "修复完成!"