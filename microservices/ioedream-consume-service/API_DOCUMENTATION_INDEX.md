# IOE-DREAM 消费服务 API 文档索引

## 📋 概述

本文档描述了 IOE-DREAM 智能管理系统中消费服务（ioedream-consume-service）的完整 API 接口规范。

**服务信息**:
- **服务名称**: ioedream-consume-service
- **服务端口**: 8094
- **API版本**: v1
- **基础路径**: `/api/v1/consume`
- **文档版本**: 1.0.0
- **最后更新**: 2025-12-22

## 🔗 API 文档结构

### 1. 核心业务模块

| 模块 | 文档链接 | 描述 |
|------|---------|------|
| **产品管理** | [产品API文档](#产品管理-api) | 产品CRUD、价格管理、库存管理 |
| **消费记录** | [消费记录API文档](#消费记录-api) | 消费记录查询、创建、统计分析 |
| **补贴管理** | [补贴API文档](#补贴管理-api) | 补贴发放、使用、统计 |
| **餐次分类** | [餐次分类API文档](#餐次分类-api) | 分类管理、树结构构建 |
| **设备管理** | [设备API文档](#设备管理-api) | 设备监控、状态管理、配置 |
| **充值记录** | [充值记录API文档](#充值记录-api) | 充值记录、统计分析、对账 |

### 2. 支撑功能模块

| 模块 | 文档链接 | 描述 |
|------|---------|------|
| **统计分析** | [统计分析API文档](#统计分析-api) | 综合统计、报表生成、数据可视化 |
| **数据导入导出** | [数据导入导出API文档](#数据导入导出-api) | 批量导入、数据导出、模板下载 |
| **配置管理** | [配置管理API文档](#配置管理-api) | 系统配置、业务参数、开关管理 |

## 📚 API 详细文档

### 产品管理 API

#### 产品信息管理

```http
GET    /api/v1/consume/products                    # 获取产品列表
GET    /api/v1/consume/products/{id}               # 获取产品详情
POST   /api/v1/consume/products                    # 创建产品
PUT    /api/v1/consume/products/{id}               # 更新产品
DELETE /api/v1/consume/products/{id}               # 删除产品

GET    /api/v1/consume/products/categories         # 获取产品分类列表
GET    /api/v1/consume/products/search              # 产品搜索
GET    /api/v1/consume/products/recommended         # 获取推荐产品
GET    /api/v1/consume/products/low-stock           # 获取低库存产品
```

#### 产品库存管理

```http
PUT    /api/v1/consume/products/{id}/stock           # 更新产品库存
POST   /api/v1/consume/products/batch-stock-update    # 批量更新库存
GET    /api/v1/consume/products/{id}/stock-history   # 获取库存变更历史
```

#### 产品价格管理

```http
PUT    /api/v1/consume/products/{id}/price           # 更新产品价格
POST   /api/v1/consume/products/batch-price-adjust   # 批量调整价格
GET    /api/v1/consume/products/{id}/price-history   # 获取价格变更历史
```

### 消费记录 API

#### 消费记录管理

```http
GET    /api/v1/consume/records                       # 获取消费记录列表
GET    /api/v1/consume/records/{id}                  # 获取消费记录详情
POST   /api/v1/consume/records                       # 创建消费记录
PUT    /api/v1/consume/records/{id}/status           # 更新消费记录状态
DELETE /api/v1/consume/records/{id}                  # 删除消费记录
```

#### 消费统计分析

```http
GET    /api/v1/consume/records/statistics            # 消费统计
GET    /api/v1/consume/records/daily-summary         # 每日消费汇总
GET    /api/v1/consume/records/monthly-summary        # 每月消费汇总
GET    /api/v1/consume/records/product-summary        # 产品消费统计
GET    /api/v1/consume/records/user-summary          # 用户消费统计
```

### 补贴管理 API

#### 补贴信息管理

```http
GET    /api/v1/consume/subsidies                     # 获取补贴列表
GET    /api/v1/consume/subsidies/{id}                # 获取补贴详情
POST   /api/v1/consume/subsidies                     # 创建补贴
PUT    /api/v1/consume/subsidies/{id}                # 更新补贴
DELETE /api/v1/consume/subsidies/{id}                # 删除补贴

GET    /api/v1/consume/subsidies/users/{userId}      # 获取用户补贴列表
GET    /api/v1/consume/subsidies/expiring-soon        # 获取即将过期补贴
GET    /api/v1/consume/subsidies/nearly-depleted     # 获取即将用完补贴
```

#### 补贴发放与使用

```http
POST   /api/v1/consume/subsidies/{id}/issue          # 发放补贴
POST   /api/v1/consume/subsidies/{id}/use            # 使用补贴
POST   /api/v1/consume/subsidies/batch-issue         # 批量发放补贴
POST   /api/v1/consume/subsidies/cancel              # 作废补贴
```

#### 补贴统计分析

```http
GET    /api/v1/consume/subsidies/statistics         # 补贴统计
GET    /api/v1/consume/subsidies/usage-analysis      # 使用情况分析
GET    /api/v1/consume/subsidies/balance-analysis     # 余额分析
```

### 餐次分类 API

#### 分类信息管理

```http
GET    /api/v1/consume/meal-categories              # 获取餐次分类列表
GET    /api/v1/consume/meal-categories/tree          # 获取分类树结构
GET    /api/v1/consume/meal-categories/{id}         # 获取分类详情
POST   /api/v1/consume/meal-categories              # 创建餐次分类
PUT    /api/v1/consume/meal-categories/{id}         # 更新餐次分类
DELETE /api/v1/consume/meal-categories/{id}         # 删除餐次分类

GET    /api/v1/consume/meal-categories/{id}/children # 获取子分类列表
GET    /api/v1/consume/meal-categories/available      # 获取可用分类
```

### 设备管理 API

#### 设备信息管理

```http
GET    /api/v1/consume/devices                       # 获取设备列表
GET    /api/v1/consume/devices/{id}                  # 获取设备详情
POST   /api/v1/consume/devices                       # 创建设备
PUT    /api/v1/consume/devices/{id}                  # 更新设备
DELETE /api/v1/consume/devices/{id}                  # 删除设备

GET    /api/v1/consume/devices/online                 # 获取在线设备
GET    /api/v1/consume/devices/offline                # 获取离线设备
GET    /api/v1/consume/devices/fault                  # 获取故障设备
```

#### 设备状态管理

```http
PUT    /api/v1/consume/devices/{id}/status           # 更新设备状态
PUT    /api/v1/consume/devices/{id}/configuration    # 更新设备配置
POST   /api/v1/consume/devices/{id}/heartbeat        # 设备心跳
GET    /api/v1/consume/devices/{id}/health            # 获取设备健康状态
```

#### 设备通信管理

```http
POST   /api/v1/consume/devices/{id}/command           # 发送设备命令
GET    /api/v1/consume/devices/{id}/communication-log # 获取通信日志
GET    /api/v1/consume/devices/{id}/status-history    # 获取状态变更历史
```

### 充值记录 API

#### 充值记录管理

```http
GET    /api/v1/consume/recharges                      # 获取充值记录列表
GET    /api/v1/consume/recharges/{id}                 # 获取充值记录详情
POST   /api/v1/consume/recharges                      # 创建充值记录
PUT    /api/v1/consume/recharges/{id}/status          # 更新充值状态
DELETE /api/v1/consume/recharges/{id}                 # 删除充值记录

GET    /api/v1/consume/recharges/users/{userId}      # 获取用户充值记录
GET    /api/v1/consume/recharges/pending              # 获取待审核充值
GET    /api/v1/consume/recharges/success              # 获取成功充值
```

#### 充值统计分析

```http
GET    /api/v1/consume/recharges/statistics          # 充值统计
GET    /api/v1/consume/recharges/daily-summary       # 每日充值汇总
GET    /api/v1/consume/recharges/monthly-summary      # 每月充值汇总
GET    /api/v1/consume/recharges/payment-method-summary # 支付方式统计
```

#### 充值审核管理

```http
POST   /api/v1/consume/recharges/{id}/approve          # 审核通过
POST   /api/v1/consume/recharges/{id}/reject           # 审核驳回
POST   /api/v1/consume/recharges/batch-approve         # 批量审核
GET    /api/v1/consume/recharges/audit-log            # 获取审核日志
```

### 统计分析 API

#### 综合统计

```http
GET    /api/v1/consume/statistics/overview            # 系统概览统计
GET    /api/v1/consume/statistics/consumption         # 消费分析统计
GET    /api/v1/consume/statistics/revenue             # 收入分析统计
GET    /api/v1/consume/statistics/user-behavior       # 用户行为分析
```

#### 报表生成

```http
POST   /api/v1/consume/reports/daily-consumption      # 生成每日消费报表
POST   /api/v1/consume/reports/monthly-summary        # 生成月度汇总报表
POST   /api/v1/consume/reports/product-analysis      # 生成产品分析报表
POST   /api/v1/consume/reports/user-analysis         # 生成用户分析报表
GET    /api/v1/consume/reports/{reportId}/download    # 下载报表
```

#### 数据可视化

```http
GET    /api/v1/consume/charts/consumption-trend       # 消费趋势图表
GET    /api/v1/consume/charts/product-distribution   # 产品分布图表
GET    /api/v1/consume/charts/time-distribution      # 时间分布图表
GET    /api/v1/consume/charts/user-segmentation      # 用户分层图表
```

### 数据导入导出 API

#### 数据导入

```http
POST   /api/v1/consume/import/products                # 导入产品数据
POST   /api/v1/consume/import/users                   # 导入用户数据
POST   /api/v1/consume/import/subsidies               # 导入补贴数据
GET    /api/v1/consume/import/templates/{type}       # 下载导入模板
GET    /api/v1/consume/import/history                 # 获取导入历史
```

#### 数据导出

```http
POST   /api/v1/consume/export/products                # 导出产品数据
POST   /api/v1/consume/export/consume-records          # 导出消费记录
POST   /api/v1/consume/export/subsidies               # 导出补贴数据
POST   /api/v1/consume/export/statistics              # 导出统计数据
GET    /api/v1/consume/export/{taskId}/status         # 获取导出任务状态
GET    /api/v1/consume/export/{taskId}/download        # 下载导出文件
```

### 配置管理 API

#### 系统配置

```http
GET    /api/v1/consume/config/system                 # 获取系统配置
PUT    /api/v1/consume/config/system                 # 更新系统配置
GET    /api/v1/consume/config/business               # 获取业务配置
PUT    /api/v1/consume/config/business               # 更新业务配置
```

#### 开关管理

```http
GET    /api/v1/consume/config/switches                # 获取功能开关列表
GET    /api/v1/consume/config/switches/{key}         # 获取开关状态
PUT    /api/v1/consume/config/switches/{key}         # 更新开关状态
POST   /api/v1/consume/config/switches/batch-update   # 批量更新开关
```

## 📊 API 响应格式

### 统一响应结构

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": "2025-12-22T10:00:00"
}
```

### 分页响应结构

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "list": [],
    "total": 100,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 5
  },
  "timestamp": "2025-12-22T10:00:00"
}
```

### 错误响应结构

```json
{
  "code": 400,
  "message": "参数错误",
  "error": {
    "field": "productName",
    "message": "产品名称不能为空"
  },
  "timestamp": "2025-12-22T10:00:00"
}
```

## 🔐 认证与授权

### 认证方式

- **Bearer Token**: 在请求头中添加 `Authorization: Bearer {token}`
- **API Key**: 在请求头中添加 `X-API-Key: {api_key}`

### 权限级别

| 权限级别 | 描述 | 可访问API |
|---------|------|----------|
| **READ** | 只读权限 | 查询类API |
| **WRITE** | 读写权限 | 增删改查API |
| **ADMIN** | 管理员权限 | 所有API |

## 📝 使用示例

### JavaScript/TypeScript

```javascript
// 获取产品列表
const response = await fetch('/api/v1/consume/products', {
  method: 'GET',
  headers: {
    'Authorization': 'Bearer ' + token,
    'Content-Type': 'application/json'
  }
});

const result = await response.json();
console.log(result.data.list);
```

### cURL

```bash
# 创建产品
curl -X POST "http://localhost:8094/api/v1/consume/products" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "productName": "营养早餐",
    "productCode": "BREAKFAST_001",
    "salePrice": 15.00,
    "productCategory": 1
  }'
```

### Postman

导入以下环境变量：
- `BASE_URL`: `http://localhost:8094`
- `TOKEN`: 您的访问令牌

然后使用预置的API请求集合。

## 🚨 错误码说明

| 错误码范围 | 类型 | 说明 |
|-----------|------|------|
| 200 | 成功 | 操作成功 |
| 400-499 | 客户端错误 | 参数错误、认证失败、权限不足 |
| 500-599 | 服务端错误 | 服务器内部错误 |
| 1000-1999 | 业务通用错误 | 数据不存在、重复操作等 |
| 4000-4999 | 消费模块错误 | 产品、消费、补贴相关错误 |

## 📞 技术支持

- **API文档维护**: IOE-DREAM 开发团队
- **技术支持邮箱**: support@ioe-dream.com
- **问题反馈**: [GitHub Issues](https://github.com/ioe-dream/issues)

## 📅 更新日志

| 版本 | 日期 | 更新内容 |
|------|------|----------|
| 1.0.0 | 2025-12-22 | 初始版本，完整API规范 |

---

*本文档遵循 IOE-DREAM 项目技术规范，如有疑问请联系开发团队。*