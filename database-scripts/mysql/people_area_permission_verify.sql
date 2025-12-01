-- ====================================================================
-- 人员·区域·权限 升级验证脚本
-- 版本: 1.0.0
-- 创建时间: 2025-11-16
-- 描述: 验证升级脚本创建的表结构和数据完整性
-- ====================================================================

-- 1) 验证表是否创建成功
SELECT 'Table Verification' AS check_type,
       table_name AS name,
       table_comment AS description,
       table_rows AS row_count
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN (
    't_person_profile', 't_person_credential', 't_area_info',
    't_area_person', 't_rbac_resource', 't_rbac_role',
    't_rbac_role_resource', 't_rbac_user_role'
  )
ORDER BY table_name;

-- 2) 验证索引是否创建成功
SELECT 'Index Verification' AS check_type,
       table_name AS table_name,
       index_name AS index_name,
       index_type AS index_type,
       CASE WHEN non_unique = 0 THEN 'UNIQUE' ELSE 'NON-UNIQUE' END AS uniqueness
FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND table_name IN (
    't_person_profile', 't_person_credential', 't_area_info',
    't_area_person', 't_rbac_resource', 't_rbac_role',
    't_rbac_role_resource', 't_rbac_user_role'
  )
ORDER BY table_name, index_name;

-- 3) 验证外键约束是否创建成功
SELECT 'Foreign Key Verification' AS check_type,
       table_name AS table_name,
       constraint_name AS constraint_name,
       column_name AS column_name,
       referenced_table_name AS referenced_table,
       referenced_column_name AS referenced_column
FROM information_schema.key_column_usage
WHERE table_schema = DATABASE()
  AND referenced_table_schema = DATABASE()
  AND table_name IN (
    't_person_credential', 't_area_person', 't_rbac_role_resource', 't_rbac_user_role'
  )
ORDER BY table_name, constraint_name;

-- 4) 验证基础数据是否插入成功
-- 验证角色数据
SELECT 'Role Data Verification' AS check_type,
       role_code,
       role_name,
       role_level,
       status
FROM t_rbac_role
ORDER BY role_level DESC;

-- 验证资源数据
SELECT 'Resource Data Verification' AS check_type,
       module,
       resource_type,
       COUNT(*) AS resource_count,
       SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS active_count
FROM t_rbac_resource
GROUP BY module, resource_type
ORDER BY module, resource_type;

-- 5) 验证表结构完整性
SELECT 'Table Structure Verification' AS check_type,
       table_name,
       column_name,
       data_type,
       character_maximum_length,
       is_nullable,
       column_default,
       column_comment
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name IN (
    't_person_profile', 't_person_credential', 't_area_info',
    't_area_person', 't_rbac_resource', 't_rbac_role',
    't_rbac_role_resource', 't_rbac_user_role'
  )
  AND column_name IN ('create_time', 'update_time', 'create_user_id', 'update_user_id', 'deleted_flag', 'version')
ORDER BY table_name, column_name;

-- 6) 数据一致性检查
-- 检查人员档案表的数据完整性
SELECT 'Person Profile Data Check' AS check_type,
       COUNT(*) AS total_records,
       COUNT(CASE WHEN person_code IS NOT NULL AND person_code != '' THEN 1 END) AS has_code,
       COUNT(CASE WHEN person_name IS NOT NULL AND person_name != '' THEN 1 END) AS has_name,
       COUNT(CASE WHEN person_status IN (0,1,2,3) THEN 1 END) AS valid_status
FROM t_person_profile;

-- 检查区域表的数据完整性
SELECT 'Area Info Data Check' AS check_type,
       COUNT(*) AS total_records,
       COUNT(CASE WHEN area_code IS NOT NULL AND area_code != '' THEN 1 END) AS has_code,
       COUNT(CASE WHEN area_name IS NOT NULL AND area_name != '' THEN 1 END) AS has_name,
       COUNT(CASE WHEN parent_id >= 0 THEN 1 END) AS valid_parent,
       COUNT(CASE WHEN path IS NOT NULL AND path != '' THEN 1 END) AS has_path
FROM t_area_info
WHERE deleted_flag = 0;

-- 7) 性能检查建议
SELECT 'Performance Check Suggestions' AS check_type,
       '建议为以下表添加适当索引以提高查询性能' AS suggestion
UNION ALL
SELECT 'Large Tables' AS check_type,
       CONCAT(table_name, ': ', ROUND(data_length/1024/1024, 2), ' MB') AS size_info
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN (
    't_person_profile', 't_person_credential', 't_area_info',
    't_area_person', 't_rbac_role_resource'
  )
  AND data_length > 1024*1024  -- 大于1MB的表
ORDER BY table_name;

-- 8) 安全性检查
SELECT 'Security Check' AS check_type,
       table_name,
       column_name,
       data_type,
       '敏感数据字段' AS security_note
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name IN ('t_person_profile', 't_person_credential')
  AND (column_name LIKE '%card%' OR column_name LIKE '%phone%' OR column_name LIKE '%credential%')
ORDER BY table_name, column_name;

-- ====================================================================
-- 验证完成
-- ====================================================================

SELECT 'Verification Complete' AS status,
       NOW() AS verification_time,
       '请检查以上输出，确认所有表结构、索引、数据和约束都正确创建' AS final_note;