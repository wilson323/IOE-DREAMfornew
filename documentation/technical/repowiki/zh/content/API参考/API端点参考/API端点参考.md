# API端点参考

<cite>
**本文档引用的文件**
- [CategoryController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\category\controller\CategoryController.java)
- [DepartmentController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\department\controller\DepartmentController.java)
- [AdminApiEncryptController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\support\AdminApiEncryptController.java)
- [category-api.js](file://smart-admin-web-javascript\src\api\business\category\category-api.js)
- [department-api.js](file://smart-admin-web-javascript\src\api\system\department-api.js)
- [api-encrypt-api.js](file://smart-admin-web-javascript\src\api\support\api-encrypt-api.js)
- [CategoryAddForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\category\domain\form\CategoryAddForm.java)
- [CategoryUpdateForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\category\domain\form\CategoryUpdateForm.java)
- [CategoryTreeQueryForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\category\domain\form\CategoryTreeQueryForm.java)
- [DepartmentAddForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\department\domain\form\DepartmentAddForm.java)
- [DepartmentUpdateForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\department\domain\form\DepartmentUpdateForm.java)
- [JweForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\support\AdminApiEncryptController.java#L68-L79)
- [AdminSwaggerTagConst.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\constant\AdminSwaggerTagConst.java)
- [SwaggerTagConst.java](file://smart-admin-api-java17-springboot3\sa-base\src\main\java\net\lab1024\sa\base\constant\SwaggerTagConst.java)
</cite>

## 目录
1. [业务管理模块](#业务管理模块)
2. [系统管理模块](#系统管理模块)
3. [支持功能模块](#支持功能模块)

## 业务管理模块

本模块包含ERP进销存系统的分类管理功能，提供类目的增删改查及层级树查询接口。

### 类目管理接口

#### 添加类目
- **HTTP方法**: POST
- **URL路径**: `/category/add`
- **请求头要求**: 需要认证令牌，权限要求 `category:add`
- **请求参数**: 请求体（JSON格式）
- **请求体JSON结构**:
  ```json
  {
    "categoryName": "类目名称",
    "categoryType": 1,
    "parentId": 1,
    "sort": 1,
    "remark": "备注",
    "disabledFlag": false
  }
  ```
  - `categoryName`: 类目名称（必填，最多20字符）
  - `categoryType`: 分类类型（必填，参考CategoryTypeEnum）
  - `parentId`: 父级类目id（可选）
  - `sort`: 排序值（可选）
  - `remark`: 备注（可选，最多200字符）
  - `disabledFlag`: 禁用状态（必填）
- **响应格式**: 
  ```json
  {
    "code": 1,
    "ok": true,
    "msg": "操作成功",
    "data": "添加成功"
  }
  ```
- **可能的错误码**:
  - 401: 未授权访问
  - 403: 权限不足
  - 400: 参数验证失败（如类目名称为空或超长）

**前端调用示例**:
```javascript
import { categoryApi } from '/@/api/business/category/category-api.js';

// 添加类目
const addCategory = async () => {
  const param = {
    categoryName: '电子产品',
    categoryType: 1,
    parentId: null,
    sort: 1,
    remark: '电子产品分类',
    disabledFlag: false
  };
  
  try {
    const res = await categoryApi.addCategory(param);
    if (res.ok) {
      console.log('类目添加成功');
    }
  } catch (error) {
    console.error('添加失败:', error);
  }
};
```

**Section sources**
- [CategoryController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\category\controller\CategoryController.java#L35-L40)
- [category-api.js](file://smart-admin-web-javascript\src\api\business\category\category-api.js#L14-L16)
- [CategoryAddForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\category\domain\form\CategoryAddForm.java)

#### 更新类目
- **HTTP方法**: POST
- **URL路径**: `/category/update`
- **请求头要求**: 需要认证令牌，权限要求 `category:update`
- **请求参数**: 请求体（JSON格式）
- **请求体JSON结构**:
  ```json
  {
    "categoryId": 1,
    "categoryName": "更新后的类目名称",
    "categoryType": 1,
    "parentId": 1,
    "sort": 1,
    "remark": "更新后的备注",
    "disabledFlag": false
  }
  ```
  - `categoryId`: 类目id（必填）
  - 其他字段同添加类目接口
- **响应格式**: 
  ```json
  {
    "code": 1,
    "ok": true,
    "msg": "操作成功",
    "data": "更新成功"
  }
  ```
- **可能的错误码**:
  - 401: 未授权访问
  - 403: 权限不足
  - 400: 参数验证失败
  - 404: 类目不存在

**前端调用示例**:
```javascript
// 更新类目
const updateCategory = async () => {
  const param = {
    categoryId: 1,
    categoryName: '更新后的电子产品',
    categoryType: 1,
    sort: 2,
    disabledFlag: false
  };
  
  try {
    const res = await categoryApi.updateCategory(param);
    if (res.ok) {
      console.log('类目更新成功');
    }
  } catch (error) {
    console.error('更新失败:', error);
  }
};
```

**Section sources**
- [CategoryController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\category\controller\CategoryController.java#L42-L47)
- [category-api.js](file://smart-admin-web-javascript\src\api\business\category\category-api.js#L26-L28)
- [CategoryUpdateForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\category\domain\form\CategoryUpdateForm.java)

#### 查询类目详情
- **HTTP方法**: GET
- **URL路径**: `/category/{categoryId}`
- **请求头要求**: 需要认证令牌
- **请求参数**: 路径参数
  - `categoryId`: 类目id
- **响应格式**: 
  ```json
  {
    "code": 1,
    "ok": true,
    "msg": "操作成功",
    "data": {
      "categoryId": 1,
      "categoryName": "电子产品",
      "categoryType": 1,
      "parentId": null,
      "sort": 1,
      "remark": "电子产品分类",
      "disabledFlag": false,
      "createTime": "2023-01-01T00:00:00",
      "updateTime": "2023-01-01T00:00:00"
    }
  }
  ```

**Section sources**
- [CategoryController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\category\controller\CategoryController.java#L49-L53)

#### 查询类目层级树
- **HTTP方法**: POST
- **URL路径**: `/category/tree`
- **请求头要求**: 需要认证令牌，权限要求 `category:tree`
- **请求参数**: 请求体（JSON格式）
- **请求体JSON结构**:
  ```json
  {
    "categoryType": 1,
    "parentId": 1
  }
  ```
  - `categoryType`: 分类类型（可选）
  - `parentId`: 父级类目id（可选）
- **响应格式**: 
  ```json
  {
    "code": 1,
    "ok": true,
    "msg": "操作成功",
    "data": [
      {
        "categoryId": 1,
        "categoryName": "电子产品",
        "categoryFullName": "电子产品",
        "parentId": null,
        "value": 1,
        "label": "电子产品",
        "children": [
          {
            "categoryId": 2,
            "categoryName": "手机",
            "categoryFullName": "电子产品/手机",
            "parentId": 1,
            "value": 2,
            "label": "手机",
            "children": []
          }
        ]
      }
    ]
  }
  ```

**前端调用示例**:
```javascript
// 查询类目层级树
const queryCategoryTree = async () => {
  const param = {
    categoryType: 1
  };
  
  try {
    const res = await categoryApi.queryCategoryTree(param);
    if (res.ok) {
      console.log('类目树:', res.data);
      // 处理类目树数据
      renderCategoryTree(res.data);
    }
  } catch (error) {
    console.error('查询失败:', error);
  }
};
```

**Section sources**
- [CategoryController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\category\controller\CategoryController.java#L55-L60)
- [category-api.js](file://smart-admin-web-javascript\src\api\business\category\category-api.js#L23-L25)
- [CategoryTreeQueryForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\category\domain\form\CategoryTreeQueryForm.java)

#### 删除类目
- **HTTP方法**: GET
- **URL路径**: `/category/delete/{categoryId}`
- **请求头要求**: 需要认证令牌，权限要求 `category:delete`
- **请求参数**: 路径参数
  - `categoryId`: 类目id
- **响应格式**: 
  ```json
  {
    "code": 1,
    "ok": true,
    "msg": "操作成功",
    "data": "删除成功"
  }
  ```

**前端调用示例**:
```javascript
// 删除类目
const deleteCategory = async (categoryId) => {
  try {
    const res = await categoryApi.deleteCategoryById(categoryId);
    if (res.ok) {
      console.log('类目删除成功');
      // 刷新类目列表
      refreshCategoryList();
    }
  } catch (error) {
    console.error('删除失败:', error);
  }
};
```

**Section sources**
- [CategoryController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\category\controller\CategoryController.java#L62-L67)
- [category-api.js](file://smart-admin-web-javascript\src\api\business\category\category-api.js#L19-L21)

## 系统管理模块

本模块包含部门管理功能，提供部门的增删改查及树形结构查询接口。

### 部门管理接口

#### 查询部门树形列表
- **HTTP方法**: GET
- **URL路径**: `/department/treeList`
- **请求头要求**: 需要认证令牌
- **请求参数**: 无
- **响应格式**: 
  ```json
  {
    "code": 1,
    "ok": true,
    "msg": "操作成功",
    "data": [
      {
        "departmentId": 1,
        "departmentName": "技术部",
        "managerId": 1,
        "managerName": "张三",
        "parentId": null,
        "sort": 1,
        "createTime": "2023-01-01T00:00:00",
        "updateTime": "2023-01-01T00:00:00",
        "preId": null,
        "nextId": 2,
        "children": [
          {
            "departmentId": 2,
            "departmentName": "前端组",
            "managerId": 2,
            "managerName": "李四",
            "parentId": 1,
            "sort": 1,
            "createTime": "2023-01-01T00:00:00",
            "updateTime": "2023-01-01T00:00:00",
            "preId": null,
            "nextId": null,
            "children": []
          }
        ],
        "selfAndAllChildrenIdList": [1, 2]
      }
    ]
  }
  ```

**前端调用示例**:
```javascript
import { departmentApi } from '/@/api/system/department-api.js';

// 查询部门树形列表
const queryDepartmentTree = async () => {
  try {
    const res = await departmentApi.queryDepartmentTree();
    if (res.ok) {
      console.log('部门树:', res.data);
      // 渲染部门树
      renderDepartmentTree(res.data);
    }
  } catch (error) {
    console.error('查询失败:', error);
  }
};
```

**Section sources**
- [DepartmentController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\department\controller\DepartmentController.java#L34-L38)
- [department-api.js](file://smart-admin-web-javascript\src\api\system\department-api.js#L23-L25)

#### 添加部门
- **HTTP方法**: POST
- **URL路径**: `/department/add`
- **请求头要求**: 需要认证令牌，权限要求 `system:department:add`
- **请求参数**: 请求体（JSON格式）
- **请求体JSON结构**:
  ```json
  {
    "departmentName": "部门名称",
    "sort": 1,
    "managerId": 1,
    "parentId": 1
  }
  ```
  - `departmentName`: 部门名称（必填，1-50字符）
  - `sort`: 排序值（必填）
  - `managerId`: 部门负责人id（可选）
  - `parentId`: 上级部门id（可选）
- **响应格式**: 
  ```json
  {
    "code": 1,
    "ok": true,
    "msg": "操作成功",
    "data": "添加成功"
  }
  ```

**前端调用示例**:
```javascript
// 添加部门
const addDepartment = async () => {
  const param = {
    departmentName: '测试部',
    sort: 1,
    managerId: 3,
    parentId: 1
  };
  
  try {
    const res = await departmentApi.addDepartment(param);
    if (res.ok) {
      console.log('部门添加成功');
      // 刷新部门列表
      refreshDepartmentList();
    }
  } catch (error) {
    console.error('添加失败:', error);
  }
};
```

**Section sources**
- [DepartmentController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\department\controller\DepartmentController.java#L40-L45)
- [department-api.js](file://smart-admin-web-javascript\src\api\system\department-api.js#L30-L32)
- [DepartmentAddForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\department\domain\form\DepartmentAddForm.java)

#### 更新部门
- **HTTP方法**: POST
- **URL路径**: `/department/update`
- **请求头要求**: 需要认证令牌，权限要求 `system:department:update`
- **请求参数**: 请求体（JSON格式）
- **请求体JSON结构**:
  ```json
  {
    "departmentId": 1,
    "departmentName": "更新后的部门名称",
    "sort": 2,
    "managerId": 2,
    "parentId": 1
  }
  ```
  - `departmentId`: 部门id（必填）
  - 其他字段同添加部门接口
- **响应格式**: 
  ```json
  {
    "code": 1,
    "ok": true,
    "msg": "操作成功",
    "data": "更新成功"
  }
  ```

**前端调用示例**:
```javascript
// 更新部门
const updateDepartment = async () => {
  const param = {
    departmentId: 1,
    departmentName: '更新后的技术部',
    sort: 2,
    managerId: 4
  };
  
  try {
    const res = await departmentApi.updateDepartment(param);
    if (res.ok) {
      console.log('部门更新成功');
      // 刷新部门列表
      refreshDepartmentList();
    }
  } catch (error) {
    console.error('更新失败:', error);
  }
};
```

**Section sources**
- [DepartmentController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\department\controller\DepartmentController.java#L47-L52)
- [department-api.js](file://smart-admin-web-javascript\src\api\system\department-api.js#L36-L38)
- [DepartmentUpdateForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\department\domain\form\DepartmentUpdateForm.java)

#### 删除部门
- **HTTP方法**: GET
- **URL路径**: `/department/delete/{departmentId}`
- **请求头要求**: 需要认证令牌，权限要求 `system:department:delete`
- **请求参数**: 路径参数
  - `departmentId`: 部门id
- **响应格式**: 
  ```json
  {
    "code": 1,
    "ok": true,
    "msg": "操作成功",
    "data": "删除成功"
  }
  ```

**前端调用示例**:
```javascript
// 删除部门
const deleteDepartment = async (departmentId) => {
  try {
    const res = await departmentApi.deleteDepartment(departmentId);
    if (res.ok) {
      console.log('部门删除成功');
      // 刷新部门列表
      refreshDepartmentList();
    }
  } catch (error) {
    console.error('删除失败:', error);
  }
};
```

**Section sources**
- [DepartmentController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\department\controller\DepartmentController.java#L54-L59)
- [department-api.js](file://smart-admin-web-javascript\src\api\system\department-api.js#L42-L44)

#### 查询部门列表
- **HTTP方法**: GET
- **URL路径**: `/department/listAll`
- **请求头要求**: 需要认证令牌
- **请求参数**: 无
- **响应格式**: 
  ```json
  {
    "code": 1,
    "ok": true,
    "msg": "操作成功",
    "data": [
      {
        "departmentId": 1,
        "departmentName": "技术部",
        "managerId": 1,
        "managerName": "张三",
        "parentId": null,
        "sort": 1,
        "createTime": "2023-01-01T00:00:00",
        "updateTime": "2023-01-01T00:00:00"
      }
    ]
  }
  ```

**Section sources**
- [DepartmentController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\department\controller\DepartmentController.java#L61-L65)

## 支持功能模块

本模块包含API加密测试功能，用于测试请求和响应的加密解密。

### 加密测试接口

#### 测试请求加密
- **HTTP方法**: POST
- **URL路径**: `/apiEncrypt/testRequestEncrypt`
- **请求头要求**: 需要认证令牌，请求数据需要加密
- **请求参数**: 请求体（加密后的JSON数据）
- **请求体JSON结构**:
  ```json
  {
    "name": "姓名",
    "age": 25
  }
  ```
  - `name`: 姓名（必填）
  - `age`: 年龄（必填，大于等于1）
- **响应格式**: 
  ```json
  {
    "code": 1,
    "ok": true,
    "msg": "操作成功",
    "data": {
      "name": "姓名",
      "age": 25
    },
    "dataType": "ENCRYPT"
  }
  ```

**前端调用示例**:
```javascript
import { encryptApi } from '/@/api/support/api-encrypt-api.js';

// 测试请求加密
const testRequestEncrypt = async () => {
  const param = {
    name: '张三',
    age: 25
  };
  
  try {
    const res = await encryptApi.testRequestEncrypt(param);
    if (res.ok) {
      console.log('请求加密测试成功:', res.data);
    }
  } catch (error) {
    console.error('测试失败:', error);
  }
};
```

**Section sources**
- [AdminApiEncryptController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\support\AdminApiEncryptController.java#L38-L42)
- [api-encrypt-api.js](file://smart-admin-web-javascript\src\api\support\api-encrypt-api.js#L15-L17)
- [JweForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\support\AdminApiEncryptController.java#L68-L79)

#### 测试返回加密
- **HTTP方法**: POST
- **URL路径**: `/apiEncrypt/testResponseEncrypt`
- **请求头要求**: 需要认证令牌
- **请求参数**: 请求体（JSON格式）
- **请求体JSON结构**:
  ```json
  {
    "name": "姓名",
    "age": 25
  }
  ```
- **响应格式**: 返回加密后的数据（base64编码的JWE格式）

**前端调用示例**:
```javascript
// 测试返回加密
const testResponseEncrypt = async () => {
  const param = {
    name: '李四',
    age: 30
  };
  
  try {
    const res = await encryptApi.testResponseEncrypt(param);
    if (res.ok) {
      console.log('返回加密测试成功，数据已加密');
      // 响应数据已被自动解密
      console.log('解密后数据:', res.data);
    }
  } catch (error) {
    console.error('测试失败:', error);
  }
};
```

**Section sources**
- [AdminApiEncryptController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\support\AdminApiEncryptController.java#L44-L49)
- [api-encrypt-api.js](file://smart-admin-web-javascript\src\api\support\api-encrypt-api.js#L22-L24)

#### 测试请求和返回加密解密
- **HTTP方法**: POST
- **URL路径**: `/apiEncrypt/testDecryptAndEncrypt`
- **请求头要求**: 需要认证令牌，请求数据需要加密
- **请求参数**: 请求体（加密后的JSON数据）
- **请求体JSON结构**: 同"测试请求加密"接口
- **响应格式**: 返回加密后的数据

**前端调用示例**:
```javascript
// 测试请求和返回加密解密
const testDecryptAndEncrypt = async () => {
  const param = {
    name: '王五',
    age: 35
  };
  
  try {
    const res = await encryptApi.testDecryptAndEncrypt(param);
    if (res.ok) {
      console.log('双向加密测试成功');
    }
  } catch (error) {
    console.error('测试失败:', error);
  }
};
```

**Section sources**
- [AdminApiEncryptController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\support\AdminApiEncryptController.java#L51-L57)
- [api-encrypt-api.js](file://smart-admin-web-javascript\src\api\support\api-encrypt-api.js#L29-L31)

#### 测试数组加密解密
- **HTTP方法**: POST
- **URL路径**: `/apiEncrypt/testArray`
- **请求头要求**: 需要认证令牌，请求数据需要加密
- **请求参数**: 请求体（加密后的JSON数组）
- **请求体JSON结构**:
  ```json
  [
    {
      "name": "张三",
      "age": 25
    },
    {
      "name": "李四",
      "age": 30
    }
  ]
  ```
- **响应格式**: 返回加密后的数组数据

**前端调用示例**:
```javascript
// 测试数组加密解密
const testArrayEncrypt = async () => {
  const param = [
    { name: '张三', age: 25 },
    { name: '李四', age: 30 }
  ];
  
  try {
    const res = await encryptApi.testArray(param);
    if (res.ok) {
      console.log('数组加密测试成功');
    }
  } catch (error) {
    console.error('测试失败:', error);
  }
};
```

**Section sources**
- [AdminApiEncryptController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\system\support\AdminApiEncryptController.java#L59-L65)
- [api-encrypt-api.js](file://smart-admin-web-javascript\src\api\support\api-encrypt-api.js#L36-L38)