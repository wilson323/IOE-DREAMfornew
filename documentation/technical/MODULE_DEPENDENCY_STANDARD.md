# 模块依赖规范标准

**文档版本**: v1.0.0
**生成时间**: 2025-12-23
**适用范围**: IOE-DREAM微服务项目

---

## 1. 依赖架构设计原则

### 1.1 核心原则

**零循环依赖**：依赖关系必须单向，禁止任何形式的循环依赖

**分层依赖**：严格遵循依赖层次，上层可依赖下层，下层不可依赖上层

**最小依赖**：只依赖必需的模块，避免不必要的依赖

**接口隔离**：跨服务调用通过GatewayClient，禁止直接依赖其他业务服务

### 1.2 依赖层次（4层架构）

```
第1层（最底层）：
├── microservices-common-core        # 核心基础（纯Java，尽量不依赖Spring）
└── microservices-common-entity      # 所有实体类统一管理

第2层（基础能力层）：
├── microservices-common-storage     # 文件存储
├── microservices-common-data        # 数据访问层（MyBatis-Plus、Druid、Flyway）
├── microservices-common-security    # 安全认证（JWT、Spring Security）
├── microservices-common-cache       # 缓存管理（Caffeine、Redis）
├── microservices-common-monitor     # 监控告警（Micrometer）
├── microservices-common-export      # 导出功能（EasyExcel、iText PDF）
├── microservices-common-workflow    # 工作流（Aviator、Quartz）
├── microservices-common-business    # 业务公共组件（DAO、Manager、Service接口）
└── microservices-common-permission  # 权限验证

第3层（配置容器层）：
└── microservices-common             # 配置类和工具类容器（GatewayClient、IoeDreamGatewayProperties）

第4层（业务服务层）：
├── ioedream-gateway-service         # 服务网关
├── ioedream-common-service          # 公共业务服务
├── ioedream-access-service          # 门禁服务
├── ioedream-attendance-service      # 考勤服务
├── ioedream-consume-service         # 消费服务
├── ioedream-video-service           # 视频服务
├── ioedream-visitor-service         # 访客服务
└── ...其他业务服务
```

---

## 2. 依赖规则（强制执行）

### 2.1 业务服务依赖规则

**✅ 允许的依赖模式**：

```xml
<!-- 正确：业务服务只依赖细粒度模块 -->
<dependencies>
    <dependency>
        <groupId>net.lab1024.sa</groupId>
        <artifactId>microservices-common-core</artifactId>
    </dependency>
    <dependency>
        <groupId>net.lab1024.sa</groupId>
        <artifactId>microservices-common-data</artifactId>
    </dependency>
    <dependency>
        <groupId>net.lab1024.sa</groupId>
        <artifactId>microservices-common-security</artifactId>
    </dependency>
    <!-- 其他按需依赖的细粒度模块 -->
</dependencies>
```

**❌ 禁止的依赖模式**：

```xml
<!-- 错误1：业务服务同时依赖 microservices-common 和细粒度模块 -->
<dependencies>
    <dependency>
        <groupId>net.lab1024.sa</groupId>
        <artifactId>microservices-common</artifactId>  <!-- ❌ 聚合模块 -->
    </dependency>
    <dependency>
        <groupId>net.lab1024.sa</groupId>
        <artifactId>microservices-common-data</artifactId>  <!-- ❌ 细粒度模块 -->
    </dependency>
</dependencies>

<!-- 错误2：业务服务直接依赖其他业务服务 -->
<dependency>
    <groupId>net.lab1024.sa</groupId>
    <artifactId>ioedream-common-service</artifactId>  <!-- ❌ 禁止 -->
</dependency>
```

**例外情况**：
- ✅ 网关服务（ioedream-gateway-service）可以依赖`microservices-common`配置容器

### 2.2 细粒度模块依赖规则

**❌ 禁止的依赖模式**：

```xml
<!-- 错误：细粒度模块依赖 microservices-common -->
<!-- microservices-common-storage/pom.xml -->
<dependencies>
    <dependency>
        <groupId>net.lab1024.sa</groupId>
        <artifactId>microservices-common</artifactId>  <!-- ❌ 禁止 -->
    </dependency>
</dependencies>
```

**✅ 允许的依赖模式**：

```xml
<!-- 正确：细粒度模块只依赖 core 或 entity -->
<dependencies>
    <dependency>
        <groupId>net.lab1024.sa</groupId>
        <artifactId>microservices-common-core</artifactId>
    </dependency>
    <dependency>
        <groupId>net.lab1024.sa</groupId>
        <artifactId>microservices-common-entity</artifactId>
    </dependency>
</dependencies>
```

### 2.3 跨服务调用规则

**✅ 正确的跨服务调用**：

```java
// 使用 GatewayServiceClient 调用其他服务
@Service
public class AttendanceServiceImpl {

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    public UserVO getUserInfo(Long userId) {
        // 通过网关调用用户服务
        return gatewayServiceClient.callUserService(
            "/api/user/" + userId,
            HttpMethod.GET,
            null,
            UserVO.class
        );
    }
}
```

**❌ 禁止的跨服务调用**：

```java
// 错误1：直接依赖其他业务服务
@Resource
private UserService userService;  // ❌ 禁止

// 错误2：直接访问其他服务的DAO
@Resource
private UserDao userDao;  // ❌ 禁止

// 错误3：直接调用其他服务的Manager
@Resource
private UserManager userManager;  // ❌ 禁止
```

---

## 3. 当前依赖状态验证（2025-12-23）

### 3.1 业务服务依赖检查

| 服务名称 | 细粒度模块数 | 聚合模块依赖 | 状态 |
|---------|-------------|-------------|------|
| ioedream-access-service | 8 | 无 | ✅ 符合规范 |
| ioedream-attendance-service | 10 | 无 | ✅ 符合规范 |
| ioedream-consume-service | 7 | 无 | ✅ 符合规范 |
| ioedream-video-service | 9 | 无 | ✅ 符合规范 |
| ioedream-visitor-service | 7 | 无 | ✅ 符合规范 |

### 3.2 细粒度模块依赖检查

所有细粒度模块均符合规范，未发现违规依赖`microservices-common`的情况。

### 3.3 Lombok版本管理

**当前版本**: Lombok 1.18.30 ✅
**降级时间**: 2025-12-23
**降级原因**: 解决Java 17 + Maven 3.11.0兼容性问题

---

## 4. 依赖冲突处理

### 4.1 传递依赖冲突

**问题**: 多个模块依赖不同版本的同一个库

**解决方案**: 在父POM中统一管理版本

```xml
<!-- microservices/pom.xml -->
<properties>
    <lombok.version>1.18.30</lombok.version>
    <mybatis-plus.version>3.5.15</mybatis-plus.version>
    <druid.version>1.2.25</druid.version>
    <!-- 其他版本统一管理 -->
</properties>
```

### 4.2 版本排除

**问题**: 某个传递依赖引入了不兼容的版本

**解决方案**: 使用exclusions排除

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>some-library</artifactId>
    <version>1.0.0</version>
    <exclusions>
        <exclusion>
            <groupId>org.conflicting</groupId>
            <artifactId>conflicting-library</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

---

## 5. 最佳实践

### 5.1 依赖最小化

**原则**: 只依赖真正需要的模块

**示例**: 如果只需要缓存功能，只依赖`microservices-common-cache`，而不是`microservices-common`

### 5.2 版本统一管理

**原则**: 所有第三方库版本在父POM中统一管理

**好处**:
- 避免版本冲突
- 便于升级维护
- 确保版本一致性

### 5.3 定期依赖审查

**频率**: 每月一次

**内容**:
- 检查是否有新的依赖违规
- 更新第三方库到安全版本
- 清理未使用的依赖

---

## 6. 违规检查工具

### 6.1 自动检查脚本

```bash
# 运行依赖违规检查
cd microservices
bash scripts/check-dependency-violations.sh
```

### 6.2 Maven依赖分析

```bash
# 生成依赖树
mvn dependency:tree > dependency-reports/full-dependency-tree.txt

# 分析依赖
mvn dependency:analyze
```

### 6.3 CI/CD集成

**检查时机**:
- Git pre-commit钩子
- PR合并前
- 定时构建（每日一次）

---

## 7. 变更流程

### 7.1 新增依赖

**流程**:
1. 在父POM中添加版本管理
2. 在需要的模块中添加依赖
3. 运行依赖违规检查
4. 提交代码审查

### 7.2 修改现有依赖

**流程**:
1. 评估影响范围
2. 更新版本号
3. 运行完整的测试套件
4. 代码审查和批准

---

## 8. 术语表

| 术语 | 说明 |
|------|------|
| 细粒度模块 | 单一职责的公共模块（如common-data、common-cache） |
| 聚合模块 | 包含多个细粒度模块的容器模块（如microservices-common） |
| 循环依赖 | A依赖B，B又依赖A的依赖关系 |
| 传递依赖 | 通过依赖的依赖间接引入的依赖 |
| 依赖冲突 | 同一个库的不同版本在项目中同时存在 |

---

**文档维护者**: IOE-DREAM架构委员会
**最后更新**: 2025-12-23
**下次审查**: 2026-01-23
