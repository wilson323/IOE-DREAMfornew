-- =====================================================
-- IOE-DREAM 数据库创建脚本
-- 版本: 1.0.0
-- 说明: 创建所有业务数据库
-- =====================================================

-- 设置字符集
SET NAMES utf8mb4;

-- 创建数据库管理服务数据库
CREATE DATABASE IF NOT EXISTS ioedream_database
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
    COMMENT '数据库管理服务数据库';

-- 创建公共数据库
CREATE DATABASE IF NOT EXISTS ioedream_common_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
    COMMENT '公共数据库-用户权限区域管理';

-- 创建业务数据库
CREATE DATABASE IF NOT EXISTS ioedream_access_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
    COMMENT '门禁管理数据库';

CREATE DATABASE IF NOT EXISTS ioedream_attendance_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
    COMMENT '考勤管理数据库';

CREATE DATABASE IF NOT EXISTS ioedream_consume_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
    COMMENT '消费管理数据库';

CREATE DATABASE IF NOT EXISTS ioedream_visitor_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
    COMMENT '访客管理数据库';

CREATE DATABASE IF NOT EXISTS ioedream_video_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
    COMMENT '视频监控数据库';

CREATE DATABASE IF NOT EXISTS ioedream_device_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
    COMMENT '设备管理数据库';

SELECT '数据库创建完成' as message, COUNT(*) as created_count
FROM INFORMATION_SCHEMA.SCHEMATA
WHERE SCHEMA_NAME LIKE 'ioedream_%';