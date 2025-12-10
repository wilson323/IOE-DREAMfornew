-- =====================================================
-- IOE-DREAM Flyway版本管理表脚本
-- 版本: 1.0.0
-- 说明: 为所有数据库创建Flyway版本管理表
-- =====================================================

-- 定义需要创建Flyway表的数据库列表
SET @databases = 'ioedream_database,ioedream_common_db,ioedream_access_db,ioedream_attendance_db,ioedream_consume_db,ioedream_visitor_db,ioedream_video_db,ioedream_device_db';

-- 创建存储过程来为每个数据库创建Flyway表
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS create_flyway_tables()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE db_name VARCHAR(100);
    DECLARE db_cursor CURSOR FOR
        SELECT schema_name FROM INFORMATION_SCHEMA.SCHEMATA
        WHERE schema_name IN ('ioedream_database','ioedream_common_db','ioedream_access_db','ioedream_attendance_db','ioedream_consume_db','ioedream_visitor_db','ioedream_video_db','ioedream_device_db');
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN db_cursor;
    read_loop: LOOP
        FETCH db_cursor INTO db_name;
        IF done THEN
            LEAVE read_loop;
        END IF;

        SET @sql = CONCAT('
        USE ', db_name, ';
        CREATE TABLE IF NOT EXISTS flyway_schema_history (
            installed_rank INT NOT NULL,
            version VARCHAR(50),
            description VARCHAR(200) NOT NULL,
            type VARCHAR(20) NOT NULL,
            script VARCHAR(1000) NOT NULL,
            checksum INT,
            installed_by VARCHAR(100) NOT NULL,
            installed_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            execution_time INT NOT NULL,
            success TINYINT NOT NULL,
            PRIMARY KEY (installed_rank),
            INDEX flyway_schema_history_s_idx (success)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

        -- 创建数据库版本检查视图
        CREATE OR REPLACE VIEW v_flyway_version AS
        SELECT
            ''1.0.0'' as current_version,
            ''INITIAL'' as migration_status,
            NOW() as last_update_time,
            ''Database initialized with flyway'' as description;
        ');

        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;

    END LOOP;
    CLOSE db_cursor;
END //
DELIMITER ;

-- 执行存储过程
CALL create_flyway_tables();

-- 删除存储过程
DROP PROCEDURE IF EXISTS create_flyway_tables;

-- 验证Flyway表创建结果
SELECT
    TABLE_SCHEMA as database_name,
    TABLE_NAME as table_name,
    TABLE_COMMENT as description
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_NAME = 'flyway_schema_history'
  AND TABLE_SCHEMA LIKE 'ioedream_%'
ORDER BY TABLE_SCHEMA;

SELECT 'Flyway版本管理表创建完成' as message,
       COUNT(*) as created_count
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_NAME = 'flyway_schema_history'
  AND TABLE_SCHEMA LIKE 'ioedream_%';