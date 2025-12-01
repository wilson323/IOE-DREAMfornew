# SmartAdmin 角色-资源权限矩阵

> 版本: v1.0.0  
> 责任人: 安全委员会  
> 最后更新: 2025-11-12

## 1. 角色定义
| 角色编码 | 角色名称 | 说明 | 数据域 |
| --- | --- | --- | --- |
| ROLE_SUPER | 超级管理员 | 管理全局配置、最高权限 | 全部 |
| ROLE_OPS | 运维管理员 | 负责设备、告警、监控 | 区域/设备权限 |
| ROLE_BUSINESS | 业务管理员 | 管理人员、考勤、消费策略 | 区域/组织 |
| ROLE_AUDITOR | 审计员 | 查看日志、告警、报表 | 只读 |
| ROLE_GUEST | 访客 | 限制访问, 仅可查看授权信息 | 自定义 |

## 2. 菜单与接口映射
| 菜单编码 | 菜单名称 | 所属模块 | 接口资源码 | 角色授权 |
| --- | --- | --- | --- | --- |
| MENU_SYS_ROLE | 角色管理 | 系统设置 | `system:role:*` | ROLE_SUPER |
| MENU_SYS_USER | 用户管理 | 系统设置 | `system:user:*` | ROLE_SUPER, ROLE_OPS |
| MENU_DEVICE_OVERVIEW | 设备总览 | 设备管理 | `device:manage:query` | ROLE_SUPER, ROLE_OPS |
| MENU_DEVICE_CONFIG | 设备配置 | 设备管理 | `device:manage:add`, `device:manage:update` | ROLE_SUPER |
| MENU_ALERT_CENTER | 告警中心 | 告警管理 | `alert:event:*` | ROLE_SUPER, ROLE_OPS, ROLE_AUDITOR |
| MENU_CONSUME_POLICY | 消费策略 | 消费系统 | `consume:policy:*` | ROLE_SUPER, ROLE_BUSINESS |
| MENU_ATTEND_RULE | 考勤规则 | 考勤系统 | `attend:rule:*` | ROLE_SUPER, ROLE_BUSINESS |
| MENU_REPORT_CENTER | 报表中心 | 分析报表 | `report:query:*` | 全角色 |

## 3. 数据权限映射
| 数据域 | 描述 | 控制字段 | 适用角色 |
| --- | --- | --- | --- |
| AREA_SCOPE | 区域维度权限 | `area_id` | ROLE_OPS, ROLE_BUSINESS |
| DEVICE_SCOPE | 设备维度权限 | `device_id` | ROLE_OPS |
| ORG_SCOPE | 组织维度 | `org_id` | ROLE_BUSINESS |
| AUDIT_SCOPE | 审计范围 | `log_type` | ROLE_AUDITOR |

## 4. 更新流程
1. 角色或菜单调整需提出安全变更单;
2. 更新 Sa-Token 权限配置与前端 `v-permission` 映射;
3. 同步本矩阵并在 24 小时内通知相关负责人;
4. 记录于 `SmartAdmin规范体系_v4/CHANGELOG.md`。

## 5. 审计说明
- 每季度进行权限对账, 确认角色与资源一致;
- 日志保留: 角色变更 3 年, 菜单授权 1 年;
- 异常检测: 权限超配或越权访问将触发告警 `SEC-ALERT-001`。

## 6. 附录
- 权限字典: 参考 `common_permission` 数据表;
- 菜单编码: 参考 `sys_menu` 表;
- 数据域配置: 参考 `common_data_scope` 表。
