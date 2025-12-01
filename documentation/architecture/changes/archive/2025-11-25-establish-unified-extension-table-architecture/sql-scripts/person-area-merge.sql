-- =====================================================
-- IOE-DREAM 项目 人员区域关系实体合并数据迁移脚本
-- 修正时间：2025-11-25
-- 修正目标：合并AreaPersonEntity到PersonAreaRelationEntity
-- 解决重复实体问题，统一人员区域权限管理
--
-- 迁移策略：保留PersonAreaRelationEntity，扩展其功能吸收AreaPersonEntity
-- 消除数据一致性风险，统一权限验证机制
-- =====================================================

-- 1. 数据备份（重要！）
-- -------------------------------------------------
CREATE TABLE IF NOT EXISTS t_area_person_backup AS
SELECT * FROM t_area_person;

CREATE TABLE IF NOT EXISTS t_person_area_relation_backup AS
SELECT * FROM t_person_area_relation;

-- 2. 数据分析
-- -------------------------------------------------
SELECT '=== 数据迁移前分析 ===' as analysis_info;

-- 统计现有数据
SELECT
    'AreaPerson表数据量' as table_name,
    COUNT(*) as total_count,
    COUNT(CASE WHEN deleted_flag = 0 THEN 1 END) as active_count
FROM t_area_person;

SELECT
    'PersonAreaRelation表数据量' as table_name,
    COUNT(*) as total_count,
    COUNT(CASE WHEN deleted_flag = 0 THEN 1 END) as active_count
FROM t_person_area_relation;

-- 分析数据重叠情况
SELECT
    '数据重叠分析' as analysis_type,
    COUNT(*) as overlap_count,
    COUNT(DISTINCT person_id) as unique_person_count,
    COUNT(DISTINCT area_id) as unique_area_count
FROM (
    SELECT DISTINCT person_id, area_id
    FROM t_area_person ap
    WHERE ap.deleted_flag = 0
    AND EXISTS (
        SELECT 1 FROM t_person_area_relation par
        WHERE par.person_id = ap.person_id
        AND par.area_id = ap.area_id
        AND par.deleted_flag = 0
    )
) overlap_data;

-- 3. 扩展PersonAreaRelationEntity表结构
-- -------------------------------------------------
-- 检查新字段是否已存在，避免重复添加
-- data_scope字段
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE()
     AND TABLE_NAME = 't_person_area_relation'
     AND COLUMN_NAME = 'data_scope') > 0,
    'SELECT "data_scope字段已存在，跳过添加" as info;',
    'ALTER TABLE t_person_area_relation ADD COLUMN data_scope VARCHAR(20) COMMENT "数据域(AREA|DEPT|SELF|CUSTOM)";'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- access_level字段
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE()
     AND TABLE_NAME = 't_person_area_relation'
     AND COLUMN_NAME = 'access_level') > 0,
    'SELECT "access_level字段已存在，跳过添加" as info;',
    'ALTER TABLE t_person_area_relation ADD COLUMN access_level INT DEFAULT 1 COMMENT "访问级别，数字越大权限要求越高";'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- special_permissions字段
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE()
     AND TABLE_NAME = 't_person_area_relation'
     AND COLUMN_NAME = 'special_permissions') > 0,
    'SELECT "special_permissions字段已存在，跳过添加" as info;',
    'ALTER TABLE t_person_area_relation ADD COLUMN special_permissions TEXT COMMENT "特殊权限配置(JSON格式)";'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. 数据迁移：将AreaPerson数据迁移到PersonAreaRelation
-- -------------------------------------------------
INSERT INTO t_person_area_relation (
    person_id, area_id, data_scope, effective_time, expire_time, status,
    create_time, update_time, create_user_id, update_user_id, deleted_flag, version
)
SELECT
    person_id,
    area_id,
    data_scope,
    effective_time,
    expire_time,
    status,
    create_time,
    update_time,
    create_user_id,
    update_user_id,
    deleted_flag,
    1 as version
FROM t_area_person
WHERE deleted_flag = 0
ON DUPLICATE KEY UPDATE
    data_scope = VALUES(data_scope),
    access_level = COALESCE(access_level, 1),
    update_time = VALUES(update_time),
    update_user_id = VALUES(update_user_id);

-- 5. 性能优化索引
-- -------------------------------------------------
-- 为新增字段添加索引
CREATE INDEX IF NOT EXISTS idx_person_area_data_scope ON t_person_area_relation(data_scope);
CREATE INDEX IF NOT EXISTS idx_person_area_access_level ON t_person_area_relation(access_level);
CREATE INDEX IF NOT EXISTS idx_person_area_type_scope ON t_person_area_relation(person_type, data_scope);
CREATE INDEX IF NOT EXISTS idx_person_area_person_data ON t_person_area_relation(person_id, data_scope);
CREATE INDEX IF NOT EXISTS idx_person_area_area_data ON t_person_area_relation(area_id, data_scope);

-- 6. 数据完整性验证
-- =====================================================
SELECT '=== 数据迁移后验证 ===' as verification_info;

-- 验证基础数据
SELECT
    'PersonAreaRelation迁移后统计' as table_name,
    COUNT(*) as total_count,
    COUNT(CASE WHEN deleted_flag = 0 THEN 1 END) as active_count,
    COUNT(CASE WHEN data_scope IS NOT NULL THEN 1 END) as data_scope_count
FROM t_person_area_relation;

-- 验证data_scope分布
SELECT
    'DataScope分布统计' as scope_type,
    data_scope,
    COUNT(*) as count
FROM t_person_area_relation
WHERE deleted_flag = 0 AND data_scope IS NOT NULL
GROUP BY data_scope;

-- 验证数据完整性
SELECT
    '数据完整性检查' as check_type,
    '检查total_count' as description,
    COUNT(*) as total_count,
    COUNT(CASE WHEN person_id IS NULL OR person_id = 0 THEN 1 END) as missing_person_id,
    COUNT(CASE WHEN area_id IS NULL OR area_id = 0 THEN 1 END) as missing_area_id
FROM t_person_area_relation
WHERE deleted_flag = 0;

-- 7. 外键约束检查
-- =====================================================
-- 检查人员ID是否存在关联
SELECT
    '人员ID存在性检查' as check_type,
    COUNT(*) as invalid_person_count
FROM t_person_area_relation par
WHERE par.deleted_flag = 0
AND par.person_id IS NOT NULL
AND NOT EXISTS (
    SELECT 1 FROM t_employee e
    WHERE e.employee_id = par.person_id AND e.deleted_flag = 0
    UNION
    SELECT 1 FROM t_visitor v
    WHERE v.visitor_id = par.person_id AND v.deleted_flag = 0
);

-- 检查区域ID是否存在关联
SELECT
    '区域ID存在性检查' as check_type,
    COUNT(*) as invalid_area_count
FROM t_person_area_relation par
WHERE par.deleted_flag = 0
AND par.area_id IS NOT NULL
AND NOT EXISTS (
    SELECT 1 FROM t_area a
    WHERE a.area_id = par.area_id AND a.deleted_flag = 0
);

-- 8. 数据一致性保证触发器
-- =====================================================
DELIMITER //

-- 创建数据一致性检查触发器
CREATE TRIGGER check_person_area_relation_insert
BEFORE INSERT ON t_person_area_relation
FOR EACH ROW
BEGIN
    DECLARE person_exists INT;
    DECLARE area_exists INT;

    -- 检查人员是否存在
    SELECT COUNT(*) INTO person_exists
    FROM (
        SELECT employee_id as id FROM t_employee WHERE employee_id = NEW.person_id AND deleted_flag = 0
        UNION
        SELECT visitor_id as id FROM t_visitor WHERE visitor_id = NEW.person_id AND deleted_flag = 0
    ) persons;

    IF person_exists = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = '关联的人员不存在或已删除';
    END IF;

    -- 检查区域是否存在
    SELECT COUNT(*) INTO area_exists
    FROM t_area
    WHERE area_id = NEW.area_id AND deleted_flag = 0;

    IF area_exists = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = '关联的区域不存在或已删除';
    END IF;
END//

-- 创建更新触发器
CREATE TRIGGER check_person_area_relation_update
BEFORE UPDATE ON t_person_area_relation
FOR EACH ROW
BEGIN
    DECLARE person_exists INT;
    DECLARE area_exists INT;

    -- 检查人员是否存在（如果person_id发生变化）
    IF NEW.person_id <> OLD.person_id THEN
        SELECT COUNT(*) INTO person_exists
        FROM (
            SELECT employee_id as id FROM t_employee WHERE employee_id = NEW.person_id AND deleted_flag = 0
            UNION
            SELECT visitor_id as id FROM t_visitor WHERE visitor_id = NEW.person_id AND deleted_flag = 0
        ) persons;

        IF person_exists = 0 THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '关联的人员不存在或已删除';
        END IF;
    END IF;

    -- 检查区域是否存在（如果area_id发生变化）
    IF NEW.area_id <> OLD.area_id THEN
        SELECT COUNT(*) INTO area_exists
        FROM t_area
        WHERE area_id = NEW.area_id AND deleted_flag = 0;

        IF area_exists = 0 THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '关联的区域不存在或已删除';
        END IF;
    END IF;
END//

DELIMITER ;

-- 9. 代码重构建议（手动执行）
-- =====================================================
-- 以下为代码重构建议，需要在Java代码中手动修改：

/*
1. 修改AreaPermissionService，改用PersonAreaRelationEntity：

@Service
public class AreaPermissionServiceImpl implements AreaPermissionService {

    @Resource
    private PersonAreaRelationDao personAreaRelationDao; // 改用PersonAreaRelationDao

    @Override
    public boolean hasAreaPermission(Long userId, Long areaId) {
        // 改用PersonAreaRelationEntity的逻辑
        return personAreaRelationDao.existsRelation(userId, areaId, null);
    }

    @Override
    public List<Long> getUserAuthorizedAreaIds(Long userId) {
        return personAreaRelationDao.getAreaIdsByPerson(userId, null);
    }

    @Override
    public List<Long> getUserAreaIdsByDataScope(Long userId, String dataScope) {
        return personAreaRelationDao.getAreaIdsByPersonAndDataScope(userId, dataScope);
    }
}

2. 创建统一的PersonAreaService：

@Service
public class PersonAreaServiceImpl implements PersonAreaService {

    // 保留现有的PersonAreaRelationEntity功能
    // 新增AreaPersonEntity的dataScope相关功能

    public ResponseDTO<String> batchAddPersonAreaRelation(BatchPersonAreaRelationForm form) {
        // 整合两个服务的功能
    }
}

3. API兼容性保持：

@RestController
public class PersonAreaCompatibilityController {

    // 保持旧API兼容，内部调用新Service
    @Deprecated
    @PostMapping("/area/person/add")
    public ResponseDTO<String> addAreaPersonOld(@RequestBody AreaPersonForm form) {
        // 转换为PersonAreaRelationForm并调用新Service
        return unifiedPersonAreaService.addPersonAreaRelation(convertForm(form));
    }
}
*/

-- 10. 清理建议（谨慎执行！）
-- =====================================================
-- 以下为可选的清理步骤，请在充分测试后执行：

-- -- 标记旧表为废弃（建议保留一段时间）
-- ALTER TABLE t_area_person RENAME TO t_area_person_deprecated;
--
-- -- 创建视图以保持向后兼容（可选）
-- CREATE OR REPLACE VIEW v_area_person AS
-- SELECT
--     id, person_id, area_id, data_scope, effective_time, expire_time, status,
--     create_time, update_time, create_user_id, update_user_id, deleted_flag, version
-- FROM t_person_area_relation
-- WHERE data_scope IS NOT NULL;

-- =====================================================
-- 迁移完成！
-- =====================================================

SELECT '=== 人员区域关系实体合并迁移完成 ===' as completion_info;
SELECT '迁移时间：' AS info, NOW() AS migration_time;
SELECT '注意事项：' AS info,
       '1. 请验证应用程序功能正常' AS note1,
       '2. 请重构AreaPermissionService使用PersonAreaRelationEntity' AS note2,
       '3. 请测试RBAC权限验证功能' AS note3,
       '4. 如有问题，可从备份表恢复数据' AS note4;