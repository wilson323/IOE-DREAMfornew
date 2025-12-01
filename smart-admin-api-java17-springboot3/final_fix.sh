#!/bin/bash

echo "🔧 最终修复步骤..."

# 1. 清理编译缓存
echo "清理编译缓存..."
mvn clean -q

# 2. 先编译基础模块
echo "编译基础模块..."
cd sa-base && mvn compile -DskipTests -q
cd ../sa-support && mvn compile -DskipTests -q
cd ..

# 3. 检查类是否能找到
echo "检查关键类..."
find . -name "BiometricEngineStatus.java" | head -1
find . -name "TemplateRegistrationRequest.java" | head -1

# 4. 尝试编译sa-admin
echo "尝试编译sa-admin..."
cd sa-admin && mvn compile -DskipTests -q

echo "✅ 最终修复完成"
