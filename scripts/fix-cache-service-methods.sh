#!/bin/bash

echo "🔧 开始修复缓存服务方法调用不匹配问题..."

cd smart-admin-api-java17-springboot3

# 查找所有使用旧缓存接口的文件
echo "🔍 查找需要修复的文件..."

# 修复模式1: cacheService.get(key, Class<T>) -> cacheService.get(CacheModule, namespace, key, Class<T>)
echo "修复模式1: get方法调用"
find . -name "*.java" -exec grep -l "cacheService\.get(" {} \; | while read file; do
    echo "处理文件: $file"

    # 备份原文件
    cp "$file" "$file.backup"

    # 使用sed修复方法调用
    sed -i 's/cacheService\.get(\([^,]*\), \([^)]*\))/cacheService.get(CacheModule.SYSTEM, "device", \1, \2)/g' "$file"

    # 检查修复结果
    if grep -q "cacheService\.get(" "$file"; then
        echo "⚠️  $file 可能仍有未修复的get调用"
    fi
done

# 修复模式2: cacheService.set(key, value, ttl, TimeUnit) -> cacheService.set(CacheModule, namespace, key, value, ttl, TimeUnit)
echo "修复模式2: set方法调用"
find . -name "*.java" -exec grep -l "cacheService\.set(" {} \; | while read file; do
    echo "处理文件: $file"

    # 备份原文件
    cp "$file" "$file.backup"

    # 修复4参数set方法
    sed -i 's/cacheService\.set(\([^,]*\), \([^,]*\), \([^,]*\), \([^)]*\))/cacheService.set(CacheModule.SYSTEM, "device", \1, \2, \3, \4)/g' "$file"

    # 修复3参数set方法
    sed -i 's/cacheService\.set(\([^,]*\), \([^,]*\), \([^)]*\))/cacheService.set(CacheModule.SYSTEM, "device", \1, \2, \3)/g' "$file"

    # 检查修复结果
    if grep -q "cacheService\.set(" "$file"; then
        echo "⚠️  $file 可能仍有未修复的set调用"
    fi
done

echo "✅ 缓存服务方法调用修复完成"
echo ""
echo "📋 修复后需要手动检查的文件："
echo "  - UnifiedDeviceManagerImpl.java"
echo "  - 其他使用了cacheService的文件"
echo ""
echo "🧪 验证修复结果："
echo "  mvn clean compile -DskipTests"