# 架构设计规范

## 🏗️ 系统架构概述

### 技术栈
- **后端框架**: Spring Boot 3.x + Jakarta EE
- **数据库**: MySQL 8.0+ / 国产数据库（达梦、金仓等）
- **缓存**: Redis
- **消息队列**: RabbitMQ / RocketMQ
- **前端**: Vue 3 + TypeScript + Ant Design Vue 4.x

### 架构模式
- **分层架构**: 严格四层架构设计
- **微服务**: 模块化设计，支持微服务拆分
- **领域驱动**: 基于业务领域的模块划分

## 📐 四层架构规范

### Controller 层（表现层）
**职责**: 接收HTTP请求，参数校验，调用Service层

#### RAC权限控制规范 (必须遵循)
```java
@RestController
@RequestMapping("/api/smart/access")
public class AccessDeviceController {

    @Resource
    private AccessDeviceService accessDeviceService;

    @PostMapping("/page")
    @SaCheckLogin
    @RequireResource(
        resource = "smart:access:device",
        action = "READ",
        dataScope = DataScope.AREA,
        message = "您没有权限查询门禁设备列表"
    )
    public ResponseDTO<PageResult<AccessDeviceEntity>> getDevicePage(@Valid @RequestBody PageParam pageParam) {
        return ResponseDTO.ok(accessDeviceService.getDevicePage(pageParam));
    }

    @PostMapping("/remoteOpen/{deviceId}")
    @SaCheckLogin
    @RequireResource(
        resource = "smart:access:device",
        action = "WRITE",
        dataScope = DataScope.AREA,
        message = "您没有权限远程控制门禁设备"
    )
    public ResponseDTO<String> remoteOpenDoor(@PathVariable Long deviceId) {
        return ResponseDTO.ok(accessDeviceService.remoteOpenDoor(deviceId));
    }
}
```

#### 权限注解使用约定
1. **@SaCheckLogin**: 必须添加，验证用户登录状态
2. **@RequireResource**: RAC权限控制注解，必须配置：
   - `resource`: 资源编码，格式 `模块:子模块:资源`
   - `action`: 操作类型 (READ/WRITE/DELETE/APPROVE)
   - `dataScope`: 数据域 (ALL/AREA/DEPT/SELF/CUSTOM/NONE)
   - `message`: 权限拒绝时的提示信息

#### 数据域权限选择指南
- **ALL**: 仅用于超级管理员功能
- **AREA**: 区域相关的业务数据（门禁设备、区域管理）
- **DEPT**: 部门内部的管理数据（考勤记录、人员管理）
- **SELF**: 个人相关的操作和数据（打卡、支付、个人信息）
- **CUSTOM**: 特殊业务规则场景
- **NONE**: 系统级资源，不进行数据域过滤

**规范要求**:
- ✅ 必须使用 `@Resource` 进行依赖注入
- ✅ 必须添加 `@SaCheckLogin` 登录验证
- ✅ 必须添加 `@RequireResource` RAC权限控制
- ✅ 必须使用 `@Valid` 进行参数校验
- ✅ 必须返回统一的 `ResponseDTO` 格式
- ✅ 权限注解必须明确指定数据域范围
- ❌ 禁止在Controller层编写业务逻辑
- ❌ 禁止直接访问DAO层
- ❌ 禁止绕过权限验证的业务接口

### Service 层（业务逻辑层）
**职责**: 业务逻辑处理，事务管理
```java
@Service
@Transactional(rollbackFor = Exception.class)
public class BiometricServiceImpl implements BiometricService {

    @Resource
    private BiometricManager biometricManager;

    @Override
    public AuthenticationResult authenticate(AuthenticationRequest request) {
        // 业务逻辑处理
        return biometricManager.processAuthentication(request);
    }
}
```

**规范要求**:
- ✅ 必须使用 `@Transactional` 管理事务
- ✅ 必须使用 `@Resource` 进行依赖注入
- ✅ 业务逻辑必须在此层处理
- ✅ 异常处理和日志记录
- ❌ 禁止直接操作数据库
- ❌ 禁止跨模块直接调用

### Manager 层（业务封装层）
**职责**: 复杂业务逻辑封装，跨模块调用
```java
@Component
public class BiometricManager {

    @Resource
    private BiometricDao biometricDao;

    @Resource
    private AuthenticationStrategyManager strategyManager;

    public AuthenticationResult processAuthentication(AuthenticationRequest request) {
        // 复杂业务逻辑封装
        BiometricEntity entity = biometricDao.selectById(request.getUserId());
        return strategyManager.authenticate(entity, request);
    }
}
```

**规范要求**:
- ✅ 封装复杂业务逻辑
- ✅ 协调多个DAO或服务
- ✅ 跨模块业务处理
- ❌ 禁止管理事务（事务在Service层）
- ❌ 禁止直接处理HTTP请求

### DAO 层（数据访问层）
**职责**: 数据访问，使用MyBatis Plus
```java
@Mapper
public interface BiometricDao extends BaseMapper<BiometricEntity> {

    @Select("SELECT * FROM t_biometric_user WHERE user_id = #{userId} AND deleted_flag = 0")
    BiometricEntity selectByUserId(@Param("userId") Long userId);
}
```

**规范要求**:
- ✅ 必须继承 `BaseMapper<T>`
- ✅ 使用MyBatis Plus注解或XML配置
- ✅ 软删除查询必须包含 `deleted_flag = 0`
- ✅ 复杂查询使用XML配置文件
- ❌ 禁止在DAO层编写业务逻辑
- ❌ 禁止直接调用其他DAO

## 🗄️ 数据库设计规范

### 表命名规范
- **格式**: `t_{business}_{entity}`
- **示例**: `t_biometric_user`, `t_authentication_record`
- **字符集**: `utf8mb4`
- **存储引擎**: `InnoDB`

### 字段命名规范
- **主键**: `{table}_id` (BIGINT AUTO_INCREMENT)
- **外键**: `{referenced_table}_id`
- **时间字段**: `{event}_time` (DATETIME)
- **状态字段**: `{entity}_status` (TINYINT)
- **标志字段**: `{entity}_flag` (TINYINT)

### 必须包含的审计字段
```sql
create_time      DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
update_time      DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
create_user_id   BIGINT      COMMENT '创建人ID',
deleted_flag     TINYINT     DEFAULT 0 COMMENT '删除标志 0-正常 1-删除'
```

### 索引规范
- **主键索引**: 自动创建
- **唯一索引**: 唯一性约束字段
- **普通索引**: 查询频繁字段
- **复合索引**: 多字段组合查询

## 🔐 安全架构规范

### 认证授权
- **认证框架**: Sa-Token
- **权限控制**: 基于RAC (Resource-Action-Condition) 模型
- **会话管理**: Redis存储会话信息
- **双因子认证**: 支持多种认证方式

### RAC统一权限中间件 (Resource-Action-Condition)
SmartAdmin v3 实现了企业级的RAC权限中间件，提供精细化的权限控制：

#### 架构设计
```
┌─────────────────────────────────────────────────────────────┐
│                    RAC权限中间层架构                            │
├─────────────────────────────────────────────────────────────┤
│  @RequireResource注解 → AuthorizationInterceptor → PolicyEvaluator │
│                           ↓                                │
│                    AuthorizationContext                       │
│                           ↓                                │
│                 DataScopeResolver → 数据域过滤                  │
│                           ↓                                │
│                   业务模块数据访问控制                           │
└─────────────────────────────────────────────────────────────┘
```

#### 核心组件
1. **@RequireResource注解**: 统一权限控制注解
   ```java
   @RequireResource(
       resource = "smart:access:device",
       action = "READ",
       dataScope = DataScope.AREA,
       message = "您没有权限查询门禁设备列表"
   )
   ```

2. **AuthorizationContext**: 权限上下文信息
   ```java
   @Data @Builder
   public class AuthorizationContext {
       private Long userId;
       private String username;
       private Set<String> roleCodes;
       private String resourceCode;
       private String requestedAction;
       private DataScope dataScope;
       private Set<Long> areaIds;
       private Set<Long> deptIds;
       private Map<String, Object> customRules;
       private boolean isSuperAdmin;
   }
   ```

3. **PolicyEvaluator**: RAC策略评估器
   - 权限策略评估逻辑
   - 性能优化（平均评估时间 < 10ms）
   - 缓存机制

4. **DataScopeResolver**: 数据域解析器
   - 数据域权限解析
   - 多级数据域合并
   - 自定义数据域规则

5. **AuthorizationInterceptor**: 权限拦截器
   - AOP切面权限拦截
   - 统一异常处理
   - 权限审计日志

#### RAC权限模型
- **Resource (资源)**: 系统中的受控资源，格式为 `模块:子模块:资源`
  - 示例: `smart:access:device`, `smart:attendance:punch`, `smart:consume:account`

- **Action (操作)**: 对资源的操作类型
  - `READ`: 读取权限 (GET)
  - `WRITE`: 写入权限 (POST/PUT)
  - `DELETE`: 删除权限 (DELETE)
  - `APPROVE`: 审批权限 (特殊业务)
  - `*`: 所有权限

- **Condition (条件)**: 权限生效的条件，主要指数据域权限

#### 数据域权限 (DataScope)
| 数据域 | 枚举值 | 描述 | 适用场景 | 权限范围 |
|--------|--------|------|----------|----------|
| **ALL** | `DataScope.ALL` | 全部数据权限 | 超级管理员 | 可访问所有数据 |
| **AREA** | `DataScope.AREA` | 区域数据权限 | 区域管理员 | 限定可访问的区域范围 |
| **DEPT** | `DataScope.DEPT` | 部门数据权限 | 部门管理员 | 限定可访问的部门范围 |
| **SELF** | `DataScope.SELF` | 个人数据权限 | 普通用户 | 仅限访问本人数据 |
| **CUSTOM** | `DataScope.CUSTOM` | 自定义数据域 | 特殊业务场景 | 根据业务规则自定义 |

#### 实现示例
```java
@RestController
@RequestMapping("/api/smart/access/device")
public class AccessDeviceController {

    @PostMapping("/page")
    @SaCheckPermission("smart:access:device:query")
    @RequireResource(
        resource = "smart:access:device",
        action = "READ",
        dataScope = DataScope.AREA,
        message = "您没有权限查询门禁设备列表"
    )
    public ResponseDTO<PageResult<AccessDeviceEntity>> getDevicePage(
            @Valid @RequestBody PageParam pageParam) {
        return ResponseDTO.ok(accessDeviceService.getDevicePage(pageParam));
    }

    @PostMapping("/remoteOpen/{accessDeviceId}")
    @SaCheckPermission("smart:access:device:control")
    @RequireResource(
        resource = "smart:access:device",
        action = "WRITE",
        dataScope = DataScope.AREA,
        message = "您没有权限远程控制门禁设备"
    )
    public ResponseDTO<String> remoteOpenDoor(@PathVariable Long accessDeviceId) {
        return ResponseDTO.ok(accessDeviceService.remoteOpenDoor(accessDeviceId));
    }
}
```

#### 前端权限控制
```javascript
// 1. Vue指令权限控制
<template>
  <!-- 基础权限检查 -->
  <button v-permission="'smart:access:device:read'">查看设备</button>

  <!-- 数组权限检查（OR逻辑） -->
  <div v-permission="['smart:access:device:write', 'smart:access:device:delete']">
    <button>编辑设备</button>
    <button>删除设备</button>
  </div>

  <!-- 对象格式权限检查 -->
  <button v-permission="{
    resource: 'smart:access:device',
    action: 'WRITE',
    dataScope: 'AREA',
    areaId: currentAreaId
  }">
    远程开门
  </button>
</template>

// 2. 组合式API权限检查
import { usePermission, useDataScope } from '@/utils/permission'

export default {
  setup() {
    const { hasPermission, canRead, canWrite } = usePermission()
    const { hasAreaPermission, hasDeptPermission } = useDataScope()

    const checkDevicePermission = async () => {
      const canEdit = await hasPermission('smart:access:device', 'WRITE')
      const canAccessArea = await hasAreaPermission(currentAreaId)
      return canEdit && canAccessArea
    }

    return { checkDevicePermission }
  }
}

// 3. 路由权限守卫
const routes = [
  {
    path: '/access/device',
    component: () => import('@/views/access/DeviceList.vue'),
    meta: {
      requireAuth: true,
      permission: {
        resource: 'smart:access:device',
        action: 'READ',
        dataScope: 'AREA'
      }
    }
  }
]
```

#### 权限配置示例

##### 门禁系统权限配置
| 资源编码 | 资源名称 | 动作 | 数据域 | 角色要求 | 说明 |
|----------|----------|------|--------|----------|------|
| `smart:access:device` | 门禁设备管理 | READ/WRITE/DELETE | AREA/DEPT | 门禁管理员 | 设备的增删改查 |
| `smart:access:verify` | 门禁通行校验 | READ | SELF/AREA | 所有用户 | 通行权限验证 |
| `smart:access:remote` | 远程开门控制 | WRITE | AREA | 区域管理员 | 远程开门功能 |
| `smart:access:log` | 门禁记录查询 | READ | DEPT/AREA | 安全管理员 | 访问记录查看 |

##### 考勤系统权限配置
| 资源编码 | 资源名称 | 动作 | 数据域 | 角色要求 | 说明 |
|----------|----------|------|--------|----------|------|
| `smart:attendance:punch` | 考勤打卡 | WRITE | SELF | 所有用户 | 个人考勤打卡 |
| `smart:attendance:record` | 考勤记录查询 | READ | SELF/DEPT | 部门主管 | 考勤记录查看 |
| `smart:attendance:schedule` | 排班管理 | WRITE | DEPT | 考勤管理员 | 排班规则管理 |
| `smart:attendance:statistics` | 考勤统计 | READ | DEPT/AREA | 管理层 | 考勤数据统计 |

##### 消费系统权限配置
| 资源编码 | 资源名称 | 动作 | 数据域 | 角色要求 | 说明 |
|----------|----------|------|--------|----------|------|
| `smart:consume:account` | 消费账户管理 | READ/WRITE | SELF/DEPT | 财务管理员 | 账户余额管理 |
| `smart:consume:pay` | 消费支付 | WRITE | SELF | 所有用户 | 个人消费支付 |
| `smart:consume:record` | 消费记录查询 | READ | SELF/DEPT | 财务人员 | 消费记录查看 |
| `smart:consume:refund` | 消费退款 | WRITE | DEPT | 消费管理员 | 退款操作处理 |

#### 权限缓存机制
- **多级缓存**: Caffeine (本地) + Redis (分布式)
- **缓存TTL**: 30分钟（可配置）
- **缓存失效**: 权限变更时主动清除
- **性能指标**: 权限检查平均响应时间 < 5ms

#### 权限审计
- **权限检查日志**: 记录所有权限验证结果
- **越权尝试告警**: 检测并告警权限越权行为
- **权限变更审计**: 记录权限配置的变更历史
- **数据访问审计**: 记录敏感数据的访问情况

### 数据安全
- **数据脱敏**: 敏感信息自动脱敏
- **接口加解密**: 支持国密和国际算法
- **SQL注入防护**: 使用预编译语句
- **XSS防护**: 输入输出过滤

### 操作审计
- **登录日志**: 记录所有登录行为
- **操作日志**: 记录关键业务操作
- **数据变更**: 记录数据修改历史
- **异常监控**: 实时监控系统异常

## 🚀 性能架构规范

### 缓存策略
- **缓存类型**: Redis
- **缓存模式**: Cache-Aside
- **缓存失效**: TTL + 主动失效
- **缓存穿透**: 布隆过滤器防护

### 数据库优化
- **读写分离**: 主从数据库配置
- **分库分表**: 大数据量水平拆分
- **连接池**: Druid连接池管理
- **慢查询**: 监控和优化

### 接口性能
- **响应时间**: 接口响应时间 ≤ 500ms
- **并发处理**: 支持1000+并发请求
- **限流保护**: 接口级别限流
- **熔断降级**: 服务降级策略

## 📊 监控架构规范

### 应用监控
- **性能指标**: CPU、内存、GC等
- **业务指标**: 请求量、成功率、响应时间
- **异常监控**: 异常捕获和告警
- **日志监控**: 结构化日志分析

### 基础设施监控
- **服务器监控**: 系统资源使用情况
- **网络监控**: 网络延迟和带宽
- **数据库监控**: 连接数、慢查询等
- **缓存监控**: 命中率、内存使用等

## ⚙️ 生物识别监控配置

为保证多模态生物识别稳定运行, 新增以下强制配置项:

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `smart.biometric.monitor.snapshot-ttl` | `30s` | 心跳超时时间, 超过即标记 `stale=true`。 |
| `smart.biometric.monitor.alert-sla` | `30s` | 告警响应SLA, 未在该时间内确认将进入逾期列表。 |
| `smart.biometric.monitor.dashboard-poll` | `5s` | 仪表盘轮询频率, 大屏和后端保持一致。 |
| `smart.biometric.monitor.webhook-enabled` | `true` | 是否开启Webhook, 用于转发CRITICAL告警到企业微信/飞书。 |

**落地要求**:

1. 所有配置必须录入配置中心(Nacos/Apollo), 禁止硬编码。
2. 不同环境的值可调整, 但必须记录在《环境参数表》中。
3. 运维在上线前需验证 `snapshot-ttl` 与实际心跳频率匹配(心跳=10s → ttl≥20s)。
4. 告警SLA与值班制度绑定, 若策略调整需同步更新该配置并归档。

## 🔧 开发规范要求

### RAC权限开发规范 (强制要求)

#### Controller层权限控制标准
- **必须添加**: `@SaCheckLogin` 验证用户登录状态
- **必须添加**: `@RequireResource` 进行RAC权限控制
- **资源编码规范**: 格式为 `模块:子模块:资源`，如 `smart:access:device`
- **动作类型明确**: READ/WRITE/DELETE/APPROVE/*，禁止使用模糊描述
- **数据域精确**: 根据业务场景选择合适的数据域类型
- **错误提示友好**: 权限拒绝时提供清晰的用户提示信息

#### 数据域权限使用约定
| 业务场景 | 推荐数据域 | 示例资源编码 | 说明 |
|----------|------------|--------------|------|
| 超级管理员功能 | `ALL` | `system:config:*` | 仅限超级管理员 |
| 区域管理功能 | `AREA` | `smart:access:device` | 限定用户可访问的区域 |
| 部门管理功能 | `DEPT` | `smart:attendance:record` | 限定用户可访问的部门 |
| 个人操作功能 | `SELF` | `smart:consume:pay` | 仅限用户本人操作 |
| 系统级接口 | `NONE` | `system:health:check` | 无数据域限制 |

#### 权限测试要求
- **单元测试覆盖率**: RAC权限相关测试覆盖率 ≥ 80%
- **集成测试**: 必须验证权限注解在实际Controller中的效果
- **边界测试**: 测试各种权限边界和异常情况
- **性能测试**: 权限检查平均响应时间 < 10ms

#### 权限缓存规范
- **缓存TTL**: 权限检查结果缓存30分钟
- **缓存失效**: 权限变更时立即清除相关缓存
- **缓存策略**: 本地Caffeine + 分布式Redis多级缓存
- **性能监控**: 监控权限缓存命中率和响应时间

### 代码质量
- **编译检查**: 零编译错误
- **单元测试**: 覆盖率 ≥ 80%
- **代码审查**: 强制Peer Review
- **静态检查**: SonarQube质量扫描
- **权限审计**: 所有权限变更必须记录审计日志

### 文档要求
- **API文档**: Swagger/OpenAPI规范，包含权限说明
- **架构文档**: 重要模块架构设计，包含权限模型
- **部署文档**: 部署和运维指南，包含权限配置
- **故障手册**: 常见问题排查指南，包含权限相关故障

---

**重要提醒**: 违反架构规范将导致系统稳定性问题，必须严格遵循！