# GraalVM Native Image 试点方案

## 一、概述

### 1.1 目标
- **内存节省**：50-70%（从8GB降至2-3GB）
- **启动时间**：从30秒降至<1秒
- **试点服务**：gateway-service（无状态、轻量级）

### 1.2 前提条件
- GraalVM 21+ (与Java 17兼容)
- Native Build Tools
- 完成模块化拆分（减少反射使用）

### 1.3 风险评估

| 风险 | 等级 | 缓解措施 |
|------|------|---------|
| 反射代码不兼容 | 高 | 提前生成reflect-config.json |
| 动态代理问题 | 中 | 使用接口代理，避免CGLIB |
| 第三方库不支持 | 中 | 使用GraalVM兼容版本 |
| 构建时间长 | 低 | 使用CI/CD异步构建 |

---

## 二、环境准备

### 2.1 安装GraalVM

**Windows PowerShell**：
```powershell
# 方式1：使用SDKMAN（推荐）
sdk install java 21.0.1-graalce
sdk use java 21.0.1-graalce

# 方式2：手动安装
# 下载：https://www.graalvm.org/downloads/
# 设置环境变量
$env:GRAALVM_HOME = "C:\Program Files\graalvm-ce-java21-21.0.1"
$env:PATH = "$env:GRAALVM_HOME\bin;$env:PATH"

# 安装Native Image组件
gu install native-image

# 验证安装
native-image --version
```

### 2.2 项目配置

**父POM添加Native支持**：
```xml
<properties>
    <!-- GraalVM Native Image配置 -->
    <native.maven.plugin.version>0.10.1</native.maven.plugin.version>
</properties>

<profiles>
    <profile>
        <id>native</id>
        <build>
            <plugins>
                <plugin>
                    <groupId>org.graalvm.buildtools</groupId>
                    <artifactId>native-maven-plugin</artifactId>
                    <version>${native.maven.plugin.version}</version>
                    <executions>
                        <execution>
                            <id>build-native</id>
                            <goals>
                                <goal>compile-no-fork</goal>
                            </goals>
                            <phase>package</phase>
                        </execution>
                    </executions>
                    <configuration>
                        <imageName>${project.artifactId}</imageName>
                        <buildArgs>
                            <buildArg>--no-fallback</buildArg>
                            <buildArg>-H:+ReportExceptionStackTraces</buildArg>
                            <buildArg>-H:+AddAllCharsets</buildArg>
                        </buildArgs>
                    </configuration>
                </plugin>
            </plugins>
        </build>
    </profile>
</profiles>
```

---

## 三、Gateway服务Native化改造

### 3.1 创建Native配置目录

```bash
mkdir -p ioedream-gateway-service/src/main/resources/META-INF/native-image
```

### 3.2 reflect-config.json

```json
[
  {
    "name": "net.lab1024.sa.gateway.GatewayServiceApplication",
    "allDeclaredConstructors": true,
    "allDeclaredMethods": true
  },
  {
    "name": "org.springframework.cloud.gateway.route.RouteDefinition",
    "allDeclaredConstructors": true,
    "allDeclaredMethods": true,
    "allDeclaredFields": true
  },
  {
    "name": "org.springframework.cloud.gateway.filter.FilterDefinition",
    "allDeclaredConstructors": true,
    "allDeclaredMethods": true,
    "allDeclaredFields": true
  },
  {
    "name": "org.springframework.cloud.gateway.handler.predicate.PredicateDefinition",
    "allDeclaredConstructors": true,
    "allDeclaredMethods": true,
    "allDeclaredFields": true
  }
]
```

### 3.3 resource-config.json

```json
{
  "resources": {
    "includes": [
      {"pattern": "application.yml"},
      {"pattern": "application-*.yml"},
      {"pattern": "bootstrap.yml"},
      {"pattern": "META-INF/.*"}
    ]
  },
  "bundles": []
}
```

### 3.4 serialization-config.json

```json
{
  "types": [],
  "lambdaCapturingTypes": []
}
```

### 3.5 修改Gateway pom.xml

```xml
<!-- 在gateway-service的pom.xml中添加 -->
<dependencies>
    <!-- Spring Boot Native支持 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <image>
                    <builder>paketobuildpacks/builder:tiny</builder>
                    <env>
                        <BP_NATIVE_IMAGE>true</BP_NATIVE_IMAGE>
                    </env>
                </image>
            </configuration>
        </plugin>
        
        <plugin>
            <groupId>org.graalvm.buildtools</groupId>
            <artifactId>native-maven-plugin</artifactId>
            <configuration>
                <imageName>gateway-native</imageName>
                <mainClass>net.lab1024.sa.gateway.GatewayServiceApplication</mainClass>
                <buildArgs>
                    <buildArg>--no-fallback</buildArg>
                    <buildArg>-H:+ReportExceptionStackTraces</buildArg>
                    <buildArg>--enable-http</buildArg>
                    <buildArg>--enable-https</buildArg>
                </buildArgs>
            </configuration>
        </plugin>
    </plugins>
</build>
```

---

## 四、构建与测试

### 4.1 构建Native镜像

```bash
# 进入gateway服务目录
cd microservices/ioedream-gateway-service

# 构建Native镜像（约10-20分钟）
mvn -Pnative package -DskipTests

# 或使用Spring Boot Buildpacks
mvn spring-boot:build-image -Pnative
```

### 4.2 运行Native应用

```bash
# 直接运行
./target/gateway-native

# 或Docker方式
docker run -p 8080:8080 ioedream/gateway-native:latest
```

### 4.3 验证测试

```bash
# 健康检查
curl http://localhost:8080/actuator/health

# 路由测试
curl http://localhost:8080/api/access/health
curl http://localhost:8080/api/attendance/health

# 内存监控
jcmd $(pgrep gateway-native) VM.native_memory summary
```

---

## 五、性能对比

### 5.1 预期效果

| 指标 | JVM模式 | Native模式 | 改善 |
|------|---------|-----------|------|
| 启动时间 | 15-20秒 | <1秒 | 95%+ |
| 内存占用 | 512MB | 100-150MB | 70%+ |
| 镜像大小 | 350MB | 80MB | 77% |
| CPU使用 | 正常 | 略高 | -10% |

### 5.2 监控命令

```powershell
# 内存监控
Get-Process gateway-native | Select-Object WorkingSet64

# 或使用企业监控脚本
.\scripts\memory-monitor-enterprise.ps1 -Mode snapshot
```

---

## 六、生产部署

### 6.1 Docker镜像

**Dockerfile.native**：
```dockerfile
FROM ghcr.io/graalvm/native-image:ol9-java21 AS builder

WORKDIR /build
COPY . .
RUN mvn -Pnative package -DskipTests

FROM gcr.io/distroless/base-debian12:latest
COPY --from=builder /build/target/gateway-native /app/gateway
EXPOSE 8080
ENTRYPOINT ["/app/gateway"]
```

### 6.2 Kubernetes部署

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: gateway-native
spec:
  replicas: 2
  template:
    spec:
      containers:
      - name: gateway
        image: ioedream/gateway-native:latest
        resources:
          requests:
            memory: "100Mi"
            cpu: "100m"
          limits:
            memory: "200Mi"
            cpu: "500m"
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 1  # Native启动极快
          periodSeconds: 5
```

---

## 七、实施计划

### 7.1 时间线

| 阶段 | 时间 | 任务 |
|------|------|------|
| 第1周 | 准备 | 安装GraalVM、环境配置 |
| 第2周 | 改造 | Gateway服务Native化改造 |
| 第3周 | 测试 | 功能测试、性能测试 |
| 第4周 | 部署 | 测试环境部署验证 |
| 第5-6周 | 推广 | 其他轻量服务Native化 |

### 7.2 优先级排序

**适合Native化的服务**（按优先级）：
1. **gateway-service** - 无状态、轻量、启动频繁
2. **visitor-service** - 业务简单、访问量大
3. **attendance-service** - 功能独立、反射少

**不适合Native化的服务**：
1. **oa-service** - 工作流引擎依赖多、反射重
2. **consume-service** - 大量报表导出、动态代理多

### 7.3 回滚策略

```yaml
# 保留JVM版本作为回滚
services:
  gateway:
    image: ${GATEWAY_IMAGE:-ioedream/gateway:latest}  # 可切换回JVM版本
```

---

## 八、注意事项

### 8.1 已知限制

1. **反射受限**：需要提前声明所有反射使用
2. **动态类加载禁止**：不支持运行时加载新类
3. **JNI谨慎使用**：需要显式配置
4. **序列化限制**：需要配置serialization-config.json

### 8.2 兼容性检查清单

- [ ] 检查所有@Autowired是否使用接口注入
- [ ] 检查Jackson序列化类是否有无参构造器
- [ ] 检查配置类是否使用@ConfigurationProperties
- [ ] 检查定时任务是否使用@Scheduled
- [ ] 检查AOP切面是否使用接口代理

### 8.3 调试技巧

```bash
# 生成Native配置文件
java -agentlib:native-image-agent=config-output-dir=META-INF/native-image \
     -jar gateway.jar

# 运行一段时间后，会自动生成：
# - reflect-config.json
# - resource-config.json  
# - serialization-config.json
# - proxy-config.json
# - jni-config.json
```

---

## 九、预期收益

### 9.1 资源节省

| 服务 | JVM内存 | Native内存 | 节省 |
|------|---------|-----------|------|
| Gateway | 512MB | 100MB | 80% |
| Visitor | 768MB | 150MB | 80% |
| Attendance | 768MB | 150MB | 80% |
| **合计** | **2GB** | **400MB** | **80%** |

### 9.2 运营成本

- **云服务器成本降低**：约60-70%
- **启动时间改善**：适合Serverless架构
- **弹性扩容加速**：秒级扩容

---

**文档版本**: v1.0.0  
**更新时间**: 2025-12-15  
**作者**: IOE-DREAM架构团队
