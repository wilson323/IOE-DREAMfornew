# IOE-DREAM 门禁服务 (Access Service)

## 📋 服务概述

门禁服务是IOE-DREAM平台的核心业务服务之一，负责门禁设备管理、生物识别、门禁监控等功能。

### 核心功能

- ✅ **设备管理**: 支持多种门禁设备（大华、海康威视等）
- ✅ **生物识别**: 人脸识别、指纹识别、虹膜识别
- ✅ **门禁监控**: 实时监控门禁状态和异常告警
- ✅ **权限管理**: 门禁权限配置和验证
- ✅ **设备适配器**: 统一的设备适配器接口
- ✅ **监控告警**: 设备故障和异常告警

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
ioedream-access-service/
├── controller/          # 控制器层
├── service/             # 服务层
│   ├── impl/          # 服务实现
│   └── biometric/     # 生物识别服务
├── adapter/            # 设备适配器
│   ├── dahua/         # 大华设备适配器
│   └── hikvision/     # 海康威视适配器
├── dao/                # 数据访问层
├── domain/             # 领域模型
└── monitor/            # 监控服务
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
   CREATE DATABASE ioedream_access DEFAULT CHARACTER SET utf8mb4;
   ```

2. **配置设备连接**
   - 配置大华设备连接信息
   - 配置海康威视设备连接信息

3. **启动服务**
   ```bash
   mvn spring-boot:run
   ```

4. **访问API文档**
   - 地址: http://localhost:8082/doc.html
   - 默认端口: 8082

## 📡 API接口

### 门禁验证

- `POST /api/access/verify` - 门禁验证
- `POST /api/access/open` - 远程开门

### 设备管理

- `GET /api/access/device` - 查询设备列表
- `POST /api/access/device` - 添加设备
- `PUT /api/access/device/{id}` - 更新设备

### 生物识别

- `POST /api/access/biometric/register` - 注册生物特征
- `POST /api/access/biometric/verify` - 生物特征验证

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
mvn test -Dtest=AccessPerformanceTest
```

## 📊 性能指标

- **响应时间**: P95 < 300ms
- **并发能力**: 支持500+ TPS
- **设备连接**: 支持100+设备同时连接

## 🔧 配置说明

### 设备配置

```yaml
access:
  device:
    dahua:
      timeout: 5000
      retry: 3
    hikvision:
      timeout: 5000
      retry: 3
```

## 📝 开发规范

- 遵循IOE-DREAM代码规范
- 设备适配器必须实现统一接口
- 所有设备操作必须有异常处理
- 监控告警必须记录日志

## 🤝 贡献指南

1. Fork项目
2. 创建特性分支
3. 提交更改
4. 推送到分支
5. 创建Pull Request

## 📄 许可证

Copyright © 2025 IOE-DREAM Team

