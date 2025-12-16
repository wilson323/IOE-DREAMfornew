# 业务管理API

<cite>
**本文档引用的文件**
- [CategoryController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\category\controller\CategoryController.java)
- [GoodsController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\goods\controller\GoodsController.java)
- [NoticeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\controller\NoticeController.java)
- [category-api.js](file://smart-admin-web-javascript\src\api\business\category\category-api.js)
- [goods-api.js](file://smart-admin-web-javascript\src\api\business\goods\goods-api.js)
- [notice-api.js](file://smart-admin-web-javascript\src\api\business\oa\notice-api.js)
- [CategoryService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\category\service\CategoryService.java)
- [GoodsService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\goods\service\GoodsService.java)
- [NoticeService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\service\NoticeService.java)
- [CategoryTypeEnum.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\category\constant\CategoryTypeEnum.java)
- [GoodsStatusEnum.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\goods\constant\GoodsStatusEnum.java)
- [NoticeVisibleRangeDataTypeEnum.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\constant\NoticeVisibleRangeDataTypeEnum.java)
</cite>

## 目录
1. [分类管理API](#分类管理api)
2. [商品管理API](#商品管理api)
3. [通知公告管理API](#通知公告管理api)
4. [前端调用示例](#前端调用示例)
5. [业务规则与数据校验](#业务规则与数据校验)

## 分类管理API

分类管理API提供了对商品分类树形结构的完整管理功能，支持增删改查、排序和状态变更等操作。该API主要用于管理商品分类体系，支持多级树形结构。

### 添加类目
用于创建新的分类节点。

**HTTP方法**: POST  
**URL路径**: `/category/add`  
**权限要求**: `category:add`

**请求体结构**:
```json
{
  "categoryName": "分类名称",
  "categoryType": 1,
  "parentId": 1,
  "sort": 1,
  "remark": "备注",
  "disabledFlag": false
}
```

**请求参数说明**:
- `categoryName`: 分类名称，必填，最多20字符
- `categoryType`: 分类类型，必填，参考[分类类型枚举](#分类类型枚举)
- `parentId`: 父级分类ID，可选，为空时表示根节点
- `sort`: 排序值，可选，默认为0
- `remark`: 备注，可选，最多200字符
- `disabledFlag`: 禁用状态，必填，true表示禁用，false表示启用

**响应格式**:
```json
{
  "code": 0,
  "ok": true,
  "data": "操作成功",
  "msg": null
}
```

**错误码**:
- `400`: 参数验证失败
- `403`: 无权限访问
- `500`: 服务器内部错误

**Section sources**
- [CategoryController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\category\controller\CategoryController.java#L35-L40)
- [CategoryService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\category\service\CategoryService.java#L29-L73)
- [CategoryAddForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\category\domain\form\CategoryAddForm.java)

### 更新类目
用于修改现有分类节点的信息。

**HTTP方法**: POST  
**URL路径**: `/category/update`  
**权限要求**: `category:update`

**请求体结构**:
```json
{
  "categoryId": 1,
  "categoryName": "分类名称",
  "categoryType": 1,
  "parentId": 1,
  "sort": 1,
  "remark": "备注",
  "disabledFlag": false
}
```

**请求参数说明**:
- `categoryId`: 分类ID，必填
- 其他字段同[添加类目](#添加类目)

**响应格式**:
```json
{
  "code": 0,
  "ok": true,
  "data": "操作成功",
  "msg": null
}
```

**错误码**:
- `400`: 参数验证失败
- `403`: 无权限访问
- `404`: 分类不存在
- `500`: 服务器内部错误

**Section sources**
- [CategoryController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\category\controller\CategoryController.java#L42-L47)
- [CategoryService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\category\service\CategoryService.java#L29-L73)
- [CategoryUpdateForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\category\domain\form\CategoryUpdateForm.java)

### 查询类目详情
获取指定分类的详细信息。

**HTTP方法**: GET  
**URL路径**: `/category/{categoryId}`

**路径参数**:
- `categoryId`: 分类ID，必填

**响应格式**:
```json
{
  "code": 0,
  "ok": true,
  "data": {
    "categoryName": "分类名称",
    "categoryType": 1,
    "parentId": 1,
    "sort": 1,
    "remark": "备注",
    "disabledFlag": false,
    "categoryId": 1,
    "updateTime": "2023-01-01T00:00:00",
    "createTime": "2023-01-01T00:00:00"
  },
  "msg": null
}
```

**错误码**:
- `400`: 参数验证失败
- `404`: 分类不存在
- `500`: 服务器内部错误

**Section sources**
- [CategoryController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\category\controller\CategoryController.java#L49-L53)
- [CategoryService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\category\service\CategoryService.java)
- [CategoryVO.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\category\domain\vo\CategoryVO.java)

### 查询类目层级树
获取分类的树形结构，支持按分类类型和父级ID进行筛选。

**HTTP方法**: POST  
**URL路径**: `/category/tree`  
**权限要求**: `category:tree`

**请求体结构**:
```json
{
  "categoryType": 1,
  "parentId": 1
}
```

**请求参数说明**:
- `categoryType`: 分类类型，可选，参考[分类类型枚举](#分类类型枚举)
- `parentId`: 父级分类ID，可选，用于获取指定节点下的子树

**响应格式**:
```json
{
  "code": 0,
  "ok": true,
  "data": [
    {
      "categoryId": 1,
      "categoryName": "一级分类",
      "categoryFullName": "一级分类",
      "parentId": 0,
      "value": 1,
      "label": "一级分类",
      "children": [
        {
          "categoryId": 2,
          "categoryName": "二级分类",
          "categoryFullName": "一级分类/二级分类",
          "parentId": 1,
          "value": 2,
          "label": "二级分类",
          "children": []
        }
      ]
    }
  ],
  "msg": null
}
```

**错误码**:
- `400`: 参数验证失败
- `403`: 无权限访问
- `500`: 服务器内部错误

**Section sources**
- [CategoryController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\category\controller\CategoryController.java#L55-L60)
- [CategoryService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\category\service\CategoryService.java)
- [CategoryTreeQueryForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\category\domain\form\CategoryTreeQueryForm.java)
- [CategoryTreeVO.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\category\domain\vo\CategoryTreeVO.java)

### 删除类目
删除指定的分类节点。

**HTTP方法**: GET  
**URL路径**: `/category/delete/{categoryId}`  
**权限要求**: `category:delete`

**路径参数**:
- `categoryId`: 分类ID，必填

**响应格式**:
```json
{
  "code": 0,
  "ok": true,
  "data": "操作成功",
  "msg": null
}
```

**错误码**:
- `400`: 参数验证失败
- `403`: 无权限访问
- `404`: 分类不存在
- `409`: 分类下存在子分类或关联商品，无法删除
- `500`: 服务器内部错误

**Section sources**
- [CategoryController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\category\controller\CategoryController.java#L62-L67)
- [CategoryService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\category\service\CategoryService.java)

### 分类类型枚举
分类类型定义了不同类型的分类体系。

| 值 | 描述 |
|----|------|
| 1 | 商品 |
| 2 | 自定义 |

**Section sources**
- [CategoryTypeEnum.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\category\constant\CategoryTypeEnum.java)

## 商品管理API

商品管理API提供了商品全生命周期的管理功能，包括上架/下架、库存管理、导入导出等操作。

### 分页查询商品
根据条件分页查询商品列表。

**HTTP方法**: POST  
**URL路径**: `/goods/query`  
**权限要求**: `goods:query`

**请求体结构**:
```json
{
  "categoryId": 1,
  "searchWord": "搜索关键词",
  "goodsStatus": 1,
  "place": "产地",
  "shelvesFlag": true,
  "current": 1,
  "pageSize": 10
}
```

**请求参数说明**:
- `categoryId`: 商品分类ID，可选
- `searchWord`: 搜索关键词，可选，最多30字符
- `goodsStatus`: 商品状态，可选，参考[商品状态枚举](#商品状态枚举)
- `place`: 产地，可选
- `shelvesFlag`: 上架状态，可选，true表示已上架，false表示已下架
- `current`: 当前页码，必填，默认为1
- `pageSize`: 每页大小，必填，默认为10

**响应格式**:
```json
{
  "code": 0,
  "ok": true,
  "data": {
    "list": [
      {
        "categoryId": 1,
        "goodsName": "商品名称",
        "goodsStatus": 1,
        "place": "产地",
        "price": 100.00,
        "shelvesFlag": true,
        "remark": "备注",
        "goodsId": 1,
        "categoryName": "分类名称",
        "updateTime": "2023-01-01T00:00:00",
        "createTime": "2023-01-01T00:00:00"
      }
    ],
    "total": 1,
    "current": 1,
    "size": 10,
    "pages": 1,
    "emptyFlag": false,
    "firstFlag": true,
    "lastFlag": false
  },
  "msg": null
}
```

**错误码**:
- `400`: 参数验证失败
- `403`: 无权限访问
- `500`: 服务器内部错误

**Section sources**
- [GoodsController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\goods\controller\GoodsController.java#L41-L46)
- [GoodsService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\goods\service\GoodsService.java#L128-L166)
- [GoodsQueryForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\goods\domain\form\GoodsQueryForm.java)
- [GoodsVO.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\goods\domain\vo\GoodsVO.java)

### 添加商品
创建新的商品。

**HTTP方法**: POST  
**URL路径**: `/goods/add`  
**权限要求**: `goods:add`

**请求体结构**:
```json
{
  "categoryId": 1,
  "goodsName": "商品名称",
  "goodsStatus": 1,
  "place": "产地",
  "price": 100.00,
  "shelvesFlag": true,
  "remark": "备注"
}
```

**请求参数说明**:
- `categoryId`: 商品分类ID，必填
- `goodsName`: 商品名称，必填
- `goodsStatus`: 商品状态，必填，参考[商品状态枚举](#商品状态枚举)
- `place`: 产地，可选
- `price`: 商品价格，可选
- `shelvesFlag`: 上架状态，必填，true表示上架，false表示下架
- `remark`: 备注，可选，最多200字符

**响应格式**:
```json
{
  "code": 0,
  "ok": true,
  "data": "操作成功",
  "msg": null
}
```

**错误码**:
- `400`: 参数验证失败
- `403`: 无权限访问
- `404`: 分类不存在
- `500`: 服务器内部错误

**Section sources**
- [GoodsController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\goods\controller\GoodsController.java#L48-L53)
- [GoodsService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\goods\service\GoodsService.java#L60-L100)
- [GoodsAddForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\goods\domain\form\GoodsAddForm.java)

### 更新商品
修改现有商品信息。

**HTTP方法**: POST  
**URL路径**: `/goods/update`  
**权限要求**: `goods:update`

**请求体结构**:
```json
{
  "goodsId": 1,
  "categoryId": 1,
  "goodsName": "商品名称",
  "goodsStatus": 1,
  "place": "产地",
  "price": 100.00,
  "shelvesFlag": true,
  "remark": "备注"
}
```

**请求参数说明**:
- `goodsId`: 商品ID，必填
- 其他字段同[添加商品](#添加商品)

**响应格式**:
```json
{
  "code": 0,
  "ok": true,
  "data": "操作成功",
  "msg": null
}
```

**错误码**:
- `400`: 参数验证失败
- `403`: 无权限访问
- `404`: 商品不存在
- `500`: 服务器内部错误

**Section sources**
- [GoodsController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\goods\controller\GoodsController.java#L55-L60)
- [GoodsService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\goods\service\GoodsService.java#L60-L100)
- [GoodsUpdateForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\goods\domain\form\GoodsUpdateForm.java)

### 删除商品
删除指定的商品。

**HTTP方法**: GET  
**URL路径**: `/goods/delete/{goodsId}`  
**权限要求**: `goods:delete`

**路径参数**:
- `goodsId`: 商品ID，必填

**响应格式**:
```json
{
  "code": 0,
  "ok": true,
  "data": "操作成功",
  "msg": null
}
```

**错误码**:
- `400`: 参数验证失败
- `403`: 无权限访问
- `404`: 商品不存在
- `500`: 服务器内部错误

**Section sources**
- [GoodsController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\goods\controller\GoodsController.java#L62-L67)
- [GoodsService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\goods\service\GoodsService.java)

### 批量删除商品
批量删除多个商品。

**HTTP方法**: POST  
**URL路径**: `/goods/batchDelete`  
**权限要求**: `goods:batchDelete`

**请求体结构**:
```json
[1, 2, 3]
```

**请求参数说明**:
- 请求体为商品ID数组

**响应格式**:
```json
{
  "code": 0,
  "ok": true,
  "data": "操作成功",
  "msg": null
}
```

**错误码**:
- `400`: 参数验证失败
- `403`: 无权限访问
- `500`: 服务器内部错误

**Section sources**
- [GoodsController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\goods\controller\GoodsController.java#L69-L74)
- [GoodsService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\goods\service\GoodsService.java#L128-L166)

### 导入商品
通过Excel文件批量导入商品数据。

**HTTP方法**: POST  
**URL路径**: `/goods/importGoods`  
**权限要求**: `goods:importGoods`

**请求参数**:
- `file`: Excel文件，multipart/form-data格式

**响应格式**:
```json
{
  "code": 0,
  "ok": true,
  "data": "导入成功，共导入10条数据",
  "msg": null
}
```

**错误码**:
- `400`: 文件格式错误或数据验证失败
- `403`: 无权限访问
- `413`: 文件大小超过限制
- `500`: 服务器内部错误

**Section sources**
- [GoodsController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\goods\controller\GoodsController.java#L78-L83)
- [GoodsService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\goods\service\GoodsService.java)

### 导出商品
导出商品数据到Excel文件。

**HTTP方法**: GET  
**URL路径**: `/goods/exportGoods`  
**权限要求**: `goods:exportGoods`

**响应格式**:
- 返回Excel文件流，文件名为"商品列表.xlsx"

**错误码**:
- `403`: 无权限访问
- `500`: 服务器内部错误

**Section sources**
- [GoodsController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\goods\controller\GoodsController.java#L85-L91)
- [GoodsService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\goods\service\GoodsService.java)

### 商品状态枚举
商品状态定义了商品在销售过程中的不同状态。

| 值 | 描述 |
|----|------|
| 1 | 预约中 |
| 2 | 售卖中 |
| 3 | 售罄 |

**Section sources**
- [GoodsStatusEnum.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\goods\constant\GoodsStatusEnum.java)

## 通知公告管理API

通知公告管理API提供了企业OA中通知公告的完整管理功能，包括发布、查看和阅读状态跟踪。

### 通知公告类型管理

#### 获取全部通知类型
获取所有通知公告类型。

**HTTP方法**: GET  
**URL路径**: `/oa/noticeType/getAll`

**响应格式**:
```json
{
  "code": 0,
  "ok": true,
  "data": [
    {
      "noticeTypeId": 1,
      "noticeTypeName": "公告"
    },
    {
      "noticeTypeId": 2,
      "noticeTypeName": "通知"
    }
  ],
  "msg": null
}
```

**错误码**:
- `500`: 服务器内部错误

**Section sources**
- [NoticeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\controller\NoticeController.java#L49-L53)
- [NoticeTypeService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\service\NoticeTypeService.java)

#### 添加通知类型
创建新的通知公告类型。

**HTTP方法**: GET  
**URL路径**: `/oa/noticeType/add/{name}`

**路径参数**:
- `name`: 类型名称，必填

**响应格式**:
```json
{
  "code": 0,
  "ok": true,
  "data": "操作成功",
  "msg": null
}
```

**错误码**:
- `400`: 参数验证失败
- `409`: 类型名称已存在
- `500`: 服务器内部错误

**Section sources**
- [NoticeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\controller\NoticeController.java#L55-L59)
- [NoticeTypeService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\service\NoticeTypeService.java)

#### 更新通知类型
修改现有通知公告类型。

**HTTP方法**: GET  
**URL路径**: `/oa/noticeType/update/{noticeTypeId}/{name}`

**路径参数**:
- `noticeTypeId`: 类型ID，必填
- `name`: 新的类型名称，必填

**响应格式**:
```json
{
  "code": 0,
  "ok": true,
  "data": "操作成功",
  "msg": null
}
```

**错误码**:
- `400`: 参数验证失败
- `404`: 类型不存在
- `409`: 类型名称已存在
- `500`: 服务器内部错误

**Section sources**
- [NoticeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\controller\NoticeController.java#L61-L65)
- [NoticeTypeService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\service\NoticeTypeService.java)

#### 删除通知类型
删除指定的通知公告类型。

**HTTP方法**: GET  
**URL路径**: `/oa/noticeType/delete/{noticeTypeId}`

**路径参数**:
- `noticeTypeId`: 类型ID，必填

**响应格式**:
```json
{
  "code": 0,
  "ok": true,
  "data": "操作成功",
  "msg": null
}
```

**错误码**:
- `400`: 参数验证失败
- `404`: 类型不存在
- `409`: 类型下存在通知公告，无法删除
- `500`: 服务器内部错误

**Section sources**
- [NoticeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\controller\NoticeController.java#L67-L71)
- [NoticeTypeService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\service\NoticeTypeService.java)

### 通知公告管理

#### 分页查询通知公告
根据条件分页查询通知公告列表。

**HTTP方法**: POST  
**URL路径**: `/oa/notice/query`  
**权限要求**: `oa:notice:query`

**请求体结构**:
```json
{
  "noticeTypeId": 1,
  "keywords": "搜索关键词",
  "documentNumber": "文号",
  "createUserName": "创建人",
  "deletedFlag": false,
  "createTimeBegin": "2023-01-01",
  "createTimeEnd": "2023-12-31",
  "publishTimeBegin": "2023-01-01",
  "publishTimeEnd": "2023-12-31",
  "current": 1,
  "pageSize": 10
}
```

**请求参数说明**:
- `noticeTypeId`: 通知类型ID，可选
- `keywords`: 关键词，可选，支持标题、作者、来源搜索
- `documentNumber`: 文号，可选
- `createUserName`: 创建人姓名，可选
- `deletedFlag`: 删除标识，可选，false表示未删除
- `createTimeBegin`: 创建时间开始，可选
- `createTimeEnd`: 创建时间结束，可选
- `publishTimeBegin`: 发布时间开始，可选
- `publishTimeEnd`: 发布时间结束，可选
- `current`: 当前页码，必填
- `pageSize`: 每页大小，必填

**响应格式**:
```json
{
  "code": 0,
  "ok": true,
  "data": {
    "list": [
      {
        "noticeId": 1,
        "title": "通知标题",
        "noticeTypeName": "公告",
        "author": "作者",
        "source": "来源",
        "documentNumber": "文号",
        "publishTime": "2023-01-01T00:00:00",
        "viewCount": 10,
        "topFlag": false,
        "createUserId": 1,
        "createUserName": "创建人",
        "createTime": "2023-01-01T00:00:00",
        "updateTime": "2023-01-01T00:00:00"
      }
    ],
    "total": 1,
    "current": 1,
    "size": 10,
    "pages": 1,
    "emptyFlag": false,
    "firstFlag": true,
    "lastFlag": false
  },
  "msg": null
}
```

**错误码**:
- `400`: 参数验证失败
- `403`: 无权限访问
- `500`: 服务器内部错误

**Section sources**
- [NoticeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\controller\NoticeController.java#L76-L81)
- [NoticeService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\service\NoticeService.java)
- [NoticeQueryForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\domain\form\NoticeQueryForm.java)
- [NoticeVO.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\domain\vo\NoticeVO.java)

#### 添加通知公告
发布新的通知公告。

**HTTP方法**: POST  
**URL路径**: `/oa/notice/add`  
**权限要求**: `oa:notice:add`  
**防重复提交**: 是

**请求体结构**:
```json
{
  "noticeTypeId": 1,
  "title": "通知标题",
  "author": "作者",
  "source": "来源",
  "documentNumber": "文号",
  "publishTime": "2023-01-01T00:00:00",
  "contentHtml": "<p>内容</p>",
  "contentText": "内容",
  "attachment": "文件key",
  "topFlag": false,
  "visibleRangeList": [
    {
      "dataType": 1,
      "dataId": 1
    }
  ]
}
```

**请求参数说明**:
- `noticeTypeId`: 通知类型ID，必填
- `title`: 标题，必填
- `author`: 作者，可选
- `source`: 来源，可选
- `documentNumber`: 文号，可选
- `publishTime`: 发布时间，可选
- `contentHtml`: HTML内容，必填
- `contentText`: 纯文本内容，必填
- `attachment`: 附件文件key，可选
- `topFlag`: 是否置顶，可选
- `visibleRangeList`: 可见范围列表，可选，参考[可见范围类型](#可见范围类型)

**响应格式**:
```json
{
  "code": 0,
  "ok": true,
  "data": "操作成功",
  "msg": null
}
```

**错误码**:
- `400`: 参数验证失败
- `403`: 无权限访问
- `409`: 重复提交
- `500`: 服务器内部错误

**Section sources**
- [NoticeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\controller\NoticeController.java#L83-L90)
- [NoticeService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\service\NoticeService.java)
- [NoticeAddForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\domain\form\NoticeAddForm.java)

#### 更新通知公告
修改已发布的通知公告。

**HTTP方法**: POST  
**URL路径**: `/oa/notice/update`  
**权限要求**: `oa:notice:update`  
**防重复提交**: 是

**请求体结构**:
```json
{
  "noticeId": 1,
  "noticeTypeId": 1,
  "title": "通知标题",
  "author": "作者",
  "source": "来源",
  "documentNumber": "文号",
  "publishTime": "2023-01-01T00:00:00",
  "contentHtml": "<p>内容</p>",
  "contentText": "内容",
  "attachment": "文件key",
  "topFlag": false,
  "visibleRangeList": [
    {
      "dataType": 1,
      "dataId": 1
    }
  ]
}
```

**请求参数说明**:
- `noticeId`: 通知ID，必填
- 其他字段同[添加通知公告](#添加通知公告)

**响应格式**:
```json
{
  "code": 0,
  "ok": true,
  "data": "操作成功",
  "msg": null
}
```

**错误码**:
- `400`: 参数验证失败
- `403`: 无权限访问
- `404`: 通知不存在
- `409`: 重复提交
- `500`: 服务器内部错误

**Section sources**
- [NoticeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\controller\NoticeController.java#L92-L98)
- [NoticeService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\service\NoticeService.java)
- [NoticeUpdateForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\domain\form\NoticeUpdateForm.java)

#### 获取更新表单数据
获取通知公告更新表单所需的数据。

**HTTP方法**: GET  
**URL路径**: `/oa/notice/getUpdateVO/{noticeId}`  
**权限要求**: `oa:notice:update`

**路径参数**:
- `noticeId`: 通知ID，必填

**响应格式**:
```json
{
  "code": 0,
  "ok": true,
  "data": {
    "noticeId": 1,
    "noticeTypeId": 1,
    "title": "通知标题",
    "author": "作者",
    "source": "来源",
    "documentNumber": "文号",
    "publishTime": "2023-01-01T00:00:00",
    "contentHtml": "<p>内容</p>",
    "contentText": "内容",
    "attachment": "文件key",
    "topFlag": false,
    "visibleRangeList": [
      {
        "dataType": 1,
        "dataId": 1,
        "dataName": "员工姓名"
      }
    ]
  },
  "msg": null
}
```

**错误码**:
- `400`: 参数验证失败
- `403`: 无权限访问
- `404`: 通知不存在
- `500`: 服务器内部错误

**Section sources**
- [NoticeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\controller\NoticeController.java#L100-L105)
- [NoticeService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\service\NoticeService.java)
- [NoticeUpdateFormVO.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\domain\vo\NoticeUpdateFormVO.java)

#### 删除通知公告
删除指定的通知公告。

**HTTP方法**: GET  
**URL路径**: `/oa/notice/delete/{noticeId}`  
**权限要求**: `oa:notice:delete`

**路径参数**:
- `noticeId`: 通知ID，必填

**响应格式**:
```json
{
  "code": 0,
  "ok": true,
  "data": "操作成功",
  "msg": null
}
```

**错误码**:
- `400`: 参数验证失败
- `403`: 无权限访问
- `404`: 通知不存在
- `500`: 服务器内部错误

**Section sources**
- [NoticeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\controller\NoticeController.java#L107-L112)
- [NoticeService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\service\NoticeService.java)

### 员工查看通知公告

#### 查看通知公告详情
员工查看指定通知公告的详细信息，并记录阅读状态。

**HTTP方法**: GET  
**URL路径**: `/oa/notice/employee/view/{noticeId}`

**路径参数**:
- `noticeId`: 通知ID，必填

**响应格式**:
```json
{
  "code": 0,
  "ok": true,
  "data": {
    "noticeId": 1,
    "title": "通知标题",
    "author": "作者",
    "source": "来源",
    "documentNumber": "文号",
    "publishTime": "2023-01-01T00:00:00",
    "contentHtml": "<p>内容</p>",
    "attachment": "文件key",
    "viewCount": 11,
    "hasViewed": true
  },
  "msg": null
}
```

**错误码**:
- `400`: 参数验证失败
- `404`: 通知不存在或无权查看
- `500`: 服务器内部错误

**Section sources**
- [NoticeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\controller\NoticeController.java#L117-L126)
- [NoticeEmployeeService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\service\NoticeEmployeeService.java)
- [NoticeDetailVO.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\domain\vo\NoticeDetailVO.java)

#### 查询员工通知公告
员工查询自己有权查看的通知公告列表。

**HTTP方法**: POST  
**URL路径**: `/oa/notice/employee/query`

**请求体结构**:
```json
{
  "noticeTypeId": 1,
  "keywords": "搜索关键词",
  "hasViewed": false,
  "current": 1,
  "pageSize": 10
}
```

**请求参数说明**:
- `noticeTypeId`: 通知类型ID，可选
- `keywords`: 关键词，可选
- `hasViewed`: 是否已阅读，可选
- `current`: 当前页码，必填
- `pageSize`: 每页大小，必填

**响应格式**:
```json
{
  "code": 0,
  "ok": true,
  "data": {
    "list": [
      {
        "noticeId": 1,
        "title": "通知标题",
        "noticeTypeName": "公告",
        "author": "作者",
        "publishTime": "2023-01-01T00:00:00",
        "hasViewed": true,
        "topFlag": false
      }
    ],
    "total": 1,
    "current": 1,
    "size": 10,
    "pages": 1,
    "emptyFlag": false,
    "firstFlag": true,
    "lastFlag": false
  },
  "msg": null
}
```

**错误码**:
- `400`: 参数验证失败
- `500`: 服务器内部错误

**Section sources**
- [NoticeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\controller\NoticeController.java#L128-L132)
- [NoticeEmployeeService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\service\NoticeEmployeeService.java)
- [NoticeEmployeeQueryForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\domain\form\NoticeEmployeeQueryForm.java)
- [NoticeEmployeeVO.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\domain\vo\NoticeEmployeeVO.java)

#### 查询查看记录
查询通知公告的阅读记录。

**HTTP方法**: POST  
**URL路径**: `/oa/notice/employee/queryViewRecord`

**请求体结构**:
```json
{
  "noticeId": 1,
  "current": 1,
  "pageSize": 10
}
```

**请求参数说明**:
- `noticeId`: 通知ID，必填
- `current`: 当前页码，必填
- `pageSize`: 每页大小，必填

**响应格式**:
```json
{
  "code": 0,
  "ok": true,
  "data": {
    "list": [
      {
        "noticeId": 1,
        "employeeId": 1,
        "employeeName": "员工姓名",
        "departmentName": "部门名称",
        "viewTime": "2023-01-01T00:00:00",
        "ipAddress": "192.168.1.1",
        "userAgent": "Mozilla/5.0"
      }
    ],
    "total": 1,
    "current": 1,
    "size": 10,
    "pages": 1,
    "emptyFlag": false,
    "firstFlag": true,
    "lastFlag": false
  },
  "msg": null
}
```

**错误码**:
- `400`: 参数验证失败
- `500`: 服务器内部错误

**Section sources**
- [NoticeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\controller\NoticeController.java#L134-L138)
- [NoticeEmployeeService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\service\NoticeEmployeeService.java)
- [NoticeViewRecordQueryForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\domain\form\NoticeViewRecordQueryForm.java)
- [NoticeViewRecordVO.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\domain\vo\NoticeViewRecordVO.java)

### 可见范围类型
定义了通知公告可见范围的类型。

| 值 | 描述 |
|----|------|
| 1 | 员工 |
| 2 | 部门 |

**Section sources**
- [NoticeVisibleRangeDataTypeEnum.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\constant\NoticeVisibleRangeDataTypeEnum.java)

## 前端调用示例

### 分类管理调用示例

#### 添加分类
```javascript
import { categoryApi } from '/@/api/business/category/category-api.js';

// 添加商品分类
const addCategoryExample = async () => {
  const param = {
    categoryName: '电子产品',
    categoryType: 1, // 商品分类
    parentId: 0,
    sort: 1,
    remark: '电子产品分类',
    disabledFlag: false
  };
  
  try {
    const result = await categoryApi.addCategory(param);
    console.log('添加成功:', result.data);
  } catch (error) {
    console.error('添加失败:', error);
  }
};
```

#### 查询分类树
```javascript
// 查询商品分类树
const queryCategoryTreeExample = async () => {
  const param = {
    categoryType: 1 // 只查询商品分类
  };
  
  try {
    const result = await categoryApi.queryCategoryTree(param);
    console.log('分类树:', result.data);
    // 处理树形结构数据
    renderCategoryTree(result.data);
  } catch (error) {
    console.error('查询失败:', error);
  }
};

// 渲染分类树
const renderCategoryTree = (treeData) => {
  // 使用递归渲染树形结构
  treeData.forEach(node => {
    console.log(`${node.categoryFullName} (ID: ${node.categoryId})`);
    if (node.children && node.children.length > 0) {
      renderCategoryTree(node.children);
    }
  });
};
```

#### 更新分类
```javascript
// 更新分类信息
const updateCategoryExample = async () => {
  const param = {
    categoryId: 1,
    categoryName: '电子产品',
    categoryType: 1,
    parentId: 0,
    sort: 1,
    remark: '更新后的备注',
    disabledFlag: false
  };
  
  try {
    const result = await categoryApi.updateCategory(param);
    console.log('更新成功:', result.data);
  } catch (error) {
    console.error('更新失败:', error);
  }
};
```

### 商品管理调用示例

#### 分页查询商品
```javascript
import { goodsApi } from '/@/api/business/goods/goods-api.js';

// 分页查询商品
const queryGoodsExample = async () => {
  const param = {
    categoryId: 1,
    searchWord: '手机',
    goodsStatus: 2, // 售卖中
    shelvesFlag: true, // 已上架
    current: 1,
    pageSize: 10
  };
  
  try {
    const result = await goodsApi.queryGoodsList(param);
    console.log('商品列表:', result.data.list);
    console.log('总记录数:', result.data.total);
    // 更新分页组件
    updatePagination(result.data);
    // 渲染商品列表
    renderGoodsList(result.data.list);
  } catch (error) {
    console.error('查询失败:', error);
  }
};

// 条件筛选查询
const filterGoodsExample = async () => {
  const param = {
    searchWord: '华为', // 搜索包含"华为"的商品
    shelvesFlag: true, // 只查询已上架商品
    current: 1,
    pageSize: 20
  };
  
  try {
    const result = await goodsApi.queryGoodsList(param);
    displayGoodsResult(result.data);
  } catch (error) {
    handleError(error);
  }
};
```

#### 批量删除商品
```javascript
// 批量删除商品
const batchDeleteGoodsExample = async () => {
  const goodsIdList = [1, 2, 3]; // 要删除的商品ID列表
  
  if (goodsIdList.length === 0) {
    console.log('请选择要删除的商品');
    return;
  }
  
  // 确认删除
  const confirmed = window.confirm(`确定要删除选中的${goodsIdList.length}个商品吗？`);
  if (!confirmed) return;
  
  try {
    const result = await goodsApi.batchDelete(goodsIdList);
    console.log('批量删除成功:', result.data);
    // 刷新商品列表
    refreshGoodsList();
  } catch (error) {
    console.error('批量删除失败:', error);
    alert('删除失败，请重试');
  }
};
```

#### 文件上传导入商品
```javascript
// 导入商品Excel文件
const importGoodsExample = async (file) => {
  // 验证文件类型
  if (!file || !file.type.includes('excel') && !file.type.includes('spreadsheetml')) {
    alert('请上传Excel文件');
    return;
  }
  
  // 验证文件大小（假设限制10MB）
  if (file.size > 10 * 1024 * 1024) {
    alert('文件大小不能超过10MB');
    return;
  }
  
  // 显示加载状态
  showLoading('正在导入商品...');
  
  try {
    const formData = new FormData();
    formData.append('file', file);
    
    const result = await goodsApi.importGoods(formData);
    hideLoading();
    alert(`导入成功：${result.data}`);
    // 刷新商品列表
    refreshGoodsList();
  } catch (error) {
    hideLoading();
    console.error('导入失败:', error);
    alert(`导入失败：${error.message}`);
  }
};

// 文件选择事件处理
const handleFileSelect = (event) => {
  const file = event.target.files[0];
  if (file) {
    importGoodsExample(file);
  }
};
```

#### 导出商品数据
```javascript
// 导出商品数据
const exportGoodsExample = async () => {
  try {
    // 显示导出提示
    showLoading('正在生成导出文件...');
    
    // 调用导出API
    await goodsApi.exportGoods();
    
    hideLoading();
    console.log('导出完成');
  } catch (error) {
    hideLoading();
    console.error('导出失败:', error);
    alert('导出失败，请重试');
  }
};

// 带条件的导出
const exportFilteredGoods = async (filterParams) => {
  try {
    showLoading('正在生成导出文件...');
    
    // 先查询符合条件的数据
    const queryResult = await goodsApi.queryGoodsList(filterParams);
    
    if (queryResult.data.total === 0) {
      alert('没有符合条件的数据可导出');
      hideLoading();
      return;
    }
    
    // 确认导出
    const confirmed = window.confirm(`即将导出${queryResult.data.total}条商品数据，是否继续？`);
    if (!confirmed) {
      hideLoading();
      return;
    }
    
    // 执行导出
    await goodsApi.exportGoods();
    
    hideLoading();
    console.log('导出完成');
  } catch (error) {
    hideLoading();
    console.error('导出失败:', error);
    alert('导出失败，请重试');
  }
};
```

### 通知公告管理调用示例

#### 发布通知公告
```javascript
import { noticeApi } from '/@/api/business/oa/notice-api.js';

// 发布通知公告
const addNoticeExample = async () => {
  // 先获取通知类型
  const typeResult = await noticeApi.getAllNoticeTypeList();
  const noticeTypes = typeResult.data;
  
  const param = {
    noticeTypeId: noticeTypes[0].noticeTypeId, // 使用第一个通知类型
    title: '系统维护通知',
    author: '系统管理员',
    source: 'IT部门',
    documentNumber: 'IT-2023-001',
    publishTime: new Date().toISOString(),
    contentHtml: '<h2>系统维护通知</h2><p>将于今晚22:00-24:00进行系统维护，请提前保存工作。</p>',
    contentText: '系统维护通知\n将于今晚22:00-24:00进行系统维护，请提前保存工作。',
    attachment: null, // 无附件
    topFlag: true, // 置顶
    visibleRangeList: [
      {
        dataType: 2, // 部门
        dataId: 1 // IT部门
      }
    ]
  };
  
  try {
    const result = await noticeApi.addNotice(param);
    console.log('发布成功:', result.data);
    alert('通知发布成功');
    // 跳转到通知列表页面
    redirectToNoticeList();
  } catch (error) {
    console.error('发布失败:', error);
    alert(`发布失败：${error.message}`);
  }
};
```

#### 查询员工通知
```javascript
// 查询员工可查看的通知
const queryEmployeeNoticeExample = async () => {
  const param = {
    hasViewed: false, // 只查询未读通知
    current: 1,
    pageSize: 5
  };
  
  try {
    const result = await noticeApi.queryEmployeeNotice(param);
    console.log('未读通知:', result.data.list);
    
    // 显示未读通知数量
    updateUnreadCount(result.data.list.length);
    
    // 渲染通知列表
    renderNoticeList(result.data.list);
    
    // 如果有未读通知，播放提示音
    if (result.data.list.length > 0) {
      playNotificationSound();
    }
  } catch (error) {
    console.error('查询失败:', error);
  }
};

// 查看通知详情
const viewNoticeExample = async (noticeId) => {
  try {
    const result = await noticeApi.view(noticeId);
    console.log('通知详情:', result.data);
    
    // 显示通知详情弹窗
    showNoticeDetailModal(result.data);
    
    // 更新阅读状态显示
    markNoticeAsRead(noticeId);
  } catch (error) {
    console.error('查看失败:', error);
    alert('无法查看通知详情');
  }
};
```

#### 查询阅读记录
```javascript
// 查询通知阅读记录
const queryViewRecordExample = async (noticeId) => {
  const param = {
    noticeId: noticeId,
    current: 1,
    pageSize: 10
  };
  
  try {
    const result = await noticeApi.queryViewRecord(param);
    console.log('阅读记录:', result.data.list);
    
    // 渲染阅读记录表格
    renderViewRecordTable(result.data.list);
    
    // 显示统计信息
    displayViewStatistics(result.data.list);
  } catch (error) {
    console.error('查询失败:', error);
    alert('无法获取阅读记录');
  }
};

// 显示阅读统计
const displayViewStatistics = (viewRecords) => {
  const totalViews = viewRecords.length;
  const uniqueEmployees = new Set(viewRecords.map(r => r.employeeId)).size;
  
  console.log(`总阅读次数: ${totalViews}`);
  console.log(`独立阅读人数: ${uniqueEmployees}`);
  
  // 更新统计面板
  updateStatisticsPanel({
    totalViews,
    uniqueEmployees,
    readRate: (uniqueEmployees / totalTargetCount) * 100
  });
};
```

## 业务规则与数据校验

### 分类管理业务规则

1. **分类层级限制**:
   - 支持多级树形结构
   - 最大层级深度为5级
   - 根节点的父级ID为0或null

2. **分类类型约束**:
   - `GOODS`(1): 商品分类，用于商品管理
   - `CUSTOM`(2): 自定义分类，可用于其他业务场景

3. **唯一性约束**:
   - 同一父级下的分类名称必须唯一
   - 分类名称在同一层级内不能重复

4. **删除约束**:
   - 包含子分类的分类不能直接删除，必须先删除所有子分类
   - 与商品关联的分类不能删除，必须先解除关联

5. **排序规则**:
   - 排序值越大，显示顺序越靠前
   - 默认排序值为0
   - 支持手动调整排序

**Section sources**
- [CategoryService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\category\service\CategoryService.java)
- [CategoryDao.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\category\dao\CategoryDao.java)

### 商品管理业务规则

1. **商品状态流转**:
   ```
   预约中 → 售卖中 → 售罄
   ```
   - 状态只能按顺序升级，不能降级
   - 售罄状态的商品不能重新上架，需要创建新商品

2. **上架/下架规则**:
   - 只有"售卖中"状态的商品才能上架
   - 下架的商品仍然保留在系统中，可以重新上架
   - 上架商品必须有库存

3. **分类关联规则**:
   - 每个商品必须关联一个分类
   - 分类必须是商品分类类型(CategoryType.GOODS)
   - 修改商品分类时，需要验证新分类的有效性

4. **价格规则**:
   - 价格必须大于0
   - 支持小数点后两位
   - 价格修改需要记录变更历史

5. **导入/导出规则**:
   - 导入文件必须是Excel格式(.xlsx)
   - 文件大小限制为10MB
   - 每次导入最多处理1000条记录
   - 导出文件包含所有查询条件匹配的数据

**Section sources**
- [GoodsService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\goods\service\GoodsService.java)
- [GoodsDao.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\goods\dao\GoodsDao.java)

### 通知公告管理业务规则

1. **可见范围规则**:
   - 支持按员工或部门设置可见范围
   - 如果不设置可见范围，则所有员工可见
   - 部门可见范围包含该部门的所有员工

2. **发布与阅读规则**:
   - 发布时间可以设置为未来时间，实现定时发布
   - 阅读状态自动记录，包含IP地址和User-Agent信息
   - 同一员工对同一通知只能记录一次阅读

3. **置顶规则**:
   - 置顶通知在列表中显示在最前面
   - 最多同时显示5个置顶通知
   - 置顶通知有特殊标识

4. **附件管理规则**:
   - 附件通过文件服务上传，返回文件key
   - 支持常见文档格式(PDF, DOC, XLS等)
   - 附件大小限制为50MB

5. **防重复提交规则**:
   - 添加和更新操作有防重复提交机制
   - 相同内容在短时间内重复提交会被拒绝
   - 防重复提交有效期为5分钟

**Section sources**
- [NoticeService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\service\NoticeService.java)
- [NoticeEmployeeService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\service\NoticeEmployeeService.java)

### 数据校验规则

#### 分类数据校验
| 字段 | 校验规则 | 错误提示 |
|------|---------|---------|
| categoryName | 必填，长度1-20字符 | "类目名称不能为空"，"类目名称最多20字符" |
| categoryType | 必填，必须是有效类型 | "分类错误" |
| sort | 可选，整数 | 无 |
| remark | 可选，长度0-200字符 | "备注最多200字符" |
| disabledFlag | 必填，布尔值 | "禁用状态不能为空" |

#### 商品数据校验
| 字段 | 校验规则 | 错误提示 |
|------|---------|---------|
| categoryId | 必填，有效分类ID | "商品分类不能为空" |
| goodsName | 必填 | "商品名称不能为空" |
| goodsStatus | 必填，有效状态 | "商品状态错误" |
| price | 可选，大于0 | 无 |
| shelvesFlag | 必填，布尔值 | "上架状态不能为空" |
| remark | 可选，长度0-200字符 | "备注最多200字符" |

#### 通知公告数据校验
| 字段 | 校验规则 | 错误提示 |
|------|---------|---------|
| noticeTypeId | 必填，有效类型ID | "通知类型不能为空" |
| title | 必填，长度1-100字符 | "标题不能为空"，"标题长度超出限制" |
| contentHtml | 必填 | "内容不能为空" |
| contentText | 必填 | "纯文本内容不能为空" |
| publishTime | 可选，有效日期时间 | "发布时间格式错误" |
| visibleRangeList | 可选，有效范围 | "可见范围数据无效" |

**Section sources**
- [CategoryAddForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\category\domain\form\CategoryAddForm.java)
- [GoodsAddForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\goods\domain\form\GoodsAddForm.java)
- [NoticeAddForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\domain\form\NoticeAddForm.java)