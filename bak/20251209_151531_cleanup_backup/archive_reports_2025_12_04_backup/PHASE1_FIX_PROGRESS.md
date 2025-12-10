# Phase 1 导入路径修复进度追踪

**开始时间**: 2025-12-03
**当前状态**: 进行中
**总文件数**: 19个
**已修复**: 1个（5.3%）
**剩余**: 18个

---

## 📋 需要修复的文件列表

### DAO层文件（优先级P0）
1. [x] `AntiPassbackRuleDao.java` - ✅ **已修复**
   - 旧导入: `import net.lab1024.sa.access.advanced.domain.entity.AntiPassbackRuleEntity;`
   - 新导入: `import net.lab1024.sa.common.access.entity.AntiPassbackRuleEntity;`

2. [ ] `InterlockRuleDao.java` - ⏳ 待修复
3. [ ] `LinkageRuleDao.java` (access/advanced/dao) - ⏳ 待修复
4. [ ] `LinkageRuleDao.java` (access/dao) - ⏳ 待修复
5. [ ] `VisitorReservationDao.java` - ⏳ 待修复

### Service层文件（优先级P1）
6. [ ] `AdvancedAccessControlService.java` - ⏳ 待修复
7. [ ] `LinkageRuleServiceImpl.java` - ⏳ 待修复
8. [ ] `InterlockLogServiceImpl.java` - ⏳ 待修复
9. [ ] `InterlockRuleServiceImpl.java` - ⏳ 待修复
10. [ ] `AccessApprovalServiceImpl.java` - ⏳ 待修复
11. [ ] `LinkageRuleService.java` - ⏳ 待修复
12. [ ] `InterlockRuleService.java` - ⏳ 待修复
13. [ ] `EvacuationService.java` - ⏳ 待修复

### Manager层文件（优先级P1）
14. [ ] `LinkageRuleManagerImpl.java` - ⏳ 待修复
15. [ ] `LinkageRuleManager.java` - ⏳ 待修复

### Controller层文件（优先级P2）
16. [ ] `AdvancedAccessControlController.java` - ⏳ 待修复

### 工具类文件（优先级P2）
17. [ ] `InterlockLogConverter.java` - ⏳ 待修复

### 引擎类文件（优先级P1）
18. [ ] `GlobalLinkageEngine.java` - ⏳ 待修复
19. [ ] `GlobalInterlockEngine.java` - ⏳ 待修复

---

## 📊 修复统计

| 层级 | 总数 | 已修复 | 待修复 | 进度 |
|------|------|--------|--------|------|
| DAO层 | 5 | 1 | 4 | 20% |
| Service层 | 8 | 0 | 8 | 0% |
| Manager层 | 2 | 0 | 2 | 0% |
| Controller层 | 1 | 0 | 1 | 0% |
| 工具类 | 1 | 0 | 1 | 0% |
| 引擎类 | 2 | 0 | 2 | 0% |
| **总计** | **19** | **1** | **18** | **5.3%** |

---

## 🔄 修复模式

### 标准替换模式
```java
// 旧导入（错误）
import net.lab1024.sa.access.advanced.domain.entity.{EntityName};
import net.lab1024.sa.access.approval.domain.entity.{EntityName};
import net.lab1024.sa.access.domain.entity.{EntityName};

// 新导入（正确）
import net.lab1024.sa.common.access.entity.{EntityName};
```

### 涉及的Entity类
- AntiPassbackRecordEntity
- AntiPassbackRuleEntity
- InterlockLogEntity
- InterlockRuleEntity
- LinkageRuleEntity
- EvacuationRecordEntity
- EvacuationPointEntity
- EvacuationEventEntity
- AreaAccessExtEntity
- AntiPassbackEntity
- AccessEventEntity
- AccessRuleEntity
- ApprovalRequestEntity
- InterlockGroupEntity
- ApprovalProcessEntity
- DeviceMonitorEntity
- VisitorReservationEntity

---

## ⏭️ 下一步计划

1. **继续修复DAO层文件** (剩余4个)
2. **修复Service层文件** (8个)
3. **修复Manager层文件** (2个)
4. **修复其他文件** (4个)
5. **编译验证**
6. **删除重复Entity类**

---

## 📝 注意事项

1. **手动修复** - 每个文件单独修复，禁止批量脚本
2. **逐个验证** - 每修复1-2个文件就编译验证
3. **保持记录** - 及时更新此文档
4. **回滚准备** - 每个文件修改前确保可回滚

---

**执行人**: AI架构师团队
**更新频率**: 实时更新
**预计完成时间**: Phase 1.2 - 第2-3天

