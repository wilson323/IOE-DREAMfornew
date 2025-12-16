-- =====================================================
-- IOE-DREAM 统一数据库初始化入口脚本（推荐唯一入口）
-- 说明：按顺序执行所有初始化/增量脚本，并纳入database-scripts补丁脚本
-- 约定：表命名以 t_* 为准
-- =====================================================

-- 建议用法：
-- mysql -u root -p < deployment/mysql/init/00-all-init.sql

SOURCE deployment/mysql/init/00-version-check.sql;
SOURCE deployment/mysql/init/01-ioedream-schema.sql;

-- 按环境选择其一（默认用通用开发数据）
SOURCE deployment/mysql/init/02-ioedream-data.sql;
-- SOURCE deployment/mysql/init/02-ioedream-data-dev.sql;
-- SOURCE deployment/mysql/init/02-ioedream-data-test.sql;
-- SOURCE deployment/mysql/init/02-ioedream-data-prod.sql;

SOURCE deployment/mysql/init/03-optimize-indexes.sql;
SOURCE deployment/mysql/init/04-v2.0.0__enhance-consume-record.sql;
SOURCE deployment/mysql/init/05-v2.0.1__enhance-account-table.sql;
SOURCE deployment/mysql/init/06-v2.0.2__create-refund-table.sql;
SOURCE deployment/mysql/init/07-v2.1.0__api-compatibility-validation.sql;

-- V2.1.1：表命名规范统一（t_common_area -> t_area）
SOURCE deployment/mysql/init/08-v2.1.1__rename_common_area_to_area.sql;

-- V2.1.2：对齐 t_audit_log 表结构到 AuditLogEntity
SOURCE deployment/mysql/init/09-v2.1.2__enhance_audit_log.sql;

-- V2.1.3：RBAC/用户权限表命名体系统一（t_common_* -> t_*）
SOURCE deployment/mysql/init/10-v2.1.3__rename_common_rbac_tables_to_t.sql;

-- V2.1.4：对齐 t_rbac_resource 表结构到实体（并迁移旧permission字段数据）
SOURCE deployment/mysql/init/11-v2.1.4__align_rbac_resource_schema.sql;

-- V2.1.4：创建 t_role_menu（角色菜单关联表）
SOURCE deployment/mysql/init/12-v2.1.4__create_role_menu.sql;

-- V2.1.5：批量补齐缺口表（第1批：RBAC核心字段对齐 + 字典/菜单/员工/会话/主题）
SOURCE deployment/mysql/init/13-v2.1.5__create_missing_tables_batch1.sql;

-- V2.1.5：批量补齐缺口表（第2批：告警/通知/监控/日志/区域关联）
SOURCE deployment/mysql/init/14-v2.1.5__create_missing_tables_batch2.sql;

-- V2.1.5：批量补齐缺口表（第3批：访客/消费/门禁/考勤/工作流/审批）
SOURCE deployment/mysql/init/15-v2.1.5__create_missing_tables_batch3.sql;

-- V2.1.5：批量补齐缺口表（第4批：工作流/审批/考勤/门禁申请）
SOURCE deployment/mysql/init/16-v2.1.5__create_missing_tables_batch4.sql;

-- 补丁脚本（来自database-scripts/，需纳入统一入口避免遗漏）
SOURCE database-scripts/common-service/25-t_user_preference.sql;
SOURCE database-scripts/common-service/24-t_config_change_audit.sql;
SOURCE database-scripts/consume-service/01-t_meal_order.sql;
SOURCE database-scripts/consume-service/02-t_offline_consume_record.sql;
SOURCE database-scripts/oa-service/01-t_workflow_tables.sql;

-- 初始化Nacos数据库（独立库/独立schema）
SOURCE deployment/mysql/init/nacos-schema.sql;

-- nacos-schema.sql 会切换到 nacos 库，执行完后切回 ioedream，避免后续操作落错库
USE ioedream;
