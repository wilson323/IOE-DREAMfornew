# 通知公告API

<cite>
**本文档引用文件**  
- [NoticeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\controller\NoticeController.java)
- [NoticeService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\service\NoticeService.java)
- [NoticeEmployeeService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\service\NoticeEmployeeService.java)
- [NoticeTypeService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\service\NoticeTypeService.java)
- [NoticeDetailVO.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\domain\vo\NoticeDetailVO.java)
- [NoticeVO.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\domain\vo\NoticeVO.java)
- [NoticeEmployeeVO.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\domain\vo\NoticeEmployeeVO.java)
- [NoticeQueryForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\domain\form\NoticeQueryForm.java)
- [NoticeAddForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\domain\form\NoticeAddForm.java)
- [NoticeEmployeeQueryForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\domain\form\NoticeEmployeeQueryForm.java)
- [NoticeVisibleRangeDataTypeEnum.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\constant\NoticeVisibleRangeDataTypeEnum.java)
- [RepeatSubmit.java](file://smart-admin-api-java17-springboot3\sa-base\src\main\java\net\lab1024\sa\base\module\support\repeatsubmit\annoation\RepeatSubmit.java)
- [RepeatSubmitAspect.java](file://smart-admin-api-java17-springboot3\sa-base\src\main\java\net\lab1024\sa\base\module\support\repeatsubmit\RepeatSubmitAspect.java)
</cite>

## 目录
1. [通知公告管理接口](#通知公告管理接口)
2. [通知类型管理接口](#通知类型管理接口)
3. [员工查看通知接口](#员工查看通知接口)
4. [数据结构定义](#数据结构定义)
5. [重复提交校验机制](#重复提交校验机制)

## 通知公告管理接口

本节描述管理员对通知公告的增删改查操作接口，以`NoticeController`为核心，提供完整的管理功能。

### 分页查询接口 (/oa/notice/query)

**功能说明**  
管理员分页查询通知公告列表，支持按分类、关键字、文号、创建人等条件筛选。

**请求方式**  
POST

**请求路径**  
/oa/notice/query

**权限控制**  
`oa:notice:query`

**请求参数**  
请求体为`NoticeQueryForm`对象，包含以下字段：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| noticeTypeId | Long | 否 | 通知分类ID |
| keywords | String | 否 | 标题、作者、来源关键字 |
| documentNumber | String | 否 | 文号 |
| createUserName | String | 否 | 创建人姓名 |
| deletedFlag | Boolean | 否 | 删除标识 |
| createTimeBegin | LocalDate | 否 | 创建时间起始 |
| createTimeEnd | LocalDate | 否 | 创建时间截止 |
| publishTimeBegin | LocalDate | 否 | 发布时间起始 |
| publishTimeEnd | LocalDate | 否 | 发布时间截止 |
| current | Integer | 是 | 当前页码 |
| pageSize | Integer | 是 | 每页数量 |

**响应格式**  
```json
{
  "ok": true,
  "data": {
    "list": [
      {
        "noticeId": 1,
        "title": "系统升级通知",
        "noticeTypeId": 1,
        "noticeTypeName": "公告",
        "allVisibleFlag": true,
        "scheduledPublishFlag": false,
        "publishFlag": true,
        "publishTime": "2024-01-01T10:00:00",
        "author": "管理员",
        "source": "IT部门",
        "documentNumber": "2024-001",
        "pageViewCount": 100,
        "userViewCount": 50,
        "deletedFlag": false,
        "createUserName": "admin",
        "createTime": "2024-01-01T09:00:00",
        "updateTime": "2024-01-01T09:30:00"
      }
    ],
    "total": 1,
    "current": 1,
    "size": 10,
    "pages": 1
  },
  "code": 200,
  "message": "OK"
}
```

**Section sources**
- [NoticeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\controller\NoticeController.java#L77-L82)
- [NoticeService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\service\NoticeService.java#L78-L83)
- [NoticeQueryForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\domain\form\NoticeQueryForm.java)

### 添加通知接口 (/oa/notice/add)

**功能说明**  
管理员添加新的通知公告。

**请求方式**  
POST

**请求路径**  
/oa/notice/add

**权限控制**  
`oa:notice:add`

**重复提交校验**  
`@RepeatSubmit`注解防止重复提交

**请求参数**  
请求体为`NoticeAddForm`对象，包含以下字段：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| title | String | 是 | 标题（最多200字符） |
| noticeTypeId | Long | 是 | 通知分类ID |
| allVisibleFlag | Boolean | 是 | 是否全部可见 |
| scheduledPublishFlag | Boolean | 是 | 是否定时发布 |
| publishTime | LocalDateTime | 是 | 发布时间 |
| contentText | String | 是 | 纯文本内容 |
| contentHtml | String | 是 | HTML内容 |
| attachment | String | 否 | 附件（多个英文逗号分隔） |
| author | String | 是 | 作者 |
| source | String | 是 | 来源 |
| documentNumber | String | 否 | 文号 |
| visibleRangeList | List<NoticeVisibleRangeForm> | 否 | 可见范围设置 |

**响应格式**  
```json
{
  "ok": true,
  "data": null,
  "code": 200,
  "message": "OK"
}
```

**Section sources**
- [NoticeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\controller\NoticeController.java#L84-L91)
- [NoticeService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\service\NoticeService.java#L89-L104)
- [NoticeAddForm.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\domain\form\NoticeAddForm.java)

### 更新通知接口 (/oa/notice/update)

**功能说明**  
管理员更新已有的通知公告。

**请求方式**  
POST

**请求路径**  
/oa/notice/update

**权限控制**  
`oa:notice:update`

**重复提交校验**  
`@RepeatSubmit`注解防止重复提交

**请求参数**  
请求体为`NoticeUpdateForm`对象，包含与添加接口相同的字段，外加`noticeId`字段。

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| noticeId | Long | 是 | 通知ID |

**响应格式**  
```json
{
  "ok": true,
  "data": null,
  "code": 200,
  "message": "OK"
}
```

**Section sources**
- [NoticeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\controller\NoticeController.java#L93-L98)
- [NoticeService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\service\NoticeService.java#L166-L182)

### 删除通知接口 (/oa/notice/delete/{noticeId})

**功能说明**  
管理员删除指定的通知公告。

**请求方式**  
GET

**请求路径**  
/oa/notice/delete/{noticeId}

**权限控制**  
`oa:notice:delete`

**路径参数**  
- noticeId: Long - 通知ID

**响应格式**  
```json
{
  "ok": true,
  "data": null,
  "code": 200,
  "message": "OK"
}
```

**Section sources**
- [NoticeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\controller\NoticeController.java#L108-L113)
- [NoticeService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\service\NoticeService.java#L190-L198)

## 通知类型管理接口

本节描述通知公告类型的管理接口，包括获取全部类型、添加类型等操作。

### 获取全部类型接口 (/oa/noticeType/getAll)

**功能说明**  
获取所有通知公告类型。

**请求方式**  
GET

**请求路径**  
/oa/noticeType/getAll

**请求参数**  
无

**响应格式**  
```json
{
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
  "code": 200,
  "message": "OK"
}
```

**Section sources**
- [NoticeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\controller\NoticeController.java#L50-L53)
- [NoticeTypeService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\service\NoticeTypeService.java#L49-L55)

### 添加类型接口 (/oa/noticeType/add/{name})

**功能说明**  
添加新的通知公告类型。

**请求方式**  
GET

**请求路径**  
/oa/noticeType/add/{name}

**路径参数**  
- name: String - 类型名称

**响应格式**  
```json
{
  "ok": true,
  "data": null,
  "code": 200,
  "message": "OK"
}
```

**校验规则**  
- 类型名称不能为空
- 类型名称不能重复

**Section sources**
- [NoticeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\controller\NoticeController.java#L56-L59)
- [NoticeTypeService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\service\NoticeTypeService.java#L57-L68)

### 更新类型接口 (/oa/noticeType/update/{noticeTypeId}/{name})

**功能说明**  
更新通知公告类型名称。

**请求方式**  
GET

**请求路径**  
/oa/noticeType/update/{noticeTypeId}/{name}

**路径参数**  
- noticeTypeId: Long - 类型ID
- name: String - 新的类型名称

**响应格式**  
```json
{
  "ok": true,
  "data": null,
  "code": 200,
  "message": "OK"
}
```

**校验规则**  
- 类型ID必须存在
- 新名称不能为空
- 新名称不能与其他类型重复

**Section sources**
- [NoticeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\controller\NoticeController.java#L62-L65)
- [NoticeTypeService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\service\NoticeTypeService.java#L70-L86)

### 删除类型接口 (/oa/noticeType/delete/{noticeTypeId})

**功能说明**  
删除指定的通知公告类型。

**请求方式**  
GET

**请求路径**  
/oa/noticeType/delete/{noticeTypeId}

**路径参数**  
- noticeTypeId: Long - 类型ID

**响应格式**  
```json
{
  "ok": true,
  "data": null,
  "code": 200,
  "message": "OK"
}
```

**Section sources**
- [NoticeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\controller\NoticeController.java#L68-L71)
- [NoticeTypeService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\service\NoticeTypeService.java#L87-L91)

## 员工查看通知接口

本节描述员工查看通知公告的接口，包括查看详情和查询全部通知。

### 查看详情接口 (/oa/notice/employee/view/{noticeId})

**功能说明**  
员工查看指定通知公告的详细信息，同时记录浏览行为。

**请求方式**  
GET

**请求路径**  
/oa/notice/employee/view/{noticeId}

**路径参数**  
- noticeId: Long - 通知ID

**请求头参数**  
- User-Agent: 用户代理信息

**响应格式**  
```json
{
  "ok": true,
  "data": {
    "noticeId": 1,
    "title": "系统升级通知",
    "noticeTypeId": 1,
    "noticeTypeName": "公告",
    "allVisibleFlag": true,
    "scheduledPublishFlag": false,
    "contentText": "系统将于今晚进行升级维护...",
    "contentHtml": "<p>系统将于今晚进行升级维护...</p>",
    "attachment": "upgrade_plan.pdf",
    "publishTime": "2024-01-01T10:00:00",
    "author": "管理员",
    "source": "IT部门",
    "documentNumber": "2024-001",
    "pageViewCount": 101,
    "userViewCount": 51,
    "createUserName": "admin",
    "createTime": "2024-01-01T09:00:00",
    "updateTime": "2024-01-01T09:30:00"
  },
  "code": 200,
  "message": "OK"
}
```

**权限校验**  
- 通知必须存在且未删除
- 如果不是全部可见，则需校验员工是否有查看权限（员工本人或所在部门）

**浏览记录**  
- 首次查看：页面浏览量+1，用户浏览量+1
- 非首次查看：页面浏览量+1，用户浏览量不变

**Section sources**
- [NoticeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\controller\NoticeController.java#L118-L126)
- [NoticeEmployeeService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\service\NoticeEmployeeService.java#L94-L120)

### 查询全部接口 (/oa/notice/employee/query)

**功能说明**  
员工分页查询可查看的通知公告列表。

**请求方式**  
POST

**请求路径**  
/oa/notice/employee/query

**请求参数**  
请求体为`NoticeEmployeeQueryForm`对象，包含以下字段：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keywords | String | 否 | 标题、作者、来源、文号关键字 |
| noticeTypeId | Long | 否 | 通知分类ID |
| publishTimeBegin | LocalDate | 否 | 发布时间起始 |
| publishTimeEnd | LocalDate | 否 | 发布时间截止 |
| notViewFlag | Boolean | 否 | 未读标识 |
| current | Integer | 是 | 当前页码 |
| pageSize | Integer | 是 | 每页数量 |

**响应格式**  
```json
{
  "ok": true,
  "data": {
    "list": [
      {
        "noticeId": 1,
        "title": "系统升级通知",
        "noticeTypeId": 1,
        "noticeTypeName": "公告",
        "allVisibleFlag": true,
        "scheduledPublishFlag": false,
        "publishFlag": true,
        "publishTime": "2024-01-01T10:00:00",
        "author": "管理员",
        "source": "IT部门",
        "documentNumber": "2024-001",
        "pageViewCount": 100,
        "userViewCount": 50,
        "deletedFlag": false,
        "createUserName": "admin",
        "createTime": "2024-01-01T09:00:00",
        "updateTime": "2024-01-01T09:30:00",
        "viewFlag": true,
        "publishDate": "2024-01-01"
      }
    ],
    "total": 1,
    "current": 1,
    "size": 10,
    "pages": 1
  },
  "code": 200,
  "message": "OK"
}
```

**权限范围**  
- 管理员：可查看所有通知
- 普通员工：可查看全部可见的通知，或自己所在部门及子部门的通知，或指定给自己的通知

**Section sources**
- [NoticeController.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\controller\NoticeController.java#L129-L132)
- [NoticeEmployeeService.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\service\NoticeEmployeeService.java#L52-L87)

## 数据结构定义

本节定义通知公告模块中使用的主要数据结构。

### 通知详情 (NoticeDetailVO)

**功能说明**  
通知公告详情数据结构，用于查看详情接口。

**字段定义**  

| 字段名 | 类型 | 说明 |
|--------|------|------|
| noticeId | Long | 通知ID |
| title | String | 标题 |
| noticeTypeId | Long | 通知分类ID |
| noticeTypeName | Long | 分类名称（注意：此处应为String类型，代码中可能存在错误） |
| allVisibleFlag | Boolean | 是否全部可见 |
| scheduledPublishFlag | Boolean | 是否定时发布 |
| contentText | String | 纯文本内容 |
| contentHtml | String | HTML内容 |
| attachment | String | 附件 |
| publishTime | LocalDateTime | 发布时间 |
| author | String | 作者 |
| source | String | 来源 |
| documentNumber | String | 文号 |
| pageViewCount | Integer | 页面浏览量 |
| userViewCount | Integer | 用户浏览量 |
| createUserName | Long | 创建人名称（注意：此处应为String类型，代码中可能存在错误） |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |

**Section sources**
- [NoticeDetailVO.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\domain\vo\NoticeDetailVO.java)

### 分页查询结果

**功能说明**  
分页查询结果的通用数据结构。

**字段定义**  

| 字段名 | 类型 | 说明 |
|--------|------|------|
| list | List<T> | 数据列表 |
| total | Long | 总记录数 |
| current | Integer | 当前页码 |
| size | Integer | 每页数量 |
| pages | Integer | 总页数 |

**Section sources**
- [NoticeVO.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\domain\vo\NoticeVO.java)
- [NoticeEmployeeVO.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\domain\vo\NoticeEmployeeVO.java)

### 可见范围类型 (NoticeVisibleRangeDataTypeEnum)

**功能说明**  
定义通知公告可见范围的类型。

**枚举值**  

| 值 | 说明 |
|----|------|
| 1 | 员工 |
| 2 | 部门 |

**Section sources**
- [NoticeVisibleRangeDataTypeEnum.java](file://smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\business\oa\notice\constant\NoticeVisibleRangeDataTypeEnum.java)

## 重复提交校验机制

本节说明通知公告模块中使用的重复提交校验机制。

### 实现原理

系统通过AOP切面`RepeatSubmitAspect`实现重复提交校验，核心组件包括：

1. **@RepeatSubmit注解**：标记需要防止重复提交的方法
2. **RepeatSubmitAspect切面**：拦截带有@RepeatSubmit注解的方法
3. **AbstractRepeatSubmitTicket抽象类**：定义凭证操作接口
4. **RepeatSubmitRedisTicket实现类**：基于Redis的凭证实现

### 注解参数

**intervalMilliSecond**  
- 默认值：0
- 说明：
  - intervalMilliSecond = 0：只有上个请求执行完以后才可以执行
  - intervalMilliSecond > 0：指定时间内只允许执行一次

### 凭证生成

凭证由`RepeatSubmitConfig`中的`ticket`方法生成，格式为：
```
请求路径 + "_" + 用户ID
```

例如：`/oa/notice/add_123`

### 执行流程

```mermaid
flowchart TD
A[开始] --> B{方法是否有@RepeatSubmit注解?}
B --> |是| C[生成凭证ticket]
C --> D{凭证是否为空?}
D --> |是| E[执行原方法]
D --> |否| F[尝试加锁]
F --> G{加锁成功?}
G --> |否| H[返回重复提交错误]
G --> |是| I[执行原方法]
I --> J[释放锁]
J --> K[结束]
B --> |否| E
```

**Section sources**
- [RepeatSubmit.java](file://smart-admin-api-java17-springboot3\sa-base\src\main\java\net\lab1024\sa\base\module\support\repeatsubmit\annoation\RepeatSubmit.java)
- [RepeatSubmitAspect.java](file://smart-admin-api-java17-springboot3\sa-base\src\main\java\net\lab1024\sa\base\module\support\repeatsubmit\RepeatSubmitAspect.java)
- [RepeatSubmitConfig.java](file://smart-admin-api-java17-springboot3\sa-base\src\main\java\net\lab1024\sa\base\config\RepeatSubmitConfig.java)
- [RepeatSubmitRedisTicket.java](file://smart-admin-api-java17-springboot3\sa-base\src\main\java\net\lab1024\sa\base\module\support\repeatsubmit\ticket\RepeatSubmitRedisTicket.java)