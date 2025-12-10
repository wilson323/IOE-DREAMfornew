# 端口冲突分析与修复方案

**日期**: 2025-01-30  
**严重性**: P0级 - 必须立即修复

---

## 🚨 发现的端口冲突

### 冲突1: 设备通讯服务TCP端口与common-service冲突

**冲突详情**:
- **设备通讯服务TCP端口**: 8088 (application.yml配置)
- **common-service HTTP端口**: 8088 (docker-compose配置)
- **冲突类型**: 端口完全冲突，无法同时使用

**影响**:
- ❌ 设备通讯服务无法启动TCP服务器
- ❌ common-service无法正常启动
- ❌ 整个服务栈启动失败

---

### 冲突2: 设备通讯服务UDP端口与oa-service冲突

**冲突详情**:
- **设备通讯服务UDP端口**: 8089 (application.yml配置)
- **oa-service HTTP端口**: 8089 (docker-compose配置)
- **冲突类型**: 端口完全冲突，无法同时使用

**影响**:
- ❌ 设备通讯服务无法启动UDP服务器
- ❌ oa-service无法正常启动
- ❌ 整个服务栈启动失败

---

## ✅ 修复方案

### 方案: 调整设备通讯服务内部端口

**原则**:
1. 设备通讯服务的TCP/UDP端口是**内部端口**，不需要在docker-compose中映射
2. 这些端口只在容器内部使用，用于接收设备推送数据
3. 选择未被占用的端口范围

**推荐端口分配**:
- **TCP端口**: 从 `8088` 改为 `18087` (设备通讯服务HTTP端口8087 + 10000)
- **UDP端口**: 从 `8089` 改为 `18089` (oa-service HTTP端口8089 + 10000)

**理由**:
- 18087/18089 在10000+范围，避免与HTTP服务端口冲突
- 容易记忆（设备通讯服务8087 + 10000）
- 端口范围充足，未来扩展方便

---

## 🔧 修复步骤

### 步骤1: 修改application.yml配置

**文件**: `microservices/ioedream-device-comm-service/src/main/resources/application.yml`

```yaml
device:
  protocol:
    tcp:
      port: ${DEVICE_PROTOCOL_TCP_PORT:18087}  # 从8088改为18087
    udp:
      port: ${DEVICE_PROTOCOL_UDP_PORT:18089}  # 从8089改为18089
```

### 步骤2: 更新Java代码默认值（如果存在）

**文件**: `microservices/ioedream-device-comm-service/src/main/java/net/lab1024/sa/devicecomm/protocol/server/TcpPushServer.java`

```java
@Value("${device.protocol.tcp.port:18087}")  // 从8087改为18087
private int tcpPort;
```

### 步骤3: 更新docker-compose环境变量（可选）

如果需要从外部访问设备TCP/UDP端口，可以在docker-compose中添加端口映射：

```yaml
device-comm-service:
  ports:
    - "8087:8087"  # HTTP端口
    - "18087:18087"  # TCP端口（如果需要外部访问）
    - "18089:18089/udp"  # UDP端口（如果需要外部访问）
```

**注意**: 通常设备推送数据来自内网，不需要外部映射。

---

## 📊 完整端口分配表

| 服务/功能 | 端口 | 类型 | 说明 |
|---------|------|------|------|
| **基础设施** |
| MySQL | 3306 | TCP | 数据库 |
| Redis | 6379 | TCP | 缓存 |
| Nacos | 8848 | TCP | 服务注册中心 |
| Nacos | 9848 | TCP | Nacos gRPC端口 |
| **微服务HTTP** |
| gateway-service | 8080 | TCP | API网关 |
| device-comm-service | 8087 | TCP | 设备通讯服务HTTP |
| common-service | 8088 | TCP | 公共业务服务 |
| oa-service | 8089 | TCP | OA服务 |
| access-service | 8090 | TCP | 门禁服务 |
| attendance-service | 8091 | TCP | 考勤服务 |
| video-service | 8092 | TCP | 视频服务 |
| consume-service | 8094 | TCP | 消费服务 |
| visitor-service | 8095 | TCP | 访客服务 |
| **设备通讯内部端口** |
| device-comm TCP | 18087 | TCP | 设备TCP推送端口（内部） |
| device-comm UDP | 18089 | UDP | 设备UDP推送端口（内部） |
| **监控服务** |
| Prometheus | 9090 | TCP | 监控数据采集 |
| AlertManager | 9093 | TCP | 告警管理 |
| Grafana | 3000 | TCP | 可视化面板 |
| Node Exporter | 9100 | TCP | 系统监控 |

---

## ✅ 验证清单

修复后验证：
- [ ] 设备通讯服务TCP端口改为18087
- [ ] 设备通讯服务UDP端口改为18089
- [ ] common-service在8088正常启动
- [ ] oa-service在8089正常启动
- [ ] 设备通讯服务TCP服务器正常启动
- [ ] 设备通讯服务UDP服务器正常启动
- [ ] 所有服务无端口冲突
- [ ] docker-compose启动成功

---

## 📝 相关文档

- [全局配置标准](../technical/GLOBAL_CONFIGURATION_STANDARDS.md)
- [Docker Compose配置规范](./docker-compose-configuration-standards.md)

---

**修复优先级**: P0 - 立即修复  
**修复负责人**: 架构委员会  
**修复时间**: 2025-01-30
