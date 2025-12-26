# 测试环境部署指南

**版本**: v1.0.0  
**日期**: 2025-01-30  
**项目**: IOE-DREAM智能管理系统  
**环境**: 测试环境（Test Environment）

---

## 📋 部署概述

本指南详细说明如何将IOE-DREAM智能管理系统部署到测试环境，包括环境准备、配置管理、服务启动和验证步骤。

---

## 🎯 部署目标

- ✅ 部署所有7个核心微服务
- ✅ 配置Nacos注册中心和配置中心
- ✅ 配置Redis缓存服务
- ✅ 配置MySQL数据库
- ✅ 验证服务健康状态
- ✅ 验证功能完整性

---

## 📦 前置要求

### 系统要求
- **操作系统**: Linux (CentOS 7+ / Ubuntu 18.04+) 或 Windows Server 2016+
- **Java版本**: JDK 17+
- **Maven版本**: 3.8+
- **Docker版本**: 20.10+（可选，用于容器化部署）

### 基础设施要求
- **Nacos**: 2.0.4+ (注册中心 + 配置中心)
- **MySQL**: 8.0+ (数据库)
- **Redis**: 6.0+ (缓存)
- **MinIO/OSS**: 对象存储（可选，文件存储）

### 网络要求
- **端口范围**: 8080-8095
- **服务间通信**: 确保网络互通
- **防火墙**: 开放必要端口

---

## 🚀 部署步骤

### 步骤1: 环境准备

#### 1.1 安装Java环境
```bash
# 检查Java版本
java -version

# 设置JAVA_HOME环境变量
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export PATH=$JAVA_HOME/bin:$PATH
```

#### 1.2 安装Maven
```bash
# 检查Maven版本
mvn -version

# 配置Maven仓库（如果需要）
```

#### 1.3 启动基础设施服务

**启动Nacos**:
```bash
# 下载Nacos
wget https://github.com/alibaba/nacos/releases/download/2.0.4/nacos-server-2.0.4.tar.gz

# 解压并启动
tar -xzf nacos-server-2.0.4.tar.gz
cd nacos/bin
sh startup.sh -m standalone  # Linux
startup.cmd -m standalone    # Windows
```

**启动MySQL**:
```bash
# 使用Docker启动MySQL
docker run -d \
  --name mysql-test \
  -e MYSQL_ROOT_PASSWORD=root123456 \
  -e MYSQL_DATABASE=ioedream_test \
  -p 3306:3306 \
  mysql:8.0

# 或使用本地MySQL服务
systemctl start mysql  # Linux
net start mysql        # Windows
```

**启动Redis**:
```bash
# 使用Docker启动Redis
docker run -d \
  --name redis-test \
  -p 6379:6379 \
  redis:6.0

# 或使用本地Redis服务
redis-server  # Linux
redis-server.exe  # Windows
```

---

### 步骤2: 数据库初始化

#### 2.1 执行数据库脚本
```bash
# 进入数据库脚本目录
cd deployment/mysql/init

# 执行初始化脚本
mysql -uroot -proot123456 ioedream_test < 01-init-database.sql
mysql -uroot -proot123456 ioedream_test < 02-init-tables.sql
mysql -uroot -proot123456 ioedream_test < 03-init-data.sql
```

#### 2.2 验证数据库
```sql
-- 连接MySQL
mysql -uroot -proot123456 ioedream_test

-- 检查表是否创建成功
SHOW TABLES;

-- 检查关键表数据
SELECT COUNT(*) FROM t_common_user;
SELECT COUNT(*) FROM t_common_department;
```

---

### 步骤3: 配置Nacos

#### 3.1 登录Nacos控制台
- 访问: http://localhost:8848/nacos
- 用户名: nacos
- 密码: nacos

#### 3.2 创建命名空间
- 命名空间ID: `test`
- 命名空间名称: `测试环境`
- 描述: `IOE-DREAM测试环境配置`

#### 3.3 导入配置文件
- 从 `deployment/nacos/config/test/` 目录导入所有配置文件
- 配置文件命名规则: `{service-name}-{profile}.yaml`

#### 3.4 配置数据库连接
更新各服务的数据库配置:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ioedream_test?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: root123456
```

---

### 步骤4: 构建项目

#### 4.1 构建公共模块（必须先构建）
```bash
cd D:\IOE-DREAM

# 构建microservices-common
cd microservices/microservices-common
mvn clean install -DskipTests
```

#### 4.2 构建各微服务
```bash
cd D:\IOE-DREAM

# 构建网关服务
cd microservices/ioedream-gateway-service
mvn clean package -DskipTests

# 构建公共业务服务
cd ../ioedream-common-service
mvn clean package -DskipTests

# 构建设备通讯服务
cd ../ioedream-device-comm-service
mvn clean package -DskipTests

# 构建OA服务
cd ../ioedream-oa-service
mvn clean package -DskipTests

# 构建业务服务（可并行构建）
cd ../ioedream-access-service
mvn clean package -DskipTests

cd ../ioedream-attendance-service
mvn clean package -DskipTests

cd ../ioedream-consume-service
mvn clean package -DskipTests

cd ../ioedream-visitor-service
mvn clean package -DskipTests

cd ../ioedream-video-service
mvn clean package -DskipTests
```

---

### 步骤5: 启动服务

#### 5.1 启动顺序（重要！）

**必须按以下顺序启动**:

1. **Nacos** (已启动)
2. **MySQL** (已启动)
3. **Redis** (已启动)
4. **ioedream-gateway-service** (8080)
5. **ioedream-common-service** (8088)
6. **ioedream-device-comm-service** (8087)
7. **ioedream-oa-service** (8089)
8. **业务服务**（可并行启动）
   - ioedream-access-service (8090)
   - ioedream-attendance-service (8091)
   - ioedream-video-service (8092)
   - ioedream-consume-service (8094)
   - ioedream-visitor-service (8095)

#### 5.2 启动脚本示例

**Windows (PowerShell)**:
```powershell
# 启动网关服务
cd microservices/ioedream-gateway-service/target
java -jar ioedream-gateway-service-1.0.0.jar

# 启动公共业务服务
cd ../../ioedream-common-service/target
java -jar ioedream-common-service-1.0.0.jar

# ... 其他服务类似
```

**Linux (Bash)**:
```bash
# 启动网关服务
cd microservices/ioedream-gateway-service/target
nohup java -jar ioedream-gateway-service-1.0.0.jar > gateway.log 2>&1 &

# 启动公共业务服务
cd ../../ioedream-common-service/target
nohup java -jar ioedream-common-service-1.0.0.jar > common.log 2>&1 &

# ... 其他服务类似
```

#### 5.3 使用Docker Compose启动（推荐）

```bash
cd deployment/docker/test

# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看服务日志
docker-compose logs -f
```

---

### 步骤6: 验证部署

#### 6.1 验证服务注册

**检查Nacos服务列表**:
- 访问: http://localhost:8848/nacos
- 路径: 服务管理 → 服务列表
- 验证: 所有7个服务都已注册

**期望结果**:
- ✅ ioedream-gateway-service
- ✅ ioedream-common-service
- ✅ ioedream-device-comm-service
- ✅ ioedream-oa-service
- ✅ ioedream-access-service
- ✅ ioedream-attendance-service
- ✅ ioedream-video-service
- ✅ ioedream-consume-service
- ✅ ioedream-visitor-service

#### 6.2 验证服务健康

**检查健康端点**:
```bash
# 网关服务
curl http://localhost:8080/actuator/health

# 公共业务服务
curl http://localhost:8088/actuator/health

# 其他服务类似...
```

**期望响应**:
```json
{
  "status": "UP"
}
```

#### 6.3 验证功能端点

**测试登录接口**:
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "123456"
  }'
```

**测试用户查询接口**:
```bash
curl http://localhost:8080/api/v1/user/1 \
  -H "Authorization: Bearer {token}"
```

---

## 🔧 配置说明

### 环境变量配置

**测试环境变量**:
```bash
# Nacos配置
export NACOS_SERVER_ADDR=localhost:8848
export NACOS_NAMESPACE=test
export NACOS_GROUP=IOE-DREAM

# 数据库配置
export MYSQL_HOST=localhost
export MYSQL_PORT=3306
export MYSQL_DATABASE=ioedream_test
export MYSQL_USERNAME=root
export MYSQL_PASSWORD=root123456

# Redis配置
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PASSWORD=

# 服务端口
export GATEWAY_PORT=8080
export COMMON_SERVICE_PORT=8088
export DEVICE_COMM_SERVICE_PORT=8087
export OA_SERVICE_PORT=8089
```

### JVM参数配置

**推荐JVM参数**:
```bash
-Xms512m -Xmx1024m
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:+PrintGCDetails
-XX:+PrintGCTimeStamps
-Dfile.encoding=UTF-8
-Duser.timezone=Asia/Shanghai
```

---

## 📊 监控和日志

### 日志配置

**日志路径**:
- Linux: `/var/log/ioedream/{service-name}/`
- Windows: `D:\logs\ioedream\{service-name}\`

**日志级别**: `INFO`（测试环境）

### 监控端点

**各服务监控端点**:
- 健康检查: `/actuator/health`
- 指标监控: `/actuator/metrics`
- Prometheus: `/actuator/prometheus`

---

## 🐛 故障排查

### 常见问题

#### 1. 服务无法注册到Nacos
**问题**: 服务启动后无法在Nacos看到
**解决**:
- 检查Nacos地址配置是否正确
- 检查网络连通性
- 检查Nacos服务是否正常运行

#### 2. 数据库连接失败
**问题**: 启动时报数据库连接错误
**解决**:
- 检查数据库服务是否启动
- 检查数据库连接配置
- 检查数据库用户权限

#### 3. 端口被占用
**问题**: 启动时报端口被占用错误
**解决**:
- 检查端口占用情况: `netstat -ano | findstr 8080`
- 停止占用端口的进程
- 修改服务端口配置

---

## ✅ 部署验证清单

### 基础设施验证
- [ ] Nacos服务正常运行
- [ ] MySQL数据库正常运行
- [ ] Redis缓存服务正常运行
- [ ] 网络连通性正常

### 服务部署验证
- [ ] 所有服务构建成功
- [ ] 所有服务启动成功
- [ ] 所有服务注册到Nacos
- [ ] 所有服务健康检查通过

### 功能验证
- [ ] 用户登录功能正常
- [ ] 用户查询功能正常
- [ ] 移动端消费统计功能正常
- [ ] 移动端账户查询功能正常
- [ ] 扫码消费功能正常
- [ ] 工作流引擎功能正常

### 性能验证
- [ ] 接口响应时间 < 500ms
- [ ] 服务启动时间 < 30s
- [ ] 内存使用 < 1GB（单服务）

---

## 📝 部署后操作

### 1. 数据初始化
- [ ] 导入测试用户数据
- [ ] 导入测试组织架构数据
- [ ] 导入测试设备数据

### 2. 功能测试
- [ ] 执行功能测试用例
- [ ] 执行集成测试用例
- [ ] 执行端到端测试用例

### 3. 性能测试
- [ ] 执行压力测试
- [ ] 执行并发测试
- [ ] 执行稳定性测试

---

## 🔄 回滚方案

如果部署失败，按以下步骤回滚:

1. **停止所有服务**
2. **恢复数据库备份**（如果有）
3. **恢复配置文件**
4. **重新部署上一版本**

---

**文档版本**: v1.0.0  
**最后更新**: 2025-01-30  
**维护人**: IOE-DREAM 运维团队

