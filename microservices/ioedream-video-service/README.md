# IOE-DREAM 视频服务 (Video Service)

## 📋 服务概述

视频服务是IOE-DREAM平台的核心业务服务之一，负责视频监控、视频流处理、AI分析、设备管理等功能。

### 核心功能

- ✅ **视频监控**: 实时视频监控和回放
- ✅ **视频流处理**: 视频流采集、转码、存储
- ✅ **AI分析**: 智能视频分析和识别
- ✅ **设备管理**: 视频设备管理和配置
- ✅ **告警管理**: 视频异常告警和处理
- ✅ **存储管理**: 视频存储和检索

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
ioedream-video-service/
├── controller/          # 控制器层
├── service/             # 服务层
│   ├── impl/          # 服务实现
│   ├── analytics/     # AI分析服务
│   └── stream/        # 视频流处理
├── dao/                # 数据访问层
├── domain/             # 领域模型
└── engine/             # AI分析引擎
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
   CREATE DATABASE ioedream_video DEFAULT CHARACTER SET utf8mb4;
   ```

2. **配置视频设备**
   - 配置视频设备连接信息
   - 配置视频流地址

3. **启动服务**
   ```bash
   mvn spring-boot:run
   ```

4. **访问API文档**
   - 地址: http://localhost:8085/doc.html
   - 默认端口: 8085

## 📡 API接口

### 视频监控

- `GET /api/video/stream/{deviceId}` - 获取视频流
- `GET /api/video/playback` - 视频回放

### 设备管理

- `GET /api/video/device` - 查询设备列表
- `POST /api/video/device` - 添加设备
- `PUT /api/video/device/{id}` - 更新设备

### AI分析

- `POST /api/video/analyze` - 视频分析
- `GET /api/video/analysis/result` - 查询分析结果

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
mvn test -Dtest=VideoPerformanceTest
```

## 📊 性能指标

- **响应时间**: P95 < 1000ms
- **并发能力**: 支持100+视频流
- **AI分析**: 支持实时分析

## 🔧 配置说明

### 视频流配置

```yaml
video:
  stream:
    timeout: 30000
    buffer-size: 1024
  ai:
    enabled: true
    model: default
```

## 📝 开发规范

- 遵循IOE-DREAM代码规范
- 视频流处理必须有异常处理
- AI分析结果必须有缓存
- 设备操作必须有日志记录

## 🤝 贡献指南

1. Fork项目
2. 创建特性分支
3. 提交更改
4. 推送到分支
5. 创建Pull Request

## 📄 许可证

Copyright © 2025 IOE-DREAM Team

