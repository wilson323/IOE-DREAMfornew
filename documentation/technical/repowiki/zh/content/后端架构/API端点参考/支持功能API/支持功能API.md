# 支持功能API

<cite>
**本文档引用的文件**  
- [AdminDictController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/support/AdminDictController.java)
- [FileController.java](file://smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/module/support/file/controller/FileController.java)
- [AdminOperateLogController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/support/AdminOperateLogController.java)
- [AdminHelpDocController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/support/AdminHelpDocController.java)
- [AdminSmartJobController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/support/AdminSmartJobController.java)
- [dict-api.js](file://smart-admin-web-javascript/src/api/support/dict-api.js)
- [file-api.js](file://smart-admin-web-javascript/src/api/support/file-api.js)
- [operate-log-api.js](file://smart-admin-web-javascript/src/api/support/operate-log-api.js)
- [help-doc-api.js](file://smart-admin-web-javascript/src/api/support/help-doc-api.js)
- [job-api.js](file://smart-admin-web-javascript/src/api/support/job-api.js)
</cite>

## 目录
1. [引言](#引言)
2. [数据字典API](#数据字典api)
3. [文件服务API](#文件服务api)
4. [操作日志API](#操作日志api)
5. [帮助文档API](#帮助文档api)
6. [定时任务API](#定时任务api)
7. [权限控制与安全考虑](#权限控制与安全考虑)
8. [结论](#结论)

## 引言
本文档旨在详细说明系统支持功能的API设计原则和使用模式，涵盖数据字典、文件服务、操作日志、帮助文档和定时任务等核心功能模块。通过分析系统架构和实现细节，为开发者提供全面的API使用指南。

## 数据字典API

数据字典功能提供了一套完整的CRUD（创建、读取、更新、删除）操作API，用于管理系统中的字典数据。系统采用分层设计，将字典分为字典项（Dict）和字典数据（DictData）两个层级。

```mermaid
classDiagram
class DictVO {
+Long dictId
+String dictCode
+String dictName
+String remark
+Boolean disabledFlag
+LocalDateTime createTime
+LocalDateTime updateTime
}
class DictDataVO {
+Long dictDataId
+Long dictId
+String dictCode
+String dictName
+String dataValue
+String dataLabel
+Integer sortOrder
+Boolean disabledFlag
+LocalDateTime createTime
+LocalDateTime updateTime
}
DictVO "1" -- "0..*" DictDataVO : 包含
class DictAddForm {
+String dictCode
+String dictName
+String remark
}
class DictUpdateForm {
+Long dictId
+String dictName
+String remark
+Boolean disabledFlag
}
class DictDataAddForm {
+Long dictId
+String dataValue
+String dataLabel
+Integer sortOrder
}
class DictDataUpdateForm {
+Long dictDataId
+String dataValue
+String dataLabel
+Integer sortOrder
+Boolean disabledFlag
}
DictAddForm --> DictVO : 创建
DictUpdateForm --> DictVO : 更新
DictDataAddForm --> DictDataVO : 创建
DictDataUpdateForm --> DictDataVO : 更新
```

**图源**  
- [DictVO.java](file://smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/module/support/dict/domain/vo/DictVO.java)
- [DictDataVO.java](file://smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/module/support/dict/domain/vo/DictDataVO.java)

### 数据字典查询API
数据字典提供了多种查询方式，包括分页查询、获取全部字典数据和根据字典ID查询字典数据。

```mermaid
sequenceDiagram
participant 前端 as 前端应用
participant DictApi as dictApi
participant DictController as AdminDictController
participant DictService as DictService
前端->>DictApi : queryDict(param)
DictApi->>DictController : POST /support/dict/queryPage
DictController->>DictService : queryPage(queryForm)
DictService-->>DictController : PageResult<DictVO>
DictController-->>DictApi : ResponseDTO<PageResult<DictVO>>
DictApi-->>前端 : 返回分页结果
```

**图源**  
- [dict-api.js](file://smart-admin-web-javascript/src/api/support/dict-api.js#L24-L27)
- [AdminDictController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/support/AdminDictController.java#L53-L58)

### 数据字典增删改API
数据字典的增删改操作遵循统一的API设计模式，所有修改操作都需要相应的权限校验。

```mermaid
flowchart TD
Start([开始]) --> CheckAuth["权限校验 @SaCheckPermission"]
CheckAuth --> ValidateInput["参数校验 @Valid"]
ValidateInput --> Operation["执行操作"]
Operation --> UpdateDB["更新数据库"]
UpdateDB --> RefreshCache["刷新缓存"]
RefreshCache --> ReturnResult["返回结果"]
ReturnResult --> End([结束])
subgraph Operation
Add["添加: add()"]
Update["更新: update()"]
Delete["删除: batchDelete()"]
end
```

**图源**  
- [AdminDictController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/support/AdminDictController.java#L60-L96)

**本节源码**  
- [AdminDictController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/support/AdminDictController.java)
- [dict-api.js](file://smart-admin-web-javascript/src/api/support/dict-api.js)

## 文件服务API

文件服务API提供了完整的文件上传、下载、查询和管理功能，支持多种文件存储方式和安全控制。

```mermaid
classDiagram
class FileUploadVO {
+String fileKey
+String fileName
+Long fileSize
+String fileUrl
}
class FileDownloadVO {
+byte[] data
+FileMetadataVO metadata
}
class FileMetadataVO {
+String fileName
+Long fileSize
+String contentType
}
class FileVO {
+Long fileId
+String fileKey
+String fileName
+String originalFileName
+Long fileSize
+String fileType
+String fileUrl
+Integer folder
+String uploadIp
+LocalDateTime createTime
}
class FileQueryForm {
+String keywords
+Integer folder
+String uploadIp
+String startDate
+String endDate
+Integer pageNum
+Integer pageSize
}
FileController --> FileService : 调用
FileService --> FileDao : 数据访问
FileService --> FileStorageService : 存储服务
FileStorageService --> LocalStorage : 本地存储
FileStorageService --> CloudStorage : 云存储
```

**图源**  
- [FileController.java](file://smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/module/support/file/controller/FileController.java)
- [FileService.java](file://smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/module/support/file/service/FileService.java)

### 文件上传下载实现
文件上传下载API采用流式处理方式，确保大文件处理的效率和稳定性。

```mermaid
sequenceDiagram
participant 前端 as 前端应用
participant FileApi as fileApi
participant FileController as FileController
participant FileService as FileService
participant FileStorage as FileStorageService
前端->>FileApi : uploadFile(file, folder)
FileApi->>FileController : POST /support/file/upload
FileController->>FileService : fileUpload(file, folder, requestUser)
FileService->>FileStorage : 上传到存储服务
FileStorage-->>FileService : 返回FileUploadVO
FileService-->>FileController : ResponseDTO<FileUploadVO>
FileController-->>FileApi : 返回上传结果
FileApi-->>前端 : 返回文件信息
前端->>FileApi : downLoadFile(fileKey)
FileApi->>FileController : GET /support/file/downLoad
FileController->>FileService : getDownloadFile(fileKey, userAgent)
FileService->>FileStorage : download(fileKey)
FileStorage-->>FileService : ResponseDTO<FileDownloadVO>
FileService-->>FileController : 设置响应头
FileController-->>前端 : 直接输出文件流
```

**图源**  
- [FileController.java](file://smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/module/support/file/controller/FileController.java#L44-L73)
- [file-api.js](file://smart-admin-web-javascript/src/api/support/file-api.js)

### 文件分片上传与预览
系统支持文件分片上传和预览功能，确保大文件上传的稳定性和用户体验。

```mermaid
flowchart TD
A([文件上传开始]) --> B["前端分片: 10MB/片"]
B --> C["计算文件MD5"]
C --> D["检查文件是否已存在"]
D --> |已存在| E["秒传: 直接返回文件信息"]
D --> |不存在| F["逐片上传"]
F --> G["服务端合并分片"]
G --> H["生成文件Key"]
H --> I["保存文件元数据"]
I --> J["返回文件URL"]
J --> K([上传完成])
L([文件预览]) --> M["根据fileKey获取文件URL"]
M --> N["浏览器直接访问URL"]
N --> O["服务端验证权限"]
O --> P["返回文件流"]
P --> Q([文件预览])
```

**图源**  
- [FileController.java](file://smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/module/support/file/controller/FileController.java)
- [file-api.js](file://smart-admin-web-javascript/src/api/support/file-api.js)

**本节源码**  
- [FileController.java](file://smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/module/support/file/controller/FileController.java)
- [file-api.js](file://smart-admin-web-javascript/src/api/support/file-api.js)

## 操作日志API

操作日志功能记录系统中所有重要操作，提供完整的审计追踪能力。

```mermaid
classDiagram
class OperateLogEntity {
+Long operateLogId
+Long operateUserId
+Integer operateUserType
+String operateUserName
+String module
+String content
+String requestUrl
+String requestMethod
+String requestParam
+String response
+String ip
+String userAgent
+String ipRegion
+Boolean successFlag
+LocalDateTime createTime
}
class OperateLogVO {
+Long operateLogId
+Long operateUserId
+Integer operateUserType
+String operateUserName
+String module
+String content
+String requestUrl
+String requestMethod
+String requestParam
+String response
+String ip
+String userAgent
+String ipRegion
+Boolean successFlag
+LocalDateTime createTime
+String browser
+String os
+String device
}
class OperateLogQueryForm {
+Long operateUserId
+Integer operateUserType
+String keywords
+String requestKeywords
+String startDate
+String endDate
+String userName
+Boolean successFlag
+Integer pageNum
+Integer pageSize
}
OperateLogEntity --> OperateLogVO : 转换
OperateLogQueryForm --> OperateLogEntity : 查询条件
```

**图源**  
- [OperateLogEntity.java](file://smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/module/support/operatelog/domain/OperateLogEntity.java)
- [OperateLogVO.java](file://smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/module/support/operatelog/domain/OperateLogVO.java)

### 操作日志记录机制
操作日志通过AOP切面自动记录，确保所有重要操作都被完整记录。

```mermaid
sequenceDiagram
participant 业务方法 as 业务方法
participant AOP切面 as 操作日志AOP
participant LogService as OperateLogService
participant LogDao as OperateLogDao
业务方法->>AOP切面 : 执行方法
AOP切面->>AOP切面 : 记录开始时间
AOP切面->>业务方法 : 执行业务逻辑
业务方法-->>AOP切面 : 返回结果
AOP切面->>AOP切面 : 记录结束时间
AOP切面->>AOP切面 : 构建日志实体
AOP切面->>LogService : 保存日志
LogService->>LogDao : insert(operateLogEntity)
LogDao-->>LogService : 返回结果
LogService-->>AOP切面 : 返回结果
AOP切面-->>业务方法 : 返回结果
```

**图源**  
- [AdminOperateLogController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/support/AdminOperateLogController.java)

### 操作日志查询API
操作日志提供灵活的查询接口，支持按多种条件组合查询。

```mermaid
flowchart TD
A([查询操作日志]) --> B["前端发送查询参数"]
B --> C["权限校验: @SaCheckPermission"]
C --> D["参数校验: @Valid"]
D --> E["构建查询条件"]
E --> F["分页查询: queryByPage"]
F --> G["处理用户代理信息"]
G --> H["解析浏览器、操作系统、设备"]
H --> I["返回分页结果"]
I --> J([查询完成])
subgraph 查询条件
K["用户ID"]
L["用户类型"]
M["关键字"]
N["请求关键字"]
O["日期范围"]
P["成功/失败"]
end
K --> E
L --> E
M --> E
N --> E
O --> E
P --> E
```

**图源**  
- [AdminOperateLogController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/support/AdminOperateLogController.java#L34-L39)
- [operate-log-api.js](file://smart-admin-web-javascript/src/api/support/operate-log-api.js)

**本节源码**  
- [AdminOperateLogController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/support/AdminOperateLogController.java)
- [operate-log-api.js](file://smart-admin-web-javascript/src/api/support/operate-log-api.js)

## 帮助文档API

帮助文档功能提供了一套完整的文档管理系统，支持文档的增删改查和目录管理。

```mermaid
classDiagram
class HelpDocCatalogEntity {
+Long helpDocCatalogId
+String catalogName
+Long parentId
+Integer sort
+LocalDateTime createTime
+LocalDateTime updateTime
}
class HelpDocEntity {
+Long helpDocId
+Long helpDocCatalogId
+String title
+String content
+Integer viewCount
+Integer userViewCount
+LocalDateTime publishTime
+LocalDateTime createTime
+LocalDateTime updateTime
}
class HelpDocViewRecordEntity {
+Long helpDocViewRecordId
+Long helpDocId
+Long userId
+LocalDateTime createTime
}
HelpDocCatalogEntity "1" -- "0..*" HelpDocEntity : 包含
HelpDocEntity "1" -- "0..*" HelpDocViewRecordEntity : 有记录
```

**图源**  
- [HelpDocCatalogEntity.java](file://smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/module/support/helpdoc/domain/entity/HelpDocCatalogEntity.java)
- [HelpDocEntity.java](file://smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/module/support/helpdoc/domain/entity/HelpDocEntity.java)

### 帮助文档管理API
帮助文档管理API提供了完整的CRUD操作和目录管理功能。

```mermaid
sequenceDiagram
participant 前端 as 前端应用
participant HelpDocApi as helpDocApi
participant HelpDocController as AdminHelpDocController
participant HelpDocService as HelpDocService
前端->>HelpDocApi : query(queryForm)
HelpDocApi->>HelpDocController : POST /support/helpDoc/query
HelpDocController->>HelpDocService : query(queryForm)
HelpDocService-->>HelpDocController : PageResult<HelpDocVO>
HelpDocController-->>HelpDocApi : ResponseDTO<PageResult<HelpDocVO>>
HelpDocApi-->>前端 : 返回分页结果
前端->>HelpDocApi : add(addForm)
HelpDocApi->>HelpDocController : POST /support/helpDoc/add
HelpDocController->>HelpDocService : add(addForm)
HelpDocService-->>HelpDocController : ResponseDTO<String>
HelpDocController-->>HelpDocApi : 返回结果
HelpDocApi-->>前端 : 返回添加结果
```

**图源**  
- [AdminHelpDocController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/support/AdminHelpDocController.java)
- [help-doc-api.js](file://smart-admin-web-javascript/src/api/support/help-doc-api.js)

**本节源码**  
- [AdminHelpDocController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/support/AdminHelpDocController.java)
- [help-doc-api.js](file://smart-admin-web-javascript/src/api/support/help-doc-api.js)

## 定时任务API

定时任务功能提供了一套完整的任务调度管理系统，支持任务的增删改查和执行记录查询。

```mermaid
classDiagram
class SmartJobEntity {
+Integer jobId
+String jobName
+String jobGroup
+String cronExpression
+String className
+String methodName
+String methodParams
+String jobStatus
+String concurrent
+String misfirePolicy
+String createBy
+String updateBy
+LocalDateTime createTime
+LocalDateTime updateTime
+String remark
}
class SmartJobLogEntity {
+Long jobLogId
+Integer jobId
+String jobName
+String jobGroup
+String methodName
+String methodParams
+String jobMessage
+Boolean status
+String exceptionInfo
+LocalDateTime createTime
}
class SmartJobQueryForm {
+String jobName
+String jobGroup
+String jobStatus
+Integer pageNum
+Integer pageSize
}
class SmartJobLogQueryForm {
+Integer jobId
+String jobName
+String jobGroup
+String status
+String startDate
+String endDate
+Integer pageNum
+Integer pageSize
}
SmartJobEntity "1" -- "0..*" SmartJobLogEntity : 有日志
```

**图源**  
- [SmartJobEntity.java](file://smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/module/support/job/domain/entity/SmartJobEntity.java)
- [SmartJobLogEntity.java](file://smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/module/support/job/domain/entity/SmartJobLogEntity.java)

### 定时任务管理API
定时任务管理API提供了完整的任务调度功能，包括任务的增删改查和立即执行。

```mermaid
sequenceDiagram
participant 前端 as 前端应用
participant JobApi as jobApi
participant JobController as AdminSmartJobController
participant JobService as SmartJobService
前端->>JobApi : queryJob(queryForm)
JobApi->>JobController : POST /support/job/query
JobController->>JobService : queryJob(queryForm)
JobService-->>JobController : PageResult<SmartJobVO>
JobController-->>JobApi : ResponseDTO<PageResult<SmartJobVO>>
JobApi-->>前端 : 返回分页结果
前端->>JobApi : executeJob(executeForm)
JobApi->>JobController : POST /support/job/execute
JobController->>JobService : execute(executeForm)
JobService-->>JobController : ResponseDTO<String>
JobController-->>JobApi : 返回执行结果
JobApi-->>前端 : 返回执行结果
```

**图源**  
- [AdminSmartJobController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/support/AdminSmartJobController.java)
- [job-api.js](file://smart-admin-web-javascript/src/api/support/job-api.js)

**本节源码**  
- [AdminSmartJobController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/support/AdminSmartJobController.java)
- [job-api.js](file://smart-admin-web-javascript/src/api/support/job-api.js)

## 权限控制与安全考虑

系统采用多层次的安全控制机制，确保API的安全性和可靠性。

```mermaid
graph TD
A[API请求] --> B[身份认证]
B --> C[权限校验]
C --> D[参数校验]
D --> E[业务逻辑]
E --> F[数据访问]
F --> G[响应返回]
subgraph 安全控制
B --> |JWT Token| H[Token校验]
C --> |Sa-Token| I[权限校验 @SaCheckPermission]
D --> |JSR-303| J[参数校验 @Valid]
E --> |日志记录| K[操作日志]
F --> |SQL注入防护| L[MyBatis-Plus]
end
```

**图源**  
- [AdminDictController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/support/AdminDictController.java)
- [AdminOperateLogController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/support/AdminOperateLogController.java)

### 权限校验机制
系统使用Sa-Token框架实现细粒度的权限控制，每个API接口都有明确的权限要求。

```mermaid
flowchart TD
A([API请求]) --> B["解析JWT Token"]
B --> C["获取用户信息"]
C --> D["检查权限注解"]
D --> |有@SaCheckPermission| E["验证用户权限"]
D --> |无注解| F["允许访问"]
E --> |有权限| G["执行业务逻辑"]
E --> |无权限| H["返回403错误"]
G --> I["返回结果"]
H --> I
```

**图源**  
- [AdminDictController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/support/AdminDictController.java)
- [SmartOperationCustomizer.java](file://smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/common/swagger/SmartOperationCustomizer.java)

### 安全最佳实践
系统遵循多项安全最佳实践，确保API的安全性。

```mermaid
erDiagram
USER ||--o{ OPERATE_LOG : "记录"
USER ||--o{ FILE : "上传"
USER ||--o{ HELP_DOC : "查看"
USER ||--o{ JOB : "执行"
OPERATE_LOG }|--|| USER : "属于"
FILE }|--|| USER : "上传者"
HELP_DOC }|--|| USER : "创建者"
JOB }|--|| USER : "执行者"
class USER {
+userId
+userName
+userType
}
class OPERATE_LOG {
+operateLogId
+operateUserId
+module
+content
+successFlag
}
class FILE {
+fileId
+fileKey
+uploadUserId
+folder
}
class HELP_DOC {
+helpDocId
+createUserId
+viewCount
}
class JOB {
+jobId
+executeUserId
+status
}
```

**图源**  
- [UserPermission.java](file://smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/common/domain/UserPermission.java)
- [Level3ProtectConfigForm.java](file://smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/module/support/securityprotect/domain/Level3ProtectConfigForm.java)

**本节源码**  
- [AdminDictController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/support/AdminDictController.java)
- [SmartOperationCustomizer.java](file://smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/common/swagger/SmartOperationCustomizer.java)

## 结论
本文档详细介绍了系统支持功能的API设计原则和使用模式。通过分析数据字典、文件服务、操作日志、帮助文档和定时任务等核心功能模块，展示了系统的完整架构和实现细节。系统采用现代化的技术栈和安全机制，确保了API的稳定性、安全性和可维护性。开发者可以基于本文档快速理解和使用系统提供的各项支持功能API。