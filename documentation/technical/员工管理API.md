# 员工管理API

<cite>
**本文档引用的文件**
- [EmployeeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\employee\controller\EmployeeController.java)
- [EmployeeService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\employee\service\EmployeeService.java)
- [EmployeeQueryForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\employee\domain\form\EmployeeQueryForm.java)
- [EmployeeVO.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\employee\domain\vo\EmployeeVO.java)
- [EmployeeAddForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\employee\domain\form\EmployeeAddForm.java)
- [EmployeeUpdateForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\employee\domain\form\EmployeeUpdateForm.java)
- [EmployeeUpdatePasswordForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\employee\domain\form\EmployeeUpdatePasswordForm.java)
- [EmployeeBatchUpdateDepartmentForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\employee\domain\form\EmployeeBatchUpdateDepartmentForm.java)
- [employee-api.js](file://smart-admin-web-javascript\src\api\system\employee-api.js)
- [ApiDecrypt.java](file://smart-admin-api-java17-springboot3\sa-base\src\main\java\net\lab1024\sa\base\module\support\apiencrypt\annotation\ApiDecrypt.java)
- [SecurityPasswordService.java](file://smart-admin-api-java17-springboot3\sa-base\src\main\java\net\lab1024\sa\base\module\support\securityprotect\service\SecurityPasswordService.java)
</cite>

## 目录
1. [简介](#简介)
2. [核心接口](#核心接口)
3. [查询接口](#查询接口)
4. [员工添加](#员工添加)
5. [员工更新](#员工更新)
6. [禁用启用操作](#禁用启用操作)
7. [批量删除](#批量删除)
8. [密码管理](#密码管理)
9. [批量调整部门](#批量调整部门)
10. [响应数据结构](#响应数据结构)

## 简介
员工管理API提供了完整的员工全生命周期管理功能，包括员工的查询、添加、更新、禁用启用、批量删除等操作。系统特别关注员工密码安全，实现了密码加解密传输、密码复杂度校验等安全机制。本API支持分页查询，能够高效处理大量员工数据的检索和管理。

**本节不分析具体源文件，因此不提供来源信息**

## 核心接口
员工管理API提供了一套完整的RESTful接口，覆盖了员工管理的各个方面。主要接口包括：
- 查询接口：/employee/query 和 /employee/queryAll
- 添加接口：/employee/add
- 更新接口：/employee/update
- 禁用启用接口：/employee/update/disabled/{employeeId}
- 批量删除接口：/employee/update/batch/delete
- 密码管理接口：/employee/update/password 和 /employee/update/password/reset/{employeeId}
- 批量调整部门接口：/employee/update/batch/department

这些接口通过Spring Boot框架实现，使用Sa-Token进行权限控制，确保只有授权用户才能执行相应操作。

**本节不分析具体源文件，因此不提供来源信息**

## 查询接口
### 分页查询
分页查询接口 `/employee/query` 支持多种查询条件，允许客户端根据需要检索员工信息。

**请求参数**
```json
{
  "keyword": "搜索词",
  "departmentId": 1,
  "disabledFlag": false,
  "employeeIdList": [1, 2, 3],
  "current": 1,
  "size": 10
}
```

**参数说明**
- `keyword`: 搜索关键词，支持员工姓名、登录账号等字段的模糊匹配
- `departmentId`: 部门ID，用于按部门筛选员工
- `disabledFlag`: 是否禁用状态，true表示禁用，false表示启用
- `employeeIdList`: 员工ID列表，用于批量查询特定员工
- `current`: 当前页码，从1开始
- `size`: 每页大小，控制返回的记录数量

**使用示例**
```javascript
employeeApi.queryEmployee({
  keyword: "张",
  departmentId: 1,
  current: 1,
  size: 20
});
```

**Section sources**
- [EmployeeQueryForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\employee\domain\form\EmployeeQueryForm.java#L20-L40)
- [EmployeeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\employee\controller\EmployeeController.java#L40-L44)

### 查询所有员工
`/employee/queryAll` 接口用于查询所有员工，支持按禁用状态过滤。

**请求示例**
```javascript
employeeApi.queryAll();
// 或按禁用状态查询
employeeApi.queryAll({ disabledFlag: false });
```

**Section sources**
- [EmployeeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\employee\controller\EmployeeController.java#L123-L127)

## 员工添加
### 添加员工接口
添加员工接口 `/employee/add` 用于创建新员工账户。

**请求参数**
```json
{
  "actualName": "张三",
  "loginName": "zhangsan",
  "gender": 1,
  "departmentId": 1,
  "disabledFlag": false,
  "phone": "13800138000",
  "email": "zhangsan@example.com",
  "positionId": 1,
  "roleIdList": [1, 2],
  "remark": "新员工"
}
```

**参数说明**
- `actualName`: 员工姓名，必填，最多30字符
- `loginName`: 登录账号，必填，最多30字符，必须唯一
- `gender`: 性别，1表示男，2表示女
- `departmentId`: 所属部门ID，必填
- `disabledFlag`: 是否禁用，false表示启用
- `phone`: 手机号码，必填，格式必须正确
- `email`: 邮箱地址，必填，格式必须正确
- `positionId`: 职务ID
- `roleIdList`: 角色ID列表
- `remark`: 备注信息，最多200字符

**响应说明**
成功添加员工后，接口会返回生成的随机密码，该密码将作为员工的初始登录密码。

**Section sources**
- [EmployeeAddForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\employee\domain\form\EmployeeAddForm.java#L24-L68)
- [EmployeeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\employee\controller\EmployeeController.java#L46-L51)

## 员工更新
### 更新员工信息
更新员工接口 `/employee/update` 用于修改现有员工的信息。

**请求参数**
```json
{
  "employeeId": 1,
  "actualName": "张三",
  "loginName": "zhangsan",
  "gender": 1,
  "departmentId": 1,
  "disabledFlag": false,
  "phone": "13800138000",
  "email": "zhangsan@example.com",
  "positionId": 1,
  "roleIdList": [1, 2],
  "remark": "更新信息"
}
```

**参数说明**
- `employeeId`: 员工ID，必填
- 其他参数与添加员工接口相同

**业务逻辑**
1. 验证员工是否存在
2. 验证部门是否存在
3. 检查登录名、手机号、邮箱的唯一性
4. 更新员工信息
5. 清除员工缓存

**Section sources**
- [EmployeeUpdateForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\employee\domain\form\EmployeeUpdateForm.java#L16-L22)
- [EmployeeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\employee\controller\EmployeeController.java#L53-L58)

## 禁用启用操作
### 更新禁用状态
`/employee/update/disabled/{employeeId}` 接口用于切换员工的禁用状态。

**请求示例**
```javascript
employeeApi.updateDisabled(1);
```

**业务流程**
1. 根据employeeId查找员工
2. 切换员工的disabledFlag状态
3. 更新数据库记录
4. 清除相关缓存

此操作是幂等的，即多次调用会交替改变员工的禁用状态。

**Section sources**
- [EmployeeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\employee\controller\EmployeeController.java#L74-L79)

## 批量删除
### 批量删除员工
`/employee/update/batch/delete` 接口用于批量删除员工。

**请求参数**
```json
[1, 2, 3]
```

**参数说明**
- 接收一个员工ID数组，最多支持99个员工同时删除

**业务逻辑**
1. 验证员工ID列表
2. 批量更新员工的deletedFlag为true
3. 清除相关缓存

**前端调用示例**
```javascript
employeeApi.batchDeleteEmployee([1, 2, 3]);
```

**Section sources**
- [EmployeeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\employee\controller\EmployeeController.java#L81-L86)

## 密码管理
### 密码安全机制
系统实现了多层次的密码安全机制，包括：

1. **传输加密**: 使用@ApiDecrypt注解对密码进行加解密
2. **存储加密**: 使用Argon2算法对密码进行哈希存储
3. **复杂度校验**: 强制要求密码包含大小写字母、数字和特殊符号
4. **历史密码检查**: 防止重复使用最近的历史密码

### 修改密码
`/employee/update/password` 接口用于修改员工密码。

**请求参数**
```json
{
  "oldPassword": "旧密码",
  "newPassword": "新密码"
}
```

**@ApiDecrypt注解作用**
@ApiDecrypt注解用于在密码传输过程中进行解密操作。当客户端发送加密的密码数据时，服务器端会自动解密，确保密码在传输过程中的安全性。

**前端调用**
```javascript
employeeApi.updateEmployeePassword({
  oldPassword: "旧密码",
  newPassword: "新密码"
});
```

**业务流程**
1. 验证原密码是否正确
2. 校验新密码复杂度
3. 检查是否与历史密码重复
4. 更新密码并记录密码修改日志

**Section sources**
- [EmployeeUpdatePasswordForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\employee\domain\form\EmployeeUpdatePasswordForm.java#L16-L29)
- [EmployeeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\employee\controller\EmployeeController.java#L95-L101)
- [ApiDecrypt.java](file://smart-admin-api-java17-springboot3\sa-base\src\main\java\net\lab1024\sa\base\module\support\apiencrypt\annotation\ApiDecrypt.java#L1-L20)
- [SecurityPasswordService.java](file://smart-admin-api-java17-springboot3\sa-base\src\main\java\net\lab1024\sa\base\module\support\securityprotect\service\SecurityPasswordService.java#L34-L76)

### 重置密码
`/employee/update/password/reset/{employeeId}` 接口用于重置员工密码。

**请求示例**
```javascript
employeeApi.resetPassword(1);
```

**业务逻辑**
1. 生成符合复杂度要求的随机密码
2. 更新员工密码
3. 返回新密码

**权限控制**
只有具有"system:employee:password:reset"权限的管理员才能执行此操作。

**Section sources**
- [EmployeeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\employee\controller\EmployeeController.java#L110-L115)

## 批量调整部门
### 批量调整部门接口
`/employee/update/batch/department` 接口用于批量调整员工的所属部门。

**请求参数**
```json
{
  "employeeIdList": [1, 2, 3],
  "departmentId": 2
}
```

**参数说明**
- `employeeIdList`: 员工ID列表，最多支持99个员工
- `departmentId`: 目标部门ID

**业务流程**
1. 验证员工ID列表和部门ID
2. 批量更新员工的departmentId
3. 清除相关缓存

**前端调用示例**
```javascript
employeeApi.batchUpdateDepartmentEmployee({
  employeeIdList: [1, 2, 3],
  departmentId: 2
});
```

**Section sources**
- [EmployeeBatchUpdateDepartmentForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\employee\domain\form\EmployeeBatchUpdateDepartmentForm.java#L20-L31)
- [EmployeeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\employee\controller\EmployeeController.java#L88-L93)

## 响应数据结构
### 员工信息响应
查询接口返回的员工信息包含以下字段：

```json
{
  "employeeId": 1,
  "loginName": "zhangsan",
  "gender": 1,
  "actualName": "张三",
  "phone": "13800138000",
  "departmentId": 1,
  "disabledFlag": false,
  "administratorFlag": false,
  "departmentName": "技术部",
  "createTime": "2023-01-01T00:00:00",
  "roleIdList": [1, 2],
  "roleNameList": ["管理员", "普通用户"],
  "positionId": 1,
  "positionName": "高级工程师",
  "email": "zhangsan@example.com"
}
```

**字段说明**
- `employeeId`: 员工主键ID
- `loginName`: 登录账号
- `gender`: 性别，1表示男，2表示女
- `actualName`: 员工姓名
- `phone`: 手机号码
- `departmentId`: 所属部门ID
- `disabledFlag`: 是否禁用，true表示禁用，false表示启用
- `administratorFlag`: 是否为超级管理员
- `departmentName`: 部门名称（包含部门层级路径）
- `createTime`: 创建时间
- `roleIdList`: 角色ID列表
- `roleNameList`: 角色名称列表
- `positionId`: 职务ID
- `positionName`: 职务名称
- `email`: 邮箱地址

### 分页响应结构
分页查询返回的标准响应结构：

```json
{
  "code": 0,
  "ok": true,
  "data": {
    "records": [...],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  },
  "msg": "成功"
}
```

**字段说明**
- `records`: 当前页的员工记录列表
- `total`: 总记录数
- `size`: 每页大小
- `current`: 当前页码
- `pages`: 总页数

**Section sources**
- [EmployeeVO.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\employee\domain\vo\EmployeeVO.java#L20-L68)