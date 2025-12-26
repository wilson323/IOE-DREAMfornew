# IOE-DREAM 全局一致性优化路线图

> **版本**: v1.0.0  
> **生成日期**: 2025-12-14  
> **目标**: 确保架构、技术栈、开发规范、代码规范全局一致  
> **执行原则**: P0级问题立即修复，P1级问题1周内完成，P2级问题1个月内完成

---

## 📊 全局一致性扫描结果摘要

### 技术栈版本一致性 ✅

| 技术栈组件 | 标准版本 | 实际版本 | 一致性状态 |
|-----------|---------|---------|-----------|
| Spring Boot | 3.5.8 | 3.5.8 (父POM统一管理) | ✅ 一致 |
| Spring Cloud | 2025.0.0 | 2025.0.0 (父POM统一管理) | ✅ 一致 |
| Spring Cloud Alibaba | 2025.0.0.0 | 2025.0.0.0 (父POM统一管理) | ✅ 一致 |
| Java | 17 | 17 (父POM统一管理) | ✅ 一致 |
| MyBatis-Plus | 3.5.15 | 3.5.15 (父POM统一管理) | ✅ 一致 |
| MySQL | 8.0.35 | 8.0.35 (父POM统一管理) | ✅ 一致 |
| Druid | 1.2.25 | 1.2.25 (父POM统一管理) | ✅ 一致 |

**结论**: 技术栈版本通过父POM统一管理，**全局一致** ✅

---

### 代码规范一致性扫描

| 规范项 | 标准要求 | 实际统计 | 合规状态 | 问题文件数 |
|-------|---------|---------|---------|-----------|
| **依赖注入** | 统一使用`@Resource` | 452个匹配（@Resource/@Autowired） | ⚠️ 需确认 | 需逐文件检查 |
| **Jakarta包** | 统一使用`jakarta.*` | 154个文件使用`jakarta.annotation.Resource` | ✅ 合规 | 0 |
| **DAO层命名** | 统一使用`@Mapper` + `Dao`后缀 | 203个`@Mapper`匹配 | ✅ 合规 | 0 |
| **Controller层** | 统一使用`@RestController` | 75个文件 | ✅ 合规 | 0 |
| **Service层** | 统一使用`@Service` | 150个文件 | ✅ 合规 | 0 |
| **HTTP方法** | RESTful规范 | 393个匹配 | ⚠️ 需确认POST滥用 | 需抽样检查 |

---

## 🎯 P0级立即修复项（架构违规）

### 1. 架构边界违规：ioedream-database-service

**问题描述**:
- `ioedream-database-service`不在7微服务架构清单中
- 启用了`@EnableFeignClients`，违反"默认经网关调用"规范

**证据定位**:
- `microservices/ioedream-database-service/pom.xml:64-67` - 引入Feign依赖
- `microservices/ioedream-database-service/src/main/java/net/lab1024/sa/database/DatabaseServiceApplication.java:38` - 启用Feign

**修复方案**:
```
方案A（推荐）: 隔离下线
- 在CI/CD中默认不构建ioedream-database-service
- 在pom.xml中标记为<skip>true</skip>或移除modules列表
- 功能迁移到ioedream-common-service的database模块

方案B（保留）: 架构合规化
- 移除@EnableFeignClients
- 移除spring-cloud-starter-openfeign依赖
- 改用GatewayServiceClient调用其他服务
- 更新架构文档，明确该服务职责边界
```

**验收标准**:
- [ ] 方案A：ioedream-database-service从构建流程中移除
- [ ] 方案B：0个@EnableFeignClients，0个Feign依赖，100%使用GatewayServiceClient

**执行时间**: 1-2天

---

### 2. 配置安全：疑似明文密码（需逐文件确认）

**问题描述**:
- 扫描发现107个`password:`字段匹配
- 需要确认是否都是`ENC(...)`加密格式或环境变量

**证据定位**:
- `microservices/common-config/nacos/common-database.yaml:16` - 已使用`ENC(...)` ✅
- 需检查其他46个配置文件

**修复方案**:
```powershell
# 步骤1: 扫描所有疑似明文密码
Get-ChildItem -Path "microservices" -Recurse -Include "*.yml","*.yaml","*.properties" |
    Select-String -Pattern "password:\s*["']?[a-zA-Z0-9]{3,}["']?" -CaseSensitive:$false |
    Where-Object { $_.Line -notmatch "ENC\(|\\$\{" } |
    Select-Object Path, LineNumber, Line

# 步骤2: 替换为ENC()或环境变量
# 使用Nacos配置加密或Jasypt加密
```

**验收标准**:
- [ ] 0个明文密码（所有password字段使用ENC()或${ENV_VAR}）
- [ ] 配置文件通过安全扫描工具验证

**执行时间**: 2-3天

---

## 🔧 P1级快速优化项（1周内完成）

### 3. 依赖注入规范：确认@Autowired违规

**问题描述**:
- 扫描发现452个@Resource/@Autowired匹配
- 需要确认是否有@Autowired违规使用

**修复方案**:
```powershell
# 扫描@Autowired使用（排除注释和文档）
Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" |
    Select-String -Pattern "^\s*@Autowired\b" |
    Where-Object { $_.Line -notmatch "//|/\*|\*" } |
    Select-Object Path, LineNumber, Line

# 批量替换为@Resource
# 注意：需要检查字段名是否匹配
```

**验收标准**:
- [ ] 0个@Autowired注解（测试类除外）
- [ ] 100%使用@Resource注入

**执行时间**: 3-5天

---

### 4. RESTful API规范：查询接口POST滥用

**问题描述**:
- 发现查询接口使用POST方法（如`MealOrderController.queryOrders`）
- 违反RESTful设计原则

**证据定位**:
- `microservices/ioedream-consume-service/src/main/java/net/lab1024/sa/consume/controller/MealOrderController.java:48` - `@PostMapping("/query")`

**修复方案**:
```java
// ❌ 错误示例
@PostMapping("/query")
public ResponseDTO<PageResult<MealOrderVO>> queryOrders(@RequestBody MealOrderQueryForm form)

// ✅ 正确示例
@GetMapping("/page")
public ResponseDTO<PageResult<MealOrderVO>> page(
    @RequestParam(required = false) Long areaId,
    @RequestParam(required = false) String status,
    @RequestParam(defaultValue = "1") Integer pageNum,
    @RequestParam(defaultValue = "10") Integer pageSize
)
```

**验收标准**:
- [ ] 查询接口100%使用GET方法
- [ ] 分页参数通过@RequestParam传递
- [ ] 保持API向后兼容（可双路由过渡）

**执行时间**: 5-7天

---

### 5. PowerShell脚本编码规范统一

**问题描述**:
- `start.ps1`声明"UTF-8 with BOM Required"
- 项目规范要求"UTF-8 without BOM"
- 导致控制台输出乱码风险

**证据定位**:
- `start.ps1:1-24` - 编码声明冲突

**修复方案**:
```powershell
# 步骤1: 统一编码标准为UTF-8 without BOM
# 步骤2: 移除脚本中的"BOM Required"声明
# 步骤3: 更新编码检测逻辑，兼容两种格式
# 步骤4: 拆分start.ps1为模块化脚本（≤400行/文件）
```

**验收标准**:
- [ ] 所有PowerShell脚本使用UTF-8 without BOM
- [ ] 脚本编码检测逻辑统一
- [ ] 无控制台乱码问题

**执行时间**: 2-3天

---

## 📈 P2级架构完善项（1个月内完成）

### 6. 分布式追踪全链路闭环

**问题描述**:
- TracingConfiguration已存在，但需确认是否全服务启用
- 需验证TraceId在服务间传递是否完整

**当前状态**:
- ✅ `microservices-common-monitor`已实现TracingConfiguration
- ✅ 支持@Observed注解
- ⚠️ 需确认所有服务是否启用追踪

**修复方案**:
```yaml
# 确保所有服务application.yml包含：
management:
  tracing:
    enabled: true
    sampling:
      probability: 1.0  # 生产环境可调整为0.1
```

**验收标准**:
- [ ] 所有9个微服务启用分布式追踪
- [ ] 服务间调用TraceId完整传递
- [ ] Zipkin/Jaeger可完整展示调用链

**执行时间**: 1-2周

---

### 7. 可观测性指标统一

**问题描述**:
- 部分服务已使用@Observed，但需统一指标命名规范
- 需确保关键业务路径都有指标埋点

**修复方案**:
```java
// 统一指标命名规范：{service}.{module}.{operation}
@Observed(name = "consume.payment.process", contextualName = "consume-payment-process")
public ResponseDTO<PaymentResultDTO> processPayment(PaymentRequestDTO request) {
    // ...
}
```

**验收标准**:
- [ ] 所有Service层关键方法使用@Observed
- [ ] 指标命名遵循统一规范
- [ ] Prometheus可采集所有业务指标

**执行时间**: 2-3周

---

### 8. 微服务边界清晰化

**问题描述**:
- 需明确ioedream-database-service的去留决策
- 需确保所有服务职责边界清晰

**修复方案**:
```
1. 架构决策：确认ioedream-database-service是否保留
   - 如果保留：更新架构文档，明确职责
   - 如果下线：功能迁移到ioedream-common-service

2. 服务边界文档化：
   - 每个服务明确职责范围
   - 禁止跨边界直接调用
   - 统一通过GatewayServiceClient
```

**验收标准**:
- [ ] 所有服务职责边界文档化
- [ ] 0个架构违规服务
- [ ] 服务间调用100%通过网关

**执行时间**: 2-3周

---

## 📋 全局一致性检查清单

### 架构一致性检查

- [ ] **7微服务架构严格限制**
  - [ ] 确认所有服务在架构清单中
  - [ ] 移除或隔离非清单服务
  - [ ] 更新架构文档

- [ ] **服务间调用规范**
  - [ ] 0个@EnableFeignClients（白名单除外）
  - [ ] 100%使用GatewayServiceClient
  - [ ] 无跨服务直接数据库访问

- [ ] **四层架构边界**
  - [ ] Controller不直接调用DAO
  - [ ] Service不直接访问数据库
  - [ ] Manager类不使用Spring注解

### 技术栈一致性检查

- [ ] **版本统一管理**
  - [ ] 所有版本在父POM中定义
  - [ ] 子模块不覆盖父POM版本
  - [ ] 文档引用技术栈标准规范

- [ ] **依赖管理**
  - [ ] 统一使用dependencyManagement
  - [ ] 无版本冲突
  - [ ] 依赖版本与标准规范一致

### 代码规范一致性检查

- [ ] **依赖注入规范**
  - [ ] 0个@Autowired（测试类除外）
  - [ ] 100%使用@Resource
  - [ ] 无字段注入（使用setter/构造器）

- [ ] **DAO层规范**
  - [ ] 0个@Repository注解
  - [ ] 100%使用@Mapper注解
  - [ ] 统一使用Dao后缀

- [ ] **包名规范**
  - [ ] 0个javax.*包（除javax.crypto等JDK自带）
  - [ ] 100%使用jakarta.*包
  - [ ] 无包名混用

- [ ] **HTTP方法规范**
  - [ ] 查询接口使用GET
  - [ ] 创建使用POST
  - [ ] 更新使用PUT
  - [ ] 删除使用DELETE

### 开发规范一致性检查

- [ ] **文件编码规范**
  - [ ] PowerShell脚本：UTF-8 without BOM
  - [ ] Java文件：UTF-8
  - [ ] 配置文件：UTF-8

- [ ] **代码行数规范**
  - [ ] 单文件≤400行
  - [ ] 单方法≤50行
  - [ ] 超大文件已拆分

- [ ] **注释规范**
  - [ ] 所有公共方法有JavaDoc
  - [ ] 关键业务逻辑有中文注释
  - [ ] 复杂算法有说明

---

## 🚀 执行计划时间表

### 第1周：P0级紧急修复

| 日期 | 任务 | 负责人 | 验收标准 |
|------|------|--------|---------|
| Day 1-2 | 架构边界违规修复（database-service） | 架构师 | 服务隔离或合规化 |
| Day 3-5 | 配置安全加固（明文密码） | 安全团队 | 0个明文密码 |
| Day 6-7 | 依赖注入规范检查 | 开发团队 | @Autowired违规清单 |

### 第2-3周：P1级快速优化

| 日期 | 任务 | 负责人 | 验收标准 |
|------|------|--------|---------|
| Week 2 | RESTful API重构 | 后端团队 | 查询接口100%使用GET |
| Week 2 | PowerShell编码统一 | DevOps团队 | 脚本编码规范统一 |
| Week 3 | @Autowired批量替换 | 开发团队 | 0个@Autowired |

### 第4周：P2级架构完善

| 日期 | 任务 | 负责人 | 验收标准 |
|------|------|--------|---------|
| Week 4 | 分布式追踪全链路 | 架构团队 | 所有服务启用追踪 |
| Week 4 | 可观测性指标统一 | 开发团队 | 指标命名规范统一 |
| Week 4 | 微服务边界文档化 | 架构团队 | 服务职责边界清晰 |

---

## ✅ 验收标准总览

### P0级验收标准（必须100%通过）

- [ ] **架构合规性**: 0个架构违规服务
- [ ] **配置安全性**: 0个明文密码
- [ ] **依赖注入**: 0个@Autowired（测试类除外）

### P1级验收标准（必须≥95%通过）

- [ ] **RESTful规范**: 查询接口POST滥用率<5%
- [ ] **编码规范**: PowerShell脚本编码统一率100%
- [ ] **代码规范**: @Autowired替换率100%

### P2级验收标准（必须≥90%通过）

- [ ] **分布式追踪**: 服务启用率100%
- [ ] **可观测性**: 关键路径指标覆盖率≥90%
- [ ] **架构文档**: 服务边界文档完整率100%

---

## 🔍 自动化检查脚本

### 全局一致性检查脚本

```powershell
# scripts/check-global-consistency.ps1

# 1. 检查架构违规
Write-Host "[检查] 架构边界违规..." -ForegroundColor Cyan
$feignServices = Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" |
    Select-String -Pattern "@EnableFeignClients" |
    Select-Object -ExpandProperty Path -Unique
if ($feignServices) {
    Write-Host "[违规] 发现Feign使用: $($feignServices.Count)个文件" -ForegroundColor Red
    $feignServices | ForEach-Object { Write-Host "  - $_" -ForegroundColor Yellow }
} else {
    Write-Host "[通过] 无Feign违规" -ForegroundColor Green
}

# 2. 检查@Autowired违规
Write-Host "[检查] @Autowired违规..." -ForegroundColor Cyan
$autowiredFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" |
    Select-String -Pattern "^\s*@Autowired\b" |
    Where-Object { $_.Path -notmatch "Test\.java$" -and $_.Line -notmatch "//|/\*" } |
    Select-Object -ExpandProperty Path -Unique
if ($autowiredFiles) {
    Write-Host "[违规] 发现@Autowired: $($autowiredFiles.Count)个文件" -ForegroundColor Red
} else {
    Write-Host "[通过] 无@Autowired违规" -ForegroundColor Green
}

# 3. 检查配置安全
Write-Host "[检查] 配置安全（明文密码）..." -ForegroundColor Cyan
$plainPasswords = Get-ChildItem -Path "microservices" -Recurse -Include "*.yml","*.yaml","*.properties" |
    Select-String -Pattern "password:\s*["']?[a-zA-Z0-9]{3,}["']?" -CaseSensitive:$false |
    Where-Object { $_.Line -notmatch "ENC\(|\\$\{" } |
    Select-Object -ExpandProperty Path -Unique
if ($plainPasswords) {
    Write-Host "[违规] 发现疑似明文密码: $($plainPasswords.Count)个文件" -ForegroundColor Red
} else {
    Write-Host "[通过] 无明文密码" -ForegroundColor Green
}

# 4. 检查RESTful规范
Write-Host "[检查] RESTful规范（POST滥用）..." -ForegroundColor Cyan
$postQuery = Get-ChildItem -Path "microservices" -Recurse -Filter "*Controller.java" |
    Select-String -Pattern '@PostMapping\("/get|@PostMapping\("/query|@PostMapping\("/list' |
    Select-Object -ExpandProperty Path -Unique
if ($postQuery) {
    Write-Host "[违规] 发现POST查询接口: $($postQuery.Count)个文件" -ForegroundColor Red
} else {
    Write-Host "[通过] 无POST查询接口违规" -ForegroundColor Green
}
```

---

## 📚 相关文档

- [CLAUDE.md - 全局架构规范](../CLAUDE.md)
- [技术栈标准规范](../documentation/technical/PR_REVIEW_TECHNOLOGY_STACK_CHECKLIST.md)
- [开发规范体系](../documentation/technical/repowiki/zh/content/开发规范体系/)
- [阶段2全局分析报告](./anti-fragile-roadmap.md)

---

**最后更新**: 2025-12-14  
**维护团队**: IOE-DREAM 架构委员会  
**执行状态**: 待执行
