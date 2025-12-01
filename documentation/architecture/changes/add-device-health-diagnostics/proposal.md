# Change: 设备健康诊断与预测维护

## Why
- 门禁、考勤等设备缺少统一健康评分，运维只能靠心跳日志排查，导致定位故障耗时。
- 设备异常通常在完全离线后才告警，缺乏趋势分析与预警，造成业务中断。
- 缺少维护建议与历史诊断记录，无法量化供应商质量及计划巡检。

## What Changes
- **健康评分模型**：汇总心跳、CPU/温度、指令成功率、告警频次等指标，生成 0-100 设备健康分并存储历史快照。
- **异常预警服务**：为指标设置阈值与波动策略，检测趋势后推送智能告警（站内+短信），并同步至门禁/监控告警中心。
- **维护建议引擎**：结合健康分与最近工单，生成待巡检清单、自动安排维护任务，并在前端提供实时健康面板与趋势图。

## Impact
- **Affected specs**: `smart-device`
- **Affected repos**:
  - `smart-admin-api-java17-springboot3/sa-admin`: 新增健康诊断 Controller、Service、Manager、定时任务、告警推送集成。
  - `smart-admin-api-java17-springboot3/sa-base`: 新增指标聚合组件、健康评分算法、Redis/L1 缓存封装。
  - `smart-admin-web-javascript`: 新增设备健康看板、趋势图、维护建议 UI，扩展设备详情页面。
  - `数据库SQL脚本/mysql`: 新增 `t_device_health_snapshot`、`t_device_maintenance_plan` 等数据表及索引。
