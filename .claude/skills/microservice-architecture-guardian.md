# 🏗️ 微服务架构守护专家技能

> **文档版本**: v2.0.0
> **状态**: [架构升级中]
> **创建时间**: 2025-11-26
> **最后更新**: 2025-11-26
> **作者**: IOE-DREAM架构治理委员会
> **审批人**: 微服务架构专家委员会
> **变更类型**: MAJOR (微服务架构升级)
> **关联代码版本**: IOE-DREAM v3.0.0
> **技能名称**: 微服务架构守护专家
> **技能等级**: ★★★ 高级
> **适用角色**: 微服务架构师、技术负责人、系统架构师
> **前置技能**: 微服务架构设计、DDD领域驱动设计、Spring Cloud
> **预计学时**: 40小时

---

## 📋 变更历史

| 版本 | 日期 | 变更内容 | 变更人 | 审批人 | 变更类型 |
|------|------|----------|--------|--------|----------|
| v1.1.0 | 2025-11-25 | 集成文档版本化体系，添加完整变更历史和质量指标 | SmartAdmin Team | 技术架构委员会 | MINOR |
| v1.0.0 | 2025-11-16 | 初始版本，四层架构规范完整指南 | SmartAdmin Team | 技术架构委员会 | MAJOR |

---

## 📊 技能质量指标

| 指标名称 | 目标值 | 当前值 | 状态 |
|---------|--------|--------|------|
| **微服务架构合规率** | 100% | 95% | ⚠️ 需要改进 |
| **API契约一致率** | 100% | 98% | ✅ 良好 |
| **服务解耦度** | ≥80% | 75% | ⚠️ 需要改进 |
| **数据一致性保障** | 100% | 90% | ⚠️ 需要改进 |
| **自动化检查覆盖率** | ≥90% | 85% | ⚠️ 需要改进 |

---

## 📚 知识要求

### 理论知识
- **微服务架构原则**: 深入理解微服务设计原则和最佳实践
- **领域驱动设计(DDD)**: 理解限界上下文、聚合、实体、值对象等DDD核心概念
- **API契约优先**: 掌握OpenAPI规范和API设计先行的方法论
- **分布式系统设计**: 理解CAP理论、BASE理论和分布式事务处理

### 业务理解
- **IOE-DREAM微服务架构**: 深入理解API契约层→微服务实现层→基础设施层架构
- **业务领域划分**: 理解消费、门禁、考勤等业务域的边界和职责
- **服务间通信**: 掌握RESTful API、事件驱动、消息队列等通信方式
- **数据一致性**: 理解Saga模式、事件存储、最终一致性等概念

### 技术背景
- **Spring Cloud微服务**: 熟练掌握Spring Boot 3.x + Spring Cloud生态
- **API网关技术**: 掌握Spring Cloud Gateway、Kong等API网关实现
- **服务发现与注册**: 理解Nacos、Eureka等服务注册发现机制
- **容器化技术**: 熟悉Docker、Kubernetes等容器化部署
- **监控与可观测性**: 掌握Prometheus、Grafana、ELK等监控技术栈

---

## 🛠️ 操作步骤

### 1. 微服务架构三层设计

#### 步骤1: 微服务架构层级定义
```java
// 第一层：API契约层 - 接口定义和版本管理
// 位置：ioe-dream-api/模块
@OpenAPIDefinition(info = @Info(title = "设备管理API", version = "v1.0"))
@RestController
@RequestMapping("/api/v1/device")
public class DeviceControllerV1 {
    // 职责：API接口暴露、参数验证、协议转换、调用微服务
    // 禁止：编写业务逻辑、直接访问数据库、跨服务调用
}

// 第二层：微服务实现层 - 业务逻辑和数据管理
// 位置：ioe-dream-service/模块
@Service
@Transactional
public class DeviceServiceImpl implements DeviceService {
    // 职责：业务逻辑处理、事务管理、数据持久化、事件发布
    // 禁止：处理HTTP协议、跨数据库事务、直接返回ResponseEntity
}

// 第三层：基础设施层 - 技术支撑和公共服务
// 位置：ioe-dream-infrastructure/
@Component
public class ServiceRegistryManager {
    // 职责：服务发现、配置管理、监控告警、日志聚合
    // 禁止：业务逻辑处理、数据存储、HTTP接口暴露
}
```

#### 步骤2: 微服务架构边界定义
```java
// 微服务架构：API契约层 ↔ 微服务实现层 ↔ 基础设施层
// 严格的服务边界和职责分离

// ✅ 正确的微服务架构
// 1. API契约层 (ioe-dream-api/device)
@RestController
@RequestMapping("/api/v1/device")
public class DeviceControllerV1 {
    @Resource
    private DeviceClient deviceClient;    // ✅ 调用微服务客户端
}

// 2. 微服务实现层 (ioe-dream-service/device)
@Service
public class DeviceServiceImpl implements DeviceService {
    @Resource
    private DeviceRepository deviceRepository; // ✅ 数据访问抽象
    @Resource
    private EventPublisher eventPublisher;     // ✅ 事件发布
}

// 3. 基础设施层 (ioe-dream-infrastructure/)
@Component
public class DatabaseConfigManager {
    // ✅ 数据库连接池配置和管理
}

// ❌ 错误的架构（严格禁止）
// 1. 单体应用中混合所有服务
@RestController
@RequestMapping("/api")  // ❌ 统一API入口，违反微服务原则
public class UnifiedController {
    @Resource
    private DeviceService deviceService;      // ❌ 直接注入业务服务
    @Resource
    private ConsumeService consumeService;    // ❌ 跨模块直接依赖
}
```

### 2. API契约层严格实施

#### 步骤1: API契约层标准实现
```java
// API契约层 (ioe-dream-api/device-api)
@RestController
@RequestMapping("/api/v1/device")
@Tag(name = "设备管理API v1.0", description = "设备管理相关接口")
public class DeviceControllerV1 {

    @Resource
    private DeviceClient deviceClient;  // 微服务客户端，不是Service

    @PostMapping("/devices")
    @PreAuthorize("hasAuthority('device:add')")
    @Operation(summary = "新增设备", description = "创建新的设备记录")
    public ResponseEntity<ApiResponse<DeviceVO>> addDevice(
            @Valid @RequestBody CreateDeviceRequest request) {

        // ✅ 正确：只做API协议转换和参数验证
        DeviceVO result = deviceClient.createDevice(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/devices")
    @PreAuthorize("hasAuthority('device:read')")
    @Operation(summary = "获取设备列表", description = "分页查询设备信息")
    public ResponseEntity<ApiResponse<PageResult<DeviceVO>>> getDevices(
            @Valid DeviceQueryRequest query) {

        // ✅ 正确：只做查询参数传递
        PageResult<DeviceVO> result = deviceClient.queryDevices(query);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PutMapping("/devices/{deviceId}")
    @PreAuthorize("hasAuthority('device:update')")
    @Operation(summary = "更新设备", description = "更新设备信息")
    public ResponseEntity<ApiResponse<DeviceVO>> updateDevice(
            @PathVariable String deviceId,
            @Valid @RequestBody UpdateDeviceRequest request) {

        // ✅ 正确：只做参数传递和路由
        DeviceVO result = deviceClient.updateDevice(deviceId, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
```

#### 步骤2: 微服务客户端实现
```java
// 微服务客户端 (ioe-dream-api/device-api)
@FeignClient(name = "device-service", url = "${device.service.url:}")
public interface DeviceClient {

    @PostMapping("/internal/devices")
    DeviceVO createDevice(@Valid CreateDeviceRequest request);

    @GetMapping("/internal/devices")
    PageResult<DeviceVO> queryDevices(@Valid DeviceQueryRequest query);

    @PutMapping("/internal/devices/{deviceId}")
    DeviceVO updateDevice(@PathVariable String deviceId,
                          @Valid UpdateDeviceRequest request);
}
```

#### 步骤3: API契约层约束检查
```bash
# 检查API契约层违规操作
echo "🔍 检查API契约层微服务架构合规性..."

# 1. 检查API层直接注入业务Service（应该通过Client）
controller_direct_service=$(grep -r "@Resource.*Service" --include="*Controller.java" sa-admin/ | wc -l)
if [ $controller_direct_service -gt 0 ]; then
    echo "❌ 发现API层直接注入业务Service: $controller_direct_service 处"
    echo "应该使用微服务Client替代直接Service注入"
    grep -r "@Resource.*Service" --include="*Controller.java" sa-admin/
    exit 1
fi

# 2. 检查API层复杂业务逻辑（应该在微服务中）
api_business_logic=$(grep -r -E "if.*else.*{|for.*{|while.*{" --include="*Controller.java" . | wc -l)
if [ $api_business_logic -gt 15 ]; then
    echo "⚠️ API层可能存在复杂业务逻辑，建议移到微服务中"
fi

# 3. 检查API层事务注解（不应该有）
api_transaction=$(grep -r "@Transactional" --include="*Controller.java" . | wc -l)
if [ $api_transaction -gt 0 ]; then
    echo "❌ API层不应该管理事务: $api_transaction 处"
    exit 1
fi

# 4. 检查API版本管理（必须有）
api_version_check=$(find . -name "*Controller.java" -exec grep -l "@RequestMapping.*v[0-9]" {} \; | wc -l)
echo "API版本化控制器数量: $api_version_check"
```

### 3. 微服务实现层业务处理

#### 步骤1: 微服务实现层标准实现
```java
// 微服务实现层 (ioe-dream-service/device-service)
@Service
@Transactional
@Tag(name = "设备微服务")
public class DeviceServiceImpl implements DeviceService {

    @Resource
    private DeviceRepository deviceRepository;  // 数据访问抽象层

    @Resource
    private EventPublisher eventPublisher;    // 事件发布器

    @Resource
    private DeviceDomainService domainService; // 领域服务

    @Override
    public DeviceVO createDevice(CreateDeviceRequest request) {
        // ✅ 正确：微服务内业务逻辑处理，事务管理，事件发布
        log.info("开始创建设备: {}", request.getDeviceName());

        // 业务规则校验
        if (deviceRepository.existsByCode(request.getDeviceCode())) {
            throw new DeviceBusinessException("设备编码已存在");
        }

        // 领域模型构建和验证
        Device device = domainService.createDevice(request);

        // 数据持久化
        Device savedDevice = deviceRepository.save(device);

        // 发布领域事件
        eventPublisher.publish(new DeviceCreatedEvent(savedDevice));

        log.info("设备创建成功: {}", savedDevice.getDeviceId());
        return DeviceMapper.toVO(savedDevice);
    }

    @Override
    public PageResult<DeviceVO> queryDevices(DeviceQueryRequest request) {
        // ✅ 正确：查询业务逻辑处理，使用仓储模式
        return deviceRepository.findWithPagination(request);
    }

    @Override
    public DeviceVO updateDevice(String deviceId, UpdateDeviceRequest request) {
        // ✅ 正确：更新业务逻辑处理，包含事件发布
        log.info("开始更新设备: {}", deviceId);

        // 聚合根验证
        Device device = deviceRepository.findById(deviceId)
            .orElseThrow(() -> new DeviceNotFoundException("设备不存在: " + deviceId));

        // 业务更新
        device.update(request);

        // 持久化
        Device updatedDevice = deviceRepository.save(device);

        // 发布更新事件
        eventPublisher.publish(new DeviceUpdatedEvent(updatedDevice));

        return DeviceMapper.toVO(updatedDevice);
    }
}
```

#### 步骤2: 领域服务实现
```java
// 领域服务 (ioe-dream-service/device-service)
@Component
public class DeviceDomainService {

    @Resource
    private DeviceValidationService validationService;

    @Resource
    private DeviceIdentifierGenerator idGenerator;

    public Device createDevice(CreateDeviceRequest request) {
        // 领域业务规则验证
        validationService.validateDeviceRequest(request);

        // 创建设备聚合根
        Device device = Device.builder()
            .deviceId(idGenerator.generate())
            .deviceCode(request.getDeviceCode())
            .deviceName(request.getDeviceName())
            .deviceType(DeviceType.fromCode(request.getDeviceType()))
            .status(DeviceStatus.INACTIVE)
            .createdAt(LocalDateTime.now())
            .build();

        // 业务规则应用
        device.applyBusinessRules();

        return device;
    }
}
```

#### 步骤3: 微服务实现层约束检查
```bash
# 检查微服务实现层架构合规性
echo "🔍 检查微服务实现层微服务架构合规性..."

# 1. 检查微服务事务管理（应该有明确的事务边界）
service_transaction=$(grep -r "@Transactional" --include="*Service*.java" . | wc -l)
service_methods=$(grep -r "public.*(" --include="*Service*.java" . | wc -l)
echo "微服务事务覆盖率: $service_transaction/$service_methods"

# 2. 检查微服务是否有事件发布（微服务应该有）
event_publisher_usage=$(grep -r "EventPublisher|publishEvent" --include="*Service*.java" . | wc -l)
echo "微服务事件发布使用数量: $event_publisher_usage"

# 3. 检查微服务是否依赖其他微服务（应该通过事件或API）
microservice_dependencies=$(grep -r -E "@Resource.*Service|@Autowired.*Service" --include="*Service*.java" . | wc -l)
if [ $microservice_dependencies -gt 5 ]; then
    echo "⚠️ 微服务间存在过多直接依赖，建议使用事件或API客户端"
fi

# 4. 检查仓储模式使用（微服务应该使用仓储）
repository_usage=$(grep -r "@Resource.*Repository" --include="*Service*.java" . | wc -l)
echo "仓储模式使用数量: $repository_usage"
```

### 4. 基础设施层技术支撑

#### 步骤1: 基础设施层标准实现
```java
// 基础设施层 (ioe-dream-infrastructure/config)
@Configuration
@EnableConfigurationProperties
public class MicroserviceInfrastructureConfig {

    // 1. 服务注册与发现配置
    @Bean
    public ServiceRegistryManager serviceRegistryManager() {
        return new NacosServiceRegistryManager();
    }

    // 2. 配置管理中心
    @Bean
    public ConfigManager configManager() {
        return new NacosConfigManager();
    }

    // 3. 分布式链路追踪
    @Bean
    public TracingManager tracingManager() {
        return new JaegerTracingManager();
    }

    // 4. 监控指标收集
    @Bean
    public MetricsCollector metricsCollector() {
        return new PrometheusMetricsCollector();
    }
}

// 服务注册管理器
@Component
public class ServiceRegistryManager {

    @Resource
    private NacosServiceRegistry serviceRegistry;

    @Resource
    private ApplicationInfoManager applicationInfoManager;

    public void registerService(MicroserviceInfo info) {
        // 微服务注册逻辑
        Instance instance = Instance.builder()
            .ip(info.getIp())
            .port(info.getPort())
            .serviceName(info.getServiceName())
            .metadata(info.getMetadata())
            .build();

        serviceRegistry.register(instance);
        log.info("微服务注册成功: {}", info.getServiceName());
    }

    public void deregisterService(String serviceName) {
        // 微服务注销逻辑
        serviceRegistry.deregister(serviceName);
        log.info("微服务注销成功: {}", serviceName);
    }
}
```

#### 步骤2: 事件驱动基础设施
```java
// 事件总线配置
@Configuration
public class EventInfrastructureConfig {

    // 1. 事件发布器
    @Bean
    public EventPublisher eventPublisher() {
        return new KafkaEventPublisher();
    }

    // 2. 事件处理器
    @Bean
    public EventProcessor eventProcessor() {
        return new ConcurrentEventProcessor();
    }

    // 3. 事件存储
    @Bean
    public EventStore eventStore() {
        return new RelationalEventStore();
    }
}

// 消息队列基础设施
@Component
public class MessageQueueManager {

    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void publishEvent(DomainEvent event) {
        // 发布领域事件
        String topic = event.getEventName();
        kafkaTemplate.send(topic, event);
        log.info("事件已发布: {} to topic: {}", event.getEventId(), topic);
    }

    @KafkaListener(topics = "device-events")
    public void handleDeviceEvent(DeviceEvent event) {
        // 处理设备相关事件
        eventProcessor.process(event);
    }
}
```

#### 步骤3: 基础设施层约束检查
```bash
# 检查基础设施层微服务架构合规性
echo "🔍 检查基础设施层微服务架构合规性..."

# 1. 检查服务发现配置（必须有）
service_discovery_config=$(find . -name "*.yml" -o -name "*.properties" | xargs grep -l "nacos\|eureka\|consul" | wc -l)
echo "服务发现配置文件数量: $service_discovery_config"

# 2. 检查配置中心使用（应该有）
config_center_usage=$(find . -name "*.yml" -o -name "*.properties" | xargs grep -l "spring.cloud.config\|nacos.config" | wc -l)
echo "配置中心使用数量: $config_center_usage"

# 3. 检查监控配置（应该有）
monitoring_config=$(find . -name "*.yml" -o -name "*.properties" | xargs grep -l "management.endpoints\|prometheus\|micrometer" | wc -l)
echo "监控配置文件数量: $monitoring_config"

# 4. 检查消息队列配置（微服务应该有）
message_queue_config=$(find . -name "*.yml" -o -name "*.properties" | xargs grep -l "spring.kafka\|spring.rabbitmq" | wc -l)
echo "消息队列配置数量: $message_queue_config"
```

### 5. 仓储模式数据访问

#### 步骤1: 仓储接口标准实现
```java
// 仓储接口 (ioe-dream-service/device-service/domain/repository)
@Repository
public interface DeviceRepository {

    /**
     * 保存设备聚合根
     * @param device 设备聚合根
     * @return 保存后的设备
     */
    Device save(Device device);

    /**
     * 根据ID查找设备
     * @param deviceId 设备ID
     * @return 设备Optional
     */
    Optional<Device> findById(String deviceId);

    /**
     * 根据编码查找设备
     * @param deviceCode 设备编码
     * @return 是否存在
     */
    boolean existsByCode(String deviceCode);

    /**
     * 分页查询设备
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<DeviceVO> findWithPagination(DeviceQueryRequest request);

    /**
     * 删除设备
     * @param deviceId 设备ID
     */
    void deleteById(String deviceId);
}
```

#### 步骤2: 仓储实现
```java
// 仓储实现 (ioe-dream-service/device-service/infrastructure/persistence)
@Repository
public class DeviceRepositoryImpl implements DeviceRepository {

    @Resource
    private DeviceMapper deviceMapper;  // MyBatis Mapper

    @Resource
    private DeviceMapperCustom deviceMapperCustom;  // 自定义SQL

    @Override
    public Device save(Device device) {
        // ✅ 正确：聚合根持久化，包含领域事件
        DeviceEntity entity = DeviceMapper.toEntity(device);

        if (entity.getDeviceId() == null) {
            deviceMapper.insert(entity);
        } else {
            deviceMapper.updateById(entity);
        }

        return DeviceMapper.toDomain(entity);
    }

    @Override
    public Optional<Device> findById(String deviceId) {
        DeviceEntity entity = deviceMapper.selectById(deviceId);
        return Optional.ofNullable(entity)
            .map(DeviceMapper::toDomain);
    }

    @Override
    public boolean existsByCode(String deviceCode) {
        LambdaQueryWrapper<DeviceEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeviceEntity::getDeviceCode, deviceCode);
        return deviceMapper.selectCount(wrapper) > 0;
    }

    @Override
    public PageResult<DeviceVO> findWithPagination(DeviceQueryRequest request) {
        // ✅ 正确：复杂查询逻辑在仓储层实现
        Page<DeviceEntity> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<DeviceEntity> wrapper = buildQueryWrapper(request);
        Page<DeviceEntity> result = deviceMapper.selectPage(page, wrapper);

        List<DeviceVO> voList = result.getRecords().stream()
            .map(DeviceMapper::toVO)
            .collect(Collectors.toList());

        return new PageResult<>(voList, result.getTotal());
    }

    @Override
    public void deleteById(String deviceId) {
        deviceMapper.deleteById(deviceId);
    }

    private LambdaQueryWrapper<DeviceEntity> buildQueryWrapper(DeviceQueryRequest request) {
        // 构建查询条件的复杂逻辑
        LambdaQueryWrapper<DeviceEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(request.getDeviceName()),
                    DeviceEntity::getDeviceName, request.getDeviceName())
               .eq(request.getDeviceType() != null,
                   DeviceEntity::getDeviceType, request.getDeviceType())
               .eq(request.getStatus() != null,
                   DeviceEntity::getStatus, request.getStatus())
               .orderByDesc(DeviceEntity::getCreateTime);
        return wrapper;
    }
}
```

#### 步骤3: 仓储模式约束检查
```bash
# 检查仓储模式微服务架构合规性
echo "🔍 检查仓储模式微服务架构合规性..."

# 1. 检查Repository接口定义（微服务应该有）
repository_interfaces=$(find . -name "*Repository.java" | wc -l)
echo "Repository接口数量: $repository_interfaces"

# 2. 检查是否有聚合根模型（DDD应该有）
aggregate_roots=$(find . -name "*.java" -exec grep -l "@Entity\|@Table.*Entity" {} \; | wc -l)
echo "聚合根实体数量: $aggregate_roots"

# 3. 检查领域事件定义（微服务应该有）
domain_events=$(find . -name "*Event.java" | wc -l)
echo "领域事件数量: $domain_events"

# 4. 检查仓储实现是否包含业务逻辑（可以有限制）
repository_business_logic=$(grep -r -E "if.*else.*{|for.*{" --include="*Repository*.java" . | wc -l)
echo "仓储层业务逻辑复杂度: $repository_business_logic"
if [ $repository_business_logic -gt 20 ]; then
    echo "⚠️ 仓储层业务逻辑过于复杂，建议移到领域服务"
fi
```

---

## ⚠️ 注意事项

### 微服务架构约束
- **严禁服务边界混乱**: API契约层不能包含业务逻辑，微服务层不能处理HTTP协议
- **严禁分布式事务**: 避免跨微服务的分布式事务，使用Saga模式和最终一致性
- **事件驱动**: 微服务间通过领域事件通信，避免直接RPC调用
- **数据隔离**: 每个微服务有独立的数据存储，避免共享数据库

### 服务边界设计
- **API契约**: 只负责接口暴露、参数验证、协议转换
- **微服务实现**: 处理业务逻辑、事务管理、数据持久化、事件发布
- **基础设施**: 提供服务发现、配置管理、监控告警等技术支撑
- **数据一致性**: 通过事件溯源和Saga模式保证最终一致性

### 分布式系统考虑
- **服务发现**: 使用Nacos或Eureka实现服务注册和发现
- **配置中心**: 统一管理微服务配置，支持动态更新
- **链路追踪**: 实现分布式链路追踪，便于问题定位
- **监控告警**: 完整的监控体系，包括业务指标和技术指标
- **容错设计**: 实现断路器、限流、降级等容错机制

---

## 📊 评估标准

### 操作时间
- **微服务架构设计**: 6小时内完成微服务架构和领域模型设计
- **API契约设计**: 4小时内完成OpenAPI规范和API版本设计
- **微服务实现**: 16小时内完成标准微服务代码实现
- **基础设施配置**: 4小时内完成服务发现、配置中心等基础设施
- **集成测试**: 8小时内完成微服务集成测试和部署验证

### 准确率要求
- **微服务架构**: 100%符合微服务设计原则和DDD领域驱动设计
- **API契约合规**: 100%遵循RESTful设计和OpenAPI规范
- **服务边界清晰**: 100%正确的服务边界，无职责混乱
- **事件驱动设计**: 100%正确的事件驱动架构，无跨服务直接调用

### 质量标准
- **分布式一致性**: 通过Saga模式保证最终一致性
- **高可用性**: 实现服务容错、限流、降级机制
- **可扩展性**: 支持水平扩展和独立部署
- **可观测性**: 完整的监控、链路追踪、日志聚合体系

---

## 🔗 相关技能

### 相关微服务技能
- **[微服务架构师](microservices-architect.md)**: 微服务架构设计和技术选型
- **[API设计专家](api-design-expert.md)**: RESTful API设计和OpenAPI规范
- **[领域建模专家](domain-modeling-expert.md)**: DDD领域驱动设计和聚合设计
- **[Spring Boot Jakarta守护专家](spring-boot-jakarta-guardian.md)**: Spring Boot 3.x技术规范

### 进阶路径
- **云原生架构师**: 负责云原生微服务架构设计和技术演进
- **分布式系统专家**: 深入分布式系统设计、一致性算法和容错机制
- **DevOps架构师**: 负责微服务CI/CD、容器化和云平台集成
- **团队技术负责人**: 带领微服务开发团队，把控架构方向和质量

### 参考资料
- **[微服务架构规范](../docs/repowiki/zh/content/开发规范体系/微服务架构规范.md)**: 完整的微服务架构标准
- **[技术架构v3.0](../docs/repowiki/zh/content/技术架构/技术架构.md)**: 三层微服务架构设计
- **[API设计规范](../docs/repowiki/zh/content/开发规范体系/API设计规范.md)**: API版本管理和RESTful设计
- **[Spring Cloud官方文档](https://spring.io/projects/spring-cloud)**: Spring Cloud微服务框架
- **[Nacos官方文档](https://nacos.io/)**: 服务发现和配置中心
- **[DDD领域驱动设计](https://github.com/ddd-crew/ddd-starter-modelling-process)**: 领域驱动设计实践

---

## 📋 检查清单

### 微服务架构设计检查
- [ ] 已明确微服务边界和领域划分
- [ ] 已设计API契约层和微服务实现层分离
- [ ] 已确定事件驱动架构和数据一致性策略
- [ ] 已规划服务发现、配置中心、监控体系
- [ ] 已考虑分布式系统容错和高可用设计

### API契约层实现检查
- [ ] API控制器只处理接口协议转换
- [ ] 使用微服务客户端而非直接Service注入
- [ ] 实现API版本管理策略(v0/v1/v2)
- [ ] 遵循RESTful设计和OpenAPI规范
- [ ] 无业务逻辑和跨服务直接调用

### 微服务实现层检查
- [ ] 微服务包含完整的业务逻辑和事务管理
- [ ] 实现仓储模式和领域驱动设计
- [ ] 使用事件驱动架构进行服务间通信
- [ ] 包含领域服务和聚合根设计
- [ ] 避免分布式事务，使用最终一致性

### 基础设施层检查
- [ ] 实现服务注册与发现机制
- [ ] 配置中心统一管理配置
- [ ] 建立监控、告警、链路追踪体系
- [ ] 实现消息队列和事件总线
- [ ] 支持容器化部署和CI/CD流水线

### 文档完整性检查
- [ ] 微服务架构设计文档完整
- [ ] API契约文档(OpenAPI)清晰
- [ ] 事件驱动架构文档明确
- [ ] 部署和运维文档齐全
- [ ] 分布式系统故障排查指南完善

---

**💡 核心理念**: 严格遵循微服务架构原则，通过API契约层、微服务实现层、基础设施层的分离，构建高内聚、低耦合的分布式系统，支撑企业级应用的持续演进和规模化发展。