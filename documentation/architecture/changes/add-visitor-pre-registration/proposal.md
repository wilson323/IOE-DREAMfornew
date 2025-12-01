# Change: 智能访客预约通行能力

## Why
- 访客管理依赖人工登记，缺乏提前审核导致门禁放行效率低、排队严重。
- 访客凭证仅支持现场临时卡，无法在闸机实现自助核验，存在安保风险。
- 门禁、访客、消息模块之间没有统一的数据流，访客状态很难追踪与审计。

## What Changes
- **访客预约后台能力**：在 smart-access 模块新增访客预约应用服务，提供访客申请、内部审批、预约日程管理、状态流转。
- **凭证生成与下发**：生成一次性访客二维码凭证（含有效期/区域/设备列表），推送至访客邮箱/短信并同步门禁设备白名单。
- **通行核验链路**：闸机扫码后调用访客核验接口，校验凭证、访问时间与审批状态，自动写入通行记录并触发通知。
- **前端交互**：在 admin 前端提供访客预约页面、审批面板与通行记录视图，支持导出与提醒。

## Impact
- **Affected specs**: `smart-access`
- **Affected repos**:
  - `smart-admin-api-java17-springboot3/sa-admin`：新增访客预约 Controller、Service、Manager 与持久层。
  - `smart-admin-api-java17-springboot3/sa-base`：新增访客凭证领域模型、二维码生成与Redis缓存封装。
  - `smart-admin-web-javascript`：新增访客预约与审批页面、二维码展示组件。
  - `数据库SQL脚本/mysql`：新增访客预约、凭证、通行日志扩展表及索引。
