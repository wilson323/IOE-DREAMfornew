-- 人员状态机与联动撤销策略（最小可用DDL与巡检脚本）
-- 说明：
-- 1) 为 t_person_profile 增加状态字段与必要索引
-- 2) 提供离职/停用联动撤销的参考更新语句（需结合业务服务执行）
-- 3) 输出巡检：处于非激活状态但仍持有有效授权的人员

-- 1. 字段补强：状态与扩展信息
ALTER TABLE t_person_profile
  ADD COLUMN IF NOT EXISTS person_status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE|INACTIVE|LEAVE|BLACKLIST',
  ADD COLUMN IF NOT EXISTS extra_json JSON NULL,
  ADD COLUMN IF NOT EXISTS deleted_flag TINYINT NOT NULL DEFAULT 0,
  ADD INDEX IF NOT EXISTS idx_person_status(person_status);

-- 2. 联动撤销（示例语句，需在应用层控制事务与审计）
-- 2.1 将指定人员置为离职，并撤销其角色、数据域与门禁授权（参考）
-- UPDATE t_person_profile SET person_status = 'LEAVE' WHERE person_id = :personId;
-- DELETE FROM t_rbac_user_role WHERE user_id = :personId;
-- UPDATE t_area_person SET status = 0 WHERE person_id = :personId;
-- UPDATE t_access_permission SET status = 0 WHERE person_id = :personId; -- 示例表名，实际以门禁授权表为准
-- INSERT INTO t_audit_log(actor_id, action, target_id, detail_json) VALUES (:operatorId, 'PERSON_LEAVE_REVOKE', :personId, :detail);

-- 3. 巡检：非激活人员仍持有有效数据域授权
SELECT p.person_id, p.person_status, ap.area_id
FROM t_person_profile p
JOIN t_area_person ap ON ap.person_id = p.person_id AND ap.status = 1
WHERE p.person_status IN ('LEAVE','INACTIVE','BLACKLIST')
  AND p.deleted_flag = 0;

-- 4. 巡检：黑名单人员仍存在角色绑定
SELECT p.person_id, r.role_id
FROM t_person_profile p
JOIN t_rbac_user_role r ON r.user_id = p.person_id
WHERE p.person_status = 'BLACKLIST'
  AND p.deleted_flag = 0;


