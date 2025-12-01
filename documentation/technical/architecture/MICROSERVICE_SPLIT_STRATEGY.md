# IOE-DREAM微服务拆分策略设计文档

## 文档信息
- **文档版本**: v1.0.0
- **创建时间**: 2025-01-13
- **最后更新**: 2025-01-13
- **作者**: SmartAdmin Team
- **审批状态**: 待审批

---

## 📋 项目现状分析

### 现有架构概况
基于对IOE-DREAM项目916个Java文件的深度分析：

#### 文件分布统计
```
总Java文件数: 916个
├── sa-admin (主要业务模块): 612个
├── sa-base (公共模块): 187个
├── sa-support (支持模块): 117个

按业务模块分布:
├── 用户权限相关: ~45个文件
├── 门禁管理相关: ~75个文件
├── 消费管理相关: ~168个文件 (100%完成)
├── 考勤管理相关: ~88个文件
├── 视频监控相关: ~65个文件
├── 通知服务相关: ~35个文件
├── 文件服务相关: ~28个文件
└── 系统监控相关: ~42个文件
```

#### 技术栈现状
- **后端框架**: Spring Boot 3.5.7 + Jakarta EE
- **数据库**: MySQL 8.0 + Redis
- **架构模式**: 四层架构 (Controller→Service→Manager→DAO)
- **权限框架**: Sa-Token
- **ORM框架**: MyBatis Plus
- **缓存**: Caffeine (L1) + Redis (L2)

---

## 🎯 微服务化目标

### 业务目标
1. **提升系统可扩展性**: 支持业务快速迭代和独立部署
2. **降低系统耦合度**: 实现业务模块独立演进
3. **提高系统可用性**: 单个服务故障不影响整体系统
4. **增强开发效率**: 团队并行开发，缩短交付周期

### 技术目标
1. **服务独立性**: 每个服务独立开发、测试、部署
2. **数据自治**: 每个服务管理自己的数据
3. **技术多样性**: 允许不同服务使用不同技术栈
4. **弹性伸缩**: 根据负载动态调整服务实例

---

## 🏗️ 微服务拆分原则

### 1. 领域驱动设计(DDD)原则
```
单一职责原则: 每个服务只负责一个业务领域
高内聚低耦合: 服务内部高内聚，服务之间低耦合
限界上下文: 明确的业务边界和数据边界
聚合根设计: 每个服务有自己的聚合根
```

### 2. 业务能力原则
```
业务能力拆分: 按业务能力而非技术层次拆分
数据一致性: 服务内部强一致性，服务间最终一致性
接口稳定性: 服务间接口稳定，避免频繁变更
故障隔离: 单个服务故障不影响其他服务
```

### 3. 团队组织原则
```
Conway定律: 组织架构决定系统架构
团队自治: 每个服务由独立团队负责
沟通成本: 减少跨团队沟通成本
技术决策: 团队内部技术决策自主权
```

---

## 📊 服务拆分策略

### 第一层：核心业务服务

#### 1. 用户权限服务 (smart-auth-service)
**职责范围**:
- 用户认证授权
- 员工信息管理
- 角色权限管理
- 组织架构管理
- 数据权限控制

**拆分依据**:
- 基础服务，其他服务都依赖
- 相对独立的业务域
- 数据变更频率较低
- 业务逻辑相对稳定

**数据范围**:
```sql
-- 用户权限服务数据库表
t_employee              -- 员工表
t_role                  -- 角色表
t_permission            -- 权限表
t_department            -- 部门表
t_user_role             -- 用户角色关系表
t_role_permission       -- 角色权限关系表
t_employee_dept         -- 员工部门关系表
```

**API接口**:
```yaml
核心API:
  POST /api/auth/login              # 用户登录
  POST /api/auth/logout             # 用户登出
  GET  /api/auth/user/info          # 获取用户信息
  GET  /api/employee/list           # 员工列表
  GET  /api/role/tree               # 角色树
  GET  /api/department/tree          # 部门树

管理API:
  POST /api/employee/add            # 新增员工
  PUT  /api/employee/update         # 更新员工
  DELETE /api/employee/{id}         # 删除员工
  POST /api/role/add                # 新增角色
  PUT  /api/role/update             # 更新角色
```

**技术特性**:
- 读写分离: 读取频率远高于写入
- 多级缓存: 用户信息缓存
- SSO支持: 支持单点登录
- 数据权限: 细粒度数据权限控制

#### 2. 区域管理服务 (smart-area-service)
**职责范围**:
- 区域信息管理
- 区域层级关系
- 人员区域权限
- 设备区域关系
- 区域分发策略

**拆分依据**:
- 基础共享服务，多个业务服务依赖
- 数据变更频率较低
- 权限控制核心模块
- 独立的数据域边界

**数据范围**:
```sql
-- 区域管理服务数据库表
t_area                 -- 区域表
t_person_area_relation -- 人员区域关系表
t_area_access_ext      -- 区域访问扩展表
t_device_dispatch_record -- 设备分发记录表
t_device_dispatch_strategy -- 设备分发策略表
```

**API接口**:
```yaml
区域管理:
  GET  /api/area/tree                 # 区域树
  POST /api/area/add                  # 新增区域
  PUT  /api/area/update               # 更新区域
  DELETE /api/area/{id}               # 删除区域

人员区域关系:
  GET  /api/person-area/relation/list # 人员区域关系列表
  POST /api/person-area/batch         # 批量设置人员区域关系
  DELETE /api/person-area/{id}        # 删除人员区域关系

设备区域关系:
  GET  /api/device-area/relation/list # 设备区域关系列表
  POST /api/device-area/dispatch      # 设备区域分发
```

**技术特性**:
- 多级缓存: 区域树缓存
- 数据权限: 基于区域的权限控制
- 批量操作: 支持批量设置权限
- 同步机制: 多系统数据同步

#### 3. 门禁服务 (smart-access-service)
**职责范围**:
- 门禁设备管理
- 门禁记录管理
- 访客预约管理
- 实时监控控制
- 门禁权限验证

**拆分依据**:
- 独立的业务域
- 高实时性要求
- 硬件交互较多
- 访客管理专业化

**数据范围**:
```sql
-- 门禁服务数据库表
t_access_device        -- 门禁设备表
t_access_record        -- 门禁记录表
t_visitor              -- 访客表
t_visitor_reservation  -- 访客预约表
t_visitor_permission   -- 访客权限表
t_device_area          -- 设备区域关系表 (引用区域服务)
```

**API接口**:
```yaml
设备管理:
  GET  /api/device/list              # 设备列表
  POST /api/device/add               # 新增设备
  PUT  /api/device/update            # 更新设备
  DELETE /api/device/{id}            # 删除设备

门禁控制:
  POST /api/door/open                # 开门
  GET  /api/door/status/{id}         # 门状态
  GET  /api/access/record/list       # 门禁记录

访客管理:
  POST /api/visitor/reservation      # 访客预约
  GET  /api/visitor/reservation/list # 访客预约列表
  PUT  /api/visitor/approve/{id}     # 审批访客
  GET  /api/visitor/record/list      # 访客记录列表
  POST /api/visitor/checkin          # 访客签到
  POST /api/visitor/checkout         # 访客签退
```

**技术特性**:
- WebSocket实时监控
- 设备状态缓存
- 高并发写入支持
- 数据分区存储

#### 3. 消费服务 (smart-consume-service)
**职责范围**:
- 账户管理
- 消费记录
- 充值管理
- 退款处理
- 对账结算

**拆分依据**:
- 财务相关，数据敏感性高
- 事务一致性要求高
- 业务规则复杂
- 独立的审计需求

**数据范围**:
```sql
-- 消费服务数据库表
t_account              # 账户表
t_consume_record       # 消费记录表
t_recharge_record      # 充值记录表
t_refund_record        # 退款记录表
t_account_transaction  # 账户交易表
t_consume_limit        # 消费限制表
```

**API接口**:
```yaml
账户管理:
  GET  /api/account/info/{userId}    # 账户信息
  POST /api/account/recharge         # 账户充值
  GET  /api/account/transaction      # 交易记录

消费业务:
  POST /api/consume/pay              # 消费支付
  GET  /api/consume/record/list      # 消费记录
  POST /api/consume/refund           # 申请退款

对账管理:
  GET  /api/reconciliation/daily      # 日对账
  GET  /api/reconciliation/monthly    # 月对账
```

**技术特性**:
- 分布式事务
- 数据一致性保障
- 高安全性要求
- 审计日志完整

#### 4. 消费服务 (smart-consume-service)
**职责范围**:
- 账户管理
- 消费记录
- 充值管理
- 退款处理
- 对账结算

**拆分依据**:
- 业务逻辑复杂
- 规则引擎需求
- 统计计算密集
- 独立的数据分析

**数据范围**:
```sql
-- 考勤服务数据库表
t_attendance_rule     # 考勤规则表
t_work_schedule       # 排班表
t_attendance_record   # 考勤记录表
t_attendance_exception # 考勤异常表
t_attendance_statistics # 考勤统计表
```

**API接口**:
```yaml
考勤管理:
  GET  /api/attendance/record/list   # 考勤记录
  POST /api/attendance/clock          # 打卡
  GET  /api/attendance/statistics     # 考勤统计

排班管理:
  GET  /api/schedule/list             # 排班列表
  POST /api/schedule/create           # 创建排班
  PUT  /api/schedule/update           # 更新排班
```

**技术特性**:
- 规则引擎
- 批量计算
- 报表生成
- 数据分析

#### 6. 考勤服务 (smart-attendance-service)
**职责范围**:
- 考勤规则管理
- 排班管理
- 打卡记录
- 异常处理
- 统计报表

**拆分依据**:
- 业务逻辑复杂
- 规则引擎需求
- 批量计算
- 报表生成
- 数据分析

**数据范围**:
```sql
-- 考勤服务数据库表
t_attendance_rule     # 考勤规则表
t_work_schedule       # 排班表
t_attendance_record   # 考勤记录表
t_attendance_exception # 考勤异常表
t_attendance_statistics # 考勤统计表
```

**API接口**:
```yaml
考勤管理:
  GET  /api/attendance/record/list   # 考勤记录
  POST /api/attendance/clock          # 打卡
  GET  /api/attendance/statistics     # 考勤统计

排班管理:
  GET  /api/schedule/list             # 排班列表
  POST /api/schedule/create           # 创建排班
  PUT  /api/schedule/update           # 更新排班
```

**技术特性**:
- 规则引擎
- 批量计算
- 报表生成
- 数据分析

#### 7. 视频监控服务 (smart-video-service)
**职责范围**:
- 视频设备管理
- 实时视频流
- 录像存储
- 智能分析
- 历史回放

**拆分依据**:
- 媒体处理专业性
- 存储需求大
- 实时性要求高
- AI算法集成

**数据范围**:
```sql
-- 视频服务数据库表
t_video_device         # 视频设备表
t_video_stream         # 视频流表
t_video_record         # 录像记录表
t_ai_analysis          # AI分析结果表
t_video_device_area    # 设备区域关系表
```

**API接口**:
```yaml
设备管理:
  GET  /api/video/device/list       # 视频设备列表
  POST /api/video/device/add         # 新增设备

实时监控:
  GET  /api/video/stream/{deviceId}  # 获取视频流
  POST /api/video/record/start       # 开始录像
  POST /api/video/record/stop        # 停止录像

历史回放:
  GET  /api/video/record/list        # 录像列表
  GET  /api/video/playback/{id}      # 播放录像
```

**技术特性**:
- 流媒体处理
- 分布式存储
- AI算法集成
- 高带宽支持

### 第二层：支撑服务

#### 8. 通知服务 (smart-notification-service)
**职责范围**:
- 消息推送
- 邮件通知
- 短信服务
- 站内信
- 模板管理

**拆分依据**:
- 通用支撑服务
- 多渠道整合
- 消息队列依赖
- 异步处理需求

#### 9. 文件服务 (smart-file-service)
**职责范围**:
- 文件上传
- 文件存储
- 文件预览
- 文件下载
- 文件管理

**拆分依据**:
- 存储服务专业化
- 多存储支持
- 文件安全处理
- CDN集成需求

#### 10. 监控服务 (smart-monitor-service)
**职责范围**:
- 系统监控
- 业务监控
- 告警管理
- 性能分析
- 健康检查

**拆分依据**:
- 运维支撑服务
- 数据采集专业
- 可视化需求
- 实时分析要求

---

## 🔄 数据迁移策略

### 迁移原则
1. **渐进式迁移**: 逐个服务迁移，降低风险
2. **数据同步**: 确保迁移过程中数据一致性
3. **回滚能力**: 支持快速回滚到原有架构
4. **业务无感知**: 迁移过程不影响业务使用

### 迁移步骤

#### 第一阶段：用户权限服务迁移
```bash
# 1. 数据库准备
CREATE DATABASE smart_auth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 2. 表结构创建
USE smart_auth;
-- 创建用户权限相关表

# 3. 数据迁移
INSERT INTO smart_auth.t_employee
SELECT * FROM smart_admin_v3.t_employee;

# 4. 验证数据一致性
SELECT COUNT(*) FROM smart_auth.t_employee;
SELECT COUNT(*) FROM smart_admin_v3.t_employee;
```

#### 第二阶段：其他服务迁移
按照相同模式逐个迁移其他服务

### 服务间通信设计

#### 1. 同步通信
```java
// Feign客户端定义
@FeignClient(name = "smart-auth-service", path = "/api/auth")
public interface AuthServiceClient {

    @GetMapping("/user/info/{userId}")
    ResponseDTO<UserInfoVO> getUserInfo(@PathVariable Long userId);

    @PostMapping("/permission/check")
    ResponseDTO<Boolean> checkPermission(
        @RequestParam String resource,
        @RequestParam String action);
}
```

#### 2. 异步通信
```java
// 事件发布
@Component
public class ConsumeEventPublisher {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void publishConsumeEvent(ConsumeRecordEntity record) {
        ConsumeEvent event = ConsumeEvent.builder()
            .userId(record.getUserId())
            .amount(record.getAmount())
            .timestamp(System.currentTimeMillis())
            .build();

        eventPublisher.publishEvent(event);
    }
}

// 事件监听
@Component
public class NotificationEventListener {

    @EventListener
    public void handleConsumeEvent(ConsumeEvent event) {
        // 发送消费通知
        notificationService.sendConsumeNotification(event);
    }
}
```

---

## 🛡️ 技术保障措施

### 1. 服务注册发现
```yaml
# Nacos配置
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
        namespace: ioe-dream
        group: DEFAULT_GROUP
        heart-beat-interval: 5000
        heart-beat-timeout: 15000
        ip-delete-timeout: 30000
```

### 2. 配置管理
```yaml
# 分布式配置
spring:
  cloud:
    nacos:
      config:
        server-addr: localhost:8848
        namespace: ioe-dream
        group: DEFAULT_GROUP
        file-extension: yml
        shared-configs:
          - data-id: common-mysql.yml
            group: DEFAULT_GROUP
            refresh: true
          - data-id: common-redis.yml
            group: DEFAULT_GROUP
            refresh: true
```

### 3. 负载均衡
```yaml
# 负载均衡配置
spring:
  cloud:
    loadbalancer:
      ribbon:
        enabled: false
      health:
        check-interval: 10s
        health-indicator-path: /actuator/health
```

### 4. 熔断降级
```java
@RestController
@DefaultProperties(defaultFallback = "defaultFallback")
public class ConsumeController {

    @GetMapping("/account/info/{userId}")
    @HystrixCommand(fallbackMethod = "getAccountInfoFallback")
    public ResponseDTO<AccountVO> getAccountInfo(@PathVariable Long userId) {
        return accountService.getAccountInfo(userId);
    }

    public ResponseDTO<AccountVO> getAccountInfoFallback(Long userId) {
        return ResponseDTO.error("服务暂时不可用，请稍后重试");
    }

    public ResponseDTO<String> defaultFallback() {
        return ResponseDTO.error("系统维护中，请稍后访问");
    }
}
```

### 5. 分布式事务
```java
@Service
@RequiredArgsConstructor
public class ConsumeTransactionService {

    @GlobalTransactional(rollbackFor = Exception.class)
    public ResponseDTO<String> processConsume(ConsumeRequest request) {
        try {
            // 1. 扣减账户
            accountService.deduct(request.getUserId(), request.getAmount());

            // 2. 创建消费记录
            consumeService.createRecord(request);

            // 3. 发送通知
            notificationService.sendNotification(request);

            return ResponseDTO.ok("消费成功");

        } catch (Exception e) {
            log.error("消费处理失败", e);
            throw new BusinessException("消费失败");
        }
    }
}
```

---

## 📈 性能优化策略

### 1. 数据库优化
```sql
-- 读写分离配置
# 主库配置
spring.datasource.primary.url=jdbc:mysql://master-host:3306/smart_auth
spring.datasource.primary.username=root
spring.datasource.primary.password=password

# 从库配置
spring.datasource.replica.url=jdbc:mysql://slave-host:3306/smart_auth
spring.datasource.replica.username=root
spring.datasource.replica.password=password

-- 分库分表策略
# 按用户ID分表
t_consume_record_0, t_consume_record_1, ..., t_consume_record_9
```

### 2. 缓存策略
```java
@Service
@RequiredArgsConstructor
public class UserInfoCacheManager {

    private final RedisTemplate<String, Object> redisTemplate;

    @Cacheable(value = "userInfo", key = "#userId", unless = "#result == null")
    public UserInfoVO getUserInfo(Long userId) {
        return userService.getUserInfoFromDB(userId);
    }

    @CacheEvict(value = "userInfo", key = "#userId")
    public void evictUserInfoCache(Long userId) {
        // 缓存失效
    }

    // 本地缓存 + 分布式缓存
    private final Cache<Long, UserInfoVO> localCache =
        Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();
}
```

### 3. 连接池优化
```yaml
# 数据库连接池优化
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 20000
      leak-detection-threshold: 60000

# Redis连接池优化
  data:
    redis:
      lettuce:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5
          max-wait: 2000ms
```

---

## 🔍 监控告警体系

### 1. 应用监控
```yaml
# Spring Boot Actuator配置
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true
```

### 2. 链路追踪
```yaml
# SkyWalking配置
spring:
  application:
    name: smart-consume-service
skywalking:
  service-name: ${spring.application.name}
  collector:
    backend-service: localhost:11800
  agent:
    sampling:
      per-3-sampling-rate: 1
```

### 3. 日志管理
```xml
<!-- Logback配置 -->
<configuration>
    <springProfile name="!prod">
        <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
        <include resource="org/springframework/boot/logging/logback/console-appender.xml"/>
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>

    <springProfile name="prod">
        <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>logs/smart-consume-service.log</file>
            <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                <fileNamePattern>logs/smart-consume-service.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
                <maxFileSize>100MB</maxFileSize>
                <maxHistory>30</maxHistory>
                <totalSizeCap>3GB</totalSizeCap>
            </rollingPolicy>
            <encoder>
                <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>

        <root level="INFO">
            <appender-ref ref="FILE"/>
        </root>
    </springProfile>
</configuration>
```

---

## 📋 实施计划

### 第一阶段：基础设施搭建 (2-3周)
- [ ] 微服务父POM创建
- [ ] 公共模块开发
- [ ] API网关搭建
- [ ] Nacos服务注册发现
- [ ] 分布式配置中心

### 第二阶段：核心服务拆分 (4-6周)
- [ ] 用户权限服务迁移
- [ ] 门禁服务迁移
- [ ] 消费服务迁移
- [ ] 考勤服务迁移
- [ ] 视频监控服务迁移

### 第三阶段：支撑服务拆分 (2-3周)
- [ ] 通知服务拆分
- [ ] 文件服务拆分
- [ ] 监控服务拆分

### 第四阶段：优化完善 (2-3周)
- [ ] 性能优化
- [ ] 监控告警
- [ ] 文档完善
- [ ] 培训交付

### 总计：10-15周

---

## ⚠️ 风险控制

### 1. 技术风险
- **服务依赖复杂**: 通过服务网格管理依赖关系
- **数据一致性**: 使用分布式事务保障
- **性能下降**: 通过缓存和优化提升性能
- **运维复杂**: 自动化运维和监控

### 2. 业务风险
- **业务中断**: 蓝绿部署和灰度发布
- **数据丢失**: 数据备份和恢复机制
- **功能缺失**: 完整的测试验证
- **用户体验**: 兼容性保障

### 3. 团队风险
- **技能不足**: 培训和知识分享
- **沟通成本**: 规范和工具支持
- **责任不清**: 明确的团队职责
- **进度延期**: 合理的里程碑设置

---

## 📊 成功标准

### 技术指标
- [ ] 服务可用性 ≥ 99.9%
- [ ] API响应时间 P95 ≤ 200ms
- [ ] 系统吞吐量提升 50%
- [ ] 部署效率提升 80%

### 业务指标
- [ ] 功能完整率 100%
- [ ] 性能不降反升
- [ ] 用户满意度 ≥ 95%
- [ ] 运维效率提升 60%

### 团队指标
- [ ] 开发效率提升 40%
- [ ] 发布频率提升 3倍
- [ ] 故障恢复时间缩短 70%
- [ ] 技术债务减少 50%

---

## 📚 参考资料

1. **《微服务设计》** - Sam Newman
2. **《领域驱动设计》** - Eric Evans
3. **Spring Cloud官方文档**
4. **Nacos官方文档**
5. **阿里巴巴微服务实践**

---

**文档维护**: 本文档将随着项目进展持续更新，确保与实际架构设计保持一致。