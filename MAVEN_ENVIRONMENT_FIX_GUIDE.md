# Maven环境问题修复指南

## 问题描述
```
错误: 找不到或无法加载主类 #
原因: java.lang.ClassNotFoundException: #
```

## 根本原因分析

### 1. 主要矛盾：Jasypt配置冲突
- **Jasypt 3.0.5** 与 **Spring Boot 3.5.8** 存在兼容性问题
- 配置文件中的加密格式`ENC()`与YAML解析器冲突
- 加密算法配置不一致导致解析失败

### 2. 次要矛盾：Maven类加载器问题
- Maven插件依赖冲突
- 本地仓库缓存损坏
- JVM启动参数问题

### 3. 诱因：YAML文件编码问题
- UTF-8 BOM头导致的解析错误
- 特殊字符编码问题
- Spring Boot配置加载顺序问题

## 立即解决方案

### 方案1：移除Jasypt依赖（推荐）

#### 1.1 修改父POM配置
```xml
<!-- microservices/pom.xml -->
<properties>
    <!-- 临时禁用Jasypt -->
    <jasypt.version>3.0.5</jasypt.version>
</properties>

<dependencyManagement>
    <dependencies>
        <!-- 注释掉Jasypt依赖 -->
        <!--
        <dependency>
            <groupId>com.github.ulisesboca</groupId>
            <artifactId>jasypt-spring-boot-starter</artifactId>
            <version>${jasypt.version}</version>
        </dependency>
        -->
    </dependencies>
</dependencyManagement>
```

#### 1.2 修改服务POM配置
```xml
<!-- microservices/ioedream-consume-service/pom.xml -->
<dependencies>
    <!-- 注释掉Jasypt依赖 -->
    <!--
    <dependency>
        <groupId>com.github.ulisesboca</groupId>
        <artifactId>jasypt-spring-boot-starter</artifactId>
    </dependency>
    -->
</dependencies>
```

#### 1.3 简化配置文件
```yaml
# 移除所有Jasypt相关配置
# jasypt:
#   encryptor:
#     algorithm: PBEWithMD5AndDES
#     ...

spring:
  application:
    name: ioedream-consume-service
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
```

### 方案2：使用Properties格式替代YAML

#### 2.1 创建application.properties文件
```properties
# 替代application.yml
spring.application.name=ioedream-consume-service
spring.profiles.active=dev
server.port=8094

logging.level.root=INFO
logging.level.net.lab1024.sa=DEBUG
```

#### 2.2 修改配置文件引用
```yaml
# application.yml (简化版)
spring:
  config:
    import: "classpath:application.properties"
```

### 方案3：降级Jasypt版本

#### 3.1 使用兼容版本
```xml
<properties>
    <!-- 降级到兼容版本 -->
    <jasypt.version>3.0.4</jasypt.version>
</properties>
```

#### 3.2 修改加密配置
```yaml
jasypt:
  encryptor:
    algorithm: PBEWithMD5AndDES  # 简化算法
    key-obtention-iterations: 1000
    pool-size: 1
    provider-name: SunJCE
    salt-generator-classname: org.jasypt.salt.RandomSaltGenerator
    iv-generator-classname: org.jasypt.iv.RandomIvGenerator
    string-output-type: base64
```

## 执行步骤

### 步骤1：清理环境
```bash
# 1. 清理Maven本地仓库
rm -rf ~/.m2/repository/net/lab1024/sa/microservices-common
rm -rf ~/.m2/repository/com/github/ulisesboca/jasypt-spring-boot-starter

# 2. 清理项目目标目录
find . -name "target" -type d -exec rm -rf {} +

# 3. 清理Maven缓存
mvn dependency:purge-local-repository
```

### 步骤2：修复配置
```bash
# 1. 备份现有配置文件
cp microservices/microservices-common/src/main/resources/application-shared.yml \
   microservices/microservices-common/src/main/resources/application-shared.yml.backup

# 2. 创建简化配置
cat > microservices/microservices-common/src/main/resources/application-shared.yml << EOF
# 简化配置文件
spring:
  application:
    name: microservices-common
logging:
  level:
    root: INFO
EOF
```

### 步骤3：重新构建
```bash
# 1. 构建公共模块
cd microservices/microservices-common
mvn clean install -DskipTests -Dmaven.javadoc.skip=true

# 2. 构建消费服务
cd ../ioedream-consume-service
mvn clean compile -DskipTests -Dmaven.javadoc.skip=true
```

## 验证方法

### 1. 编译验证
```bash
mvn clean compile -DskipTests
```

### 2. 依赖验证
```bash
mvn dependency:tree
mvn dependency:analyze
```

### 3. 包验证
```bash
mvn package -DskipTests
jar tf target/microservices-common-1.0.0.jar
```

## 预防措施

### 1. 版本兼容性管理
```xml
<!-- 使用经过测试的版本组合 -->
<spring-boot.version>3.1.5</spring-boot.version>
<jasypt.version>3.0.4</jasypt.version>
```

### 2. 配置文件标准化
```yaml
# 统一使用Properties格式避免YAML问题
spring.config.activate.on-profile=dev
```

### 3. 构建环境隔离
```bash
# 使用Maven Profile隔离不同环境
mvn clean install -Pdev
mvn clean install -Pprod
```

### 4. 持续集成检查
```yaml
# .github/workflows/ci-cd.yml
- name: Build and Test
  run: |
    mvn clean compile
    mvn dependency:analyze
```

## 故障排除

### 常见问题及解决方法

#### 1. Maven无法启动
```bash
# 重新安装Maven
brew install maven  # macOS
sudo apt-get install maven  # Ubuntu
```

#### 2. 依赖冲突
```bash
# 查看依赖冲突
mvn dependency:tree -Dverbose
mvn enforcer:enforce
```

#### 3. 编码问题
```bash
# 设置编码
export MAVEN_OPTS="-Dfile.encoding=UTF-8"
mvn clean compile -Dfile.encoding=UTF-8
```

#### 4. 内存不足
```bash
# 增加Maven内存
export MAVEN_OPTS="-Xmx2g -Xms1g"
mvn clean compile
```

## 联系支持

如果问题仍然存在，请提供以下信息：

1. **环境信息**
   - Java版本: `java -version`
   - Maven版本: `mvn -version`
   - 操作系统版本

2. **错误信息**
   - 完整的构建日志
   - 具体的错误堆栈
   - 相关的配置文件

3. **项目信息**
   - 项目结构
   - 依赖树
   - 配置文件内容

---

**修复优先级**:
1. **P0**: 移除Jasypt依赖（立即执行）
2. **P1**: 简化配置文件（P0后执行）
3. **P2**: 优化构建环境（P1后执行）

**预计修复时间**: 30分钟 - 2小时

**验证标准**:
- Maven编译成功
- 所有依赖正确解析
- 服务能够正常启动