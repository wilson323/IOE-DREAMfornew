# 支持功能API

<cite>
**本文档引用的文件**
- [AdminApiEncryptController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/support/AdminApiEncryptController.java)
- [AdminHelpDocController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/support/AdminHelpDocController.java)
- [AdminSmartJobController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/support/AdminSmartJobController.java)
- [AdminSerialNumberController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/support/AdminSerialNumberController.java)
- [api-encrypt-api.js](file://smart-admin-web-javascript/src/api/support/api-encrypt-api.js)
- [help-doc-api.js](file://smart-admin-web-javascript/src/api/support/help-doc-api.js)
- [job-api.js](file://smart-admin-web-javascript/src/api/support/job-api.js)
- [serial-number-api.js](file://smart-admin-web-javascript/src/api/support/serial-number-api.js)
- [SerialNumberIdEnum.java](file://smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/module/support/serialnumber/constant/SerialNumberIdEnum.java)
- [HelpDocAddForm.java](file://smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/module/support/helpdoc/domain/form/HelpDocAddForm.java)
- [SmartJobAddForm.java](file://smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/module/support/job/api/domain/SmartJobAddForm.java)
- [SerialNumberGenerateForm.java](file://smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/module/support/serialnumber/domain/SerialNumberGenerateForm.java)
</cite>

## 目录
1. [接口加解密功能](#接口加解密功能)
2. [帮助文档管理功能](#帮助文档管理功能)
3. [定时任务管理功能](#定时任务管理功能)
4. [流水号生成功能](#流水号生成功能)
5. [业务应用场景](#业务应用场景)
6. [技术实现要点](#技术实现要点)

## 接口加解密功能

该功能提供API接口的请求参数加密、响应数据加密以及双向加解密能力，确保敏感数据在传输过程中的安全性。

### 接口加解密测试接口

#### 测试请求加密
- **HTTP方法**: POST
- **URL路径**: `/support/apiEncrypt/testRequestEncrypt`
- **请求参数**: 无
- **请求体结构**:
```json
{
  "name": "姓名",
  "age": 1
}
```
- **响应格式**:
```json
{
  "code": 0,
  "data": {
    "name": "姓名",
    "age": 1
  },
  "msg": "OK"
}
```
- **错误码**: 400（参数验证失败）

#### 测试响应加密
- **HTTP方法**: POST
- **URL路径**: `/support/apiEncrypt/testResponseEncrypt`
- **请求参数**: 无
- **请求体结构**:
```json
{
  "name": "姓名",
  "age": 1
}
```
- **响应格式**: 响应数据经过JWE加密处理
- **错误码**: 400（参数验证失败）

#### 测试请求参数加密和解密、返回数据加密和解密
- **HTTP方法**: POST
- **URL路径**: `/support/apiEncrypt/testDecryptAndEncrypt`
- **请求参数**: 无
- **请求体结构**: 请求数据经过加密处理
- **响应格式**: 响应数据经过加密处理
- **错误码**: 400（参数验证失败）

#### 测试数组加解密
- **HTTP方法**: POST
- **URL路径**: `/support/apiEncrypt/testArray`
- **请求参数**: 无
- **请求体结构**: 加密的数组数据
- **响应格式**: 加密的数组数据
- **错误码**: 400（参数验证失败）

**前端调用示例**:
```javascript
// 调用请求加密接口
encryptApi.testRequestEncrypt({
  name: '张三',
  age: 25
});

// 调用双向加解密接口
encryptApi.testDecryptAndEncrypt({
  name: '李四',
  age: 30
});
```

**使用场景**: 该功能适用于需要高安全性的场景，如金融交易、用户隐私数据传输等，通过在前端使用`postEncryptRequest`方法发送请求，后端自动解密并处理，返回时再自动加密，实现了透明的安全传输机制。

**Section sources**
- [AdminApiEncryptController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/support/AdminApiEncryptController.java#L38-L66)
- [api-encrypt-api.js](file://smart-admin-web-javascript/src/api/support/api-encrypt-api.js#L15-L37)

## 帮助文档管理功能

该功能提供完整的帮助文档管理系统，支持文档的增删改查以及目录管理。

### 帮助文档目录管理接口

#### 添加目录
- **HTTP方法**: POST
- **URL路径**: `/support/helpDoc/helpDocCatalog/add`
- **请求参数**: 无
- **请求体结构**:
```json
{
  "catalogName": "目录名称",
  "parentId": 0,
  "sort": 1
}
```
- **响应格式**:
```json
{
  "code": 0,
  "data": "添加成功",
  "msg": "OK"
}
```
- **错误码**: 400（参数验证失败）

#### 更新目录
- **HTTP方法**: POST
- **URL路径**: `/support/helpDoc/helpDocCatalog/update`
- **请求参数**: 无
- **请求体结构**:
```json
{
  "helpDocCatalogId": 1,
  "catalogName": "更新后的目录名称",
  "parentId": 0,
  "sort": 1
}
```
- **响应格式**: 同添加目录
- **错误码**: 400（参数验证失败）、404（目录不存在）

#### 删除目录
- **HTTP方法**: GET
- **URL路径**: `/support/helpDoc/helpDocCatalog/delete/{helpDocCatalogId}`
- **请求参数**: `helpDocCatalogId`（路径参数）
- **请求体结构**: 无
- **响应格式**: 同添加目录
- **错误码**: 404（目录不存在）

### 帮助文档管理接口

#### 分页查询
- **HTTP方法**: POST
- **URL路径**: `/support/helpDoc/query`
- **请求参数**: 无
- **请求体结构**:
```json
{
  "catalogId": 0,
  "title": "",
  "current": 1,
  "pageSize": 10
}
```
- **响应格式**:
```json
{
  "code": 0,
  "data": {
    "list": [
      {
        "helpDocId": 1,
        "title": "文档标题",
        "catalogName": "目录名称",
        "updateTime": "2023-01-01 00:00:00"
      }
    ],
    "total": 1,
    "current": 1,
    "size": 10,
    "pages": 1
  },
  "msg": "OK"
}
```
- **错误码**: 403（无权限）

#### 添加文档
- **HTTP方法**: POST
- **URL路径**: `/support/helpDoc/add`
- **请求参数**: 无
- **请求体结构**:
```json
{
  "title": "文档标题",
  "catalogId": 1,
  "content": "文档内容",
  "relationId": 0
}
```
- **响应格式**: 同添加目录
- **错误码**: 400（参数验证失败）、403（重复提交）

#### 更新文档
- **HTTP方法**: POST
- **URL路径**: `/support/helpDoc/update`
- **请求参数**: 无
- **请求体结构**:
```json
{
  "helpDocId": 1,
  "title": "更新后的标题",
  "catalogId": 1,
  "content": "更新后的内容"
}
```
- **响应格式**: 同添加目录
- **错误码**: 400（参数验证失败）、403（重复提交）、404（文档不存在）

#### 删除文档
- **HTTP方法**: GET
- **URL路径**: `/support/helpDoc/delete/{helpDocId}`
- **请求参数**: `helpDocId`（路径参数）
- **请求体结构**: 无
- **响应格式**: 同添加目录
- **错误码**: 404（文档不存在）

#### 获取文档详情
- **HTTP方法**: GET
- **URL路径**: `/support/helpDoc/getDetail/{helpDocId}`
- **请求参数**: `helpDocId`（路径参数）
- **请求体结构**: 无
- **响应格式**:
```json
{
  "code": 0,
  "data": {
    "helpDocId": 1,
    "title": "文档标题",
    "content": "文档内容",
    "catalogName": "目录名称",
    "updateTime": "2023-01-01 00:00:00"
  },
  "msg": "OK"
}
```
- **错误码**: 404（文档不存在）

**前端调用示例**:
```javascript
// 查询帮助文档列表
helpDocApi.query({
  current: 1,
  pageSize: 10
});

// 添加帮助文档
helpDocApi.add({
  title: '新文档',
  catalogId: 1,
  content: '文档内容'
});

// 获取文档详情
helpDocApi.getDetail(1);
```

**Section sources**
- [AdminHelpDocController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/support/AdminHelpDocController.java#L44-L103)
- [help-doc-api.js](file://smart-admin-web-javascript/src/api/support/help-doc-api.js#L14-L58)

## 定时任务管理功能

该功能提供强大的定时任务管理能力，支持任务的创建、执行、状态管理和日志查询。

### 定时任务管理接口

#### 分页查询
- **HTTP方法**: POST
- **URL路径**: `/support/job/query`
- **请求参数**: 无
- **请求体结构**:
```json
{
  "jobName": "",
  "jobGroup": "",
  "current": 1,
  "pageSize": 10
}
```
- **响应格式**:
```json
{
  "code": 0,
  "data": {
    "list": [
      {
        "jobId": 1,
        "jobName": "任务名称",
        "jobGroup": "任务组",
        "cronExpression": "0 0 12 * * ?",
        "jobStatus": 1,
        "description": "任务描述"
      }
    ],
    "total": 1,
    "current": 1,
    "size": 10,
    "pages": 1
  },
  "msg": "OK"
}
```
- **错误码**: 无

#### 查询任务详情
- **HTTP方法**: GET
- **URL路径**: `/support/job/{jobId}`
- **请求参数**: `jobId`（路径参数）
- **请求体结构**: 无
- **响应格式**:
```json
{
  "code": 0,
  "data": {
    "jobId": 1,
    "jobName": "任务名称",
    "jobGroup": "任务组",
    "cronExpression": "0 0 12 * * ?",
    "jobStatus": 1,
    "description": "任务描述",
    "createTime": "2023-01-01 00:00:00"
  },
  "msg": "OK"
}
```
- **错误码**: 404（任务不存在）

#### 执行任务
- **HTTP方法**: POST
- **URL路径**: `/support/job/execute`
- **请求参数**: 无
- **请求体结构**:
```json
{
  "jobId": 1
}
```
- **响应格式**:
```json
{
  "code": 0,
  "data": "执行成功",
  "msg": "OK"
}
```
- **错误码**: 400（参数验证失败）、404（任务不存在）

#### 添加任务
- **HTTP方法**: POST
- **URL路径**: `/support/job/add`
- **请求参数**: 无
- **请求体结构**:
```json
{
  "jobName": "任务名称",
  "jobGroup": "DEFAULT",
  "cronExpression": "0 0 12 * * ?",
  "jobClass": "com.example.JobClass",
  "description": "任务描述"
}
```
- **响应格式**: 同执行任务
- **错误码**: 400（参数验证失败）、403（重复提交）

#### 更新任务
- **HTTP方法**: POST
- **URL路径**: `/support/job/update`
- **请求参数**: 无
- **请求体结构**:
```json
{
  "jobId": 1,
  "jobName": "更新后的名称",
  "cronExpression": "0 0 13 * * ?",
  "description": "更新后的描述"
}
```
- **响应格式**: 同执行任务
- **错误码**: 400（参数验证失败）、403（重复提交）、404（任务不存在）

#### 更新任务状态
- **HTTP方法**: POST
- **URL路径**: `/support/job/update/enabled`
- **请求参数**: 无
- **请求体结构**:
```json
{
  "jobId": 1,
  "jobStatus": 1
}
```
- **响应格式**: 同执行任务
- **错误码**: 400（参数验证失败）、403（重复提交）

#### 删除任务
- **HTTP方法**: GET
- **URL路径**: `/support/job/delete?jobId={jobId}`
- **请求参数**: `jobId`（查询参数）
- **请求体结构**: 无
- **响应格式**: 同执行任务
- **错误码**: 403（重复提交）

#### 查询执行记录
- **HTTP方法**: POST
- **URL路径**: `/support/job/log/query`
- **请求参数**: 无
- **请求体结构**:
```json
{
  "jobId": 0,
  "jobName": "",
  "current": 1,
  "pageSize": 10
}
```
- **响应格式**:
```json
{
  "code": 0,
  "data": {
    "list": [
      {
        "jobLogId": 1,
        "jobId": 1,
        "jobName": "任务名称",
        "executeResult": 1,
        "executeMessage": "执行成功",
        "executeTime": "2023-01-01 00:00:00"
      }
    ],
    "total": 1,
    "current": 1,
    "size": 10,
    "pages": 1
  },
  "msg": "OK"
}
```
- **错误码**: 无

**前端调用示例**:
```javascript
// 查询定时任务列表
jobApi.queryJob({
  current: 1,
  pageSize: 10
});

// 添加定时任务
jobApi.addJob({
  jobName: '新任务',
  jobGroup: 'DEFAULT',
  cronExpression: '0 0 12 * * ?',
  jobClass: 'com.example.JobClass'
});

// 执行任务
jobApi.executeJob({
  jobId: 1
});
```

**Section sources**
- [AdminSmartJobController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/support/AdminSmartJobController.java#L34-L93)
- [job-api.js](file://smart-admin-web-javascript/src/api/support/job-api.js#L11-L42)

## 流水号生成功能

该功能提供灵活的流水号生成机制，支持多种业务场景的单据编号生成。

### 流水号生成接口

#### 生成单号
- **HTTP方法**: POST
- **URL路径**: `/support/serialNumber/generate`
- **请求参数**: 无
- **请求体结构**:
```json
{
  "serialNumberId": 1,
  "count": 1
}
```
- **响应格式**:
```json
{
  "code": 0,
  "data": ["SN202300001"],
  "msg": "OK"
}
```
- **错误码**: 400（参数验证失败）、403（无权限）

#### 获取所有单号定义
- **HTTP方法**: GET
- **URL路径**: `/support/serialNumber/all`
- **请求参数**: 无
- **请求体结构**: 无
- **响应格式**:
```json
{
  "code": 0,
  "data": [
    {
      "serialNumberId": 1,
      "serialNumberName": "销售订单",
      "prefix": "SO",
      "dateFormat": "yyyyMM",
      "digit": 5,
      "currentNumber": 1,
      "description": "销售订单编号"
    }
  ],
  "msg": "OK"
}
```
- **错误码**: 无

#### 查询生成记录
- **HTTP方法**: POST
- **URL路径**: `/support/serialNumber/queryRecord`
- **请求参数**: 无
- **请求体结构**:
```json
{
  "serialNumberId": 0,
  "current": 1,
  "pageSize": 10
}
```
- **响应格式**:
```json
{
  "code": 0,
  "data": {
    "list": [
      {
        "serialNumberRecordId": 1,
        "serialNumberId": 1,
        "serialNumber": "SN202300001",
        "generateTime": "2023-01-01 00:00:00"
      }
    ],
    "total": 1,
    "current": 1,
    "size": 10,
    "pages": 1
  },
  "msg": "OK"
}
```
- **错误码**: 403（无权限）

**前端调用示例**:
```javascript
// 生成流水号
serialNumberApi.generate({
  serialNumberId: 1,
  count: 1
});

// 获取所有单号定义
serialNumberApi.getAll();

// 查询生成记录
serialNumberApi.queryRecord({
  current: 1,
  pageSize: 10
});
```

**参数配置说明**: `serialNumberId`参数对应`SerialNumberIdEnum`枚举值，系统预定义了多种业务类型的流水号规则，如销售订单、采购订单、入库单等。每个流水号规则包含前缀、日期格式、位数等配置，支持灵活的业务需求。

**Section sources**
- [AdminSerialNumberController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/support/AdminSerialNumberController.java#L50-L72)
- [serial-number-api.js](file://smart-admin-web-javascript/src/api/support/serial-number-api.js#L14-L23)
- [SerialNumberIdEnum.java](file://smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/module/support/serialnumber/constant/SerialNumberIdEnum.java)

## 业务应用场景

### 接口加解密功能
适用于需要高安全性的业务场景，如金融支付、用户隐私信息传输、敏感数据交换等。通过在传输层对数据进行加密，有效防止数据被窃取或篡改，满足等保要求和行业安全规范。

### 帮助文档管理功能
适用于企业内部知识库管理、产品使用手册维护、技术文档共享等场景。通过目录化的文档管理，实现知识的结构化存储和高效检索，提升团队协作效率和新员工培训效果。

### 定时任务管理功能
适用于数据同步、报表生成、缓存刷新、消息推送等需要周期性执行的业务场景。通过可视化的任务管理界面，降低运维复杂度，提高系统自动化水平。

### 流水号生成功能
适用于各类业务单据的编号生成，如订单号、合同编号、工单号等。通过统一的编号规则管理，确保编号的唯一性和规范性，便于业务追踪和审计。

## 技术实现要点

### 接口加解密
采用JWE（JSON Web Encryption）标准实现数据加密，通过AOP切面在控制器层自动处理加解密逻辑，对业务代码无侵入。前端通过`postEncryptRequest`方法发送加密请求，后端自动解密后处理业务逻辑，返回时再自动加密。

### 帮助文档管理
采用树形结构管理文档目录，支持无限层级。文档内容使用富文本编辑器存储，支持图文混排。通过权限注解`@SaCheckPermission`控制不同角色的访问权限，确保文档安全。

### 定时任务管理
基于Quartz调度框架实现，通过`SmartJobService`封装调度逻辑，支持Cron表达式配置。任务执行日志持久化存储，便于问题排查和性能分析。采用`@RepeatSubmit`注解防止重复提交，确保系统稳定性。

### 流水号生成
采用数据库行锁机制保证编号的唯一性，避免并发生成时的重复问题。通过`SerialNumberIdEnum`枚举统一管理不同业务类型的编号规则，支持前缀、日期格式、位数等灵活配置。生成记录完整保存，满足审计要求。