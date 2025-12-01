-- 人员·区域·权限 回滚脚本（仅示例；生产回滚需结合备份与变更窗口）

-- 注：删除列/表会导致数据丢失，生产环境回滚需依赖备份恢复方案。

-- 1) 回滚新增表
DROP TABLE IF EXISTS t_person_credential;
DROP TABLE IF EXISTS t_area_person;
DROP TABLE IF EXISTS t_rbac_role_resource;
DROP TABLE IF EXISTS t_rbac_resource;
DROP TABLE IF EXISTS t_rbac_role;

-- 2) 回滚新增索引
DROP INDEX IF EXISTS idx_person_status ON t_person_profile;
DROP INDEX IF EXISTS idx_area_path_hash ON t_area_info;
DROP INDEX IF EXISTS idx_area_level ON t_area_info;

-- 3) 回滚新增列（谨慎执行）
ALTER TABLE t_person_profile
  DROP COLUMN IF EXISTS person_status,
  DROP COLUMN IF EXISTS id_masked,
  DROP COLUMN IF EXISTS ext_json,
  DROP COLUMN IF EXISTS deleted_flag;

ALTER TABLE t_area_info
  DROP COLUMN IF EXISTS path,
  DROP COLUMN IF EXISTS level,
  DROP COLUMN IF EXISTS path_hash,
  DROP COLUMN IF EXISTS deleted_flag;

