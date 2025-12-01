# IOE-DREAM 考勤服务 (Attendance Service)

## 📋 服务概述

考勤服务是IOE-DREAM平台的核心业务服务之一，负责员工考勤管理、排班管理、考勤统计等功能。

### 核心功能

- ✅ **考勤打卡**: 支持多种打卡方式（位置打卡、设备打卡、离线打卡）
- ✅ **排班管理**: 灵活的排班规则配置和管理
- ✅ **考勤统计**: 自动计算考勤数据，生成统计报表
- ✅ **异常处理**: 考勤异常申请和审批流程
- ✅ **规则引擎**: 可配置的考勤规则引擎
- ✅ **缓存优化**: Redis缓存提升查询性能

## 🏗️ 技术架构

### 技术栈

- **框架**: Spring Boot 3.x
- **数据库**: MySQL 8.0+
- **缓存**: Redis
- **ORM**: MyBatis-Plus
- **服务发现**: Nacos
- **API文档**: Knife4j (Swagger)

### 核心模块

```
ioedream-attendance-service/
├── controller/          # 控制器层
├── service/             # 服务层
│   ├── impl/          # 服务实现
│   └── cache/         # 缓存服务
├── manager/            # 业务管理层
├── dao/                # 数据访问层
├── domain/             # 领域模型
│   ├── entity/        # 实体类
│   ├── dto/           # 数据传输对象
│   ├── vo/            # 视图对象
│   └── form/          # 表单对象
├── rule/              # 规则引擎
└── repository/        # 仓储层
```

## 🚀 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+
- Nacos 2.0+

### 启动步骤

1. **配置数据库**
   ```sql
   -- 创建数据库
   CREATE DATABASE ioedream_attendance DEFAULT CHARACTER SET utf8mb4;
   ```

2. **配置Nacos**
   - 启动Nacos服务
   - 在Nacos配置中心添加服务配置

3. **配置Redis**
   - 启动Redis服务
   - 配置连接信息

4. **启动服务**
   ```bash
   mvn spring-boot:run
   ```

5. **访问API文档**
   - 地址: http://localhost:8081/doc.html
   - 默认端口: 8081

## 📡 API接口

### 考勤打卡

- `POST /api/attendance/punch` - 员工打卡
- `POST /api/attendance/makeup` - 补打卡申请

### 考勤查询

- `GET /api/attendance/records` - 查询考勤记录
- `GET /api/attendance/statistics` - 查询考勤统计

### 排班管理

- `POST /api/attendance/schedule` - 创建排班
- `GET /api/attendance/schedule` - 查询排班

## 🧪 测试

### 单元测试

```bash
mvn test
```

### 集成测试

```bash
mvn verify
```

### 性能测试

```bash
mvn test -Dtest=AttendancePerformanceTest
```

## 📊 性能指标

- **响应时间**: P95 < 200ms
- **并发能力**: 支持1000+ TPS
- **可用性**: 99.9%

## 🔧 配置说明

### 应用配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ioedream_attendance
    username: root
    password: password
  redis:
    host: localhost
    port: 6379
```

### 缓存配置

- 考勤记录缓存: 1小时
- 排班信息缓存: 24小时
- 统计结果缓存: 6小时

## 📝 开发规范

- 遵循IOE-DREAM代码规范
- 使用统一的ResponseDTO响应格式
- 所有公共方法必须有JavaDoc注释
- 异常处理使用SmartException

## 🤝 贡献指南

1. Fork项目
2. 创建特性分支
3. 提交更改
4. 推送到分支
5. 创建Pull Request

## 📄 许可证

Copyright © 2025 IOE-DREAM Team

