# IOE-DREAM 消费服务 (Consume Service)

## 📋 服务概述

消费服务是IOE-DREAM平台的核心业务服务之一，负责消费管理、账户管理、支付处理、消费统计等功能。

### 核心功能

- ✅ **消费支付**: 支持多种支付方式（账户余额、刷卡、扫码等）
- ✅ **账户管理**: 账户余额、充值、冻结、解冻
- ✅ **消费模式**: 支持多种消费模式（固定金额、自由金额、计量、点餐等）
- ✅ **权限管理**: 消费权限验证和限额管理
- ✅ **报表统计**: 消费报表生成和数据分析
- ✅ **分布式事务**: Saga分布式事务支持

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
ioedream-consume-service/
├── controller/          # 控制器层
├── service/             # 服务层
│   ├── impl/          # 服务实现
│   ├── engine/        # 消费引擎
│   └── ordering/      # 点餐服务
├── manager/            # 业务管理层
├── dao/                # 数据访问层
├── domain/             # 领域模型
└── engine/             # 消费模式引擎
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
   CREATE DATABASE ioedream_consume DEFAULT CHARACTER SET utf8mb4;
   ```

2. **启动服务**
   ```bash
   mvn spring-boot:run
   ```

3. **访问API文档**
   - 地址: http://localhost:8083/doc.html
   - 默认端口: 8083

## 📡 API接口

### 消费支付

- `POST /api/consume/pay` - 执行消费支付
- `POST /api/consume/refund` - 退款处理

### 账户管理

- `GET /api/consume/account/balance` - 查询账户余额
- `POST /api/consume/account/recharge` - 账户充值
- `POST /api/consume/account/freeze` - 冻结账户

### 消费查询

- `GET /api/consume/records` - 查询消费记录
- `GET /api/consume/statistics` - 消费统计

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
mvn test -Dtest=ConsumePerformanceTest
```

## 📊 性能指标

- **响应时间**: P95 < 500ms
- **并发能力**: 支持1000+ TPS
- **事务处理**: 支持分布式事务

## 🔧 配置说明

### 消费模式配置

```yaml
consume:
  mode:
    fixed-amount:
      enabled: true
    free-amount:
      enabled: true
    metering:
      enabled: true
```

## 📝 开发规范

- 遵循IOE-DREAM代码规范
- 所有支付操作必须有事务保护
- 账户操作必须有权限验证
- 消费记录必须完整记录

## 🤝 贡献指南

1. Fork项目
2. 创建特性分支
3. 提交更改
4. 推送到分支
5. 创建Pull Request

## 📄 许可证

Copyright © 2025 IOE-DREAM Team

