## Design: 设备健康诊断与预测维护

### 1. 审批准备
- 已复核 `proposal.md` / `tasks.md` / delta spec，范围与依赖均落在 smart-device 能力，可直接提交评审。
- `openspec validate add-device-health-diagnostics --strict` 通过（2025-XX-XX，本地执行记录在 CLI）。
- 无其它变更冲突：`openspec list` 中 smart-device 相关提案只有本项，不会与 area/permission 等任务交叉。

### 2. 指标建模与数据来源（Task 1.1）
| 指标 | 来源 | 采样频率 | 默认权重 | 说明 |
| --- | --- | --- | --- | --- |
| `heartbeat_latency_ms` | 设备心跳日志（sa-base 心跳监听） | 1 分钟 | 0.20 | 超阈值触发扣分，离线记 0 分 |
| `cpu_usage_pct` | 设备 SNMP/Agent 上报 | 5 分钟 | 0.15 | >85% 连续 3 次扣分 |
| `temperature_celsius` | 设备传感器 | 5 分钟 | 0.10 | 超过额定温度 5℃ 扣分 |
| `command_success_ratio` | 指令执行日志 | 5 分钟 | 0.25 | <98% 扣分，<90% 触发告警 |
| `alarm_count` | 设备告警表 | 5 分钟 | 0.15 | 每条异常扣固定分 |
| `uptime_hours` | 心跳累计 | 30 分钟 | 0.05 | 连续稳定运行加分 |
| `maintenance_delay_days` | 运维计划表 | 1 天 | 0.10 | 超期待维护扣分 |

> 评分函数示例：`score = Σ(weight_i * normalized(metric_i)) * 100`。归一化策略按指标上下限映射 0-1。

### 3. 表结构草案（Task 1.2）
```sql
CREATE TABLE t_device_health_snapshot (
    snapshot_id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    device_id             BIGINT NOT NULL,
    score_value           DECIMAL(5,2) NOT NULL,
    level_code            VARCHAR(20) NOT NULL COMMENT 'healthy/warning/critical',
    heartbeat_latency_ms  INT,
    cpu_usage_pct         DECIMAL(5,2),
    temperature_celsius   DECIMAL(5,2),
    command_success_ratio DECIMAL(5,2),
    alarm_count           INT,
    uptime_hours          DECIMAL(7,2),
    maintenance_delay_days DECIMAL(5,2),
    snapshot_time         DATETIME NOT NULL,
    create_time           DATETIME NOT NULL,
    update_time           DATETIME NOT NULL,
    create_user_id        BIGINT NOT NULL,
    deleted_flag          TINYINT DEFAULT 0,
    INDEX idx_device_time (device_id, snapshot_time)
);

CREATE TABLE t_device_health_metric (
    metric_id       BIGINT PRIMARY KEY AUTO_INCREMENT,
    device_id       BIGINT NOT NULL,
    metric_code     VARCHAR(50) NOT NULL,
    metric_value    DECIMAL(10,4) NOT NULL,
    metric_time     DATETIME NOT NULL,
    source_type     VARCHAR(20) NOT NULL,
    create_time     DATETIME NOT NULL,
    update_time     DATETIME NOT NULL,
    create_user_id  BIGINT NOT NULL,
    deleted_flag    TINYINT DEFAULT 0,
    INDEX idx_device_metric (device_id, metric_code, metric_time)
);

CREATE TABLE t_device_maintenance_plan (
    plan_id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    device_id         BIGINT NOT NULL,
    plan_status       VARCHAR(20) NOT NULL COMMENT 'pending,in-progress,done,cancelled',
    trigger_reason    VARCHAR(100) NOT NULL COMMENT 'low-score,trend-alert,manual',
    score_on_create   DECIMAL(5,2),
    assignee_id       BIGINT,
    schedule_start    DATETIME,
    schedule_end      DATETIME,
    result_note       VARCHAR(500),
    create_time       DATETIME NOT NULL,
    update_time       DATETIME NOT NULL,
    create_user_id    BIGINT NOT NULL,
    deleted_flag      TINYINT DEFAULT 0,
    INDEX idx_device_status (device_id, plan_status)
);
```

字段均包含审计列与 `deleted_flag`，符合项目数据库规范（`openspec/project.md:56`）。

### 4. 状态机与阈值（Task 1.3）
- **健康等级**：`score ≥ 85 -> healthy`；`60-84 -> warning`；`<60 -> critical`。
- **趋势判定**：最近 15 分钟内连续 3 次评分下降 ≥5 分触发“warning”，≥10 分触发“critical”。
- **维护状态机**：`pending → in-progress → done`；任意状态可转 `cancelled`，流转必须记录操作者与备注。
- **协作同步**：运维团队提供心跳/性能数据接入明细；设备团队确认指令执行日志字段与采集频率。

### 5. 集成校验计划
1. **后端检查点**（完成 Task 2.x 后执行）
   - 运行健康评分、告警、维护建议的单元/集成测试，覆盖率 ≥85%。
   - 使用 Testcontainers/Mock Redis 验证调度任务与缓存一致性。
   - 导出示例 `t_device_health_snapshot` 数据供前端联调。
2. **前端检查点**（完成 Task 3.x 前）
   - Vitest + Vue Test Utils 验证看板组件、详情页、维护流程。
   - 使用 MSW 模拟后端 API，跑主要用户路径的 e2e（Puppeteer/Playwright）。
   - 在联调环境复测评分可视化与告警联动，确保 UI/接口契合。

### 6. 后续步骤
1. 上述内容可作为审批材料提交，待负责人确认后进入实现阶段。
2. 审批通过即刻开始 Task 1.x，同步运维/设备团队落实数据接入；数据库脚本落地到 `数据库SQL脚本/mysql/`.
3. 按集成校验计划安排后台/前端两个验证里程碑，确保交付前满足覆盖率与 UI 要求。
