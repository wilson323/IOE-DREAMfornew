# 统计分析子模块

## 1. 功能说明

### 1.1 访客统计
- 访客数量统计（日/周/月/年）
- 访客类型分布
- 访客来源分析
- 高峰时段分析

### 1.2 预约统计
- 预约数量统计
- 预约审批率
- 预约履约率
- 预约取消分析

### 1.3 通行统计
- 通行次数统计
- 区域流量统计
- 平均停留时长
- 通行方式分布

### 1.4 异常统计
- 异常类型分布
- 异常处理时效
- 异常趋势分析
- 黑名单统计

### 1.5 报表生成
- 日报/周报/月报
- 自定义报表
- 报表导出（Excel/PDF）
- 报表定时推送

## 2. 用户故事

### US-VIS-STAT-001: 访客统计报表
**作为** 管理人员  
**我希望** 查看访客统计报表  
**以便** 了解访客接待情况

**验收标准:**
- 支持按时间范围查询
- 支持多维度统计
- 支持图表展示
- 支持导出报表

### US-VIS-STAT-002: 趋势分析
**作为** 管理人员  
**我希望** 查看访客趋势分析  
**以便** 预测访客流量

### US-VIS-STAT-003: 定时报表
**作为** 管理人员  
**我希望** 定时收到访客报表  
**以便** 及时掌握访客动态

## 3. 数据库设计

### vis_statistics_daily 日统计表
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键 |
| stat_date | DATE | 统计日期 |
| total_visitors | INT | 访客总数 |
| new_visitors | INT | 新访客数 |
| returning_visitors | INT | 回访访客数 |
| reservation_count | INT | 预约数 |
| walk_in_count | INT | 临时访客数 |
| vip_count | INT | VIP访客数 |
| check_in_count | INT | 签到数 |
| check_out_count | INT | 签出数 |
| avg_stay_duration | INT | 平均停留时长(分钟) |
| exception_count | INT | 异常数 |
| create_time | DATETIME | 创建时间 |

### vis_statistics_area 区域统计表
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键 |
| stat_date | DATE | 统计日期 |
| area_id | BIGINT | 区域ID |
| area_name | VARCHAR(100) | 区域名称 |
| visitor_count | INT | 访客数 |
| access_count | INT | 通行次数 |
| peak_count | INT | 峰值人数 |
| peak_time | TIME | 峰值时间 |
| create_time | DATETIME | 创建时间 |

### vis_report_config 报表配置表
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键 |
| report_name | VARCHAR(100) | 报表名称 |
| report_type | VARCHAR(20) | 报表类型 |
| schedule_type | VARCHAR(20) | 调度类型 |
| schedule_time | VARCHAR(50) | 调度时间 |
| recipients | JSON | 接收人列表 |
| template_id | BIGINT | 模板ID |
| enabled | TINYINT | 是否启用 |
| create_time | DATETIME | 创建时间 |

## 4. API接口

### 4.1 获取访客统计
```http
POST /api/v1/visitor/statistics/visitor
```

**请求参数:**
```json
{
  "startDate": "2024-01-01",
  "endDate": "2024-01-31",
  "dimension": "DAY"
}
```

**响应:**
```json
{
  "code": 200,
  "data": {
    "totalVisitors": 1500,
    "newVisitors": 800,
    "returningVisitors": 700,
    "trend": [
      {"date": "2024-01-01", "count": 50},
      {"date": "2024-01-02", "count": 65}
    ]
  }
}
```

### 4.2 获取区域统计
```http
POST /api/v1/visitor/statistics/area
```

### 4.3 获取预约统计
```http
POST /api/v1/visitor/statistics/reservation
```

### 4.4 获取异常统计
```http
POST /api/v1/visitor/statistics/exception
```

### 4.5 导出报表
```http
POST /api/v1/visitor/report/export
```

### 4.6 配置定时报表
```http
POST /api/v1/visitor/report/schedule/config
```

## 5. 统计维度

### 5.1 时间维度
- 按小时统计
- 按日统计
- 按周统计
- 按月统计
- 按年统计

### 5.2 访客维度
- 按访客类型（VIP/普通/临时）
- 按访客来源（公司/地区）
- 按来访目的
- 按被访部门

### 5.3 区域维度
- 按楼栋
- 按楼层
- 按区域类型

## 6. 报表类型

| 报表类型 | 内容 | 周期 |
|----------|------|------|
| 日报 | 当日访客概况、异常事件 | 每日 |
| 周报 | 本周访客趋势、对比分析 | 每周 |
| 月报 | 月度汇总、同比环比 | 每月 |
| 异常报表 | 异常事件统计、处理情况 | 按需 |
| 区域报表 | 区域流量、容量分析 | 按需 |

## 7. 数据可视化

### 7.1 图表类型
- 折线图：访客趋势
- 柱状图：区域对比
- 饼图：类型分布
- 热力图：时段分布
- 地图：区域分布

### 7.2 大屏展示
- 实时访客数
- 今日统计
- 趋势图表
- 告警列表
- 区域热力图

