# Frontend Organization Compliance Specification

## ADDED Requirements

### Requirement: Frontend Module Organization Standards
**Description**: 建立前端模块组织标准化规范，指导未来的模块开发和维护

#### Scenario: Define Frontend Module Organization Standards
**Given**: 需要建立标准化的前端模块组织规范

**When**: 执行标准制定

**Then**: 标准应该包含：
1. **模块分类标准**：
   ```text
   模块分类体系:
   - system: 系统管理类模块
   - business: 业务功能类模块
   - support: 技术支撑类模块
   - common: 通用功能类模块
   ```

2. **命名规范标准**：
   ```text
   命名规范:
   - 目录名: 小写+连字符 (employee-management)
   - 组件名: PascalCase (EmployeeManagement)
   - 路由路径: 小写+连字符 (/employee-management)
   - 权限标识: 模块:动作:资源 (system:employee:view)
   ```

3. **组织结构标准**：
   ```text
   标准模块结构:
   src/views/{category}/{module}/
   ├── index.vue           # 模块主页面
   ├── components/        # 模块组件
   ├── sub-pages/         # 子页面
   ├── api/              # 模块API
   └── styles/           # 模块样式
   ```

4. **开发规范标准**：
   - 代码组织规范
   - 文件命名规范
   - 注释文档规范
   - 测试覆盖规范

**And**: 标准文档特性：
- 详细的使用说明
- 丰富的代码示例
- 清晰的约束条件
- 实际的应用案例

## MODIFIED Requirements

### Requirement: Consolidate Duplicate Frontend API Directories
**Description**: 解决前端API目录重复问题，统一consume和consumption目录，建立清晰的API组织结构

#### Scenario: Analyze Duplicate API Directory Structure
**Given**: 前端项目中存在API目录重复问题
```text
当前重复结构:
src/api/business/consume/
├── account.js
├── device.js
├── record.js
└── report.js

src/api/business/consumption/
├── account.js
├── device.js
├── record.js
└── report.js
```

**When**: 执行API目录结构分析

**Then**: 系统应该能够：
1. 识别重复的API目录和文件
2. 分析文件内容差异
3. 确定合并策略（保留、合并、替换）
4. 评估引用关系影响
5. 生成详细的合并计划

**And**: 分析报告包含：
- 重复文件清单
- 内容差异分析
- 引用关系图谱
- 合并风险评估

## MODIFIED Requirements

### Requirement: Reorganize Frontend Module Structure
**Description**: 重新组织前端模块结构，将分散的业务模块统一到标准目录结构中

#### Scenario: Analyze Current Frontend Module Distribution
**Given**: 前端模块分散在多个位置
```text
当前分散结构:
src/views/
├── business/                 # 业务功能
│   ├── access/              # 门禁系统
│   ├── attendance/         # 考勤系统
│   ├── consume/            # 消费系统
│   └── consumption/        # 消费系统（重复）
├── smart-permission/        # 门禁权限（重复）
├── area/                   # 区域管理
├── location/               # 位置管理
├── realtime/               # 实时监控
└── system/                 # 系统管理
```

**When**: 执行模块分布分析

**Then**: 分析结果应该识别：
1. **模块重复问题**：
   - smart-permission vs business/access
   - consume vs consumption
   - 功能重叠和冗余

2. **模块分类问题**：
   - 业务模块分散
   - 系统模块混杂
   - 缺乏统一标准

3. **组织结构问题**：
   - 目录层次不清晰
   - 模块粒度不一致
   - 命名规范不统一

**And**: 生成重组计划，包括：
- 模块分类标准
- 迁移路径规划
- 依赖关系分析
- 风险评估报告

## MODIFIED Requirements

### Requirement: Unify Frontend-Backend Module Granularity
**Description**: 统一前后端模块粒度，建立一致的模块组织方式和命名规范

#### Scenario: Analyze Frontend-Backend Module Alignment
**Given**: 需要分析前后端模块对应关系
```text
后端模块结构:
sa-admin/src/main/java/net/lab1024/sa/admin/module/
├── system/                   # 系统管理
├── attendance/              # 考勤系统
├── consume/                  # 消费系统
├── hr/                       # 人力资源
├── oa/                       # 办公自动化
├── smart/                    # 智能设备
│   ├── access/              # 门禁访问
│   ├── biometric/           # 生物识别
│   ├── device/              # 设备管理
│   ├── monitor/             # 设备监控
│   └── video/               # 视频监控
└── ...

前端模块结构（重组前）:
├── business/                 # 不完全对应
├── smart-permission/         # 对应smart/access
├── area/                     # 无直接对应
├── location/                 # 无直接对应
├── realtime/                 # 对应smart/monitor
└── ...
```

**When**: 执行前后端模块对齐分析

**Then**: 分析结果应该识别：
1. **模块映射关系**：
   - 完全对应：consume, attendance
   - 部分对应：smart/* → business/smart/*
   - 无对应：area, location, hr, oa
   - 重复对应：smart-permission → smart/access

2. **粒度不一致问题**：
   - 后端粒度更细
   - 前端粒度较粗
   - 命名规范不统一

3. **组织标准需求**：
   - 前后端命名一致性
   - 模块粒度统一标准
   - 组织层次规范化

**And**: 生成对齐方案，包括：
- 模块映射对照表
- 粒度统一标准
- 命名规范建议
- 实施计划路线图

#### Scenario: Execute Frontend-Backend Module Alignment
**Given**: 前后端模块对齐方案确定

**When**: 执行模块对齐操作

**Then**: 系统应该能够：
1. 统一模块命名规范：
   ```text
   对齐标准:
   - system → system (保持)
   - attendance → attendance (保持)
   - consume → consume (保持)
   - smart/* → business/smart/* (统一前缀)
   - hr → business/hr (新增)
   - oa → business/oa (新增)
   - area → system/area (迁移)
   - location → system/location (迁移)
   ```

2. 补充缺失模块：
   ```javascript
   // 新增hr模块
   src/views/business/hr/
   ├── employee/
   ├── position/
   └── salary/

   // 新增oa模块
   src/views/business/oa/
   ├── document/
   ├── workflow/
   └── notice/
   ```

3. 更新模块配置：
   ```javascript
   // 统一模块配置
   const moduleConfig = {
     system: {
       name: '系统管理',
       icon: 'SettingOutlined',
       children: ['employee', 'department', 'role', 'area', 'location']
     },
     business: {
       name: '业务管理',
       icon: 'AppstoreOutlined',
       children: ['consume', 'attendance', 'access', 'hr', 'oa', 'smart']
     }
   }
   ```

4. 同步权限标识：
   ```javascript
   // 前后端权限标识同步
   const permissionMapping = {
     'system:employee:view': 'system.employee.view',
     'business:consume:manage': 'business.consume.manage',
     'business:smart:monitor': 'business.smart.monitor'
   }
   ```

**And**: 验证对齐结果：
- 前后端模块命名100%一致
- 模块粒度标准统一
- 权限标识完全同步
- 开发文档准确更新

---

## Cross-Reference Relationships

- **frontend-organization-compliance** → **project-structure-compliance**: 前端组织规范化为整体项目结构规范化提供支持
- **api-directory-consolidation** → **module-reorganization**: API整合为模块重组提供基础
- **frontend-backend-alignment** → **development-efficiency-improvement**: 前后端对齐提高开发效率

#### Scenario: Analyze Duplicate API Directory Structure
**Given**: 前端项目中存在API目录重复问题
```text
当前重复结构:
src/api/business/consume/
├── account.js
├── device.js
├── record.js
└── report.js

src/api/business/consumption/
├── account.js
├── device.js
├── record.js
└── report.js
```

**When**: 执行API目录结构分析

**Then**: 系统应该能够：
1. 识别重复的API目录和文件
2. 分析文件内容差异
3. 确定合并策略（保留、合并、替换）
4. 评估引用关系影响
5. 生成详细的合并计划

**And**: 分析报告包含：
- 重复文件清单
- 内容差异分析
- 引用关系图谱
- 合并风险评估

#### Scenario: Execute API Directory Consolidation
**Given**: API目录合并计划确定

**When**: 执行目录整合操作

**Then**: 系统应该能够：
1. 合并重复的API文件：
   ```text
   合并策略:
   - 保留consume目录作为主目录
   - 将consumption目录中的独特内容合并到consume
   - 删除重复的consumption目录
   - 统一API接口命名规范
   ```

2. 更新组件引用：
   ```javascript
   // 更新前
   import { getAccountList } from '@/api/business/consumption/account'
   import { getDeviceList } from '@/api/business/consume/device'

   // 更新后
   import { getAccountList, getDeviceList } from '@/api/business/consume/account'
   import { getDeviceList } from '@/api/business/consume/device'
   ```

3. 验证引用完整性：
   - 扫描所有Vue组件文件
   - 检查import语句更新
   - 验证API调用正确性

4. 构建验证：
   ```bash
   npm run build
   # 确保构建成功，无引用错误
   ```

**And**: 验证结果包括：
- consumption目录完全删除
- consume目录包含所有必要API
- 所有组件引用正确更新
- 前端构建成功
- 功能测试通过

#### Scenario: Validate API Directory Consistency
**Given**: API目录整合完成

**When**: 执行目录一致性验证

**Then**: 验证结果应该确认：
1. **目录结构统一**：
   - 0个重复的API目录
   - consume目录结构完整
   - 文件命名规范统一

2. **引用关系正确**：
   - 0个无效的import引用
   - 所有组件引用正确路径
   - API调用正常工作

3. **功能完整性**：
   - 前端构建100%成功
   - API接口响应正常
   - 用户界面功能正常

---

### Requirement: Reorganize Frontend Module Structure
**Description**: 重新组织前端模块结构，将分散的业务模块统一到标准目录结构中

#### Scenario: Analyze Current Frontend Module Distribution
**Given**: 前端模块分散在多个位置
```text
当前分散结构:
src/views/
├── business/                 # 业务功能
│   ├── access/              # 门禁系统
│   ├── attendance/         # 考勤系统
│   ├── consume/            # 消费系统
│   └── consumption/        # 消费系统（重复）
├── smart-permission/        # 门禁权限（重复）
├── area/                   # 区域管理
├── location/               # 位置管理
├── realtime/               # 实时监控
└── system/                 # 系统管理
```

**When**: 执行模块分布分析

**Then**: 分析结果应该识别：
1. **模块重复问题**：
   - smart-permission vs business/access
   - consume vs consumption
   - 功能重叠和冗余

2. **模块分类问题**：
   - 业务模块分散
   - 系统模块混杂
   - 缺乏统一标准

3. **组织结构问题**：
   - 目录层次不清晰
   - 模块粒度不一致
   - 命名规范不统一

**And**: 生成重组计划，包括：
- 模块分类标准
- 迁移路径规划
- 依赖关系分析
- 风险评估报告

#### Scenario: Execute Frontend Module Reorganization
**Given**: 模块重组计划确定

**When**: 执行模块重组操作

**Then**: 系统应该能够：
1. 按照业务类型重新组织：
   ```text
   标准目标结构:
   src/views/
   ├── system/                   # 系统管理模块
   │   ├── employee/            # 员工管理
   │   ├── department/          # 部门管理
   │   ├── role/                # 角色管理
   │   ├── menu/                # 菜单管理
   │   ├── area/                # 区域管理（迁移）
   │   └── location/            # 位置管理（迁移）
   ├── business/                # 业务功能模块
   │   ├── consume/             # 消费系统
   │   ├── attendance/          # 考勤系统
   │   ├── access/              # 门禁系统
   │   ├── visitor/             # 访客管理
   │   └── smart/               # 智能设备
   │       ├── biometric/       # 生物识别
   │       ├── device/          # 设备管理
   │       ├── monitor/         # 设备监控（迁移）
   │       └── video/           # 视频监控
   ├── support/                 # 支撑功能
   │   ├── api-encrypt/         # API加密
   │   ├── cache/               # 缓存管理
   │   ├── config/              # 配置管理
   │   └── dict/                # 数据字典
   └── common/                  # 通用功能
       ├── error/               # 错误页面
       ├── login/               # 登录页面
       └── layout/              # 布局组件
   ```

2. 执行模块迁移：
   ```bash
   # 迁移示例
   mv src/views/smart-permission/* src/views/business/access/
   mv src/views/area/* src/views/system/area/
   mv src/views/location/* src/views/system/location/
   mv src/views/realtime/* src/views/business/smart/monitor/
   ```

3. 更新路由配置：
   ```javascript
   // router/modules/business.js
   const businessRoutes = [
     {
       path: '/business/consume',
       component: () => import('@/views/business/consume/index.vue')
     },
     {
       path: '/business/access',
       component: () => import('@/views/business/access/index.vue')
     },
     // ... 其他路由配置
   ]
   ```

4. 更新导航菜单：
   ```javascript
   // 菜单配置更新
   const menuConfig = [
     {
       key: 'business',
       title: '业务管理',
       children: [
         { key: 'consume', title: '消费系统', path: '/business/consume' },
         { key: 'access', title: '门禁系统', path: '/business/access' },
         { key: 'smart', title: '智能设备', path: '/business/smart' }
       ]
     }
   ]
   ```

5. 更新权限配置：
   ```javascript
   // 权限标识更新
   const permissions = [
     'business:consume:view',
     'business:access:manage',
     'business:smart:monitor'
   ]
   ```

**And**: 验证迁移结果：
- 模块按照新结构正确组织
- 重复模块完全消除
- 路由配置正确更新
- 导航菜单正确显示
- 权限控制正常工作

#### Scenario: Validate Frontend Module Organization
**Given**: 前端模块重组完成

**When**: 执行模块组织验证

**Then**: 验证结果应该确认：
1. **目录结构规范**：
   - 0个重复模块
   - 100%符合标准结构
   - 模块分类正确

2. **功能访问正常**：
   - 所有路由可正常访问
   - 导航菜单正确显示
   - 权限控制有效
   - 用户界面完整

3. **开发体验改善**：
   - 模块结构清晰易懂
   - 开发定位快速准确
   - 代码维护成本降低

---

### Requirement: Unify Frontend-Backend Module Granularity
**Description**: 统一前后端模块粒度，建立一致的模块组织方式和命名规范

#### Scenario: Analyze Frontend-Backend Module Alignment
**Given**: 需要分析前后端模块对应关系
```text
后端模块结构:
sa-admin/src/main/java/net/lab1024/sa/admin/module/
├── system/                   # 系统管理
├── attendance/              # 考勤系统
├── consume/                  # 消费系统
├── hr/                       # 人力资源
├── oa/                       # 办公自动化
├── smart/                    # 智能设备
│   ├── access/              # 门禁访问
│   ├── biometric/           # 生物识别
│   ├── device/              # 设备管理
│   ├── monitor/             # 设备监控
│   └── video/               # 视频监控
└── ...

前端模块结构（重组前）:
├── business/                 # 不完全对应
├── smart-permission/         # 对应smart/access
├── area/                     # 无直接对应
├── location/                 # 无直接对应
├── realtime/                 # 对应smart/monitor
└── ...
```

**When**: 执行前后端模块对齐分析

**Then**: 分析结果应该识别：
1. **模块映射关系**：
   - 完全对应：consume, attendance
   - 部分对应：smart/* → business/smart/*
   - 无对应：area, location, hr, oa
   - 重复对应：smart-permission → smart/access

2. **粒度不一致问题**：
   - 后端粒度更细
   - 前端粒度较粗
   - 命名规范不统一

3. **组织标准需求**：
   - 前后端命名一致性
   - 模块粒度统一标准
   - 组织层次规范化

**And**: 生成对齐方案，包括：
- 模块映射对照表
- 粒度统一标准
- 命名规范建议
- 实施计划路线图

#### Scenario: Execute Frontend-Backend Module Alignment
**Given**: 前后端模块对齐方案确定

**When**: 执行模块对齐操作

**Then**: 系统应该能够：
1. 统一模块命名规范：
   ```text
   对齐标准：
   - system → system (保持)
   - attendance → attendance (保持)
   - consume → consume (保持)
   - smart/* → business/smart/* (统一前缀)
   - hr → business/hr (新增)
   - oa → business/oa (新增)
   - area → system/area (迁移)
   - location → system/location (迁移)
   ```

2. 补充缺失模块：
   ```javascript
   // 新增hr模块
   src/views/business/hr/
   ├── employee/
   ├── position/
   └── salary/

   // 新增oa模块
   src/views/business/oa/
   ├── document/
   ├── workflow/
   └── notice/
   ```

3. 更新模块配置：
   ```javascript
   // 统一模块配置
   const moduleConfig = {
     system: {
       name: '系统管理',
       icon: 'SettingOutlined',
       children: ['employee', 'department', 'role', 'area', 'location']
     },
     business: {
       name: '业务管理',
       icon: 'AppstoreOutlined',
       children: ['consume', 'attendance', 'access', 'hr', 'oa', 'smart']
     }
   }
   ```

4. 同步权限标识：
   ```javascript
   // 前后端权限标识同步
   const permissionMapping = {
     'system:employee:view': 'system.employee.view',
     'business:consume:manage': 'business.consume.manage',
     'business:smart:monitor': 'business.smart.monitor'
   }
   ```

**And**: 验证对齐结果：
- 前后端模块命名100%一致
- 模块粒度标准统一
- 权限标识完全同步
- 开发文档准确更新

---

## ADDED Requirements

### Requirement: Frontend Module Organization Standards
**Description**: 建立前端模块组织标准化规范，指导未来的模块开发和维护

#### Scenario: Define Frontend Module Organization Standards
**Given**: 需要建立标准化的前端模块组织规范

**When**: 执行标准制定

**Then**: 标准应该包含：
1. **模块分类标准**：
   ```text
   模块分类体系:
   - system: 系统管理类模块
   - business: 业务功能类模块
   - support: 技术支撑类模块
   - common: 通用功能类模块
   ```

2. **命名规范标准**：
   ```text
   命名规范:
   - 目录名: 小写+连字符 (employee-management)
   - 组件名: PascalCase (EmployeeManagement)
   - 路由路径: 小写+连字符 (/employee-management)
   - 权限标识: 模块:动作:资源 (system:employee:view)
   ```

3. **组织结构标准**：
   ```text
   标准模块结构:
   src/views/{category}/{module}/
   ├── index.vue           # 模块主页面
   ├── components/        # 模块组件
   ├── sub-pages/         # 子页面
   ├── api/              # 模块API
   └── styles/           # 模块样式
   ```

4. **开发规范标准**：
   - 代码组织规范
   - 文件命名规范
   - 注释文档规范
   - 测试覆盖规范

**And**: 标准文档特性：
- 详细的使用说明
- 丰富的代码示例
- 清晰的约束条件
- 实际的应用案例

---

## Cross-Reference Relationships

- **frontend-organization-compliance** → **project-structure-compliance**: 前端组织规范化为整体项目结构规范化提供支持
- **api-directory-consolidation** → **module-reorganization**: API整合为模块重组提供基础
- **frontend-backend-alignment** → **development-efficiency-improvement**: 前后端对齐提高开发效率

---

## Implementation Considerations

### 迁移策略
1. **渐进式迁移**: 分阶段执行，降低风险
2. **向后兼容**: 保持API接口稳定性
3. **功能验证**: 每步操作后验证功能
4. **用户测试**: 重要变更进行用户测试

### 风险控制
1. **完整备份**: 操作前备份所有文件
2. **版本控制**: 使用Git分支管理
3. **回滚机制**: 准备快速回滚方案
4. **团队沟通**: 及时通知变更计划

### 质量保证
1. **自动化测试**: 建立前端测试套件
2. **代码审查**: 变更代码需要审查
3. **用户验收**: 重要功能需要用户确认
4. **文档更新**: 同步更新相关文档

---

**Specification Version**: v1.0
**Last Updated**: 2025-11-24
**Status**: Ready for Implementation