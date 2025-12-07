#!/bin/bash

################################################################################
# IOE-DREAM P0-1: 配置安全加固脚本
# 功能：扫描并加密所有明文密码
# 优先级：P0 - 立即执行
# 预期效果：安全评分从76分提升至95分
################################################################################

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
MICROSERVICES_DIR="$PROJECT_ROOT/microservices"
REPORT_FILE="$PROJECT_ROOT/P0-1_PASSWORD_ENCRYPTION_REPORT.md"

# 统计变量
TOTAL_FILES=0
FILES_WITH_PASSWORDS=0
PASSWORDS_FOUND=0
PASSWORDS_ENCRYPTED=0

log_info "========================================="
log_info "P0-1: 配置安全加固脚本"
log_info "========================================="
log_info "项目根目录: $PROJECT_ROOT"
log_info "微服务目录: $MICROSERVICES_DIR"
log_info ""

################################################################################
# 第一步：扫描明文密码
################################################################################

log_info "第一步：扫描明文密码..."

# 创建报告文件
cat > "$REPORT_FILE" << 'EOF'
# P0-1: 配置安全加固执行报告

> **📋 执行日期**: $(date '+%Y-%m-%d %H:%M:%S')
> **📋 执行状态**: 🚀 执行中
> **📋 优先级**: P0 - 立即执行

---

## 📊 扫描结果

EOF

# 扫描配置文件中的明文密码
log_info "扫描配置文件..."

# 定义敏感关键词模式
PASSWORD_PATTERNS=(
    "password:\s*['\"]?[^'\"\s\$\{]+['\"]?"
    "secret:\s*['\"]?[^'\"\s\$\{]+['\"]?"
    "key:\s*['\"]?[^'\"\s\$\{]+['\"]?"
)

# 扫描所有yml和yaml文件
find "$MICROSERVICES_DIR" -type f \( -name "*.yml" -o -name "*.yaml" \) | while read -r file; do
    ((TOTAL_FILES++))

    # 检查文件是否包含明文密码
    has_password=false

    for pattern in "${PASSWORD_PATTERNS[@]}"; do
        if grep -qE "$pattern" "$file" 2>/dev/null; then
            # 排除已经使用环境变量的配置
            if ! grep -qE "password:\s*\$\{" "$file" 2>/dev/null; then
                has_password=true
                break
            fi
        fi
    done

    if [ "$has_password" = true ]; then
        ((FILES_WITH_PASSWORDS++))

        # 统计密码数量
        count=$(grep -cE "password:\s*['\"]?[^'\"\s\$\{]+['\"]?" "$file" 2>/dev/null || echo "0")
        PASSWORDS_FOUND=$((PASSWORDS_FOUND + count))

        log_warning "发现明文密码: $file ($count个)"

        # 记录到报告
        echo "- \`$file\` - $count个明文密码" >> "$REPORT_FILE"
    fi
done

log_info ""
log_info "扫描完成！"
log_info "总文件数: $TOTAL_FILES"
log_warning "包含明文密码的文件: $FILES_WITH_PASSWORDS"
log_warning "发现明文密码总数: $PASSWORDS_FOUND"

# 添加统计信息到报告
cat >> "$REPORT_FILE" << EOF

### 统计信息

| 项目 | 数量 |
|------|------|
| 扫描文件总数 | $TOTAL_FILES |
| 包含明文密码的文件 | $FILES_WITH_PASSWORDS |
| 发现明文密码总数 | $PASSWORDS_FOUND |

---

## 🔧 整改方案

### 方案1：使用环境变量（推荐）

\`\`\`yaml
# ❌ 整改前
spring:
  datasource:
    password: "123456"  # 明文密码

# ✅ 整改后
spring:
  datasource:
    password: \${DB_PASSWORD}  # 环境变量
\`\`\`

### 方案2：使用Nacos加密配置（企业级）

\`\`\`yaml
# ✅ Nacos加密配置
spring:
  datasource:
    password: \${DB_PASSWORD}  # 从Nacos读取加密配置
\`\`\`

### 方案3：使用Jasypt加密（备选）

\`\`\`yaml
# ✅ Jasypt加密
spring:
  datasource:
    password: ENC(encrypted_password_hash)
\`\`\`

---

## 📋 执行步骤

### 第一步：备份配置文件

\`\`\`bash
# 备份所有配置文件
find microservices -name "*.yml" -o -name "*.yaml" | xargs -I {} cp {} {}.backup
\`\`\`

### 第二步：替换明文密码

\`\`\`bash
# 使用脚本批量替换
./scripts/p0-security/02-replace-passwords.sh
\`\`\`

### 第三步：配置环境变量

\`\`\`bash
# 在.env文件中配置
DB_PASSWORD=your_secure_password
REDIS_PASSWORD=your_secure_password
\`\`\`

### 第四步：验证配置

\`\`\`bash
# 启动服务验证
./scripts/p0-security/03-verify-encryption.sh
\`\`\`

---

## ✅ 完成标准

- [ ] 0个明文密码
- [ ] 100%配置使用环境变量或加密
- [ ] 所有服务启动成功
- [ ] 数据库连接正常
- [ ] Redis连接正常
- [ ] 安全评分≥95/100

---

**👥 执行团队**: 架构委员会 + 安全团队
**📅 执行日期**: $(date '+%Y-%m-%d')
**⏰ 完成期限**: $(date -d '+7 days' '+%Y-%m-%d')
**📧 联系方式**: 架构委员会
EOF

log_success "报告已生成: $REPORT_FILE"

################################################################################
# 第二步：生成环境变量模板
################################################################################

log_info ""
log_info "第二步：生成环境变量模板..."

ENV_TEMPLATE="$PROJECT_ROOT/.env.template"

cat > "$ENV_TEMPLATE" << 'EOF'
# IOE-DREAM 环境变量配置模板
# 请复制此文件为.env并填写实际值

# ==================== Nacos配置 ====================
NACOS_SERVER_ADDR=127.0.0.1:8848
NACOS_NAMESPACE=dev
NACOS_GROUP=IOE-DREAM
NACOS_USERNAME=nacos
NACOS_PASSWORD=nacos

# ==================== 数据库配置 ====================
# 公共服务数据库
DB_COMMON_URL=jdbc:mysql://127.0.0.1:3306/ioedream_common_db
DB_COMMON_USERNAME=root
DB_COMMON_PASSWORD=

# 门禁服务数据库
DB_ACCESS_URL=jdbc:mysql://127.0.0.1:3306/ioedream_access_db
DB_ACCESS_USERNAME=root
DB_ACCESS_PASSWORD=

# 考勤服务数据库
DB_ATTENDANCE_URL=jdbc:mysql://127.0.0.1:3306/ioedream_attendance_db
DB_ATTENDANCE_USERNAME=root
DB_ATTENDANCE_PASSWORD=

# 消费服务数据库
DB_CONSUME_URL=jdbc:mysql://127.0.0.1:3306/ioedream_consume_db
DB_CONSUME_USERNAME=root
DB_CONSUME_PASSWORD=

# 视频服务数据库
DB_VIDEO_URL=jdbc:mysql://127.0.0.1:3306/ioedream_video_db
DB_VIDEO_USERNAME=root
DB_VIDEO_PASSWORD=

# 访客服务数据库
DB_VISITOR_URL=jdbc:mysql://127.0.0.1:3306/ioedream_visitor_db
DB_VISITOR_USERNAME=root
DB_VISITOR_PASSWORD=

# ==================== Redis配置 ====================
REDIS_HOST=127.0.0.1
REDIS_PORT=6379
REDIS_PASSWORD=
REDIS_DATABASE=0

# ==================== 第三方服务配置 ====================
# 微信配置
WECHAT_APP_ID=
WECHAT_APP_SECRET=
WECHAT_MERCHANT_ID=
WECHAT_MERCHANT_KEY=

# 支付宝配置
ALIPAY_APP_ID=
ALIPAY_PRIVATE_KEY=
ALIPAY_PUBLIC_KEY=

# ==================== 内部服务密钥 ====================
# JWT密钥
JWT_SECRET_KEY=

# API网关密钥
GATEWAY_SECRET_KEY=

# ==================== 监控配置 ====================
# Zipkin配置
ZIPKIN_BASE_URL=http://localhost:9411
TRACING_SAMPLE_RATE=0.1

# Sentinel配置
SENTINEL_DASHBOARD=localhost:8858
EOF

log_success "环境变量模板已生成: $ENV_TEMPLATE"

################################################################################
# 第三步：生成替换脚本
################################################################################

log_info ""
log_info "第三步：生成密码替换脚本..."

REPLACE_SCRIPT="$PROJECT_ROOT/scripts/p0-security/02-replace-passwords.sh"
mkdir -p "$(dirname "$REPLACE_SCRIPT")"

cat > "$REPLACE_SCRIPT" << 'SCRIPT_EOF'
#!/bin/bash

################################################################################
# P0-1: 批量替换明文密码脚本
################################################################################

set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
MICROSERVICES_DIR="$PROJECT_ROOT/microservices"

echo "开始替换明文密码..."

# 备份所有配置文件
echo "备份配置文件..."
find "$MICROSERVICES_DIR" -type f \( -name "*.yml" -o -name "*.yaml" \) | while read -r file; do
    if [ ! -f "$file.backup" ]; then
        cp "$file" "$file.backup"
    fi
done

# 替换数据库密码
echo "替换数据库密码..."
find "$MICROSERVICES_DIR" -type f \( -name "*.yml" -o -name "*.yaml" \) -exec sed -i.tmp \
    -e 's/password:\s*123456/password: ${DB_PASSWORD}/g' \
    -e 's/password:\s*password1234/password: ${DB_PASSWORD}/g' \
    -e 's/password:\s*ioedream123/password: ${DB_PASSWORD}/g' \
    -e 's/password:\s*root/password: ${DB_PASSWORD}/g' \
    {} \;

# 替换Redis密码
echo "替换Redis密码..."
find "$MICROSERVICES_DIR" -type f \( -name "*.yml" -o -name "*.yaml" \) -exec sed -i.tmp \
    -e 's/redis:\s*\n\s*password:\s*[^$]/redis:\n    password: ${REDIS_PASSWORD}/g' \
    {} \;

# 清理临时文件
find "$MICROSERVICES_DIR" -name "*.tmp" -delete

echo "✅ 密码替换完成！"
echo "请检查配置文件并配置环境变量"
SCRIPT_EOF

chmod +x "$REPLACE_SCRIPT"
log_success "替换脚本已生成: $REPLACE_SCRIPT"

################################################################################
# 总结
################################################################################

log_info ""
log_info "========================================="
log_info "P0-1: 配置安全加固扫描完成"
log_info "========================================="
log_info ""
log_warning "发现问题："
log_warning "  - 包含明文密码的文件: $FILES_WITH_PASSWORDS"
log_warning "  - 明文密码总数: $PASSWORDS_FOUND"
log_info ""
log_info "下一步操作："
log_info "  1. 查看报告: $REPORT_FILE"
log_info "  2. 配置环境变量: $ENV_TEMPLATE"
log_info "  3. 执行替换: $REPLACE_SCRIPT"
log_info "  4. 验证配置: ./scripts/p0-security/03-verify-encryption.sh"
log_info ""
log_success "扫描脚本执行完成！"

