# System-Service 迁移计划

## 迁移目标
将 `ioedream-system-service` 的所有功能迁移到 `microservices-common` 和 `ioedream-common-service`

## 迁移模块清单

### 1. Dict（字典管理）✅ 实体已存在
- [x] DictTypeEntity - 已存在
- [x] DictDataEntity - 已存在
- [ ] DictTypeDao - 需迁移
- [ ] DictDataDao - 需迁移
- [ ] DictTypeService - 需迁移
- [ ] DictDataService - 需迁移
- [ ] DictTypeManager - 需迁移
- [ ] DictDataManager - 需迁移
- [ ] DictController - 需创建到common-service

### 2. Config（配置管理）✅ 实体已存在
- [x] ConfigEntity - 已存在
- [x] ConfigDao - 已存在（基础方法）
- [ ] ConfigService - 需迁移
- [ ] ConfigManager - 需迁移
- [ ] ConfigController - 需创建到common-service

### 3. Department（部门管理）✅ 实体已存在
- [x] DepartmentEntity - 已存在
- [x] DepartmentDao - 已存在（基础方法）
- [ ] DepartmentService - 需迁移
- [ ] DepartmentManager - 需迁移
- [ ] DepartmentController - 需创建到common-service

### 4. Employee（员工管理）✅ 实体已存在
- [x] EmployeeEntity - 已存在
- [x] EmployeeDao - 已存在（基础方法）
- [ ] EmployeeService - 需迁移
- [ ] EmployeeManager - 需迁移
- [ ] EmployeeController - 需创建到common-service

### 5. Menu（菜单管理）
- [ ] MenuEntity - 需迁移
- [ ] MenuDao - 需迁移
- [ ] MenuService - 需迁移
- [ ] MenuManager - 需迁移
- [ ] MenuController - 需创建到common-service

### 6. Role（角色管理）✅ 实体已存在
- [x] RoleEntity - 已存在（在security模块）
- [x] RoleDao - 已存在（在security模块）
- [ ] RoleService - 需检查是否已存在
- [ ] RoleController - 需创建到common-service

### 7. Cache（缓存管理）
- [ ] CacheController - 需创建到common-service

### 8. Login（登录管理）
- [ ] LoginController - 需创建到common-service

## 迁移步骤

### Phase 1: DAO层迁移（当前阶段）
1. 补充DictTypeDao和DictDataDao的自定义方法
2. 补充ConfigDao的自定义方法
3. 补充DepartmentDao的自定义方法
4. 补充EmployeeDao的自定义方法
5. 迁移MenuDao

### Phase 2: Manager层迁移
1. 迁移DictTypeManager和DictDataManager
2. 迁移ConfigManager
3. 迁移DepartmentManager
4. 迁移EmployeeManager
5. 迁移MenuManager

### Phase 3: Service层迁移
1. 迁移DictTypeService和DictDataService
2. 迁移ConfigService
3. 迁移DepartmentService
4. 迁移EmployeeService
5. 迁移MenuService

### Phase 4: Controller层创建
在 `ioedream-common-service` 中创建所有Controller

### Phase 5: 测试和验证
1. 单元测试（80%覆盖率）
2. 集成测试
3. 性能测试

## 迁移原则
1. 严格遵循CLAUDE.md规范
2. 禁止代码冗余
3. 确保全局一致性
4. 100%功能完整性验证

