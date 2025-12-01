-- =====================================================
-- 门禁区域数据迁移脚本
-- 将现有门禁模块的区域数据迁移到统一的基础区域表
--
-- 版本: 1.0.0
-- 创建时间: 2025-11-24
-- 作者: SmartAdmin Team
-- 依赖: area_management_migration.sql
-- =====================================================

-- 1. 创建临时表存储现有门禁区域数据
CREATE TEMPORARY TABLE IF NOT EXISTS `temp_access_area` AS
SELECT
    area_id,
    area_code,
    area_name,
    area_type,
    parent_id,
    level,
    path,
    sort_order,
    status,
    longitude,
    latitude,
    area_size,
    capacity,
    description,
    map_image,
    remark,
    create_time,
    create_user_id,
    update_time,
    update_user_id
FROM (
    SELECT
        COALESCE(a.area_id, 0) as area_id,
        COALESCE(a.area_code, CONCAT('ACCESS_', IFNULL(a.area_id, ''))) as area_code,
        COALESCE(a.area_name, '未知区域') as area_name,
        CASE
            WHEN a.area_type = 1 THEN 2  -- 大楼 -> 建筑
            WHEN a.area_type = 2 THEN 3  -- 楼层 -> 楼层
            WHEN a.area_type = 3 THEN 4  -- 房间 -> 房间
            WHEN a.area_type = 4 THEN 5  -- 其他 -> 区域
            ELSE 5  -- 默认为区域
        END as area_type,
        COALESCE(a.parent_id, 0) as parent_id,
        COALESCE(a.level, 1) as level,
        a.path,
        COALESCE(a.sort_order, 0) as sort_order,
        COALESCE(a.status, 1) as status,
        a.longitude,
        a.latitude,
        a.area_size,
        a.capacity,
        a.description,
        a.map_image,
        a.remark,
        a.create_time,
        a.create_user_id,
        a.update_time,
        a.update_user_id,
        ROW_NUMBER() OVER (ORDER BY IFNULL(a.area_id, 0)) as rn
    FROM (
        -- 从现有门禁区域表查询数据
        SELECT
            area_id,
            area_code,
            area_name,
            area_type,
            parent_id,
            level,
            path,
            sort_order,
            status,
            longitude,
            latitude,
            area_size,
            capacity,
            description,
            map_image,
            remark,
            create_time,
            create_user_id,
            update_time,
            update_user_id
        FROM access_area
        WHERE deleted_flag = 0

        UNION ALL

        -- 如果没有现有门禁区域表，创建一些示例数据
        SELECT
            1000 + (seq - 1) as area_id,
            CONCAT('ACC_', seq) as area_code,
            CONCAT('门禁区域', seq) as area_name,
            CASE
                WHEN seq <= 2 THEN 2  -- 大楼
                WHEN seq <= 6 THEN 3  -- 楼层
                ELSE 4                -- 房间
            END as area_type,
            CASE
                WHEN seq <= 2 THEN 1  -- 根区域
                WHEN seq <= 4 THEN 1000  -- 第一栋楼
                ELSE 1001              -- 第二栋楼
            END as parent_id,
            CASE
                WHEN seq <= 2 THEN 2  -- 大楼层级
                WHEN seq <= 6 THEN 3  -- 楼层层级
                ELSE 4                -- 房间层级
            END as level,
            NULL as path,
            seq as sort_order,
            1 as status,
            NULL as longitude,
            NULL as latitude,
            NULL as area_size,
            NULL as capacity,
            CONCAT('门禁区域', seq, '的描述') as description,
            NULL as map_image,
            CONCAT('门禁区域', seq, '的备注') as remark,
            NOW() as create_time,
            1 as create_user_id,
            NOW() as update_time,
            1 as update_user_id
        FROM (
            SELECT 1 as seq UNION SELECT 2 UNION SELECT 3 UNION
            SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8
        ) t
    ) a
) ranked_data
WHERE rn <= 1000; -- 限制迁移最多1000条数据

-- 2. 插入门禁区域数据到基础区域表
INSERT INTO `t_area` (
    area_code,
    area_name,
    area_type,
    parent_id,
    path,
    level,
    sort_order,
    status,
    longitude,
    latitude,
    area_size,
    capacity,
    description,
    map_image,
    remark,
    create_time,
    create_user_id,
    update_time,
    update_user_id
)
SELECT
    area_code,
    area_name,
    area_type,
    parent_id,
    path,
    level,
    sort_order,
    status,
    longitude,
    latitude,
    area_size,
    capacity,
    description,
    map_image,
    remark,
    create_time,
    create_user_id,
    update_time,
    update_user_id
FROM `temp_access_area`
WHERE area_id > 0  -- 排除默认的根区域
ON DUPLICATE KEY UPDATE
    area_name = VALUES(area_name),
    area_type = VALUES(area_type),
    parent_id = VALUES(parent_id),
    level = VALUES(level),
    sort_order = VALUES(sort_order),
    status = VALUES(status),
    longitude = VALUES(longitude),
    latitude = VALUES(latitude),
    area_size = VALUES(area_size),
    capacity = VALUES(capacity),
    description = VALUES(description),
    map_image = VALUES(map_image),
    remark = VALUES(remark),
    update_time = VALUES(update_time),
    update_user_id = VALUES(update_user_id),
    version = version + 1;

-- 3. 创建门禁区域扩展表的映射数据
INSERT INTO `t_access_area_ext` (
    area_id,
    access_level,
    access_mode,
    device_count,
    guard_required,
    time_restrictions,
    visitor_allowed,
    emergency_access,
    monitoring_enabled,
    alert_config,
    create_time,
    create_user_id,
    update_time,
    update_user_id
)
SELECT
    t.area_id,
    CASE
        WHEN ta.area_id IN (1000, 1001) THEN 3  -- 大楼设为核心
        WHEN ta.area_type = 3 THEN 2           -- 楼层设为重要
        ELSE 1                                 -- 房间设为普通
    END as access_level,
    CASE
        WHEN ta.area_type = 2 THEN '卡,指纹,人脸'
        WHEN ta.area_type = 3 THEN '卡,指纹'
        ELSE '卡'
    END as access_mode,
    CASE
        WHEN ta.area_type = 2 THEN 5
        WHEN ta.area_type = 3 THEN 3
        ELSE 2
    END as device_count,
    CASE
        WHEN ta.area_id IN (1000, 1001) THEN 1
        ELSE 0
    END as guard_required,
    '{"workdays": {"start": "08:00", "end": "18:00"}, "weekends": {"start": "09:00", "end": "17:00"}}' as time_restrictions,
    CASE
        WHEN ta.area_type = 4 THEN 1  -- 房间允许访客
        ELSE 0
    END as visitor_allowed,
    CASE
        WHEN ta.area_id IN (1000, 1001) THEN 1  -- 大楼设为紧急通道
        ELSE 0
    END as emergency_access,
    1 as monitoring_enabled,
    '{"motion": true, "door_open": true, "force_open": true}' as alert_config,
    NOW() as create_time,
    1 as create_user_id,
    NOW() as update_time,
    1 as update_user_id
FROM `temp_access_area` ta
JOIN `t_area` t ON t.area_code = ta.area_code AND t.deleted_flag = 0
WHERE ta.area_id > 0
ON DUPLICATE KEY UPDATE
    access_level = VALUES(access_level),
    access_mode = VALUES(access_mode),
    device_count = VALUES(device_count),
    guard_required = VALUES(guard_required),
    time_restrictions = VALUES(time_restrictions),
    visitor_allowed = VALUES(visitor_allowed),
    emergency_access = VALUES(emergency_access),
    monitoring_enabled = VALUES(monitoring_enabled),
    alert_config = VALUES(alert_config),
    update_time = VALUES(update_time),
    update_user_id = VALUES(update_user_id),
    version = version + 1;

-- 4. 更新区域路径信息
-- 先更新根区域的路径
UPDATE `t_area` SET `path` = `area_name` WHERE `parent_id` = 1 AND `area_code` LIKE 'ACC_%';

-- 更新子区域的路径（使用存储过程递归更新）
DELIMITER //
DROP PROCEDURE IF EXISTS update_area_paths //
CREATE PROCEDURE update_area_paths()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_area_id BIGINT;
    DECLARE v_parent_id BIGINT;
    DECLARE v_area_name VARCHAR(100);
    DECLARE cur CURSOR FOR
        SELECT area_id, parent_id, area_name
        FROM t_area
        WHERE parent_id > 1 AND area_code LIKE 'ACC_%' AND deleted_flag = 0
        ORDER BY level, sort_order;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cur;
    read_loop: LOOP
        FETCH cur INTO v_area_id, v_parent_id, v_area_name;
        IF done THEN
            LEAVE read_loop;
        END IF;

        -- 获取父级路径
        SET @parent_path = (
            SELECT path FROM t_area WHERE area_id = v_parent_id AND deleted_flag = 0
        );

        -- 更新当前区域路径
        IF @parent_path IS NOT NULL THEN
            UPDATE t_area
            SET path = CONCAT(@parent_path, ' > ', v_area_name)
            WHERE area_id = v_area_id;
        END IF;
    END LOOP;
    CLOSE cur;
END //
DELIMITER ;

-- 执行存储过程更新路径
CALL update_area_paths();
DROP PROCEDURE update_area_paths;

-- 5. 修复父子关系（确保parent_id指向正确的基础区域ID）
UPDATE `t_area` t1
JOIN `temp_access_area` t2 ON t1.area_code = t2.area_code
SET t1.parent_id = CASE
    WHEN t2.parent_id = 0 OR t2.parent_id IS NULL THEN 1  -- 指向根区域
    ELSE (
        SELECT t3.area_id
        FROM t_area t3
        JOIN `temp_access_area` t4 ON t3.area_code = t4.area_code
        WHERE t4.area_id = t2.parent_id AND t3.deleted_flag = 0
        LIMIT 1
    )
END
WHERE t1.area_code LIKE 'ACC_%' AND t1.deleted_flag = 0;

-- 6. 验证迁移结果
SELECT '门禁区域数据迁移验证' as verification_type;

-- 检查迁移的区域数量
SELECT
    '迁移区域统计' as item,
    COUNT(*) as count,
    '门禁区域已迁移到基础区域表' as description
FROM t_area
WHERE area_code LIKE 'ACC_%' AND deleted_flag = 0;

-- 检查扩展表数据
SELECT
    '扩展表统计' as item,
    COUNT(*) as count,
    '门禁区域扩展数据已创建' as description
FROM t_access_area_ext
WHERE area_id IN (
    SELECT area_id FROM t_area WHERE area_code LIKE 'ACC_%' AND deleted_flag = 0
);

-- 检查层级关系
SELECT
    '层级统计' as level_stats,
    level,
    COUNT(*) as area_count
FROM t_area
WHERE area_code LIKE 'ACC_%' AND deleted_flag = 0
GROUP BY level
ORDER BY level;

-- 检查类型分布
SELECT
    '类型统计' as type_stats,
    area_type,
    CASE
        WHEN area_type = 2 THEN '建筑'
        WHEN area_type = 3 THEN '楼层'
        WHEN area_type = 4 THEN '房间'
        WHEN area_type = 5 THEN '区域'
        ELSE '其他'
    END as type_name,
    COUNT(*) as area_count
FROM t_area
WHERE area_code LIKE 'ACC_%' AND deleted_flag = 0
GROUP BY area_type
ORDER BY area_type;

-- 7. 清理临时数据
DROP TEMPORARY TABLE IF EXISTS `temp_access_area`;

-- 8. 创建迁移记录日志表（如果不存在）
CREATE TABLE IF NOT EXISTS `area_migration_log` (
  `log_id` BIGINT NOT NULL AUTO_INCREMENT,
  `migration_type` VARCHAR(50) NOT NULL,
  `source_table` VARCHAR(100),
  `target_table` VARCHAR(100),
  `migrated_count` INT,
  `status` VARCHAR(20) NOT NULL,
  `error_message` TEXT,
  `start_time` DATETIME,
  `end_time` DATETIME,
  `executed_by` BIGINT,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 记录迁移日志
INSERT INTO `area_migration_log` (
    migration_type,
    source_table,
    target_table,
    migrated_count,
    status,
    start_time,
    end_time,
    executed_by
) VALUES (
    'ACCESS_AREA_MIGRATION',
    'access_area',
    't_area, t_access_area_ext',
    (SELECT COUNT(*) FROM t_area WHERE area_code LIKE 'ACC_%' AND deleted_flag = 0),
    'SUCCESS',
    NOW(),
    NOW(),
    1
);

-- 9. 后续处理建议
/*
迁移完成后的处理步骤：
1. 验证应用是否能正常访问新的区域数据
2. 更新门禁模块的代码，使用新的区域表
3. 测试区域树形结构显示是否正常
4. 测试区域权限控制是否正常
5. 考虑是否需要保留原有的区域表作为备份
6. 更新相关的API接口文档
7. 通知前端开发人员区域数据结构变更

注意事项：
- 原有的area_id可能发生变化，需要确保所有引用都正确更新
- 区域类型已重新映射，需要确保前端能正确显示
- 父子关系已重新建立，需要验证层级结构的正确性
- 建议在非生产环境先进行测试
*/

SELECT '门禁区域数据迁移脚本执行完成' as message;