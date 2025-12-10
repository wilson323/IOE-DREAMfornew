#!/bin/bash

# ============================================================
# IOE-DREAM Jasypt 加密工具配置脚本
#
# @Author:    IOE-DREAM Team
# @Date:      2025-12-09
# @Description: 配置Jasypt加密工具，解决明文密码安全问题
# ============================================================

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

# 检查Java环境
check_java() {
    log_info "检查Java环境..."
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -n1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$JAVA_VERSION" -ge 11 ]; then
            log_success "Java环境检查通过，版本: $(java -version 2>&1 | head -n1)"
        else
            log_error "Java版本过低，需要Java 11+，当前版本: $(java -version 2>&1 | head -n1)"
            exit 1
        fi
    else
        log_error "Java未安装或未配置到PATH"
        exit 1
    fi
}

# 检查Maven环境
check_maven() {
    log_info "检查Maven环境..."
    if command -v mvn &> /dev/null; then
        log_success "Maven环境检查通过，版本: $(mvn -version | head -n1)"
    else
        log_error "Maven未安装或未配置到PATH"
        exit 1
    fi
}

# 生成加密密钥
generate_encryption_key() {
    log_info "生成Jasypt加密密钥..."

    # 生成256位（32字节）的加密密钥
    ENCRYPTION_KEY=$(openssl rand -hex 32)

    if [ $? -eq 0 ]; then
        log_success "加密密钥生成成功"
        echo "$ENCRYPTION_KEY" > .jasypt-encryption-key
        chmod 600 .jasypt-encryption-key
        log_info "密钥已保存到 .jasypt-encryption-key 文件"
        echo "加密密钥: $ENCRYPTION_KEY"
    else
        log_error "加密密钥生成失败"
        exit 1
    fi
}

# 下载Jasypt CLI工具
download_jasypt_cli() {
    log_info "下载Jasypt CLI工具..."

    JASYPT_VERSION="3.0.5"
    JASYPT_CLI_JAR="jasypt-cli-${JASYPT_VERSION}.jar"
    JASYPT_CLI_URL="https://repo1.maven.org/maven2/org/jasypt/jasypt/${JASYPT_VERSION}/${JASYPT_CLI_JAR}"

    if [ ! -f "$JASYPT_CLI_JAR" ]; then
        log_info "下载Jasypt CLI工具..."
        curl -L -o "$JASYPT_CLI_JAR" "$JASYPT_CLI_URL"

        if [ $? -eq 0 ]; then
            log_success "Jasypt CLI工具下载完成"
        else
            log_error "Jasypt CLI工具下载失败"
            exit 1
        fi
    else
        log_info "Jasypt CLI工具已存在，跳过下载"
    fi
}

# 创建加密脚本
create_encryption_script() {
    log_info "创建密码加密脚本..."

    cat > encrypt-password.sh << 'EOF'
#!/bin/bash

# IOE-DREAM 密码加密脚本
# 使用方法: ./encrypt-password.sh "要加密的密码"

if [ $# -eq 0 ]; then
    echo "使用方法: $0 \"要加密的密码\""
    exit 1
fi

PLAIN_PASSWORD="$1"
JASYPT_VERSION="3.0.5"
JASYPT_CLI_JAR="jasypt-cli-${JASYPT_VERSION}.jar"

# 读取加密密钥
if [ ! -f ".jasypt-encryption-key" ]; then
    echo "错误: 找不到加密密钥文件 .jasypt-encryption-key"
    exit 1
fi

ENCRYPTION_KEY=$(cat .jasypt-encryption-key)

# 执行加密
ENCRYPTED_PASSWORD=$(java -cp "$JASYPT_CLI_JAR" org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI \
    input="$PLAIN_PASSWORD" \
    password="$ENCRYPTION_KEY" \
    algorithm="PBEWITHHMACSHA512ANDAES_256")

if [ $? -eq 0 ]; then
    echo "原始密码: $PLAIN_PASSWORD"
    echo "加密结果: ENC($ENCRYPTED_PASSWORD)"
    echo ""
    echo "请在配置文件中使用: ENC($ENCRYPTED_PASSWORD)"
else
    echo "加密失败"
    exit 1
fi
EOF

    chmod +x encrypt-password.sh
    log_success "密码加密脚本创建完成: encrypt-password.sh"
}

# 创建解密脚本
create_decryption_script() {
    log_info "创建密码解密脚本..."

    cat > decrypt-password.sh << 'EOF'
#!/bin/bash

# IOE-DREAM 密码解密脚本
# 使用方法: ./decrypt-password.sh "ENC(加密后的密码)"

if [ $# -eq 0 ]; then
    echo "使用方法: $0 \"ENC(加密后的密码)\""
    exit 1
fi

ENCRYPTED_INPUT="$1"

# 提取ENC()中的内容
PLAIN_ENCRYPTED=$(echo "$ENCRYPTED_INPUT" | sed 's/ENC(\(.*\))/\1/')

JASYPT_VERSION="3.0.5"
JASYPT_CLI_JAR="jasypt-cli-${JASYPT_VERSION}.jar"

# 读取加密密钥
if [ ! -f ".jasypt-encryption-key" ]; then
    echo "错误: 找不到加密密钥文件 .jasypt-encryption-key"
    exit 1
fi

ENCRYPTION_KEY=$(cat .jasypt-encryption-key)

# 执行解密
DECRYPTED_PASSWORD=$(java -cp "$JASYPT_CLI_JAR" org.jasypt.intf.cli.JasyptPBEStringDecryptionCLI \
    input="$PLAIN_ENCRYPTED" \
    password="$ENCRYPTION_KEY" \
    algorithm="PBEWITHHMACSHA512ANDAES_256")

if [ $? -eq 0 ]; then
    echo "加密输入: $ENCRYPTED_INPUT"
    echo "解密结果: $DECRYPTED_PASSWORD"
else
    echo "解密失败"
    exit 1
fi
EOF

    chmod +x decrypt-password.sh
    log_success "密码解密脚本创建完成: decrypt-password.sh"
}

# 创建批量加密脚本
create_batch_encryption_script() {
    log_info "创建批量密码加密脚本..."

    cat > batch-encrypt.sh << 'EOF'
#!/bin/bash

# IOE-DREAM 批量密码加密脚本
# 用于加密常见的数据库连接密码

JASYPT_VERSION="3.0.5"
JASYPT_CLI_JAR="jasypt-cli-${JASYPT_VERSION}.jar"

# 读取加密密钥
if [ ! -f ".jasypt-encryption-key" ]; then
    echo "错误: 找不到加密密钥文件 .jasypt-encryption-key"
    exit 1
fi

ENCRYPTION_KEY=$(cat .jasypt-encryption-key)

# 要加密的密码列表（根据实际情况修改）
declare -A PASSWORDS=(
    ["数据库root密码"]="your_root_password_here"
    ["数据库应用密码"]="your_app_password_here"
    ["Redis密码"]="your_redis_password_here"
    ["Druid监控密码"]="your_druid_password_here"
    ["JWT密钥"]="your_jwt_secret_here"
    ["SMTP密码"]="your_smtp_password_here"
    ["短信API密钥"]="your_sms_key_here"
    ["MinIO密钥"]="your_minio_key_here"
    ["支付宝密钥"]="your_alipay_key_here"
    ["微信支付密钥"]="your_wechat_key_here"
)

echo "开始批量加密密码..."
echo ""

# 加密每个密码
for desc in "${!PASSWORDS[@]}"; do
    password="${PASSWORDS[$desc]}"

    # 如果密码是占位符，跳过加密
    if [[ "$password" =~ _here$ ]]; then
        echo "[$desc] 占位符密码，跳过加密"
        continue
    fi

    encrypted=$(java -cp "$JASYPT_CLI_JAR" org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI \
        input="$password" \
        password="$ENCRYPTION_KEY" \
        algorithm="PBEWITHHMACSHA512ANDAES_256" 2>/dev/null)

    if [ $? -eq 0 ]; then
        echo "[$desc]"
        echo "原始: $password"
        echo "加密: ENC($encrypted)"
        echo ""
    else
        echo "[$desc] 加密失败"
        echo ""
    fi
done

echo "批量加密完成！"
echo "请在配置文件中替换为对应的 ENC() 加密值。"
EOF

    chmod +x batch-encrypt.sh
    log_success "批量密码加密脚本创建完成: batch-encrypt.sh"
}

# 创建环境变量配置文件
create_env_config() {
    log_info "创建环境变量配置文件..."

    cat > .env.jasypt << 'EOF'
# Jasypt 加密配置环境变量
# 请在部署时设置这些环境变量

# Jasypt加密密钥（生产环境必须设置）
export JASYPT_PASSWORD=your_encryption_key_here

# 可选：Jasypt算法
export JASYPT_ENCRYPTOR_ALGORITHM=PBEWITHHMACSHA512ANDAES_256

# 可选：密钥获取迭代次数
export JASYPT_ENCRYPTOR_KEY_OBTENTION_ITERATIONS=1000

# 可选：盐生成器类
export JASYPT_ENCRYPTOR_SALT_GENERATOR_CLASSNAME=org.jasypt.salt.RandomSaltGenerator

# 可选：IV生成器类
export JASYPT_ENCRYPTOR_IV_GENERATOR_CLASSNAME=org.jasypt.iv.RandomIvGenerator

# 可选：字符串输出类型
export JASYPT_ENCRYPTOR_STRING_OUTPUT_TYPE=base64
EOF

    log_success "环境变量配置文件创建完成: .env.jasypt"
    log_warning "请编辑 .env.jasypt 文件，设置实际的加密密钥"
}

# 创建Spring Boot启动配置
create_spring_boot_config() {
    log_info "创建Spring Boot Jasypt配置示例..."

    cat > jasypt-spring-config.yml << 'EOF'
# ============================================================
# Spring Boot Jasypt 配置示例
# 复制到你的 application.yml 或 application-prod.yml 中
# ============================================================

jasypt:
  encryptor:
    # 从环境变量读取加密密钥
    password: ${JASYPT_PASSWORD}

    # 加密算法
    algorithm: PBEWITHHMACSHA512ANDAES_256

    # 密钥获取迭代次数
    key-obtention-iterations: 1000

    # 池大小
    pool-size: 1

    # 提供者名称
    provider-name: SunJCE

    # 盐生成器类名
    salt-generator-classname: org.jasypt.salt.RandomSaltGenerator

    # IV生成器类名
    iv-generator-classname: org.jasypt.iv.RandomIvGenerator

    # 字符串输出类型
    string-output-type: base64

    # 属性前缀和后缀
    property:
      prefix: "ENC("
      suffix: ")"

# 应用示例
spring:
  # 使用加密的数据库配置
  datasource:
    url: jdbc:mysql://localhost:3306/ioedream?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: ENC(这里放加密后的数据库密码)

  # 使用加密的Redis配置
  redis:
    host: localhost
    port: 6379
    password: ENC(这里放加密后的Redis密码)
EOF

    log_success "Spring Boot Jasypt配置示例创建完成: jasypt-spring-config.yml"
}

# 创建Maven依赖配置
create_maven_dependency() {
    log_info "创建Maven Jasypt依赖配置..."

    cat > jasypt-maven-dependency.xml << 'EOF'
<!-- ============================================================
        Jasypt Maven 依赖配置
        复制到你的 pom.xml 文件中的 <dependencies> 部分
        ============================================================ -->

<!-- Jasypt 加密依赖 -->
<dependency>
    <groupId>com.github.ulisesbocchio</groupId>
    <artifactId>jasypt-spring-boot-starter</artifactId>
    <version>3.0.5</version>
</dependency>

<!-- 可选：如果你需要更高级的加密功能 -->
<dependency>
    <groupId>org.jasypt</groupId>
    <artifactId>jasypt</artifactId>
    <version>1.9.3</version>
</dependency>
EOF

    log_success "Maven依赖配置创建完成: jasypt-maven-dependency.xml"
}

# 创建使用说明文档
create_usage_guide() {
    log_info "创建使用说明文档..."

    cat > JASYPT_USAGE_GUIDE.md << 'EOF'
# Jasypt 加密工具使用指南

## 概述

Jasypt是一个Java加密库，用于简化应用程序中的加密和解密操作。在Spring Boot应用中，Jasypt可以加密配置文件中的敏感信息（如数据库密码、API密钥等），避免在配置文件中明文存储。

## 快速开始

### 1. 加密单个密码

```bash
./encrypt-password.sh "你的密码"
```

输出示例：
```
原始密码: mypassword123
加密结果: ENC(G6K7X8J9M0N1O2P3Q4R5S6T7U8V9W0X)
请在配置文件中使用: ENC(G6K7X8J9M0N1O2P3Q4R5S6T7U8V9W0X)
```

### 2. 批量加密密码

编辑 `batch-encrypt.sh` 文件中的密码列表，然后运行：

```bash
./batch-encrypt.sh
```

### 3. 解密密码

```bash
./decrypt-password.sh "ENC(加密后的密码)"
```

## 配置文件使用

在Spring Boot配置文件中使用加密后的密码：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ioedream
    username: root
    password: ENC(G6K7X8J9M0N1O2P3Q4R5S6T7U8V9W0X)
```

## 环境变量配置

在生产环境中，通过环境变量设置加密密钥：

```bash
export JASYPT_PASSWORD=your_encryption_key_here
java -jar your-app.jar
```

或者创建 `.env.jasypt` 文件：

```bash
source .env.jasypt
java -jar your-app.jar
```

## Maven依赖

在 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>com.github.ulisesbocchio</groupId>
    <artifactId>jasypt-spring-boot-starter</artifactId>
    <version>3.0.5</version>
</dependency>
```

## 安全最佳实践

1. **加密密钥管理**
   - 不要将加密密钥提交到代码仓库
   - 使用环境变量或安全的密钥管理服务
   - 定期轮换加密密钥

2. **密码安全**
   - 使用强密码策略
   - 不同环境使用不同的密码
   - 定期更新敏感配置

3. **配置安全**
   - 生产环境配置文件不要包含明文密码
   - 使用最小权限原则
   - 定期审计配置文件

## 故障排除

### 1. 解密失败

- 检查加密密钥是否正确
- 确认加密算法配置一致
- 验证ENC()格式是否正确

### 2. 启动失败

- 确认Jasypt依赖已正确添加
- 检查环境变量是否设置
- 验证配置文件格式

### 3. 性能问题

- 考虑缓存解密结果
- 优化加密算法选择
- 减少不必要的加密操作

## 更多信息

- [Jasypt官方文档](https://github.com/ulisesbocchio/jasypt)
- [Spring Boot Jasypt集成](https://github.com/ulisesbocchio/jasypt-spring-boot)
EOF

    log_success "使用说明文档创建完成: JASYPT_USAGE_GUIDE.md"
}

# 主函数
main() {
    echo "=============================================================="
    echo "🔐 IOE-DREAM Jasypt 加密工具配置"
    echo "=============================================================="
    echo ""

    log_info "开始配置Jasypt加密工具..."

    # 检查环境
    check_java
    check_maven

    # 生成加密密钥
    generate_encryption_key

    # 下载工具
    download_jasypt_cli

    # 创建脚本
    create_encryption_script
    create_decryption_script
    create_batch_encryption_script

    # 创建配置文件
    create_env_config
    create_spring_boot_config
    create_maven_dependency
    create_usage_guide

    echo ""
    echo "=============================================================="
    log_success "✅ Jasypt加密工具配置完成！"
    echo "=============================================================="
    echo ""
    echo "📋 生成的文件："
    echo "  🔑 .jasypt-encryption-key          - 加密密钥文件"
    echo "  🔐 jasypt-cli-3.0.5.jar           - Jasypt CLI工具"
    echo "  🔧 encrypt-password.sh           - 密码加密脚本"
    echo "  🔓 decrypt-password.sh           - 密码解密脚本"
    echo "  📦 batch-encrypt.sh              - 批量加密脚本"
    echo "  ⚙️  .env.jasypt                    - 环境变量配置"
    echo "  📄 jasypt-spring-config.yml        - Spring配置示例"
    echo "  📚 jasypt-maven-dependency.xml    - Maven依赖配置"
    echo "  📖 JASYPT_USAGE_GUIDE.md         - 详细使用说明"
    echo ""
    echo "🚀 下一步操作："
    echo "  1. 编辑 batch-encrypt.sh 添加需要加密的密码"
    echo "  2. 运行 ./batch-encrypt.sh 批量加密密码"
    echo "  3. 在配置文件中替换明文密码为 ENC(加密值)"
    echo "  4. 设置环境变量 JASYPT_PASSWORD"
    echo "  5. 启动应用测试配置"
    echo ""
    echo "🔒 安全提醒："
    echo "  - 请妥善保管加密密钥文件 .jasypt-encryption-key"
    echo "  - 不要将加密密钥提交到代码仓库"
    echo "  - 生产环境请使用环境变量设置密钥"
    echo ""
}

# 执行主函数
main "$@"