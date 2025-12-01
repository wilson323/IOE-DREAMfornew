-- 区域层级一致性维护与巡检脚本
-- 说明：
-- 1. 回填 t_area_info 的 path / level / path_hash 字段
-- 2. 扫描潜在问题区域（环路、孤立节点、重复 path_hash）
-- 注意：
-- - 请在灰度环境验证通过后再在生产执行
-- - 执行前务必备份 t_area_info

-- 假定 t_area_info 结构包含：area_id, parent_id, area_name, path, level, path_hash, deleted_flag

-- 1. 初始化根节点（parent_id 为空或为 0）
UPDATE t_area_info
SET
  path       = CONCAT('/', area_id, '/'),
  level      = 1,
  path_hash  = SHA2(CONCAT('/', area_id, '/'), 256)
WHERE (parent_id IS NULL OR parent_id = 0)
  AND deleted_flag = 0;

-- 2. 迭代回填子节点（示例：最多 10 层，避免死循环）
-- 每次根据父节点已计算好的 path/level 回填一层
SET @max_iterations := 10;
SET @iter := 0;

WHILE @iter < @max_iterations DO
  UPDATE t_area_info AS child
  JOIN t_area_info AS parent ON child.parent_id = parent.area_id
  SET
    child.path      = CONCAT(parent.path, child.area_id, '/'),
    child.level     = parent.level + 1,
    child.path_hash = SHA2(CONCAT(parent.path, child.area_id, '/'), 256)
  WHERE child.deleted_flag = 0
    AND parent.deleted_flag = 0
    AND (child.path IS NULL OR child.level IS NULL OR child.path_hash IS NULL);

  SET @iter := @iter + 1;
END WHILE;

-- 3. 巡检：找出潜在异常数据

-- 3.1 孤立节点：parent_id 不为 0/NULL，但找不到父节点
SELECT child.area_id, child.parent_id, child.area_name
FROM t_area_info AS child
LEFT JOIN t_area_info AS parent ON child.parent_id = parent.area_id
WHERE child.deleted_flag = 0
  AND child.parent_id IS NOT NULL
  AND child.parent_id <> 0
  AND parent.area_id IS NULL;

-- 3.2 环路或自引用：parent_id = area_id
SELECT area_id, parent_id, area_name
FROM t_area_info
WHERE deleted_flag = 0
  AND parent_id = area_id;

-- 3.3 重复 path_hash（理论上一个 path_hash 只对应一条记录）
SELECT path_hash, COUNT(*) AS cnt
FROM t_area_info
WHERE deleted_flag = 0
GROUP BY path_hash
HAVING cnt > 1;

-- 3.4 缺失 path / level / path_hash 的残缺数据
SELECT area_id, parent_id, area_name, path, level, path_hash
FROM t_area_info
WHERE deleted_flag = 0
  AND (path IS NULL OR level IS NULL OR path_hash IS NULL);


