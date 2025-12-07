# 依赖升级运行时验证指南

> **创建日期**: 2025-01-30  
> **适用范围**: MySQL Connector迁移和依赖升级后的运行时验证  
> **前置条件**: MySQL、Redis、Nacos服务已启动

---

## 🎯 验证目标

验证以下功能在依赖升级后是否正常工作：
1. MySQL数据库连接（新连接器 `mysql-connector-j:8.3.0`）
2. MyBatis-Plus查询功能
3. Druid连接池功能
4. JSON序列化/反序列化（Fastjson2）
5. JWT token生成和验证（JJWT）
6. Excel导入导出（Apache POI）
7. MapStruct对象映射

---

## 📋 前置条件检查

### 1. 检查基础设施服务

```powershell
# 检查MySQL服务
Test-NetConnection -ComputerName localhost -Port 3306

# 检查Redis服务
Test-NetConnection -ComputerName localhost -Port 6379

# 检查Nacos服务
Test-NetConnection -ComputerName localhost -Port 8848
```

### 2. 启动基础设施服务（如果未运行）

#### 方式1：使用Docker Compose（推荐）
```powershell
cd D:\IOE-DREAM\documentation\technical\verification\docker
docker-compose up -d mysql redis nacos

# 等待服务启动
Start-Sleep -Seconds 30
```

#### 方式2：使用Docker命令
```powershell
# 启动MySQL
docker run -d --name ioedream-mysql `
  -e MYSQL_ROOT_PASSWORD=root123456 `
  -e MYSQL_DATABASE=ioedream `
  -p 3306:3306 `
  mysql:8.0

# 启动Redis
docker run -d --name ioedream-redis `
  -p 6379:6379 `
  redis:6.2-alpine

# 启动Nacos
docker run -d --name ioedream-nacos `
  -e MODE=standalone `
  -p 8848:8848 `
  nacos/nacos-server:v2.3.0
```

---

## 🔍 功能验证步骤

### 1. MySQL连接验证

#### 1.1 使用MySQL客户端测试
```powershell
# 测试数据库连接
mysql -h localhost -u root -proot123456 -e "SELECT VERSION();"

# 测试新连接器（通过Java应用）
# 启动服务后查看日志，确认使用 mysql-connector-j:8.3.0
```

#### 1.2 验证连接器版本
```powershell
# 检查依赖树
cd D:\IOE-DREAM
mvn dependency:tree -Dincludes=com.mysql:mysql-connector-j | Select-String "mysql-connector-j"

# 应该看到：com.mysql:mysql-connector-j:8.3.0
```

#### 1.3 验证驱动类名
- **驱动类名**: `com.mysql.cj.jdbc.Driver`（保持不变）
- **连接字符串**: 无需修改，保持原有配置
- **API兼容性**: 完全兼容，无需修改代码

### 2. MyBatis-Plus查询功能验证

#### 2.1 启动服务
```powershell
cd D:\IOE-DREAM\microservices\ioedream-common-service
mvn spring-boot:run
```

#### 2.2 执行查询测试
```powershell
# 测试用户查询接口
curl http://localhost:8088/api/v1/users/1

# 检查日志，确认查询正常
```

#### 2.3 验证要点
- ✅ 查询功能正常
- ✅ SQL执行无错误
- ✅ 结果映射正确

### 3. Druid连接池功能验证

#### 3.1 访问Druid监控页面
```
http://localhost:8088/druid/index.html
```

#### 3.2 验证要点
- ✅ 连接池初始化成功
- ✅ 连接数正常
- ✅ SQL监控正常
- ✅ 慢SQL记录正常

### 4. JSON序列化/反序列化验证

#### 4.1 测试Fastjson2功能
```powershell
# 测试JSON序列化
curl -X POST http://localhost:8088/api/v1/users `
  -H "Content-Type: application/json" `
  -d '{"username":"test","realName":"测试用户"}'

# 验证响应JSON格式正确
```

#### 4.2 验证要点
- ✅ JSON序列化正常
- ✅ JSON反序列化正常
- ✅ 特殊字符处理正确

### 5. JWT Token生成和验证

#### 5.1 测试登录获取Token
```powershell
# 登录获取Token
$response = Invoke-RestMethod -Uri "http://localhost:8088/api/v1/auth/login" `
  -Method POST `
  -Body (@{username="admin";password="123456"} | ConvertTo-Json) `
  -ContentType "application/json"

$token = $response.data.token
Write-Host "Token: $token"
```

#### 5.2 使用Token访问接口
```powershell
# 使用Token访问受保护接口
Invoke-RestMethod -Uri "http://localhost:8088/api/v1/users/me" `
  -Headers @{Authorization="Bearer $token"}
```

#### 5.3 验证要点
- ✅ Token生成正常（JJWT 0.13.0）
- ✅ Token验证正常
- ✅ Token过期处理正常

### 6. Excel导入导出功能验证

#### 6.1 测试Excel导出
```powershell
# 导出Excel文件
Invoke-WebRequest -Uri "http://localhost:8088/api/v1/users/export" `
  -Headers @{Authorization="Bearer $token"} `
  -OutFile "users.xlsx"

# 验证文件格式正确（Apache POI 5.5.1）
```

#### 6.2 测试Excel导入
```powershell
# 上传Excel文件
$file = Get-Item "users.xlsx"
Invoke-RestMethod -Uri "http://localhost:8088/api/v1/users/import" `
  -Method POST `
  -Headers @{Authorization="Bearer $token"} `
  -InFile $file.FullName `
  -ContentType "multipart/form-data"
```

#### 6.3 验证要点
- ✅ Excel导出正常
- ✅ Excel导入正常
- ✅ 文件格式兼容

### 7. MapStruct对象映射验证

#### 7.1 检查生成的Mapper接口
```powershell
# 查找生成的Mapper实现类
Get-ChildItem -Path "microservices" -Recurse -Filter "*MapperImpl.java" | Select-Object FullName
```

#### 7.2 测试对象转换
```powershell
# 测试用户对象转换
# 通过API调用验证UserEntity到UserVO的转换
curl http://localhost:8088/api/v1/users/1
```

#### 7.3 验证要点
- ✅ Mapper接口生成正常（MapStruct 1.6.3）
- ✅ 对象转换正确
- ✅ 编译时注解处理正常

---

## 🚀 快速验证脚本

### 使用验证脚本
```powershell
# 检查服务状态
.\scripts\runtime-verification-guide.ps1 -CheckServices

# 测试数据库连接
.\scripts\runtime-verification-guide.ps1 -TestDatabase

# 查看服务启动指南
.\scripts\runtime-verification-guide.ps1 -TestServices
```

---

## 📊 验证结果记录

### 验证清单

| 功能 | 验证状态 | 备注 |
|------|---------|------|
| MySQL连接 | ⏳ 待验证 | 需要服务启动 |
| MyBatis-Plus查询 | ⏳ 待验证 | 需要服务启动 |
| Druid连接池 | ⏳ 待验证 | 需要服务启动 |
| JSON序列化 | ⏳ 待验证 | 需要服务启动 |
| JWT Token | ⏳ 待验证 | 需要服务启动 |
| Excel处理 | ⏳ 待验证 | 需要服务启动 |
| MapStruct映射 | ⏳ 待验证 | 需要服务启动 |

---

## ⚠️ 常见问题

### 1. MySQL连接失败
**问题**: 无法连接到MySQL数据库  
**解决**:
- 检查MySQL服务是否运行
- 检查连接配置（host、port、username、password）
- 检查防火墙设置
- 验证新连接器版本是否正确

### 2. 服务启动失败
**问题**: 微服务启动失败  
**解决**:
- 检查依赖是否正确安装
- 检查配置文件是否正确
- 查看启动日志定位问题
- 验证Nacos连接是否正常

### 3. 功能异常
**问题**: 升级后功能异常  
**解决**:
- 检查日志中的错误信息
- 验证依赖版本是否正确
- 检查是否有破坏性变更
- 参考升级文档进行排查

---

## 📝 验证报告模板

```markdown
# 运行时验证报告

**验证日期**: 2025-01-30
**验证人**: [姓名]
**验证环境**: [开发/测试/生产]

## 验证结果

### MySQL连接
- [ ] 连接成功
- [ ] 查询正常
- [ ] 新连接器版本: mysql-connector-j:8.3.0

### 功能验证
- [ ] MyBatis-Plus查询正常
- [ ] Druid连接池正常
- [ ] JSON序列化正常
- [ ] JWT Token正常
- [ ] Excel处理正常
- [ ] MapStruct映射正常

## 问题记录
[记录发现的问题]

## 结论
[验证结论]
```

---

**指南状态**: ✅ **已创建**  
**下一步**: 启动服务进行运行时验证  
**维护**: 开发团队
