# 系统管理API

<cite>
**本文档引用的文件**
- [AreaController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\area\controller\AreaController.java)
- [DepartmentController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\department\controller\DepartmentController.java)
- [EmployeeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\employee\controller\EmployeeController.java)
- [MenuController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\menu\controller\MenuController.java)
- [RoleController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\role\controller\RoleController.java)
- [ResponseDTO.java](file://smart-admin-api-java17-springboot3\sa-base\src\main\java\net\lab1024\sa\base\common\domain\ResponseDTO.java)
- [PageParam.java](file://smart-admin-api-java17-springboot3\sa-base\src\main\java\net\lab1024\sa\base\common\domain\PageParam.java)
- [area-api.js](file://smart-admin-web-javascript\src\api\system\area-api.js)
- [department-api.js](file://smart-admin-web-javascript\src\api\system\department-api.js)
- [employee-api.js](file://smart-admin-web-javascript\src\api\system\employee-api.js)
- [menu-api.js](file://smart-admin-web-javascript\src\api\system\menu-api.js)
- [role-api.js](file://smart-admin-web-javascript\src\api\system\role-api.js)
</cite>

## 目录
1. [系统管理API概述](#系统管理api概述)
2. [统一响应格式ResponseDTO](#统一响应格式responsedto)
3. [分页参数PageParam](#分页参数pageparam)
4. [权限控制注解@SaCheckPermission](#权限控制注解sacheckpermission)
5. [区域管理API](#区域管理api)
6. [部门管理API](#部门管理api)
7. [员工管理API](#员工管理api)
8. [菜单管理API](#菜单管理api)
9. [角色管理API](#角色管理api)
10. [前端调用示例](#前端调用示例)

## 系统管理API概述

系统管理模块提供了区域、部门、员工、菜单、角色等核心管理功能的RESTful API接口。这些API遵循统一的设计模式，采用标准的HTTP方法和URL路径规范，确保接口的一致性和易用性。所有接口都返回统一的响应格式ResponseDTO，并支持分页查询、权限控制等通用功能。

系统管理API的设计遵循RESTful架构风格，使用标准的HTTP方法来表示不同的操作类型：
- **GET**: 用于获取资源（查询、详情）
- **POST**: 用于创建或更新资源（新增、修改）
- **DELETE**: 用于删除资源

**本节来源**
- [DepartmentController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\department\controller\DepartmentController.java#L28-L68)
- [EmployeeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\employee\controller\EmployeeController.java#L30-L129)

## 统一响应格式ResponseDTO

### ResponseDTO结构说明

ResponseDTO是系统管理API中统一的响应数据传输对象，用于封装所有API的返回结果。它包含状态码、消息、数据等关键信息，确保前后端交互的一致性。

```java
@Data
public class ResponseDTO<T> {
    public static final int OK_CODE = 0;
    public static final String OK_MSG = "操作成功";
    
    @Schema(description = "返回码")
    private Integer code;
    
    @Schema(description = "级别")
    private String level;
    
    private String msg;
    
    private Boolean ok;
    
    @Schema(description = "返回数据")
    private T data;
}
```

### 成功响应示例

当API调用成功时，返回标准的成功响应格式：

```json
{
  "code": 0,
  "msg": "操作成功",
  "ok": true,
  "data": {
    // 具体业务数据
  }
}
```

### 失败响应示例

当API调用失败时，返回错误信息：

```json
{
  "code": 1001,
  "msg": "参数错误",
  "ok": false,
  "data": null
}
```

### 常用静态方法

ResponseDTO提供了多个静态工厂方法，简化了成功和失败响应的创建：

- `ResponseDTO.ok()`: 创建无数据的成功响应
- `ResponseDTO.ok(T data)`: 创建包含数据的成功响应
- `ResponseDTO.userErrorParam()`: 创建用户参数错误的失败响应
- `ResponseDTO.error(ErrorCode errorCode)`: 创建指定错误码的失败响应

**本节来源**
- [ResponseDTO.java](file://smart-admin-api-java17-springboot3\sa-base\src\main\java\net\lab1024\sa\base\common\domain\ResponseDTO.java#L21-L122)

## 分页参数PageParam

### PageParam结构说明

PageParam是系统管理API中用于分页查询的基础参数对象，包含页码、每页数量、排序等信息。

```java
@Data
public class PageParam {
    @Schema(description = "页码(不能为空)", example = "1")
    @NotNull(message = "分页参数不能为空")
    private Long pageNum;
    
    @Schema(description = "每页数量(不能为空)", example = "10")
    @NotNull(message = "每页数量不能为空")
    @Max(value = 500, message = "每页最大为500")
    private Long pageSize;
    
    @Schema(description = "是否查询总条数")
    protected Boolean searchCount;
    
    @Schema(description = "排序字段集合")
    @Size(max = 10, message = "排序字段最多10")
    @Valid
    private List<SortItem> sortItemList;
}
```

### 分页响应格式

分页查询的响应数据封装在PageResult对象中，包含分页信息和数据列表：

```json
{
  "code": 0,
  "msg": "操作成功",
  "ok": true,
  "data": {
    "pageNum": 1,
    "pageSize": 10,
    "total": 100,
    "pages": 10,
    "list": [
      // 数据列表
    ],
    "emptyFlag": false
  }
}
```

### 使用示例

在前端调用分页API时，需要传递PageParam参数：

```javascript
const param = {
  pageNum: 1,
  pageSize: 10,
  sortItemList: [
    {
      column: "createTime",
      isAsc: false
    }
  ]
};
```

**本节来源**
- [PageParam.java](file://smart-admin-api-java17-springboot3\sa-base\src\main\java\net\lab1024\sa\base\common\domain\PageParam.java#L1-L48)
- [PageResult.java](file://smart-admin-api-java17-springboot3\sa-base\src\main\java\net\lab1024\sa\base\common\domain\PageResult.java#L1-L53)

## 权限控制注解@SaCheckPermission

### 注解说明

@SaCheckPermission是系统管理API中用于权限控制的注解，基于Sa-Token框架实现。它确保只有具有相应权限的用户才能访问特定的API接口。

```java
@PostMapping("/department/add")
@SaCheckPermission("system:department:add")
public ResponseDTO<String> addDepartment(@Valid @RequestBody DepartmentAddForm createDTO) {
    return departmentService.addDepartment(createDTO);
}
```

### 权限标识规则

权限标识采用冒号分隔的层级结构，遵循`模块:功能:操作`的命名规范：

- `system:department:add` - 部门模块的新增权限
- `system:employee:update` - 员工模块的修改权限
- `system:menu:delete` - 菜单模块的删除权限

### 支持的操作类型

常见的权限操作类型包括：
- `page` - 页面访问权限
- `add` - 新增权限
- `update` - 修改权限
- `delete` - 删除权限
- `disabled` - 禁用/启用权限

### 使用场景

@SaCheckPermission注解通常应用于需要权限验证的写操作（POST、DELETE），而读操作（GET）可能不需要权限验证或使用不同的权限控制策略。

**本节来源**
- [DepartmentController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\department\controller\DepartmentController.java#L43-L58)
- [EmployeeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\employee\controller\EmployeeController.java#L48-L83)

## 区域管理API

### 分页查询区域

**HTTP方法**: POST  
**URL路径**: `/system/area/page`  
**权限要求**: `area:page`  
**请求参数**: PageParam对象  
**响应格式**: ResponseDTO<PageResult<AreaVO>>

```javascript
areaApi.queryPage(param).then(response => {
  console.log(response.data.list);
});
```

### 查询区域树

**HTTP方法**: GET  
**URL路径**: `/system/area/tree`  
**权限要求**: `area:tree`  
**请求参数**: 无  
**响应格式**: ResponseDTO<List<AreaTreeVO>>

```javascript
areaApi.getAreaTree().then(response => {
  console.log(response.data);
});
```

### 查询区域详情

**HTTP方法**: GET  
**URL路径**: `/system/area/detail/{areaId}`  
**权限要求**: `area:detail`  
**请求参数**: areaId（路径参数）  
**响应格式**: ResponseDTO<AreaVO>

```javascript
areaApi.getDetail(1).then(response => {
  console.log(response.data);
});
```

### 新增区域

**HTTP方法**: POST  
**URL路径**: `/system/area/add`  
**权限要求**: `area:add`  
**请求参数**: AreaAddForm对象  
**响应格式**: ResponseDTO<String>

```javascript
const param = {
  areaName: "新区域",
  areaCode: "AREA001",
  parentId: 0,
  sort: 1
};
areaApi.add(param).then(response => {
  console.log(response.msg);
});
```

### 更新区域

**HTTP方法**: POST  
**URL路径**: `/system/area/update`  
**权限要求**: `area:update`  
**请求参数**: AreaUpdateForm对象  
**响应格式**: ResponseDTO<String>

```javascript
const param = {
  areaId: 1,
  areaName: "更新后的区域",
  sort: 2
};
areaApi.update(param).then(response => {
  console.log(response.msg);
});
```

### 删除区域

**HTTP方法**: POST  
**URL路径**: `/system/area/delete/{areaId}`  
**权限要求**: `area:delete`  
**请求参数**: areaId（路径参数）  
**响应格式**: ResponseDTO<String>

```javascript
areaApi.delete(1).then(response => {
  console.log(response.msg);
});
```

**本节来源**
- [area-api.js](file://smart-admin-web-javascript\src\api\system\area-api.js#L1-L59)

## 部门管理API

### 查询部门树形列表

**HTTP方法**: GET  
**URL路径**: `/department/treeList`  
**权限要求**: 无  
**请求参数**: 无  
**响应格式**: ResponseDTO<List<DepartmentTreeVO>>

```javascript
departmentApi.queryDepartmentTree().then(response => {
  console.log(response.data);
});
```

### 查询部门列表

**HTTP方法**: GET  
**URL路径**: `/department/listAll`  
**权限要求**: 无  
**请求参数**: 无  
**响应格式**: ResponseDTO<List<DepartmentVO>>

```javascript
departmentApi.queryAllDepartment().then(response => {
  console.log(response.data);
});
```

### 添加部门

**HTTP方法**: POST  
**URL路径**: `/department/add`  
**权限要求**: `system:department:add`  
**请求参数**: DepartmentAddForm对象  
**响应格式**: ResponseDTO<String>

```javascript
const param = {
  departmentName: "技术部",
  parentId: 0,
  sort: 1
};
departmentApi.addDepartment(param).then(response => {
  console.log(response.msg);
});
```

### 更新部门

**HTTP方法**: POST  
**URL路径**: `/department/update`  
**权限要求**: `system:department:update`  
**请求参数**: DepartmentUpdateForm对象  
**响应格式**: ResponseDTO<String>

```javascript
const param = {
  departmentId: 1,
  departmentName: "研发部",
  sort: 2
};
departmentApi.updateDepartment(param).then(response => {
  console.log(response.msg);
});
```

### 删除部门

**HTTP方法**: GET  
**URL路径**: `/department/delete/{departmentId}`  
**权限要求**: `system:department:delete`  
**请求参数**: departmentId（路径参数）  
**响应格式**: ResponseDTO<String>

```javascript
departmentApi.deleteDepartment(1).then(response => {
  console.log(response.msg);
});
```

**本节来源**
- [DepartmentController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\department\controller\DepartmentController.java#L35-L66)
- [department-api.js](file://smart-admin-web-javascript\src\api\system\department-api.js#L1-L45)

## 员工管理API

### 员工分页查询

**HTTP方法**: POST  
**URL路径**: `/employee/query`  
**权限要求**: 无  
**请求参数**: EmployeeQueryForm对象  
**响应格式**: ResponseDTO<PageResult<EmployeeVO>>

```javascript
const query = {
  pageNum: 1,
  pageSize: 10,
  employeeName: "张三"
};
employeeApi.query(query).then(response => {
  console.log(response.data.list);
});
```

### 添加员工

**HTTP方法**: POST  
**URL路径**: `/employee/add`  
**权限要求**: `system:employee:add`  
**请求参数**: EmployeeAddForm对象  
**响应格式**: ResponseDTO<String>

```javascript
const param = {
  employeeName: "李四",
  departmentId: 1,
  positionId: 1,
  phone: "13800138000"
};
employeeApi.addEmployee(param).then(response => {
  console.log(response.msg);
});
```

### 更新员工

**HTTP方法**: POST  
**URL路径**: `/employee/update`  
**权限要求**: `system:employee:update`  
**请求参数**: EmployeeUpdateForm对象  
**响应格式**: ResponseDTO<String>

```javascript
const param = {
  employeeId: 1,
  employeeName: "李四更新",
  phone: "13800138001"
};
employeeApi.updateEmployee(param).then(response => {
  console.log(response.msg);
});
```

### 批量删除员工

**HTTP方法**: POST  
**URL路径**: `/employee/update/batch/delete`  
**权限要求**: `system:employee:delete`  
**请求参数**: List<Long> employeeIdList  
**响应格式**: ResponseDTO<String>

```javascript
const employeeIdList = [1, 2, 3];
employeeApi.batchDeleteEmployee(employeeIdList).then(response => {
  console.log(response.msg);
});
```

### 更新员工状态

**HTTP方法**: GET  
**URL路径**: `/employee/update/disabled/{employeeId}`  
**权限要求**: `system:employee:disabled`  
**请求参数**: employeeId（路径参数）  
**响应格式**: ResponseDTO<String>

```javascript
employeeApi.updateDisableFlag(1).then(response => {
  console.log(response.msg);
});
```

**本节来源**
- [EmployeeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\employee\controller\EmployeeController.java#L40-L127)
- [employee-api.js](file://smart-admin-web-javascript\src\api\system\employee-api.js)

## 菜单管理API

### 查询菜单树

**HTTP方法**: GET  
**URL路径**: `/menu/tree`  
**权限要求**: 无  
**请求参数**: onlyMenu（查询参数）  
**响应格式**: ResponseDTO<List<MenuTreeVO>>

```javascript
menuApi.queryMenuTree(true).then(response => {
  console.log(response.data);
});
```

### 查询所有菜单

**HTTP方法**: GET  
**URL路径**: `/menu/query`  
**权限要求**: 无  
**请求参数**: 无  
**响应格式**: ResponseDTO<List<MenuVO>>

```javascript
menuApi.queryMenu().then(response => {
  console.log(response.data);
});
```

### 添加菜单

**HTTP方法**: POST  
**URL路径**: `/menu/add`  
**权限要求**: `system:menu:add`  
**请求参数**: MenuAddForm对象  
**响应格式**: ResponseDTO<String>

```javascript
const param = {
  menuName: "新菜单",
  parentMenuId: 0,
  path: "new-menu",
  component: "system/new-menu/index.vue",
  perms: "system:new-menu:page"
};
menuApi.addMenu(param).then(response => {
  console.log(response.msg);
});
```

### 更新菜单

**HTTP方法**: POST  
**URL路径**: `/menu/update`  
**权限要求**: `system:menu:update`  
**请求参数**: MenuUpdateForm对象  
**响应格式**: ResponseDTO<String>

```javascript
const param = {
  menuId: 1,
  menuName: "更新菜单",
  sort: 2
};
menuApi.updateMenu(param).then(response => {
  console.log(response.msg);
});
```

### 批量删除菜单

**HTTP方法**: GET  
**URL路径**: `/menu/batchDelete`  
**权限要求**: `system:menu:delete`  
**请求参数**: menuIdList（查询参数）  
**响应格式**: ResponseDTO<String>

```javascript
menuApi.batchDeleteMenu("1,2,3").then(response => {
  console.log(response.msg);
});
```

**本节来源**
- [MenuController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\menu\controller\MenuController.java)
- [menu-api.js](file://smart-admin-web-javascript\src\api\system\menu-api.js#L1-L55)

## 角色管理API

### 查询所有角色

**HTTP方法**: GET  
**URL路径**: `/role/getAll`  
**权限要求**: 无  
**请求参数**: 无  
**响应格式**: ResponseDTO<List<RoleVO>>

```javascript
roleApi.queryAll().then(response => {
  console.log(response.data);
});
```

### 添加角色

**HTTP方法**: POST  
**URL路径**: `/role/add`  
**权限要求**: `system:role:add`  
**请求参数**: RoleAddForm对象  
**响应格式**: ResponseDTO<String>

```javascript
const data = {
  roleName: "管理员",
  remark: "系统管理员角色"
};
roleApi.addRole(data).then(response => {
  console.log(response.msg);
});
```

### 更新角色

**HTTP方法**: POST  
**URL路径**: `/role/update`  
**权限要求**: `system:role:update`  
**请求参数**: RoleUpdateForm对象  
**响应格式**: ResponseDTO<String>

```javascript
const data = {
  roleId: 1,
  roleName: "超级管理员",
  remark: "最高权限角色"
};
roleApi.updateRole(data).then(response => {
  console.log(response.msg);
});
```

### 删除角色

**HTTP方法**: GET  
**URL路径**: `/role/delete/{roleId}`  
**权限要求**: `system:role:delete`  
**请求参数**: roleId（路径参数）  
**响应格式**: ResponseDTO<String>

```javascript
roleApi.deleteRole(1).then(response => {
  console.log(response.msg);
});
```

### 角色成员管理

**批量添加角色成员**: POST `/role/employee/batchAddRoleEmployee`  
**批量移除角色成员**: POST `/role/employee/batchRemoveRoleEmployee`  
**查询角色成员**: POST `/role/employee/queryEmployee`

```javascript
// 批量添加角色成员
const data = {
  roleId: 1,
  employeeIdList: [1, 2, 3]
};
roleApi.batchAddRoleEmployee(data).then(response => {
  console.log(response.msg);
});
```

**本节来源**
- [RoleController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\role\controller\RoleController.java)
- [role-api.js](file://smart-admin-web-javascript\src\api\system\role-api.js#L1-L85)

## 前端调用示例

### curl命令示例

```bash
# 查询部门列表
curl -X GET "http://localhost:8080/department/listAll" \
  -H "Authorization: Bearer your-token" \
  -H "Content-Type: application/json"

# 添加部门
curl -X POST "http://localhost:8080/department/add" \
  -H "Authorization: Bearer your-token" \
  -H "Content-Type: application/json" \
  -d '{
    "departmentName": "测试部门",
    "parentId": 0,
    "sort": 1
  }'

# 分页查询员工
curl -X POST "http://localhost:8080/employee/query" \
  -H "Authorization: Bearer your-token" \
  -H "Content-Type: application/json" \
  -d '{
    "pageNum": 1,
    "pageSize": 10
  }'
```

### 前端调用代码片段

```javascript
import { areaApi, departmentApi, employeeApi } from '/@/api/system';

// 区域管理 - 分页查询
async function queryAreas() {
  try {
    const param = {
      pageNum: 1,
      pageSize: 10
    };
    const response = await areaApi.queryPage(param);
    if (response.ok) {
      console.log('查询成功:', response.data.list);
      return response.data;
    } else {
      console.error('查询失败:', response.msg);
      return null;
    }
  } catch (error) {
    console.error('请求异常:', error);
    return null;
  }
}

// 部门管理 - 添加部门
async function addDepartment(departmentName, parentId = 0) {
  try {
    const param = {
      departmentName,
      parentId,
      sort: 1
    };
    const response = await departmentApi.addDepartment(param);
    if (response.ok) {
      console.log('添加成功:', response.msg);
      return true;
    } else {
      console.error('添加失败:', response.msg);
      return false;
    }
  } catch (error) {
    console.error('请求异常:', error);
    return false;
  }
}

// 员工管理 - 更新员工
async function updateEmployeeInfo(employeeId, phone) {
  try {
    const param = {
      employeeId,
      phone
    };
    const response = await employeeApi.updateEmployee(param);
    return response;
  } catch (error) {
    console.error('更新失败:', error);
    throw error;
  }
}

// 统一错误处理
function handleApiResponse(response) {
  if (response.ok) {
    return {
      success: true,
      data: response.data,
      message: response.msg
    };
  } else {
    return {
      success: false,
      data: null,
      message: response.msg || '操作失败'
    };
  }
}
```

### Vue组件中使用示例

```vue
<template>
  <div>
    <a-table
      :columns="columns"
      :data-source="employeeList"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { employeeApi } from '/@/api/system';

const employeeList = ref([]);
const loading = ref(false);
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
});

// 查询员工列表
const queryEmployees = async () => {
  loading.value = true;
  try {
    const param = {
      pageNum: pagination.value.current,
      pageSize: pagination.value.pageSize
    };
    const response = await employeeApi.query(param);
    if (response.ok) {
      employeeList.value = response.data.list;
      pagination.value.total = response.data.total;
    }
  } catch (error) {
    console.error('查询员工失败:', error);
  } finally {
    loading.value = false;
  }
};

// 表格分页变化
const handleTableChange = (pag) => {
  pagination.value = pag;
  queryEmployees();
};

// 页面加载时查询数据
onMounted(() => {
  queryEmployees();
});
</script>
```

**本节来源**
- [area-api.js](file://smart-admin-web-javascript\src\api\system\area-api.js)
- [department-api.js](file://smart-admin-web-javascript\src\api\system\department-api.js)
- [employee-api.js](file://smart-admin-web-javascript\src\api\system\employee-api.js)