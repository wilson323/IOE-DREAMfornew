# 依赖问题和警告修复进度报告

## 已完成的修复

### 1. 废弃方法修复 ✅
- **文件**: `AttendanceRecordRepository.java`
- **修复**: 将 `deleteBatchIds()` 替换为 `removeByIds()`
- **位置**: 
  - Line 184-186: `batchDelete()` 方法
  - Line 639-641: `batchDeleteByIds()` 方法

### 2. 修复计划文档 ✅
- 创建了 `DEPENDENCY_AND_WARNING_FIX_PLAN.md` 修复计划文档

## 待修复问题分类

### 高优先级问题（影响编译）

#### 1. 依赖问题 - `net.lab1024.sa.common` 无法解析
**影响范围**: 几乎所有 Controller、Service、Entity 类
**解决方案**:
```powershell
# 步骤1: 构建 common 模块
cd D:\IOE-DREAM\microservices\microservices-common
mvn clean install -DskipTests

# 步骤2: 刷新 attendance-service 项目依赖
cd D:\IOE-DREAM\microservices\ioedream-attendance-service
mvn clean compile
```

**受影响的文件（部分）**:
- 所有 Controller 类（`AttendanceController.java`, `AttendanceMobileController.java` 等）
- 所有 Service 接口和实现类
- 所有 Entity 类（继承 BaseEntity 的类）
- Manager 类

#### 2. BaseEntity 方法问题
**问题**: 实体类继承 BaseEntity 后，某些方法（如 `setCreateTime()`, `setUpdateTime()`, `getDeletedFlag()`）无法识别

**原因分析**:
- BaseEntity 使用了 `@Data` 注解，应该自动生成这些方法
- 可能是 Lombok 配置问题或 IDE 未正确识别

**受影响的文件**:
- `AttendanceSyncService.java`: Line 181, 357-359
- `AttendanceExceptionService.java`: Line 703-705
- `ShiftsServiceImpl.java`: Line 143, 148, 181-182, 214-215
- `AttendanceManager.java`: Line 290, 318-319
- 其他多处使用这些方法的地方

### 中优先级问题（警告）

#### 3. 废弃方法调用
**已修复**: `deleteBatchIds()` ✅

**待修复**:
- `BigDecimal.divide()` 使用 `ROUND_HALF_UP` (Java 9+ 已废弃)
  - 受影响文件:
    - `AttendanceRecordEntity.java`: Line 283
    - `AttendanceScheduleEntity.java`: Line 223-224
    - `AttendanceStatisticsEntity.java`: 多处
    - `AttendanceRuleEngine.java`: Line 375, 408

**修复方法**: 
```java
// 旧代码（已废弃）
bigDecimal.divide(other, 2, BigDecimal.ROUND_HALF_UP);

// 新代码
bigDecimal.divide(other, 2, RoundingMode.HALF_UP);
```

- `Schema.required()` (OpenAPI 废弃方法)
  - 受影响文件:
    - `AttendancePunchDTO.java`: Line 29, 34, 39
    - `AttendanceRecordCreateDTO.java`: Line 29, 37
    - `AttendanceRecordUpdateDTO.java`: Line 28

**修复方法**: 移除 `required()` 调用，使用 `@Schema(required = true)` 或 `@Schema(requiredMode = Schema.RequiredMode.REQUIRED)`

#### 4. 未使用的导入和变量

**未使用的导入**:
- `AttendanceExceptionDao.java`: `java.time.LocalDateTime`
- `ExceptionApplicationsDao.java`: `QueryWrapper`
- `ExceptionApprovalsDao.java`: `QueryWrapper`
- `AttendanceRuleRepository.java`: `LambdaQueryWrapper`
- `AttendanceStatisticsRepository.java`: `LocalDate`
- `AttendanceRuleVO.java`: `@NotBlank`, `@NotNull`
- `MobilePunchService.java`: `@Resource`, `RoundingMode`
- `AttendanceStatisticsService.java`: `@Resource`, `Collectors`
- `ExceptionApprovalWorkflow.java`: `@Resource`

**未使用的字段/变量**:
- `AttendanceServiceSimpleImpl.java`: `DEFAULT_LOCATION_ACCURACY`
- `AttendancePerformanceAnalyzer.java`: `tableStats`
- `AttendanceCustomReportService.java`: `DATE_FORMATTER`
- `AttendanceLocationService.java`: `GPS_ACCURACY_THRESHOLD`
- `AttendancePerformanceMonitorService.java`: `SLOW_QUERY_THRESHOLD_MS`, `FRAGMENTATION_THRESHOLD`, `currentStats`, `slowQueryStats`
- `AttendanceTimeUtil.java`: `TIME_FORMATTER`, `MIN_REST_TIME`
- `ExceptionApplicationVO.java`: `exceptionTypeDesc`, `applicationStatusDesc`
- `AttendanceDataSyncService.java`: `record`, 未使用的私有方法
- `AttendanceMobileService.java`: 多个未使用的私有方法
- `AttendanceExportService.java`: 未使用的私有方法

### 低优先级问题（代码质量）

#### 5. 类型安全问题
- `AttendanceStatisticsManager.java`: Null type safety 警告 (Line 515, 588)
- `AttendanceRuleValidator.java`: Unchecked cast (Line 389)

#### 6. Lombok 注解问题
多个实体类使用了 `@EqualsAndHashCode(callSuper = true)`，但编译器报错说方法必须重写父类方法。这可能是因为：
- Lombok 配置问题
- 或者需要显式添加 `@Override` 注解（但 Lombok 应该自动处理）

## 建议的修复顺序

1. **第一步（必须）**: 构建 common 模块并刷新依赖
   - 这将解决大部分编译错误

2. **第二步**: 修复 BaseEntity 相关问题
   - 检查 Lombok 配置
   - 或者为实体类显式添加方法（如果 Lombok 不工作）

3. **第三步**: 清理未使用的导入和变量
   - 提高代码质量
   - 减少警告数量

4. **第四步**: 修复废弃方法调用
   - 提高代码兼容性
   - 为未来版本迁移做准备

5. **第五步**: 修复类型安全警告
   - 提高代码健壮性

## 快速修复脚本

```powershell
# 1. 构建 common 模块
cd D:\IOE-DREAM\microservices\microservices-common
mvn clean install -DskipTests

# 2. 刷新 attendance-service
cd ..\ioedream-attendance-service
mvn clean compile

# 3. 检查编译错误
mvn compile 2>&1 | Select-String "ERROR"
```

## 预计工作量

- 构建 common 模块: 5-10 分钟
- 修复 BaseEntity 问题: 30-60 分钟
- 清理未使用的导入: 30 分钟
- 修复废弃方法: 1 小时
- 修复类型安全警告: 30 分钟
- **总计**: 约 2.5-3 小时

## 注意事项

1. **必须先构建 common 模块**，否则其他修复无法验证
2. **建议分批修复**，每修复一批后重新编译验证
3. **保留备份**，重要修改前先提交代码
4. **测试验证**，修复后运行单元测试确保功能正常

## 下一步行动

建议先执行以下命令构建 common 模块：
```powershell
cd D:\IOE-DREAM\microservices\microservices-common
mvn clean install -DskipTests
```

然后刷新项目依赖，查看还有哪些错误需要修复。

