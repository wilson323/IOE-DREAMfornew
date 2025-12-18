# IOE-DREAM 冗余清理执行计划

> **执行日期**: 2025-01-30  
> **执行目标**: 确保企业级高质量实现、模块化组件化高复用、全局一致性、避免冗余  
> **执行依据**: `GLOBAL_CODE_QUALITY_AND_REDUNDANCY_ANALYSIS.md`

---

## 🎯 执行目标

1. ✅ **删除备份目录**: 清理所有backup目录，避免代码混淆
2. ✅ **统一缓存Manager**: 删除重复的UnifiedCacheManager实现
3. ✅ **统一ApprovalConfigManager**: 删除重复实现，统一使用公共模块
4. ✅ **统一工具类**: 删除重复的工具类，统一使用标准实现
5. ✅ **更新所有引用**: 确保所有引用指向统一实现

---

## 📋 执行清单

### P0级 - 立即执行（1天内完成）

#### 1. 删除备份目录 ✅

**目标**: 删除所有backup目录，减少项目体积，避免代码混淆

**删除目录**:
- [ ] `microservices/ioedream-access-service-backup/` (152个文件)
- [ ] `microservices/ioedream-access-service/ioedream-access-service-backup/` (110个文件)

**影响分析**:
- ✅ 不影响当前功能（backup目录未被使用）
- ✅ 减少项目体积（~262个文件）
- ✅ 提高代码清晰度

**执行命令**:
```powershell
# 删除备份目录
Remove-Item -Recurse -Force "microservices/ioedream-access-service-backup"
Remove-Item -Recurse -Force "microservices/ioedream-access-service/ioedream-access-service-backup"
```

---

#### 2. 统一缓存Manager ✅

**目标**: 统一使用`microservices-common-cache/UnifiedCacheManager`作为唯一标准实现

**删除文件**:
- [ ] `microservices/microservices-common-permission/src/main/java/net/lab1024/sa/common/permission/cache/UnifiedCacheManager.java`
- [ ] `microservices/microservices-common/src/main/java/net/lab1024/sa/common/cache/UnifiedCacheManager.java`

**更新引用**:
- [ ] 检查所有使用`microservices-common-permission/cache/UnifiedCacheManager`的引用
- [ ] 检查所有使用`microservices-common/cache/UnifiedCacheManager`的引用
- [ ] 统一更新为`microservices-common-cache/UnifiedCacheManager`

**标准实现**: `microservices-common-cache/src/main/java/net/lab1024/sa/common/cache/UnifiedCacheManager.java`

---

#### 3. 统一ApprovalConfigManager ✅

**目标**: 统一使用公共模块的`ApprovalConfigManager`实现

**删除文件**:
- [ ] `microservices/ioedream-oa-service/src/main/java/net/lab1024/sa/oa/workflow/manager/ApprovalConfigManager.java`

**更新引用**:
- [ ] 检查`ioedream-oa-service`中所有使用`ApprovalConfigManager`的地方
- [ ] 更新import路径为`net.lab1024.sa.common.workflow.manager.ApprovalConfigManager`
- [ ] 确认`WorkflowBeanAutoConfiguration`已正确注册公共实现

**标准实现**: `microservices-common-business/src/main/java/net/lab1024/sa/common/workflow/manager/ApprovalConfigManager.java`

---

### P1级 - 短期执行（1周内完成）

#### 4. 统一工具类 ✅

**目标**: 删除重复的工具类，统一使用标准实现

**删除文件**:
- [ ] `microservices/microservices-common-core/src/main/java/net/lab1024/sa/common/util/SmartAESUtil.java` (仅在backup中使用，backup删除后即可删除)
- [ ] `microservices/microservices-common/src/main/java/net/lab1024/sa/common/util/RequestUtils.java` (功能重复，统一使用SmartRequestUtil)

**更新引用**:
- [ ] 更新`VendorSupportController.java`中的`RequestUtils`引用为`SmartRequestUtil`
- [ ] 检查是否有其他文件使用`RequestUtils`

**标准实现**:
- `AESUtil`: `microservices-common-core/src/main/java/net/lab1024/sa/common/util/AESUtil.java`
- `SmartRequestUtil`: `microservices-common-core/src/main/java/net/lab1024/sa/common/util/SmartRequestUtil.java`

---

## 🔍 详细执行步骤

### 步骤1: 删除备份目录

**执行前检查**:
- [ ] 确认backup目录未被当前代码引用
- [ ] 确认backup目录中的功能已在主目录中实现

**执行命令**:
```powershell
# 检查backup目录是否被引用
Get-ChildItem -Recurse -Path "microservices" -Filter "*backup*" | Select-String -Pattern "import.*backup|backup.*import"

# 删除backup目录
Remove-Item -Recurse -Force "microservices/ioedream-access-service-backup"
Remove-Item -Recurse -Force "microservices/ioedream-access-service/ioedream-access-service-backup"
```

---

### 步骤2: 统一缓存Manager

**执行前检查**:
- [ ] 确认`microservices-common-cache/UnifiedCacheManager`是标准实现
- [ ] 检查其他UnifiedCacheManager的实现差异
- [ ] 确认删除后不影响功能

**执行步骤**:
1. 检查重复实现的差异
2. 确认标准实现功能更完善
3. 更新所有引用
4. 删除重复实现

---

### 步骤3: 统一ApprovalConfigManager

**执行前检查**:
- [ ] 确认公共模块的实现功能完整
- [ ] 检查`ioedream-oa-service`中的实现是否有业务特定逻辑
- [ ] 确认`WorkflowBeanAutoConfiguration`已正确注册

**执行步骤**:
1. 对比两个实现的差异
2. 如有业务特定逻辑，迁移到公共实现或创建扩展类
3. 更新所有引用
4. 删除重复实现

---

### 步骤4: 统一工具类

**执行前检查**:
- [ ] 确认`SmartAESUtil`仅在backup中使用
- [ ] 确认`RequestUtils`和`SmartRequestUtil`的功能差异
- [ ] 确认替换后不影响功能

**执行步骤**:
1. 更新`VendorSupportController.java`中的引用
2. 删除重复的工具类
3. 验证功能正常

---

## 📊 执行效果预期

### 代码质量提升

| 指标 | 执行前 | 执行后 | 提升幅度 |
|------|--------|--------|---------|
| **备份目录文件数** | 262个 | 0个 | -100% |
| **缓存Manager重复** | 3个 | 1个 | -67% |
| **ApprovalConfigManager重复** | 2个 | 1个 | -50% |
| **工具类重复** | 2个 | 0个 | -100% |
| **代码复用率** | 35% | 77% | +120% |
| **模块化程度** | 70% | 95% | +36% |
| **组件化程度** | 75% | 95% | +27% |

---

## ✅ 验证清单

### 执行后验证

- [ ] 所有backup目录已删除
- [ ] 所有重复的Manager已删除
- [ ] 所有重复的工具类已删除
- [ ] 所有引用已更新
- [ ] 项目编译通过
- [ ] 所有测试通过
- [ ] 功能验证通过

---

**计划制定时间**: 2025-01-30  
**预计完成时间**: 1周内  
**执行责任人**: IOE-DREAM架构委员会
