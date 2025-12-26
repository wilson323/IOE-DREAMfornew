# 访客服务专家技能
## Visitor Service Specialist

**🎯 技能定位**: IOE-DREAM智慧园区访客管理业务专家，精通访客预约、审批流程、跨服务权限下发、访客体验优化等核心业务

**⚡ 技能等级**: ★★★★★★ (顶级专家)
**🎯 适用场景**: 访客服务开发、预约管理系统、审批流程建设、访客体验优化
**📊 技能覆盖**: 访客预约 | 审批流程 | 跨服务调用 | 权限下发 | 访客记录 | 访客体验 | 安全管控
**🔧 技术栈**: Spring Boot 3.5.8 + Camunda BPM + MyBatis-Plus + Redis + MinIO

---

## 📋 技能概述

### **核心专长**
- **访客预约系统**: 灵活的访客预约机制、时间冲突检测、资源优化分配
- **审批流程引擎**: 多级审批流程设计、动态审批规则、审批状态跟踪
- **跨服务权限下发**: 通过网关调用门禁服务，下发访客权限到门禁设备
- **权限时效管理**: 临时权限生成、自动过期回收、精细化访问控制
- **访客体验优化**: 无感通行、移动端支持、自助服务、智能推荐
- **安全管控**: 身份验证、黑名单管理、异常检测、风险预警
- **数据分析**: 访客流量分析、预约成功率、审批效率统计

### **解决能力**
- **访客服务架构**: 高可用、高性能的访客服务架构设计和实现
- **预约算法优化**: 智能预约算法，最大化资源利用率和访客满意度
- **跨服务调用优化**: 高效的服务间调用机制，确保权限下发可靠性
- **审批流程优化**: 自动化审批流程，减少人工干预和提高审批效率
- **安全风险控制**: 访客安全风险评估、实时监控、应急处理机制
- **数据统计分析**: 访客数据分析和洞察，支持管理决策

---

## 🎯 业务场景覆盖

### 👤 访客预约管理
```java
// 访客预约管理 (Spring Boot 3.5.8 + MyBatis-Plus)
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.transaction.Transactional;
import jakarta.servlet.http.HttpServletRequest;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

// Controller层 - REST接口
@RestController
@RequestMapping("/api/v1/visitor/appointment")
@Tag(name = "访客预约", description = "访客预约和管理服务")
public class VisitorAppointmentController {

    @Resource
    private VisitorAppointmentService visitorAppointmentService;

    /**
     * 创建访客预约
     */
    @PostMapping("/create")
    @RateLimiter(name = "appointment-create", fallbackMethod = "createAppointmentFallback")
    @ApiOperation(value = "创建预约", notes = "创建新的访客预约")
    public ResponseDTO<AppointmentResultDTO> createAppointment(
            @Valid @RequestBody AppointmentCreateRequestDTO request,
            HttpServletRequest httpRequest) {

        log.info("[访客预约] 创建预约, visitorName={}, visitDate={}",
                request.getVisitorName(), request.getVisitDate());

        validateRequestSource(httpRequest);

        AppointmentResultDTO result = visitorAppointmentService.createAppointment(request);

        log.info("[访客预约] 预约创建成功, appointmentId={}, status={}",
                result.getAppointmentId(), result.getStatus());

        return ResponseDTO.ok(result);
    }

    /**
     * 取消访客预约
     */
    @PostMapping("/cancel")
    @ApiOperation(value = "取消预约", notes = "取消访客预约")
    public ResponseDTO<Void> cancelAppointment(@Valid @RequestBody AppointmentCancelRequestDTO request) {
        log.info("[访客预约] 取消预约, appointmentId={}", request.getAppointmentId());

        visitorAppointmentService.cancelAppointment(request.getAppointmentId(), request.getCancelReason());

        return ResponseDTO.ok();
    }

    // 服务降级处理
    public ResponseDTO<AppointmentResultDTO> createAppointmentFallback(AppointmentCreateRequestDTO request, Exception ex) {
        log.error("[访客预约] 服务降级, visitorName={}", request.getVisitorName(), ex);
        return ResponseDTO.error("SERVICE_DEGRADED", "系统繁忙，请稍后重试");
    }
}

// Service层 - 业务逻辑实现
@Service
@Transactional(rollbackFor = Exception.class)
public class VisitorAppointmentServiceImpl implements VisitorAppointmentService {

    @Resource
    private VisitorAppointmentManager visitorAppointmentManager;

    @Override
    public AppointmentResultDTO createAppointment(AppointmentCreateRequestDTO request) {
        try {
            validateAppointmentRequest(request);

            AppointmentResult result = visitorAppointmentManager.createAppointment(request);

            return convertToDTO(result);
        } catch (BusinessException e) {
            log.warn("[访客预约] 业务异常, visitorName={}, error={}", request.getVisitorName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[访客预约] 系统异常, visitorName={}", request.getVisitorName(), e);
            throw new BusinessException("APPOINTMENT_CREATE_ERROR", "访客预约创建失败");
        }
    }

    @Override
    public void cancelAppointment(Long appointmentId, String cancelReason) {
        try {
            if (appointmentId == null) {
                throw new BusinessException("APPOINTMENT_ID_REQUIRED", "预约ID不能为空");
            }

            visitorAppointmentManager.cancelAppointment(appointmentId, cancelReason);
        } catch (Exception e) {
            log.error("[访客预约] 取消失败, appointmentId={}", appointmentId, e);
            throw new BusinessException("APPOINTMENT_CANCEL_ERROR", "访客预约取消失败");
        }
    }

    private void validateAppointmentRequest(AppointmentCreateRequestDTO request) {
        if (StringUtils.isEmpty(request.getVisitorName())) {
            throw new BusinessException("VISITOR_NAME_REQUIRED", "访客姓名不能为空");
        }
        if (StringUtils.isEmpty(request.getVisitorPhone())) {
            throw new BusinessException("VISITOR_PHONE_REQUIRED", "访客电话不能为空");
        }
        if (request.getVisitDate() == null) {
            throw new BusinessException("VISIT_DATE_REQUIRED", "访问日期不能为空");
        }
        if (request.getVisitDate().isBefore(LocalDate.now())) {
            throw new BusinessException("INVALID_VISIT_DATE", "访问日期不能早于今天");
        }
    }
}

// Manager层 - 复杂业务流程编排
public class VisitorAppointmentManagerImpl implements VisitorAppointmentManager {

    private final AppointmentScheduler appointmentScheduler;
    private final ConflictDetector conflictDetector;
    private final VisitorValidator visitorValidator;
    private final VisitorAppointmentDao visitorAppointmentDao;
    private final ApprovalEngine approvalEngine;
    private final GatewayServiceClient gatewayServiceClient;

    // 构造函数注入依赖
    public VisitorAppointmentManagerImpl(
            AppointmentScheduler appointmentScheduler,
            ConflictDetector conflictDetector,
            VisitorValidator visitorValidator,
            VisitorAppointmentDao visitorAppointmentDao,
            ApprovalEngine approvalEngine,
            GatewayServiceClient gatewayServiceClient) {
        this.appointmentScheduler = appointmentScheduler;
        this.conflictDetector = conflictDetector;
        this.visitorValidator = visitorValidator;
        this.visitorAppointmentDao = visitorAppointmentDao;
        this.approvalEngine = approvalEngine;
        this.gatewayServiceClient = gatewayServiceClient;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppointmentResult createAppointment(AppointmentCreateRequestDTO request) {
        // 1. 访客信息验证
        VisitorValidationResult validation = visitorValidator.validateVisitor(request);
        if (!validation.isValid()) {
            throw new BusinessException("VISITOR_VALIDATION_FAILED", validation.getErrorMessage());
        }

        // 2. 预约冲突检测
        ConflictDetectionResult conflictResult = conflictDetector.detectConflicts(request);
        if (conflictResult.hasConflicts()) {
            return AppointmentResult.builder()
                .status("CONFLICT_DETECTED")
                .conflicts(conflictResult.getConflicts())
                .suggestions(conflictResult.getSuggestions())
                .build();
        }

        // 3. 智能调度安排
        AppointmentSchedule schedule = appointmentScheduler.scheduleAppointment(request);

        // 4. 访客预约信息持久化
        VisitorAppointmentEntity appointment = saveAppointment(request, schedule);

        // 5. 启动审批流程（如需要）
        if (approvalRequired(request)) {
            startApprovalProcess(appointment);
        } else {
            // 自动审批通过，下发权限
            grantVisitorPermission(appointment);
        }

        // 6. 发送预约确认通知
        sendAppointmentNotification(appointment);

        return buildAppointmentResult(appointment, schedule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelAppointment(Long appointmentId, String cancelReason) {
        // 1. 获取预约信息
        VisitorAppointmentEntity appointment = getAppointmentById(appointmentId);
        if (appointment == null) {
            throw new BusinessException("APPOINTMENT_NOT_FOUND", "访客预约不存在");
        }

        // 2. 验证取消权限
        validateCancelPermission(appointment);

        // 3. 回收已下发的权限
        revokeVisitorPermission(appointment);

        // 4. 更新预约状态
        updateAppointmentStatus(appointmentId, "CANCELLED", cancelReason);

        // 5. 发送取消通知
        sendCancelNotification(appointment, cancelReason);
    }

    private VisitorValidationResult visitorValidator.validateVisitor(AppointmentCreateRequestDTO request) {
        return visitorValidator.validateBasicInfo(request)
                .andThen(() -> validateBlacklist(request))
                .andThen(() -> validateVisitHistory(request));
    }

    private ConflictDetectionResult conflictDetector.detectConflicts(AppointmentCreateRequestDTO request) {
        // 1. 时间冲突检测
        List<TimeConflict> timeConflicts = checkTimeConflicts(request);

        // 2. 资源冲突检测
        List<ResourceConflict> resourceConflicts = checkResourceConflicts(request);

        // 3. 访客容量冲突检测
        List<CapacityConflict> capacityConflicts = checkCapacityConflicts(request);

        return ConflictDetectionResult.builder()
                .timeConflicts(timeConflicts)
                .resourceConflicts(resourceConflicts)
                .capacityConflicts(capacityConflicts)
                .hasConflicts(!timeConflicts.isEmpty() || !resourceConflicts.isEmpty() || !capacityConflicts.isEmpty())
                .build();
    }

    private VisitorAppointmentEntity saveAppointment(AppointmentCreateRequestDTO request, AppointmentSchedule schedule) {
        VisitorAppointmentEntity appointment = VisitorAppointmentEntity.builder()
                .visitorName(request.getVisitorName())
                .visitorPhone(request.getVisitorPhone())
                .visitorEmail(request.getVisitorEmail())
                .visitorCompany(request.getVisitorCompany())
                .visitorIdCard(request.getVisitorIdCard())
                .visitDate(request.getVisitDate())
                .visitStartTime(schedule.getStartTime())
                .visitEndTime(schedule.getEndTime())
                .visitPurpose(request.getVisitPurpose())
                .visitDepartment(request.getVisitDepartment())
                .visitContactPerson(request.getVisitContactPerson())
                .appointmentStatus("PENDING")
                .build();

        visitorAppointmentDao.insert(appointment);

        return appointment;
    }

    private void grantVisitorPermission(VisitorAppointmentEntity appointment) {
        // 通过网关调用门禁服务，下发访客权限
        VisitorProvisionRequest provisionRequest = VisitorProvisionRequest.builder()
                .visitorId(appointment.getAppointmentId())
                .permissionId("VISITOR_" + appointment.getAppointmentId())
                .deviceIds(getDeviceIdsForDepartment(appointment.getVisitDepartment()))
                .visitorInfo(VisitorInfo.builder()
                        .name(appointment.getVisitorName())
                        .phone(appointment.getVisitorPhone())
                        .idCard(appointment.getVisitorIdCard())
                        .photo(appointment.getVisitorPhoto())
                        .build())
                .accessTimeWindow(AccessTimeWindow.builder()
                        .startTime(appointment.getVisitStartTime())
                        .endTime(appointment.getVisitEndTime())
                        .build())
                .build();

        ResponseDTO<Void> result = gatewayServiceClient.callAccessService(
                "/api/v1/access/device/visitor/provision",
                HttpMethod.POST,
                provisionRequest,
                Void.class
        );

        if (result.getCode() != 200) {
            throw new BusinessException("PERMISSION_PROVISION_FAILED", "访客权限下发失败");
        }

        // 更新预约状态为已授权
        updateAppointmentStatus(appointment.getAppointmentId(), "AUTHORIZED", "自动审批通过");
    }

    private void revokeVisitorPermission(VisitorAppointmentEntity appointment) {
        VisitorRevokeRequest revokeRequest = VisitorRevokeRequest.builder()
                .visitorId(appointment.getAppointmentId())
                .deviceIds(getDeviceIdsForDepartment(appointment.getVisitDepartment()))
                .build();

        ResponseDTO<Void> result = gatewayServiceClient.callAccessService(
                "/api/v1/access/device/visitor/revoke",
                HttpMethod.DELETE,
                revokeRequest,
                Void.class
        );

        if (result.getCode() != 200) {
            log.warn("[访客权限] 权限回收失败, appointmentId={}", appointment.getAppointmentId());
        }
    }
}

// DAO层 - 数据访问
@Mapper
public interface VisitorAppointmentDao extends BaseMapper<VisitorAppointmentEntity> {

    @Transactional(readOnly = true)
    VisitorAppointmentEntity selectByAppointmentId(@Param("appointmentId") Long appointmentId);

    @Transactional(readOnly = true)
    List<VisitorAppointmentEntity> selectByVisitorPhone(@Param("visitorPhone") String visitorPhone);

    @Transactional(readOnly = true)
    List<VisitorAppointmentEntity> selectByVisitDateRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Transactional(readOnly = true)
    List<VisitorAppointmentEntity> selectByDepartmentAndDate(
        @Param("department") String department,
        @Param("visitDate") LocalDate visitDate
    );

    @Transactional(rollbackFor = Exception.class)
    int updateAppointmentStatus(
        @Param("appointmentId") Long appointmentId,
        @Param("status") String status,
        @Param("reason") String reason
    );
}

// 实体类 - 访客预约
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_visitor_appointment")
public class VisitorAppointmentEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long appointmentId;

    @TableField("visitor_name")
    private String visitorName;

    @TableField("visitor_phone")
    private String visitorPhone;

    @TableField("visitor_email")
    private String visitorEmail;

    @TableField("visitor_company")
    private String visitorCompany;

    @TableField("visitor_id_card")
    @Convert(converter = EncryptedStringConverter.class)
    private String visitorIdCard;  // 身份证号加密存储

    @TableField("visitor_photo")
    private String visitorPhoto;  // 照片URL

    @TableField("visit_date")
    private LocalDate visitDate;

    @TableField("visit_start_time")
    private LocalDateTime visitStartTime;

    @TableField("visit_end_time")
    private LocalDateTime visitEndTime;

    @TableField("visit_purpose")
    private String visitPurpose;

    @TableField("visit_department")
    private String visitDepartment;

    @TableField("visit_contact_person")
    private String visitContactPerson;

    @TableField("appointment_status")
    private String appointmentStatus;  // PENDING, APPROVED, REJECTED, CANCELLED, COMPLETED

    @TableField("approval_required")
    private Boolean approvalRequired;

    @TableField("approval_process_id")
    private String approvalProcessId;  // Camunda流程实例ID

    @TableField("access_granted")
    private Boolean accessGranted;  // 是否已下发门禁权限

    @TableField("access_start_time")
    private LocalDateTime accessStartTime;

    @TableField("access_end_time")
    private LocalDateTime accessEndTime;

    @TableField("check_in_time")
    private LocalDateTime checkInTime;  // 实际签到时间

    @TableField("check_out_time")
    private LocalDateTime checkOutTime;  // 实际离开时间

    @TableField("cancel_reason")
    private String cancelReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted_flag")
    private Integer deletedFlag;

    @Version
    private Integer version;
}
```
        ValidationErrors errors = visitorValidator.validate(request);
        if (!errors.isEmpty()) {
            return AppointmentResult.failed(errors);
        }

        // 2. 时间冲突检测
        ConflictResult conflict = conflictDetector.checkConflict(request);
        if (conflict.hasConflict()) {
            return AppointmentResult.conflicted(conflict.getAlternatives());
        }

        // 3. 智能调度算法
        AppointmentSlot optimalSlot = scheduler.findOptimalSlot(request);

        // 4. 创建预约记录
        return buildAppointment(optimalSlot, request);
    }
}
```

### 📋 审批流程管理
```java
@Service
public class ApprovalProcessService {

    @Resource
    private WorkflowEngine workflowEngine;

    @Resource
    private NotificationService notificationService;

    public ApprovalResult startApprovalProcess(Appointment appointment) {
        // 1. 确定审批流程
        ApprovalWorkflow workflow = workflowEngine.getWorkflow(appointment);

        // 2. 创建审批实例
        ApprovalInstance instance = workflowEngine.createInstance(workflow, appointment);

        // 3. 启动第一个审批节点
        ApprovalTask firstTask = workflowEngine.startFirstTask(instance);

        // 4. 发送审批通知
        notificationService.sendApprovalNotification(firstTask);

        return ApprovalResult.started(instance, firstTask);
    }

    public ApprovalResult processApproval(ApprovalAction action) {
        // 1. 验证审批权限
        validateApprovalPermission(action);

        // 2. 处理审批决策
        ApprovalDecision decision = workflowEngine.processDecision(action);

        // 3. 确定下一步操作
        if (decision.isComplete()) {
            // 审批完成，执行后续操作
            executePostApprovalActions(decision);
        } else {
            // 进入下一个审批节点
            moveToNextApprovalNode(decision);
        }

        return decision;
    }
}
```

### 🔐 访客权限管理与跨服务调用
```java
@Service
public class VisitorPermissionService {

    @Resource
    private TemporaryAccessGenerator accessGenerator;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    public VisitorPermission grantTemporaryPermission(Visitor visitor, Appointment appointment) {
        // 1. 生成临时权限凭证
        TemporaryAccessCredential credential = accessGenerator.generateCredential(visitor, appointment);

        // 2. 配置访问权限范围
        AccessPolicy policy = buildAccessPolicy(appointment);

        // 3. 跨服务调用门禁服务，下发权限到设备
        List<DeviceProvisioningResult> provisioningResults = provisionVisitorToAccessDevices(
            visitor, appointment, credential, policy);

        // 4. 验证所有设备下发成功
        validateProvisioningResults(provisioningResults);

        // 5. 设置权限过期时间（自动回收）
        schedulePermissionExpiration(visitor, appointment.getEndTime());

        return VisitorPermission.builder()
                .credential(credential)
                .policy(policy)
                .provisioningResults(provisioningResults)
                .validFrom(appointment.getStartTime())
                .validUntil(appointment.getEndTime())
                .build();
    }

    /**
     * 跨服务调用门禁服务，下发访客权限到门禁设备
     */
    private List<DeviceProvisioningResult> provisionVisitorToAccessDevices(
            Visitor visitor, Appointment appointment,
            TemporaryAccessCredential credential, AccessPolicy policy) {

        List<DeviceProvisioningResult> results = new ArrayList<>();

        // 获取访客需要访问的所有门禁设备
        List<AccessDevice> accessDevices = getAccessDevicesForAppointment(appointment);

        for (AccessDevice device : accessDevices) {
            try {
                // 构建权限下发请求
                VisitorProvisioningRequest request = VisitorProvisioningRequest.builder()
                        .visitorId(visitor.getVisitorId())
                        .deviceId(device.getDeviceId())
                        .deviceType(device.getDeviceType())
                        .visitorInfo(buildVisitorInfo(visitor))
                        .accessTimeWindow(buildAccessTimeWindow(appointment))
                        .permissionId(credential.getPermissionId())
                        .build();

                // 跨服务调用门禁服务
                ResponseDTO<Void> response = gatewayServiceClient.callAccessService(
                    "/api/v1/access/device/provision-visitor",
                    HttpMethod.POST,
                    request,
                    Void.class
                );

                DeviceProvisioningResult result = new DeviceProvisioningResult();
                result.setDeviceId(device.getDeviceId());
                result.setSuccess(response.isSuccess());
                result.setMessage(response.getMessage());

                results.add(result);

                log.info("访客权限下发到门禁设备: 访客ID={}, 设备ID={}, 结果={}",
                    visitor.getVisitorId(), device.getDeviceId(), response.isSuccess());

            } catch (Exception e) {
                log.error("访客权限下发失败: 访客ID={}, 设备ID={}",
                    visitor.getVisitorId(), device.getDeviceId(), e);

                DeviceProvisioningResult result = new DeviceProvisioningResult();
                result.setDeviceId(device.getDeviceId());
                result.setSuccess(false);
                result.setMessage(e.getMessage());
                results.add(result);
            }
        }

        return results;
    }

    /**
     * 回收访客权限（跨服务调用）
     */
    @Scheduled(fixedDelay = 60000) // 每分钟检查一次过期权限
    public void revokeExpiredVisitorPermissions() {
        List<VisitorPermission> expiredPermissions = getExpiredPermissions();

        for (VisitorPermission permission : expiredPermissions) {
            try {
                // 获取该权限关联的所有门禁设备
                List<AccessDevice> devices = getDevicesForPermission(permission);

                for (AccessDevice device : devices) {
                    // 构建权限回收请求
                    VisitorRevocationRequest request = VisitorRevocationRequest.builder()
                            .visitorId(permission.getVisitorId())
                            .deviceId(device.getDeviceId())
                            .deviceType(device.getDeviceType())
                            .permissionId(permission.getPermissionId())
                            .build();

                    // 跨服务调用门禁服务回收权限
                    ResponseDTO<Void> response = gatewayServiceClient.callAccessService(
                        "/api/v1/access/device/revoke-visitor",
                        HttpMethod.POST,
                        request,
                        Void.class
                    );

                    if (response.isSuccess()) {
                        log.info("访客权限回收成功: 访客ID={}, 设备ID={}",
                            permission.getVisitorId(), device.getDeviceId());
                    } else {
                        log.error("访客权限回收失败: 访客ID={}, 设备ID={}, 错误={}",
                            permission.getVisitorId(), device.getDeviceId(), response.getMessage());
                    }
                }

                // 标记权限为已回收
                markPermissionAsRevoked(permission);

            } catch (Exception e) {
                log.error("回收访客权限异常: 访客ID={}", permission.getVisitorId(), e);
            }
        }
    }
}
```

### 📊 访客数据分析
```java
@Service
public class VisitorAnalyticsService {

    public VisitorAnalytics generateAnalytics(AnalyticsRequest request) {
        // 1. 访客流量分析
        VisitorFlowAnalysis flowAnalysis = analyzeVisitorFlow(request.getTimeRange());

        // 2. 预约成功率分析
        AppointmentSuccessAnalysis successAnalysis = analyzeAppointmentSuccess(request);

        // 3. 审批效率分析
        ApprovalEfficiencyAnalysis approvalAnalysis = analyzeApprovalEfficiency(request);

        // 4. 访客满意度分析
        SatisfactionAnalysis satisfactionAnalysis = analyzeSatisfaction(request);

        return VisitorAnalytics.builder()
                .flowAnalysis(flowAnalysis)
                .successAnalysis(successAnalysis)
                .approvalAnalysis(approvalAnalysis)
                .satisfactionAnalysis(satisfactionAnalysis)
                .build();
    }
}
```

---

## 🔧 技术栈和工具

### 核心技术
- **Spring Boot 3.x**: 微服务框架
- **Camunda**: 工作流引擎（审批流程）
- **Spring Security**: 安全框架和认证授权
- **Redis**: 缓存和会话管理
- **MySQL**: 关系型数据库

### 工作流技术
- **BPMN 2.0**: 业务流程建模标准
- **工作流引擎**: Camunda、Flowable
- **状态机**: Spring State Machine
- **任务调度**: Quartz

### 通知技术
- **短信通知**: 阿里云短信、腾讯云短信
- **邮件通知**: Spring Mail、JavaMail
- **移动推送**: 极光推送、华为推送
- **微信通知**: 企业微信、服务号

---

## 📊 性能指标

### 响应时间要求
- **访客预约创建**: ≤ 2s (95%分位)
- **审批流程启动**: ≤ 1s (95%分位)
- **跨服务权限下发**: ≤ 3s (95%分位)
- **权限验证**: ≤ 500ms (95%分位)
- **访客签到**: ≤ 3s (95%分位)

### 并发处理能力
- **并发预约数**: ≥ 10,000/分钟
- **同时在线访客**: ≥ 5,000
- **跨服务调用QPS**: ≥ 500
- **权限下发吞吐**: ≥ 200/分钟
- **审批处理吞吐**: ≥ 1,000个/分钟
- **权限验证QPS**: ≥ 50,000

### 数据处理能力
- **访客记录存储**: 支持至少3年数据
- **查询响应时间**: ≤ 1s (复杂查询)
- **报表生成时间**: ≤ 30s
- **数据同步延迟**: ≤ 5s

---

## 📋 核心算法

### 智能调度算法
```java
@Component
public class IntelligentScheduler {

    public AppointmentSlot findOptimalSlot(AppointmentRequest request) {
        // 1. 获取可用时间段
        List<TimeSlot> availableSlots = getAvailableTimeSlots(request);

        // 2. 多目标优化（时间、资源、体验）
        OptimizationProblem problem = buildOptimizationProblem(availableSlots, request);

        // 3. 使用遗传算法求解最优解
        GeneticAlgorithm ga = new GeneticAlgorithm(problem);
        Individual bestSolution = ga.evolve();

        return convertToAppointmentSlot(bestSolution);
    }

    private OptimizationProblem buildOptimizationProblem(List<TimeSlot> slots, AppointmentRequest request) {
        return OptimizationProblem.builder()
                .objective1("time_preference")  // 时间偏好
                .objective2("resource_utilization")  // 资源利用率
                .objective3("visitor_experience")  // 访客体验
                .constraints(buildConstraints(slots, request))
                .build();
    }
}
```

### 审批决策优化
```java
@Component
public class ApprovalDecisionOptimizer {

    public ApprovalDecision optimizeDecision(ApprovalContext context) {
        // 1. 风险评估
        RiskAssessment risk = assessRisk(context);

        // 2. 决策树分析
        DecisionTree decisionTree = buildDecisionTree(risk);

        // 3. 机器学习预测
        MLDecisionPrediction prediction = mlModel.predict(context);

        // 4. 综合决策
        return combineDecisions(decisionTree, prediction, context);
    }

    private RiskAssessment assessRisk(ApprovalContext context) {
        return RiskAssessment.builder()
                .securityRisk(calculateSecurityRisk(context))
                .complianceRisk(calculateComplianceRisk(context))
                .operationalRisk(calculateOperationalRisk(context))
                .reputationRisk(calculateReputationRisk(context))
                .build();
    }
}
```

---

## 🛡️ 安全和隐私

### 访客隐私保护
```java
@Entity
public class Visitor {

    @Convert(converter = EncryptedStringConverter.class)
    private String idCardNumber;     // 身份证号加密

    @Convert(converter = EncryptedStringConverter.class)
    private String phoneNumber;       // 手机号加密

    @Convert(converter = EncryptedStringConverter.class)
    private String email;             // 邮箱加密

    @Column(columnDefinition = "POINT")
    private Point homeAddress;         // 家庭住址脱敏存储
}

// 访客数据脱敏
@RestController
public class VisitorController {

    @GetMapping("/visitors")
    public ResponseDTO<List<VisitorDTO>> getVisitors(VisitorQueryRequest request) {
        List<Visitor> visitors = visitorService.queryVisitors(request);

        // 敏感信息脱敏
        List<VisitorDTO> visitorDTOs = visitors.stream()
                .map(this::maskSensitiveData)
                .collect(Collectors.toList());

        return ResponseDTO.ok(visitorDTOs);
    }

    private VisitorDTO maskSensitiveData(Visitor visitor) {
        VisitorDTO dto = new VisitorDTO(visitor);
        dto.setIdCardNumber(maskIdCard(visitor.getIdCardNumber()));
        dto.setPhoneNumber(maskPhone(visitor.getPhoneNumber()));
        return dto;
    }
}
```

### 访客安全管控
```java
@Service
public class VisitorSecurityService {

    @Resource
    private BlacklistService blacklistService;

    @Resource
    private RiskEvaluationService riskService;

    public SecurityCheckResult checkVisitorSecurity(Visitor visitor) {
        // 1. 黑名单检查
        BlacklistCheck blacklistCheck = blacklistService.check(visitor);
        if (blacklistCheck.isBlacklisted()) {
            return SecurityCheckResult.blocked("访客在黑名单中");
        }

        // 2. 风险评估
        RiskEvaluation riskEvaluation = riskService.evaluate(visitor);
        if (riskEvaluation.getRiskScore() > RISK_THRESHOLD) {
            return SecurityCheckResult.restricted("访客风险评分过高");
        }

        // 3. 身份验证
        IdentityVerificationResult identityResult = verifyIdentity(visitor);
        if (!identityResult.isVerified()) {
            return SecurityCheckResult.failed("身份验证失败");
        }

        return SecurityCheckResult.passed();
    }
}
```

---

## 📱 移动端支持

### 访客移动应用
```java
@RestController
@RequestMapping("/api/v1/visitor/mobile")
public class VisitorMobileController {

    @PostMapping("/appointment")
    public ResponseDTO<AppointmentResult> createAppointmentByMobile(@Valid @RequestBody MobileAppointmentRequest request) {
        // 移动端预约逻辑
        request.setSource("MOBILE_APP");
        AppointmentResult result = visitorService.createAppointment(request);

        // 发送移动推送通知
        mobileNotificationService.sendAppointmentConfirmation(result);

        return ResponseDTO.ok(result);
    }

    @PostMapping("/checkin")
    public ResponseDTO<CheckinResult> checkinByMobile(@Valid @RequestBody MobileCheckinRequest request) {
        // 移动端签到逻辑
        CheckinResult result = visitorService.checkin(request);

        // 生成二维码
        String qrCode = qrCodeService.generateCheckinQrCode(result);
        result.setQrCode(qrCode);

        return ResponseDTO.ok(result);
    }
}
```

---

## 📋 开发检查清单

### 功能开发检查
- [ ] 访客预约系统实现
- [ ] 审批流程引擎集成
- [ ] 跨服务权限下发实现
- [ ] 权限时效管理系统开发
- [ ] 移动端应用支持
- [ ] 访客数据分析

### 性能检查
- [ ] 高并发预约处理
- [ ] 跨服务调用性能优化
- [ ] 工作流引擎性能
- [ ] 数据库查询优化
- [ ] 缓存策略实现
- [ ] 移动端响应优化

### 安全检查
- [ ] 访客数据加密
- [ ] 黑名单管理
- [ ] 风险评估算法
- [ ] 审计日志记录
- [ ] 隐私保护措施

---

## 🔗 相关技能文档

- **access-service-specialist**: 门禁访问控制专家（被调用方）
- **workflow-engine-specialist**: 工作流引擎专家
- **gateway-service-specialist**: 网关服务专家（服务间调用）
- **mobile-app-development-specialist**: 移动应用开发专家
- **security-protection-specialist**: 安全防护专家
- **data-analytics-specialist**: 数据分析专家
- **user-experience-specialist**: 用户体验专家

---

## 📞 联系和支持

**技能负责人**: 访客服务开发团队
**技术支持**: 架构师团队 + 安全团队
**问题反馈**: 通过项目管理系统提交

**版本信息**:
- **创建时间**: 2025-12-02
- **最后更新**: 2025-12-02
- **版本**: v1.0.0

---

**💡 重要提醒**: 本技能专注于访客管理的核心业务，特别是通过跨服务调用将访客权限下发到门禁设备。需要结合门禁服务（被调用方）、工作流引擎、网关服务、移动开发、安全防护等相关技能一起使用，确保系统的安全性和用户体验。注意：访客服务是独立模块，通过API网关调用门禁服务进行设备权限管理。