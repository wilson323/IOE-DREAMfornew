# 紧急文件恢复指南

## ⚠️ 紧急情况

发现大量核心业务文件被删除，需要立即恢复！

## 被删除的文件清单

### Controller层（6个文件）
1. `ConsumeController.java` - 消费管理主控制器
2. `ConsumptionModeController.java` - 消费模式控制器
3. `RechargeController.java` - 充值控制器
4. `RefundController.java` - 退款控制器
5. `AccountController.java` - 账户控制器
6. `IndexOptimizationController.java` - 索引优化控制器

### Service层（8个文件）
1. `ConsumeServiceImpl.java` - 消费服务实现
2. `RechargeService.java` - 充值服务
3. `IndexOptimizationService.java` - 索引优化服务
4. `AbnormalDetectionServiceImpl.java` - 异常检测服务实现
5. `ReconciliationService.java` - 对账服务
6. `WechatPaymentService.java` - 微信支付服务
7. `SecurityNotificationServiceImpl.java` - 安全通知服务实现
8. `ConsumePermissionService.java` - 消费权限服务

### 测试文件（2个文件）
1. `ConsumeIntegrationTest.java` - 集成测试
2. `ConsumePerformanceTest.java` - 性能测试

## 恢复步骤

### 方法1：从Git恢复所有文件（推荐）

在PowerShell中执行以下命令：

```powershell
# 切换到项目目录
cd D:\IOE-DREAM\microservices\ioedream-consume-service

# 恢复Controller层文件
git checkout HEAD -- src/main/java/net/lab1024/sa/consume/controller/ConsumeController.java
git checkout HEAD -- src/main/java/net/lab1024/sa/consume/controller/ConsumptionModeController.java
git checkout HEAD -- src/main/java/net/lab1024/sa/consume/controller/RechargeController.java
git checkout HEAD -- src/main/java/net/lab1024/sa/consume/controller/RefundController.java
git checkout HEAD -- src/main/java/net/lab1024/sa/consume/controller/AccountController.java
git checkout HEAD -- src/main/java/net/lab1024/sa/consume/controller/IndexOptimizationController.java

# 恢复Service层文件
git checkout HEAD -- src/main/java/net/lab1024/sa/consume/service/impl/ConsumeServiceImpl.java
git checkout HEAD -- src/main/java/net/lab1024/sa/consume/service/RechargeService.java
git checkout HEAD -- src/main/java/net/lab1024/sa/consume/service/IndexOptimizationService.java
git checkout HEAD -- src/main/java/net/lab1024/sa/consume/service/impl/AbnormalDetectionServiceImpl.java
git checkout HEAD -- src/main/java/net/lab1024/sa/consume/service/consistency/ReconciliationService.java
git checkout HEAD -- src/main/java/net/lab1024/sa/consume/service/payment/WechatPaymentService.java
git checkout HEAD -- src/main/java/net/lab1024/sa/consume/service/impl/SecurityNotificationServiceImpl.java
git checkout HEAD -- src/main/java/net/lab1024/sa/consume/service/ConsumePermissionService.java

# 恢复测试文件
git checkout HEAD -- src/test/java/net/lab1024/sa/consume/integration/ConsumeIntegrationTest.java
git checkout HEAD -- src/test/java/net/lab1024/sa/consume/performance/ConsumePerformanceTest.java
```

### 方法2：批量恢复整个目录

```powershell
# 恢复整个controller目录（会恢复所有文件，包括已存在的）
git checkout HEAD -- src/main/java/net/lab1024/sa/consume/controller/

# 恢复整个service目录
git checkout HEAD -- src/main/java/net/lab1024/sa/consume/service/

# 恢复整个test目录
git checkout HEAD -- src/test/java/net/lab1024/sa/consume/
```

### 方法3：从特定提交恢复

如果HEAD中没有这些文件，可以从历史提交中恢复：

```powershell
# 查找包含这些文件的提交
git log --all --full-history --oneline -- "src/main/java/net/lab1024/sa/consume/controller/ConsumeController.java"

# 从特定提交恢复（替换<commit-hash>为实际的提交哈希）
git checkout <commit-hash> -- src/main/java/net/lab1024/sa/consume/controller/ConsumeController.java
```

## 验证恢复

恢复后，请验证：

1. **检查文件是否存在**
```powershell
# 检查Controller文件
Get-ChildItem -Path "src/main/java/net/lab1024/sa/consume/controller" -Filter "*.java" | Select-Object Name

# 检查Service文件
Get-ChildItem -Path "src/main/java/net/lab1024/sa/consume/service" -Recurse -Filter "*.java" | Select-Object FullName
```

2. **检查文件内容**
- 打开几个关键文件，确认内容完整
- 检查是否有编码问题

3. **尝试编译**
```powershell
mvn clean compile -DskipTests
```

## 预防措施

### 立即执行
1. **提交当前状态**（如果文件已恢复）
```powershell
git add .
git commit -m "恢复被删除的核心业务文件"
```

2. **创建备份分支**
```powershell
git checkout -b backup-before-cleanup
git push origin backup-before-cleanup
```

### 长期措施
1. **建立文件保护清单**
   - 列出所有关键业务文件
   - 在删除前检查清单

2. **使用Git分支进行清理**
   - 在独立分支进行清理操作
   - 确认无误后再合并

3. **定期备份**
   - 定期提交代码
   - 重要操作前创建备份分支

## 注意事项

⚠️ **重要提醒**：
- 恢复文件前，请确认当前工作已保存
- 如果文件已被修改，恢复会覆盖当前修改
- 建议先创建备份分支

## 后续工作

文件恢复后，需要：
1. ✅ 验证所有文件完整性
2. ✅ 检查编码问题
3. ✅ 重新编译验证
4. ✅ 重新评估清理策略
5. ✅ 建立文件保护机制

---

**创建时间**：2025-01-30  
**紧急程度**：🔴 极高  
**建议**：立即执行恢复操作

