# Service层更新指南 - SelfServiceRegistrationEntity拆分

**更新日期**: 2025-12-26
**适用模块**: ioedream-visitor-service
**Entity拆分**: SelfServiceRegistrationEntity (451行) → 6个Entity

---

## 📋 更新概览

### Entity拆分影响

**原Entity** (已拆分):
- `SelfServiceRegistrationEntity` - 451行，36字段

**新Entity结构** (6个Entity):
1. `SelfServiceRegistrationEntity` - 核心登记信息（20字段）
2. `VisitorBiometricEntity` - 生物识别信息（4字段）
3. `VisitorApprovalEntity` - 审批流程信息（5字段）
4. `VisitRecordEntity` - 访问记录信息（5字段）
5. `TerminalInfoEntity` - 终端信息（5字段）
6. `VisitorAdditionalInfoEntity` - 附加信息（3字段）

---

## 🔄 Service层更新策略

### 策略A: 保持现有Service接口不变（推荐）

**优势**:
- ✅ Service接口保持向后兼容
- ✅ Controller层无需修改
- ✅ 复杂的数据组装逻辑封装在Manager层

**实现方式**:
```java
// Service接口保持不变
public interface SelfServiceRegistrationService {
    SelfServiceRegistrationEntity createRegistration(SelfServiceRegistrationEntity registration);
    SelfServiceRegistrationEntity getRegistrationByVisitorCode(String visitorCode);
    // ... 其他方法
}
```

**Service实现修改**:
```java
@Service
public class SelfServiceRegistrationServiceImpl implements SelfServiceRegistrationService {

    @Resource
    private SelfServiceRegistrationManager selfServiceRegistrationManager;

    @Override
    public SelfServiceRegistrationEntity createRegistration(SelfServiceRegistrationEntity registration) {
        // Manager层负责数据组装和事务管理
        return selfServiceRegistrationManager.createRegistration(registration);
    }

    @Override
    public SelfServiceRegistrationEntity getRegistrationByVisitorCode(String visitorCode) {
        // Manager层负责JOIN查询和数据组装
        return selfServiceRegistrationManager.getRegistrationByVisitorCode(visitorCode);
    }
}
```

### 策略B: 创建新的VO对象（可选）

**优势**:
- ✅ 更清晰的API响应结构
- ✅ 前后端职责分离

**实现方式**:
```java
// 创建完整的登记信息VO
@Data
@Schema(description = "访客登记完整信息")
public class VisitorRegistrationFullVO {

    // 核心登记信息
    private Long registrationId;
    private String registrationCode;
    private String visitorName;
    // ... 其他核心字段

    // 生物识别信息
    private String facePhotoUrl;
    private String faceFeature;

    // 审批信息
    private String approverName;
    private LocalDateTime approvalTime;

    // 访问记录信息
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    // 终端信息
    private String terminalId;
    private String terminalLocation;
    private String visitorCard;

    // 附加信息
    private String belongings;
    private String licensePlate;
}
```

---

## 🔧 Manager层更新重点

Manager层需要处理数据组装和事务管理：

### 1. 创建登记（涉及6个表）

```java
@Slf4j
public class SelfServiceRegistrationManager {

    private final SelfServiceRegistrationDao registrationDao;
    private final VisitorBiometricDao biometricDao;
    private final VisitorApprovalDao approvalDao;
    private final VisitRecordDao recordDao;
    private final TerminalInfoDao terminalInfoDao;
    private final VisitorAdditionalInfoDao additionalInfoDao;

    /**
     * 创建自助登记（事务处理）
     */
    @Transactional(rollbackFor = Exception.class)
    public SelfServiceRegistrationEntity createRegistration(SelfServiceRegistrationEntity registration) {
        log.info("[登记管理] 创建自助登记: visitorName={}", registration.getVisitorName());

        // 1. 保存核心登记信息
        registrationDao.insert(registration);

        // 2. 保存生物识别信息（如果有）
        if (registration.getFacePhotoUrl() != null) {
            VisitorBiometricEntity biometric = VisitorBiometricEntity.builder()
                    .registrationId(registration.getRegistrationId())
                    .facePhotoUrl(registration.getFacePhotoUrl())
                    .faceFeature(registration.getFaceFeature())
                    .build();
            biometricDao.insert(biometric);
        }

        // 3. 保存终端信息（如果有）
        if (registration.getTerminalId() != null) {
            TerminalInfoEntity terminalInfo = TerminalInfoEntity.builder()
                    .registrationId(registration.getRegistrationId())
                    .terminalId(registration.getTerminalId())
                    .terminalLocation(registration.getTerminalLocation())
                    .build();
            terminalInfoDao.insert(terminalInfo);
        }

        // 4. 保存附加信息（如果有）
        if (registration.getBelongings() != null || registration.getLicensePlate() != null) {
            VisitorAdditionalInfoEntity additionalInfo = VisitorAdditionalInfoEntity.builder()
                    .registrationId(registration.getRegistrationId())
                    .belongings(registration.getBelongings())
                    .licensePlate(registration.getLicensePlate())
                    .build();
            additionalInfoDao.insert(additionalInfo);
        }

        log.info("[登记管理] 自助登记创建成功: registrationId={}", registration.getRegistrationId());
        return registration;
    }
}
```

### 2. 查询登记（JOIN 6个表）

```java
/**
 * 根据访客码查询登记信息（JOIN查询）
 */
public SelfServiceRegistrationEntity getRegistrationByVisitorCode(String visitorCode) {
    log.info("[登记管理] 查询登记记录: visitorCode={}", visitorCode);

    // 1. 查询核心登记信息
    SelfServiceRegistrationEntity registration = registrationDao.selectByVisitorCode(visitorCode);
    if (registration == null) {
        log.warn("[登记管理] 登记记录不存在: visitorCode={}", visitorCode);
        return null;
    }

    // 2. 查询并组装生物识别信息
    VisitorBiometricEntity biometric = biometricDao.selectByRegistrationId(registration.getRegistrationId());
    if (biometric != null) {
        registration.setFacePhotoUrl(biometric.getFacePhotoUrl());
        registration.setFaceFeature(biometric.getFaceFeature());
        registration.setIdCardPhotoUrl(biometric.getIdCardPhotoUrl());
    }

    // 3. 查询并组装审批信息
    VisitorApprovalEntity approval = approvalDao.selectByRegistrationId(registration.getRegistrationId());
    if (approval != null) {
        registration.setApproverId(approval.getApproverId());
        registration.setApproverName(approval.getApproverName());
        registration.setApprovalTime(approval.getApprovalTime());
        registration.setApprovalComment(approval.getApprovalComment());
    }

    // 4. 查询并组装访问记录信息
    VisitRecordEntity record = recordDao.selectByRegistrationId(registration.getRegistrationId());
    if (record != null) {
        registration.setCheckInTime(record.getCheckInTime());
        registration.setCheckOutTime(record.getCheckOutTime());
        registration.setEscortRequired(record.getEscortRequired());
        registration.setEscortUser(record.getEscortUser());
    }

    // 5. 查询并组装终端信息
    TerminalInfoEntity terminalInfo = terminalInfoDao.selectByRegistrationId(registration.getRegistrationId());
    if (terminalInfo != null) {
        registration.setTerminalId(terminalInfo.getTerminalId());
        registration.setTerminalLocation(terminalInfo.getTerminalLocation());
        registration.setVisitorCard(terminalInfo.getVisitorCard());
        registration.setCardPrintStatus(terminalInfo.getCardPrintStatus());
    }

    // 6. 查询并组装附加信息
    VisitorAdditionalInfoEntity additionalInfo = additionalInfoDao.selectByRegistrationId(registration.getRegistrationId());
    if (additionalInfo != null) {
        registration.setBelongings(additionalInfo.getBelongings());
        registration.setLicensePlate(additionalInfo.getLicensePlate());
    }

    log.info("[登记管理] 登记记录查询成功: registrationId={}", registration.getRegistrationId());
    return registration;
}
```

### 3. 审批登记（更新2个表）

```java
/**
 * 审批登记申请
 */
@Transactional(rollbackFor = Exception.class)
public SelfServiceRegistrationEntity approveRegistration(Long registrationId,
                                                      Long approverId,
                                                      String approverName,
                                                      Boolean approved,
                                                      String approvalComment) {
    log.info("[登记管理] 审批登记申请: registrationId={}, approver={}, approved={}",
            registrationId, approverName, approved);

    // 1. 更新核心登记状态
    SelfServiceRegistrationEntity registration = registrationDao.selectById(registrationId);
    if (registration == null) {
        throw new RuntimeException("登记记录不存在");
    }

    registration.setRegistrationStatus(approved ? 1 : 2); // 1-审批通过 2-审批拒绝
    registrationDao.updateById(registration);

    // 2. 插入或更新审批信息
    VisitorApprovalEntity approval = approvalDao.selectByRegistrationId(registrationId);
    if (approval == null) {
        // 新增审批记录
        approval = VisitorApprovalEntity.builder()
                .registrationId(registrationId)
                .approverId(approverId)
                .approverName(approverName)
                .approvalTime(LocalDateTime.now())
                .approvalComment(approvalComment)
                .build();
        approvalDao.insert(approval);
    } else {
        // 更新已有审批记录
        approval.setApproverId(approverId);
        approval.setApproverName(approverName);
        approval.setApprovalTime(LocalDateTime.now());
        approval.setApprovalComment(approvalComment);
        approvalDao.updateById(approval);
    }

    log.info("[登记管理] 登记审批成功: registrationId={}, status={}", registrationId, registration.getRegistrationStatus());
    return getRegistrationByVisitorCode(registration.getVisitorCode());
}
```

### 4. 签到签离（更新1个表）

```java
/**
 * 访客签到
 */
@Transactional(rollbackFor = Exception.class)
public SelfServiceRegistrationEntity checkIn(String visitorCode) {
    log.info("[登记管理] 访客签到: visitorCode={}", visitorCode);

    // 1. 查询登记信息
    SelfServiceRegistrationEntity registration = registrationDao.selectByVisitorCode(visitorCode);
    if (registration == null) {
        throw new RuntimeException("访客码不存在");
    }

    // 2. 更新登记状态为"已签到"
    registration.setRegistrationStatus(3); // 3-已签到
    registrationDao.updateById(registration);

    // 3. 创建访问记录
    VisitRecordEntity record = recordDao.selectByRegistrationId(registration.getRegistrationId());
    if (record == null) {
        record = VisitRecordEntity.builder()
                .registrationId(registration.getRegistrationId())
                .checkInTime(LocalDateTime.now())
                .build();
        recordDao.insert(record);
    } else {
        record.setCheckInTime(LocalDateTime.now());
        recordDao.updateById(record);
    }

    log.info("[登记管理] 访客签到成功: registrationId={}", registration.getRegistrationId());
    return getRegistrationByVisitorCode(visitorCode);
}
```

---

## 📝 DAO层更新清单

### 需要添加的查询方法

每个DAO需要添加以下方法：

```java
// VisitorBiometricDao
public interface VisitorBiometricDao extends BaseMapper<VisitorBiometricEntity> {
    /**
     * 根据登记ID查询生物识别信息
     */
    @Select("SELECT * FROM t_visitor_biometric WHERE registration_id = #{registrationId} AND deleted_flag = 0")
    VisitorBiometricEntity selectByRegistrationId(Long registrationId);
}

// VisitorApprovalDao
public interface VisitorApprovalDao extends BaseMapper<VisitorApprovalEntity> {
    /**
     * 根据登记ID查询审批信息
     */
    @Select("SELECT * FROM t_visitor_approval WHERE registration_id = #{registrationId} AND deleted_flag = 0")
    VisitorApprovalEntity selectByRegistrationId(Long registrationId);
}

// VisitRecordDao
public interface VisitRecordDao extends BaseMapper<VisitRecordEntity> {
    /**
     * 根据登记ID查询访问记录
     */
    @Select("SELECT * FROM t_visitor_visit_record WHERE registration_id = #{registrationId} AND deleted_flag = 0")
    VisitRecordEntity selectByRegistrationId(Long registrationId);
}

// TerminalInfoDao
public interface TerminalInfoDao extends BaseMapper<TerminalInfoEntity> {
    /**
     * 根据登记ID查询终端信息
     */
    @Select("SELECT * FROM t_visitor_terminal_info WHERE registration_id = #{registrationId} AND deleted_flag = 0")
    TerminalInfoEntity selectByRegistrationId(Long registrationId);
}

// VisitorAdditionalInfoDao
public interface VisitorAdditionalInfoDao extends BaseMapper<VisitorAdditionalInfoEntity> {
    /**
     * 根据登记ID查询附加信息
     */
    @Select("SELECT * FROM t_visitor_additional_info WHERE registration_id = #{registrationId} AND deleted_flag = 0")
    VisitorAdditionalInfoEntity selectByRegistrationId(Long registrationId);
}
```

---

## ✅ 更新完成标准

### Service层

- [x] Service接口保持向后兼容
- [x] Service实现通过Manager层操作数据
- [x] 事务管理正确（@Transactional）
- [x] 日志记录完整

### Manager层

- [x] 创建登记时同时插入6个表
- [x] 查询登记时JOIN 6个表组装数据
- [x] 更新登记时更新相关表
- [x] 事务一致性保证

### DAO层

- [x] 5个新DAO已创建
- [x] 添加selectByRegistrationId查询方法
- [x] 使用@Mapper注解（非@Repository）

---

## 🚀 执行步骤

### Step 1: 更新Manager层（2小时）

1. 在SelfServiceRegistrationManager中注入5个新DAO
2. 实现createRegistration方法（插入6个表）
3. 实现getRegistrationByVisitorCode方法（JOIN 6个表）
4. 实现其他方法（approveRegistration、checkIn、checkOut等）

### Step 2: 更新DAO层（1小时）

1. 在5个新DAO中添加selectByRegistrationId方法
2. 更新SelfServiceRegistrationDao（如果需要）

### Step 3: 验证Service层（1小时）

1. 编译通过
2. 单元测试更新
3. 集成测试验证

**总预计时间**: 4小时

---

**文档版本**: v1.0.0
**生成时间**: 2025-12-26
**维护人**: IOE-DREAM架构团队
