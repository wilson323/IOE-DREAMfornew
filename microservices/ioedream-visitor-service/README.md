# IOE-DREAM 访客服务 (Visitor Service)

## 📋 服务概述

访客服务是IOE-DREAM平台的核心业务服务之一，负责访客管理、访客预约、访客审批、访客统计等功能。

### 核心功能

- ✅ **访客登记**: 访客信息登记和管理
- ✅ **访客预约**: 访客预约申请和审批
- ✅ **审批流程**: 完整的审批工作流
- ✅ **访客统计**: 访客数据统计和分析
- ✅ **权限管理**: 访客权限配置和验证
- ✅ **通知提醒**: 访客通知和提醒功能

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
ioedream-visitor-service/
├── controller/          # 控制器层
├── service/             # 服务层
│   └── impl/          # 服务实现
├── manager/            # 业务管理层
├── dao/                # 数据访问层
└── domain/             # 领域模型
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
   CREATE DATABASE ioedream_visitor DEFAULT CHARACTER SET utf8mb4;
   ```

2. **启动服务**
   ```bash
   mvn spring-boot:run
   ```

3. **访问API文档**
   - 地址: http://localhost:8084/doc.html
   - 默认端口: 8084

## 📡 API接口

### 访客管理

- `POST /api/visitor/create` - 创建访客
- `GET /api/visitor/{id}` - 查询访客详情
- `PUT /api/visitor/{id}` - 更新访客信息

### 访客预约

- `POST /api/visitor/appointment` - 创建预约
- `GET /api/visitor/appointment` - 查询预约列表

### 审批流程

- `POST /api/visitor/approve` - 审批通过
- `POST /api/visitor/reject` - 审批拒绝

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
mvn test -Dtest=VisitorPerformanceTest
```

## 📊 性能指标

- **响应时间**: P95 < 500ms
- **并发能力**: 支持500+ TPS
- **查询性能**: 支持复杂查询优化

## 🔧 配置说明

### 审批流程配置

```yaml
visitor:
  approval:
    auto-approve: false
    timeout: 24h
```

## 📝 开发规范

- 遵循IOE-DREAM代码规范
- 审批流程必须有完整的状态管理
- 访客信息必须有数据验证
- 所有操作必须有日志记录

## 🤝 贡献指南

1. Fork项目
2. 创建特性分支
3. 提交更改
4. 推送到分支
5. 创建Pull Request

## 📄 许可证

Copyright © 2025 IOE-DREAM Team

