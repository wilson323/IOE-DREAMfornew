#!/bin/bash

# 批量修复日志模式 - 将传统Logger转换为@Slf4j注解
# 作者: IOE-DREAM Team
# 版本: 1.0.0

echo "=== 批量修复日志模式 ==="
echo "将传统Logger转换为@Slf4j注解"

# 需要修复的文件列表
files=(
    "microservices/microservices-common-business/src/main/java/net/lab1024/sa/common/biometric/strategy/impl/AbstractAuthenticationStrategy.java"
    "microservices/microservices-common-business/src/main/java/net/lab1024/sa/common/biometric/strategy/impl/CardAuthenticationStrategy.java"
    "microservices/microservices-common-business/src/main/java/net/lab1024/sa/common/biometric/strategy/impl/FaceAuthenticationStrategy.java"
    "microservices/microservices-common-business/src/main/java/net/lab1024/sa/common/biometric/strategy/impl/FingerprintAuthenticationStrategy.java"
    "microservices/microservices-common-business/src/main/java/net/lab1024/sa/common/biometric/strategy/impl/IrisAuthenticationStrategy.java"
    "microservices/microservices-common-business/src/main/java/net/lab1024/sa/common/biometric/strategy/impl/NfcAuthenticationStrategy.java"
    "microservices/microservices-common-business/src/main/java/net/lab1024/sa/common/biometric/strategy/impl/PalmAuthenticationStrategy.java"
    "microservices/microservices-common-business/src/main/java/net/lab1024/sa/common/biometric/strategy/impl/PasswordAuthenticationStrategy.java"
    "microservices/microservices-common-business/src/main/java/net/lab1024/sa/common/biometric/strategy/impl/QrCodeAuthenticationStrategy.java"
    "microservices/microservices-common-business/src/main/java/net/lab1024/sa/common/biometric/strategy/impl/VoiceAuthenticationStrategy.java"
    "microservices/microservices-common-business/src/main/java/net/lab1024/sa/common/preference/manager/UserPreferenceManager.java"
)

# 统计信息
total_files=${#files[@]}
fixed_count=0

echo "总共需要修复的文件数量: $total_files"
echo ""

# 逐个修复文件
for file in "${files[@]}"; do
    if [ -f "$file" ]; then
        echo "正在修复: $file"

        # 1. 替换import语句
        sed -i.bak 's/import org\.slf4j\.Logger;/import lombok.extern.slf4j.Slf4j;/g' "$file"
        sed -i.bak 's/import org\.slf4j\.LoggerFactory;//g' "$file"

        # 2. 删除Logger声明行
        sed -i.bak '/private static final Logger log = LoggerFactory\.getLogger.*\.class);/d' "$file"

        # 3. 在类声明前添加@Slf4j注解
        # 查找第一个public class声明并在其前添加@Slf4j
        sed -i.bak '/^public class/ i \@Slf4j' "$file"

        echo "✓ 修复完成"
        rm -f "$file.bak"
        ((fixed_count++))
    else
        echo "✗ 文件不存在: $file"
    fi
    echo ""
done

echo "=== 修复完成 ==="
echo "修复的文件数量: $fixed_count/$total_files"
echo ""

# 验证修复结果
echo "=== 验证修复结果 ==="
error_count=0

for file in "${files[@]}"; do
    if [ -f "$file" ]; then
        if grep -q "import org.slf4j.Logger" "$file"; then
            echo "✗ 仍有Logger导入: $file"
            ((error_count++))
        fi

        if grep -q "LoggerFactory.getLogger" "$file"; then
            echo "✗ 仍有LoggerFactory使用: $file"
            ((error_count++))
        fi

        if ! grep -q "@Slf4j" "$file"; then
            echo "✗ 缺少@Slf4j注解: $file"
            ((error_count++))
        fi
    fi
done

if [ $error_count -eq 0 ]; then
    echo "✓ 所有文件验证通过！"
else
    echo "✗ 发现 $error_count 个问题需要手动修复"
fi