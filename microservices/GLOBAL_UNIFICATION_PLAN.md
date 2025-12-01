# 全局统一化方案

## 📋 问题分析

### 1. ResponseDTO 重复实现（8个）
- ✅ **标准实现**: `microservices-common/src/main/java/net/lab1024/sa/common/domain/ResponseDTO.java`
- ❌ **冗余实现**: `microservices-common/src/main/java/net/lab1024/sa/common/response/ResponseDTO.java`
- ❌ **临时实现**: `ioedream-consume-service/src/main/java/net/lab1024/sa/consume/common/ResponseDTO.java`
- ❌ **本地实现**: `ioedream-report-service/src/main/java/net/lab1024/sa/report/common/ResponseDTO.java`
- ❌ **其他服务**: monitor, config, audit, infrastructure

### 2. PageResult 重复实现（3个）
- ✅ **标准实现**: `microservices-common/src/main/java/net/lab1024/sa/common/domain/PageResult.java`
- ❌ **临时实现**: `ioedream-consume-service/src/main/java/net/lab1024/sa/consume/common/PageResult.java` (字段不同：rows/total)
- ❌ **本地实现**: `ioedream-report-service/src/main/java/net/lab1024/sa/report/common/PageResult.java` (字段不同：currentPage/pageSize/totalCount/rows)

### 3. BaseEntity 重复实现（2个）
- ✅ **标准实现**: `microservices-common/src/main/java/net/lab1024/sa/common/entity/BaseEntity.java` (使用MyBatis-Plus注解)
- ❌ **本地实现**: `ioedream-identity-service/src/main/java/net/lab1024/sa/identity/common/entity/BaseEntity.java` (字段名不同：createUser vs createUserId)

### 4. PageForm 重复实现（2个）
- ✅ **标准实现**: `microservices-common/src/main/java/net/lab1024/sa/common/page/PageForm.java` (非泛型，被使用)
- ❌ **冗余实现**: `microservices-common/src/main/java/net/lab1024/sa/common/domain/PageForm.java` (泛型版本，可能未使用)

## 🎯 统一方案

### 标准类定义位置
- **ResponseDTO**: `net.lab1024.sa.common.domain.ResponseDTO`
- **PageResult**: `net.lab1024.sa.common.domain.PageResult`
- **BaseEntity**: `net.lab1024.sa.common.entity.BaseEntity`
- **PageForm**: `net.lab1024.sa.common.page.PageForm`

### 迁移计划
1. 统一identity-service的BaseEntity
2. 统一consume-service的ResponseDTO和PageResult
3. 统一report-service的ResponseDTO和PageResult
4. 统一其他服务的ResponseDTO
5. 删除microservices-common中的冗余ResponseDTO和PageForm
6. 删除所有服务的本地实现

