# IOE-DREAM 全局待办事项完整分析（V2完整版）

**文档版本**: v2.0.0 完整版
**生成时间**: 2025-12-23
**分析范围**: 全局代码TODO/FIXME + 所有业务模块
**目标**: 企业级高质量实现

---

## 📊 执行摘要（更新）

### 完整待办事项统计

| 模块 | TODO数量 | 优先级分布 | 预估工作量 |
|-----|---------|-----------|-----------|
| **安全认证模块** (microservices-common-security) | 10 | P0(6) P1(4) | 20人天 |
| **门禁服务** (ioedream-access-service) | 25 | P0(8) P1(12) P2(5) | 35人天 |
| **考勤服务** (ioedream-attendance-service) | 40+ | P0(15) P1(20) P2(5) | 60人天 |
| **消费服务** (ioedream-consume-service) | 15 | P0(5) P1(8) P2(2) | 25人天 |
| **访客服务** (ioedream-visitor-service) | 3 | P1(3) | 5人天 |
| **视频服务** (ioedream-video-service) | 30+ | P0(10) P1(15) P2(5) | 50人天 |
| **公共业务模块** (microservices-common-business) | 1 | P1 | 2人天 |

**🎯 总计**: **124+** 个待办事项需要实现
**📅 预估总工作量**: **197人天**（约6个月，5人团队）

---

## 🔍 按模块详细分析

### 4. 考勤服务 (ioedream-attendance-service) - 40+ TODO

#### P0级 - 核心功能（15项）

##### 4.1 生物识别打卡
**文件**: `StandardAttendanceProcess.java:48`
**TODO**: 实现生物识别逻辑
**业务背景**: 考勤打卡需要支持人脸、指纹等生物识别方式

**企业级实现方案**:

```java
@Service
@Slf4j
public class BiometricAttendanceService {

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Resource
    private AttendanceRecordDao attendanceRecordDao;

    /**
     * 生物识别打卡处理
     */
    public AttendanceResultVO processBiometricAttendance(AttendancePunchForm punchForm) {
        log.info("[考勤打卡] 生物识别打卡: userId={}, type={}",
            punchForm.getUserId(), punchForm.getAuthType());

        // 1. 提取生物特征数据
        byte[] biometricData = extractBiometricData(punchForm);

        // 2. 调用生物识别服务验证
        BiometricVerifyResult verifyResult = verifyBiometric(
            punchForm.getUserId(),
            punchForm.getAuthType(),
            biometricData
        );

        if (!verifyResult.isMatch()) {
            log.warn("[考勤打卡] 生物识别失败: userId={}, reason={}",
                punchForm.getUserId(), verifyResult.getFailReason());

            AttendanceResultVO result = new AttendanceResultVO();
            result.setSuccess(false);
            result.setMessage("生物识别失败: " + verifyResult.getFailReason());
            return result;
        }

        // 3. 创建考勤记录
        AttendanceRecordEntity record = createAttendanceRecord(punchForm);
        record.setAuthType(punchForm.getAuthType());
        record.setBiometricDataHash(hashBiometricData(biometricData));

        attendanceRecordDao.insert(record);

        // 4. 计算考勤结果
        AttendanceResultVO result = calculateAttendanceResult(record);

        // 5. 实时推送（WebSocket/RabbitMQ）
        pushAttendanceResult(result);

        return result;
    }

    /**
     * 从打卡表单提取生物特征数据
     */
    private byte[] extractBiometricData(AttendancePunchForm punchForm) {
        // 根据认证类型提取数据
        // 1-人脸、2-指纹、3-虹膜、4-掌纹、5-声纹
        switch (punchForm.getAuthType()) {
            case 1: // 人脸
                return Base64.getDecoder().decode(punchForm.getFaceImage());
            case 2: // 指纹
                return Base64.getDecoder().decode(punchForm.getFingerprintData());
            case 3: // 虹膜
                return Base64.getDecoder().decode(punchForm.getIrisData());
            case 4: // 掌纹
                return Base64.getDecoder().decode(punchForm.getPalmData());
            case 5: // 声纹
                return Base64.getDecoder().decode(punchForm.getVoiceData());
            default:
                throw new BusinessException("UNSUPPORTED_AUTH_TYPE", "不支持的认证类型");
        }
    }

    /**
     * 生物识别验证
     */
    private BiometricVerifyResult verifyBiometric(Long userId, Integer authType, byte[] biometricData) {
        // 调用生物识别服务
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("userId", userId);
            request.put("authType", authType);
            request.put("biometricData", biometricData);

            ResponseDTO<BiometricVerifyResult> response = gatewayServiceClient.callBiometricService(
                "/api/biometric/verify",
                HttpMethod.POST,
                request,
                new TypeReference<ResponseDTO<BiometricVerifyResult>>() {}
            );

            if (response.isSuccess()) {
                return response.getData();
            } else {
                log.error("[考勤打卡] 生物识别服务调用失败: {}", response.getMessage());
                return BiometricVerifyResult.fail("识别服务异常");
            }
        } catch (Exception e) {
            log.error("[考勤打卡] 生物识别异常", e);
            return BiometricVerifyResult.fail("识别服务异常");
        }
    }

    /**
     * 实时推送考勤结果
     */
    private void pushAttendanceResult(AttendanceResultVO result) {
        // TODO: 实现WebSocket推送、RabbitMQ消息等
        log.info("[考勤打卡] 推送考勤结果: userId={}, status={}",
            result.getUserId(), result.getStatus());

        // 方案1: WebSocket实时推送
        // webSocketMessageService.sendToUser(result.getUserId(), "ATTENDANCE_RESULT", result);

        // 方案2: RabbitMQ异步消息
        // rabbitTemplate.convertAndSend("attendance.exchange", "attendance.result", result);

        // 方案3: Server-Sent Events (SSE)
        // sseEmitterService.send(result.getUserId(), result);
    }

    private String hashBiometricData(byte[] data) {
        // SHA-256哈希，用于隐私保护
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("[考勤打卡] 生物特征哈希失败", e);
            return "";
        }
    }
}
```

##### 4.2 GPS位置验证
**文件**: `GPSValidationDecorator.java:38`
**TODO**: 实现GPS位置验证逻辑
**业务背景**: 打卡需要验证用户是否在指定区域内

**企业级实现方案**:

```java
@Component
@Slf4j
public class GPSLocationValidator {

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    private static final String AREA_CACHE_PREFIX = "attendance:area:";
    private static final int CACHE_HOURS = 24;

    /**
     * 验证GPS位置是否在考勤区域内
     */
    public boolean isWithinAttendanceArea(Long userId, Double latitude, Double longitude) {
        log.debug("[GPS验证] 验证位置: userId={}, lat={}, lng={}", userId, latitude, longitude);

        // 1. 查询用户所属部门的考勤区域配置
        List<AttendanceAreaConfig> areaConfigs = getAttendanceAreaConfigs(userId);

        if (areaConfigs.isEmpty()) {
            log.warn("[GPS验证] 未配置考勤区域: userId={}", userId);
            // 如果没有配置区域，默认允许
            return true;
        }

        // 2. 检查是否在任意一个配置的区域内
        for (AttendanceAreaConfig config : areaConfigs) {
            if (isPointInArea(latitude, longitude, config)) {
                log.debug("[GPS验证] 位置验证通过: userId={}, area={}", userId, config.getAreaName());
                return true;
            }
        }

        log.warn("[GPS验证] 位置不在考勤区域内: userId={}, lat={}, lng={}", userId, latitude, longitude);
        return false;
    }

    /**
     * 获取用户的考勤区域配置
     */
    private List<AttendanceAreaConfig> getAttendanceAreaConfigs(Long userId) {
        // 先从缓存获取
        String cacheKey = AREA_CACHE_PREFIX + userId;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return JSON.parseArray(cached, AttendanceAreaConfig.class);
        }

        // 调用区域服务查询
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("userId", userId);
            request.put("areaType", "ATTENDANCE");

            ResponseDTO<List<AttendanceAreaConfig>> response = gatewayServiceClient.callAreaService(
                "/api/area/user-areas",
                HttpMethod.POST,
                request,
                new TypeReference<ResponseDTO<List<AttendanceAreaConfig>>>() {}
            );

            if (response.isSuccess() && response.getData() != null) {
                List<AttendanceAreaConfig> configs = response.getData();

                // 缓存结果
                redisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(configs),
                    CACHE_HOURS, TimeUnit.HOURS);

                return configs;
            }
        } catch (Exception e) {
            log.error("[GPS验证] 查询考勤区域失败: userId={}", userId, e);
        }

        return Collections.emptyList();
    }

    /**
     * 判断点是否在区域内
     * 支持圆形区域和多边形区域
     */
    private boolean isPointInArea(Double lat, Double lng, AttendanceAreaConfig config) {
        if ("CIRCLE".equals(config.getAreaType())) {
            // 圆形区域：计算距离
            return isPointInCircle(lat, lng, config);
        } else if ("POLYGON".equals(config.getAreaType())) {
            // 多边形区域：射线法
            return isPointInPolygon(lat, lng, config);
        }

        log.warn("[GPS验证] 不支持的区域类型: {}", config.getAreaType());
        return false;
    }

    /**
     * 判断点是否在圆形区域内
     */
    private boolean isPointInCircle(Double lat, Double lng, AttendanceAreaConfig config) {
        // 中心点
        Double centerLat = config.getCenterLatitude();
        Double centerLng = config.getCenterLongitude();
        Double radius = config.getRadius(); // 半径（米）

        // 计算两点间距离（Haversine公式）
        double distance = calculateDistance(lat, lng, centerLat, centerLng);

        return distance <= radius;
    }

    /**
     * 计算两点间距离（米）
     * 使用Haversine公式
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371000; // 地球半径（米）

        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    /**
     * 判断点是否在多边形内
     * 使用射线法（Ray Casting Algorithm）
     */
    private boolean isPointInPolygon(Double lat, Double lng, AttendanceAreaConfig config) {
        List<Point> polygon = config.getPolygonPoints();
        if (polygon == null || polygon.size() < 3) {
            return false;
        }

        int crossings = 0;
        for (int i = 0; i < polygon.size(); i++) {
            Point p1 = polygon.get(i);
            Point p2 = polygon.get((i + 1) % polygon.size());

            // 检查射线是否与边相交
            if (((p1.getLatitude() <= lat && lat < p2.getLatitude()) ||
                 (p2.getLatitude() <= lat && lat < p1.getLatitude())) &&
                (lng < (p2.getLongitude() - p1.getLongitude()) *
                 (lat - p1.getLatitude()) /
                 (p2.getLatitude() - p1.getLatitude()) + p1.getLongitude())) {
                crossings++;
            }
        }

        return (crossings % 2) == 1;
    }
}
```

##### 4.3 工时计算策略

**文件**:
- `StandardWorkingHoursStrategy.java:36` - 标准工时制
- `ShiftWorkingHoursStrategy.java:36` - 轮班制
- `FlexibleWorkingHoursStrategy.java:36` - 弹性工作制

**TODO**: 实现各种工时计算逻辑
**业务背景**: 不同企业采用不同的工时制度

**企业级实现方案**:

```java
/**
 * 标准工时制计算策略
 */
@Service
@Slf4j
public class StandardWorkingHoursStrategy implements WorkingHoursStrategy {

    @Resource
    private WorkShiftDao workShiftDao;

    @Override
    public AttendanceResultVO calculate(AttendanceRecordEntity record, Object rule) {
        log.info("[工时计算] 标准工时制: recordId={}, userId={}",
            record.getRecordId(), record.getUserId());

        // 1. 查询当天班次计划
        WorkShiftEntity shift = getWorkShiftForDate(record.getUserId(), record.getPunchTime());
        if (shift == null) {
            log.warn("[工时计算] 未找到班次计划: userId={}, date={}",
                record.getUserId(), record.getPunchTime());
            return createNoShiftResult(record);
        }

        // 2. 解析考勤规则
        StandardWorkingHoursRule stdRule = (StandardWorkingHoursRule) rule;
        LocalTime workStartTime = shift.getWorkStartTime();
        LocalTime workEndTime = shift.getWorkEndTime();
        LocalTime lateGracePeriod = stdRule.getLateGracePeriod(); // 迟到宽限期

        // 3. 计算迟到、早退、加班等
        LocalTime punchTime = record.getPunchTime().toLocalTime();

        AttendanceResultVO result = new AttendanceResultVO();
        result.setUserId(record.getUserId());
        result.setDate(record.getPunchTime().toLocalDate());

        // 判断是否迟到
        if (punchTime.isAfter(workStartTime.plusMinutes(lateGracePeriod.getMinute()))) {
            long lateMinutes = ChronoUnit.MINUTES.between(workStartTime, punchTime);
            result.setLate(true);
            result.setLateMinutes((int) lateMinutes);
            result.setStatus(AttendanceStatus.LATE.getCode());
            log.info("[工时计算] 迟到: userId={}, lateMinutes={}", record.getUserId(), lateMinutes);
        } else {
            result.setStatus(AttendanceStatus.NORMAL.getCode());
        }

        // 判断是否早退（需要下班打卡）
        // ...

        // 计算工作时长
        // ...

        return result;
    }

    /**
     * 查询指定日期的班次
     */
    private WorkShiftEntity getWorkShiftForDate(Long userId, LocalDateTime punchTime) {
        LocalDate date = punchTime.toLocalDate();

        // 查询用户的班次安排
        WorkShiftScheduleEntity schedule = workShiftDao.selectOne(
            new LambdaQueryWrapper<WorkShiftScheduleEntity>()
                .eq(WorkShiftScheduleEntity::getUserId, userId)
                .eq(WorkShiftScheduleEntity::getScheduleDate, date)
                .last("LIMIT 1")
        );

        if (schedule != null) {
            return workShiftDao.selectById(schedule.getShiftId());
        }

        // 如果没有个人排班，查询部门默认班次
        return workShiftDao.selectOne(
            new LambdaQueryWrapper<WorkShiftEntity>()
                .eq(WorkShiftEntity::getIsDefault, true)
                .last("LIMIT 1")
        );
    }

    private AttendanceResultVO createNoShiftResult(AttendanceRecordEntity record) {
        AttendanceResultVO result = new AttendanceResultVO();
        result.setUserId(record.getUserId());
        result.setDate(record.getPunchTime().toLocalDate());
        result.setStatus(AttendanceStatus.NO_SHIFT.getCode());
        result.setMessage("未找到班次安排");
        return result;
    }
}

/**
 * 轮班制计算策略
 */
@Service
@Slf4j
public class ShiftWorkingHoursStrategy implements WorkingHoursStrategy {

    @Resource
    private ShiftScheduleDao shiftScheduleDao;

    @Override
    public AttendanceResultVO calculate(AttendanceRecordEntity record, Object rule) {
        log.info("[工时计算] 轮班制: recordId={}, userId={}",
            record.getRecordId(), record.getUserId());

        // 1. 查询当天轮班计划
        ShiftScheduleEntity shiftSchedule = getShiftSchedule(record.getUserId(), record.getPunchTime());
        if (shiftSchedule == null) {
            return createNoShiftResult(record);
        }

        // 2. 获取班次信息
        WorkShiftEntity shift = workShiftDao.selectById(shiftSchedule.getShiftId());

        // 3. 计算考勤结果（考虑跨天班次）
        AttendanceResultVO result = new AttendanceResultVO();
        result.setUserId(record.getUserId());
        result.setDate(record.getPunchTime().toLocalDate());
        result.setShiftId(shift.getShiftId());
        result.setShiftName(shift.getShiftName());

        // 计算迟到、早退等（需要考虑班次可能跨天）
        // ...

        return result;
    }

    private ShiftScheduleEntity getShiftSchedule(Long userId, LocalDateTime punchTime) {
        LocalDate date = punchTime.toLocalDate();

        return shiftScheduleDao.selectOne(
            new LambdaQueryWrapper<ShiftScheduleEntity>()
                .eq(ShiftScheduleEntity::getUserId, userId)
                .eq(ShiftScheduleEntity::getScheduleDate, date)
                .last("LIMIT 1")
        );
    }
}

/**
 * 弹性工作制计算策略
 */
@Service
@Slf4j
public class FlexibleWorkingHoursStrategy implements WorkingHoursStrategy {

    @Override
    public AttendanceResultVO calculate(AttendanceRecordEntity record, Object rule) {
        log.info("[工时计算] 弹性工作制: recordId={}, userId={}",
            record.getRecordId(), record.getUserId());

        // 1. 查询当天所有打卡记录
        List<AttendanceRecordEntity> allRecords = getAllRecordsForDate(
            record.getUserId(),
            record.getPunchTime().toLocalDate()
        );

        if (allRecords.isEmpty()) {
            return createNoRecordResult(record);
        }

        // 2. 计算工作时长（第一次打卡到最后一次打卡）
        LocalTime firstPunch = allRecords.get(0).getPunchTime().toLocalTime();
        LocalTime lastPunch = allRecords.get(allRecords.size() - 1).getPunchTime().toLocalTime();

        long workMinutes = ChronoUnit.MINUTES.between(firstPunch, lastPunch);

        // 3. 判断是否满足弹性工作时长要求
        FlexibleWorkingHoursRule flexRule = (FlexibleWorkingHoursRule) rule;
        int requiredMinutes = flexRule.getRequiredWorkMinutes(); // 要求工作时长（分钟）

        AttendanceResultVO result = new AttendanceResultVO();
        result.setUserId(record.getUserId());
        result.setDate(record.getPunchTime().toLocalDate());
        result.setWorkMinutes((int) workMinutes);

        if (workMinutes >= requiredMinutes) {
            result.setStatus(AttendanceStatus.NORMAL.getCode());
        } else {
            result.setStatus(AttendanceStatus.INSUFFICIENT_HOURS.getCode());
            result.setMessage(String.format("工作时长不足: %d分钟/%d分钟", workMinutes, requiredMinutes));
        }

        return result;
    }

    private List<AttendanceRecordEntity> getAllRecordsForDate(Long userId, LocalDate date) {
        // 查询当天所有打卡记录
        // ...
        return Collections.emptyList();
    }
}
```

#### P1级 - 功能增强（20+项）

##### 4.4 智能排班引擎

**文件**: `SmartSchedulingEngine.java`
**TODO**: 实现智能排班算法
**业务背景**: 自动为员工排班，优化人力资源配置

**关键待办**:
- 员工数据获取逻辑（282行）
- 冲突检测逻辑（607行、621行）
- 最约束员工查找（664行）
- 最佳班次选择（673行）
- 价值评估计算（714行）

##### 4.5 排班冲突检测与解决

**文件**: `ConflictDetectorImpl.java`、`ConflictResolverImpl.java`
**TODO**: 实现冲突检测和解决策略

**关键待办**:
- 技能冲突检测（210行、416行）
- 班次容量冲突检测（277行）
- 时间调整策略（416行）
- 优先级策略（445行）
- 人员替换策略（485行）
- 分段处理策略（516行）

#### P2级 - 优化增强（5项）

##### 4.6 用户权限验证

**文件**: `RuleValidatorImpl.java:425`
**TODO**: 实现用户权限验证逻辑

---

### 5. 消费服务 (ioedream-consume-service) - 15 TODO

#### P0级 - 核心功能（5项）

##### 5.1 补贴管理功能
**文件**: `ConsumeSubsidyManager.java`
**TODO**: 实现补贴发放、批量发放、作废、延期、统计

**企业级实现方案**:

```java
@Service
@Slf4j
public class ConsumeSubsidyManagerImpl {

    @Resource
    private ConsumeSubsidyDao subsidyDao;

    @Resource
    private ConsumeAccountDao accountDao;

    @Resource
    private TransactionTemplate transactionTemplate;

    /**
     * 发放补贴到用户账户
     */
    @Transactional(rollbackFor = Exception.class)
    public void grantSubsidy(Long subsidyId, Long operatorId) {
        log.info("[补贴发放] 开始发放: subsidyId={}, operator={}", subsidyId, operatorId);

        // 1. 查询补贴记录
        ConsumeSubsidyEntity subsidy = subsidyDao.selectById(subsidyId);
        if (subsidy == null) {
            throw new BusinessException("SUBSIDY_NOT_FOUND", "补贴记录不存在");
        }

        // 2. 验证状态
        if (subsidy.getSubsidyStatus() != 0) {
            throw new BusinessException("SUBSIDY_ALREADY_GRANTED", "补贴已发放");
        }

        // 3. 查询用户账户
        ConsumeAccountEntity account = accountDao.selectOne(
            new LambdaQueryWrapper<ConsumeAccountEntity>()
                .eq(ConsumeAccountEntity::getUserId, subsidy.getUserId())
                .eq(ConsumeAccountEntity::getAccountType, subsidy.getAccountType())
        );

        if (account == null) {
            // 自动创建账户
            account = createAccount(subsidy.getUserId(), subsidy.getAccountType());
        }

        // 4. 发放补贴（事务处理）
        transactionTemplate.execute(status -> {
            // 更新账户余额
            account.setBalance(account.getBalance().add(subsidy.getAmount()));
            accountDao.updateById(account);

            // 更新补贴状态
            subsidy.setSubsidyStatus(1); // 已发放
            subsidy.setGrantTime(LocalDateTime.now());
            subsidy.setGrantBy(operatorId);
            subsidyDao.updateById(subsidy);

            // 记录交易流水
            ConsumeTransactionEntity transaction = new ConsumeTransactionEntity();
            transaction.setUserId(subsidy.getUserId());
            transaction.setAccountType(subsidy.getAccountType());
            transaction.setTransactionType(2); // 补贴发放
            transaction.setAmount(subsidy.getAmount());
            transaction.setBalanceBefore(account.getBalance().subtract(subsidy.getAmount()));
            transaction.setBalanceAfter(account.getBalance());
            transaction.setRelatedId(subsidyId.toString());
            transaction.setRemark("补贴发放: " + subsidy.getSubsidyName());

            // 保存交易记录
            // ...

            log.info("[补贴发放] 发放成功: subsidyId={}, userId={}, amount={}",
                subsidyId, subsidy.getUserId(), subsidy.getAmount());

            return true;
        });

        // 5. 发送通知
        sendSubsidyGrantedNotification(subsidy, account);
    }

    /**
     * 批量发放补贴
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchGrantSubsidy(Long subsidyBatchId, Long operatorId) {
        log.info("[补贴发放] 批量发放开始: batchId={}, operator={}", subsidyBatchId, operatorId);

        // 查询批量补贴记录
        List<ConsumeSubsidyEntity> subsidies = subsidyDao.selectList(
            new LambdaQueryWrapper<ConsumeSubsidyEntity>()
                .eq(ConsumeSubsidyEntity::getBatchId, subsidyBatchId)
                .eq(ConsumeSubsidyEntity::getSubsidyStatus, 0) // 未发放
        );

        if (subsidies.isEmpty()) {
            log.warn("[补贴发放] 没有需要发放的补贴: batchId={}", subsidyBatchId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("totalCount", 0);
            result.put("grantedCount", 0);
            result.put("failedCount", 0);
            return result;
        }

        // 批量发放
        int grantedCount = 0;
        int failedCount = 0;
        List<String> errors = new ArrayList<>();

        for (ConsumeSubsidyEntity subsidy : subsidies) {
            try {
                grantSubsidy(subsidy.getSubsidyId(), operatorId);
                grantedCount++;
            } catch (Exception e) {
                log.error("[补贴发放] 发放失败: subsidyId={}, error={}",
                    subsidy.getSubsidyId(), e.getMessage(), e);
                failedCount++;
                errors.add(subsidy.getUserId() + ": " + e.getMessage());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("totalCount", subsidies.size());
        result.put("grantedCount", grantedCount);
        result.put("failedCount", failedCount);
        result.put("errors", errors);

        log.info("[补贴发放] 批量发放完成: total={}, granted={}, failed={}",
            subsidies.size(), grantedCount, failedCount);

        return result;
    }

    /**
     * 作废补贴
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelSubsidy(Long subsidyId, String reason, Long operatorId) {
        log.info("[补贴作废] 作废补贴: subsidyId={}, reason={}, operator={}",
            subsidyId, reason, operatorId);

        ConsumeSubsidyEntity subsidy = subsidyDao.selectById(subsidyId);
        if (subsidy == null) {
            throw new BusinessException("SUBSIDY_NOT_FOUND", "补贴记录不存在");
        }

        // 如果已发放，需要回收
        if (subsidy.getSubsidyStatus() == 1) {
            // 回收补贴
            revertSubsidy(subsidy, reason, operatorId);
        }

        // 更新状态为已作废
        subsidy.setSubsidyStatus(3); // 已作废
        subsidy.setCancelReason(reason);
        subsidy.setCancelBy(operatorId);
        subsidy.setCancelTime(LocalDateTime.now());
        subsidyDao.updateById(subsidy);

        log.info("[补贴作废] 作废成功: subsidyId={}", subsidyId);
    }

    /**
     * 回收已发放的补贴
     */
    private void revertSubsidy(ConsumeSubsidyEntity subsidy, String reason, Long operatorId) {
        // 查询账户
        ConsumeAccountEntity account = accountDao.selectOne(
            new LambdaQueryWrapper<ConsumeAccountEntity>()
                .eq(ConsumeAccountEntity::getUserId, subsidy.getUserId())
                .eq(ConsumeAccountEntity::getAccountType, subsidy.getAccountType())
        );

        if (account == null) {
            throw new BusinessException("ACCOUNT_NOT_FOUND", "账户不存在");
        }

        // 检查余额是否足够
        if (account.getBalance().compareTo(subsidy.getAmount()) < 0) {
            throw new BusinessException("INSUFFICIENT_BALANCE", "余额不足，无法回收补贴");
        }

        // 回收补贴（事务处理）
        transactionTemplate.execute(status -> {
            // 扣除余额
            account.setBalance(account.getBalance().subtract(subsidy.getAmount()));
            accountDao.updateById(account);

            // 记录交易流水
            ConsumeTransactionEntity transaction = new ConsumeTransactionEntity();
            transaction.setUserId(subsidy.getUserId());
            transaction.setAccountType(subsidy.getAccountType());
            transaction.setTransactionType(3); // 补贴回收
            transaction.setAmount(subsidy.getAmount().negate()); // 负数表示扣减
            transaction.setBalanceBefore(account.getBalance().add(subsidy.getAmount()));
            transaction.setBalanceAfter(account.getBalance());
            transaction.setRelatedId(subsidy.getSubsidyId().toString());
            transaction.setRemark("补贴回收: " + reason);

            // 保存交易记录
            // ...

            log.info("[补贴回收] 回收成功: subsidyId={}, userId={}, amount={}",
                subsidy.getSubsidyId(), subsidy.getUserId(), subsidy.getAmount());

            return true;
        });
    }

    /**
     * 延期补贴
     */
    @Transactional(rollbackFor = Exception.class)
    public void extendSubsidy(Long subsidyId, Integer days, Long operatorId) {
        log.info("[补贴延期] 延期补贴: subsidyId={}, days={}, operator={}",
            subsidyId, days, operatorId);

        ConsumeSubsidyEntity subsidy = subsidyDao.selectById(subsidyId);
        if (subsidy == null) {
            throw new BusinessException("SUBSIDY_NOT_FOUND", "补贴记录不存在");
        }

        // 延长有效期
        subsidy.setExpiryDate(subsidy.getExpiryDate().plusDays(days));
        subsidy.setUpdateBy(operatorId);
        subsidy.setUpdateTime(LocalDateTime.now());
        subsidyDao.updateById(subsidy);

        log.info("[补贴延期] 延期成功: subsidyId={}, newExpiryDate={}",
            subsidyId, subsidy.getExpiryDate());

        // 发送通知
        sendSubsidyExtendedNotification(subsidy, days);
    }

    /**
     * 获取补贴统计数据
     */
    public ConsumeSubsidyStatisticsVO getSubsidyStatistics() {
        log.info("[补贴统计] 查询统计数据");

        // 统计各状态补贴数量
        Map<Integer, Long> statusCounts = subsidyDao.selectGroupByStatus();

        // 统计总金额
        BigDecimal totalAmount = subsidyDao.selectSumAmount();

        // 统计本月发放金额
        BigDecimal monthGrantedAmount = subsidyDao.selectSumAmountByMonth(
            LocalDateTime.now().withDayOfMonth(1).toLocalDate()
        );

        ConsumeSubsidyStatisticsVO statistics = new ConsumeSubsidyStatisticsVO();
        statistics.setTotalCount(statusCounts.values().stream().mapToLong(Long::longValue).sum());
        statistics.setPendingCount(statusCounts.getOrDefault(0, 0L));
        statistics.setGrantedCount(statusCounts.getOrDefault(1, 0L));
        statistics.setExpiredCount(statusCounts.getOrDefault(2, 0L));
        statistics.setCancelledCount(statusCounts.getOrDefault(3, 0L));
        statistics.setTotalAmount(totalAmount);
        statistics.setMonthGrantedAmount(monthGrantedAmount);

        return statistics;
    }

    private ConsumeAccountEntity createAccount(Long userId, Integer accountType) {
        ConsumeAccountEntity account = new ConsumeAccountEntity();
        account.setUserId(userId);
        account.setAccountType(accountType);
        account.setBalance(BigDecimal.ZERO);
        account.setStatus(1); // 正常
        account.setCreateTime(LocalDateTime.now());
        accountDao.insert(account);
        return account;
    }

    private void sendSubsidyGrantedNotification(ConsumeSubsidyEntity subsidy, ConsumeAccountEntity account) {
        // 发送补贴到账通知
        // ...
    }

    private void sendSubsidyExtendedNotification(ConsumeSubsidyEntity subsidy, Integer days) {
        // 发送延期通知
        // ...
    }
}
```

##### 5.2 报表导出功能
**文件**: `ConsumeReportServiceImpl.java:372`
**TODO**: 实现报表导出逻辑

#### P1级 - 功能增强（8项）

##### 5.3 补贴审核流程
**文件**: `ConsumeSubsidyServiceImpl.java`
**TODO**: 实现审核、拒绝、审批逻辑

**关键待办**:
- 审核逻辑（507行）
- 拒绝逻辑（554行）
- 审批逻辑（577行）

---

### 6. 访客服务 (ioedream-visitor-service) - 3 TODO

#### P1级 - 功能增强（3项）

##### 6.1 访客验证策略

**文件**:
- `TemporaryVisitorStrategy.java:32` - 临时访客中心验证
- `RegularVisitorStrategy.java:32` - 常客边缘验证

**TODO**: 实现访客验证逻辑
**业务背景**: 临时访客需要中心验证，常客支持边缘验证

**企业级实现方案**:

```java
/**
 * 临时访客验证策略（中心验证）
 */
@Service
@Slf4j
public class TemporaryVisitorStrategy implements VisitorVerificationStrategy {

    @Resource
    private VisitorAppointmentDao appointmentDao;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Override
    public VisitorVerificationResult verify(Long visitorId, String verificationData) {
        log.info("[访客验证] 临时访客验证: visitorId={}", visitorId);

        // 1. 查询预约记录
        VisitorAppointmentEntity appointment = appointmentDao.selectOne(
            new LambdaQueryWrapper<VisitorAppointmentEntity>()
                .eq(VisitorAppointmentEntity::getVisitorId, visitorId)
                .eq(VisitorAppointmentEntity::getAppointmentStatus, 1) // 已审批
                .ge(VisitorAppointmentEntity::getVisitStartTime, LocalDateTime.now())
                .le(VisitorAppointmentEntity::getVisitEndTime, LocalDateTime.now())
                .last("LIMIT 1")
        );

        if (appointment == null) {
            log.warn("[访客验证] 没有有效预约: visitorId={}", visitorId);
            return VisitorVerificationResult.fail("没有有效预约");
        }

        // 2. 提取验证数据（二维码/访客码）
        VisitorVerificationData data = parseVerificationData(verificationData);

        // 3. 验证访客码
        if (!data.getVisitorId().equals(visitorId) ||
            !data.getAppointmentId().equals(appointment.getAppointmentId())) {
            log.warn("[访客验证] 访客码验证失败: visitorId={}", visitorId);
            return VisitorVerificationResult.fail("访客码验证失败");
        }

        // 4. 检查访问时间
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(appointment.getVisitStartTime()) ||
            now.isAfter(appointment.getVisitEndTime())) {
            log.warn("[访客验证] 不在访问时间范围内: visitorId={}, now={}, start={}, end={}",
                visitorId, now, appointment.getVisitStartTime(), appointment.getVisitEndTime());
            return VisitorVerificationResult.fail("不在访问时间范围内");
        }

        // 5. 验证通过，生成临时生物特征模板
        BiometricTemplateEntity template = generateTemporaryTemplate(
            visitorId,
            appointment.getVisitEndTime()
        );

        // 6. 下发模板到门禁设备
        List<String> deviceIds = getAccessControlDevices(appointment.getVisitAreaId());
        for (String deviceId : deviceIds) {
            sendTemplateToDevice(deviceId, template);
        }

        log.info("[访客验证] 临时访客验证成功: visitorId={}, templateId={}",
            visitorId, template.getTemplateId());

        return VisitorVerificationResult.success(appointment);
    }

    /**
     * 生成临时生物特征模板
     */
    private BiometricTemplateEntity generateTemporaryTemplate(Long visitorId, LocalDateTime expiryTime) {
        // 调用生物识别服务创建临时模板
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("userId", visitorId);
            request.put("templateType", "TEMPORARY");
            request.put("expiryTime", expiryTime);

            ResponseDTO<BiometricTemplateEntity> response = gatewayServiceClient.callBiometricService(
                "/api/biometric/template/create-temporary",
                HttpMethod.POST,
                request,
                new TypeReference<ResponseDTO<BiometricTemplateEntity>>() {}
            );

            if (response.isSuccess()) {
                return response.getData();
            } else {
                throw new BusinessException("TEMPLATE_CREATE_FAILED", response.getMessage());
            }
        } catch (Exception e) {
            log.error("[访客验证] 创建临时模板失败: visitorId={}", visitorId, e);
            throw new BusinessException("TEMPLATE_CREATE_FAILED", "创建临时模板失败");
        }
    }

    /**
     * 获取门禁设备列表
     */
    private List<String> getAccessControlDevices(Long areaId) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("areaId", areaId);
            request.put("deviceType", 1); // 门禁设备

            ResponseDTO<List<String>> response = gatewayServiceClient.callDeviceService(
                "/api/device/area-devices",
                HttpMethod.POST,
                request,
                new TypeReference<ResponseDTO<List<String>>>() {}
            );

            if (response.isSuccess()) {
                return response.getData();
            }
        } catch (Exception e) {
            log.error("[访客验证] 获取门禁设备失败: areaId={}", areaId, e);
        }

        return Collections.emptyList();
    }

    /**
     * 发送模板到设备
     */
    private void sendTemplateToDevice(String deviceId, BiometricTemplateEntity template) {
        // 通过设备通讯服务发送模板
        // ...
    }

    private VisitorVerificationData parseVerificationData(String verificationData) {
        // 解析访客码（JSON格式）
        // ...
        return new VisitorVerificationData();
    }
}

/**
 * 常客验证策略（边缘验证）
 */
@Service
@Slf4j
public class RegularVisitorStrategy implements VisitorVerificationStrategy {

    @Resource
    private VisitorPassDao visitorPassDao;

    @Override
    public VisitorVerificationResult verify(Long visitorId, String verificationData) {
        log.info("[访客验证] 常客验证: visitorId={}", visitorId);

        // 1. 查询电子通行证
        VisitorPassEntity pass = visitorPassDao.selectOne(
            new LambdaQueryWrapper<VisitorPassEntity>()
                .eq(VisitorPassEntity::getVisitorId, visitorId)
                .eq(VisitorPassEntity::getPassStatus, 1) // 有效
                .le(VisitorPassEntity::getExpiryDate, LocalDateTime.now())
                .last("LIMIT 1")
        );

        if (pass == null) {
            log.warn("[访客验证] 没有有效通行证: visitorId={}", visitorId);
            return VisitorVerificationResult.fail("没有有效通行证");
        }

        // 2. 常客可以直接在设备端验证（边缘验证）
        // 生物特征模板已预先下发到所有授权设备

        log.info("[访客验证] 常客验证成功: visitorId={}, passId={}",
            visitorId, pass.getPassId());

        return VisitorVerificationResult.success(pass);
    }
}
```

---

### 7. 视频服务 (ioedream-video-service) - 30+ TODO

#### P0级 - 核心功能（10项）

##### 7.1 录像管理功能

**文件**: `VideoRecordingServiceImpl.java`
**TODO**: 实现录像查询、搜索、播放、删除、下载、转码、备份等完整功能

**关键待办**:
- 录像分页查询（55行）
- 录像搜索（78行）
- 录像详情（100行）
- 播放令牌生成（124行）
- 时间轴查询（158行）
- 按时间范围查询（183行）
- 录像删除（204行）
- 录像下载（269行）
- 录像统计（291行）
- 录像完整性检查（320行）
- 录像文件修复（347行）
- 重要标记（443行）
- 取消重要标记（462行）
- 录像转码（502行）
- 转码任务状态查询（530行）
- 取消转码任务（556行）
- 录像备份（575行）
- 备份任务状态查询（602行）
- 过期录像清理（628行）
- 存储使用情况查询（651行）

**企业级实现方案**:

```java
@Service
@Slf4j
public class VideoRecordingServiceImpl implements VideoRecordingService {

    @Resource
    private VideoRecordingDao videoRecordingDao;

    @Resource
    private DeviceServiceClient deviceServiceClient;

    @Resource
    private StorageService storageService;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 录像分页查询
     */
    @Override
    public PageResult<VideoRecordingVO> queryRecordings(VideoRecordingQueryForm form) {
        log.info("[录像查询] 分页查询: {}", form);

        // 构建查询条件
        LambdaQueryWrapper<VideoRecordingEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(form.getDeviceId() != null, VideoRecordingEntity::getDeviceId, form.getDeviceId())
               .ge(form.getStartTime() != null, VideoRecordingEntity::getStartTime, form.getStartTime())
               .le(form.getEndTime() != null, VideoRecordingEntity::getEndTime, form.getEndTime())
               .orderByDesc(VideoRecordingEntity::getStartTime);

        // 分页查询
        Page<VideoRecordingEntity> page = new Page<>(form.getPageNum(), form.getPageSize());
        videoRecordingDao.selectPage(page, wrapper);

        // 转换为VO
        List<VideoRecordingVO> voList = page.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());

        return PageResult.of(voList, page.getTotal(), form.getPageNum(), form.getPageSize());
    }

    /**
     * 录像搜索（按事件类型、时间范围等）
     */
    @Override
    public List<VideoRecordingVO> searchRecordings(VideoRecordingSearchForm form) {
        log.info("[录像搜索] 搜索录像: {}", form);

        // 从ES搜索录像事件
        // ...

        return Collections.emptyList();
    }

    /**
     * 生成播放令牌
     */
    @Override
    public VideoRecordingPlaybackVO getPlaybackToken(Long recordingId, Long userId) {
        log.info("[录像回放] 生成播放令牌: recordingId={}, userId={}", recordingId, userId);

        // 1. 查询录像信息
        VideoRecordingEntity recording = videoRecordingDao.selectById(recordingId);
        if (recording == null) {
            throw new BusinessException("RECORDING_NOT_FOUND", "录像不存在");
        }

        // 2. 验证权限（用户是否有权播放该录像）
        if (!hasPlaybackPermission(userId, recording)) {
            throw new BusinessException("NO_PERMISSION", "没有播放权限");
        }

        // 3. 生成播放令牌（JWT，有效期1小时）
        String playbackToken = generatePlaybackToken(recordingId, userId);

        // 4. 构建播放URL
        String playbackUrl = buildPlaybackUrl(recording, playbackToken);

        VideoRecordingPlaybackVO vo = new VideoRecordingPlaybackVO();
        vo.setRecordingId(recordingId);
        vo.setPlaybackToken(playbackToken);
        vo.setPlaybackUrl(playbackUrl);
        vo.setStartTime(recording.getStartTime());
        vo.setEndTime(recording.getEndTime());
        vo.setDuration(recording.getDuration());
        vo.setFileSize(recording.getFileSize());

        return vo;
    }

    /**
     * 录像下载
     */
    @Override
    public Map<String, Object> downloadRecording(Long recordingId, Long userId) {
        log.info("[录像下载] 下载录像: recordingId={}, userId={}", recordingId, userId);

        // 1. 查询录像信息
        VideoRecordingEntity recording = videoRecordingDao.selectById(recordingId);
        if (recording == null) {
            throw new BusinessException("RECORDING_NOT_FOUND", "录像不存在");
        }

        // 2. 验证下载权限
        if (!hasDownloadPermission(userId, recording)) {
            throw new BusinessException("NO_PERMISSION", "没有下载权限");
        }

        // 3. 生成下载令牌（有效期2小时）
        String downloadToken = generateDownloadToken(recordingId, userId);

        // 4. 构建下载URL
        String downloadUrl = buildDownloadUrl(recording, downloadToken);

        Map<String, Object> result = new HashMap<>();
        result.put("recordingId", recordingId);
        result.put("downloadToken", downloadToken);
        result.put("downloadUrl", downloadUrl);
        result.put("fileName", generateFileName(recording));
        result.put("fileSize", recording.getFileSize());
        result.put("expiresAt", LocalDateTime.now().plusHours(2));

        return result;
    }

    /**
     * 录像转码
     */
    @Override
    @Async("videoTranscodeExecutor")
    public void transcodeRecording(Long recordingId, String targetFormat, Long userId) {
        log.info("[录像转码] 开始转码: recordingId={}, format={}, userId={}",
            recordingId, targetFormat, userId);

        // 1. 查询录像信息
        VideoRecordingEntity recording = videoRecordingDao.selectById(recordingId);
        if (recording == null) {
            log.error("[录像转码] 录像不存在: recordingId={}", recordingId);
            return;
        }

        // 2. 创建转码任务
        VideoTranscodeTaskEntity task = new VideoTranscodeTaskEntity();
        task.setRecordingId(recordingId);
        task.setSourceFormat(recording.getFileFormat());
        task.setTargetFormat(targetFormat);
        task.setTaskStatus(0); // 处理中
        task.setCreateTime(LocalDateTime.now());
        task.setCreateBy(userId);
        // 保存任务...

        // 3. 调用转码服务
        try {
            // 从存储服务下载原文件
            InputStream sourceStream = storageService.download(recording.getFilePath());

            // 执行转码（使用FFmpeg）
            InputStream transcodedStream = transcodeVideo(sourceStream, targetFormat);

            // 上传转码后的文件
            String transcodedPath = storageService.upload(
                transcodedStream,
                generateTranscodedFileName(recording, targetFormat)
            );

            // 更新任务状态
            task.setTaskStatus(1); // 成功
            task.setTranscodedPath(transcodedPath);
            task.setFinishTime(LocalDateTime.now());
            // 保存...

            log.info("[录像转码] 转码成功: recordingId={}, transcodedPath={}",
                recordingId, transcodedPath);

        } catch (Exception e) {
            log.error("[录像转码] 转码失败: recordingId={}", recordingId, e);

            // 更新任务状态为失败
            task.setTaskStatus(2); // 失败
            task.setErrorMessage(e.getMessage());
            task.setFinishTime(LocalDateTime.now());
            // 保存...
        }
    }

    /**
     * 录像备份
     */
    @Override
    @Async("videoBackupExecutor")
    public void backupRecording(Long recordingId, String backupType, Long userId) {
        log.info("[录像备份] 开始备份: recordingId={}, backupType={}, userId={}",
            recordingId, backupType, userId);

        // 1. 查询录像信息
        VideoRecordingEntity recording = videoRecordingDao.selectById(recordingId);
        if (recording == null) {
            log.error("[录像备份] 录像不存在: recordingId={}", recordingId);
            return;
        }

        // 2. 创建备份任务
        VideoBackupTaskEntity task = new VideoBackupTaskEntity();
        task.setRecordingId(recordingId);
        task.setBackupType(backupType);
        task.setTaskStatus(0); // 处理中
        task.setCreateTime(LocalDateTime.now());
        // 保存任务...

        // 3. 执行备份
        try {
            String backupPath;
            if ("OSS".equals(backupType)) {
                // 备份到对象存储
                backupPath = backupToOSS(recording);
            } else if ("NAS".equals(backupType)) {
                // 备份到NAS
                backupPath = backupToNAS(recording);
            } else {
                throw new BusinessException("UNSUPPORTED_BACKUP_TYPE", "不支持的备份类型");
            }

            // 更新任务状态
            task.setTaskStatus(1); // 成功
            task.setBackupPath(backupPath);
            task.setFinishTime(LocalDateTime.now());
            // 保存...

            log.info("[录像备份] 备份成功: recordingId={}, backupPath={}",
                recordingId, backupPath);

        } catch (Exception e) {
            log.error("[录像备份] 备份失败: recordingId={}", recordingId, e);

            task.setTaskStatus(2); // 失败
            task.setErrorMessage(e.getMessage());
            task.setFinishTime(LocalDateTime.now());
            // 保存...
        }
    }

    /**
     * 过期录像清理
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void cleanupExpiredRecordings() {
        log.info("[录像清理] 开始清理过期录像");

        // 1. 查询过期录像
        LocalDateTime expiryDate = LocalDateTime.now().minusDays(30); // 默认保留30天
        List<VideoRecordingEntity> expiredRecordings = videoRecordingDao.selectList(
            new LambdaQueryWrapper<VideoRecordingEntity>()
                .lt(VideoRecordingEntity::getStartTime, expiryDate)
                .eq(VideoRecordingEntity::getImportantFlag, false) // 非重要录像
                .orderByAsc(VideoRecordingEntity::getStartTime)
                .last("LIMIT 1000") // 每次最多清理1000个
        );

        log.info("[录像清理] 发现{}个过期录像", expiredRecordings.size());

        // 2. 清理录像
        int cleanedCount = 0;
        int failedCount = 0;

        for (VideoRecordingEntity recording : expiredRecordings) {
            try {
                // 从存储删除文件
                storageService.delete(recording.getFilePath());

                // 删除数据库记录
                videoRecordingDao.deleteById(recording.getRecordingId());

                cleanedCount++;

            } catch (Exception e) {
                log.error("[录像清理] 清理失败: recordingId={}",
                    recording.getRecordingId(), e);
                failedCount++;
            }
        }

        log.info("[录像清理] 清理完成: total={}, cleaned={}, failed={}",
            expiredRecordings.size(), cleanedCount, failedCount);
    }

    /**
     * 存储使用情况查询
     */
    @Override
    public Map<String, Object> getStorageUsage() {
        log.info("[录像存储] 查询存储使用情况");

        // 查询录像总大小
        Long totalSize = videoRecordingDao.selectSumFileSize();

        // 查询录像数量
        Long totalCount = videoRecordingDao.selectCount(
            new LambdaQueryWrapper<VideoRecordingEntity>()
        );

        // 查询各设备录像大小
        List<Map<String, Object>> deviceUsage = videoRecordingDao.selectFileSizeByDevice();

        Map<String, Object> result = new HashMap<>();
        result.put("totalSize", totalSize);
        result.put("totalSizeGB", totalSize / 1024.0 / 1024 / 1024);
        result.put("totalCount", totalCount);
        result.put("deviceUsage", deviceUsage);
        result.put("queryTime", LocalDateTime.now());

        return result;
    }

    // 辅助方法...

    private String generatePlaybackToken(Long recordingId, Long userId) {
        // 生成JWT令牌
        // ...
        return "";
    }

    private String buildPlaybackUrl(VideoRecordingEntity recording, String token) {
        // 构建播放URL
        return String.format("http://video-service/api/v1/playback/%s?token=%s",
            recording.getRecordingId(), token);
    }

    private boolean hasPlaybackPermission(Long userId, VideoRecordingEntity recording) {
        // 验证播放权限
        // ...
        return true;
    }

    private boolean hasDownloadPermission(Long userId, VideoRecordingEntity recording) {
        // 验证下载权限
        // ...
        return true;
    }

    private String generateDownloadToken(Long recordingId, Long userId) {
        // 生成下载令牌
        // ...
        return "";
    }

    private String buildDownloadUrl(VideoRecordingEntity recording, String token) {
        // 构建下载URL
        return "";
    }

    private String generateFileName(VideoRecordingEntity recording) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        return String.format("recording_%s_%s.mp4",
            recording.getDeviceId(),
            recording.getStartTime().format(formatter)
        );
    }

    private InputStream transcodeVideo(InputStream sourceStream, String targetFormat) {
        // 使用FFmpeg转码
        // ...
        return null;
    }

    private String generateTranscodedFileName(VideoRecordingEntity recording, String format) {
        // 生成转码文件名
        // ...
        return "";
    }

    private String backupToOSS(VideoRecordingEntity recording) {
        // 备份到对象存储
        // ...
        return "";
    }

    private String backupToNAS(VideoRecordingEntity recording) {
        // 备份到NAS
        // ...
        return "";
    }

    private VideoRecordingVO convertToVO(VideoRecordingEntity entity) {
        // 实体转VO
        // ...
        return new VideoRecordingVO();
    }
}
```

#### P1级 - 功能增强（15项）

##### 7.2 视频流协议适配器

**文件**:
- `HTTPAdapter.java:47,63` - HTTP流
- `RTMPAdapter.java:53,68` - RTMP流
- `RTSPAdapter.java:52,67` - RTSP流

**TODO**: 实现流停止和URL构建逻辑

##### 7.3 AI分析功能

**文件**: `VideoAiAnalysisServiceImpl.java`
**TODO**: 实现统计数据计算、历史分析、报告生成

**关键待办**:
- 统计数据计算（215行、298行）
- 历史视频分析（479行）
- 报告生成（509行）
- 视频帧获取（563行）

#### P2级 - 优化增强（5项）

##### 7.4 视频墙功能

**文件**: `VideoWallServiceImpl.java`
**TODO**: 实现预案解析、轮巡调度

**关键待办**:
- 预案配置解析（516行）
- 轮巡任务启动（617行）
- 轮巡任务停止（640行）

---

## 📈 完整实施路线图（更新）

### 第一阶段：核心安全（2周）✅ 已在V1版本中

### 第二阶段：门禁增强（3周）✅ 已在V1版本中

### 第三阶段：考勤功能（4周）⭐ 新增

**目标**: 完成考勤核心功能

| 任务 | 工作量 | 依赖 |
|------|--------|------|
| 生物识别打卡 | 5天 | - |
| GPS位置验证 | 4天 | - |
| 工时计算策略 | 5天 | - |
| WebSocket推送 | 2天 | - |
| 单元测试 | 3天 | 以上全部 |
| 集成测试 | 3天 | 以上全部 |

### 第四阶段：消费功能（3周）⭐ 新增

| 任务 | 工作量 | 依赖 |
|------|--------|------|
| 补贴管理 | 5天 | - |
| 补贴审核流程 | 3天 | 补贴管理 |
| 报表导出 | 3天 | - |
| 前端页面 | 4天 | 后端API |
| 测试和优化 | 3天 | 以上全部 |

### 第五阶段：视频功能（4周）⭐ 新增

| 任务 | 工作量 | 依赖 |
|------|--------|------|
| 录像管理 | 6天 | - |
| 流协议适配 | 4天 | - |
| AI分析 | 5天 | - |
| 视频墙 | 3天 | - |
| 前端页面 | 5天 | 后端API |
| 测试和优化 | 4天 | 以上全部 |

### 第六阶段：访客功能（1周）⭐ 新增

| 任务 | 工作量 | 依赖 |
|------|--------|------|
| 访客验证策略 | 3天 | - |
| 通行证管理 | 2天 | - |
| 测试和优化 | 2天 | 以上全部 |

---

## 📊 最终统计

### 人力投入评估

| 阶段 | 功能模块 | 人天 | 开始时间 | 结束时间 |
|------|---------|------|---------|---------|
| 1 | 核心安全 | 20 | Week 1 | Week 2 |
| 2 | 门禁增强 | 35 | Week 3 | Week 5 |
| 3 | 考勤功能 | 60 | Week 6 | Week 9 |
| 4 | 消费功能 | 25 | Week 10 | Week 12 |
| 5 | 视频功能 | 50 | Week 13 | Week 16 |
| 6 | 访客功能 | 5 | Week 17 | Week 17 |
| 7 | 集成测试 | 2 | Week 17 | Week 17 |

**总计**: **197人天**（约6个月，5人团队）

---

## 🏆 企业级实施建议

### 1. 优先级矩阵

根据**业务价值**和**技术风险**确定实施顺序：

```
高价值+低风险（优先实现）:
- JWT令牌撤销
- 用户锁定检查
- GPS位置验证
- 补贴发放

高价值+高风险（重点保障）:
- 并发登录控制
- 报警管理
- 录像管理
- 生物识别打卡

低价值+低风险（按需实现）:
- 认证方式统计
- 访客验证
- 视频墙
```

### 2. 技术债务管理

**当前技术债务**:
- 124+个TODO需要实现
- 部分功能只有骨架代码
- 缺少完整的单元测试

**还债策略**:
1. **P0级**（6个月内）：完成所有核心功能
2. **P1级**（12个月内）：完成功能增强
3. **P2级**（持续优化）：不断完善

### 3. 质量保障措施

```yaml
代码审查:
  - 所有代码必须经过Code Review
  - 遵循四层架构规范
  - 统一日志格式

测试覆盖:
  - 单元测试覆盖率 > 80%
  - 集成测试覆盖核心流程
  - 性能测试达标

文档要求:
  - API文档完整
  - 业务流程图清晰
  - 部署文档可操作
```

---

## 📝 附录：完整TODO清单

### A. 安全认证模块（10项）

| ID | 功能 | 优先级 | 工作量 |
|----|------|-------|--------|
| S1 | JWT令牌撤销 | P0 | 3天 |
| S2 | 用户锁定检查 | P0 | 3天 |
| S3 | 并发登录检查 | P0 | 4天 |
| S4 | 用户会话管理 | P1 | 3天 |
| S5 | 清除登录失败 | P1 | 1天 |
| S6 | 记录登录失败 | P1 | 2天 |
| S7 | 令牌黑名单 | P1 | 3天 |
| S8 | 黑名单检查 | P1 | 1天 |
| S9 | 移除用户会话 | P1 | 1天 |
| S10 | 用户会话验证 | P1 | 2天 |

### B. 门禁服务（25项）

| ID | 功能 | 优先级 | 工作量 |
|----|------|-------|--------|
| A1 | 异常监控集成 | P1 | 2天 |
| A2-A10 | 认证方式统计（9种认证方式） | P1 | 4天 |
| A11 | 认证统计实现 | P1 | 4天 |
| A12 | 报警查询 | P0 | 5天 |
| A13 | 报警处理 | P0 | 3天 |
| A14-A22 | 监控和统计增强 | P1-P2 | 15天 |

### C. 考勤服务（40+项）

| ID | 功能 | 优先级 | 工作量 |
|----|------|-------|--------|
| T1 | 生物识别打卡 | P0 | 5天 |
| T2 | GPS位置验证 | P0 | 4天 |
| T3-T5 | 工时计算策略（3种） | P0 | 5天 |
| T6 | 实时推送 | P0 | 2天 |
| T7-T25 | 智能排班引擎 | P1 | 30天 |
| T26-T40 | 冲突检测与解决 | P1 | 30天 |

### D. 消费服务（15项）

| ID | 功能 | 优先级 | 工作量 |
|----|------|-------|--------|
| C1-C4 | 补贴发放管理 | P0 | 5天 |
| C5-C7 | 补贴审核流程 | P1 | 3天 |
| C8 | 报表导出 | P1 | 3天 |
| C9-C15 | 其他功能增强 | P1-P2 | 14天 |

### E. 访客服务（3项）

| ID | 功能 | 优先级 | 工作量 |
|----|------|-------|--------|
| V1 | 临时访客验证 | P1 | 2天 |
| V2 | 常客验证 | P1 | 1天 |
| V3 | 通行证管理 | P1 | 2天 |

### F. 视频服务（30+项）

| ID | 功能 | 优先级 | 工作量 |
|----|------|-------|--------|
| V1-V19 | 录像管理 | P0 | 6天 |
| V20-V25 | 流协议适配 | P0 | 4天 |
| V26-V30 | AI分析 | P1 | 5天 |
| V31-V33 | 视频墙 | P1 | 3天 |
| V34-V40 | 其他功能 | P2 | 32天 |

---

**文档结束**

**下一步行动**:
1. 立即开始P0级任务
2. 建立周会机制跟踪进度
3. 每月更新待办清单
4. 持续优化代码质量
