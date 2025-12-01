# IOE-DREAM 微服务依赖关系分析

## 📋 分析时间
**生成时间**: 2025-01-30  
**分析范围**: 所有微服务间的调用关系

---

## 🏗️ 服务依赖关系图

```
┌─────────────────────────────────────────────────────────────┐
│                    API Gateway (8080)                        │
│              ioedream-gateway-service                        │
└──────────────────────┬──────────────────────────────────────┘
                       │
        ┌──────────────┼──────────────┐
        │              │              │
        ▼              ▼              ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│ Auth Service │ │Identity Svc  │ │Config Svc   │
│    (8081)    │ │   (8082)     │ │  (8888)     │
└──────┬───────┘ └──────────────┘ └──────────────┘
       │
       │ (认证调用)
       │
       ▼
┌─────────────────────────────────────────────────────────────┐
│                    业务服务层                                │
├─────────────────────────────────────────────────────────────┤
│  Access Service (8085)  │  Consume Service (8086)          │
│  Attendance Service(8087)│  Visitor Service (8089)          │
│  Device Service (8083)   │  Video Service (8088)           │
└─────────────────────────────────────────────────────────────┘
       │
       │ (设备调用)
       │
       ▼
┌─────────────────────────────────────────────────────────────┐
│                    支撑服务层                                │
├─────────────────────────────────────────────────────────────┤
│  Notification Service (8090)  │  Report Service (8092)      │
│  Audit Service (8085)         │  Monitor Service           │
│  Scheduler Service (8087)      │  Integration Service(8088) │
│  Infrastructure Service(8089)  │  System Service            │
│  Enterprise Service            │  OA Service                │
└─────────────────────────────────────────────────────────────┘
```

---

## 📊 服务依赖详情

### 1. 网关层依赖

**ioedream-gateway-service** (8080)
- **依赖**: 所有微服务（通过路由转发）
- **职责**: 统一入口、路由转发、负载均衡、限流熔断

### 2. 基础设施层依赖

#### ioedream-auth-service (8081)
- **被依赖**: 
  - gateway-service (路由转发)
  - consume-service (通过FeignClient调用)
- **依赖**: 
  - identity-service (用户信息查询)
  - redis (缓存token)

#### ioedream-identity-service (8082)
- **被依赖**: 
  - gateway-service (路由转发)
  - auth-service (用户信息查询)
- **依赖**: 
  - redis (缓存用户信息)
  - mysql (用户数据存储)

#### ioedream-config-service (8888)
- **被依赖**: 所有微服务（配置管理）
- **依赖**: nacos (配置存储)

### 3. 业务服务层依赖

#### ioedream-consume-service (8086)
- **被依赖**: gateway-service
- **依赖**: 
  - auth-service (通过FeignClient认证)
  - device-service (通过FeignClient设备操作)
  - redis (缓存)
  - mysql (数据存储)

#### ioedream-access-service (8085)
- **被依赖**: gateway-service
- **依赖**: 
  - identity-service (用户权限验证)
  - device-service (设备控制)
  - redis (缓存)

#### ioedream-attendance-service (8087)
- **被依赖**: gateway-service
- **依赖**: 
  - identity-service (员工信息)
  - device-service (考勤设备)
  - redis (缓存)

#### ioedream-device-service (8083)
- **被依赖**: 
  - gateway-service
  - consume-service (通过FeignClient)
  - access-service
  - attendance-service
- **依赖**: 
  - redis (设备状态缓存)
  - mysql (设备数据存储)

### 4. 支撑服务层依赖

#### ioedream-notification-service (8090)
- **被依赖**: 所有业务服务（通知发送）
- **依赖**: 
  - redis (消息队列)
  - mysql (通知记录)

#### ioedream-audit-service (8085)
- **被依赖**: 所有业务服务（操作审计）
- **依赖**: 
  - mysql (审计日志存储)

#### ioedream-report-service (8092)
- **被依赖**: 所有业务服务（报表生成）
- **依赖**: 
  - 所有业务服务（数据聚合）
  - mysql (报表数据存储)

---

## 🔍 发现的依赖问题

### 1. 循环依赖风险 ⚠️
- **consume-service** ↔ **device-service**: 存在相互调用风险
- **建议**: 通过消息队列解耦

### 2. 直接依赖过多 ⚠️
- **consume-service** 直接依赖多个服务
- **建议**: 考虑通过网关统一调用

### 3. 服务职责重叠 ⚠️
- **enterprise-service** 与 **oa-service** 存在功能重叠
- **建议**: 明确服务边界

---

## 📝 优化建议

### 高优先级
1. **解耦循环依赖**
   - 使用消息队列替代直接调用
   - 引入事件驱动架构

2. **统一服务调用**
   - 所有服务间调用通过网关
   - 使用FeignClient统一管理

3. **明确服务边界**
   - 梳理enterprise-service和oa-service的职责
   - 避免功能重复

### 中优先级
4. **依赖关系可视化**
   - 生成依赖关系图
   - 定期检查依赖变化

5. **服务治理**
   - 实现服务降级
   - 添加熔断机制

---

## 🔧 依赖管理工具

### 当前使用
- **FeignClient**: 服务间HTTP调用
- **Nacos**: 服务注册发现
- **Gateway**: 统一入口

### 建议引入
- **消息队列**: 解耦服务调用
- **服务网格**: 统一服务治理
- **链路追踪**: 监控服务调用

---

**分析完成**: 2025-01-30  
**下次更新**: 依赖关系变化后

