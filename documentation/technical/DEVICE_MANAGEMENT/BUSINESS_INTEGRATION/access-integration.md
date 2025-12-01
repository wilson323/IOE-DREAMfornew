# 🚪 门禁业务集成方案

**文档版本**: v1.0.0
**创建日期**: 2025-11-16
**最后更新**: 2025-11-16
**维护者**: SmartAdmin Team

---

## 📋 概述

本文档详细描述了IOE-DREAM智慧园区一卡通管理平台中门禁系统与各业务模块的集成方案。基于repowiki规范体系，遵循四层架构设计，提供完整的门禁业务集成架构和技术实现方案。

---

## 🏗️ 门禁业务集成架构

### 📐 四层架构设计（遵循repowiki规范）

```mermaid
graph TB
    subgraph "Controller层 - 表现层"
        A1[AccessController]
        A2[AccessDeviceController]
        A3[AccessLogController]
        A4[AccessPermissionController]
    end

    subgraph "Service层 - 业务逻辑层"
        B1[AccessService]
        B2[AccessDeviceService]
        B3[AccessLogService]
        B4[AccessPermissionService]
        B5[AccessAreaService]
    end

    subgraph "Manager层 - 业务封装层"
        C1[AccessManager]
        C2[AccessDeviceManager]
        C3[AccessRuleManager]
        C4[AccessAreaManager]
        C5[AccessSecurityManager]
    end

    subgraph "DAO层 - 数据访问层"
        D1[AccessDao]
        D2[AccessDeviceDao]
        D3[AccessLogDao]
        D4[AccessPermissionDao]
        D5[AccessAreaDao]
    end

    A1 --> B1
    A2 --> B2
    A3 --> B3
    A4 --> B4

    B1 --> C1
    B2 --> C2
    B3 --> C3
    B4 --> C4
    B5 --> C5

    C1 --> D1
    C2 --> D2
    C3 --> D3
    C4 --> D4
    C5 --> D5
```

---

## 🔗 业务模块集成矩阵

### 📊 门禁系统与各业务模块集成关系

| 业务模块 | 集成方式 | 数据流向 | 实时性要求 | 安全级别 | 集成复杂度 |
|----------|----------|----------|------------|----------|------------|
| 人员管理 | RESTful API | 双向同步 | 高 | 高 | 中 |
| 权限管理 | 内部服务调用 | 双向同步 | 极高 | 极高 | 高 |
| 考勤管理 | 消息队列 | 单向推送 | 中 | 中 | 低 |
| 消费管理 | HTTP API | 单向查询 | 低 | 中 | 低 |
| 视频监控 | WebSocket | 双向通讯 | 极高 | 高 | 高 |
| 报警系统 | 事件驱动 | 单向推送 | 极高 | 高 | 中 |

---

## 👥 人员管理模块集成

### 🔗 集成架构设计

```mermaid
graph LR
    subgraph "人员管理模块"
        A1[EmployeeService]
        A2[DepartmentService]
        A3[PositionService]
    end

    subgraph "门禁管理模块"
        B1[AccessPermissionService]
        B2[AccessUserService]
        B3[AccessAreaService]
    end

    subgraph "数据同步层"
        C1[同步服务]
        C2[消息队列]
        C3[数据验证器]
    end

    subgraph "缓存层"
        D1[Redis缓存]
        D2[本地缓存]
    end

    A1 --> C1
    A2 --> C1
    A3 --> C1

    C1 --> C2
    C1 --> C3

    C2 --> B1
    C3 --> B2

    B1 --> D1
    B2 --> D1
    B3 --> D2
```

### 📡 数据同步流程

```mermaid
sequenceDiagram
    participant HR as 人员管理服务
    participant S as 同步服务
    participant Q as 消息队列
    participant A as 门禁权限服务
    participant R as Redis缓存
    participant DB as 数据库

    HR->>S: 人员信息变更
    S->>S: 数据验证和转换

    alt 实时同步
        S->>A: 直接权限更新
        A->>R: 更新缓存权限
        A->>DB: 持久化权限信息
        A-->>S: 同步成功
    else 批量同步
        S->>Q: 批量同步消息
        Q->>A: 批量权限处理
        A->>DB: 批量更新
        A->>R: 批量缓存更新
        A-->>S: 批量同步完成
    end

    S-->>HR: 同步结果确认
```

### 💻 Controller层实现示例

```java
/**
 * 门禁权限控制器 - 遵循repowiki规范
 */
@RestController
@RequestMapping("/api/smart/access/permission")
public class AccessPermissionController {

    @Resource
    private AccessPermissionService accessPermissionService;

    /**
     * 根据用户ID获取门禁权限
     */
    @GetMapping("/user/{userId}")
    @SaCheckLogin
    @SaCheckPermission("access:permission:query")
    public ResponseDTO<List<AccessPermissionVO>> getUserPermissions(@PathVariable Long userId) {
        List<AccessPermissionVO> permissions = accessPermissionService.getUserPermissions(userId);
        return ResponseDTO.ok(permissions);
    }

    /**
     * 同步人员权限信息
     */
    @PostMapping("/sync/user/{userId}")
    @SaCheckLogin
    @SaCheckPermission("access:permission:sync")
    public ResponseDTO<String> syncUserPermissions(@PathVariable Long userId) {
        accessPermissionService.syncUserPermissions(userId);
        return ResponseDTO.ok("权限同步成功");
    }
}
```

### 💼 Service层实现示例

```java
/**
 * 门禁权限服务实现 - 遵循repowiki规范
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class AccessPermissionServiceImpl implements AccessPermissionService {

    @Resource
    private AccessPermissionManager accessPermissionManager;

    @Resource
    private RedisUtil redisUtil;

    @Override
    public List<AccessPermissionVO> getUserPermissions(Long userId) {
        // 先从缓存获取
        String cacheKey = "access:permission:user:" + userId;
        List<AccessPermissionVO> cachedPermissions = redisUtil.getList(cacheKey, AccessPermissionVO.class);

        if (!CollectionUtils.isEmpty(cachedPermissions)) {
            return cachedPermissions;
        }

        // 缓存未命中，从数据库获取
        List<AccessPermissionVO> permissions = accessPermissionManager.getUserPermissions(userId);

        // 写入缓存，有效期5分钟
        redisUtil.setList(cacheKey, permissions, 300);

        return permissions;
    }

    @Override
    public void syncUserPermissions(Long userId) {
        // 清除相关缓存
        String cacheKey = "access:permission:user:" + userId;
        redisUtil.delete(cacheKey);

        // 执行权限同步
        accessPermissionManager.syncUserPermissions(userId);
    }
}
```

---

## 🔐 权限管理模块集成

### 🔗 权限集成架构

```mermaid
graph TB
    subgraph "权限管理系统"
        A1[RoleService]
        A2[PermissionService]
        A3[UserService]
    end

    subgraph "门禁权限映射"
        B1[AccessRoleMapper]
        B2[AccessPermissionMapper]
        B3[AccessTimeRule]
    end

    subgraph "权限验证层"
        C1[权限验证器]
        C2[时间规则引擎]
        C3[区域规则引擎]
    end

    subgraph "设备执行层"
        D1[门禁控制器]
        D2[读卡器]
        D3[电控锁]
    end

    A1 --> B1
    A2 --> B2
    A3 --> B1

    B1 --> C1
    B2 --> C2
    B3 --> C3

    C1 --> D1
    C2 --> D1
    C3 --> D1

    D1 --> D2
    D1 --> D3
```

### 🛡️ 权限验证流程

```mermaid
sequenceDiagram
    participant D as 门禁设备
    participant V as 权限验证服务
    participant R as 规则引擎
    participant C as 缓存服务
    participant DB as 数据库

    D->>V: 访问请求(用户ID, 设备ID)
    V->>C: 检查用户权限缓存

    alt 缓存命中
        C-->>V: 返回缓存权限
    else 缓存未命中
        V->>DB: 查询用户权限
        DB-->>V: 返回权限数据
        V->>C: 更新权限缓存
    end

    V->>R: 执行权限规则验证

    par 时间验证 区域验证 角色验证
        R->>R: 验证时间规则
    and
        R->>R: 验证区域权限
    and
        R->>R: 验证角色权限
    end

    R-->>V: 验证结果

    alt 验证通过
        V-->>D: 允许通行
        D->>D: 开门操作
        V->>DB: 记录访问日志
    else 验证拒绝
        V-->>D: 拒绝通行
        V->>DB: 记录拒绝日志
    end
```

---

## 📹 视频监控模块集成

### 🎥 门禁视频联动架构

```mermaid
graph LR
    subgraph "门禁系统"
        A1[门禁控制器]
        A2[开门事件]
        A3[报警事件]
    end

    subgraph "视频系统"
        B1[摄像头]
        B2[NVR设备]
        B3[视频分析服务]
    end

    subgraph "联动服务"
        C1[事件处理器]
        C2[视频联动服务]
        C3[录像管理服务]
    end

    subgraph "存储系统"
        D1[实时视频流]
        D2[录像存储]
        D3[事件快照]
    end

    A1 --> C1
    A2 --> C1
    A3 --> C1

    C1 --> C2
    C2 --> B1
    C2 --> B2

    C3 --> B3
    C3 --> D2
    C1 --> D3

    B1 --> D1
    B2 --> D1
```

### 📡 视频联动处理流程

```mermaid
sequenceDiagram
    participant A as 门禁系统
    participant L as 联动服务
    participant V as 视频服务
    participant S as 存储服务
    participant F as 前端应用

    A->>L: 开门事件(用户ID, 设备ID, 时间)
    L->>L: 分析关联摄像头

    L->>V: 触发录像指令
    V->>V: 开始事件录像
    V->>S: 保存视频流

    L->>V: 抓取快照指令
    V->>V: 生成事件快照
    V->>S: 保存快照图片

    L->>F: 推送实时视频流
    F->>F: 显示联动视频

    alt 事件类型为报警
        L->>V: 启动报警录像
        V->>S: 保存报警视频
        L->>F: 推送报警通知
    end

    V-->>L: 录像完成确认
    L-->>A: 联动处理完成
```

---

## ⏰ 考勤管理模块集成

### 📊 门禁考勤数据集成架构

```mermaid
graph TB
    subgraph "门禁设备"
        A1[门禁读卡器]
        A2[人脸识别机]
        A3[指纹识别机]
    end

    subgraph "门禁系统"
        B1[开门事件捕获]
        B2[用户识别服务]
        B3[位置验证服务]
    end

    subgraph "数据转换层"
        C1[事件过滤服务]
        C2[数据映射器]
        C3[考勤记录生成器]
    end

    subgraph "考勤系统"
        D1[考勤记录服务]
        D2[排班规则引擎]
        D3[考勤统计服务]
    end

    subgraph "数据存储"
        E1[门禁日志表]
        E2[考勤记录表]
        E3[考勤统计表]
    end

    A1 --> B1
    A2 --> B2
    A3 --> B2

    B1 --> C1
    B2 --> C2
    B3 --> C3

    C1 --> D1
    C2 --> D1
    C3 --> D1

    D1 --> E2
    D2 --> E3
    D3 --> E3

    B1 --> E1
```

### 🔄 考勤数据同步处理

```mermaid
sequenceDiagram
    participant D as 门禁设备
    participant A as 门禁服务
    participant F as 数据过滤器
    participant T as 数据转换器
    participant K as 考勤服务
    participant DB as 数据库

    D->>A: 开门事件
    A->>F: 事件数据传输

    F->>F: 过滤考勤相关事件
    Note over F: 过滤条件：<br/>1.工作时间段<br/>2.有效人员<br/>3.考勤区域

    alt 有效考勤事件
        F->>T: 传输过滤后事件
        T->>T: 数据格式转换
        T->>K: 考勤记录创建

        K->>K: 考勤规则验证
        alt 验证通过
            K->>DB: 保存考勤记录
            K-->>T: 考勤记录ID
        else 验证失败
            K-->>T: 验证失败原因
        end

        T-->>F: 处理结果
    else 无效事件
        F->>F: 忽略处理
    end

    F-->>A: 同步结果
    A-->>D: 响应确认
```

---

## 💳 消费管理模块集成

### 🛒 门禁消费联动架构

```mermaid
graph LR
    subgraph "门禁系统"
        A1[身份验证]
        A2[用户识别]
        A3[位置信息]
    end

    subgraph "消费系统"
        B1[账户服务]
        B2[支付服务]
        B3[商户服务]
    end

    subgraph "集成服务层"
        C1[身份映射服务]
        C2[支付网关]
        C3[账单服务]
    end

    subgraph "数据存储"
        D1[用户账户表]
        D2[交易记录表]
        D3[门禁日志表]
    end

    A1 --> C1
    A2 --> C1
    A3 --> C2

    C1 --> B1
    C2 --> B2
    C3 --> B3

    B1 --> D1
    B2 --> D2
    C3 --> D2

    A1 --> D3
```

---

## 🚨 报警系统模块集成

### ⚠️ 门禁报警联动架构

```mermaid
graph TB
    subgraph "门禁系统"
        A1[非法闯入检测]
        A2[设备异常检测]
        A3[胁迫报警检测]
    end

    subgraph "报警系统"
        B1[报警规则引擎]
        B2[报警级别管理]
        B3[报警通知服务]
    end

    subgraph "联动系统"
        C1[视频联动]
        C2[灯光联动]
        C3[声光报警器]
        C4[短信通知]
    end

    subgraph "处理流程"
        D1[报警接收]
        D2[规则匹配]
        D3[级别判定]
        D4[联动执行]
        D5[记录存档]
    end

    A1 --> D1
    A2 --> D1
    A3 --> D1

    D1 --> D2
    D2 --> B1
    D3 --> B2

    B1 --> D4
    B3 --> D4

    D4 --> C1
    D4 --> C2
    D4 --> C3
    D4 --> C4

    D4 --> D5
```

---

## 📊 集成监控与运维

### 📈 系统集成健康监控

```mermaid
graph TB
    subgraph "监控指标"
        A1[接口调用监控]
        A2[数据同步监控]
        A3[性能指标监控]
        A4[错误率监控]
    end

    subgraph "告警机制"
        B1[实时告警]
        B2[阈值告警]
        B3[趋势告警]
        B4[异常告警]
    end

    subgraph "运维工具"
        C1[日志分析]
        C2[性能调优]
        C3[故障诊断]
        C4[自动恢复]
    end

    A1 --> B1
    A2 --> B2
    A3 --> B3
    A4 --> B4

    B1 --> C1
    B2 --> C2
    B3 --> C3
    B4 --> C4
```

### 📋 关键性能指标(KPI)

| 指标类别 | 指标名称 | 目标值 | 监控频率 | 告警阈值 |
|----------|----------|--------|----------|----------|
| 可用性 | 系统可用率 | ≥99.9% | 实时 | <99.5% |
| 性能 | 接口响应时间 | ≤200ms | 实时 | >500ms |
| 同步 | 数据同步延迟 | ≤5s | 1分钟 | >30s |
| 错误率 | 接口错误率 | ≤0.1% | 实时 | >1% |
| 吞吐量 | 每秒处理请求数 | ≥1000 | 10秒 | <500 |

---

## 🔧 集成实施指南

### 📋 实施阶段规划

#### 第一阶段：基础集成（1-2周）
- [ ] 完成人员管理模块集成
- [ ] 实现基础权限同步功能
- [ ] 建立数据同步机制
- [ ] 完成基础联调测试

#### 第二阶段：核心功能集成（2-3周）
- [ ] 实现视频监控联动
- [ ] 完成考勤数据集成
- [ ] 建立报警联动机制
- [ ] 性能优化和稳定性测试

#### 第三阶段：高级功能集成（1-2周）
- [ ] 消费系统联动集成
- [ ] 高级报警规则配置
- [ ] 数据分析和报表功能
- [ ] 系统集成压力测试

#### 第四阶段：上线部署（1周）
- [ ] 生产环境部署
- [ ] 数据迁移和初始化
- [ ] 用户培训和文档交付
- [ ] 运维监控配置

### ⚠️ 风险控制措施

#### 技术风险
- **数据一致性风险**：采用分布式事务和补偿机制
- **性能风险**：实施缓存策略和异步处理
- **安全风险**：加强身份认证和数据加密

#### 业务风险
- **功能缺失风险**：充分的需求调研和原型验证
- **用户体验风险**：用户参与测试和反馈收集
- **运维风险**：完善的监控和自动恢复机制

---

## 📚 参考规范

### 🔗 repowiki核心规范
- **[架构设计规范](../../../repowiki/zh/content/核心规范/架构设计规范.md)** - 四层架构设计标准
- **[Java编码规范](../../../repowiki/zh/content/核心规范/Java编码规范.md)** - Java代码编写标准
- **[API设计规范](../../../repowiki/zh/content/核心规范/RESTfulAPI设计规范.md)** - RESTful接口设计标准
- **[系统安全规范](../../../repowiki/zh/content/核心规范/系统安全规范.md)** - 系统安全要求

### 📖 项目规范文档
- **[架构设计规范](../../ARCHITECTURE_STANDARDS.md)** - IOE-DREAM架构设计要求
- **[通用开发检查清单](../../CHECKLISTS/通用开发检查清单.md)** - 代码质量保证清单
- **[门禁系统开发检查清单](../../CHECKLISTS/门禁系统开发检查清单.md)** - 门禁功能专用检查清单

---

**⚠️ 重要提醒**: 本门禁业务集成方案严格遵循repowiki规范体系和IOE-DREAM项目架构标准。所有集成开发工作必须按照本文档中的技术规范和实施计划执行，确保系统集成的稳定性、安全性和可维护性。