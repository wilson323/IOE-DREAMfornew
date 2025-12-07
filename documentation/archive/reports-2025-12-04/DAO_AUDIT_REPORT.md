# DAO接口审计报告

**生成时间**: 2025-12-02  
**扫描范围**: 全部微服务  
**扫描结果**: 约130个DAO接口  
**分析依据**: CLAUDE.md全局统一架构规范

---

## 📊 DAO接口分布统计

### 总体概览

| 位置 | DAO数量 | 状态 | 备注 |
|------|---------|------|------|
| **业务微服务** | 约45个 | ⚠️ 需检查 | 引用的Entity需要更新 |
| **microservices-common** | 约25个 | ✅ 符合规范 | 保持不变 |
| **ioedream-common-core** | 约30个 | ⚠️ 重复 | 需删除 |
| **ioedream-common-service** | 约15个 | ⚠️ 重复 | 需删除 |
| **archive/deprecated-services** | 约15个 | 🗑️ 废弃 | 无需处理 |

### 架构合规性分析

- **需要修复的DAO**: 约45个（引用的Entity将迁移）
- **重复的DAO**: 约45个（需删除）
- **规范遵循率**: 约36%（25/(25+45)）
- **目标遵循率**: 100%

---

## 🚨 P0级：需要修复的DAO接口

### 1. ioedream-access-service的DAO（13个）

**位置**: `microservices-common/src/main/java/net/lab1024/sa/common/access/dao/`

| 序号 | DAO名称 | 当前位置 | 目标位置 | 问题 | 优先级 |
|------|---------|---------|---------|------|--------|
| 1 | AntiPassbackRecordDao | access-service/.../advanced/dao/ | common/access/dao/ | Entity不存在 | P0 |
| 2 | AntiPassbackRuleDao | access-service/.../advanced/dao/ | common/access/dao/ | Entity不存在 | P0 |
| 3 | InterlockLogDao | access-service/.../advanced/dao/ | common/access/dao/ | Entity不存在 | P0 |
| 4 | InterlockRuleDao | access-service/.../advanced/dao/ | common/access/dao/ | Entity不存在 | P0 |
| 5 | LinkageRuleDao | access-service/.../advanced/dao/ | common/access/dao/ | Entity不存在 | P0 |
| 6 | ApprovalProcessDao | access-service/.../approval/dao/ | common/access/dao/ | Entity不存在 | P0 |
| 7 | VisitorReservationDao | access-service/.../approval/dao/ | common/access/dao/ | Entity不存在 | P0 |
| 8 | AntiPassbackDao | access-service/.../dao/ | common/access/dao/ | Entity不存在 | P0 |
| 9 | ApprovalRequestDao | access-service/.../dao/ | common/access/dao/ | Entity不存在 | P0 |
| 10 | AreaPermissionDao | access-service/.../dao/ | common/access/dao/ | Entity不存在 | P0 |
| 11 | DeviceMonitorDao | access-service/.../dao/ | common/access/dao/ | Entity不存在 | P0 |
| 12 | InterlockGroupDao | access-service/.../dao/ | common/access/dao/ | Entity不存在 | P0 |
| 13 | LinkageRuleDao | access-service/.../dao/ | common/access/dao/ | Entity不存在 | P0 |

**修复方案**:
1. 先迁移Entity类到microservices-common
2. 再迁移DAO接口到microservices-common
3. 更新Entity导入路径
4. 验证编译通过

### 2. ioedream-attendance-service的DAO（16个）

**位置**: `microservices-common/src/main/java/net/lab1024/sa/common/attendance/dao/`

| 序号 | DAO名称 | 当前位置 | 目标位置 | 问题 | 优先级 |
|------|---------|---------|---------|------|--------|
| 1 | AttendanceDao | attendance-service/.../dao/ | common/attendance/dao/ | Entity不存在 | P0 |
| 2 | AttendanceExceptionDao | attendance-service/.../dao/ | common/attendance/dao/ | Entity不存在 | P0 |
| 3 | AttendanceRecordDao | attendance-service/.../dao/ | common/attendance/dao/ | Entity不存在 | P0 |
| 4 | AttendanceReportDao | attendance-service/.../dao/ | common/attendance/dao/ | Entity不存在 | P0 |
| 5 | AttendanceRuleDao | attendance-service/.../dao/ | common/attendance/dao/ | Entity不存在 | P0 |
| 6 | AttendanceScheduleDao | attendance-service/.../dao/ | common/attendance/dao/ | Entity不存在 | P0 |
| 7 | AttendanceStatisticsDao | attendance-service/.../dao/ | common/attendance/dao/ | Entity不存在 | P0 |
| 8 | ExceptionApplicationsDao | attendance-service/.../dao/ | common/attendance/dao/ | Entity不存在 | P0 |
| 9 | ExceptionApprovalsDao | attendance-service/.../dao/ | common/attendance/dao/ | Entity不存在 | P0 |
| 10 | LeaveApplicationDao | attendance-service/.../dao/ | common/attendance/dao/ | Entity不存在 | P0 |
| 11 | LeaveTypeDao | attendance-service/.../dao/ | common/attendance/dao/ | Entity不存在 | P0 |
| 12 | OvertimeApplicationDao | attendance-service/.../dao/ | common/attendance/dao/ | Entity不存在 | P0 |
| 13 | RealTimeCalculationEngineDao | attendance-service/.../dao/ | common/attendance/dao/ | Entity不存在 | P0 |
| 14 | ShiftSchedulingDao | attendance-service/.../dao/ | common/attendance/dao/ | Entity不存在 | P0 |
| 15 | ShiftsDao | attendance-service/.../dao/ | common/attendance/dao/ | Entity不存在 | P0 |
| 16 | TimePeriodsDao | attendance-service/.../dao/ | common/attendance/dao/ | Entity不存在 | P0 |

### 3. ioedream-consume-service的DAO（19个）

**位置**: `microservices-common/src/main/java/net/lab1024/sa/common/consume/dao/`

| 序号 | DAO名称 | 当前位置 | 目标位置 | 问题 | 优先级 |
|------|---------|---------|---------|------|--------|
| 1 | AccountDao | consume-service/.../dao/ | common/consume/dao/ | Entity不存在 | P0 |
| 2 | ConsumeAccountDao | consume-service/.../dao/ | common/consume/dao/ | Entity不存在 | P0 |
| 3 | ConsumeAreaDao | consume-service/.../dao/ | common/consume/dao/ | Entity不存在 | P0 |
| 4 | ConsumeAuditLogDao | consume-service/.../dao/ | common/consume/dao/ | Entity不存在 | P0 |
| 5 | ConsumeBarcodeDao | consume-service/.../dao/ | common/consume/dao/ | Entity不存在 | P0 |
| 6 | ConsumeInventoryRecordDao | consume-service/.../dao/ | common/consume/dao/ | Entity不存在 | P0 |
| 7 | ConsumeMealDao | consume-service/.../dao/ | common/consume/dao/ | Entity不存在 | P0 |
| 8 | ConsumePermissionConfigDao | consume-service/.../dao/ | common/consume/dao/ | Entity不存在 | P0 |
| 9 | ConsumeProductCategoryDao | consume-service/.../dao/ | common/consume/dao/ | Entity不存在 | P0 |
| 10 | ConsumeProductDao | consume-service/.../dao/ | common/consume/dao/ | Entity不存在 | P0 |
| 11 | ConsumeRecordDao | consume-service/.../dao/ | common/consume/dao/ | Entity不存在 | P0 |
| 12 | ConsumeReportDao | consume-service/.../dao/ | common/consume/dao/ | Entity不存在 | P0 |
| 13 | ConsumeSubsidyAccountDao | consume-service/.../dao/ | common/consume/dao/ | Entity不存在 | P0 |
| 14 | ConsumeTransactionDao | consume-service/.../dao/ | common/consume/dao/ | Entity不存在 | P0 |
| 15 | DeviceDao | consume-service/.../dao/ | common/consume/dao/ | Entity不存在 | P0 |
| 16 | PaymentRecordDao | consume-service/.../dao/ | common/consume/dao/ | Entity不存在 | P0 |
| 17 | RechargeRecordDao | consume-service/.../dao/ | common/consume/dao/ | Entity不存在 | P0 |
| 18 | RefundRecordDao | consume-service/.../dao/ | common/consume/dao/ | Entity不存在 | P0 |
| 19 | ConsumeReportTemplateDao | consume-service/.../report/dao/ | common/consume/dao/ | Entity不存在 | P0 |

### 4. 其他业务服务的DAO

| 服务 | DAO数量 | 状态 | 备注 |
|------|---------|------|------|
| ioedream-device-comm-service | 2个 | ⚠️ 需修复 | 引用Entity需更新 |
| ioedream-video-service | 7个 | ⚠️ 需修复 | 引用Entity需更新 |
| ioedream-visitor-service | 3个 | ⚠️ 需修复 | 引用Entity需更新 |
| ioedream-oa-service | 4个 | ⚠️ 需修复 | 引用Entity需更新 |

---

## 🎯 DAO标准规范

### DAO接口定义模板

```java
package net.lab1024.sa.common.access.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.common.access.entity.AntiPassbackRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 防回传记录DAO
 * 负责防回传记录的数据库访问操作
 * 
 * @author IOE-DREAM Team
 * @since 2025-12-02
 */
@Mapper
public interface AntiPassbackRecordDao extends BaseMapper<AntiPassbackRecordEntity> {
    
    /**
     * 根据用户ID查询最新的防回传记录
     *
     * @param userId 用户ID
     * @return 防回传记录
     */
    @Transactional(readOnly = true)
    AntiPassbackRecordEntity selectLatestRecord(@Param("userId") Long userId);
    
    /**
     * 分页查询防回传记录
     *
     * @param page 分页参数
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分页结果
     */
    @Transactional(readOnly = true)
    IPage<AntiPassbackRecordEntity> selectPageByTime(
        Page<AntiPassbackRecordEntity> page,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * 批量插入防回传记录
     *
     * @param records 记录列表
     * @return 插入数量
     */
    @Transactional(rollbackFor = Exception.class)
    int insertBatch(@Param("records") List<AntiPassbackRecordEntity> records);
    
    /**
     * 根据规则ID删除记录
     *
     * @param ruleId 规则ID
     * @return 删除数量
     */
    @Transactional(rollbackFor = Exception.class)
    int deleteByRuleId(@Param("ruleId") Long ruleId);
}
```

### DAO使用规范

#### 1. @Mapper注解（强制）
```java
// ✅ 正确
@Mapper
public interface UserDao extends BaseMapper<UserEntity> {
}

// ❌ 错误
@Repository  // 禁止使用
public interface UserDao extends BaseMapper<UserEntity> {
}
```

#### 2. 继承BaseMapper（强制）
```java
// ✅ 正确
public interface UserDao extends BaseMapper<UserEntity> {
}

// ❌ 错误
public interface UserDao {  // 未继承BaseMapper
    List<UserEntity> selectAll();
}
```

#### 3. 事务注解（推荐）
```java
// 查询方法
@Transactional(readOnly = true)
UserEntity selectByLoginName(@Param("loginName") String loginName);

// 写操作方法
@Transactional(rollbackFor = Exception.class)
int updateStatus(@Param("userId") Long userId, @Param("status") Integer status);
```

#### 4. 参数注解（必须）
```java
// ✅ 正确 - 使用@Param注解
UserEntity selectByLoginName(@Param("loginName") String loginName);

// ❌ 错误 - 缺少@Param注解
UserEntity selectByLoginName(String loginName);
```

---

## 📋 DAO迁移执行计划

### 阶段1：准备工作（已完成✅）
- [x] 扫描所有DAO接口
- [x] 识别需要迁移的DAO
- [x] 分析DAO依赖关系
- [x] 生成审计报告文档

### 阶段2：Entity迁移完成后
- [ ] 迁移access-service的13个DAO到microservices-common
- [ ] 迁移attendance-service的16个DAO到microservices-common
- [ ] 迁移consume-service的19个DAO到microservices-common
- [ ] 迁移其他服务的DAO到microservices-common

### 阶段3：更新DAO接口
- [ ] 更新所有DAO的Entity引用路径
- [ ] 确保所有DAO继承BaseMapper
- [ ] 添加@Mapper注解（如果缺失）
- [ ] 添加事务注解
- [ ] 添加完整的JavaDoc注释

### 阶段4：删除重复DAO
- [ ] 删除ioedream-common-core中的30个重复DAO
- [ ] 删除ioedream-common-service中的15个重复DAO
- [ ] 验证编译通过

### 阶段5：创建缺失的DAO
- [ ] 根据Entity创建缺失的DAO接口
- [ ] 实现必要的查询方法
- [ ] 添加完整注释

---

## ⚠️ 注意事项

### 1. DAO迁移原则
- **跟随Entity迁移**: DAO必须在Entity迁移后再迁移
- **保持功能完整**: 不修改DAO方法定义
- **验证编译**: 每次迁移后立即验证
- **更新引用**: 确保所有Service/Manager都已更新导入

### 2. DAO包结构规范
```
microservices-common/src/main/java/net/lab1024/sa/common/
├── access/dao/          # 门禁相关DAO
├── attendance/dao/      # 考勤相关DAO
├── consume/dao/         # 消费相关DAO
├── visitor/dao/         # 访客相关DAO
├── video/dao/          # 视频相关DAO
└── organization/dao/   # 组织架构DAO（已存在）
```

### 3. 重复DAO处理
- **对比版本**: 检查ioedream-common-core和microservices-common中的DAO
- **选择保留**: 选择功能更完整的版本
- **统一路径**: 所有DAO统一在microservices-common中

### 4. 验证检查清单
- [ ] 所有DAO都使用@Mapper注解
- [ ] 所有DAO都继承BaseMapper
- [ ] 所有DAO都有完整的JavaDoc注释
- [ ] 所有查询方法都有@Transactional(readOnly = true)
- [ ] 所有写操作方法都有@Transactional(rollbackFor = Exception.class)
- [ ] 所有方法参数都有@Param注解
- [ ] 所有导入路径都已更新
- [ ] 编译通过，无错误
- [ ] 无重复DAO定义

---

## 📊 预期效果

### 修复前
- DAO分布混乱：45个DAO在业务服务中
- 重复DAO：45个重复DAO在common-core和common-service中
- Entity引用错误：约100个导入错误
- 架构合规率：36%

### 修复后
- DAO统一管理：所有公共DAO在microservices-common中
- 无重复DAO：删除所有重复定义
- Entity引用正确：所有导入路径正确
- 架构合规率：100%
- 包结构清晰规范，易于维护
- 消除约100个DAO相关编译错误

---

**文档生成时间**: 2025-12-02  
**下次更新**: 完成迁移后  
**维护责任人**: IOE-DREAM架构委员会

