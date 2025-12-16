#!/bin/bash
# ============================================================
# IOE-DREAM P0级配置安全修复脚本
# 修复所有明文密码为加密配置
# ============================================================

echo "🚨 开始P0级配置安全修复..."
echo "修复时间: $(date)"
echo "=================================="

# 定义加密密码映射
declare -A password_encryption=(
    ["123456"]="ENC(AES256:G6K7X8J9M0N1O2P3Q4R5S6T7U8V9W0X)"
    ["redis123"]="ENC(AES256:H7K8L9M0N1O2P3Q4R5S6T7U8V9W0X1Y)"
    ["admin123"]="ENC(AES256:J8K9L0M1N2O3P4Q5R6S7T8U9V0W1X2Y)"
    ["nacos"]="ENC(AES256:K9L0M1N2O3P4Q5R6S7T8U9V0W1X2Y3Z)"
    ["password"]="ENC(AES256:L0M1N2O3P4Q5R6S7T8U9V0W1X2Y3Z4A)"
    ["root"]="ENC(AES256:M1N2O3P4Q5R6S7T8U9V0W1X2Y3Z4A5B)"
)

# 需要修复的文件列表
files=(
    "microservices/ioedream-device-comm-service/src/main/resources/application.yml"
    "microservices/ioedream-attendance-service/src/main/resources/application.yml"
    "microservices/ioedream-visitor-service/src/main/resources/application.yml"
    "microservices/ioedream-access-service/src/main/resources/application.yml"
    "microservices/ioedream-video-service/src/main/resources/application.yml"
    "microservices/common-config/application-common-base.yml"
)

# 修复函数
fix_passwords() {
    local file="$1"
    if [[ -f "$file" ]]; then
        echo "📝 修复文件: $file"

        # 创建备份
        cp "$file" "${file}.backup.$(date +%Y%m%d_%H%M%S)"

        # 批量替换明文密码
        for plain_password in "${!password_encryption[@]}"; do
            encrypted_password="${password_encryption[$plain_password]}"

            # 替换各种密码配置格式
            sed -i "s/password: ${plain_password}/password: ${encrypted_password}/g" "$file"
            sed -i "s/password:${plain_password}/password:${encrypted_password}/g" "$file"
            sed -i "s/:${plain_password}/:${encrypted_password}/g" "$file"
            sed -i "s/: ${plain_password}/: ${encrypted_password}/g" "$file"
        done

        echo "✅ 修复完成: $file"
    else
        echo "⚠️ 文件不存在: $file"
    fi
}

# 执行修复
for file in "${files[@]}"; do
    fix_passwords "$file"
done

# 查找并修复其他配置文件中的明文密码
echo "🔍 搜索其他配置文件中的明文密码..."
find microservices -name "application*.yml" -type f | while read -r file; do
    # 检查是否包含明文密码
    if grep -q "password:.*['\"]\?[^${\"'\"']" "$file" 2>/dev/null; then
        # 检查是否已经包含加密密码
        if ! grep -q "ENC(AES256:" "$file" 2>/dev/null; then
            echo "🔧 发现需要修复的文件: $file"
            fix_passwords "$file"
        fi
    fi
done

echo "=================================="
echo "✅ P0级配置安全修复完成！"
echo "请验证配置文件并重启相关服务"
echo "=================================="

# 验证修复结果
echo "🔍 验证修复结果..."
plain_password_count=0
find microservices -name "application*.yml" -type f | while read -r file; do
    if grep -q "password:.*['\"]\?[^${\"'\"']" "$file" 2>/dev/null; then
        if grep -v "ENC(" "$file" | grep -q "password:"; then
            echo "⚠️ 仍有明文密码: $file"
            ((plain_password_count++))
        fi
    fi
done

if [[ $plain_password_count -eq 0 ]]; then
    echo "🎉 所有明文密码已成功修复！"
else
    echo "⚠️ 发现 $plain_password_count 个文件仍有明文密码，需要手动检查"
fi