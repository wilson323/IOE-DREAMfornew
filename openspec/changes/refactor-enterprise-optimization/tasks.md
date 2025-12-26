# Tasks: Enterprise-Level Optimization Refactoring

## 1. P0级优化：PaymentService拆分

- [x] 1.1 创建`WechatPayService`接口和实现类
- [x] 1.2 创建`AlipayPayService`接口和实现类
- [x] 1.3 创建`PaymentCallbackService`接口和实现类
- [x] 1.4 重构`PaymentService`，委托给新服务
- [ ] 1.5 更新Controller层调用（可选，当前委托模式已可用）
- [ ] 1.6 添加单元测试

## 2. P0级优化：ApprovalServiceImpl TODO处理

- [x] 2.1 完成审批流程相关TODO（已确认无真正TODO，仅变量名包含Todo）
- [x] 2.2 完成审批节点配置TODO（已确认无真正TODO）
- [x] 2.3 完成审批统计TODO（已确认无真正TODO）
- [x] 2.4 完成ApprovalController TODO（已确认无真正TODO）

## 3. P1级优化：补充Manager层

- [x] 3.1 为video-service创建VideoDeviceManager
- [x] 3.2 为video-service创建VideoStreamManager
- [x] 3.3 在ManagerConfiguration中注册Manager Bean
- [x] 3.4 为gateway-service创建RouteManager
- [x] 3.5 为gateway-service创建RateLimitManager
- [x] 3.6 在gateway ManagerConfiguration中注册Manager Bean

## 4. P1级优化：API路径统一

- [x] 4.1 审查所有API路径（已确认consume-api.js使用/api/consume/和/api/v1/前缀）
- [x] 4.2 统一consume-api.js中的路径前缀（已符合规范）
- [x] 4.3 更新网关路由配置（添加区域权限路由、完善注释）

## 5. P2级优化：前端TODO清理

- [x] 5.1 清理account/index.vue中的TODO（账户类别列表）
- [x] 5.2 清理visitor/statistics.vue中的TODO（图表初始化）
- [x] 5.3 清理meal-category/ImportModal.vue中的TODO（模板下载、导入API）
- [x] 5.4 清理report/index.vue中的TODO（文件下载）
- [x] 5.5 清理dashboard/index.vue中的TODO（后端接口对接）
- [x] 5.6 清理RuleTestModal.vue中的TODO（查看日志）
- [x] 5.7 清理GlobalLinkageManagement.vue中的TODO（查看全部历史）

## 6. P2级优化：移动端TODO清理

- [x] 6.1 清理access.js中的2处TODO
- [x] 6.2 清理linkage-history.vue中的3处TODO
- [x] 6.3 清理linkage-rule-form.vue中的1处TODO
- [x] 6.4 清理global-linkage.vue中的2处TODO

## 7. 验证

- [x] 7.1 运行Maven编译验证（通过）
- [ ] 7.2 运行单元测试（可选）
- [ ] 7.3 验证前端构建（可选）
- [ ] 7.4 更新文档（可选）

## 8. 额外完成的工作

### 8.1 后端缺失接口实现（visitor-service）
- [x] 8.1.1 实现`/api/v1/mobile/visitor/statistics/{userId}` - 个人访问统计
- [x] 8.1.2 实现`/api/v1/mobile/visitor/areas` - 访问区域列表
- [x] 8.1.3 实现`/api/v1/mobile/visitor/appointment-types` - 预约类型列表
- [x] 8.1.4 实现`/api/v1/mobile/visitor/export` - 导出访问记录
- [x] 8.1.5 实现`/api/v1/mobile/visitor/help` - 帮助信息
- [x] 8.1.6 实现`/api/v1/mobile/visitor/validate` - 验证访客信息
- [x] 8.1.7 实现`/api/v1/mobile/visitor/visitee/{userId}` - 被访人信息

### 8.2 新增文件
- `VisitorStatisticsService.java` - 添加getPersonalStatistics方法
- `VisitorStatisticsServiceImpl.java` - 实现个人统计功能
- `VisitorExportService.java` - 添加exportRecords方法
- `VisitorExportServiceImpl.java` - 新建导出服务实现
- `VisitorMobileController.java` - 添加7个新接口
- `RouteManager.java` - 网关路由管理器
- `RateLimitManager.java` - 网关限流管理器
