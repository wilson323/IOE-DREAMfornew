-- =====================================================
-- IOE-DREAM Flyway 迁移脚本
-- 版本: V2.1.4
-- 描述: 对齐 t_rbac_resource 表结构到 RbacResourceEntity（字段/索引 + 从旧permission字段迁移数据）
-- =====================================================

CREATE TABLE IF NOT EXISTS t_rbac_resource (
    resource_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '资源ID',
    resource_code VARCHAR(100) NOT NULL COMMENT '资源编码',
    resource_name VARCHAR(200) NOT NULL COMMENT '资源名称',
    resource_type VARCHAR(20) NOT NULL COMMENT '资源类型',
    resource_path VARCHAR(500) COMMENT '资源路径',
    parent_id BIGINT DEFAULT 0 COMMENT '父资源ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    UNIQUE KEY uk_resource_code (resource_code, deleted_flag),
    KEY idx_parent_id (parent_id),
    KEY idx_resource_type (resource_type),
    KEY idx_status (status),
    KEY idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RBAC资源表';

ALTER TABLE t_rbac_resource
    ADD COLUMN IF NOT EXISTS resource_code VARCHAR(100) NULL COMMENT '资源编码' AFTER resource_id,
    ADD COLUMN IF NOT EXISTS resource_name VARCHAR(200) NULL COMMENT '资源名称' AFTER resource_code,
    ADD COLUMN IF NOT EXISTS resource_path VARCHAR(500) NULL COMMENT '资源路径' AFTER resource_type,
    ADD COLUMN IF NOT EXISTS remark VARCHAR(500) NULL COMMENT '备注' AFTER status;

UPDATE t_rbac_resource
SET resource_code = permission_code
WHERE resource_code IS NULL AND permission_code IS NOT NULL;

UPDATE t_rbac_resource
SET resource_name = permission_name
WHERE resource_name IS NULL AND permission_name IS NOT NULL;

UPDATE t_rbac_resource
SET resource_path = resource_url
WHERE resource_path IS NULL AND resource_url IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_resource_code ON t_rbac_resource (resource_code, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_parent_id ON t_rbac_resource (parent_id);
CREATE INDEX IF NOT EXISTS idx_resource_type ON t_rbac_resource (resource_type);
CREATE INDEX IF NOT EXISTS idx_status ON t_rbac_resource (status);
CREATE INDEX IF NOT EXISTS idx_sort_order ON t_rbac_resource (sort_order);

SELECT 'V2.1.4 t_rbac_resource 字段对齐完成' AS status;
