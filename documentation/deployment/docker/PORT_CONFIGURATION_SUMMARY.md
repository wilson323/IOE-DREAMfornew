# IOE-DREAM 端口配置汇总

**最后更新**: 2025-01-30  
**状态**: ✅ 已修复所有端口冲突

---

## 📊 完整端口分配表

### 基础设施服务端口

| 服务 | 端口 | 协议 | 说明 | 外部访问 |
|------|------|------|------|---------|
| MySQL | 3306 | TCP | 数据库 | ❌ 仅内网 |
| Redis | 6379 | TCP | 缓存 | ❌ 仅内网 |
| Nacos | 8848 | TCP | 服务注册中心 | ✅ 需要访问控制 |
| Nacos gRPC | 9848 | TCP | Nacos gRPC端口 | ❌ 仅内网 |

---

### 微服务HTTP端口

| 服务名称 | 端口 | 协议 | 说明 | 外部访问 |
|---------|------|------|------|---------|
| ioedream-gateway-service | 8080 | TCP | API网关 | ✅ 对外暴露 |
| ioedream-device-comm-service | 8087 | TCP | 设备通讯服务 | ❌ 仅内网 |
| ioedream-common-service | 8088 | TCP | 公共业务服务 | ❌ 仅内网 |
| ioedream-oa-service | 8089 | TCP | OA服务 | ❌ 仅内网 |
| ioedream-access-service | 8090 | TCP | 门禁服务 | ❌ 仅内网 |
| ioedream-attendance-service | 8091 | TCP | 考勤服务 | ❌ 仅内网 |
| ioedream-video-service | 8092 | TCP | 视频服务 | ❌ 仅内网 |
| ioedream-consume-service | 8094 | TCP | 消费服务 | ❌ 仅内网 |
| ioedream-visitor-service | 8095 | TCP | 访客服务 | ❌ 仅内网 |

---

### 设备通讯服务内部端口

| 端口类型 | 端口 | 协议 | 说明 | 外部访问 |
|---------|------|------|------|---------|
| TCP推送 | 18087 | TCP | 接收设备TCP推送数据 | ❌ 仅容器内部 |
| UDP推送 | 18089 | UDP | 接收设备UDP推送数据 | ❌ 仅容器内部 |

**重要**: 这些端口仅在容器内部使用，不需要在docker-compose中映射。

---

### 监控服务端口

| 服务 | 端口 | 协议 | 说明 | 外部访问 |
|------|------|------|------|---------|
| Prometheus | 9090 | TCP | 监控数据采集 | ✅ 需要访问控制 |
| AlertManager | 9093 | TCP | 告警管理 | ✅ 需要访问控制 |
| Grafana | 3000 | TCP | 可视化面板 | ✅ 需要访问控制 |
| Node Exporter | 9100 | TCP | 系统监控 | ❌ 仅内网 |

---

## ✅ 端口冲突修复记录

### 已修复的冲突

1. **设备通讯服务TCP端口冲突** ✅
   - **原配置**: TCP端口8088
   - **冲突服务**: common-service HTTP端口8088
   - **修复方案**: 改为18087
   - **修复文件**: 
     - `microservices/ioedream-device-comm-service/src/main/resources/application.yml`
     - `microservices/ioedream-device-comm-service/src/main/java/.../TcpPushServer.java`

2. **设备通讯服务UDP端口冲突** ✅
   - **原配置**: UDP端口8089
   - **冲突服务**: oa-service HTTP端口8089
   - **修复方案**: 改为18089
   - **修复文件**: 
     - `microservices/ioedream-device-comm-service/src/main/resources/application.yml`

---

## 🔍 端口冲突检查工具

使用以下脚本检查端口冲突：

```powershell
# 检查所有端口冲突
.\scripts\check-port-conflicts.ps1

# 检查配置一致性
.\scripts\verify-config-consistency.ps1

# 诊断Docker Compose问题
.\scripts\diagnose-docker-compose.ps1
```

---

## 📝 配置规范

### 端口分配原则

1. **HTTP服务端口**: 8080-8095范围（按服务顺序分配）
2. **设备内部端口**: 18000+范围（避免与HTTP端口冲突）
3. **基础设施端口**: 标准端口（3306, 6379, 8848等）
4. **监控服务端口**: 9000+范围（9090, 9093, 9100等）

### 端口命名规范

- **环境变量**: 使用 `SERVER_PORT` 统一管理HTTP端口
- **设备端口**: 使用 `DEVICE_PROTOCOL_TCP_PORT` 和 `DEVICE_PROTOCOL_UDP_PORT`
- **配置默认值**: 所有端口配置必须提供默认值

---

## 🚨 注意事项

1. **设备端口不映射**: 设备通讯服务的TCP/UDP端口（18087/18089）不需要在docker-compose中映射，仅在容器内部使用
2. **端口冲突检查**: 新增服务前必须运行端口冲突检查脚本
3. **配置一致性**: 确保application.yml、docker-compose-all.yml和实际代码中的端口配置一致

---

**维护责任人**: IOE-DREAM 架构委员会  
**相关文档**: 
- [全局配置标准](../technical/GLOBAL_CONFIGURATION_STANDARDS.md)
- [端口冲突分析](./PORT_CONFLICT_ANALYSIS.md)
