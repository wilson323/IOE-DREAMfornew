#!/bin/bash
# 紧急修复脚本 - 解决ResponseDTO语法错误

echo "🔧 开始执行紧急修复..."

cd "D:/IOE-DREAM/smart-admin-api-java17-springboot3"

# 1. 备份问题文件
cp sa-base/src/main/java/net/lab1024/sa/base/common/domain/ResponseDTO.java sa-base/src/main/java/net/lab1024/sa/base/common/domain/ResponseDTO.java.backup

# 2. 修复ResponseDTO.java第121行的语法错误
sed -i '121s/.*/    public static <T> ResponseDTO<T> error(String msg) {/' sa-base/src/main/java/net/lab1024/sa/base/common/domain/ResponseDTO.java

# 3. 删除第122行的错误内容
sed -i '122d' sa-base/src/main/java/net/lab1024/sa/base/common/domain/ResponseDTO.java

echo "✅ ResponseDTO语法错误修复完成"

# 4. 修复剩余javax包问题
echo "🔧 开始修复javax包问题..."
find . -name "*.java" -exec sed -i 's/javax\.annotation/jakarta.annotation/g' {} \;
find . -name "*.java" -exec sed -i 's/javax\.validation/jakarta.validation/g' {} \;
find . -name "*.java" -exec sed -i 's/javax\.persistence/jakarta.persistence/g' {} \;
find . -name "*.java" -exec sed -i 's/javax\.servlet/jakarta.servlet/g' {} \;
echo "✅ javax包迁移完成"

# 5. 编译验证
echo "🔍 验证编译结果..."
mvn clean compile -q

if [ $? -eq 0 ]; then
    echo "🎉 编译成功！紧急修复完成"
else
    echo "❌ 编译仍有问题，需要进一步分析"
    mvn clean compile > detailed_error_report.txt 2>&1
    echo "详细错误已保存到 detailed_error_report.txt"
fi