# 🔌 IOE-DREAM 设备交互架构完整设计

**文档版本**: v3.1.0-DEVICE-INTERACTION  
**创建日期**: 2025-12-18  
**适用范围**: 所有智能设备与软件平台的交互模式  
**重要性**: ⭐⭐⭐⭐⭐ **架构核心设计**

---

## 🎯 **核心设计理念**

基于**设备端智能化程度**的差异，IOE-DREAM采用**混合架构模式**：
- **边缘计算设备**：设备端完成验证，软件端接收结果
- **中心计算设备**：设备端仅识别，软件端完成验证
- **混合模式设备**：根据场景动态选择计算位置

---

## 📊 **5种设备交互模式详解**

### **模式1: 门禁设备 - 边缘自主验证模式**

#### **交互流程**

```
┌─────────────────────────────────────────────────────────────────────┐
│  场景: 员工刷脸通过门禁                                               │
└─────────────────────────────────────────────────────────────────────┘

【初始化阶段】
软件端 (access-service + biometric-service)
  │
  ├─> 1. 配置用户权限
  │     └─> UserAreaPermissionEntity
  │         - userId: 10001
  │         - areaId: 2001 (办公楼3层)
  │         - allowedTimeSlots: [[09:00-18:00]]
  │         - validStartTime: 2025-01-01
  │         - validEndTime: 2025-12-31
  │
  ├─> 2. 准备生物模板
  │     └─> BiometricTemplateEntity
  │         - userId: 10001
  │         - biometricType: FACE
  │         - featureData: [512维向量]
  │
  ├─> 3. 下发数据到门禁设备
  │     └─> device-comm-service.syncToDevice()
  │         ├─ 人员信息 (姓名、工号)
  │         ├─ 生物模板 (人脸特征)
  │         └─ 权限数据 (时间段、区域)
  │
  └─> ✅ 下发完成，设备本地存储

【实时通行阶段】
门禁设备 (边缘端)
  │
  ├─> 1. 采集人脸图像
  │     └─> 摄像头拍摄
  │
  ├─> 2. 本地特征提取
  │     └─> 设备内嵌算法提取512维向量
  │
  ├─> 3. 本地1:N识别
  │     └─> 与本地存储的所有模板比对
  │         └─> 匹配到: userId=10001, 相似度=0.92
  │
  ├─> 4. 本地权限验证
  │     └─> 检查本地权限表
  │         - 当前时间: 14:30 ✅ 在[09:00-18:00]内
  │         - 当前日期: 2025-06-15 ✅ 在有效期内
  │         └─> ✅ 权限验证通过
  │
  ├─> 5. 开门动作
  │     └─> 继电器控制，开门5秒
  │
  └─> 6. 生成通行记录
        └─> AccessRecordLocal
            - userId: 10001
            - deviceId: DOOR-301
            - accessTime: 2025-06-15 14:30:25
            - authMethod: FACE
            - authResult: SUCCESS
            - matchScore: 0.92

【事后上传阶段】
门禁设备
  │
  └─> 批量上传通行记录 (每分钟或累计100条)
      └─> TCP/HTTP POST to device-comm-service

软件端 (device-comm-service)
  │
  ├─> 接收设备上传
  │
  ├─> 转发到 access-service
  │     └─> AccessRecordEntity.insert()
  │
  ├─> 实时监控推送
  │     └─> WebSocket → 监控大屏
  │
  ├─> 异常检测
  │     └─> 如果非授权时间通行 → 告警
  │
  └─> 视频联动
        └─> RabbitMQ → video-service
            └─> 触发该门禁点摄像头录像5分钟
```

#### **关键设计要点**

**✅ 优势**:
- **离线可用**: 网络中断时设备仍可正常工作
- **秒级响应**: 无需等待服务器验证，体验流畅
- **降低服务器压力**: 每秒1000次通行，服务器只需处理记录存储

**⚠️ 挑战**:
- **数据一致性**: 权限变更需要实时同步到设备
- **设备存储**: 大型园区可能有10000+人员，设备存储有限
- **安全风险**: 设备端存储敏感数据（模板、权限）

**🔧 解决方案**:
```java
/**
 * 权限变更实时推送
 */
@Service
public class AccessPermissionSyncService {
    
    @Resource
    private DeviceCommServiceClient deviceCommClient;
    
    /**
     * 当权限变更时，立即推送到相关设备
     */
    @Async("permissionSyncExecutor")
    public void syncPermissionChange(UserAreaPermissionEntity permission) {
        // 1. 查询该区域的所有门禁设备
        List<DeviceEntity> devices = deviceDao.selectByAreaId(
            permission.getAreaId()
        );
        
        // 2. 并行推送到所有设备
        List<CompletableFuture<Void>> futures = devices.stream()
            .map(device -> CompletableFuture.runAsync(() -> {
                deviceCommClient.updatePermission(
                    device.getDeviceId(),
                    permission
                );
            }))
            .collect(Collectors.toList());
        
        // 3. 等待所有推送完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .join();
        
        log.info("[权限同步完成] areaId={}, deviceCount={}", 
            permission.getAreaId(), devices.size());
    }
}
```

---

### **模式2: 消费设备 - 中心实时验证模式**

#### **交互流程**

```
┌─────────────────────────────────────────────────────────────────────┐
│  场景: 员工食堂刷脸消费                                               │
└─────────────────────────────────────────────────────────────────────┘

【初始化阶段】
软件端 (consume-service + biometric-service)
  │
  ├─> 1. 创建消费账户
  │     └─> AccountEntity
  │         - userId: 10001
  │         - balance: ¥500.00
  │         - accountType: PREPAID
  │
  ├─> 2. 准备生物模板
  │     └─> BiometricTemplateEntity (人脸)
  │
  ├─> 3. 下发数据到消费设备
  │     └─> device-comm-service.syncToDevice()
  │         ├─ 人员信息
  │         ├─ 生物模板
  │         └─ ⚠️ 不下发余额（安全考虑）
  │
  └─> ✅ 下发完成

【实时消费阶段】
消费设备 (边缘端)
  │
  ├─> 1. 采集人脸图像
  │
  ├─> 2. 本地特征提取
  │
  ├─> 3. 本地1:N识别
  │     └─> 匹配到: userId=10001
  │
  ├─> 4. 请求软件验证消费
  │     └─> HTTP POST to consume-service
  │         {
  │           "userId": 10001,
  │           "deviceId": "CANTEEN-101",
  │           "amount": 15.00,
  │           "mealType": "LUNCH",
  │           "timestamp": "2025-06-15 12:30:00"
  │         }
  │
  └─> ⏳ 等待软件返回结果...

软件端 (consume-service)
  │
  ├─> 1. 接收消费请求
  │
  ├─> 2. 验证账户余额
  │     └─> SELECT balance FROM account WHERE userId=10001
  │         └─> balance = ¥500.00 ✅ 充足
  │
  ├─> 3. 扣款（乐观锁）
  │     └─> UPDATE account 
  │         SET balance = balance - 15.00,
  │             version = version + 1
  │         WHERE userId=10001 
  │           AND version=10
  │         └─> ✅ 更新成功
  │
  ├─> 4. 创建消费记录
  │     └─> ConsumeRecordEntity.insert()
  │
  ├─> 5. 返回结果给设备
  │     └─> HTTP 200 OK
  │         {
  │           "success": true,
  │           "newBalance": 485.00,
  │           "consumeNo": "C20250615001"
  │         }
  │
  └─> 6. 异步通知
        └─> WebSocket → 监控大屏
        └─> 短信 → 用户手机（余额提醒）

消费设备
  │
  ├─> 接收成功响应
  │
  ├─> 打印小票
  │     └─> 消费金额: ¥15.00
  │         剩余余额: ¥485.00
  │         消费时间: 12:30
  │
  └─> 语音播报: "消费成功，余额485元"
```

#### **关键设计要点**

**✅ 优势**:
- **数据安全**: 余额数据不存储在设备端
- **实时准确**: 余额扣款实时同步，无超支风险
- **灵活控制**: 可实时冻结账户、调整费率

**⚠️ 挑战**:
- **网络依赖**: 网络中断时无法消费
- **并发冲突**: 同一账户同时消费可能冲突
- **响应延迟**: 需要等待服务器验证（100-300ms）

**🔧 解决方案**:

```java
/**
 * 消费验证服务（高并发优化）
 */
@Service
public class ConsumeVerificationService {
    
    @Resource
    private AccountDao accountDao;
    
    @Resource
    private RedissonClient redissonClient;
    
    /**
     * 处理消费请求（分布式锁）
     */
    public ResponseDTO<ConsumeResult> processConsume(ConsumeRequestDTO request) {
        String lockKey = "consume:lock:user:" + request.getUserId();
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            // 尝试获取锁，最多等待1秒
            if (lock.tryLock(1, 5, TimeUnit.SECONDS)) {
                // 1. 查询账户
                AccountEntity account = accountDao.selectByUserId(
                    request.getUserId()
                );
                
                // 2. 余额验证
                if (account.getBalance().compareTo(request.getAmount()) < 0) {
                    return ResponseDTO.error("余额不足");
                }
                
                // 3. 扣款（乐观锁）
                int updated = accountDao.deductBalance(
                    account.getAccountId(),
                    request.getAmount(),
                    account.getVersion()
                );
                
                if (updated == 0) {
                    // 版本冲突，重试
                    return processConsume(request);
                }
                
                // 4. 创建消费记录
                ConsumeRecordEntity record = new ConsumeRecordEntity();
                record.setUserId(request.getUserId());
                record.setAmount(request.getAmount());
                record.setConsumeTime(LocalDateTime.now());
                consumeRecordDao.insert(record);
                
                // 5. 返回结果
                AccountEntity newAccount = accountDao.selectByUserId(
                    request.getUserId()
                );
                
                return ResponseDTO.ok(ConsumeResult.builder()
                    .success(true)
                    .newBalance(newAccount.getBalance())
                    .consumeNo(record.getConsumeNo())
                    .build());
                
            } else {
                return ResponseDTO.error("系统繁忙，请稍后重试");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseDTO.error("系统异常");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}

/**
 * 离线消费支持
 */
@Service
public class OfflineConsumeService {
    
    /**
     * 离线白名单（账户余额充足的VIP用户）
     */
    public void syncOfflineWhitelist() {
        // 1. 查询余额>1000元的用户
        List<AccountEntity> vipAccounts = accountDao.selectByBalanceGreaterThan(
            new BigDecimal("1000.00")
        );
        
        // 2. 下发离线白名单到设备
        for (AccountEntity account : vipAccounts) {
            deviceCommClient.updateOfflineWhitelist(
                account.getUserId(),
                account.getBalance()
            );
        }
    }
}
```

---

### **模式3: 考勤设备 - 边缘识别+中心计算模式**

#### **交互流程**

```
┌─────────────────────────────────────────────────────────────────────┐
│  场景: 员工上班打卡                                                   │
└─────────────────────────────────────────────────────────────────────┘

【初始化阶段】
软件端 (attendance-service + biometric-service)
  │
  ├─> 1. 准备生物模板
  │     └─> BiometricTemplateEntity (人脸/指纹)
  │
  ├─> 2. 下发数据到考勤设备
  │     └─> device-comm-service.syncToDevice()
  │         ├─ 人员信息
  │         ├─ 生物模板
  │         └─ ⚠️ 不下发考勤规则（规则在软件端）
  │
  └─> ✅ 下发完成

【实时打卡阶段】
考勤设备 (边缘端)
  │
  ├─> 1. 采集生物特征
  │
  ├─> 2. 本地识别
  │     └─> 匹配到: userId=10001
  │
  ├─> 3. 生成打卡记录
  │     └─> AttendancePunchLocal
  │         - userId: 10001
  │         - punchTime: 2025-06-15 08:55:00
  │         - punchType: CLOCK_IN
  │         - deviceId: ATT-101
  │
  ├─> 4. 立即上传记录
  │     └─> HTTP POST to device-comm-service
  │
  └─> 5. 显示打卡成功
        └─> "张三，打卡成功！时间: 08:55"

软件端 (device-comm-service → attendance-service)
  │
  ├─> 1. 接收打卡记录
  │
  ├─> 2. 转发到 attendance-service
  │
  └─> 3. 保存原始记录
        └─> AttendancePunchRecordEntity.insert()

【事后计算阶段】（定时任务 + 实时计算）
软件端 (attendance-service)
  │
  ├─> **每日凌晨2点**，计算前一天考勤
  │
  ├─> 1. 查询用户打卡记录
  │     └─> SELECT * FROM attendance_punch_record
  │         WHERE userId=10001 
  │           AND punchDate='2025-06-15'
  │         └─> [08:55 CLOCK_IN, 18:10 CLOCK_OUT]
  │
  ├─> 2. 查询排班计划 ⭐ 新增
  │     └─> SELECT * FROM attendance_schedule
  │         WHERE userId=10001 
  │           AND scheduleDate='2025-06-15'
  │         └─> 排班信息:
  │             - shiftType: STANDARD_DAY (标准白班)
  │             - workStartTime: 09:00
  │             - workEndTime: 18:00
  │             - breakStartTime: 12:00
  │             - breakEndTime: 13:00
  │             - requiredWorkMinutes: 480
  │
  ├─> 3. 查询考勤规则
  │     └─> SELECT * FROM attendance_rule 
  │         WHERE ruleId=(从排班获取)
  │         └─> 规则详情:
  │             - lateGraceMinutes: 15 (迟到宽限)
  │             - earlyLeaveGraceMinutes: 10
  │             - overtimeCalculateAfterMinutes: 30
  │             - absenceAfterMinutes: 120
  │
  ├─> 4. 应用规则策略（结合排班+打卡）⭐ 增强
  │     └─> StandardWorkingHoursStrategy.calculate()
  │         
  │         【输入参数】
  │         - punchRecords: [08:55 IN, 18:10 OUT]
  │         - schedule: {09:00-18:00, 休息12:00-13:00}
  │         - rule: {宽限15分钟}
  │         
  │         【计算逻辑】
  │         a) 上班考勤:
  │            - 打卡时间: 08:55
  │            - 要求时间: 09:00
  │            - 差值: -5分钟（提前5分钟）
  │            └─> ✅ 正常
  │         
  │         b) 下班考勤:
  │            - 打卡时间: 18:10
  │            - 要求时间: 18:00
  │            - 差值: +10分钟
  │            └─> ✅ 正常（加班10分钟，不足30分钟不计）
  │         
  │         c) 工时计算:
  │            - 实际在岗: 08:55 ~ 18:10 = 555分钟
  │            - 扣除休息: 555 - 60(午休) = 495分钟
  │            - 要求工时: 480分钟
  │            - 实际工时: 495分钟 ✅
  │            - 加班工时: 15分钟（不足30分钟，不计入加班）
  │
  ├─> 5. 特殊场景处理 ⭐ 新增
  │     └─> 场景A: 弹性工时制
  │         - 查询是否弹性制度
  │         - 只要满足每日8小时即可
  │         - 不计迟到早退
  │     
  │     └─> 场景B: 轮班制
  │         - 根据排班动态调整标准时间
  │         - 夜班: 22:00-次日06:00
  │         - 跨日期计算工时
  │     
  │     └─> 场景C: 外勤打卡
  │         - 外勤人员无固定打卡地点
  │         - GPS定位验证
  │         - 只计工时不计迟到
  │
  ├─> 6. 生成考勤结果
  │     └─> AttendanceRecordEntity
  │         - userId: 10001
  │         - date: 2025-06-15
  │         - scheduleId: 10001 ⭐ 关联排班
  │         - scheduledWorkMinutes: 480 ⭐ 应出勤
  │         - actualWorkMinutes: 495 ⭐ 实际出勤
  │         - status: NORMAL
  │         - lateMinutes: 0
  │         - earlyLeaveMinutes: 0
  │         - overtimeMinutes: 0（15分钟不足30分钟）
  │         - absenceMinutes: 0
  │
  ├─> 7. 月度统计汇总 ⭐ 新增
  │     └─> 每月1日，汇总上月数据
  │         - 出勤天数 vs 应出勤天数
  │         - 总工时 vs 标准工时
  │         - 迟到次数、早退次数
  │         - 加班总时长、请假总时长
  │         - 生成月度考勤报表
  │
  └─> 8. 异常处理与通知
        └─> if (status != NORMAL) {
                - 迟到 → 推送通知给本人+部门主管
                - 早退 → 推送通知 + 扣除绩效
                - 缺卡 → 推送通知 + 提醒补卡
                - 旷工 → 推送通知 + 触发审批流程
            }
```

#### **关键设计要点**

**✅ 优势**:
- **设备端轻量**: 只负责识别，不需要存储复杂规则
- **规则灵活**: 考勤规则变更无需更新设备
- **事后审计**: 可以重新计算历史考勤数据
- **排班联动**: ⭐ 结合排班计划，支持多班次、轮班制
- **多维度计算**: ⭐ 综合打卡记录+排班+规则，精准计算工时

**⚠️ 注意**:
- **非实时反馈**: 打卡时不告知迟到/正常（可通过WebSocket实时推送）
- **规则复杂**: 需要支持多种考勤制度（标准/弹性/轮班/外勤）
- **跨日计算**: 夜班跨日期需要特殊处理

**⭐ 排班与考勤关系**:
```
排班计划 (Schedule)
  ↓
  定义: 谁、何时、在哪里、工作多久
  ↓
打卡记录 (PunchRecord)
  ↓
  提供: 实际打卡时间
  ↓
考勤规则 (Rule)
  ↓
  定义: 迟到标准、加班计算、异常判断
  ↓
考勤结果 (Record)
  ↓
  生成: 正常/迟到/早退/旷工 + 工时统计
```

**🔧 实现示例**:

```java
/**
 * 考勤规则引擎（策略模式 + 排班联动）⭐ 增强版
 */
@Service
public class AttendanceCalculationService {
    
    @Resource
    private StrategyFactory<IAttendanceRuleStrategy> strategyFactory;
    
    @Resource
    private AttendanceScheduleDao scheduleDao;
    
    @Resource
    private AttendancePunchRecordDao punchRecordDao;
    
    @Resource
    private AttendanceRuleDao ruleDao;
    
    @Resource
    private AttendanceRecordDao recordDao;
    
    /**
     * 计算每日考勤（定时任务）
     */
    @Scheduled(cron = "0 0 2 * * ?")  // 每天凌晨2点
    public void calculateDailyAttendance() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        
        // 1. 查询昨天所有排班计划 ⭐ 从排班开始
        List<AttendanceScheduleEntity> schedules = 
            scheduleDao.selectByDate(yesterday);
        
        // 2. 逐个排班计算考勤
        for (AttendanceScheduleEntity schedule : schedules) {
            calculateAttendanceBySchedule(schedule);
        }
        
        // 3. 处理无排班但有打卡的情况（补卡/外勤）
        handleUnscheduledPunches(yesterday);
    }
    
    /**
     * 基于排班计算考勤 ⭐ 核心方法
     */
    private void calculateAttendanceBySchedule(AttendanceScheduleEntity schedule) {
        Long userId = schedule.getUserId();
        LocalDate date = schedule.getScheduleDate();
        
        // 1. 查询该用户当天的打卡记录
        List<AttendancePunchRecordEntity> punchRecords = 
            punchRecordDao.selectByUserIdAndDate(userId, date);
        
        // 2. 查询考勤规则
        AttendanceRuleEntity rule = ruleDao.selectByRuleId(
            schedule.getRuleId()
        );
        
        // 3. 选择策略
        IAttendanceRuleStrategy strategy = strategyFactory.get(
            schedule.getShiftType()  // ⭐ 根据班次类型选择策略
        );
        
        // 4. 计算结果（三要素：打卡+排班+规则+异常记录+加班数据等）⭐
        AttendanceCalculationContext context = AttendanceCalculationContext.builder()
            .punchRecords(punchRecords)
            .schedule(schedule)  // ⭐ 传入排班
            .rule(rule)
            .build();
        
        AttendanceResult result = strategy.calculate(context);
        
        // 5. 保存考勤结果
        AttendanceRecordEntity record = new AttendanceRecordEntity();
        record.setUserId(userId);
        record.setDate(date);
        record.setScheduleId(schedule.getScheduleId());  // ⭐ 关联排班
        record.setScheduledWorkMinutes(schedule.getRequiredWorkMinutes());  // ⭐ 应出勤
        record.setActualWorkMinutes(result.getActualWorkMinutes());  // ⭐ 实际出勤
        record.setStatus(result.getStatus());
        record.setLateMinutes(result.getLateMinutes());
        record.setEarlyLeaveMinutes(result.getEarlyLeaveMinutes());
        record.setOvertimeMinutes(result.getOvertimeMinutes());
        record.setAbsenceMinutes(result.getAbsenceMinutes());
        recordDao.insert(record);
        
        // 6. 异常通知
        if (result.getStatus() != AttendanceStatus.NORMAL) {
            notifyAbnormalAttendance(userId, result, schedule);
        }
    }
    
    /**
     * 计算实际工时（扣除休息时间）⭐ 新增
     */
    private int calculateActualWorkMinutes(
            LocalDateTime clockIn, 
            LocalDateTime clockOut,
            AttendanceScheduleEntity schedule) {
        
        // 1. 总在岗时间
        long totalMinutes = ChronoUnit.MINUTES.between(clockIn, clockOut);
        
        // 2. 扣除休息时间
        if (schedule.getBreakStartTime() != null) {
            LocalDateTime breakStart = LocalDateTime.of(
                schedule.getScheduleDate(), 
                schedule.getBreakStartTime()
            );
            LocalDateTime breakEnd = LocalDateTime.of(
                schedule.getScheduleDate(), 
                schedule.getBreakEndTime()
            );
            
            // 判断是否在休息时段工作
            if (clockIn.isBefore(breakEnd) && clockOut.isAfter(breakStart)) {
                long breakMinutes = ChronoUnit.MINUTES.between(
                    breakStart.isAfter(clockIn) ? breakStart : clockIn,
                    breakEnd.isBefore(clockOut) ? breakEnd : clockOut
                );
                totalMinutes -= breakMinutes;
            }
        }
        
        return (int) totalMinutes;
    }
    
    /**
     * 月度考勤统计 ⭐ 新增
     */
    @Scheduled(cron = "0 0 3 1 * ?")  // 每月1日凌晨3点
    public void calculateMonthlyAttendance() {
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        LocalDate startDate = lastMonth.withDayOfMonth(1);
        LocalDate endDate = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth());
        
        // 查询上月所有员工
        List<Long> userIds = recordDao.selectDistinctUserIds(startDate, endDate);
        
        for (Long userId : userIds) {
            // 统计该员工上月考勤
            List<AttendanceRecordEntity> monthRecords = 
                recordDao.selectByUserIdAndDateRange(userId, startDate, endDate);
            
            AttendanceMonthlyStatisticsEntity statistics = 
                new AttendanceMonthlyStatisticsEntity();
            statistics.setUserId(userId);
            statistics.setStatisticsMonth(lastMonth);
            statistics.setScheduledDays(monthRecords.size());
            statistics.setActualDays((int) monthRecords.stream()
                .filter(r -> r.getStatus() != AttendanceStatus.ABSENCE)
                .count());
            statistics.setTotalScheduledMinutes(monthRecords.stream()
                .mapToInt(AttendanceRecordEntity::getScheduledWorkMinutes)
                .sum());
            statistics.setTotalActualMinutes(monthRecords.stream()
                .mapToInt(AttendanceRecordEntity::getActualWorkMinutes)
                .sum());
            statistics.setTotalLateCount((int) monthRecords.stream()
                .filter(r -> r.getLateMinutes() > 0)
                .count());
            statistics.setTotalEarlyLeaveCount((int) monthRecords.stream()
                .filter(r -> r.getEarlyLeaveMinutes() > 0)
                .count());
            statistics.setTotalOvertimeMinutes(monthRecords.stream()
                .mapToInt(AttendanceRecordEntity::getOvertimeMinutes)
                .sum());
            
            monthlyStatisticsDao.insert(statistics);
        }
    }
}
```

---

### **模式4: 访客设备 - 混合验证模式**

#### **交互流程**

```
┌─────────────────────────────────────────────────────────────────────┐
│  场景: 访客现场签到                                                   │
└─────────────────────────────────────────────────────────────────────┘

【预约阶段】（软件端）
visitor-service
  │
  ├─> 1. 访客提交预约
  │     └─> VisitorAppointmentEntity
  │         - visitorName: 李四
  │         - visitDate: 2025-06-15
  │         - allowedAreas: [1号楼大厅]
  │
  ├─> 2. 审批通过
  │
  ├─> 3. 生成通行证
  │     └─> QR Code: VIS20250615001
  │
  ├─> 4. 下发访客信息到设备
  │     └─> device-comm-service.syncVisitor()
  │         ├─ 访客姓名
  │         ├─ 通行证二维码
  │         ├─ 允许区域
  │         ├─ 有效时间
  │         └─ 可选: 人脸照片
  │
  └─> ✅ 下发完成

【签到阶段】（设备端+软件端）
访客设备 (边缘端)
  │
  ├─> 1. 扫描二维码
  │     └─> 读取: VIS20250615001
  │
  ├─> 2. 本地验证（快速检查）
  │     └─> 检查本地访客列表
  │         └─> ✅ 找到匹配记录
  │
  ├─> 3. 可选: 人脸验证
  │     └─> 采集人脸 vs 预约照片
  │         └─> ✅ 相似度 > 0.7
  │
  ├─> 4. 请求软件确认
  │     └─> HTTP POST to visitor-service
  │         {
  │           "qrCode": "VIS20250615001",
  │           "deviceId": "VISITOR-GATE-01",
  │           "checkInTime": "2025-06-15 14:00:00",
  │           "faceVerified": true
  │         }
  │
  └─> ⏳ 等待软件返回...

软件端 (visitor-service)
  │
  ├─> 1. 接收签到请求
  │
  ├─> 2. 验证预约状态
  │     └─> SELECT * FROM visitor_appointment
  │         WHERE qrCode='VIS20250615001'
  │         └─> status=APPROVED ✅
  │
  ├─> 3. 验证时间范围
  │     └─> visitDate='2025-06-15' ✅
  │
  ├─> 4. 创建签到记录
  │     └─> VisitorCheckInRecord.insert()
  │
  ├─> 5. 更新预约状态
  │     └─> status=CHECKED_IN
  │
  ├─> 6. 返回结果
  │     └─> HTTP 200 OK
  │         {
  │           "success": true,
  │           "visitorName": "李四",
  │           "allowedAreas": ["1号楼大厅"],
  │           "validUntil": "18:00"
  │         }
  │
  ├─> 7. 开始轨迹追踪
  │     └─> Redis存储访客实时位置
  │
  └─> 8. 通知被访人
        └─> 短信: "您的访客李四已到达"

访客设备
  │
  ├─> 接收成功响应
  │
  ├─> 打印访客凭证
  │     └─> 访客: 李四
  │         访问区域: 1号楼大厅
  │         有效时间: 至18:00
  │
  └─> 开门放行
```

#### **关键设计要点**

**✅ 混合设计的优势**:
- **离线应急**: 设备本地有访客列表，网络故障时可降级使用
- **双重验证**: 二维码+人脸，安全性高
- **实时追踪**: 软件端实时记录访客位置

---

### **模式5: 视频设备 - 边缘AI计算模式**

#### **交互流程**

```
┌─────────────────────────────────────────────────────────────────────┐
│  场景: 视频监控AI人脸识别                                             │
└─────────────────────────────────────────────────────────────────────┘

【初始化阶段】
软件端 (video-service + biometric-service)
  │
  ├─> 1. 准备人员库
  │     └─> 所有在职员工+访客的人脸模板
  │
  ├─> 2. 下发人员库到边缘设备
  │     └─> device-comm-service.syncToVideoDevice()
  │         ├─ 人员ID
  │         ├─ 姓名
  │         ├─ 人脸特征向量
  │         └─ 人员类型(员工/访客)
  │
  └─> ✅ 边缘设备加载人员库

【实时分析阶段】
视频设备 (边缘AI盒子)
  │
  ├─> 1. 实时视频流
  │     └─> 每秒25帧
  │
  ├─> 2. 边缘AI检测
  │     └─> 每帧检测人脸
  │         └─> 检测到2个人脸
  │
  ├─> 3. 特征提取
  │     └─> 提取512维向量
  │
  ├─> 4. 本地识别
  │     └─> 与本地人员库比对
  │         ├─ 人脸1: userId=10001, 张三, 相似度=0.93
  │         └─ 人脸2: 未识别 (陌生人)
  │
  ├─> 5. 生成分析结果
  │     └─> VideoAnalysisResult
  │         - deviceId: CAM-301
  │         - timestamp: 14:30:25.123
  │         - detectedFaces: 2
  │         - identifiedPersons: [
  │             {userId: 10001, name: "张三", score: 0.93},
  │             {userId: null, name: "陌生人", score: 0.0}
  │           ]
  │         - snapshot: base64图片
  │
  └─> 6. 上传结果（仅上传关键事件）
        └─> HTTP POST to video-service
            - 员工出现: 每小时上传一次
            - 陌生人出现: 立即上传 ⚠️
            - 人员聚集(>5人): 立即上传 ⚠️

软件端 (video-service)
  │
  ├─> 1. 接收AI分析结果
  │
  ├─> 2. 陌生人告警
  │     └─> if (陌生人 && 非公共区域) {
  │             发送告警到安保部门
  │             WebSocket推送到监控大屏
  │             触发该摄像头高清录像
  │         }
  │
  ├─> 3. 人员轨迹分析
  │     └─> 更新人员位置
  │         - 张三: CAM-301 (3楼会议室)
  │         - 存入Redis: "person:track:10001"
  │
  ├─> 4. 异常行为分析
  │     └─> 长时间逗留检测
  │         工作时间外出现检测
  │         频繁进出检测
  │
  └─> 5. 数据存储
        └─> VideoAnalysisRecordEntity.insert()
```

#### **关键设计要点**

**✅ 边缘计算的优势**:
- **实时性**: 毫秒级识别，不依赖网络
- **带宽节省**: 只上传结果，不上传原始视频
- **隐私保护**: 敏感视频不离开现场

**🧠 AI模型部署**:
```
边缘设备硬件:
- NVIDIA Jetson Xavier NX (8GB)
- 支持TensorRT加速
- 处理能力: 25fps实时识别

AI模型:
- 人脸检测: MTCNN
- 特征提取: FaceNet (512维)
- 模型大小: 100MB
- 推理时间: 20ms/帧
```

---

## 🏗️ **统一的设备通讯服务架构**

基于5种交互模式，`device-comm-service`需要统一处理：

```java
/**
 * 设备通讯服务 - 统一协议适配
 */
@Service
public class DeviceProtocolDispatcher {
    
    @Resource
    private Map<String, IDeviceProtocolHandler> protocolHandlers;
    
    /**
     * 根据设备类型分发消息
     */
    public void dispatchDeviceMessage(DeviceMessage message) {
        String deviceType = message.getDeviceType();
        
        IDeviceProtocolHandler handler = protocolHandlers.get(deviceType);
        if (handler == null) {
            throw new UnsupportedDeviceException(deviceType);
        }
        
        handler.handle(message);
    }
}

/**
 * 门禁协议处理器
 */
@Component("ACCESS")
public class AccessProtocolHandler implements IDeviceProtocolHandler {
    
    @Resource
    private AccessServiceClient accessServiceClient;
    
    @Override
    public void handle(DeviceMessage message) {
        if (message.getMessageType() == MessageType.ACCESS_RECORD_UPLOAD) {
            // 设备上传通行记录
            AccessRecordUpload upload = message.parseBody(AccessRecordUpload.class);
            
            // 转发到access-service
            accessServiceClient.saveAccessRecord(upload);
        }
        else if (message.getMessageType() == MessageType.HEARTBEAT) {
            // 心跳处理
            updateDeviceOnlineStatus(message.getDeviceId());
        }
    }
}

/**
 * 消费协议处理器
 */
@Component("CONSUME")
public class ConsumeProtocolHandler implements IDeviceProtocolHandler {
    
    @Resource
    private ConsumeServiceClient consumeServiceClient;
    
    @Override
    public void handle(DeviceMessage message) {
        if (message.getMessageType() == MessageType.CONSUME_REQUEST) {
            // 设备请求消费验证（实时）
            ConsumeRequest request = message.parseBody(ConsumeRequest.class);
            
            // 调用consume-service验证
            ResponseDTO<ConsumeResult> result = 
                consumeServiceClient.verifyConsume(request);
            
            // 立即返回结果给设备
            sendResponseToDevice(message.getDeviceId(), result);
        }
    }
}

/**
 * 考勤协议处理器
 */
@Component("ATTENDANCE")
public class AttendanceProtocolHandler implements IDeviceProtocolHandler {
    
    @Resource
    private AttendanceServiceClient attendanceServiceClient;
    
    @Override
    public void handle(DeviceMessage message) {
        if (message.getMessageType() == MessageType.PUNCH_RECORD_UPLOAD) {
            // 设备上传打卡记录
            PunchRecordUpload upload = message.parseBody(PunchRecordUpload.class);
            
            // 转发到attendance-service
            attendanceServiceClient.savePunchRecord(upload);
        }
    }
}

/**
 * 访客协议处理器
 */
@Component("VISITOR")
public class VisitorProtocolHandler implements IDeviceProtocolHandler {
    
    @Resource
    private VisitorServiceClient visitorServiceClient;
    
    @Override
    public void handle(DeviceMessage message) {
        if (message.getMessageType() == MessageType.VISITOR_CHECKIN_REQUEST) {
            // 设备请求访客签到验证
            VisitorCheckInRequest request = 
                message.parseBody(VisitorCheckInRequest.class);
            
            // 调用visitor-service验证
            ResponseDTO<VisitorCheckInResult> result = 
                visitorServiceClient.verifyCheckIn(request);
            
            // 返回结果
            sendResponseToDevice(message.getDeviceId(), result);
        }
    }
}

/**
 * 视频协议处理器
 */
@Component("VIDEO")
public class VideoProtocolHandler implements IDeviceProtocolHandler {
    
    @Resource
    private VideoServiceClient videoServiceClient;
    
    @Override
    public void handle(DeviceMessage message) {
        if (message.getMessageType() == MessageType.AI_ANALYSIS_RESULT) {
            // 设备上传AI分析结果
            AIAnalysisResult result = message.parseBody(AIAnalysisResult.class);
            
            // 转发到video-service
            videoServiceClient.saveAnalysisResult(result);
        }
    }
}
```

---

## 📊 **数据同步策略总结**

| 数据类型 | 同步方向 | 同步时机 | 实时性 | 备注 |
|---------|---------|---------|-------|------|
| **人员模板** | 软件→设备 | 人员入职/离职时 | 实时推送 | 所有设备类型 |
| **门禁权限** | 软件→设备 | 权限变更时 | 实时推送 | 仅门禁设备 |
| **账户余额** | 软件→设备 | ❌ 不同步 | - | 消费设备不存余额 |
| **考勤规则** | 仅软件端 | - | - | 设备不存规则 |
| **访客信息** | 软件→设备 | 预约审批通过时 | 实时推送 | 访客设备 |
| **视频人员库** | 软件→设备 | 每日凌晨 | 批量同步 | 视频AI设备 |
| **通行记录** | 设备→软件 | 批量上传(每分钟) | 准实时 | 门禁设备 |
| **消费请求** | 设备→软件 | 实时请求 | 实时同步 | 消费设备 |
| **打卡记录** | 设备→软件 | 立即上传 | 实时同步 | 考勤设备 |
| **AI分析结果** | 设备→软件 | 事件驱动上传 | 实时同步 | 视频设备 |

---

## ✅ **总结：架构设计原则**

基于5种设备交互模式，我们的架构设计遵循：

1. **边缘智能优先**: 能在设备端完成的不上传云端
2. **数据安全第一**: 敏感数据(余额)不下发设备
3. **离线能力保障**: 关键场景(门禁)支持离线工作
4. **实时性平衡**: 根据业务需求选择同步/异步
5. **带宽优化**: 只传必要数据，批量传输非紧急数据

---

**文档版本**: v3.1.0  
**最后更新**: 2025-12-18  
**审核状态**: ✅ 已完成  
**下一步**: 更新所有微服务文档以反映真实设备交互模式
