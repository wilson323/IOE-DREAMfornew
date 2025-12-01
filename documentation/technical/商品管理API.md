# 商品管理API

<cite>
**本文档引用的文件**   
- [GoodsController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/controller/GoodsController.java)
- [GoodsService.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/service/GoodsService.java)
- [GoodsStatusEnum.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/constant/GoodsStatusEnum.java)
- [GoodsQueryForm.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/domain/form/GoodsQueryForm.java)
- [GoodsAddForm.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/domain/form/GoodsAddForm.java)
- [GoodsUpdateForm.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/domain/form/GoodsUpdateForm.java)
- [GoodsVO.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/domain/vo/GoodsVO.java)
- [GoodsExcelVO.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/domain/vo/GoodsExcelVO.java)
- [goods-api.js](file://smart-admin-web-javascript/src/api/business/goods/goods-api.js)
- [goods-list.vue](file://smart-admin-web-javascript/src/views/business/erp/goods/goods-list.vue)
</cite>

## 目录
1. [商品管理API](#商品管理api)
2. [核心接口说明](#核心接口说明)
3. [商品状态管理](#商品状态管理)
4. [批量删除实现](#批量删除实现)
5. [导入导出功能](#导入导出功能)
6. [数据结构定义](#数据结构定义)

## 核心接口说明

商品管理模块提供了完整的商品增删改查功能，所有接口均通过`GoodsController`类实现，位于`net.lab1024.sa.admin.module.business.goods.controller`包中。接口采用RESTful风格设计，使用Spring Boot框架构建。

### 分页查询接口 (/goods/query)

分页查询接口用于获取商品列表，支持多种查询条件过滤。

**请求信息**
- **请求方法**: POST
- **请求路径**: `/goods/query`
- **权限控制**: `goods:query`
- **请求参数**: `GoodsQueryForm`对象

**请求参数说明**
```json
{
  "categoryId": 1,
  "searchWord": "商品名称",
  "goodsStatus": 2,
  "place": "产地",
  "shelvesFlag": true,
  "pageNum": 1,
  "pageSize": 10
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| categoryId | Integer | 否 | 商品分类ID |
| searchWord | String | 否 | 搜索关键词，最多30字符 |
| goodsStatus | Integer | 否 | 商品状态，参考GoodsStatusEnum |
| place | String | 否 | 产地，使用字典数据 |
| shelvesFlag | Boolean | 否 | 上架状态 |
| pageNum | Long | 是 | 当前页码 |
| pageSize | Long | 是 | 每页数量 |

**响应格式**
```json
{
  "code": 0,
  "ok": true,
  "msg": "操作成功",
  "data": {
    "pageNum": 1,
    "pageSize": 10,
    "total": 100,
    "pages": 10,
    "list": [...],
    "emptyFlag": false
  }
}
```

**Section sources**
- [GoodsController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/controller/GoodsController.java#L42-L47)
- [GoodsService.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/service/GoodsService.java#L147-L165)
- [GoodsQueryForm.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/domain/form/GoodsQueryForm.java#L22-L45)

### 添加商品接口 (/goods/add)

添加商品接口用于创建新的商品记录。

**请求信息**
- **请求方法**: POST
- **请求路径**: `/goods/add`
- **权限控制**: `goods:add`
- **请求参数**: `GoodsAddForm`对象

**请求参数说明**
```json
{
  "categoryId": 1,
  "goodsName": "商品名称",
  "goodsStatus": 2,
  "place": "产地",
  "price": 99.99,
  "shelvesFlag": true,
  "remark": "备注信息"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| categoryId | Long | 是 | 商品分类ID，不能为空 |
| goodsName | String | 是 | 商品名称，不能为空 |
| goodsStatus | Integer | 是 | 商品状态，必须为有效值 |
| place | String | 是 | 产地，不能为空 |
| price | BigDecimal | 是 | 商品价格，不能低于0 |
| shelvesFlag | Boolean | 是 | 上架状态，不能为空 |
| remark | String | 否 | 备注信息 |

**响应格式**
```json
{
  "code": 0,
  "ok": true,
  "msg": "操作成功",
  "data": null
}
```

**Section sources**
- [GoodsController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/controller/GoodsController.java#L49-L54)
- [GoodsService.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/service/GoodsService.java#L68-L79)
- [GoodsAddForm.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/domain/form/GoodsAddForm.java#L26-L56)

### 更新商品接口 (/goods/update)

更新商品接口用于修改现有商品信息。

**请求信息**
- **请求方法**: POST
- **请求路径**: `/goods/update`
- **权限控制**: `goods:update`
- **请求参数**: `GoodsUpdateForm`对象

**请求参数说明**
```json
{
  "goodsId": 1,
  "categoryId": 1,
  "goodsName": "商品名称",
  "goodsStatus": 2,
  "place": "产地",
  "price": 99.99,
  "shelvesFlag": true,
  "remark": "备注信息"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| goodsId | Long | 是 | 商品ID，不能为空 |
| 其他参数 | - | - | 同添加商品接口 |

**响应格式**
```json
{
  "code": 0,
  "ok": true,
  "msg": "操作成功",
  "data": null
}
```

**Section sources**
- [GoodsController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/controller/GoodsController.java#L56-L61)
- [GoodsService.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/service/GoodsService.java#L85-L96)
- [GoodsUpdateForm.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/domain/form/GoodsUpdateForm.java#L17-L22)

### 删除商品接口 (/goods/delete/{goodsId})

删除商品接口用于删除指定ID的商品。

**请求信息**
- **请求方法**: GET
- **请求路径**: `/goods/delete/{goodsId}`
- **权限控制**: `goods:delete`
- **路径参数**: goodsId

**请求参数说明**
- **goodsId**: 商品ID，路径参数

**业务规则**
1. 只有状态为"售罄"的商品才能被删除
2. 删除操作会同时更新数据追踪记录

**响应格式**
```json
{
  "code": 0,
  "ok": true,
  "msg": "操作成功",
  "data": null
}
```

**Section sources**
- [GoodsController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/controller/GoodsController.java#L63-L68)
- [GoodsService.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/service/GoodsService.java#L116-L129)

## 商品状态管理

商品状态管理通过`GoodsStatusEnum`枚举类实现，定义了商品的三种状态。

### 商品状态枚举 (GoodsStatusEnum)

```java
public enum GoodsStatusEnum implements BaseEnum {
    /**
     * 1 预约中
     */
    APPOINTMENT(1, "预约中"),

    /**
     * 2 售卖
     */
    SELL(2, "售卖中"),

    /**
     * 3 售罄
     */
    SELL_OUT(3, "售罄");
}
```

| 状态值 | 状态码 | 说明 |
|-------|--------|------|
| 1 | APPOINTMENT | 预约中，商品正在接受预约 |
| 2 | SELL | 售卖中，商品正在销售 |
| 3 | SELL_OUT | 售罄，商品已售完 |

**使用说明**
- 在添加和更新商品时，`goodsStatus`字段必须为上述三个值之一
- 删除商品时，系统会检查商品状态，只有状态为"售罄"（值为3）的商品才能被删除
- 前端通过`SmartEnumUtil.getEnumDescByValue()`方法将状态值转换为对应的描述文本

**状态转换规则**
1. 新建商品时，可设置为任意状态
2. 商品销售过程中，状态可从"预约中"或"售卖中"变为"售罄"
3. "售罄"状态的商品可被删除，但不能重新上架

**Section sources**
- [GoodsStatusEnum.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/constant/GoodsStatusEnum.java#L19-L34)
- [GoodsService.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/service/GoodsService.java#L122-L124)
- [goods-list.vue](file://smart-admin-web-javascript/src/views/business/erp/goods/goods-list.vue#L127)

## 批量删除实现

批量删除功能允许用户一次性删除多个商品，提高了操作效率。

### 批量删除接口 (/goods/batchDelete)

**请求信息**
- **请求方法**: POST
- **请求路径**: `/goods/batchDelete`
- **权限控制**: `goods:batchDelete`
- **请求参数**: Long类型数组

**请求参数示例**
```json
[1, 2, 3, 4, 5]
```

**实现逻辑**
1. 接收商品ID列表作为请求体
2. 验证ID列表是否为空
3. 调用DAO层的批量更新方法，将指定商品的`deletedFlag`标记为true
4. 记录数据追踪信息

**业务规则**
- 批量删除前不会检查每个商品的状态
- 实际删除操作是通过更新`deletedFlag`字段实现的软删除
- 支持删除多个商品，包括不同状态的商品

**响应格式**
```json
{
  "code": 0,
  "ok": true,
  "msg": "操作成功",
  "data": null
}
```

**前端实现**
在前端`goods-list.vue`文件中，通过选择表格行来获取要删除的商品ID列表，然后调用批量删除接口。

```javascript
async function batchDelete() {
  try {
    SmartLoading.show();
    await goodsApi.batchDelete(selectedRowKeyList.value);
    message.success('删除成功');
    queryData();
  } catch (e) {
    smartSentry.captureError(e);
  } finally {
    SmartLoading.hide();
  }
}
```

**Section sources**
- [GoodsController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/controller/GoodsController.java#L70-L75)
- [GoodsService.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/service/GoodsService.java#L134-L141)
- [goods-api.js](file://smart-admin-web-javascript/src/api/business/goods/goods-api.js#L20-L22)
- [goods-list.vue](file://smart-admin-web-javascript/src/views/business/erp/goods/goods-list.vue#L388-L444)

## 导入导出功能

商品管理模块提供了Excel文件的导入和导出功能，方便用户进行批量数据操作。

### 导入商品接口 (/goods/importGoods)

**请求信息**
- **请求方法**: POST
- **请求路径**: `/goods/importGoods`
- **权限控制**: `goods:importGoods`
- **请求参数**: MultipartFile文件

**文件格式要求**
- 支持.xls和.xlsx格式
- 文件必须包含以下列：
  - 商品分类
  - 商品名称
  - 商品状态
  - 产地
  - 商品价格
  - 备注

**处理流程**
1. 接收上传的Excel文件
2. 使用FastExcel库读取文件内容
3. 将Excel数据映射到`GoodsImportForm`对象列表
4. 验证数据完整性
5. 返回导入结果

**响应格式**
```json
{
  "code": 0,
  "ok": true,
  "msg": "成功导入5条，具体数据为：[...]",
  "data": null
}
```

**前端实现**
```javascript
importGoods : (file) =>{
  return postRequest('/goods/importGoods',file);
}
```

**Section sources**
- [GoodsController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/controller/GoodsController.java#L79-L84)
- [GoodsService.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/service/GoodsService.java#L173-L189)

### 导出商品接口 (/goods/exportGoods)

**请求信息**
- **请求方法**: GET
- **请求路径**: `/goods/exportGoods`
- **权限控制**: `goods:exportGoods`
- **响应类型**: 文件下载

**处理流程**
1. 调用`getAllGoods()`方法获取所有商品数据
2. 将数据转换为`GoodsExcelVO`对象列表
3. 使用`SmartExcelUtil`工具类生成Excel文件
4. 通过HTTP响应直接下载文件

**文件内容**
导出的Excel文件包含以下列：
- 商品分类
- 商品名称
- 商品状态
- 产地
- 商品价格
- 备注

**前端实现**
```javascript
exportGoods : () =>{
  return getDownload('/goods/exportGoods');
}
```

**Section sources**
- [GoodsController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/controller/GoodsController.java#L86-L92)
- [GoodsService.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/service/GoodsService.java#L194-L209)
- [GoodsExcelVO.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/domain/vo/GoodsExcelVO.java#L21-L44)

## 数据结构定义

### 分页查询结果 (PageResult<GoodsVO>)

```java
@Data
public class PageResult<T> {
    private Long pageNum;
    private Long pageSize;
    private Long total;
    private Long pages;
    private List<T> list;
    private Boolean emptyFlag;
}
```

**字段说明**
| 字段名 | 类型 | 说明 |
|-------|------|------|
| pageNum | Long | 当前页码 |
| pageSize | Long | 每页数量 |
| total | Long | 总记录数 |
| pages | Long | 总页数 |
| list | List<T> | 结果列表 |
| emptyFlag | Boolean | 是否为空 |

### 商品视图对象 (GoodsVO)

```java
@Data
public class GoodsVO {
    private Long categoryId;
    private String goodsName;
    private Integer goodsStatus;
    private String place;
    private BigDecimal price;
    private Boolean shelvesFlag;
    private String remark;
    private Long goodsId;
    private String categoryName;
    private LocalDateTime updateTime;
    private LocalDateTime createTime;
}
```

**字段说明**
| 字段名 | 类型 | 说明 |
|-------|------|------|
| categoryId | Long | 商品分类ID |
| goodsName | String | 商品名称 |
| goodsStatus | Integer | 商品状态 |
| place | String | 产地 |
| price | BigDecimal | 商品价格 |
| shelvesFlag | Boolean | 上架状态 |
| remark | String | 备注 |
| goodsId | Long | 商品ID |
| categoryName | String | 商品分类名称 |
| updateTime | LocalDateTime | 更新时间 |
| createTime | LocalDateTime | 创建时间 |

### Excel商品视图对象 (GoodsExcelVO)

```java
@Data
@Builder
public class GoodsExcelVO {
    private String categoryName;
    private String goodsName;
    private String goodsStatus;
    private String place;
    private BigDecimal price;
    private String remark;
}
```

**字段说明**
| 字段名 | 类型 | 说明 |
|-------|------|------|
| categoryName | String | 商品分类名称 |
| goodsName | String | 商品名称 |
| goodsStatus | String | 商品状态（文本描述） |
| place | String | 产地 |
| price | BigDecimal | 商品价格 |
| remark | String | 备注 |

**Section sources**
- [PageResult.java](file://smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/common/domain/PageResult.java#L1-L53)
- [GoodsVO.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/domain/vo/GoodsVO.java#L21-L53)
- [GoodsExcelVO.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/domain/vo/GoodsExcelVO.java#L21-L44)