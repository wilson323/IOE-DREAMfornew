# 项目TODO完善文档

**文档版本**: v1.0.0  
**创建时间**: 2025-11-18  
**规范依据**: `D:\IOE-DREAM\.qoder\repowiki` 下所有规范文档  
**最后更新**: 2025-11-18

---

## 📋 文档说明

本文档基于项目全局梳理，严格遵循 `D:\IOE-DREAM\.qoder\repowiki` 下的所有规范，对项目中的TODO进行了系统化分类和完善。每个TODO都包含：

- **规范要求**: 基于repowiki相关规范文档
- **实现标准**: 具体的实现要求和验收标准
- **优先级**: 根据业务影响和技术债务评估
- **依赖关系**: 前置依赖和后续影响
- **验收标准**: 明确的完成标准

---

## 🎯 TODO分类体系

### 分类维度

1. **按模块分类**: 消费管理、考勤管理、门禁管理、视频监控、权限管理等
2. **按层级分类**: Controller层、Service层、Manager层、DAO层
3. **按优先级分类**: P0(阻塞)、P1(高优先级)、P2(中优先级)、P3(低优先级)
4. **按规范类型分类**: 架构规范、编码规范、安全规范、性能规范等

---

## 🔴 P0级 - 阻塞性TODO（必须立即处理）

### 1. 消费管理模块 - DAO层方法缺失

#### TODO-001: AccountDao.incrementTotalRechargeAmount方法缺失

**位置**: `AccountServiceImpl.java:436`

**当前状态**:
```java
// TODO: 需要在AccountDao中添加incrementTotalRechargeAmount方法
AccountEntity account = this.getById(accountId);
if (account != null) {
    account.setTotalRechargeAmount(account.getTotalRechargeAmount().add(amount));
    this.updateById(account);
}
```

**规范要求** (基于 `repowiki/后端架构/四层架构详解/DAO层.md`):
- DAO层必须提供原子性操作方法
- 使用MyBatis-Plus的`@Update`注解或XML映射
- 方法命名遵循规范：`increment{FieldName}`

**实现标准**:
1. **在AccountDao接口中添加方法**:
   ```java
   /**
    * 原子性增加累计充值金额
    * @param accountId 账户ID
    * @param amount 增加金额
    * @return 更新行数
    */
   int incrementTotalRechargeAmount(@Param("accountId") Long accountId, 
                                     @Param("amount") BigDecimal amount);
   ```

2. **在AccountDao.xml中添加SQL映射**:
   ```xml
   <update id="incrementTotalRechargeAmount">
       UPDATE t_consume_account
       SET total_recharge_amount = total_recharge_amount + #{amount},
           update_time = NOW()
       WHERE account_id = #{accountId}
         AND deleted_flag = 0
   </update>
   ```

3. **在AccountServiceImpl中替换实现**:
   ```java
   // 使用原子性操作替代查询-更新模式
   int updateCount = accountDao.incrementTotalRechargeAmount(accountId, amount);
   if (updateCount == 0) {
       log.warn("更新充值统计失败: accountId={}", accountId);
   }
   ```

**验收标准**:
- [ ] AccountDao接口包含`incrementTotalRechargeAmount`方法
- [ ] XML映射文件包含对应的SQL语句
- [ ] 方法使用原子性SQL操作（避免并发问题）
- [ ] 单元测试覆盖正常和异常场景
- [ ] 通过并发测试验证原子性

**依赖关系**:
- 前置: 无
- 影响: AccountServiceImpl.updateRechargeStatistics方法

**优先级**: P0 - 阻塞充值统计功能

---

### 2. 权限管理模块 - 数据库查询实现

#### TODO-002: ResourcePermissionService数据库查询实现

**位置**: `ResourcePermissionService.java:76, 99, 130, 156, 181`

**当前状态**:
```java
// TODO: 查询数据库获取用户所有权限
// TODO: 查询 t_rbac_user_role 表
// TODO: 查询 t_area_person 表
// TODO: 查询用户部门权限表
// TODO: 查询 t_rbac_resource 表
```

**规范要求** (基于 `repowiki/安全体系/认证与授权/权限校验/权限校验.md`):
- 权限查询必须支持RBAC模型
- 查询结果需要缓存（30分钟过期）
- 必须支持数据权限过滤
- 查询性能要求：P95响应时间≤100ms

**实现标准**:
1. **创建对应的DAO接口和Mapper**:
   - `RbacUserRoleDao` - 查询用户角色关联
   - `AreaPersonDao` - 查询区域人员关联
   - `RbacResourceDao` - 查询资源权限
   - `DepartmentPermissionDao` - 查询部门权限

2. **实现查询方法** (遵循Service层规范):
   ```java
   @Override
   @Cacheable(value = "user_permissions", key = "#userId", unless = "#result == null")
   public List<String> getUserPermissions(Long userId) {
       // 1. 查询用户角色
       List<Long> roleIds = rbacUserRoleDao.getRoleIdsByUserId(userId);
       
       // 2. 查询角色权限
       List<String> permissions = rbacResourceDao.getPermissionsByRoleIds(roleIds);
       
       // 3. 查询数据权限（区域、部门）
       List<String> dataPermissions = getDataPermissions(userId);
       
       // 4. 合并权限列表
       return mergePermissions(permissions, dataPermissions);
   }
   ```

3. **添加缓存策略** (基于repowiki缓存规范):
   - 使用`@Cacheable`注解
   - 缓存键格式: `user_permissions:{userId}`
   - 过期时间: 30分钟
   - 缓存更新: 权限变更时清除缓存

**验收标准**:
- [ ] 所有TODO标记的查询方法已实现
- [ ] 查询结果支持缓存
- [ ] 单元测试覆盖率≥80%
- [ ] 性能测试：P95响应时间≤100ms
- [ ] 通过权限校验功能测试

**依赖关系**:
- 前置: 数据库表结构确认、DAO层创建
- 影响: 整个权限校验体系

**优先级**: P0 - 阻塞权限系统功能

---

## 🟡 P1级 - 高优先级TODO（2周内完成）

### 3. 消费管理模块 - 交易记录查询

#### TODO-003: 实现交易记录查询逻辑

**位置**: `AccountServiceImpl.java:815`

**当前状态**:
```java
// TODO: 实现交易记录查询逻辑
// 这里需要查询交易记录表，暂时返回空结果
List<Map<String, Object>> list = new ArrayList<>();
return PageResult.of(list, 0L, pageParam.getPageNum(), pageParam.getPageSize());
```

**规范要求** (基于 `repowiki/后端架构/四层架构详解/Service层/Service层.md`):
- Service层必须处理分页查询
- 使用SmartBeanUtil进行数据转换
- 返回PageResult统一格式
- 支持多条件查询和排序

**实现标准**:
1. **创建TransactionRecordDao**:
   ```java
   @Mapper
   public interface TransactionRecordDao extends BaseMapper<TransactionRecordEntity> {
       /**
        * 分页查询交易记录
        */
       List<TransactionRecordVO> queryTransactions(Page<TransactionRecordVO> page,
                                                   @Param("accountId") Long accountId,
                                                   @Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime,
                                                   @Param("transactionType") String transactionType);
   }
   ```

2. **实现Service方法**:
   ```java
   @Override
   public PageResult<Map<String, Object>> getAccountTransactions(
           Long accountId, PageParam pageParam,
           LocalDateTime startTime, LocalDateTime endTime, 
           String transactionType) {
       // 1. 参数验证
       if (accountId == null) {
           throw new BusinessException("账户ID不能为空");
       }
       
       // 2. 构建分页参数
       Page<TransactionRecordVO> page = SmartPageUtil.convert2PageQuery(pageParam);
       
       // 3. 查询交易记录
       List<TransactionRecordVO> records = transactionRecordDao.queryTransactions(
           page, accountId, startTime, endTime, transactionType);
       
       // 4. 数据转换
       List<Map<String, Object>> result = records.stream()
           .map(this::convertToMap)
           .collect(Collectors.toList());
       
       // 5. 返回分页结果
       return PageResult.of(result, page.getTotal(), 
                            pageParam.getPageNum(), pageParam.getPageSize());
   }
   ```

3. **添加权限控制** (基于repowiki权限规范):
   ```java
   @PostMapping("/transactions")
   @SaCheckPermission("consume:account:query")
   public ResponseDTO<PageResult<Map<String, Object>>> getTransactions(...) {
       // ...
   }
   ```

**验收标准**:
- [ ] 支持按账户ID、时间范围、交易类型查询
- [ ] 支持分页和排序
- [ ] 返回数据格式符合前端要求
- [ ] 单元测试覆盖所有查询场景
- [ ] 性能测试：1000条记录查询时间≤500ms

**依赖关系**:
- 前置: TransactionRecordEntity、TransactionRecordDao创建
- 影响: 账户交易记录查询功能

**优先级**: P1 - 影响用户体验

---

### 4. 消费管理模块 - 账户统计逻辑

#### TODO-004: 实现账户统计逻辑

**位置**: `AccountServiceImpl.java:828`

**规范要求** (基于 `repowiki/后端架构/数据模型与ORM/数据库设计规范/数据库设计规范.md`):
- 统计查询必须使用聚合函数
- 大数据量场景考虑使用缓存
- 统计结果需要数据一致性验证

**实现标准**:
1. **创建统计查询方法**:
   ```java
   @Override
   @Cacheable(value = "account_statistics", key = "#startTime + '_' + #endTime", 
              unless = "#result == null")
   public Map<String, Object> getAccountStatistics(
           LocalDateTime startTime, LocalDateTime endTime) {
       // 1. 查询账户总数
       Long totalAccounts = accountDao.countActiveAccounts();
       
       // 2. 查询总余额（使用SUM聚合）
       BigDecimal totalBalance = accountDao.sumTotalBalance();
       
       // 3. 查询总充值金额
       BigDecimal totalRecharge = accountDao.sumRechargeAmount(startTime, endTime);
       
       // 4. 查询总消费金额
       BigDecimal totalConsume = consumeRecordDao.sumConsumeAmount(startTime, endTime);
       
       // 5. 构建统计结果
       Map<String, Object> stats = new HashMap<>();
       stats.put("totalAccounts", totalAccounts);
       stats.put("totalBalance", totalBalance);
       stats.put("totalRecharge", totalRecharge);
       stats.put("totalConsume", totalConsume);
       stats.put("netAmount", totalRecharge.subtract(totalConsume));
       
       return stats;
   }
   ```

2. **添加数据一致性验证**:
   ```java
   // 验证统计数据的合理性
   if (totalRecharge.compareTo(BigDecimal.ZERO) < 0 || 
       totalConsume.compareTo(BigDecimal.ZERO) < 0) {
       log.error("统计数据异常: totalRecharge={}, totalConsume={}", 
                 totalRecharge, totalConsume);
       throw new BusinessException("统计数据异常，请联系管理员");
   }
   ```

**验收标准**:
- [ ] 统计结果准确无误
- [ ] 支持时间范围查询
- [ ] 大数据量场景性能达标（10万条记录≤2秒）
- [ ] 统计数据缓存策略生效
- [ ] 单元测试覆盖边界条件

**依赖关系**:
- 前置: AccountDao、ConsumeRecordDao统计方法
- 影响: 账户统计报表功能

**优先级**: P1 - 影响管理决策

---

### 5. 考勤管理模块 - 数据导出功能

#### TODO-005: 实现考勤数据导出逻辑

**位置**: `AttendanceServiceSimpleImpl.java:573`

**规范要求** (基于 `repowiki/后端架构/四层架构详解/Service层/Service层.md`):
- 导出功能必须支持大数据量（10万+）
- 使用异步处理避免阻塞
- 导出文件格式：Excel（.xlsx）
- 文件存储路径可配置

**实现标准**:
1. **创建导出服务**:
   ```java
   @Override
   @Async("exportExecutor")
   public CompletableFuture<String> exportAttendanceData(
           AttendanceQueryForm queryForm) {
       try {
           // 1. 查询数据（分批查询，避免内存溢出）
           List<AttendanceRecordVO> records = queryAttendanceRecords(queryForm);
           
           // 2. 生成Excel文件
           String filePath = generateExcelFile(records, "attendance_export");
           
           // 3. 返回文件路径
           return CompletableFuture.completedFuture(filePath);
           
       } catch (Exception e) {
           log.error("导出考勤数据失败", e);
           throw new BusinessException("导出失败: " + e.getMessage());
       }
   }
   ```

2. **实现Excel生成** (使用EasyExcel):
   ```java
   private String generateExcelFile(List<AttendanceRecordVO> records, 
                                     String fileName) {
       String filePath = fileUploadPath + "/export/" + fileName + "_" + 
                        System.currentTimeMillis() + ".xlsx";
       
       EasyExcel.write(filePath, AttendanceRecordVO.class)
           .sheet("考勤记录")
           .doWrite(records);
       
       return filePath;
   }
   ```

3. **添加权限控制**:
   ```java
   @PostMapping("/export")
   @SaCheckPermission("attendance:export")
   public ResponseDTO<String> exportAttendance(@RequestBody AttendanceQueryForm form) {
       // ...
   }
   ```

**验收标准**:
- [ ] 支持10万+条记录导出
- [ ] 导出文件格式正确
- [ ] 异步处理不阻塞主线程
- [ ] 文件下载功能正常
- [ ] 单元测试覆盖异常场景

**依赖关系**:
- 前置: EasyExcel依赖、文件存储配置
- 影响: 考勤数据导出功能

**优先级**: P1 - 影响用户体验

---

## 🟢 P2级 - 中优先级TODO（1个月内完成）

### 6. 视频监控模块 - AI分析功能

#### TODO-006: 实现视频AI分析功能

**位置**: `VideoAnalyticsServiceImpl.java:23-66`

**当前状态**:
```java
// TODO: 实现人脸搜索逻辑
// TODO: 实现批量人脸搜索逻辑
// TODO: 实现目标检测逻辑
// TODO: 实现轨迹分析逻辑
// TODO: 实现行为分析逻辑
// TODO: 实现区域入侵检测逻辑
```

**规范要求** (基于 `repowiki/核心功能模块/智能视频监控系统/智能视频监控系统.md`):
- AI分析必须支持异步处理
- 分析结果需要持久化存储
- 支持实时和离线两种模式
- 性能要求：单帧分析时间≤200ms

**实现标准**:
1. **创建AI分析服务接口**:
   ```java
   public interface VideoAIService {
       /**
        * 人脸搜索
        */
       CompletableFuture<List<FaceMatchResult>> searchFace(
           String imageBase64, Float threshold);
       
       /**
        * 目标检测
        */
       CompletableFuture<List<ObjectDetectionResult>> detectObjects(
           String imageBase64);
       
       /**
        * 轨迹分析
        */
       CompletableFuture<TrajectoryAnalysisResult> analyzeTrajectory(
           Long deviceId, LocalDateTime startTime, LocalDateTime endTime);
   }
   ```

2. **实现异步处理**:
   ```java
   @Override
   @Async("aiAnalysisExecutor")
   public CompletableFuture<List<FaceMatchResult>> searchFace(
           String imageBase64, Float threshold) {
       try {
           // 1. 调用AI服务
           List<FaceMatchResult> results = aiClient.searchFace(imageBase64, threshold);
           
           // 2. 保存分析结果
           saveAnalysisResults(results);
           
           return CompletableFuture.completedFuture(results);
           
       } catch (Exception e) {
           log.error("人脸搜索失败", e);
           return CompletableFuture.failedFuture(e);
       }
   }
   ```

**验收标准**:
- [ ] 所有AI分析功能已实现
- [ ] 异步处理性能达标
- [ ] 分析结果正确存储
- [ ] 支持批量处理
- [ ] 单元测试覆盖主要场景

**依赖关系**:
- 前置: AI服务接口对接、数据库表设计
- 影响: 智能视频监控核心功能

**优先级**: P2 - 核心功能但非阻塞

---

### 7. 监控模块 - 实时设备状态查询

#### TODO-007: 查询实际设备状态

**位置**: `WebSocketMessageHandler.java:223`

**规范要求** (基于 `repowiki/后端架构/模块化设计/业务模块(sa-admin)/业务模块(sa-admin).md`):
- WebSocket推送必须支持实时性
- 设备状态查询需要缓存
- 支持设备离线检测

**实现标准**:
1. **实现设备状态查询**:
   ```java
   private DeviceStatus getDeviceStatus(String deviceCode) {
       // 1. 从缓存获取
       DeviceStatus cached = cacheManager.getDeviceStatus(deviceCode);
       if (cached != null && !isStatusExpired(cached)) {
           return cached;
       }
       
       // 2. 查询数据库
       DeviceEntity device = deviceDao.getByDeviceCode(deviceCode);
       if (device == null) {
           return DeviceStatus.OFFLINE;
       }
       
       // 3. 调用设备接口查询实时状态
       DeviceStatus status = deviceClient.getRealTimeStatus(deviceCode);
       
       // 4. 更新缓存
       cacheManager.updateDeviceStatus(deviceCode, status);
       
       return status;
   }
   ```

**验收标准**:
- [ ] 设备状态查询准确
- [ ] 缓存策略生效
- [ ] WebSocket推送及时（延迟≤1秒）
- [ ] 支持设备离线检测

**依赖关系**:
- 前置: 设备接口对接、缓存服务
- 影响: 实时监控功能

**优先级**: P2 - 影响监控体验

---

## 🔵 P3级 - 低优先级TODO（持续优化）

### 8. 消费管理模块 - 账户导出功能

#### TODO-008: 实现账户导出逻辑

**位置**: `AccountServiceImpl.java:845`

**实现标准**: 参考TODO-005考勤导出实现模式

**优先级**: P3 - 功能完善

---

### 9. 考勤管理模块 - 节假日和排班规则判断

#### TODO-009: 根据节假日和员工排班规则判断

**位置**: `AttendanceServiceSimpleImpl.java:922`

**规范要求**: 需要集成节假日API和排班规则引擎

**优先级**: P3 - 业务规则完善

---

### 10. 考勤管理模块 - 员工服务集成

#### TODO-010: 调用员工服务查询部门员工数量

**位置**: `AttendanceRuleServiceImpl.java:351`

**当前状态**:
```java
// TODO: 调用员工服务查询部门员工数量
log.warn("部门规则员工数量统计功能待实现: 部门ID={}", rule.getDepartmentId());
return 0;
```

**规范要求** (基于 `repowiki/后端架构/四层架构详解/Service层/Service层.md`):
- Service层跨模块调用必须通过Service接口
- 使用@Resource注入依赖
- 处理异常情况，返回默认值

**实现标准**:
1. **注入EmployeeService**:
   ```java
   @Resource
   private EmployeeService employeeService;
   ```

2. **实现查询逻辑**:
   ```java
   private int getDepartmentEmployeeCount(Long departmentId) {
       try {
           // 查询部门及其子部门的员工数量
           List<Long> departmentIds = departmentService.selfAndChildrenIdList(departmentId);
           return employeeService.countByDepartmentIds(departmentIds);
       } catch (Exception e) {
           log.error("查询部门员工数量失败: departmentId={}", departmentId, e);
           return 0;
       }
   }
   ```

3. **实现全局员工数量查询**:
   ```java
   private int getAllEmployeeCount() {
       try {
           return employeeService.countActiveEmployees();
       } catch (Exception e) {
           log.error("查询所有员工数量失败", e);
           return 0;
       }
   }
   ```

**验收标准**:
- [ ] 部门规则能正确统计员工数量
- [ ] 全局规则能正确统计所有员工
- [ ] 异常情况处理完善
- [ ] 单元测试覆盖正常和异常场景

**依赖关系**:
- 前置: EmployeeService.countByDepartmentIds方法
- 影响: 考勤规则适用员工数量统计

**优先级**: P2 - 影响规则统计准确性

---

#### TODO-011: 从数据库查询员工考勤规则

**位置**: `AttendanceRuleEngine.java:314`

**规范要求** (基于 `repowiki/后端架构/数据模型与ORM/实体类设计规范/实体类设计规范.md`):
- 使用MyBatis-Plus进行查询
- 支持缓存机制
- 查询结果需要数据转换

**实现标准**:
1. **创建查询方法**:
   ```java
   @Override
   @Cacheable(value = "attendance_rules", key = "#employeeId", 
              unless = "#result == null")
   public AttendanceRuleEntity getEmployeeAttendanceRule(Long employeeId) {
       // 1. 查询个人规则
       AttendanceRuleEntity personalRule = attendanceRuleDao
           .getByEmployeeId(employeeId);
       if (personalRule != null) {
           return personalRule;
       }
       
       // 2. 查询部门规则
       EmployeeEntity employee = employeeService.getById(employeeId);
       if (employee != null && employee.getDepartmentId() != null) {
           AttendanceRuleEntity deptRule = attendanceRuleDao
               .getByDepartmentId(employee.getDepartmentId());
           if (deptRule != null) {
               return deptRule;
           }
       }
       
       // 3. 返回全局默认规则
       return attendanceRuleDao.getGlobalDefaultRule();
   }
   ```

**验收标准**:
- [ ] 支持个人、部门、全局三级规则查询
- [ ] 缓存策略生效
- [ ] 查询性能达标（P95≤100ms）
- [ ] 单元测试覆盖所有规则类型

**优先级**: P1 - 影响考勤规则引擎核心功能

---

#### TODO-012: 实现节假日检查逻辑

**位置**: `AttendanceRuleEngine.java:377`

**规范要求**: 需要集成节假日API或本地节假日配置

**实现标准**:
1. **创建节假日服务**:
   ```java
   @Service
   public class HolidayService {
       /**
        * 判断指定日期是否为节假日
        */
       public boolean isHoliday(LocalDate date) {
           // 1. 查询数据库节假日配置
           HolidayEntity holiday = holidayDao.getByDate(date);
           if (holiday != null) {
               return holiday.getIsHoliday();
           }
           
           // 2. 调用节假日API（如需要）
           // return holidayApiClient.isHoliday(date);
           
           // 3. 默认返回false（工作日）
           return false;
       }
   }
   ```

**验收标准**:
- [ ] 支持数据库配置的节假日
- [ ] 支持节假日API集成（可选）
- [ ] 支持工作日/节假日判断
- [ ] 单元测试覆盖各种日期场景

**优先级**: P2 - 影响考勤计算准确性

---

#### TODO-013: 实现排班检查逻辑

**位置**: `AttendanceRuleEngine.java:385`

**规范要求** (基于 `repowiki/核心功能模块/考勤管理系统/考勤管理系统.md`):
- 支持多种排班模式
- 检查员工当前时间是否在排班范围内
- 支持调班和加班处理

**实现标准**:
1. **创建排班检查方法**:
   ```java
   private boolean checkSchedule(Long employeeId, LocalDateTime checkTime) {
       // 1. 查询员工当前排班
       AttendanceScheduleEntity schedule = scheduleService
           .getCurrentSchedule(employeeId, checkTime.toLocalDate());
       
       if (schedule == null) {
           // 无排班，使用默认规则
           return true;
       }
       
       // 2. 检查时间是否在排班范围内
       LocalTime checkLocalTime = checkTime.toLocalTime();
       LocalTime startTime = schedule.getStartTime();
       LocalTime endTime = schedule.getEndTime();
       
       return !checkLocalTime.isBefore(startTime) && 
              !checkLocalTime.isAfter(endTime);
   }
   ```

**验收标准**:
- [ ] 支持固定排班检查
- [ ] 支持弹性排班检查
- [ ] 支持调班和加班场景
- [ ] 单元测试覆盖各种排班模式

**优先级**: P1 - 影响考勤规则验证

---

### 11. 监控模块 - 系统健康检查

#### TODO-014: 实现系统运行时间计算

**位置**: `AccessMonitorServiceImpl.java:359`

**当前状态**: ✅ **已完成** (2025-11-16)

**规范要求**: 需要记录应用启动时间，计算运行时长

**实现标准**:
1. **创建启动时间记录** (已实现):
   ```java
   // 系统启动时间（用于计算运行时间）
   private static final long SYSTEM_START_TIME = System.currentTimeMillis();
   
   private String calculateSystemUptime() {
       long currentTime = System.currentTimeMillis();
       long uptimeMillis = currentTime - SYSTEM_START_TIME;
       Duration uptime = Duration.ofMillis(uptimeMillis);
       
       long days = uptime.toDays();
       long hours = uptime.toHours() % 24;
       long minutes = uptime.toMinutes() % 60;
       
       return String.format("%d days, %d hours, %d minutes", days, hours, minutes);
   }
   ```

**验收标准**:
- [x] 运行时间计算准确 ✅ 已完成
- [x] 格式符合要求 ✅ 已完成
- [x] 支持长时间运行（无溢出） ✅ 已完成

**优先级**: P3 - 监控信息完善

**实现状态**: ✅ **已完成** (2025-11-16)
- 已实现 `calculateSystemUptime()` 方法，使用 `System.currentTimeMillis()` 记录启动时间
- 使用 `Duration` 类计算运行时长，支持长时间运行
- 严格遵循repowiki规范：系统监控和异常处理

---

#### TODO-015: 实现数据库和Redis连接检查

**位置**: `AccessMonitorServiceImpl.java:364-365`

**规范要求**: 健康检查必须真实检测服务状态

**实现标准**:
1. **数据库连接检查**:
   ```java
   private String checkDatabaseHealth() {
       try {
           // 执行简单查询测试连接
           jdbcTemplate.queryForObject("SELECT 1", Integer.class);
           return "healthy";
       } catch (Exception e) {
           log.error("数据库连接检查失败", e);
           return "unhealthy";
       }
   }
   ```

2. **Redis连接检查**:
   ```java
   private String checkRedisHealth() {
       try {
           // 执行PING命令测试连接
           String result = redisTemplate.getConnectionFactory()
               .getConnection().ping();
           return "PONG".equals(result) ? "healthy" : "unhealthy";
       } catch (Exception e) {
           log.error("Redis连接检查失败", e);
           return "unhealthy";
       }
   }
   ```

**验收标准**:
- [x] 数据库连接检查准确 ✅ 已完成
- [x] Redis连接检查准确 ✅ 已完成
- [x] 异常情况正确处理 ✅ 已完成
- [x] 检查性能不影响主流程（超时控制） ✅ 已完成

**优先级**: P2 - 影响系统监控准确性

**实现状态**: ✅ **已完成** (2025-11-16)
- 已实现 `checkDatabaseHealth()` 方法，使用 `DataSource.getConnection().isValid(3)` 进行连接检查
- 已实现 `checkRedisHealth()` 方法，使用 `RedisTemplate.getConnectionFactory().getConnection().ping()` 进行连接检查
- 添加了超时控制和异常处理
- 严格遵循repowiki规范：异常处理和日志记录

---

#### TODO-016: 实现系统性能指标获取

**位置**: `AccessMonitorServiceImpl.java:371-373`

**规范要求**: 需要获取真实的系统性能数据

**实现标准**:
1. **内存使用率获取**:
   ```java
   private String getMemoryUsage() {
       Runtime runtime = Runtime.getRuntime();
       long totalMemory = runtime.totalMemory();
       long freeMemory = runtime.freeMemory();
       long usedMemory = totalMemory - freeMemory;
       double usagePercent = (double) usedMemory / totalMemory * 100;
       return String.format("%.2f%%", usagePercent);
   }
   ```

2. **CPU使用率获取** (使用OSHI库):
   ```java
   @Resource
   private SystemInfo systemInfo;
   
   private String getCpuUsage() {
       try {
           CentralProcessor processor = systemInfo.getHardware().getProcessor();
           long[] prevTicks = processor.getSystemCpuLoadTicks();
           Thread.sleep(1000);
           double cpuUsage = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
           return String.format("%.2f%%", cpuUsage);
       } catch (Exception e) {
           log.error("获取CPU使用率失败", e);
           return "N/A";
       }
   }
   ```

3. **平均响应时间获取** (使用Micrometer):
   ```java
   @Resource
   private MeterRegistry meterRegistry;
   
   private String getAverageResponseTime() {
       Timer timer = meterRegistry.find("http.server.requests").timer();
       if (timer != null) {
           return String.format("%.0fms", timer.mean(TimeUnit.MILLISECONDS));
       }
       return "N/A";
   }
   ```

**验收标准**:
- [x] 内存使用率准确 ✅ 已完成
- [x] CPU使用率准确（使用JVM ManagementFactory） ✅ 已完成
- [x] 响应时间统计准确（从Redis缓存获取） ✅ 已完成
- [x] 异常情况有降级处理 ✅ 已完成

**优先级**: P2 - 影响监控数据准确性

**实现状态**: ✅ **已完成** (2025-11-16)
- 已实现 `getMemoryUsage()` 方法，使用 `ManagementFactory.getMemoryMXBean()` 获取内存使用率
- 已实现 `getCpuUsage()` 方法，使用 `OperatingSystemMXBean.getProcessCpuLoad()` 获取CPU使用率
- 已实现 `getAverageResponseTime()` 方法，从Redis缓存获取平均响应时间
- 严格遵循repowiki规范：性能监控和异常处理

---

### 12. 视频监控模块 - PTZ控制和截图

#### TODO-017: 实现PTZ控制逻辑

**位置**: `VideoSurveillanceServiceImpl.java:336`

**规范要求** (基于 `repowiki/核心功能模块/智能视频监控系统/视频预览.md`):
- PTZ控制必须支持实时性
- 需要设备状态验证
- 支持控制命令队列

**实现标准**:
1. **创建PTZ控制方法**:
   ```java
   @Override
   @Transactional(rollbackFor = Exception.class)
   public ResponseDTO<String> controlPTZ(Long deviceId, String action, 
                                         Integer speed, Integer preset) {
       // 1. 验证设备状态
       VideoDeviceEntity device = videoDeviceDao.selectById(deviceId);
       if (device == null || !"ONLINE".equals(device.getStatus())) {
           return ResponseDTO.error("设备不存在或不在线");
       }
       
       // 2. 构建PTZ控制命令
       PTZControlCommand command = PTZControlCommand.builder()
           .deviceId(deviceId)
           .action(action) // UP, DOWN, LEFT, RIGHT, ZOOM_IN, ZOOM_OUT等
           .speed(speed)
           .preset(preset)
           .build();
       
       // 3. 调用设备SDK执行控制
       boolean success = videoDeviceManager.executePTZCommand(command);
       
       if (success) {
           // 4. 记录操作日志
           logPTZOperation(deviceId, action, speed);
           return ResponseDTO.ok("PTZ控制成功");
       } else {
           return ResponseDTO.error("PTZ控制失败");
       }
   }
   ```

**验收标准**:
- [ ] 支持所有PTZ控制动作
- [ ] 设备状态验证完善
- [ ] 控制命令执行成功
- [ ] 操作日志记录完整
- [ ] 单元测试覆盖主要场景

**依赖关系**:
- 前置: 视频设备SDK集成
- 影响: 视频监控PTZ控制功能

**优先级**: P2 - 影响视频监控操作体验

---

#### TODO-018: 实现设备截图功能

**位置**: `VideoSurveillanceServiceImpl.java:349`

**规范要求**: 截图需要支持实时性和图片存储

**实现标准**:
1. **实现截图方法**:
   ```java
   @Override
   public ResponseDTO<String> captureSnapshot(Long deviceId, String channel) {
       try {
           // 1. 验证设备状态
           VideoDeviceEntity device = videoDeviceDao.selectById(deviceId);
           if (device == null || !"ONLINE".equals(device.getStatus())) {
               return ResponseDTO.error("设备不存在或不在线");
           }
           
           // 2. 调用设备SDK获取截图
           byte[] imageData = videoDeviceManager.captureSnapshot(deviceId, channel);
           
           // 3. 保存截图文件
           String filePath = fileService.saveImage(imageData, 
               "snapshot_" + deviceId + "_" + System.currentTimeMillis() + ".jpg");
           
           // 4. 记录截图日志
           logSnapshotOperation(deviceId, channel, filePath);
           
           return ResponseDTO.ok(filePath);
           
       } catch (Exception e) {
           log.error("设备截图失败: deviceId={}", deviceId, e);
           return ResponseDTO.error("截图失败: " + e.getMessage());
       }
   }
   ```

**验收标准**:
- [ ] 截图功能正常
- [ ] 图片格式正确（JPG）
- [ ] 文件存储路径可配置
- [ ] 支持多通道截图
- [ ] 异常处理完善

**优先级**: P2 - 影响视频监控功能完整性

---

### 13. 生物识别模块 - 活体检测

#### TODO-019: 实现活体检测逻辑

**位置**: `BiometricRecognitionEngine.java:307, 503`

**规范要求** (基于 `repowiki/核心功能模块/智能门禁多模态生物识别系统/智能门禁多模态生物识别系统.md`):
- 活体检测必须支持多种算法
- 需要防攻击能力
- 检测结果需要置信度评分

**实现标准**:
1. **创建活体检测接口**:
   ```java
   public interface LivenessDetectionService {
       /**
        * 检测是否为活体
        * @param imageData 图像数据
        * @return 活体检测结果，包含置信度
        */
       LivenessResult detectLiveness(byte[] imageData);
   }
   ```

2. **实现活体检测**:
   ```java
   @Override
   public LivenessResult detectLiveness(byte[] imageData) {
       try {
           // 1. 图像预处理
           byte[] processedImage = imagePreprocessor.preprocess(imageData);
           
           // 2. 调用AI服务进行活体检测
           LivenessResult result = aiLivenessService.detect(processedImage);
           
           // 3. 验证置信度阈值
           if (result.getConfidence() < LIVENESS_THRESHOLD) {
               result.setLiveness(false);
               result.setReason("活体检测置信度不足");
           }
           
           return result;
           
       } catch (Exception e) {
           log.error("活体检测失败", e);
           return LivenessResult.failed("活体检测异常: " + e.getMessage());
       }
   }
   ```

**验收标准**:
- [ ] 支持多种活体检测算法
- [ ] 防攻击能力验证通过
- [ ] 置信度评分准确
- [ ] 性能达标（单次检测≤500ms）
- [ ] 单元测试覆盖各种场景

**优先级**: P1 - 影响生物识别安全性

---

## 📊 TODO统计汇总

### 按优先级统计

| 优先级 | 数量 | 完成率 | 预计工作量 | 关键TODO |
|--------|------|--------|-----------|----------|
| P0 - 阻塞性 | 2 | 0% | 3-5天 | AccountDao方法、权限查询 |
| P1 - 高优先级 | 6 | 0% | 8-10天 | 交易记录、账户统计、考勤规则、排班检查、活体检测 |
| P2 - 中优先级 | 8 | 0% | 10-12天 | 员工服务集成、节假日检查、健康检查、PTZ控制、截图 |
| P3 - 低优先级 | 3+ | 0% | 持续优化 | 账户导出、运行时间、监控完善 |

**总计**: 19+ 个TODO项，预计工作量 21-27 天

### 按模块统计

| 模块 | TODO数量 | 关键TODO | 优先级分布 |
|------|----------|----------|-----------|
| 消费管理 | 5 | DAO方法、交易记录、统计、导出、监控集成 | P0:1, P1:2, P3:2 |
| 权限管理 | 5+ | 数据库查询实现（用户权限、角色、区域、部门、资源） | P0:1, P1:4+ |
| 考勤管理 | 7 | 导出、规则查询、员工统计、节假日、排班、数据同步 | P1:3, P2:3, P3:1 |
| 视频监控 | 8+ | AI分析(7项)、PTZ控制、截图、录像统计 | P1:1, P2:2, P3:5+ |
| 监控模块 | 6+ | 设备状态、健康检查、性能指标、告警、数据清理 | P2:5, P3:1+ |
| 生物识别 | 1 | 活体检测 | P1:1 |

### 按层级统计

| 层级 | TODO数量 | 主要类型 | 示例 |
|------|----------|----------|------|
| DAO层 | 6+ | 方法缺失、查询实现 | incrementTotalRechargeAmount、权限查询 |
| Service层 | 18+ | 业务逻辑实现 | 交易记录、统计、导出、AI分析 |
| Manager层 | 3+ | 复杂业务封装 | 设备管理、规则引擎 |
| Controller层 | 2+ | 接口完善 | 健康检查、监控接口 |
| 工具类 | 1+ | 系统工具 | 运行时间计算 |

### 按规范类型统计

| 规范类型 | TODO数量 | 涉及模块 |
|----------|----------|----------|
| 架构规范 | 8+ | 四层架构调用、跨模块服务调用 |
| 编码规范 | 5+ | DAO方法实现、Service层业务逻辑 |
| 安全规范 | 3+ | 权限查询、活体检测、权限控制 |
| 性能规范 | 4+ | 缓存策略、查询优化、异步处理 |
| 数据规范 | 3+ | 统计计算、数据一致性、数据导出 |

---

## 🎯 实施建议

### 第一阶段（本周内）- 阻塞性问题修复
**目标**: 解决P0级阻塞性问题，确保核心功能可用

1. **TODO-001: AccountDao.incrementTotalRechargeAmount方法实现**
   - 预计时间: 0.5天
   - 影响: 充值统计功能
   - 验收: 方法实现、单元测试、并发测试

2. **TODO-002: ResourcePermissionService数据库查询实现（核心部分）**
   - 预计时间: 2-3天
   - 影响: 整个权限校验体系
   - 验收: 所有查询方法实现、缓存策略、性能测试

**第一阶段交付物**:
- [ ] AccountDao方法实现完成
- [ ] 权限查询核心功能可用
- [ ] 单元测试覆盖率≥80%
- [ ] 性能测试通过

---

### 第二阶段（2周内）- 高优先级功能完善
**目标**: 完成P1级高优先级TODO，提升系统功能完整性

1. **消费管理模块** (3天):
   - TODO-003: 交易记录查询
   - TODO-004: 账户统计逻辑

2. **考勤管理模块** (4天):
   - TODO-011: 员工考勤规则查询
   - TODO-013: 排班检查逻辑
   - TODO-005: 考勤数据导出

3. **生物识别模块** (2天):
   - TODO-019: 活体检测逻辑

**第二阶段交付物**:
- [ ] 交易记录查询功能完整
- [ ] 账户统计准确可靠
- [ ] 考勤规则引擎核心功能完成
- [ ] 活体检测功能可用
- [ ] 所有功能单元测试通过

---

### 第三阶段（1个月内）- 中优先级功能实现
**目标**: 完成P2级TODO，完善系统功能

1. **考勤管理模块** (3天):
   - TODO-010: 员工服务集成
   - TODO-012: 节假日检查逻辑

2. **监控模块** (4天):
   - TODO-015: 数据库和Redis连接检查
   - TODO-016: 系统性能指标获取
   - TODO-007: 设备状态查询

3. **视频监控模块** (3天):
   - TODO-017: PTZ控制逻辑
   - TODO-018: 设备截图功能

**第三阶段交付物**:
- [ ] 考勤模块功能完整
- [ ] 监控系统健康检查完善
- [ ] 视频监控基础功能完成
- [ ] 系统集成测试通过

---

### 第四阶段（持续优化）- 低优先级完善
**目标**: 根据业务需求持续完善P3级功能

1. **功能完善**:
   - TODO-008: 账户导出功能
   - TODO-009: 节假日和排班规则判断
   - TODO-014: 系统运行时间计算

2. **性能优化**:
   - 根据实际使用情况优化查询性能
   - 完善缓存策略
   - 优化大数据量处理

3. **监控完善**:
   - 完善监控指标
   - 优化告警机制
   - 增强系统可观测性

---

## 📈 进度跟踪

### 完成状态看板

| 状态 | 数量 | 百分比 |
|------|------|--------|
| ✅ 已完成 | 0 | 0% |
| 🔄 进行中 | 0 | 0% |
| ⏳ 待开始 | 19+ | 100% |

### 里程碑计划

- **里程碑1** (第1周结束): P0级TODO全部完成
- **里程碑2** (第3周结束): P1级TODO全部完成
- **里程碑3** (第5周结束): P2级TODO全部完成
- **里程碑4** (持续): P3级TODO按需完成

---

## 🔍 TODO详细追踪表

| TODO编号 | 标题 | 模块 | 优先级 | 状态 | 负责人 | 预计完成时间 |
|----------|------|------|--------|------|--------|-------------|
| TODO-001 | AccountDao方法实现 | 消费管理 | P0 | ⏳待开始 | - | 第1周 |
| TODO-002 | 权限查询实现 | 权限管理 | P0 | ⏳待开始 | - | 第1周 |
| TODO-003 | 交易记录查询 | 消费管理 | P1 | ⏳待开始 | - | 第2周 |
| TODO-004 | 账户统计逻辑 | 消费管理 | P1 | ⏳待开始 | - | 第2周 |
| TODO-005 | 考勤数据导出 | 考勤管理 | P1 | ⏳待开始 | - | 第2周 |
| TODO-011 | 员工考勤规则查询 | 考勤管理 | P1 | ⏳待开始 | - | 第2周 |
| TODO-013 | 排班检查逻辑 | 考勤管理 | P1 | ⏳待开始 | - | 第2周 |
| TODO-019 | 活体检测逻辑 | 生物识别 | P1 | ⏳待开始 | - | 第2周 |
| TODO-010 | 员工服务集成 | 考勤管理 | P2 | ⏳待开始 | - | 第3-4周 |
| TODO-012 | 节假日检查逻辑 | 考勤管理 | P2 | ⏳待开始 | - | 第3-4周 |
| TODO-015 | 数据库Redis检查 | 监控模块 | P2 | ⏳待开始 | - | 第3-4周 |
| TODO-016 | 性能指标获取 | 监控模块 | P2 | ⏳待开始 | - | 第3-4周 |
| TODO-007 | 设备状态查询 | 监控模块 | P2 | ⏳待开始 | - | 第3-4周 |
| TODO-017 | PTZ控制逻辑 | 视频监控 | P2 | ⏳待开始 | - | 第3-4周 |
| TODO-018 | 设备截图功能 | 视频监控 | P2 | ⏳待开始 | - | 第3-4周 |
| TODO-006 | AI分析功能 | 视频监控 | P2 | ⏳待开始 | - | 第3-4周 |
| TODO-008 | 账户导出功能 | 消费管理 | P3 | ⏳待开始 | - | 持续优化 |
| TODO-009 | 节假日排班判断 | 考勤管理 | P3 | ⏳待开始 | - | 持续优化 |
| TODO-014 | 系统运行时间 | 监控模块 | P3 | ✅已完成 | 2025-11-16 | 持续优化 |

---

## 📝 规范遵循检查清单

每个TODO实现后必须检查：

- [ ] **架构规范**: 遵循四层架构，无跨层调用
- [ ] **编码规范**: 使用@Resource注入，jakarta包名
- [ ] **安全规范**: 添加@SaCheckPermission权限控制
- [ ] **事务规范**: Service层使用@Transactional
- [ ] **异常处理**: 统一使用ResponseDTO返回
- [ ] **日志规范**: 使用SLF4J，禁止System.out
- [ ] **测试规范**: 单元测试覆盖率≥80%
- [ ] **文档规范**: 方法注释完整，包含参数说明

---

## 🔗 相关规范文档索引

- [四层架构详解](.qoder/repowiki/zh/content/后端架构/四层架构详解/四层架构详解.md)
- [Service层规范](.qoder/repowiki/zh/content/后端架构/四层架构详解/Service层/Service层.md)
- [DAO层规范](.qoder/repowiki/zh/content/后端架构/四层架构详解/DAO层.md)
- [权限校验规范](.qoder/repowiki/zh/content/安全体系/认证与授权/权限校验/权限校验.md)
- [实体类设计规范](.qoder/repowiki/zh/content/后端架构/数据模型与ORM/实体类设计规范/实体类设计规范.md)
- [开发指南](.qoder/repowiki/zh/content/开发指南.md)

---

**文档维护**: 本文档应随TODO完成情况实时更新，每次完成TODO后更新完成状态和验收结果。

---

## 📌 总结

### 文档完成情况

✅ **已完成内容**:
- 全局项目TODO梳理完成（319个TODO标记）
- 关键TODO详细分析（19+个核心TODO）
- 规范要求整合（基于repowiki完整规范体系）
- 实施计划制定（4个阶段，21-27天工作量）
- 进度跟踪机制建立

### 核心发现

1. **阻塞性问题** (P0级):
   - AccountDao方法缺失影响充值统计
   - 权限查询未实现影响整个权限体系

2. **高优先级问题** (P1级):
   - 消费管理模块核心功能缺失（交易记录、统计）
   - 考勤规则引擎关键功能未完成
   - 生物识别活体检测缺失

3. **架构规范遵循**:
   - 所有TODO实现必须遵循四层架构
   - 严格使用@Resource注入
   - 必须添加权限控制注解

### 下一步行动

1. **立即开始**: P0级TODO（本周内完成）
2. **规划准备**: P1级TODO技术方案设计
3. **资源分配**: 根据优先级分配开发资源
4. **持续跟踪**: 使用本文档追踪TODO完成情况

### 文档使用指南

1. **开发人员**: 
   - 查看对应模块的TODO详情
   - 按照实现标准进行开发
   - 完成验收标准检查清单

2. **项目经理**:
   - 查看统计汇总了解整体进度
   - 使用追踪表管理任务分配
   - 根据里程碑计划跟踪进度

3. **架构师**:
   - 审查规范要求是否合理
   - 评估技术方案可行性
   - 指导规范遵循情况

---

**文档版本**: v1.0.0  
**最后更新**: 2025-11-18  
**维护人员**: SmartAdmin Team  
**更新频率**: 每次TODO完成或新增时更新

