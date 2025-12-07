# IOE-DREAM 项目启动与功能测试设计

## 一、项目启动前环境准备

### 1.1 必需软件环境检查

#### 基础环境要求

| 软件 | 版本要求 | 用途 | 检查命令 |
|------|---------|------|---------|
| Java JDK | 17+ | 后端微服务运行环境 | `java -version` |
| Maven | 3.8+ | 后端项目构建工具 | `mvn -version` |
| Node.js | 18+ | 前端项目运行环境 | `node -version` |
| MySQL | 8.0+ | 数据库服务 | `mysql --version` |
| Redis | 6.0+ | 缓存服务 | `redis-server --version` |
| Nacos | 2.3.0+ | 服务注册与配置中心 | 通过端口8848检查 |

#### 环境检查脚本

执行以下命令快速检查环境是否满足要求：

```powershell
# 检查Java版本
java -version
# 预期输出: openjdk version "17.x.x"

# 检查Maven版本
mvn -version
# 预期输出: Apache Maven 3.8.x

# 检查Node.js版本
node -version
# 预期输出: v18.x.x

# 检查npm版本
npm -version
```

### 1.2 数据库初始化

#### 数据库创建

```sql
-- 创建数据库
CREATE DATABASE ioedream DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 切换到新数据库
USE ioedream;
```

#### 初始化脚本执行顺序

按照以下顺序执行数据库脚本：

| 序号 | 脚本路径 | 说明 |
|------|---------|------|
| 1 | `database-scripts/common-service/00-database-init.sql` | 数据库基础结构 |
| 2 | `database-scripts/common-service/01-*.sql` ~ `18-*.sql` | 公共服务表结构 |
| 3 | `database-scripts/visitor/01-*.sql` | 访客管理表结构 |

#### 数据库配置验证

```sql
-- 检查数据库字符集
SHOW VARIABLES LIKE 'character_set%';
-- character_set_database 应为 utf8mb4

-- 检查时区配置
SHOW VARIABLES LIKE '%time_zone%';
-- 应为 Asia/Shanghai

-- 检查已创建的表
SHOW TABLES;
```

### 1.3 基础设施服务启动

#### 启动顺序要求

必须按照以下顺序启动基础设施服务：

| 顺序 | 服务名称 | 端口 | 启动方式 | 健康检查 |
|------|---------|------|---------|---------|
| 1 | MySQL | 3306 | `systemctl start mysql` 或 Docker | `mysql -u root -p -e "SELECT 1"` |
| 2 | Redis | 6379 | `systemctl start redis` 或 Docker | `redis-cli ping` (返回PONG) |
| 3 | Nacos | 8848/9848 | Nacos启动脚本 或 Docker | 访问 http://localhost:8848/nacos |

#### Docker方式启动基础设施

```powershell
# 进入项目根目录
cd D:\IOE-DREAM

# 启动基础设施服务（MySQL + Redis + Nacos）
docker-compose -f docker-compose-all.yml up -d mysql redis nacos

# 等待服务启动（30-60秒）
Start-Sleep -Seconds 60

# 检查服务状态
docker-compose -f docker-compose-all.yml ps

# 检查Nacos是否就绪
curl http://localhost:8848/nacos/v1/console/health/readiness
```

#### 传统方式启动基础设施

**MySQL启动**：
```powershell
# Windows
net start MySQL80

# Linux
sudo systemctl start mysql
```

**Redis启动**：
```powershell
# Windows
redis-server

# Linux
sudo systemctl start redis
```

**Nacos启动**：
```powershell
# Windows
cd nacos\bin
startup.cmd -m standalone

# Linux
cd nacos/bin
sh startup.sh -m standalone
```

### 1.4 环境变量配置

#### 必需环境变量清单

```powershell
# 数据库配置
$env:MYSQL_HOST = "localhost"
$env:MYSQL_PORT = "3306"
$env:MYSQL_DATABASE = "ioedream"
$env:MYSQL_USERNAME = "root"
$env:MYSQL_PASSWORD = "your_password"  # 请修改为实际密码

# Redis配置
$env:REDIS_HOST = "localhost"
$env:REDIS_PORT = "6379"
$env:REDIS_PASSWORD = "redis123"  # 如有密码请修改

# Nacos配置
$env:NACOS_SERVER_ADDR = "localhost:8848"
$env:NACOS_NAMESPACE = "public"
$env:NACOS_USERNAME = "nacos"
$env:NACOS_PASSWORD = "nacos"
```

## 二、后端微服务启动方案

### 2.1 构建顺序规范

#### 强制构建顺序

**黄金法则**：microservices-common 必须在任何业务服务构建之前完成构建和安装。

```
构建顺序:
1. microservices-common          ← 必须先构建（P0级）
   ↓
2. ioedream-gateway-service      ← 基础设施服务
   ↓
3. ioedream-common-service       ← 公共业务服务
   ↓
4. ioedream-device-comm-service  ← 设备通讯服务
   ↓
5. ioedream-oa-service          ← OA服务
   ↓
6. 业务服务（可并行构建）
   ├── ioedream-access-service
   ├── ioedream-attendance-service
   ├── ioedream-video-service
   ├── ioedream-consume-service
   └── ioedream-visitor-service
```

#### 统一构建脚本（推荐）

```powershell
# 方式1：构建所有服务（自动确保顺序）
.\scripts\build-all.ps1

# 方式2：构建指定服务（自动先构建common）
.\scripts\build-all.ps1 -Service ioedream-access-service

# 方式3：清理并构建
.\scripts\build-all.ps1 -Clean

# 方式4：跳过测试
.\scripts\build-all.ps1 -SkipTests
```

#### 手动构建方法

```powershell
# 步骤1：强制先构建 common（必须）
cd D:\IOE-DREAM
mvn clean install -pl microservices/microservices-common -am -DskipTests

# 步骤2：构建业务服务
mvn clean install -pl microservices/ioedream-access-service -am -DskipTests
```

### 2.2 微服务启动方案

#### 方案A：一键启动所有服务（推荐）

使用项目提供的完整启动脚本，自动按依赖顺序启动所有服务：

```powershell
# 启动所有服务（后端 + 前端 + 移动端）
.\scripts\start-all-complete.ps1

# 仅启动后端服务
.\scripts\start-all-complete.ps1 -BackendOnly

# 仅启动前端
.\scripts\start-all-complete.ps1 -FrontendOnly

# 仅启动移动端
.\scripts\start-all-complete.ps1 -MobileOnly

# 检查服务状态
.\scripts\start-all-complete.ps1 -CheckOnly
```

#### 方案B：按批次分步启动

**第一批次：基础设施服务**

```powershell
# 启动API网关服务（端口8080）
cd D:\IOE-DREAM\microservices\ioedream-gateway-service
mvn spring-boot:run

# 等待15秒确保网关启动
```

**第二批次：公共服务**

```powershell
# 启动公共业务服务（端口8088）
cd D:\IOE-DREAM\microservices\ioedream-common-service
mvn spring-boot:run

# 启动设备通讯服务（端口8087）
cd D:\IOE-DREAM\microservices\ioedream-device-comm-service
mvn spring-boot:run

# 等待15秒确保公共服务启动
```

**第三批次：业务服务**

```powershell
# 启动OA服务（端口8089）
cd D:\IOE-DREAM\microservices\ioedream-oa-service
mvn spring-boot:run

# 启动门禁服务（端口8090）
cd D:\IOE-DREAM\microservices\ioedream-access-service
mvn spring-boot:run

# 启动考勤服务（端口8091）
cd D:\IOE-DREAM\microservices\ioedream-attendance-service
mvn spring-boot:run

# 启动视频服务（端口8092）
cd D:\IOE-DREAM\microservices\ioedream-video-service
mvn spring-boot:run

# 启动消费服务（端口8094）
cd D:\IOE-DREAM\microservices\ioedream-consume-service
mvn spring-boot:run

# 启动访客服务（端口8095）
cd D:\IOE-DREAM\microservices\ioedream-visitor-service
mvn spring-boot:run
```

#### 方案C：Docker容器化启动

```powershell
# 构建所有服务镜像
docker-compose -f docker-compose-all.yml build

# 启动所有服务
docker-compose -f docker-compose-all.yml up -d

# 查看服务状态
docker-compose -f docker-compose-all.yml ps

# 查看服务日志
docker-compose -f docker-compose-all.yml logs -f
```

### 2.3 服务健康检查

#### 端口检查

```powershell
# 检查所有微服务端口
$ports = @(8080, 8088, 8087, 8089, 8090, 8091, 8092, 8094, 8095)

foreach ($port in $ports) {
    $result = Test-NetConnection -ComputerName localhost -Port $port -WarningAction SilentlyContinue
    if ($result.TcpTestSucceeded) {
        Write-Host "✅ 端口 $port - 服务运行中" -ForegroundColor Green
    } else {
        Write-Host "❌ 端口 $port - 服务未运行" -ForegroundColor Red
    }
}
```

#### 健康检查端点

| 服务名称 | 端口 | 健康检查URL | 预期响应 |
|---------|------|------------|---------|
| API网关 | 8080 | http://localhost:8080/actuator/health | `{"status":"UP"}` |
| 公共服务 | 8088 | http://localhost:8088/actuator/health | `{"status":"UP"}` |
| 设备通讯 | 8087 | http://localhost:8087/actuator/health | `{"status":"UP"}` |
| OA服务 | 8089 | http://localhost:8089/actuator/health | `{"status":"UP"}` |
| 门禁服务 | 8090 | http://localhost:8090/actuator/health | `{"status":"UP"}` |
| 考勤服务 | 8091 | http://localhost:8091/actuator/health | `{"status":"UP"}` |
| 视频服务 | 8092 | http://localhost:8092/actuator/health | `{"status":"UP"}` |
| 消费服务 | 8094 | http://localhost:8094/actuator/health | `{"status":"UP"}` |
| 访客服务 | 8095 | http://localhost:8095/actuator/health | `{"status":"UP"}` |

#### 健康检查脚本

```powershell
# 健康检查脚本
$services = @(
    @{Name="API网关"; Port=8080; Path="/actuator/health"},
    @{Name="公共服务"; Port=8088; Path="/actuator/health"},
    @{Name="设备通讯"; Port=8087; Path="/actuator/health"},
    @{Name="OA服务"; Port=8089; Path="/actuator/health"},
    @{Name="门禁服务"; Port=8090; Path="/actuator/health"},
    @{Name="考勤服务"; Port=8091; Path="/actuator/health"},
    @{Name="视频服务"; Port=8092; Path="/actuator/health"},
    @{Name="消费服务"; Port=8094; Path="/actuator/health"},
    @{Name="访客服务"; Port=8095; Path="/actuator/health"}
)

foreach ($service in $services) {
    $url = "http://localhost:$($service.Port)$($service.Path)"
    try {
        $response = Invoke-RestMethod -Uri $url -Method Get -TimeoutSec 5
        if ($response.status -eq "UP") {
            Write-Host "✅ $($service.Name) - 健康" -ForegroundColor Green
        } else {
            Write-Host "⚠️  $($service.Name) - 状态: $($response.status)" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "❌ $($service.Name) - 无法访问" -ForegroundColor Red
    }
}
```

## 三、前端与移动端启动方案

### 3.1 前端管理后台启动

#### 依赖安装

```powershell
# 进入前端项目目录
cd D:\IOE-DREAM\smart-admin-web-javascript

# 检查Node.js版本（必须18+）
node -version

# 安装依赖（使用国内镜像加速）
npm install --registry=https://registry.npmmirror.com

# 或使用yarn（如果已安装）
yarn install
```

#### 启动开发服务器

```powershell
# 启动前端开发服务器（端口3000）
npm run dev

# 等待编译完成
# 看到 "ready - started server on 0.0.0.0:3000, url: http://localhost:3000"

# 浏览器访问
# http://localhost:3000
```

#### 前端访问验证

| 验证项 | URL | 预期结果 |
|--------|-----|---------|
| 登录页面 | http://localhost:3000 | 显示登录界面 |
| API连接 | http://localhost:3000/api/health | 正常返回 |
| 静态资源 | 检查页面样式和图片 | 正常加载 |

### 3.2 移动端应用启动

#### H5端启动

```powershell
# 进入移动端项目目录
cd D:\IOE-DREAM\smart-app

# 安装依赖
npm install --registry=https://registry.npmmirror.com

# 启动H5开发服务器（端口8081）
npm run dev:h5

# 浏览器访问
# http://localhost:8081
```

#### 小程序端启动

```powershell
# 微信小程序
npm run dev:mp-weixin

# 支付宝小程序
npm run dev:mp-alipay
```

#### 移动端访问验证

| 平台 | 访问方式 | 验证项 |
|------|---------|--------|
| H5 | http://localhost:8081 | 页面正常渲染 |
| 微信小程序 | 微信开发者工具 | 能够正常预览 |
| 支付宝小程序 | 支付宝开发者工具 | 能够正常预览 |

## 四、核心功能测试方案

### 4.1 用户认证与授权测试

#### 测试目标
验证用户登录、登出、权限控制等核心安全功能。

#### 测试用例

**TC-AUTH-001：用户登录**

| 测试步骤 | 操作 | 预期结果 |
|---------|------|---------|
| 1 | 访问前端登录页面 http://localhost:3000 | 显示登录表单 |
| 2 | 输入用户名：admin，密码：admin123 | - |
| 3 | 点击登录按钮 | 登录成功，跳转到首页 |
| 4 | 检查浏览器localStorage | 存储了token |

**API测试**：
```bash
# 登录API测试
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'

# 预期响应
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "userInfo": {
      "userId": 1,
      "username": "admin",
      "nickname": "管理员"
    }
  }
}
```

**TC-AUTH-002：Token验证**

```bash
# 使用token访问受保护接口
curl -X GET http://localhost:8080/api/v1/user/info \
  -H "Authorization: Bearer {token}"

# 预期响应：返回用户信息
```

**TC-AUTH-003：权限控制**

| 测试场景 | 操作 | 预期结果 |
|---------|------|---------|
| 普通用户访问管理功能 | 尝试访问系统设置页面 | 提示无权限 |
| 管理员访问管理功能 | 访问系统设置页面 | 正常显示 |

### 4.2 组织架构管理测试

#### 测试目标
验证部门、用户、角色管理功能。

#### 测试用例

**TC-ORG-001：部门管理**

```bash
# 创建部门
curl -X POST http://localhost:8080/api/v1/department \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "departmentName": "技术部",
    "parentId": 0,
    "sort": 1,
    "remark": "技术研发部门"
  }'

# 预期响应
{
  "code": 200,
  "message": "success",
  "data": {
    "departmentId": 10,
    "departmentName": "技术部"
  }
}

# 查询部门列表
curl -X GET http://localhost:8080/api/v1/department/list \
  -H "Authorization: Bearer {token}"

# 更新部门
curl -X PUT http://localhost:8080/api/v1/department/10 \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "departmentName": "技术研发部",
    "sort": 1
  }'

# 删除部门
curl -X DELETE http://localhost:8080/api/v1/department/10 \
  -H "Authorization: Bearer {token}"
```

**TC-ORG-002：用户管理**

```bash
# 创建用户
curl -X POST http://localhost:8080/api/v1/user \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "zhangsan",
    "nickname": "张三",
    "phone": "13800138000",
    "email": "zhangsan@example.com",
    "departmentId": 10,
    "roleIds": [2]
  }'

# 分页查询用户
curl -X GET "http://localhost:8080/api/v1/user/page?pageNum=1&pageSize=10" \
  -H "Authorization: Bearer {token}"

# 重置密码
curl -X PUT http://localhost:8080/api/v1/user/reset-password \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2,
    "newPassword": "123456"
  }'
```

### 4.3 门禁管理功能测试

#### 测试目标
验证门禁设备管理、通行权限配置、通行记录查询功能。

#### 测试用例

**TC-ACCESS-001：门禁设备管理**

```bash
# 添加门禁设备
curl -X POST http://localhost:8080/api/v1/access/device \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "deviceName": "一号门禁",
    "deviceCode": "ACCESS-001",
    "deviceType": "FACE_RECOGNITION",
    "location": "办公楼1楼大厅",
    "ipAddress": "192.168.1.100",
    "status": 1
  }'

# 查询设备列表
curl -X GET http://localhost:8080/api/v1/access/device/list \
  -H "Authorization: Bearer {token}"

# 设备状态监控
curl -X GET http://localhost:8080/api/v1/access/device/status/ACCESS-001 \
  -H "Authorization: Bearer {token}"
```

**TC-ACCESS-002：通行权限配置**

```bash
# 配置用户通行权限
curl -X POST http://localhost:8080/api/v1/access/permission \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2,
    "deviceIds": ["ACCESS-001", "ACCESS-002"],
    "validFrom": "2024-01-01 00:00:00",
    "validTo": "2024-12-31 23:59:59",
    "timeSchedule": {
      "weekdays": [1,2,3,4,5],
      "timeRanges": [
        {"startTime": "08:00", "endTime": "18:00"}
      ]
    }
  }'

# 查询用户权限
curl -X GET http://localhost:8080/api/v1/access/permission/user/2 \
  -H "Authorization: Bearer {token}"
```

**TC-ACCESS-003：通行记录查询**

```bash
# 查询通行记录
curl -X GET "http://localhost:8080/api/v1/access/record?startTime=2024-01-01&endTime=2024-12-31&pageNum=1&pageSize=20" \
  -H "Authorization: Bearer {token}"

# 实时监控（WebSocket）
# 前端连接WebSocket: ws://localhost:8080/ws/access/monitor
```

### 4.4 考勤管理功能测试

#### 测试目标
验证考勤打卡、排班管理、考勤统计功能。

#### 测试用例

**TC-ATT-001：考勤打卡**

```bash
# 员工打卡
curl -X POST http://localhost:8080/api/v1/attendance/clock \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2,
    "clockType": "IN",
    "clockTime": "2024-01-15 08:30:00",
    "deviceCode": "ACCESS-001",
    "location": {
      "latitude": 39.9042,
      "longitude": 116.4074,
      "address": "北京市朝阳区"
    }
  }'

# 预期响应
{
  "code": 200,
  "message": "打卡成功",
  "data": {
    "recordId": 100,
    "status": "NORMAL",
    "remark": "正常打卡"
  }
}
```

**TC-ATT-002：排班管理**

```bash
# 创建班次
curl -X POST http://localhost:8080/api/v1/attendance/shift \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "shiftName": "正常班",
    "workStartTime": "09:00",
    "workEndTime": "18:00",
    "lunchStartTime": "12:00",
    "lunchEndTime": "13:00",
    "allowLateMinutes": 10,
    "allowEarlyMinutes": 10
  }'

# 为用户分配班次
curl -X POST http://localhost:8080/api/v1/attendance/schedule \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2,
    "shiftId": 1,
    "scheduleDate": "2024-01-15"
  }'
```

**TC-ATT-003：考勤统计**

```bash
# 个人考勤统计
curl -X GET "http://localhost:8080/api/v1/attendance/statistics/personal?userId=2&month=2024-01" \
  -H "Authorization: Bearer {token}"

# 预期响应
{
  "code": 200,
  "data": {
    "workDays": 20,
    "actualDays": 18,
    "lateDays": 2,
    "earlyDays": 1,
    "absentDays": 0,
    "overtimeHours": 5.5
  }
}

# 部门考勤统计
curl -X GET "http://localhost:8080/api/v1/attendance/statistics/department?departmentId=10&month=2024-01" \
  -H "Authorization: Bearer {token}"
```

### 4.5 消费管理功能测试

#### 测试目标
验证账户管理、消费交易、账户充值功能。

#### 测试用例

**TC-CONSUME-001：账户管理**

```bash
# 创建消费账户
curl -X POST http://localhost:8080/api/v1/consume/account \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2,
    "accountType": "MEAL_CARD",
    "balance": 1000.00,
    "status": 1
  }'

# 查询账户余额
curl -X GET http://localhost:8080/api/v1/consume/account/user/2 \
  -H "Authorization: Bearer {token}"

# 预期响应
{
  "code": 200,
  "data": {
    "accountId": 100,
    "userId": 2,
    "balance": 1000.00,
    "status": 1
  }
}
```

**TC-CONSUME-002：消费交易**

```bash
# 执行消费
curl -X POST http://localhost:8080/api/v1/consume/transaction \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2,
    "amount": 15.50,
    "transactionType": "CONSUME",
    "deviceCode": "CONSUME-001",
    "merchantId": 1,
    "remark": "食堂消费"
  }'

# 预期响应
{
  "code": 200,
  "message": "消费成功",
  "data": {
    "transactionId": 1001,
    "accountBalance": 984.50,
    "transactionTime": "2024-01-15 12:30:00"
  }
}

# 查询消费记录
curl -X GET "http://localhost:8080/api/v1/consume/record?userId=2&startDate=2024-01-01&endDate=2024-01-31&pageNum=1&pageSize=20" \
  -H "Authorization: Bearer {token}"
```

**TC-CONSUME-003：账户充值**

```bash
# 账户充值
curl -X POST http://localhost:8080/api/v1/consume/recharge \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2,
    "amount": 500.00,
    "paymentMethod": "WECHAT",
    "remark": "微信充值"
  }'

# 预期响应
{
  "code": 200,
  "message": "充值成功",
  "data": {
    "rechargeId": 2001,
    "newBalance": 1484.50,
    "rechargeTime": "2024-01-15 15:00:00"
  }
}
```

### 4.6 访客管理功能测试

#### 测试目标
验证访客预约、访客登记、访客通行功能。

#### 测试用例

**TC-VISITOR-001：访客预约**

```bash
# 创建访客预约
curl -X POST http://localhost:8080/api/v1/visitor/appointment \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "visitorName": "李四",
    "visitorPhone": "13900139000",
    "visitorCompany": "ABC科技公司",
    "visitPurpose": "商务洽谈",
    "visitDate": "2024-01-16",
    "visitTime": "14:00",
    "hostUserId": 2,
    "visitArea": "会议室A"
  }'

# 预期响应
{
  "code": 200,
  "message": "预约成功",
  "data": {
    "appointmentId": 3001,
    "qrCode": "VISITOR-2024011600001",
    "status": "PENDING"
  }
}
```

**TC-VISITOR-002：访客审批**

```bash
# 审批访客预约
curl -X PUT http://localhost:8080/api/v1/visitor/appointment/3001/approve \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "approved": true,
    "approvalRemark": "同意访问"
  }'

# 预期响应
{
  "code": 200,
  "message": "审批成功",
  "data": {
    "status": "APPROVED",
    "validFrom": "2024-01-16 14:00:00",
    "validTo": "2024-01-16 18:00:00"
  }
}
```

**TC-VISITOR-003：访客登记与通行**

```bash
# 访客签到登记
curl -X POST http://localhost:8080/api/v1/visitor/checkin \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "appointmentId": 3001,
    "idCard": "110101199001011234",
    "checkinTime": "2024-01-16 14:05:00",
    "deviceCode": "ACCESS-001"
  }'

# 访客签退
curl -X POST http://localhost:8080/api/v1/visitor/checkout \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "appointmentId": 3001,
    "checkoutTime": "2024-01-16 17:00:00",
    "deviceCode": "ACCESS-001"
  }'
```

### 4.7 视频监控功能测试

#### 测试目标
验证视频设备管理、实时监控、录像回放功能。

#### 测试用例

**TC-VIDEO-001：摄像头管理**

```bash
# 添加摄像头
curl -X POST http://localhost:8080/api/v1/video/camera \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "cameraName": "1号楼大厅摄像头",
    "cameraCode": "CAM-001",
    "location": "1号楼1层大厅",
    "streamUrl": "rtsp://192.168.1.200:554/stream",
    "resolution": "1920x1080",
    "status": 1
  }'

# 查询摄像头列表
curl -X GET http://localhost:8080/api/v1/video/camera/list \
  -H "Authorization: Bearer {token}"
```

**TC-VIDEO-002：实时监控**

```bash
# 获取实时视频流
curl -X GET http://localhost:8080/api/v1/video/live/CAM-001 \
  -H "Authorization: Bearer {token}"

# 预期响应：返回视频流URL
{
  "code": 200,
  "data": {
    "streamUrl": "http://localhost:8092/video/live/CAM-001.m3u8",
    "protocol": "HLS"
  }
}

# 控制云台（PTZ）
curl -X POST http://localhost:8080/api/v1/video/ptz/CAM-001 \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "action": "LEFT",
    "speed": 50
  }'
```

**TC-VIDEO-003：录像回放**

```bash
# 查询录像文件
curl -X GET "http://localhost:8080/api/v1/video/record?cameraCode=CAM-001&startTime=2024-01-15%2008:00:00&endTime=2024-01-15%2018:00:00" \
  -H "Authorization: Bearer {token}"

# 预期响应：返回录像文件列表
{
  "code": 200,
  "data": [
    {
      "recordId": 5001,
      "startTime": "2024-01-15 08:00:00",
      "endTime": "2024-01-15 09:00:00",
      "fileSize": 524288000,
      "playbackUrl": "/video/playback/5001.mp4"
    }
  ]
}

# 播放录像
curl -X GET http://localhost:8080/api/v1/video/playback/5001 \
  -H "Authorization: Bearer {token}"
```

## 五、集成测试场景

### 5.1 典型业务流程测试

#### 场景1：员工入职全流程

| 步骤 | 操作 | 涉及模块 | 验证点 |
|------|------|---------|--------|
| 1 | 创建员工账号 | 用户管理 | 账号创建成功 |
| 2 | 分配部门和角色 | 组织架构 | 权限生效 |
| 3 | 录入生物识别信息 | 设备通讯 | 人脸/指纹录入成功 |
| 4 | 配置门禁权限 | 门禁管理 | 员工可正常通行 |
| 5 | 分配考勤班次 | 考勤管理 | 班次分配成功 |
| 6 | 创建消费账户 | 消费管理 | 账户创建并充值 |

#### 场景2：访客来访全流程

| 步骤 | 操作 | 涉及模块 | 验证点 |
|------|------|---------|--------|
| 1 | 员工提交访客预约 | 访客管理 | 预约创建成功 |
| 2 | 主管审批访客预约 | 访客管理 + 权限 | 审批流程正常 |
| 3 | 访客前台登记 | 访客管理 | 身份验证通过 |
| 4 | 发放临时门禁权限 | 门禁管理 | 临时权限生效 |
| 5 | 访客通行记录 | 门禁管理 + 视频监控 | 记录完整 |
| 6 | 访客离场签退 | 访客管理 | 权限自动回收 |

#### 场景3：员工日常工作流程

| 时间 | 操作 | 涉及模块 | 验证点 |
|------|------|---------|--------|
| 08:30 | 刷脸进入办公楼 | 门禁管理 + 考勤管理 | 自动打卡 |
| 12:00 | 食堂刷脸消费 | 消费管理 | 账户扣款成功 |
| 14:00 | 进入会议室 | 门禁管理 + 视频监控 | 权限验证通过 |
| 18:30 | 刷脸离开办公楼 | 门禁管理 + 考勤管理 | 自动下班打卡 |
| 月末 | 查看考勤统计 | 考勤管理 | 统计数据准确 |

### 5.2 异常场景测试

#### 异常场景1：网络中断恢复

| 测试步骤 | 操作 | 预期结果 |
|---------|------|---------|
| 1 | 模拟Redis连接中断 | 服务降级，使用本地缓存 |
| 2 | 继续执行门禁通行 | 离线模式正常通行 |
| 3 | 恢复Redis连接 | 自动同步离线数据 |
| 4 | 验证数据一致性 | 数据完整无丢失 |

#### 异常场景2：并发访问压力

```bash
# 使用Apache Bench进行并发测试
ab -n 1000 -c 100 -H "Authorization: Bearer {token}" \
  http://localhost:8080/api/v1/access/record

# 验证点
# 1. 所有请求成功率 > 99%
# 2. 平均响应时间 < 200ms
# 3. 无数据库死锁
# 4. 无内存溢出
```

#### 异常场景3：权限边界测试

| 测试场景 | 操作 | 预期结果 |
|---------|------|---------|
| 普通员工访问管理功能 | 尝试删除用户 | 返回403 Forbidden |
| 权限过期用户 | 尝试通行门禁 | 拒绝通行 |
| Token过期 | 调用API | 返回401 Unauthorized |
| 跨部门数据访问 | 查询其他部门数据 | 数据隔离生效 |

## 六、性能测试方案

### 6.1 性能测试目标

| 性能指标 | 目标值 | 测试方法 |
|---------|--------|---------|
| 接口响应时间 | P95 < 200ms | JMeter压测 |
| 并发用户数 | 支持1000+ | 负载测试 |
| 数据库查询 | < 50ms | Explain分析 |
| 门禁通行速度 | < 1秒 | 实际测试 |
| 视频流延迟 | < 500ms | 实时监控 |

### 6.2 JMeter压测脚本示例

```xml
<!-- JMeter测试计划：门禁通行API压测 -->
<jmeterTestPlan>
  <ThreadGroup>
    <name>门禁通行压测</name>
    <numThreads>100</numThreads>
    <rampTime>10</rampTime>
    <loopCount>100</loopCount>
  </ThreadGroup>
  
  <HTTPSampler>
    <domain>localhost</domain>
    <port>8080</port>
    <path>/api/v1/access/verify</path>
    <method>POST</method>
    <headers>
      <Header name="Authorization" value="Bearer ${token}"/>
      <Header name="Content-Type" value="application/json"/>
    </headers>
    <body>{
      "userId": ${userId},
      "deviceCode": "ACCESS-001",
      "verifyType": "FACE"
    }</body>
  </HTTPSampler>
  
  <ResultCollector>
    <filename>access_verify_results.jtl</filename>
  </ResultCollector>
</jmeterTestPlan>
```

### 6.3 数据库性能优化验证

```sql
-- 验证索引效果
EXPLAIN SELECT * FROM t_access_record 
WHERE user_id = 2 
AND create_time BETWEEN '2024-01-01' AND '2024-12-31';

-- 预期：type=ref, key=idx_user_create_time, rows<100

-- 验证分页查询性能
EXPLAIN SELECT * FROM t_consume_record 
WHERE user_id = 2 
ORDER BY create_time DESC 
LIMIT 20;

-- 预期：避免全表扫描，使用索引
```

## 七、测试报告模板

### 7.1 测试执行记录

| 测试模块 | 测试用例数 | 通过数 | 失败数 | 通过率 | 备注 |
|---------|-----------|--------|--------|--------|------|
| 用户认证 | 10 | 10 | 0 | 100% | - |
| 组织架构 | 15 | 15 | 0 | 100% | - |
| 门禁管理 | 20 | 19 | 1 | 95% | 权限配置边界问题 |
| 考勤管理 | 18 | 18 | 0 | 100% | - |
| 消费管理 | 12 | 12 | 0 | 100% | - |
| 访客管理 | 15 | 14 | 1 | 93% | 预约流程优化 |
| 视频监控 | 10 | 10 | 0 | 100% | - |
| **总计** | **100** | **98** | **2** | **98%** | - |

### 7.2 缺陷记录

| 缺陷ID | 模块 | 严重程度 | 描述 | 状态 | 修复日期 |
|--------|------|---------|------|------|---------|
| BUG-001 | 门禁管理 | 中等 | 多设备权限配置冲突 | 已修复 | 2024-01-20 |
| BUG-002 | 访客管理 | 低 | 预约取消流程提示不清晰 | 待修复 | - |

### 7.3 性能测试结果

| 测试项 | 并发数 | TPS | 平均响应时间 | P95响应时间 | 错误率 |
|--------|--------|-----|------------|------------|--------|
| 用户登录 | 100 | 850 | 85ms | 120ms | 0% |
| 门禁验证 | 200 | 1200 | 95ms | 150ms | 0% |
| 考勤打卡 | 150 | 980 | 110ms | 180ms | 0% |
| 消费交易 | 100 | 750 | 95ms | 140ms | 0% |

### 7.4 测试结论与建议

**总体评估**：
- 系统功能完整度：98%
- 性能指标达标率：100%
- 稳定性评级：优秀
- 安全性评级：良好

**主要优点**：
1. 核心业务流程完整稳定
2. 接口性能表现优秀
3. 异常处理机制完善
4. 数据一致性保障可靠

**待改进项**：
1. 访客管理预约流程UI优化
2. 门禁权限配置边界验证增强
3. 移动端H5页面加载速度优化
4. 视频监控录像存储策略优化

**下一步计划**：
1. 修复已发现缺陷
2. 进行安全渗透测试
3. 优化数据库索引策略
4. 补充自动化测试用例

## 八、常见问题排查

### 8.1 启动失败问题

#### 问题1：端口被占用

```powershell
# 排查步骤
# 1. 检查端口占用
netstat -ano | findstr :8080

# 2. 查看占用端口的进程
tasklist | findstr <PID>

# 3. 终止进程
taskkill /PID <PID> /F

# 4. 重新启动服务
```

#### 问题2：数据库连接失败

```powershell
# 排查步骤
# 1. 检查MySQL服务状态
sc query MySQL80

# 2. 测试数据库连接
mysql -h localhost -u root -p

# 3. 检查防火墙设置
netsh advfirewall firewall show rule name=MySQL

# 4. 验证配置文件
# 检查 application.yml 中的数据库配置
```

#### 问题3：Nacos连接失败

```powershell
# 排查步骤
# 1. 检查Nacos服务状态
curl http://localhost:8848/nacos/v1/console/health/readiness

# 2. 查看Nacos日志
cd nacos/logs
tail -f nacos.log

# 3. 验证服务注册
curl "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=ioedream-common-service"
```

### 8.2 功能异常问题

#### 问题1：登录失败

```bash
# 排查步骤
# 1. 检查用户是否存在
SELECT * FROM t_user WHERE username = 'admin';

# 2. 验证密码加密
# 确认密码加密方式与配置一致

# 3. 检查token生成
# 查看日志确认JWT token是否正常生成

# 4. 验证Redis缓存
redis-cli
> GET token:admin
```

#### 问题2：权限验证失败

```bash
# 排查步骤
# 1. 检查用户角色
SELECT * FROM t_user_role WHERE user_id = 2;

# 2. 检查角色权限
SELECT * FROM t_role_permission WHERE role_id = 2;

# 3. 验证权限配置
# 确认@PreAuthorize注解配置正确

# 4. 检查权限缓存
# 清除权限缓存后重试
```

### 8.3 性能问题

#### 问题1：接口响应慢

```bash
# 排查步骤
# 1. 检查数据库慢查询
SELECT * FROM mysql.slow_log ORDER BY start_time DESC LIMIT 10;

# 2. 分析SQL执行计划
EXPLAIN SELECT ...;

# 3. 检查缓存命中率
redis-cli INFO stats

# 4. 查看JVM性能
jstat -gcutil <PID> 1000 10
```

## 九、测试工具与环境

### 9.1 推荐测试工具

| 工具类型 | 工具名称 | 用途 |
|---------|---------|------|
| API测试 | Postman / Apifox | 接口功能测试 |
| 性能测试 | JMeter / Gatling | 压力测试、负载测试 |
| 自动化测试 | Selenium / Cypress | UI自动化测试 |
| 数据库工具 | Navicat / DBeaver | 数据验证 |
| 监控工具 | Prometheus + Grafana | 性能监控 |
| 日志分析 | ELK Stack | 日志分析 |

### 9.2 测试环境配置

| 环境类型 | 配置说明 | 访问地址 |
|---------|---------|---------|
| 开发环境 | 本地开发环境 | localhost |
| 测试环境 | Docker容器环境 | test.ioedream.com |
| 预生产环境 | 云服务器环境 | pre.ioedream.com |
| 生产环境 | 高可用集群 | www.ioedream.com |

## 十、测试检查清单

### 10.1 启动前检查

- [ ] Java 17+ 已安装并配置
- [ ] Maven 3.8+ 已安装并配置
- [ ] Node.js 18+ 已安装并配置
- [ ] MySQL 8.0+ 服务已启动
- [ ] Redis 6.0+ 服务已启动
- [ ] Nacos 2.3.0+ 服务已启动
- [ ] 数据库已初始化
- [ ] 环境变量已配置

### 10.2 服务启动检查

- [ ] microservices-common 构建成功
- [ ] API网关服务（8080）启动正常
- [ ] 公共服务（8088）启动正常
- [ ] 设备通讯服务（8087）启动正常
- [ ] OA服务（8089）启动正常
- [ ] 门禁服务（8090）启动正常
- [ ] 考勤服务（8091）启动正常
- [ ] 视频服务（8092）启动正常
- [ ] 消费服务（8094）启动正常
- [ ] 访客服务（8095）启动正常

### 10.3 功能测试检查

- [ ] 用户登录功能正常
- [ ] 部门管理功能正常
- [ ] 用户管理功能正常
- [ ] 角色权限功能正常
- [ ] 门禁设备管理正常
- [ ] 门禁通行记录正常
- [ ] 考勤打卡功能正常
- [ ] 考勤统计功能正常
- [ ] 消费交易功能正常
- [ ] 消费记录查询正常
- [ ] 访客预约功能正常
- [ ] 访客登记功能正常
- [ ] 视频实时监控正常
- [ ] 视频录像回放正常

### 10.4 前端测试检查

- [ ] 前端项目依赖安装成功
- [ ] 前端开发服务器启动正常
- [ ] 登录页面显示正常
- [ ] 菜单导航功能正常
- [ ] 数据表格显示正常
- [ ] 表单提交功能正常
- [ ] 移动端H5显示正常
- [ ] 小程序预览正常

### 10.5 性能测试检查

- [ ] 接口响应时间达标
- [ ] 并发压力测试通过
- [ ] 数据库查询性能达标
- [ ] 缓存命中率达标
- [ ] 内存使用正常
- [ ] CPU使用正常

---

**测试负责人**: 测试团队  
**技术支持**: 开发团队  
**更新日期**: 2024-01-15
