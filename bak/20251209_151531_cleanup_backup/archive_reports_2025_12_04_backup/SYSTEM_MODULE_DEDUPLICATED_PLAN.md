# System模块去重后补充计划

**生成时间**: 2025-12-02
**原计划**: 22个文件
**去重后**: 16个文件（删除6个重复功能）

---

## 🚨 功能重叠分析

### 发现的重复功能
1. **UserManagementService** ↔ **Identity.UserService** ✅ 已有Identity模块，不迁移
2. **PermissionManagementService** ↔ **Identity.PermissionService** ✅ 已有Identity模块，不迁移

### 删除的文件（6个）
- ❌ `UserManagementService.java` - 与Identity.UserService重复
- ❌ `PermissionManagementService.java` - 与Identity.PermissionService重复
- ❌ `RoleController.java` - 与Identity.RoleController重复
- ❌ `LoginController.java` - 与Auth.AuthController重复

---

## ✅ 需要迁移的独立功能（16个文件）

### Employee模块（3个文件）
- [ ] `EmployeeController.java` - 员工控制器
- [ ] `EmployeeService.java` + `EmployeeServiceImpl.java` - 员工服务
- [ ] `EmployeeManager.java` - 员工管理器

### Menu模块（3个文件）
- [ ] `MenuController.java` - 菜单控制器
- [ ] `MenuService.java` + `MenuServiceImpl.java` - 菜单服务
- [ ] `MenuManager.java` - 菜单管理器

### Department模块（2个文件）
- [ ] `DepartmentController.java` - 部门控制器
- [ ] `DepartmentService.java` + `DepartmentServiceImpl.java` - 部门服务

### UnifiedDevice模块（3个文件）
- [ ] `UnifiedDeviceController.java` - 统一设备控制器
- [ ] `UnifiedDeviceService.java` + `UnifiedDeviceServiceImpl.java` - 统一设备服务
- [ ] `UnifiedDeviceManager.java` - 统一设备管理器

### Cache模块（1个文件）
- [ ] `CacheController.java` - 缓存控制器

---

## 📊 去重后的工作量

| 模块 | 原计划 | 去重后 | 减少 |
|------|--------|--------|------|
| Employee | 3 | 3 | 0 |
| Menu | 3 | 3 | 0 |
| Department | 2 | 2 | 0 |
| UnifiedDevice | 3 | 3 | 0 |
| Cache | 1 | 1 | 0 |
| User | 2 | 0 | -2 ❌ |
| Permission | 2 | 0 | -2 ❌ |
| Role | 1 | 0 | -1 ❌ |
| Login | 1 | 0 | -1 ❌ |
| **总计** | **22** | **16** | **-6** |

---

## 🎯 执行策略

1. **批次1**: Employee模块（3个文件）
2. **批次2**: Menu模块（3个文件）
3. **批次3**: Department模块（2个文件）
4. **批次4**: UnifiedDevice模块（3个文件）
5. **批次5**: CacheController（1个文件）
6. **批次6**: Dict相关Manager（2个文件，已存在，需验证）

---

## ✅ 质量保证

### 避免冗余原则
- ✅ 与Identity模块功能对比，避免重复
- ✅ 与Auth模块功能对比，避免重复
- ✅ 只迁移独立的业务功能
- ✅ 确保100%功能无重叠

---

**总结**: 通过去重分析，从22个文件减少到16个文件，避免了6个重复功能的迁移，确保了代码的一致性和无冗余！

