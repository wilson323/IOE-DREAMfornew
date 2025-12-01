# TODO实现计划

本文档记录了项目中所有待实现的TODO项，并按照优先级和模块进行分类。

## 统计信息

- 总TODO数量: 504个
- 已分类TODO: 504个
- 高优先级TODO: 约50个（核心业务功能）

## 模块分类

### 1. OA服务模块 (ioedream-oa-service) - 高优先级
- [x] DocumentManager.uploadFile - 文件上传（已完成）
- [ ] DocumentManager.generateDownloadUrl - 下载URL生成（第1个）
- [ ] DocumentManager.getLatestVersionNumber - 获取最新版本号（第2个）
- [ ] DocumentServiceImpl.createDocument - 设置创建者信息（第3个）
- [ ] DocumentServiceImpl.uploadDocument - 设置创建者信息（第4个）
- [ ] DocumentServiceImpl.addDocumentPermission - 设置创建者信息（第5个）
- [ ] DocumentServiceImpl.getMyDocuments - 设置当前用户ID（第6个）

### 2. 系统服务模块 (ioedream-system-service) - 高优先级
- [ ] EmployeeManager - 7个员工查询和统计TODO
- [ ] DepartmentServiceImpl - 3个缓存和统计TODO
- [ ] UnifiedDeviceServiceImpl - 创建者信息TODO

### 3. 考勤服务模块 (ioedream-attendance-service) - 中优先级
- [ ] AttendanceServiceImpl - 位置验证、设备验证等TODO
- [ ] AttendanceScheduleServiceImpl - 班次验证TODO
- [ ] AttendanceMobileService - 离线打卡TODO

### 4. 设备服务模块 (ioedream-device-service) - 中优先级
- [ ] DeviceHealthServiceImpl - 多个健康检查相关TODO
- [ ] SmartDeviceServiceImpl - 设备远程控制TODO
- [ ] DeviceProtocolAdapterFactory - MQTT协议适配器TODO

### 5. 审计服务模块 (ioedream-audit-service) - 中优先级
- [ ] AuditServiceImpl - 导出、告警、合规性检查等TODO

### 6. 企业服务模块 (ioedream-enterprise-service) - 中优先级
- [ ] DocumentServiceImpl - 文档管理相关TODO
- [ ] WorkflowEngineServiceImpl - 工作流引擎TODO（大量）
- [ ] DocumentManager - 文件上传和下载TODO

### 7. 消费服务模块 (ioedream-consume-service) - 低优先级
- [ ] ReportServiceImpl - Excel生成、模板保存等TODO
- [ ] SecurityNotificationServiceImpl - 通知服务TODO

### 8. 控制器层TODO（需要先实现Service层）- 低优先级
- [ ] RoleController - 角色管理TODO
- [ ] MenuController - 菜单管理TODO
- [ ] LoginController - 登录服务TODO
- [ ] DictController - 字典管理TODO
- [ ] ConfigController - 配置管理TODO

## 实施策略

### 第一阶段：核心业务功能（当前进行中）
1. ✅ 文件上传功能（已完成）
2. 🔄 文档版本管理相关TODO（进行中）
3. ⏳ 创建者信息设置相关TODO

### 第二阶段：基础服务功能
1. 员工管理相关TODO
2. 部门管理相关TODO
3. 设备管理相关TODO

### 第三阶段：高级功能
1. 工作流引擎TODO
2. 审计和合规性TODO
3. 报表和导出TODO

## 注意事项

1. 所有TODO实现必须：
   - 符合项目开发规范
   - 包含完整的注释文档
   - 进行异常处理
   - 记录日志
   - 遵循命名规范

2. 优先处理：
   - 影响核心业务流程的TODO
   - 高频使用的功能TODO
   - 依赖关系链中的关键TODO

3. 实施顺序：
   - 先实现Service层，再实现Controller层
   - 先实现核心业务，再实现辅助功能
   - 先修复bug类TODO，再实现新功能TODO

