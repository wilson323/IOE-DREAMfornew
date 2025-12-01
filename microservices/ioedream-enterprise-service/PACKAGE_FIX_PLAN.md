# 包名修复计划

## 问题分析

文件路径与包名声明不匹配：
- 文件路径：`net/lab1024/sa/enterprise/oa/*`
- 包声明：`net.lab1024.sa.oa.*` ❌

## 已修复的文件 ✅

1. ✅ `DocumentManager.java` - 包名和导入已修复
2. ✅ `DocumentService.java` (service目录) - 包名和导入已修复
3. ✅ `DocumentServiceImpl.java` - 包名和导入已修复
4. ✅ `OaApplication.java` - 包名已修复
5. ✅ `MeetingManagementService.java` - 包名已修复
6. ✅ `WorkflowController.java` - 包名和导入已修复
7. ✅ `WorkflowEngineService.java` - 包名和导入已修复
8. ✅ `WorkflowEngineServiceImpl.java` - 包名和导入已修复
9. ✅ `DocumentController.java` (controller目录) - 包名已修复
10. ✅ `ApprovalProcessService.java` - 包名已修复
11. ✅ `WorkflowEngine.java` - 包名已修复（已在正确位置）

## 待处理问题 ⚠️

### 文件位置不匹配问题

以下文件在错误的目录路径下，但包名声明是正确的：

1. **WorkflowInstanceEntity.java**
   - 当前位置：`net/lab1024/sa/oa/workflow/domain/entity/`
   - 包声明：`net.lab1024.sa.enterprise.oa.workflow.domain.entity` ✅
   - **解决方案**：文件需要移动到 `net/lab1024/sa/enterprise/oa/workflow/domain/entity/`

2. **WorkflowTaskEntity.java**
   - 当前位置：`net/lab1024/sa/oa/workflow/domain/entity/`
   - 包声明：`net.lab1024.sa.enterprise.oa.workflow.domain.entity` ✅
   - **解决方案**：文件需要移动到 `net/lab1024/sa/enterprise/oa/workflow/domain/entity/`

### 待修复的导入路径

以下文件中的导入路径需要从 `net.lab1024.sa.oa.*` 改为 `net.lab1024.sa.enterprise.oa.*`：
- 所有使用这些实体类的文件的导入路径

## 修复步骤

1. ✅ 修复所有包声明（已完成）
2. ⏳ 修复所有导入路径（进行中）
3. ⚠️ 移动实体类文件到正确位置（需要手动操作或使用 git mv）

