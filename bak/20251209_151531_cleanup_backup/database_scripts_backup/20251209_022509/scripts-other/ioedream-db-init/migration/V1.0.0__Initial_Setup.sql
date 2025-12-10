-- =====================================================
-- IOE-DREAM 数据库迁移脚本 - 初始设置
-- 版本: 1.0.0
-- 说明: 初始化数据库结构和基础数据
-- Flyway版本: V1.0.0
-- =====================================================

-- 这是一个复合迁移脚本，用于Flyway管理
-- 实际的表结构和数据创建由主初始化脚本完成

-- 验证数据库是否已正确初始化
DO $$
DECLARE
    db_count INT;
BEGIN
    -- 检查ioedream相关数据库的数量
    SELECT COUNT(*) INTO db_count
    FROM INFORMATION_SCHEMA.SCHEMATA
    WHERE SCHEMA_NAME LIKE 'ioedream_%';

    IF db_count < 7 THEN
        RAISE EXCEPTION '数据库未完全初始化，当前只有 % 个ioedream数据库', db_count;
    END IF;

    RAISE NOTICE '数据库初始化验证通过，找到 % 个ioedream数据库', db_count;
END $$;

-- 为所有ioedream数据库创建版本标记
DO $$
DECLARE
    db_name VARCHAR(100);
    done INT DEFAULT FALSE;
    db_cursor CURSOR FOR
        SELECT schema_name FROM INFORMATION_SCHEMA.SCHEMATA
        WHERE schema_name LIKE 'ioedream_%';
BEGIN
    OPEN db_cursor;
    LOOP
        FETCH db_cursor INTO db_name;
        IF done THEN
            LEAVE LOOP;
        END IF;

        EXECUTE format('
            CREATE TABLE IF NOT EXISTS %I.flyway_version_log (
                version VARCHAR(50) NOT NULL,
                description TEXT,
                installed_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                PRIMARY KEY (version)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

            INSERT INTO %I.flyway_version_log (version, description)
            VALUES (''1.0.0'', ''Initial database setup with complete schema and data'')
            ON DUPLICATE KEY UPDATE installed_on = CURRENT_TIMESTAMP;
        ', db_name);

    END LOOP;
    CLOSE db_cursor;
END $$;

SELECT 'Flyway迁移 V1.0.0 - 初始设置完成' as message,
       NOW() as completion_time;