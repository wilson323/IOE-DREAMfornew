# IOE-DREAM 可观测性指标命名标准

> **版本**: v1.0.0  
> **更新日期**: 2025-12-14  
> **维护团队**: IOE-DREAM 架构委员会  
> **适用范围**: 所有使用@Observed注解的代码

---

## 🎯 命名规范原则

### 统一命名格式

**标准格式**: `{service}.{module}.{operation}`

**示例**:
- ✅ `consume.payment.process` - 消费服务的支付模块的处理操作
- ✅ `access.device.query` - 门禁服务的设备模块的查询操作
- ✅ `attendance.record.create` - 考勤服务的记录模块的创建操作

---

## 📋 命名规范详解

### 1. Service标识（服务名）

**规则**: 使用服务简写名称

| 服务名称 | Service标识 |
|---------|------------|
| ioedream-consume-service | `consume` |
| ioedream-access-service | `access` |
| ioedream-attendance-service | `attendance` |
| ioedream-visitor-service | `visitor` |
| ioedream-video-service | `video` |
| ioedream-oa-service | `oa` |
| ioedream-common-service | `common` |
| ioedream-device-comm-service | `device` |
| ioedream-gateway-service | `gateway` |

---

### 2. Module标识（模块名）

**规则**: 使用业务模块名称（小写，点分隔）

**常见模块**:
- `payment` - 支付模块
- `account` - 账户模块
- `device` - 设备模块
- `record` - 记录模块
- `permission` - 权限模块
- `workflow` - 工作流模块
- `approval` - 审批模块

---

### 3. Operation标识（操作名）

**规则**: 使用动词（小写，驼峰命名）

**标准操作动词**:
- `create` - 创建
- `update` - 更新
- `delete` - 删除
- `query` - 查询
- `get` - 获取单个
- `list` - 列表查询
- `page` - 分页查询
- `process` - 处理
- `verify` - 验证
- `cancel` - 取消

---

## ✅ 正确示例

### Controller层指标

```java
@Observed(name = "consume.payment.process", contextualName = "consume-payment-process")
@PostMapping("/process")
public ResponseDTO<PaymentResultDTO> processPayment(@Valid @RequestBody PaymentRequestDTO request) {
    // ...
}

@Observed(name = "access.device.query", contextualName = "access-device-query")
@GetMapping("/query")
public ResponseDTO<PageResult<DeviceVO>> queryDevices(@RequestParam Integer pageNum) {
    // ...
}
```

### Service层指标

```java
@Observed(name = "consume.account.getBalance", contextualName = "consume-account-get-balance")
public ResponseDTO<BigDecimal> getAccountBalance(Long accountId) {
    // ...
}

@Observed(name = "attendance.record.create", contextualName = "attendance-record-create")
public ResponseDTO<Long> createAttendanceRecord(AttendanceRecordForm form) {
    // ...
}
```

---

## ❌ 错误示例

```java
// ❌ 错误：缺少service标识
@Observed(name = "payment.process")

// ❌ 错误：命名不一致
@Observed(name = "paymentProcessPayment")

// ❌ 错误：使用下划线
@Observed(name = "consume_payment_process")

// ❌ 错误：使用大写
@Observed(name = "Consume.Payment.Process")
```

---

## 📊 指标命名检查清单

- [ ] 使用`{service}.{module}.{operation}`格式
- [ ] Service标识使用标准简写
- [ ] Module标识使用业务模块名称
- [ ] Operation标识使用标准动词
- [ ] 全部小写，点分隔
- [ ] contextualName使用kebab-case（短横线分隔）

---

## 🔍 自动化检查

### PowerShell检查脚本

```powershell
# 检查@Observed命名规范
Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" |
    Select-String -Pattern '@Observed\(name\s*=\s*"([^"]+)"' |
    ForEach-Object {
        $name = $_.Matches.Groups[1].Value
        if ($name -notmatch '^[a-z]+\.[a-z]+\.[a-z]+') {
            Write-Host "[违规] $($_.Path):$($_.LineNumber) - $name" -ForegroundColor Red
        }
    }
```

---

## 📚 相关文档

- [分布式追踪配置](../microservices/microservices-common-monitor/src/main/java/net/lab1024/sa/common/tracing/TracingConfiguration.java)
- [全局一致性优化路线图](../../.trae/plans/global-consistency-optimization-roadmap.md)

---

**最后更新**: 2025-12-14  
**维护团队**: IOE-DREAM 架构委员会
