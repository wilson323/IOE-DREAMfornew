#!/bin/bash

echo "🔧 快速修复剩余语法错误..."

# 删除所有重复的@Slf4j注解
find sa-admin/src/main/java -name "*.java" -exec sed -i '/^@Slf4j$/{
N
/\n@Slf4j$/d
}' {} \;

# 删除多余的空行
find sa-admin/src/main/java -name "*.java" -exec sed -i '/^$/{
N
/^@\n$/d
}' {} \;

echo "✅ 快速修复完成"
