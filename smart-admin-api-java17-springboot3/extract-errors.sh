#!/bin/bash
echo "=== 提取编译错误信息 ==="
echo "正在尝试编译并提取具体错误..."

# 尝试编译并提取错误
cd sa-base
javac -cp "$(mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q):target/classes" \
    $(find src/main/java -name "*.java") 2>&1 | grep -E "error:|ERROR|cannot find symbol" | head -20

echo ""
echo "=== 检查是否有 Java 编译器可用 ==="
java -version
javac -version