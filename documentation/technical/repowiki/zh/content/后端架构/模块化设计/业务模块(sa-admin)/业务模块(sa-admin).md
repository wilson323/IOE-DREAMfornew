# 业务模块(sa-admin)

<cite>
**本文档引用文件**  
- [AdminApplication.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/AdminApplication.java)
- [GoodsController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/controller/GoodsController.java)
- [GoodsService.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/service/GoodsService.java)
- [GoodsDao.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/dao/GoodsDao.java)
- [GoodsEntity.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/domain/entity/GoodsEntity.java)
- [EmployeeController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/employee/controller/EmployeeController.java)
- [EmployeeService.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/employee/service/EmployeeService.java)
- [EmployeeDao.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/employee/dao/EmployeeDao.java)
- [EmployeeEntity.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/employee/domain/entity/EmployeeEntity.java)
- [EmployeeManager.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/employee/manager/EmployeeManager.java)
</cite>

## 目录
1. [简介](#简介)
2. [项目结构](#项目结构)
3. [四层架构实现模式](#四层架构实现模式)
4. [业务域划分](#业务域划分)
5. [基础设施集成](#基础设施集成)
6. [完整处理流程](#完整处理流程)

## 简介

sa-admin业务模块是基于Spring Boot 3和Java 17构建的企业级后台管理系统，采用四层架构设计模式（Controller-Service-Manager-DAO），实现了清晰的职责分离和模块化设计。该系统包含两大业务域：business（业务系统）和system（系统管理），涵盖了商品管理、员工管理、部门管理、角色权限等核心功能。

系统通过MyBatis Plus实现数据访问，结合Redis缓存提升性能，并采用Sa-Token进行权限认证。通过拦截器、配置类等基础设施与业务代码的集成，实现了安全、高效、可扩展的后台管理解决方案。

## 项目结构

sa-admin模块采用标准的Maven项目结构，主要分为以下几个部分：

```mermaid
graph TD
A[sa-admin] --> B[src/main/java]
A --> C[src/main/resources]
B --> D[net.lab1024.sa.admin]
D --> E[config]
D --> F[constant]
D --> G[interceptor]
D --> H[module]
H --> I[business]
H --> J[system]
C --> K[resources]
K --> L[dev]
K --> M[mapper]
M --> N[business]
M --> O[system]
```

**图源**  
- [AdminApplication.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/AdminApplication.java)

**本节来源**  
- [AdminApplication.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/AdminApplication.java)

## 四层架构实现模式

sa-admin模块采用典型的四层架构设计，各层职责明确，形成了清晰的调用链路。

### Controller层 - HTTP请求处理

Controller层负责处理HTTP请求，进行参数校验、权限验证，并调用Service层处理业务逻辑。

以商品管理为例，`GoodsController`处理商品相关的HTTP请求：

```mermaid
classDiagram
class GoodsController {
+goodsService : GoodsService
+query(GoodsQueryForm) ResponseDTO~PageResult~GoodsVO~~
+add(GoodsAddForm) ResponseDTO~String~
+update(GoodsUpdateForm) ResponseDTO~String~
+delete(Long) ResponseDTO~String~
+batchDelete(ValidateList~Long~) ResponseDTO~String~
+importGoods(MultipartFile) ResponseDTO~String~
+exportGoods(HttpServletResponse) void
}
GoodsController --> GoodsService : "依赖"
```

**图源**  
- [GoodsController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/controller/GoodsController.java)

**本节来源**  
- [GoodsController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/controller/GoodsController.java)

### Service层 - 业务逻辑实现

Service层实现核心业务逻辑，协调多个DAO或Manager组件完成复杂的业务操作。

以商品服务为例，`GoodsService`实现了商品的增删改查、导入导出等业务逻辑：

```mermaid
classDiagram
class GoodsService {
+goodsDao : GoodsDao
+categoryQueryService : CategoryQueryService
+dataTracerService : DataTracerService
+dictService : DictService
+add(GoodsAddForm) ResponseDTO~String~
+update(GoodsUpdateForm) ResponseDTO~String~
+delete(Long) ResponseDTO~String~
+batchDelete(Long[]) ResponseDTO~String~
+query(GoodsQueryForm) ResponseDTO~PageResult~GoodsVO~~
+importGoods(MultipartFile) ResponseDTO~String~
+getAllGoods() GoodsExcelVO[]
-checkGoods(GoodsAddForm) ResponseDTO~String~
}
GoodsService --> GoodsDao : "依赖"
GoodsService --> CategoryQueryService : "依赖"
GoodsService --> DataTracerService : "依赖"
GoodsService --> DictService : "依赖"
```

**图源**  
- [GoodsService.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/service/GoodsService.java)

**本节来源**  
- [GoodsService.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/service/GoodsService.java)

### Manager层 - 缓存与业务编排

Manager层负责处理缓存、复杂业务编排和批量操作，通常继承自MyBatis Plus的ServiceImpl，提供更高级的业务操作封装。

以员工管理为例，`EmployeeManager`负责员工与角色关系的管理：

```mermaid
classDiagram
class EmployeeManager {
+employeeDao : EmployeeDao
+roleEmployeeService : RoleEmployeeService
+roleEmployeeDao : RoleEmployeeDao
+saveEmployee(EmployeeEntity, Long[]) void
+updateEmployee(EmployeeEntity, Long[]) void
+updateEmployeeRole(Long, RoleEmployeeEntity[]) void
}
EmployeeManager --> EmployeeDao : "依赖"
EmployeeManager --> RoleEmployeeService : "依赖"
EmployeeManager --> RoleEmployeeDao : "依赖"
```

**图源**  
- [EmployeeManager.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/employee/manager/EmployeeManager.java)

**本节来源**  
- [EmployeeManager.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/employee/manager/EmployeeManager.java)

### DAO层 - 数据访问

DAO层通过MyBatis实现数据访问，定义了与数据库交互的接口。

以商品DAO为例，`GoodsDao`定义了商品数据的访问方法：

```mermaid
classDiagram
class GoodsDao {
+query(Page, GoodsQueryForm) GoodsVO[]
+batchUpdateDeleted(Long[], Boolean) void
}
GoodsDao --|> BaseMapper : "继承"
class BaseMapper {
+insert(T) int
+deleteById(Serializable) int
+updateById(T) int
+selectById(Serializable) T
+selectList(Wrapper) T[]
}
```

**图源**  
- [GoodsDao.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/dao/GoodsDao.java)

**本节来源**  
- [GoodsDao.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/goods/dao/GoodsDao.java)

## 业务域划分

sa-admin模块将业务功能划分为两大业务域：business（业务系统）和system（系统管理），体现了清晰的业务边界划分。

### business业务系统

business业务域包含企业核心业务功能，如商品管理、分类管理、订单管理等。

```mermaid
graph TD
A[business业务域] --> B[商品管理]
A --> C[分类管理]
A --> D[OA办公]
B --> E[商品增删改查]
B --> F[商品导入导出]
B --> G[商品状态管理]
C --> H[分类增删改查]
C --> I[分类缓存管理]
D --> J[企业信息管理]
D --> K[发票管理]
D --> L[公告管理]
```

### system系统管理

system业务域包含系统基础管理功能，如员工管理、部门管理、角色权限等。

```mermaid
graph TD
A[system系统域] --> B[员工管理]
A --> C[部门管理]
A --> D[角色权限]
A --> E[菜单管理]
A --> F[登录管理]
B --> G[员工增删改查]
B --> H[员工密码管理]
B --> I[员工状态管理]
C --> J[部门树形结构]
C --> K[部门路径管理]
D --> L[角色分配]
D --> M[权限校验]
E --> N[菜单配置]
E --> O[菜单权限]
F --> P[登录认证]
F --> Q[会话管理]
```

**图源**  
- [EmployeeController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/employee/controller/EmployeeController.java)
- [EmployeeService.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/employee/service/EmployeeService.java)

**本节来源**  
- [EmployeeController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/employee/controller/EmployeeController.java)
- [EmployeeService.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/employee/service/EmployeeService.java)

## 基础设施集成

sa-admin模块通过拦截器、配置类等基础设施与业务代码集成，提供了丰富的系统功能。

### 拦截器集成

系统通过自定义拦截器实现各种横切关注点：

```mermaid
sequenceDiagram
participant Client as "客户端"
participant Interceptor as "拦截器"
participant Controller as "Controller"
participant Service as "Service"
Client->>Interceptor : 发送请求
Interceptor->>Interceptor : 权限校验(@SaCheckPermission)
Interceptor->>Interceptor : 参数解密(@ApiDecrypt)
Interceptor->>Controller : 放行请求
Controller->>Service : 调用业务逻辑
Service-->>Controller : 返回结果
Controller-->>Interceptor : 返回响应
Interceptor-->>Client : 返回客户端
```

### 配置类集成

系统通过配置类实现各种功能的配置和初始化：

```mermaid
classDiagram
class AdminApplication {
+COMPONENT_SCAN : String
+main(String[]) void
}
AdminApplication --> EnableCaching : "启用缓存"
AdminApplication --> EnableScheduling : "启用定时任务"
AdminApplication --> EnableAspectJAutoProxy : "启用AOP"
AdminApplication --> ComponentScan : "组件扫描"
AdminApplication --> MapperScan : "Mapper扫描"
AdminApplication --> SpringBootApplication : "Spring Boot应用"
```

**图源**  
- [AdminApplication.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/AdminApplication.java)

**本节来源**  
- [AdminApplication.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/AdminApplication.java)

## 完整处理流程

以员工管理为例，展示从业务请求到数据持久化的完整处理流程：

```mermaid
sequenceDiagram
participant Frontend as "前端"
participant Controller as "EmployeeController"
participant Service as "EmployeeService"
participant Manager as "EmployeeManager"
participant Dao as "EmployeeDao"
participant Database as "数据库"
Frontend->>Controller : POST /employee/add
Controller->>Service : addEmployee(EmployeeAddForm)
Service->>Service : 校验登录名、手机号唯一性
Service->>Service : 生成随机密码
Service->>Manager : saveEmployee(EmployeeEntity, roleIdList)
Manager->>Dao : insert(EmployeeEntity)
Dao->>Database : INSERT INTO t_employee
Database-->>Dao : 返回主键
Dao-->>Manager : 返回结果
Manager->>Manager : 批量插入角色关系
Manager->>Dao : batchInsert(roleEmployeeList)
Dao->>Database : INSERT INTO t_role_employee
Database-->>Dao : 返回结果
Dao-->>Manager : 返回结果
Manager-->>Service : 返回结果
Service-->>Controller : 返回随机密码
Controller-->>Frontend : 返回响应
```

**图源**  
- [EmployeeController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/employee/controller/EmployeeController.java)
- [EmployeeService.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/employee/service/EmployeeService.java)
- [EmployeeManager.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/employee/manager/EmployeeManager.java)
- [EmployeeDao.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/employee/dao/EmployeeDao.java)

**本节来源**  
- [EmployeeController.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/employee/controller/EmployeeController.java)
- [EmployeeService.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/employee/service/EmployeeService.java)
- [EmployeeManager.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/employee/manager/EmployeeManager.java)
- [EmployeeDao.java](file://smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/employee/dao/EmployeeDao.java)