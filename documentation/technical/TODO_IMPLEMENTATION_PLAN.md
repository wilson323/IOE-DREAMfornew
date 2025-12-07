# TODO事项企业级实现计划

**版本**: v1.0.0  
**创建时间**: 2025-01-30  
**状态**: 进行中

## 📋 执行概览

本文档记录所有TODO事项的企业级实现计划，确保高质量、规范化的代码实现。

## 🎯 TODO事项清单

### 1. NotificationManager - 根据告警规则创建通知
**优先级**: P0  
**位置**: `microservices-common/src/main/java/net/lab1024/sa/common/monitor/manager/NotificationManager.java:141`  
**状态**: 待实现

**实现要求**:
- 查询告警规则表（AlertRuleEntity）
- 根据规则配置的通知渠道和接收人创建通知
- 支持多渠道通知（邮件、短信、Webhook、微信）
- 支持多接收人通知

**技术方案**:
- 注入AlertRuleDao查询告警规则
- 解析notificationChannels和notificationUsers字段
- 为每个渠道和接收人创建通知记录

---

### 2. MetricsCollectorManager - 实现指标采集
**优先级**: P0  
**位置**: `microservices-common/src/main/java/net/lab1024/sa/common/monitor/manager/MetricsCollectorManager.java`  
**状态**: 待实现

**实现要求**:
- 集成Micrometer进行指标采集
- 支持QPS、TPS、响应时间、错误率等指标
- 支持业务指标采集
- 指标存储到Redis或时序数据库

**技术方案**:
- 集成Spring Boot Actuator + Micrometer
- 使用MeterRegistry进行指标采集
- Redis存储指标数据（可选InfluxDB）

---

### 3. HealthCheckManager - 微服务健康检查
**优先级**: P1  
**位置**: `microservices-common/src/main/java/net/lab1024/sa/common/monitor/manager/HealthCheckManager.java:128`  
**状态**: 待实现

**实现要求**:
- 通过Nacos获取所有注册的微服务
- 调用各微服务的健康检查端点
- 返回微服务健康状态列表

**技术方案**:
- 使用Nacos Discovery API获取服务列表
- 调用`/actuator/health`端点
- 聚合健康状态信息

---

### 4. MenuServiceImpl - 角色菜单关联查询
**优先级**: P1  
**位置**: `microservices-common/src/main/java/net/lab1024/sa/common/menu/service/impl/MenuServiceImpl.java:543`  
**状态**: 待实现

**实现要求**:
- 查询t_role_menu表
- 检查菜单是否被角色使用
- 返回布尔值

**技术方案**:
- 创建RoleMenuDao接口
- 查询t_role_menu表是否存在menuId的记录
- 返回count > 0

---

### 5. JwtTokenUtil - JWT API升级
**优先级**: P1  
**位置**: `microservices-common/src/main/java/net/lab1024/sa/common/auth/util/JwtTokenUtil.java:90`  
**状态**: 待实现

**实现要求**:
- 升级JWT依赖到0.13.0
- 更新API调用方式
- 保持向后兼容

**技术方案**:
- 更新pom.xml依赖版本
- 使用新的JWT API（Jwts.builder()等）
- 测试兼容性

---

### 6. AuditManager - 导出和归档逻辑
**优先级**: P1  
**位置**: `microservices-common/src/main/java/net/lab1024/sa/common/audit/manager/AuditManager.java`  
**状态**: 待实现

**实现要求**:
- Excel导出功能（使用EasyExcel）
- PDF导出功能（使用iText或Apache PDFBox）
- 归档逻辑（压缩存储）

**技术方案**:
- 集成EasyExcel进行Excel导出
- 集成iText进行PDF导出
- 使用ZipOutputStream进行归档压缩

---

### 7. OcrService - 腾讯云OCR SDK集成
**优先级**: P2  
**位置**: `ioedream-visitor-service/src/main/java/net/lab1024/sa/visitor/service/OcrService.java`  
**状态**: 待实现

**实现要求**:
- 集成腾讯云OCR SDK
- 支持身份证、驾驶证、车牌、营业执照识别
- 错误处理和重试机制

**技术方案**:
- 添加腾讯云OCR SDK依赖
- 实现各类型OCR识别方法
- 配置密钥和区域

---

### 8. VideoPreviewManager - 预置位列表获取
**优先级**: P2  
**位置**: `ioedream-video-service/src/main/java/net/lab1024/sa/video/manager/VideoPreviewManager.java:414`  
**状态**: 待实现

**实现要求**:
- 通过设备协议适配器获取预置位列表
- 支持不同厂商的设备协议
- 错误处理

**技术方案**:
- 调用DeviceProtocolAdapter获取预置位
- 适配不同设备协议（海康、大华等）
- 返回标准化的预置位列表

---

## 📊 实现进度

| TODO项 | 优先级 | 状态 | 预计工时 | 实际工时 |
|--------|--------|------|----------|----------|
| NotificationManager | P0 | 待实现 | 4h | - |
| MetricsCollectorManager | P0 | 待实现 | 6h | - |
| HealthCheckManager | P1 | 待实现 | 4h | - |
| MenuServiceImpl | P1 | 待实现 | 2h | - |
| JwtTokenUtil | P1 | 待实现 | 2h | - |
| AuditManager | P1 | 待实现 | 8h | - |
| OcrService | P2 | 待实现 | 6h | - |
| VideoPreviewManager | P2 | 待实现 | 4h | - |

**总计**: 36小时

---

## 🔧 技术栈要求

### 新增依赖
- `io.micrometer:micrometer-core` - 指标采集
- `com.alibaba:easyexcel` - Excel导出
- `com.itextpdf:itext7-core` - PDF导出
- `com.tencentcloudapi:tencentcloud-sdk-java-ocr` - 腾讯云OCR SDK

### 现有依赖升级
- `io.jsonwebtoken:jjwt-api` - 0.12.3 → 0.13.0

---

## ✅ 质量保证

- [ ] 代码审查通过
- [ ] 单元测试覆盖率 ≥ 80%
- [ ] 集成测试通过
- [ ] 性能测试通过
- [ ] 安全扫描通过
- [ ] 文档更新完成

---

## 📝 实施步骤

1. **阶段1**: 实现P0优先级TODO（NotificationManager、MetricsCollectorManager）
2. **阶段2**: 实现P1优先级TODO（HealthCheckManager、MenuServiceImpl、JwtTokenUtil、AuditManager）
3. **阶段3**: 实现P2优先级TODO（OcrService、VideoPreviewManager）
4. **阶段4**: 全面测试和文档更新

---

**维护人**: IOE-DREAM Team  
**最后更新**: 2025-01-30
