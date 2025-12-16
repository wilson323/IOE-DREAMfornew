# 系统管理API

<cite>
**本文档引用文件**  
- [DepartmentController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\department\controller\DepartmentController.java)
- [EmployeeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\employee\controller\EmployeeController.java)
- [MenuController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\menu\controller\MenuController.java)
- [department-api.js](file://smart-admin-web-javascript\src\api\system\department-api.js)
- [employee-api.js](file://smart-admin-web-javascript\src\api\system\employee-api.js)
- [menu-api.js](file://smart-admin-web-javascript\src\api\system\menu-api.js)
- [DepartmentAddForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\department\domain\form\DepartmentAddForm.java)
- [DepartmentUpdateForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\department\domain\form\DepartmentUpdateForm.java)
- [EmployeeAddForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\employee\domain\form\EmployeeAddForm.java)
- [EmployeeUpdateForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\employee\domain\form\EmployeeUpdateForm.java)
- [MenuAddForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\menu\domain\form\MenuAddForm.java)
- [MenuUpdateForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\menu\domain\form\MenuUpdateForm.java)
- [MenuBaseForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\menu\domain\form\MenuBaseForm.java)
- [DepartmentVO.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\department\domain\vo\DepartmentVO.java)
- [EmployeeVO.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\employee\domain\vo\EmployeeVO.java)
- [MenuVO.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\menu\domain\vo\MenuVO.java)
- [MenuTypeEnum.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\menu\constant\MenuTypeEnum.java)
- [menu-const.js](file://smart-admin-web-javascript\src\constants\system\menu-const.js)
- [employee-const.js](file://smart-admin-web-javascript\src\constants\system\employee-const.js)
</cite>

## 目录
1. [部门管理API](#部门管理api)
2. [员工管理API](#员工管理api)
3. [菜单管理API](#菜单管理api)
4. [前端调用示例](#前端调用示例)
5. [树形结构处理逻辑](#树形结构处理逻辑)
6. [权限关联接口调用顺序](#权限关联接口调用顺序)

## 部门管理API

部门管理API提供对组织架构的完整操作功能，支持树形结构的增删改查、排序和状态变更操作。所有接口均需具备相应的权限才能访问。

### 部门树形结构查询
获取部门的树形结构列表，包含完整的层级关系和排序信息。

- **HTTP方法**: GET
- **URL路径**: `/department/treeList`
- **权限要求**: 无需特定权限（已登录即可）
- **请求参数**: 无
- **响应格式**: `ResponseDTO<List<DepartmentTreeVO>>`
- **响应示例**:
```json
{
  "code": 0,
  "data": [
    {
      "departmentId": 1,
      "departmentName": "总公司",
      "parentId": 0,
      "sort": 1,
      "managerId": 1001,
      "managerName": "张总",
      "createTime": "2023-01-01T10:00:00",
      "updateTime": "2023-01-01T10:00:00",
      "preId": null,
      "nextId": 2,
      "children": [
        {
          "departmentId": 2,
          "departmentName": "技术部",
          "parentId": 1,
          "sort": 1,
          "managerId": 1002,
          "managerName": "李经理",
          "createTime": "2023-01-01T10:00:00",
          "updateTime": "2023-01-01T10:00:00",
          "preId": null,
          "nextId": 3,
          "children": []
        }
      ],
      "selfAndAllChildrenIdList": [1, 2, 3]
    }
  ],
  "msg": "OK"
}
```

### 添加部门
创建新的部门节点，支持指定上级部门、排序位置和负责人。

- **HTTP方法**: POST
- **URL路径**: `/department/add`
- **权限要求**: `system:department:add`
- **请求参数**: 请求体（JSON）
- **请求体结构**:
```json
{
  "departmentName": "string", // 部门名称 (1-50字符，必填)
  "sort": 0, // 排序值 (必填)
  "managerId": 0, // 部门负责人ID (可选)
  "parentId": 0 // 上级部门ID (可选，0表示顶级部门)
}
```
- **响应格式**: `ResponseDTO<String>`
- **业务约束**:
  - 部门名称不能为空且长度在1-50个字符之间
  - 排序值不能为空
  - 同一父级下的部门名称不能重复
  - 指定的负责人必须是有效的员工ID

### 更新部门
修改现有部门的信息，包括名称、排序、负责人等。

- **HTTP方法**: POST
- **URL路径**: `/department/update`
- **权限要求**: `system:department:update`
- **请求参数**: 请求体（JSON）
- **请求体结构**:
```json
{
  "departmentId": 0, // 部门ID (必填)
  "departmentName": "string", // 部门名称 (1-50字符，必填)
  "sort": 0, // 排序值 (必填)
  "managerId": 0, // 部门负责人ID (可选)
  "parentId": 0 // 上级部门ID (可选)
}
```
- **响应格式**: `ResponseDTO<String>`
- **业务约束**:
  - 部门ID必须存在
  - 部门名称不能为空且长度在1-50个字符之间
  - 排序值不能为空
  - 不能将部门移动到其自身的子部门下（防止循环引用）
  - 同一父级下的部门名称不能重复

### 删除部门
删除指定的部门节点。

- **HTTP方法**: GET
- **URL路径**: `/department/delete/{departmentId}`
- **权限要求**: `system:department:delete`
- **路径参数**:
  - `departmentId`: 要删除的部门ID
- **响应格式**: `ResponseDTO<String>`
- **业务约束**:
  - 部门ID必须存在
  - 不能删除包含子部门的部门（需先删除所有子部门）
  - 不能删除仍有关联员工的部门（需先调整员工部门分配）
  - 系统预置的顶级部门不能删除

### 部门列表查询
获取所有部门的扁平化列表。

- **HTTP方法**: GET
- **URL路径**: `/department/listAll`
- **权限要求**: 无需特定权限（已登录即可）
- **请求参数**: 无
- **响应格式**: `ResponseDTO<List<DepartmentVO>>`
- **响应示例**:
```json
{
  "code": 0,
  "data": [
    {
      "departmentId": 1,
      "departmentName": "总公司",
      "parentId": 0,
      "sort": 1,
      "managerId": 1001,
      "managerName": "张总",
      "createTime": "2023-01-01T10:00:00",
      "updateTime": "2023-01-01T10:00:00"
    },
    {
      "departmentId": 2,
      "departmentName": "技术部",
      "parentId": 1,
      "sort": 1,
      "managerId": 1002,
      "managerName": "李经理",
      "createTime": "2023-01-01T10:00:00",
      "updateTime": "2023-01-01T10:00:00"
    }
  ],
  "msg": "OK"
}
```

**本节来源**
- [DepartmentController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\department\controller\DepartmentController.java#L35-L68)
- [DepartmentAddForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\department\domain\form\DepartmentAddForm.java#L1-L35)
- [DepartmentUpdateForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\department\domain\form\DepartmentUpdateForm.java#L1-L23)
- [DepartmentVO.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\department\domain\vo\DepartmentVO.java#L1-L47)
- [DepartmentTreeVO.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\department\domain\vo\DepartmentTreeVO.java#L1-L32)

## 员工管理API

员工管理API提供对员工全生命周期的管理功能，包括增删改查、密码管理、状态变更和部门分配等操作。

### 员工查询
根据多种条件查询员工信息，支持分页。

- **HTTP方法**: POST
- **URL路径**: `/employee/query`
- **权限要求**: 无需特定权限（已登录即可）
- **请求参数**: 请求体（JSON）
- **请求体结构**:
```json
{
  "keyword": "string", // 搜索关键词 (最多20字符)
  "departmentId": 0, // 部门ID
  "disabledFlag": false, // 是否禁用
  "employeeIdList": [0] // 员工ID列表 (最多99个)
}
```
- **响应格式**: `ResponseDTO<PageResult<EmployeeVO>>`
- **响应示例**:
```json
{
  "code": 0,
  "data": {
    "list": [
      {
        "employeeId": 1001,
        "loginName": "zhangsan",
        "actualName": "张三",
        "gender": 1,
        "phone": "13800138001",
        "email": "zhangsan@company.com",
        "departmentId": 1,
        "departmentName": "总公司",
        "disabledFlag": false,
        "administratorFlag": true,
        "roleIdList": [1],
        "roleNameList": ["超级管理员"],
        "positionId": 1,
        "positionName": "总经理",
        "createTime": "2023-01-01T10:00:00"
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1
  },
  "msg": "OK"
}
```

### 添加员工
创建新的员工账户。

- **HTTP方法**: POST
- **URL路径**: `/employee/add`
- **权限要求**: `system:employee:add`
- **请求参数**: 请求体（JSON）
- **请求体结构**:
```json
{
  "actualName": "string", // 姓名 (必填，最多30字符)
  "loginName": "string", // 登录账号 (必填，最多30字符)
  "gender": 0, // 性别 (0:未知, 1:男, 2:女)
  "departmentId": 0, // 部门ID (必填)
  "disabledFlag": false, // 是否禁用 (必填)
  "phone": "string", // 手机号 (必填，格式正确)
  "email": "string", // 邮箱 (必填，格式正确)
  "positionId": 0, // 职务级别ID
  "roleIdList": [0], // 角色ID列表
  "remark": "string" // 备注 (最多200字符)
}
```
- **响应格式**: `ResponseDTO<String>`
- **业务约束**:
  - 姓名、登录账号、部门ID、禁用状态、手机号、邮箱均为必填项
  - 登录账号不能重复
  - 邮箱格式必须正确
  - 手机号格式必须正确
  - 指定的部门必须存在
  - 指定的角色必须存在

### 更新员工信息
修改员工的基本信息。

- **HTTP方法**: POST
- **URL路径**: `/employee/update`
- **权限要求**: `system:employee:update`
- **请求参数**: 请求体（JSON）
- **请求体结构**:
```json
{
  "employeeId": 0, // 员工ID (必填)
  "actualName": "string", // 姓名 (必填，最多30字符)
  "loginName": "string", // 登录账号 (必填，最多30字符)
  "gender": 0, // 性别 (0:未知, 1:男, 2:女)
  "departmentId": 0, // 部门ID (必填)
  "disabledFlag": false, // 是否禁用 (必填)
  "phone": "string", // 手机号 (必填，格式正确)
  "email": "string", // 邮箱 (必填，格式正确)
  "positionId": 0, // 职务级别ID
  "roleIdList": [0], // 角色ID列表
  "remark": "string" // 备注 (最多200字符)
}
```
- **响应格式**: `ResponseDTO<String>`
- **业务约束**:
  - 员工ID必须存在
  - 姓名、登录账号、部门ID、禁用状态、手机号、邮箱均为必填项
  - 登录账号不能与其他员工重复
  - 邮箱格式必须正确
  - 手机号格式必须正确
  - 指定的部门必须存在
  - 指定的角色必须存在

### 删除员工
删除指定的员工账户。

- **HTTP方法**: GET
- **URL路径**: `/employee/delete/{employeeId}`
- **权限要求**: `system:employee:delete`
- **路径参数**:
  - `employeeId`: 要删除的员工ID
- **响应格式**: `ResponseDTO<String>`
- **业务约束**:
  - 员工ID必须存在
  - 不能删除自己（当前登录用户）
  - 不能删除系统预置的超级管理员账户

### 批量删除员工
批量删除多个员工账户。

- **HTTP方法**: POST
- **URL路径**: `/employee/update/batch/delete`
- **权限要求**: `system:employee:delete`
- **请求参数**: 请求体（JSON数组）
- **请求体结构**:
```json
[1001, 1002, 1003]
```
- **响应格式**: `ResponseDTO<String>`
- **业务约束**:
  - 所有员工ID必须存在
  - 不能包含当前登录用户
  - 不能包含系统预置的超级管理员账户

### 批量调整部门
批量将员工调整到新的部门。

- **HTTP方法**: POST
- **URL路径**: `/employee/update/batch/department`
- **权限要求**: `system:employee:department:update`
- **请求参数**: 请求体（JSON）
- **请求体结构**:
```json
{
  "employeeIdList": [1001, 1002], // 员工ID列表
  "departmentId": 2 // 目标部门ID
}
```
- **响应格式**: `ResponseDTO<String>`
- **业务约束**:
  - 所有员工ID必须存在
  - 目标部门ID必须存在
  - 不能将员工调整到不存在的部门

### 修改密码
员工修改自己的登录密码。

- **HTTP方法**: POST
- **URL路径**: `/employee/update/password`
- **权限要求**: 无需特定权限（已登录即可）
- **请求参数**: 请求体（JSON）
- **请求体结构**:
```json
{
  "oldPassword": "string", // 旧密码 (已加密)
  "newPassword": "string" // 新密码 (已加密)
}
```
- **响应格式**: `ResponseDTO<String>`
- **业务约束**:
  - 旧密码必须正确
  - 新密码不能与旧密码相同
  - 密码复杂度需满足系统要求（如启用密码复杂度检查）

### 重置密码
管理员重置员工的登录密码。

- **HTTP方法**: GET
- **URL路径**: `/employee/update/password/reset/{employeeId}`
- **权限要求**: `system:employee:password:reset`
- **路径参数**:
  - `employeeId`: 要重置密码的员工ID
- **响应格式**: `ResponseDTO<String>`
- **业务约束**:
  - 员工ID必须存在
  - 不能重置自己的密码
  - 重置后的密码为系统默认密码

### 更新禁用状态
启用或禁用员工账户。

- **HTTP方法**: GET
- **URL路径**: `/employee/update/disabled/{employeeId}`
- **权限要求**: `system:employee:disabled`
- **路径参数**:
  - `employeeId`: 要变更状态的员工ID
- **响应格式**: `ResponseDTO<String>`
- **业务约束**:
  - 员工ID必须存在
  - 不能变更自己的禁用状态
  - 不能禁用系统预置的超级管理员账户

### 根据部门查询员工
根据部门ID查询该部门下的所有员工。

- **HTTP方法**: GET
- **URL路径**: `/employee/getAllEmployeeByDepartmentId/{departmentId}`
- **权限要求**: 无需特定权限（已登录即可）
- **路径参数**:
  - `departmentId`: 部门ID
- **响应格式**: `ResponseDTO<List<EmployeeVO>>`

**本节来源**
- [EmployeeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\employee\controller\EmployeeController.java#L40-L129)
- [EmployeeAddForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\employee\domain\form\EmployeeAddForm.java#L1-L70)
- [EmployeeUpdateForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\employee\domain\form\EmployeeUpdateForm.java#L1-L22)
- [EmployeeQueryForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\employee\domain\form\EmployeeQueryForm.java#L1-L40)
- [EmployeeVO.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\employee\domain\vo\EmployeeVO.java#L1-L69)
- [employee-const.js](file://smart-admin-web-javascript\src\constants\system\employee-const.js#L1-L28)

## 菜单管理API

菜单管理API提供对系统菜单的完整管理功能，包括CRUD操作、权限配置和菜单树查询。

### 添加菜单
创建新的菜单节点。

- **HTTP方法**: POST
- **URL路径**: `/menu/add`
- **权限要求**: `system:menu:add`
- **请求参数**: 请求体（JSON）
- **请求体结构**:
```json
{
  "menuName": "string", // 菜单名称 (必填，最多30字符)
  "menuType": 1, // 菜单类型 (1:目录, 2:菜单, 3:功能点)
  "parentId": 0, // 父菜单ID (0表示顶级菜单)
  "sort": 0, // 显示顺序
  "path": "string", // 路由地址
  "component": "string", // 组件路径
  "frameFlag": false, // 是否为外链
  "frameUrl": "string", // 外链地址
  "cacheFlag": false, // 是否缓存
  "visibleFlag": true, // 显示状态
  "disabledFlag": false, // 禁用状态
  "permsType": 1, // 权限类型 (1:Sa-Token模式)
  "webPerms": "string", // 前端权限字符串
  "apiPerms": "string", // 后端权限字符串
  "icon": "string", // 菜单图标
  "contextMenuId": 0 // 功能点关联菜单ID
}
```
- **响应格式**: `ResponseDTO<String>`
- **业务约束**:
  - 菜单名称不能为空且长度在1-30个字符之间
  - 菜单类型必须为有效值（1, 2, 3）
  - 父菜单ID必须指向存在的菜单或为0
  - 前端权限字符串在同一层级下不能重复
  - 菜单名称在同一父级下不能重复

### 更新菜单
修改现有菜单的信息。

- **HTTP方法**: POST
- **URL路径**: `/menu/update`
- **权限要求**: `system:menu:update`
- **请求参数**: 请求体（JSON）
- **请求体结构**:
```json
{
  "menuId": 0, // 菜单ID (必填)
  "menuName": "string", // 菜单名称 (必填，最多30字符)
  "menuType": 1, // 菜单类型 (1:目录, 2:菜单, 3:功能点)
  "parentId": 0, // 父菜单ID (0表示顶级菜单)
  "sort": 0, // 显示顺序
  "path": "string", // 路由地址
  "component": "string", // 组件路径
  "frameFlag": false, // 是否为外链
  "frameUrl": "string", // 外链地址
  "cacheFlag": false, // 是否缓存
  "visibleFlag": true, // 显示状态
  "disabledFlag": false, // 禁用状态
  "permsType": 1, // 权限类型 (1:Sa-Token模式)
  "webPerms": "string", // 前端权限字符串
  "apiPerms": "string", // 后端权限字符串
  "icon": "string", // 菜单图标
  "contextMenuId": 0 // 功能点关联菜单ID
}
```
- **响应格式**: `ResponseDTO<String>`
- **业务约束**:
  - 菜单ID必须存在
  - 菜单名称不能为空且长度在1-30个字符之间
  - 菜单类型必须为有效值（1, 2, 3）
  - 父菜单ID必须指向存在的菜单或为0
  - 不能将菜单移动到其自身的子菜单下（防止循环引用）
  - 前端权限字符串在同一层级下不能重复
  - 菜单名称在同一父级下不能重复

### 批量删除菜单
批量删除多个菜单节点。

- **HTTP方法**: GET
- **URL路径**: `/menu/batchDelete`
- **权限要求**: `system:menu:batchDelete`
- **查询参数**:
  - `menuIdList`: 要删除的菜单ID列表，以逗号分隔
- **响应格式**: `ResponseDTO<String>`
- **业务约束**:
  - 所有菜单ID必须存在
  - 不能删除仍有关联子菜单的菜单（需先删除所有子菜单）
  - 不能删除被角色权限引用的菜单
  - 系统预置的核心菜单不能删除

### 查询菜单列表
获取所有菜单的扁平化列表。

- **HTTP方法**: GET
- **URL路径**: `/menu/query`
- **权限要求**: 无需特定权限（已登录即可）
- **请求参数**: 无
- **响应格式**: `ResponseDTO<List<MenuVO>>`

### 查询菜单树
获取菜单的树形结构。

- **HTTP方法**: GET
- **URL路径**: `/menu/tree`
- **权限要求**: 无需特定权限（已登录即可）
- **查询参数**:
  - `onlyMenu`: 是否只返回菜单类型（true/false）
- **响应格式**: `ResponseDTO<List<MenuTreeVO>>`
- **业务说明**:
  - 当`onlyMenu`为true时，只返回类型为"菜单"的节点，不包含"目录"和"功能点"
  - 当`onlyMenu`为false时，返回完整的菜单树结构

### 查询菜单详情
获取指定菜单的详细信息。

- **HTTP方法**: GET
- **URL路径**: `/menu/detail/{menuId}`
- **权限要求**: 无需特定权限（已登录即可）
- **路径参数**:
  - `menuId`: 菜单ID
- **响应格式**: `ResponseDTO<MenuVO>`

### 获取所有请求路径
获取系统中所有需要权限控制的API请求路径。

- **HTTP方法**: GET
- **URL路径**: `/menu/auth/url`
- **权限要求**: 无需特定权限（已登录即可）
- **请求参数**: 无
- **响应格式**: `ResponseDTO<List<RequestUrlVO>>`
- **响应示例**:
```json
{
  "code": 0,
  "data": [
    {
      "url": "/department/treeList",
      "httpMethod": "GET"
    },
    {
      "url": "/department/add",
      "httpMethod": "POST"
    }
  ],
  "msg": "OK"
}
```

**本节来源**
- [MenuController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\menu\controller\MenuController.java#L37-L83)
- [MenuAddForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\menu\domain\form\MenuAddForm.java#L1-L20)
- [MenuUpdateForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\menu\domain\form\MenuUpdateForm.java#L1-L25)
- [MenuBaseForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\menu\domain\form\MenuBaseForm.java#L1-L82)
- [MenuVO.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\menu\domain\vo\MenuVO.java#L1-L35)
- [MenuTypeEnum.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\menu\constant\MenuTypeEnum.java#L1-L48)
- [menu-const.js](file://smart-admin-web-javascript\src\constants\system\menu-const.js#L1-L44)

## 前端调用示例

以下示例展示了前端如何调用系统管理API，包括参数传递方式和响应数据处理模式。

### 部门管理调用示例

```javascript
import { departmentApi } from '/@/api/system/department-api.js';

// 查询部门树形列表
async function loadDepartmentTree() {
  try {
    const response = await departmentApi.queryDepartmentTree();
    if (response.code === 0) {
      // 成功获取部门树
      console.log('部门树:', response.data);
      return response.data;
    } else {
      // 处理错误
      console.error('获取部门树失败:', response.msg);
      return [];
    }
  } catch (error) {
    console.error('网络请求失败:', error);
    return [];
  }
}

// 添加部门
async function addNewDepartment() {
  const param = {
    departmentName: '新部门',
    sort: 1,
    managerId: 1001,
    parentId: 1
  };
  
  try {
    const response = await departmentApi.addDepartment(param);
    if (response.code === 0) {
      console.log('部门添加成功');
      // 刷新部门树
      await loadDepartmentTree();
    } else {
      console.error('部门添加失败:', response.msg);
    }
  } catch (error) {
    console.error('网络请求失败:', error);
  }
}

// 更新部门
async function updateDepartment(departmentId) {
  const param = {
    departmentId: departmentId,
    departmentName: '更新后的部门名',
    sort: 2,
    managerId: 1002
  };
  
  try {
    const response = await departmentApi.updateDepartment(param);
    if (response.code === 0) {
      console.log('部门更新成功');
      // 刷新部门树
      await loadDepartmentTree();
    } else {
      console.error('部门更新失败:', response.msg);
    }
  } catch (error) {
    console.error('网络请求失败:', error);
  }
}

// 删除部门
async function deleteDepartment(departmentId) {
  try {
    const response = await departmentApi.deleteDepartment(departmentId);
    if (response.code === 0) {
      console.log('部门删除成功');
      // 刷新部门树
      await loadDepartmentTree();
    } else {
      console.error('部门删除失败:', response.msg);
    }
  } catch (error) {
    console.error('网络请求失败:', error);
  }
}
```

### 员工管理调用示例

```javascript
import { employeeApi } from '/@/api/system/employee-api.js';

// 查询员工列表
async function queryEmployees() {
  const params = {
    keyword: '张',
    departmentId: 1,
    disabledFlag: false
  };
  
  try {
    const response = await employeeApi.queryEmployee(params);
    if (response.code === 0) {
      console.log('员工列表:', response.data.list);
      return response.data;
    } else {
      console.error('查询员工失败:', response.msg);
      return null;
    }
  } catch (error) {
    console.error('网络请求失败:', error);
    return null;
  }
}

// 添加员工
async function addNewEmployee() {
  const params = {
    actualName: '李四',
    loginName: 'lisi',
    gender: 1,
    departmentId: 1,
    disabledFlag: false,
    phone: '13800138002',
    email: 'lisi@company.com',
    positionId: 2,
    roleIdList: [2],
    remark: '新入职员工'
  };
  
  try {
    const response = await employeeApi.addEmployee(params);
    if (response.code === 0) {
      console.log('员工添加成功');
      // 刷新员工列表
      await queryEmployees();
    } else {
      console.error('员工添加失败:', response.msg);
    }
  } catch (error) {
    console.error('网络请求失败:', error);
  }
}

// 批量调整员工部门
async function batchUpdateDepartment() {
  const updateParam = {
    employeeIdList: [1001, 1002],
    departmentId: 2
  };
  
  try {
    const response = await employeeApi.batchUpdateDepartmentEmployee(updateParam);
    if (response.code === 0) {
      console.log('批量调整部门成功');
      // 刷新员工列表
      await queryEmployees();
    } else {
      console.error('批量调整部门失败:', response.msg);
    }
  } catch (error) {
    console.error('网络请求失败:', error);
  }
}

// 修改密码
async function changePassword() {
  const param = {
    oldPassword: 'encrypted_old_password',
    newPassword: 'encrypted_new_password'
  };
  
  try {
    const response = await employeeApi.updateEmployeePassword(param);
    if (response.code === 0) {
      console.log('密码修改成功');
    } else {
      console.error('密码修改失败:', response.msg);
    }
  } catch (error) {
    console.error('网络请求失败:', error);
  }
}
```

### 菜单管理调用示例

```javascript
import { menuApi } from '/@/api/system/menu-api.js';

// 查询菜单树（只返回菜单类型）
async function loadMenuTree() {
  try {
    const response = await menuApi.queryMenuTree(true);
    if (response.code === 0) {
      console.log('菜单树:', response.data);
      return response.data;
    } else {
      console.error('获取菜单树失败:', response.msg);
      return [];
    }
  } catch (error) {
    console.error('网络请求失败:', error);
    return [];
  }
}

// 添加菜单
async function addNewMenu() {
  const param = {
    menuName: '新菜单',
    menuType: 2,
    parentId: 1,
    sort: 1,
    path: '/new-menu',
    component: 'NewMenuView',
    frameFlag: false,
    cacheFlag: true,
    visibleFlag: true,
    disabledFlag: false,
    permsType: 1,
    webPerms: 'new:menu:view',
    apiPerms: 'new:menu:view',
    icon: 'icon-menu'
  };
  
  try {
    const response = await menuApi.addMenu(param);
    if (response.code === 0) {
      console.log('菜单添加成功');
      // 刷新菜单树
      await loadMenuTree();
    } else {
      console.error('菜单添加失败:', response.msg);
    }
  } catch (error) {
    console.error('网络请求失败:', error);
  }
}

// 批量删除菜单
async function deleteMenus(menuIdList) {
  try {
    const response = await menuApi.batchDeleteMenu(menuIdList.join(','));
    if (response.code === 0) {
      console.log('菜单删除成功');
      // 刷新菜单树
      await loadMenuTree();
    } else {
      console.error('菜单删除失败:', response.msg);
    }
  } catch (error) {
    console.error('网络请求失败:', error);
  }
}
```

**本节来源**
- [department-api.js](file://smart-admin-web-javascript\src\api\system\department-api.js#L1-L46)
- [employee-api.js](file://smart-admin-web-javascript\src\api\system\employee-api.js#L1-L99)
- [menu-api.js](file://smart-admin-web-javascript\src\api\system\menu-api.js#L1-L55)

## 树形结构处理逻辑

系统中的部门和菜单都采用树形结构存储和管理，以下是树形结构的处理逻辑说明。

### 数据结构设计

树形结构通过以下字段实现：
- `parentId`: 指向父节点的ID，顶级节点的`parentId`为0
- `sort`: 同级节点的排序值，数值越小越靠前
- `preId`和`nextId`: 记录同级节点的前后关系，用于快速定位
- `selfAndAllChildrenIdList`: 当前节点及其所有子节点的ID集合，用于权限判断

### 层级关系维护

当添加或更新节点时，系统会自动维护层级关系：
1. **添加节点**：指定`parentId`确定其父节点，系统会自动计算`sort`值
2. **移动节点**：通过更新`parentId`实现节点在不同父节点间的移动
3. **排序调整**：通过更新`sort`值调整同级节点的显示顺序

### 循环引用检测

为防止出现循环引用（如A是B的父节点，B又是A的父节点），系统在添加或更新节点时会进行检测：
- 检查目标父节点是否是当前节点的子节点或后代节点
- 如果存在循环引用，则拒绝操作并返回错误

### 树形数据查询

系统提供两种查询方式：
1. **扁平化列表**：返回所有节点的简单列表，不包含层级关系
2. **树形结构**：返回嵌套的树形结构，包含完整的父子关系

前端在处理树形数据时，通常使用递归算法构建树形结构：
```javascript
function buildTree(nodes, parentId = 0) {
  return nodes
    .filter(node => node.parentId === parentId)
    .map(node => ({
      ...node,
      children: buildTree(nodes, node.departmentId || node.menuId)
    }));
}
```

**本节来源**
- [DepartmentTreeVO.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\department\domain\vo\DepartmentTreeVO.java#L1-L32)
- [MenuTreeVO.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\menu\domain\vo\MenuTreeVO.java#L1-L22)
- [DepartmentController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\department\controller\DepartmentController.java#L35-L39)
- [MenuController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\menu\controller\MenuController.java#L72-L76)

## 权限关联接口调用顺序

在进行涉及权限变更的操作时，需要按照特定顺序调用相关接口，以确保数据一致性。

### 创建新功能的完整流程

当需要创建一个新的功能模块时，应按以下顺序调用接口：

```javascript
async function createNewFeature() {
  // 1. 首先添加菜单目录
  const catalogResponse = await menuApi.addMenu({
    menuName: '新功能模块',
    menuType: 1, // 目录
    parentId: 0,
    sort: 10,
    visibleFlag: true,
    disabledFlag: false
  });
  
  if (catalogResponse.code !== 0) {
    throw new Error('创建目录失败');
  }
  
  const catalogId = catalogResponse.data; // 假设返回新创建的菜单ID
  
  // 2. 添加菜单项
  const menuResponse = await menuApi.addMenu({
    menuName: '功能列表',
    menuType: 2, // 菜单
    parentId: catalogId,
    sort: 1,
    path: '/new-feature',
    component: 'NewFeatureView',
    visibleFlag: true,
    disabledFlag: false,
    webPerms: 'new:feature:view'
  });
  
  if (menuResponse.code !== 0) {
    throw new Error('创建菜单失败');
  }
  
  const menuId = menuResponse.data;
  
  // 3. 添加功能点（操作权限）
  const addPointResponse = await menuApi.addMenu({
    menuName: '添加',
    menuType: 3, // 功能点
    parentId: menuId,
    sort: 1,
    visibleFlag: true,
    disabledFlag: false,
    webPerms: 'new:feature:add',
    apiPerms: 'new:feature:add'
  });
  
  const updatePointResponse = await menuApi.addMenu({
    menuName: '编辑',
    menuType: 3, // 功能点
    parentId: menuId,
    sort: 2,
    visibleFlag: true,
    disabledFlag: false,
    webPerms: 'new:feature:update',
    apiPerms: 'new:feature:update'
  });
  
  const deletePointResponse = await menuApi.addMenu({
    menuName: '删除',
    menuType: 3, // 功能点
    parentId: menuId,
    sort: 3,
    visibleFlag: true,
    disabledFlag: false,
    webPerms: 'new:feature:delete',
    apiPerms: 'new:feature:delete'
  });
  
  // 4. 更新角色权限（假设角色ID为2）
  const roleMenuApi = {
    updateRoleMenu: (roleId, menuIdList) => {
      return postRequest('/role/menu/update', { roleId, menuIdList });
    }
  };
  
  const roleMenuResponse = await roleMenuApi.updateRoleMenu(2, [
    catalogId, menuId, 
    addPointResponse.data, updatePointResponse.data, deletePointResponse.data
  ]);
  
  if (roleMenuResponse.code !== 0) {
    throw new Error('更新角色权限失败');
  }
  
  console.log('新功能创建完成');
}
```

### 删除功能的完整流程

当需要删除一个功能模块时，应按以下顺序调用接口：

```javascript
async function deleteFeature(menuIdList) {
  // 1. 首先移除角色权限
  const roleMenuApi = {
    updateRoleMenu: (roleId, menuIdList) => {
      return postRequest('/role/menu/update', { roleId, menuIdList });
    }
  };
  
  // 获取所有角色并移除相关权限
  const roles = await roleApi.queryAll(); // 假设存在查询所有角色的接口
  for (const role of roles.data) {
    // 获取角色现有权限
    const roleMenuList = await roleMenuApi.getRoleMenu(role.roleId);
    // 过滤掉要删除的菜单权限
    const newMenuIdList = roleMenuList.data.filter(id => !menuIdList.includes(id));
    // 更新角色权限
    await roleMenuApi.updateRoleMenu(role.roleId, newMenuIdList);
  }
  
  // 2. 批量删除菜单
  const deleteResponse = await menuApi.batchDeleteMenu(menuIdList.join(','));
  
  if (deleteResponse.code !== 0) {
    throw new Error('删除菜单失败');
  }
  
  console.log('功能删除成功');
}
```

### 权限变更的最佳实践

1. **先配置后分配**：先创建所有菜单和权限点，再分配给角色
2. **原子性操作**：将相关操作放在一个事务中，确保数据一致性
3. **权限最小化**：遵循最小权限原则，只授予必要的权限
4. **审计日志**：记录所有权限变更操作，便于追踪和审计

**本节来源**
- [menu-api.js](file://smart-admin-web-javascript\src\api\system\menu-api.js#L1-L55)
- [MenuController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\menu\controller\MenuController.java#L37-L83)
- [RoleMenuController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\role\controller\RoleMenuController.java)