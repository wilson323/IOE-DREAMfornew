#!/bin/bash
echo "获取剩余编译错误详情..."

cd "D:\IOE-DREAM\smart-admin-api-java17-springboot3"
mvn clean compile 2>&1 > current_errors.txt 2>&1

# 提取编译错误信息
echo "=== 编译错误摘要 ==="
grep -c "ERROR" current_errors.txt

echo "=== 具体错误列表 ==="
grep -A 2 "cannot find symbol\|找不到符号" current_errors.txt | head -20

echo "=== 类结构错误 ==="
grep -A 1 "非法的类型开始\|需要class" current_errors.txt | head -10

echo "=== 缺少符号错误 ==="
grep -E "找不到符号.*:" current_errors.txt | head -10