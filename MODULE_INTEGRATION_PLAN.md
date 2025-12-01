# 微服务模块整合方案

## 整合目标
将功能相关或重叠的模块整合到既有的相关联模块中，减少模块数量，提高代码复用性和维护效率。

## 整合方案

### 1. analytics → ioedream-report-service ✅ 推荐整合

**整合理由：**
- `analytics` 模块功能：统一报表分析服务、跨服务数据聚合分析、报表生成、业务洞察
- `ioedream-report-service` 模块功能：报表分析微服务、数据报表、统计分析、图表生成、Excel导出
- **功能高度重叠**，analytics 的报表生成功能更完整，应整合到 report-service

**整合步骤：**
1. 将 `analytics` 的 `ReportGenerationService` 整合到 `report-service`
2. 将 `analytics` 的 VO 类整合到 `report-service/domain/vo`
3. 修复包名和导入路径
4. 更新 pom.xml 移除 analytics 模块
5. 删除 analytics 目录

### 2. ioedream-audit-service → ioedream-logging-service ⚠️ 可选整合

**整合理由：**
- `audit-service` 功能：审计日志、合规报告
- `logging-service` 功能：系统日志查询、统计
- **功能相关但职责不同**，审计和日志可以合并为一个服务

**整合步骤：**
1. 将 `audit-service` 的审计功能整合到 `logging-service`
2. 创建 `AuditLogController` 和 `SystemLogController` 区分职责
3. 统一日志存储和查询接口
4. 更新 pom.xml

### 3. ioedream-integration-service + ioedream-scheduler-service → ioedream-system-service ⚠️ 可选整合

**整合理由：**
- `integration-service` 和 `scheduler-service` 都是系统支撑功能
- `system-service` 是系统管理服务，可以包含这些功能

**整合步骤：**
1. 将 integration 和 scheduler 功能作为 system-service 的子模块
2. 创建对应的 Controller 和 Service
3. 更新 pom.xml

## 推荐执行顺序

1. **优先整合 analytics → report-service**（功能重叠度高，整合收益大）
2. 根据实际需求决定是否整合 audit 和 logging
3. 根据实际需求决定是否整合 integration 和 scheduler

## 整合后的模块结构

```
microservices/
├── microservices-common/          # 公共模块
├── ioedream-config-service/       # 配置中心
├── ioedream-gateway-service/      # API网关
├── ioedream-auth-service/         # 认证服务
├── ioedream-identity-service/    # 身份权限服务
├── ioedream-logging-service/      # 日志服务（整合audit后）
├── ioedream-device-service/       # 设备管理服务
├── ioedream-system-service/       # 系统管理服务（整合integration+scheduler后）
├── ioedream-access-service/       # 门禁管理服务
├── ioedream-consume-service/      # 消费管理服务
├── ioedream-visitor-service/      # 访客管理服务
├── ioedream-attendance-service/   # 考勤管理服务
├── ioedream-video-service/        # 视频监控服务
├── ioedream-notification-service/ # 通知服务
├── ioedream-file-service/         # 文件服务
├── ioedream-hr-service/           # 人力资源服务
├── ioedream-oa-service/          # 办公自动化服务
├── ioedream-monitor-service/      # 监控告警服务
└── ioedream-report-service/       # 报表服务（整合analytics后）
```

## 注意事项

1. 整合前需要备份代码
2. 整合后需要更新所有依赖引用
3. 整合后需要更新 API 文档
4. 整合后需要更新部署配置

