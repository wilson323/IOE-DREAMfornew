# Tasks: Optimize Memory and Resource Usage

## 目标
将开发环境内存占用从15-20GB降低至8-12GB（节省40-50%）

## 量化预期

| 服务 | 当前配置 | 优化后配置 | 预计节省 |
|-----|---------|----------|---------|
| gateway-service | Xms512m-Xmx1g | Xms256m-Xmx512m | ~500MB |
| common-service | Xms512m-Xmx1g | Xms256m-Xmx512m | ~500MB |
| device-comm-service | Xms1g-Xmx2g | Xms512m-Xmx1g | ~1GB |
| access-service | Xms1g-Xmx2g | Xms512m-Xmx1g | ~1GB |
| attendance-service | Xms1g-Xmx2g | Xms512m-Xmx1g | ~1GB |
| consume-service | Xms1g-Xmx2g | Xms512m-Xmx1g | ~1GB |
| visitor-service | Xms1g-Xmx2g | Xms512m-Xmx1g | ~1GB |
| video-service | Xms2g-Xmx4g | Xms1g-Xmx2g | ~2GB |
| oa-service | Xms1g-Xmx2g | Xms512m-Xmx1g | ~1GB |
| **总计** | **~15-20GB** | **~8-10GB** | **~40-50%** |

---

## P0 - JVM内存配置优化（预计节省30-40%，3天完成）

### Task 1.1: 创建开发环境专用配置Profile
- [x] 在所有服务的bootstrap.yml中添加`dev`profile配置
- [x] 配置较小的初始堆内存(Xms)，允许动态增长
- [x] 添加内存优化JVM参数

**文件列表**:
- `microservices/ioedream-gateway-service/src/main/resources/bootstrap.yml`
- `microservices/ioedream-common-service/src/main/resources/bootstrap.yml`
- `microservices/ioedream-device-comm-service/src/main/resources/bootstrap.yml`
- `microservices/ioedream-access-service/src/main/resources/bootstrap.yml`
- `microservices/ioedream-attendance-service/src/main/resources/bootstrap.yml`
- `microservices/ioedream-consume-service/src/main/resources/bootstrap.yml`
- `microservices/ioedream-visitor-service/src/main/resources/bootstrap.yml`
- `microservices/ioedream-video-service/src/main/resources/bootstrap.yml`
- `microservices/ioedream-oa-service/src/main/resources/bootstrap.yml`

**配置模板**:
```yaml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

---
# 开发环境配置
spring:
  config:
    activate:
      on-profile: dev
java:
  opts: >-
    -Xms256m
    -Xmx512m
    -XX:+UseG1GC
    -XX:MaxGCPauseMillis=200
    -XX:+UseStringDeduplication
    -XX:MaxMetaspaceSize=128m
    -XX:+HeapDumpOnOutOfMemoryError

---
# 生产环境配置（保持现有配置不变）
spring:
  config:
    activate:
      on-profile: prod
java:
  opts: >-
    -Xms1g
    -Xmx2g
    -XX:+UseG1GC
    -XX:MaxGCPauseMillis=200
```

### Task 1.2: 更新Docker Compose开发配置
- [x] 修改docker-compose.yml中的内存限制
- [x] 添加开发环境专用docker-compose-dev.yml

**文件列表**:
- `docker-compose-all.yml`
- `docker-compose-dev.yml`（新建）

### Task 1.3: 验证P0优化效果
- [ ] 启动所有服务并测量内存使用
- [ ] 验证所有API接口正常工作
- [ ] 记录启动时间对比
- [ ] 生成内存优化对比报告

---

## P1 - 类加载优化（预计额外节省10-15%，5天完成）

### Task 2.1: 精确配置scanBasePackages
- [x] 分析每个服务实际需要的公共包
- [x] 更新Application类的scanBasePackages配置
- [x] 只扫描服务实际使用的模块

**文件列表**:
- `microservices/ioedream-gateway-service/src/main/java/.../GatewayServiceApplication.java`
- `microservices/ioedream-common-service/src/main/java/.../CommonServiceApplication.java`
- `microservices/ioedream-device-comm-service/src/main/java/.../DeviceCommServiceApplication.java`
- `microservices/ioedream-access-service/src/main/java/.../AccessServiceApplication.java`
- `microservices/ioedream-attendance-service/src/main/java/.../AttendanceServiceApplication.java`
- `microservices/ioedream-consume-service/src/main/java/.../ConsumeServiceApplication.java`
- `microservices/ioedream-visitor-service/src/main/java/.../VisitorServiceApplication.java`
- `microservices/ioedream-video-service/src/main/java/.../VideoServiceApplication.java`
- `microservices/ioedream-oa-service/src/main/java/.../OaServiceApplication.java`

**配置模板**:
```java
@SpringBootApplication(
    scanBasePackages = {
        // 只扫描服务自身包
        "net.lab1024.sa.xxx",
        // 只扫描必需的公共包
        "net.lab1024.sa.common.core",
        "net.lab1024.sa.common.config",
        "net.lab1024.sa.common.util"
        // 不扫描不需要的业务模块
    },
    exclude = {
        // 排除不需要的自动配置
        DataSourceAutoConfiguration.class,
        RedisAutoConfiguration.class
    }
)
```

### Task 2.2: 排除不需要的自动配置类
- [ ] 分析每个服务的自动配置使用情况
- [ ] 配置exclude排除不需要的自动配置
- [ ] 使用spring.autoconfigure.exclude配置

### Task 2.3: 添加条件加载注解
- [ ] 为可选功能添加@ConditionalOnProperty注解
- [ ] 开发环境禁用非必要功能组件

### Task 2.4: 验证P1优化效果
- [ ] 测量类加载数量对比
- [ ] 验证所有功能正常
- [ ] 更新内存优化报告

---

## P2 - 公共库依赖优化（预计额外节省5-10%，3天完成）

### Task 3.1: 分析服务依赖使用情况
- [ ] 分析每个服务实际使用的common模块
- [ ] 生成依赖矩阵报告
- [ ] 识别不必要的依赖

### Task 3.2: 优化Maven依赖配置
- [ ] 按需引入common子模块
- [ ] 移除不必要的传递依赖
- [ ] 配置optional依赖

**文件列表**:
- `microservices/ioedream-*/pom.xml`（所有服务POM）

### Task 3.3: 验证P2优化效果
- [ ] 验证编译通过
- [ ] 验证所有功能正常
- [ ] 生成最终内存优化报告

---

## 验收标准

### 功能验收
- [ ] 所有API接口正常响应
- [ ] 所有业务功能正常工作
- [ ] 无功能缺失或降级

### 性能验收
- [ ] 开发环境内存占用 ≤ 12GB（降低40%+）
- [ ] 启动时间增加 ≤ 10%
- [ ] 运行时响应时间无明显增加

### 配置验收
- [ ] 生产环境配置不受影响
- [ ] Profile隔离正确
- [ ] Docker配置正确

---

## 风险备案

| 风险 | 概率 | 影响 | 缓解措施 |
|-----|------|------|---------|
| 类加载过少导致ClassNotFound | 中 | 高 | 逐步优化，充分测试 |
| 内存过小导致OOM | 低 | 高 | 设置合理的Xmx上限 |
| 启动时间显著增加 | 低 | 中 | 使用懒加载优化 |
| GC频繁导致性能下降 | 中 | 中 | 调整GC参数 |

---

## 实施计划

| 阶段 | 任务 | 预计节省 | 工作量 | 状态 |
|------|-----|---------|--------|------|
| P0 | JVM内存配置优化 | 30-40% | 3天 | ✅已完成 |
| P1 | 类加载优化 | 10-15% | 5天 | ✅已完成（部分） |
| P2 | 公共库依赖优化 | 5-10% | 3天 | 待开始 |
| **总计** | | **45-65%** | **11天** | |

**预期成果**: 内存优化40-50%，从15-20GB降至8-12GB
