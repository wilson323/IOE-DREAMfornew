# Requirements Document

## Introduction

基于IOE-DREAM项目深度代码梳理发现：考勤模块存在Service层完整但Controller层不完整的问题。Service层提供了完整的管理功能（班次管理、规则配置、排班管理等），但缺少对应的Controller API接口，导致这些功能无法通过HTTP接口调用。

本需求的核心是创建Controller层桥接现有的Service功能，实现完整的管理API，而不是重复开发业务逻辑。这是一个典型的"基于现有增强和完善"的场景。

## Alignment with Product Vision

本功能完善支持IOE-DREAM智能管理平台的"功能完整性"目标，通过补充缺失的Controller API，充分发挥现有Service层的完整功能。

1. **功能完整性**: 让现有的Service功能能够通过API接口访问
2. **开发效率**: 100%复用现有业务逻辑，只添加Controller层
3. **用户体验**: 为前端提供完整的管理接口支持
4. **系统价值**: 释放现有64个Java文件的全部功能潜力

## Requirements

### Requirement 1 - 考勤规则管理Controller

**User Story:** 作为HR管理员，我希望通过API接口管理考勤规则，以便利用现有的AttendanceRuleService功能。

#### Acceptance Criteria

1. WHEN 创建考勤规则 THEN 系统 SHALL 创建AttendanceRuleController调用AttendanceRuleService.saveOrUpdateRule()
2. WHEN 查询考勤规则列表 THEN 系统 SHALL 提供GET /api/attendance/rules调用AttendanceRuleService.getAllValidRules()
3. WHEN 更新考勤规则 THEN 系统 SHALL 调用AttendanceRuleService.saveOrUpdateRule()
4. WHEN 删除考勤规则 THEN 系统 SHALL 调用AttendanceRuleService.deleteRule()
5. WHEN 批量删除考勤规则 THEN 系统 SHALL 调用AttendanceRuleService.batchDeleteRules()

### Requirement 2 - 排班管理Controller

**User Story:** 作为排班管理员，我希望通过API接口进行排班管理，以便利用现有的AttendanceScheduleService完整功能。

#### Acceptance Criteria

1. WHEN 创建排班记录 THEN 系统 SHALL 创建AttendanceScheduleController调用AttendanceScheduleService.saveOrUpdateSchedule()
2. WHEN 查询员工排班 THEN 系统 SHALL 调用AttendanceScheduleService.getEmployeeSchedule()
3. WHEN 查询部门排班 THEN 系统 SHALL 调用AttendanceScheduleService.getDepartmentSchedule()
4. WHEN 批量排班 THEN 系统 SHALL 循环调用AttendanceScheduleService.saveOrUpdateSchedule()
5. WHEN 删除排班 THEN 系统 SHALL 调用AttendanceScheduleService.deleteSchedule()
6. WHEN 检查排班冲突 THEN 系统 SHALL 调用AttendanceScheduleService.hasSchedule()

### Requirement 3 - 班次管理Controller

**User Story:** 作为考勤管理员，我希望通过API接口管理班次信息，以便利用现有的ShiftsService和TimePeriodsService功能。

#### Acceptance Criteria

1. WHEN 创建班次 THEN 系统 SHALL 创建ShiftsController调用现有ShiftsService（如果存在）
2. WHEN 管理时间段 THEN 系统 SHALL 创建TimePeriodsController调用现有TimePeriodsService
3. WHEN 配置班次规则 THEN 系统 SHALL 整合时间段和班次管理功能
4. WHEN 查询班次详情 THEN 系统 SHALL 返回完整的班次配置信息

### Requirement 4 - 考勤设备管理Controller

**User Story:** 作为设备管理员，我希望通过API接口管理考勤设备，以便利用现有的AttendanceDeviceManager功能。

#### Acceptance Criteria

1. WHEN 查询设备列表 THEN 系统 SHALL 创建DeviceController调用现有的Device管理Service
2. WHEN 配置设备参数 THEN 系统 SHALL 利用现有的设备配置功能
3. WHEN 监控设备状态 THEN 系统 SHALL 调用现有的设备状态监控Service
4. WHEN 测试设备连接 THEN 系统 SHALL 利用现有的设备测试功能

### Requirement 5 - 考勤统计API增强

**User Story:** 作为管理层，我希望获得更丰富的考勤统计API，以便利用现有的AttendanceStatisticsManager功能。

#### Acceptance Criteria

1. WHEN 查询出勤统计 THEN 系统 SHALL 创建StatisticsController调用AttendanceStatisticsManager
2. WHEN 生成月度报表 THEN 系统 SHALL 利用现有的月度统计功能
3. WHEN 查询异常统计 THEN 系统 SHALL 调用现有的异常分析Service
4. WHEN 导出统计报表 THEN 系统 SHALL 利用现有的导出功能

## Non-Functional Requirements

### Code Architecture and Modularity

- **100%复用现有Service**: 所有Controller只做参数验证和调用，不重复实现业务逻辑
- **四层架构严格遵循**: Controller → Service → Manager → DAO
- **统一响应格式**: 使用ResponseDTO包装所有API响应
- **权限控制一致性**: 严格遵循Sa-Token和@RequireResource注解规范

### Performance

- **轻量级Controller**: Controller层只做参数验证和Service调用，保证高性能
- **复用现有缓存**: 充分利用Service层现有的缓存机制
- **事务边界不变**: 保持Service层的事务管理边界
- **无额外开销**: 不引入额外的性能开销

### Security

- **权限一致性**: 与现有API保持相同的权限控制策略
- **参数验证**: 使用@Valid注解进行完整的参数验证
- **操作审计**: 复用Service层的现有审计机制
- **数据安全**: 不引入新的数据安全隐患

### Reliability

- **异常处理**: 统一使用Service层的异常处理机制
- **事务完整性**: 保持Service层的事务完整性
- **错误恢复**: 复用Service层的错误恢复机制
- **日志记录**: 保持与现有API一致的日志记录方式

### Usability

- **API设计一致**: 与现有Controller保持相同的设计风格
- **文档完整**: 提供完整的Swagger API文档
- **错误友好**: 提供与现有API一致的错误提示
- **参数验证**: 提供清晰的参数验证错误信息

### Global Consistency

- **编码规范100%**: 遵循repowiki编码规范，使用jakarta包名、@Resource注入
- **架构模式一致**: 与现有Controller保持完全一致的架构模式
- **命名规范统一**: 遵循项目统一的命名约定
- **依赖管理一致**: 使用现有的依赖注入和配置管理

### Zero Redundancy Principle

- **无重复业务逻辑**: 绝不重复Service层已实现的业务逻辑
- **无重复数据访问**: 绝不绕过Service层直接访问DAO
- **无重复工具类**: 充分复用现有工具类和帮助方法
- **无重复配置**: 复用现有的配置和常量定义