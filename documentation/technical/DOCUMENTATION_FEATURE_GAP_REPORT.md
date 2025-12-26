# 文档功能与代码实现差距清单（全量梳理报告）

## 目标
基于 `documentation/` 各模块文档，系统性梳理功能实现与代码现状的差距，列出**未实现/不完整/高风险缺口**，并给出“编译 0 异常”的前置条件与整改建议。

## 方法与范围
- 文档范围：`documentation/业务模块/**`（功能说明/子模块/流程/验收等）
- 代码范围：`microservices/**/src/main/java`（Controller/Service/Manager/Adapter/DAO 等）
- 佐证规则：
  - 文档声明功能，但代码中**缺少对应 Controller/Service/DAO** 或 **核心逻辑标记 TODO**
  - 核心链路缺少数据模型/持久化/事件处理

> 说明：本报告以“**功能可用**”为判断标准，不仅看类名存在，还关注核心流程实现与 TODO 占位。

---

## 一、模块级差距总览

### 1) 消费管理模块（ioedream-consume-service）
文档功能来源：
- `documentation/业务模块/04-消费管理模块/01-功能说明/README.md`
- `documentation/业务模块/04-消费管理模块/子模块/*/README.md`

代码现状：
- 仅存在 `ConsumeMobileController.java` 与少量实体（订单/交易/退款等）
- 未见区域管理、账户体系、补贴、报表等核心业务链路

**未实现/不完整清单（高优先级）**
- 区域管理（CRUD/多级区域/经营模式）
- 餐别分类（时间窗口/规则配置）
- 账户类别与消费模式（mode_config）
- 区域权限配置（area_config）
- 消费处理链路（鉴权/扣款/交易记录/SAGA）
- 充值退款（多渠道/审核/对账）
- 补贴管理（发放/使用/清零策略）
- 商品/库存/进销存
- 报表与统计（汇总/趋势/对账）
- 设备管理（绑定/状态/参数）

证据文件：
- `microservices/ioedream-consume-service/src/main/java`（类数量极少）

### 2) 设备通讯模块（ioedream-device-comm-service）
文档功能来源：
- `documentation/业务模块/06-设备通讯模块/README.md`

代码现状：
- 协议适配器广泛存在，但**大量核心流程为 TODO**

**未实现/不完整清单（高优先级）**
- 设备初始化/注册/心跳处理逻辑（多厂商适配）
- 协议消息解析/校验/错误码映射
- 设备权限验证、事件持久化、联动处理
- 设备配置读取/更新
- 设备通讯日志落库

证据文件（TODO）：
- `microservices/ioedream-device-comm-service/src/main/java/net/lab1024/sa/device/comm/protocol/hikvision/VideoHikvisionV20Adapter.java`
- `microservices/ioedream-device-comm-service/src/main/java/net/lab1024/sa/device/comm/protocol/dahua/VideoDahuaV20Adapter.java`
- `microservices/ioedream-device-comm-service/src/main/java/net/lab1024/sa/device/comm/protocol/uniview/VideoUniviewV20Adapter.java`
- `microservices/ioedream-device-comm-service/src/main/java/net/lab1024/sa/device/comm/protocol/entropy/AccessEntropyV48Adapter.java`
- `microservices/ioedream-device-comm-service/src/main/java/net/lab1024/sa/device/comm/decorator/impl/BasicCommandExecutor.java`

### 3) 门禁管理模块（ioedream-access-service）
文档功能来源：
- `documentation/业务模块/03-门禁管理模块/README.md`
- 子模块：`01-门禁设备管理`/`02-区域权限管理`/`04-事件记录管理`/`05-审批流程管理`/`07-高级功能管理`

代码现状：
- 控制器存在，但**报警、统计、容量控制链路不完整**

**未实现/不完整清单（高优先级）**
- 报警查询与处理（需告警表与 DAO）
- 故障设备统计、报警统计、响应时间真实测量
- 区域实时人数/容量控制（当前为 0 占位）
- 权限申请/紧急权限审批的门禁侧落地接口未明确

证据文件（TODO）：
- `microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/service/impl/AccessMonitorServiceImpl.java`
- `microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/service/impl/AccessAreaServiceImpl.java`

### 4) 访客管理模块（ioedream-visitor-service）
文档功能来源：
- `documentation/业务模块/05-访客管理模块/子模块/*/README.md`

代码现状：
- 预约/审批/黑名单/登记/通行核心 API 具备
- **物流管理与统计分析模块未见 Controller**

**未实现/不完整清单（中优先级）**
- 物流管理 API（司机/车辆/进出记录）
- 统计分析接口

证据：
- 控制器列表无 Logistics/Statistics Controller（代码仅见实体/服务）
- `microservices/ioedream-visitor-service/src/main/java`

### 5) 视频管理模块（ioedream-video-service）
文档功能来源：
- `documentation/业务模块/05-视频管理模块/*/README.md`

代码现状：
- 控制器覆盖面较广，但流媒体适配器存在 TODO

**未实现/不完整清单（中优先级）**
- RTSP/RTMP/HTTP 停止流逻辑
- URL 构建逻辑
- 消息中心/地图展示未见专门 API（需二次确认）

证据文件（TODO）：
- `microservices/ioedream-video-service/src/main/java/net/lab1024/sa/video/adapter/impl/RTSPAdapter.java`
- `microservices/ioedream-video-service/src/main/java/net/lab1024/sa/video/adapter/impl/RTMPAdapter.java`
- `microservices/ioedream-video-service/src/main/java/net/lab1024/sa/video/adapter/impl/HTTPAdapter.java`

### 6) 公共模块（ioedream-common-service / microservices-common-*）
文档功能来源：
- `documentation/业务模块/07-公共模块/README.md`

代码现状：
- 通知/告警基础组件具备
- **权限数据同步/变更通知逻辑为 TODO**

**未实现/不完整清单（高优先级）**
- 权限数据获取/同步/统计/变更通知

证据文件（TODO）：
- `microservices/ioedream-common-service/src/main/java/net/lab1024/sa/common/permission/service/impl/PermissionDataServiceImpl.java`

### 7) 考勤模块（ioedream-attendance-service）
文档功能来源：
- `documentation/业务模块/03-考勤管理模块/01-功能说明/README.md`

代码现状：
- 控制器/服务覆盖大量功能（排班/记录/请假/加班/补签/差旅/智能排班）
- **总体实现度较高，个别测试 TODO**

未实现/不完整（低优先级）
- 测试用例缺失：`microservices/ioedream-attendance-service/src/test/java/net/lab1024/sa/attendance/service/AttendanceMobileServiceImplTest.java`

### 8) OA 工作流模块（ioedream-oa-service）
文档功能来源：
- `documentation/业务模块/01-OA工作流模块/子模块/*`

代码现状：
- 流程/审批/设计器/性能/引擎控制器完整
- 与门禁/访客/请假审批回调已有联动

未实现/不完整：未发现显著缺口（需后续与 API 文档核对）

### 9) 网关模块（ioedream-gateway-service）
文档功能来源：
- `documentation/业务模块/02-网关服务模块/README.md`

代码现状：
- 鉴权、限流、路由、RBAC 相关配置与过滤器存在

未实现/不完整：未发现显著缺口（文档功能较少）

---

## 二、全局 TODO 级缺口（跨模块）

以下为代码中明确标记 TODO 的功能缺口（节选核心）：

- 生物识别（模型/特征提取/质量评估/对齐/检测）
  - `microservices/ioedream-biometric-service/src/main/java/net/lab1024/sa/biometric/util/ImageProcessingUtil.java`
  - `microservices/ioedream-biometric-service/src/main/java/net/lab1024/sa/biometric/service/impl/BiometricFeatureExtractionServiceImpl.java`
  - `microservices/ioedream-biometric-service/src/main/java/net/lab1024/sa/biometric/model/FaceNetModel.java`

- 安全体系（令牌黑名单/会话管理）
  - `microservices/microservices-common-security/src/main/java/net/lab1024/sa/common/auth/manager/AuthManager.java`
  - `microservices/microservices-common-security/src/main/java/net/lab1024/sa/common/auth/util/JwtTokenUtil.java`

- 门禁监控统计与报警处理
  - `microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/service/impl/AccessMonitorServiceImpl.java`
  - `microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/service/impl/AccessAreaServiceImpl.java`

- 设备通讯协议适配（多厂商）
  - `microservices/ioedream-device-comm-service/src/main/java/net/lab1024/sa/device/comm/protocol/**`

---

## 三、编译 0 异常的前置条件（当前无法保证）

基于 `erro.txt` 与历史诊断，**目前无法保证编译 0 异常**，需要先完成以下前置整改：

- 公共模块依赖链完整：`microservices-common-*` 需先构建并正确被业务模块依赖（否则 `ResponseDTO`/`BusinessException` 不可解析）。
- Spring Boot 配置弃用项迁移（`spring.redis.*` → `spring.data.redis.*` 等）。
- 未完成/缺失的类与测试类型不匹配需修复。

> 如果需要我继续执行“编译 0 异常”整改，请单独确认，我会输出可执行整改清单并分阶段实施。

---

## 四、建议的修复优先级

1. **消费管理模块**：缺口最大，建议先完成最小闭环（区域/账户/消费/充值/补贴）。
2. **设备通讯模块**：协议适配器核心 TODO 影响设备端全链路。
3. **公共权限同步**：影响门禁/访客等权限联动。
4. **门禁报警/统计**：影响可观测性与业务闭环。
5. **视频流适配器**：影响视频回放/拉流稳定性。

---

## 五、下一步可执行输出（如需）
- 按模块生成“缺口任务拆分清单”（任务颗粒度 0.5～2 人日）
- 输出 API 级缺失列表（与 `API接口设计` 文档逐条对齐）
- 制定编译 0 异常整改路线（依赖与配置迁移）
