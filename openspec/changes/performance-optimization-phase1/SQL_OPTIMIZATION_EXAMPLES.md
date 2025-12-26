# SQL优化示例集合

> **项目**: IOE-DREAM智慧园区管理平台
> **文档**: SQL优化常见模式和解决方案
> **版本**: v1.0.0
> **日期**: 2025-12-26

---

## 📋 目录

1. [索引优化示例](#1-索引优化示例)
2. [查询重写示例](#2-查询重写示例)
3. [N+1查询优化示例](#3-n1查询优化示例)
4. [批量操作示例](#4-批量操作示例)
5. [分页查询优化示例](#5-分页查询优化示例)

---

## 1. 索引优化示例

### 1.1 单列索引 vs 复合索引

**问题**: 多个单列索引不如一个复合索引

```sql
-- ❌ 优化前: 多个单列索引
CREATE INDEX idx_access_time ON t_access_record(pass_time);
CREATE INDEX idx_access_status ON t_access_record(pass_status);
CREATE INDEX idx_access_user ON t_access_record(user_id);

-- 查询: 只能使用部分索引
SELECT * FROM t_access_record
WHERE user_id = 1001
  AND pass_time >= '2025-01-01'
  AND pass_status = 1;
-- 使用索引: idx_access_user (只能用user_id部分)

-- ✅ 优化后: 复合索引(最左前缀原则)
CREATE INDEX idx_access_user_time_status
ON t_access_record(user_id, pass_time, pass_status);

-- 查询: 可以完整使用复合索引
SELECT * FROM t_access_record
WHERE user_id = 1001
  AND pass_time >= '2025-01-01'
  AND pass_status = 1;
-- 使用索引: idx_access_user_time_status (完全使用)

-- 验证索引使用效果
EXPLAIN SELECT * FROM t_access_record
WHERE user_id = 1001 AND pass_time >= '2025-01-01' AND pass_status = 1;
-- type: range
-- key: idx_access_user_time_status
-- rows: 扫描行数显著减少
```

### 1.2 覆盖索引优化

**问题**: 即使有索引,也需要回表查询

```sql
-- ❌ 优化前: 普通索引
CREATE INDEX idx_user_dept ON t_user(dept_id);

-- 查询: 需要回表获取username, phone
SELECT username, phone FROM t_user WHERE dept_id = 100;
-- 执行过程: 1. 使用idx_user_dept找到所有user_id
--          2. 回表查询每一行的username, phone

-- ✅ 优化后: 覆盖索引(包含所有查询字段)
CREATE INDEX idx_user_dept_cover
ON t_user(dept_id, username, phone);

-- 查询: 直接从索引获取所有数据,无需回表
SELECT username, phone FROM t_user WHERE dept_id = 100;
-- 执行过程: 直接从索引获取username, phone
-- Extra: Using index (覆盖索引)

-- 性能提升: 查询速度提升2-3倍
```

### 1.3 函数索引优化

**问题**: 在WHERE中使用函数导致索引失效

```sql
-- ❌ 优化前: 函数导致索引失效
CREATE INDEX idx_access_time ON t_access_record(pass_time);

-- 查询: 函数破坏索引
SELECT * FROM t_access_record
WHERE DATE(pass_time) = '2025-01-01';
-- type: ALL (全表扫描)
-- key: NULL (未使用索引)

-- ✅ 优化方案1: 重写查询条件
SELECT * FROM t_access_record
WHERE pass_time >= '2025-01-01'
  AND pass_time < '2025-01-02';
-- type: range
-- key: idx_access_time

-- ✅ 优化方案2: 使用函数索引(MySQL 8.0+)
CREATE INDEX idx_access_date_func
ON t_access_record((DATE(pass_time)));

-- 查询: 可以使用函数索引
SELECT * FROM t_access_record
WHERE DATE(pass_time) = '2025-01-01';
-- type: ref
-- key: idx_access_date_func

-- 性能提升: 查询速度提升10-50倍
```

---

## 2. 查询重写示例

### 2.1 子查询优化为JOIN

**问题**: 子查询性能差

```sql
-- ❌ 优化前: 子查询
SELECT *
FROM t_user
WHERE dept_id IN (
    SELECT dept_id FROM t_department WHERE status = 1
);
-- 执行过程: 1. 执行子查询获取dept_id列表
--          2. 外层查询遍历所有用户
-- 性能: 子查询可能被执行多次

-- ✅ 优化后: JOIN查询
SELECT u.*
FROM t_user u
INNER JOIN t_department d ON u.dept_id = d.dept_id
WHERE d.status = 1;
-- 执行过程: 一次性JOIN查询
-- 性能: 显著提升,特别是大数据量时

-- 添加索引优化
CREATE INDEX idx_department_status ON t_department(status, dept_id);
CREATE INDEX idx_user_dept ON t_user(dept_id);

-- 验证优化效果
EXPLAIN SELECT u.* FROM t_user u
INNER JOIN t_department d ON u.dept_id = d.dept_id
WHERE d.status = 1;
-- type: eq_ref (最优JOIN类型)
-- rows: 扫描行数显著减少

-- 性能提升: 查询速度提升5-20倍
```

### 2.2 OR条件优化

**问题**: OR条件导致索引失效

```sql
-- ❌ 优化前: OR条件
SELECT * FROM t_access_record
WHERE user_id = 1001 OR device_id = 2001;
-- 执行过程: 可能全表扫描
-- type: ALL

-- ✅ 优化方案1: 使用UNION ALL
SELECT * FROM t_access_record WHERE user_id = 1001
UNION ALL
SELECT * FROM t_access_record WHERE device_id = 2001;
-- 执行过程: 分别使用索引
-- 性能: 显著提升

-- 添加索引
CREATE INDEX idx_access_user ON t_access_record(user_id);
CREATE INDEX idx_access_device ON t_access_record(device_id);

-- 验证索引使用
EXPLAIN SELECT * FROM t_access_record WHERE user_id = 1001;
-- type: const (使用主键或唯一索引)

EXPLAIN SELECT * FROM t_access_record WHERE device_id = 2001;
-- type: ref (使用二级索引)

-- ✅ 优化方案2: 使用IF条件(CASE WHEN)
SELECT *
FROM t_access_record
WHERE (user_id = 1001 AND user_id IS NOT NULL)
   OR (device_id = 2001 AND device_id IS NOT NULL);
-- 某些情况下优化器可以更好优化

-- 性能提升: 查询速度提升3-10倍
```

### 2.3 SELECT * 优化

**问题**: 查询所有字段浪费资源

```sql
-- ❌ 优化前: SELECT *
SELECT * FROM t_user WHERE dept_id = 100;
-- 问题: 1. 查询所有字段(可能包含大字段)
--       2. 网络传输开销大
--       3. 无法使用覆盖索引

-- ✅ 优化后: 只查询需要的字段
SELECT user_id, username, phone
FROM t_user
WHERE dept_id = 100;
-- 优势: 1. 减少数据传输量
--       2. 可以使用覆盖索引
--       3. 减少内存占用

-- 创建覆盖索引
CREATE INDEX idx_user_dept_cover
ON t_user(dept_id, user_id, username, phone);

-- 验证优化效果
EXPLAIN SELECT user_id, username, phone
FROM t_user WHERE dept_id = 100;
-- Extra: Using index (覆盖索引)

-- 性能提升:
-- - 查询速度提升2-5倍
-- - 网络传输减少70-90%
-- - 内存占用减少50-80%
```

---

## 3. N+1查询优化示例

### 3.1 一对一关联优化

**问题**: 查询用户时逐个查询部门信息

```java
// ❌ 优化前: N+1查询
List<UserEntity> users = userDao.selectList(
    new LambdaQueryWrapper<UserEntity>()
        .eq(UserEntity::getDeptId, 100)
);

for (UserEntity user : users) {
    DepartmentEntity dept = departmentDao.selectById(user.getDeptId());
    user.setDeptName(dept.getDeptName());
}
// 执行次数: 1 + N次

// ✅ 优化方案1: 使用JOIN查询
@Mapper
public interface UserDao extends BaseMapper<UserEntity> {

    @Select("""
        SELECT
            u.user_id, u.username, u.phone,
            d.dept_id, d.dept_name
        FROM t_user u
        LEFT JOIN t_department d ON u.dept_id = d.dept_id
        WHERE u.dept_id = #{deptId}
    """)
    @Results(id = "userWithDept", value = {
        @Result(property = "userId", column = "user_id"),
        @Result(property = "username", column = "username"),
        @Result(property = "phone", column = "phone"),
        @Result(property = "deptName", column = "dept_name")
    })
    List<UserVO> queryUsersWithDepartment(@Param("deptId") Long deptId);
}

// ✅ 优化方案2: 使用批量IN查询
List<UserEntity> users = userDao.selectList(
    new LambdaQueryWrapper<UserEntity>()
        .eq(UserEntity::getDeptId, 100)
);

Set<Long> deptIds = users.stream()
    .map(UserEntity::getDeptId)
    .collect(Collectors.toSet());

Map<Long, DepartmentEntity> deptMap = departmentDao.selectBatchIds(deptIds)
    .stream()
    .collect(Collectors.toMap(DepartmentEntity::getDeptId, d -> d));

users.forEach(u -> u.setDeptName(deptMap.get(u.getDeptId()).getDeptName()));
// 执行次数: 2次

// 性能提升: 查询时间从O(N)降至O(1)
```

### 3.2 一对多关联优化

**问题**: 查询部门时逐个查询用户列表

```java
// ❌ 优化前: N+1查询
List<DepartmentEntity> depts = departmentDao.selectList(null);

for (DepartmentEntity dept : depts) {
    List<UserEntity> users = userDao.selectList(
        new LambdaQueryWrapper<UserEntity>()
            .eq(UserEntity::getDeptId, dept.getDeptId())
    );
    dept.setUsers(users);
}
// 执行次数: 1 + N次

// ✅ 优化: 批量查询+内存分组
List<DepartmentEntity> depts = departmentDao.selectList(null);

if (depts.isEmpty()) {
    return Collections.emptyList();
}

// 批量查询所有用户
Set<Long> deptIds = depts.stream()
    .map(DepartmentEntity::getDeptId)
    .collect(Collectors.toSet());

List<UserEntity> allUsers = userDao.selectList(
    new LambdaQueryWrapper<UserEntity>()
        .in(UserEntity::getDeptId, deptIds)
);

// 按deptId分组
Map<Long, List<UserEntity>> userMap = allUsers.stream()
    .collect(Collectors.groupingBy(UserEntity::getDeptId));

// 填充数据
depts.forEach(d -> d.setUsers(
    userMap.getOrDefault(d.getDeptId(), Collections.emptyList())
));
// 执行次数: 2次

// 性能提升: 查询时间从O(N)降至O(1)
```

### 3.3 多对多关联优化

**问题**: 查询角色权限时逐个查询权限列表

```java
// ❌ 优化前: N+1查询
List<RoleEntity> roles = roleDao.selectList(null);

for (RoleEntity role : roles) {
    List<PermissionEntity> permissions = permissionDao.selectList(
        new LambdaQueryWrapper<PermissionEntity>()
            .in(PermissionEntity::getPermissionId,
                selectPermissionIdsByRoleId(role.getRoleId()))
    );
    role.setPermissions(permissions);
}
// 执行次数: 1 + N + N次

// ✅ 优化: 使用中间表JOIN
@Mapper
public interface RoleDao extends BaseMapper<RoleEntity> {

    @Select("""
        SELECT DISTINCT
            r.role_id, r.role_name,
            p.permission_id, p.permission_code, p.permission_name
        FROM t_role r
        LEFT JOIN t_role_permission rp ON r.role_id = rp.role_id
        LEFT JOIN t_permission p ON rp.permission_id = p.permission_id
        WHERE r.deleted_flag = 0
        ORDER BY r.role_id, p.permission_id
    """)
    @Results(id = "roleWithPermissions", value = {
        @Result(property = "roleId", column = "role_id"),
        @Result(property = "roleName", column = "role_name"),
        @Result(property = "permissions", column = "role_id",
                many = @Many(select = "findPermissionsByRoleId"))
    })
    List<RoleVO> queryRolesWithPermissions();

    @Select("""
        SELECT p.permission_id, p.permission_code, p.permission_name
        FROM t_permission p
        LEFT JOIN t_role_permission rp ON p.permission_id = rp.permission_id
        WHERE rp.role_id = #{roleId}
    "")
    List<PermissionEntity> findPermissionsByRoleId(@Param("roleId") Long roleId);
}
// 执行次数: 1 + N次(使用@Many批量加载)

// 性能提升: 查询时间从O(N²)降至O(N)
```

---

## 4. 批量操作示例

### 4.1 批量INSERT优化

**问题**: 逐条插入性能差

```java
// ❌ 优化前: 逐条插入
for (UserEntity user : userList) {
    userDao.insert(user);
}
// 1000条数据耗时: 约15秒

// ✅ 优化方案1: MyBatis-Plus批量插入
userDao.insertBatch(userList);
// 1000条数据耗时: 约0.5秒

// ✅ 优化方案2: 自定义批量插入SQL
@Mapper
public interface UserDao extends BaseMapper<UserEntity> {

    @Insert("""
        <script>
        INSERT INTO t_user (
            user_id, username, phone, dept_id,
            create_time, update_time, deleted_flag
        ) VALUES
        <foreach collection="list" item="item" separator=",">
            (
                #{item.userId}, #{item.username}, #{item.phone},
                #{item.deptId}, NOW(), NOW(), 0
            )
        </foreach>
        </script>
    """)
    int insertBatch(@Param("list") List<UserEntity> userList);
}

// ✅ 优化方案3: 分批插入(大数据量)
@Service
public class UserServiceImpl {

    private static final int BATCH_SIZE = 500;

    public void batchInsertUsers(List<UserEntity> userList) {
        if (userList == null || userList.isEmpty()) {
            return;
        }

        int total = userList.size();
        for (int i = 0; i < total; i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, total);
            List<UserEntity> batch = userList.subList(i, end);
            userDao.insertBatch(batch);
            log.info("[批量插入] 已插入: {}/{}", end, total);
        }
    }
}
// 10000条数据耗时: 约3秒

// 性能提升: 30-50倍
```

### 4.2 批量UPDATE优化

**问题**: 逐条更新性能差

```java
// ❌ 优化前: 逐条更新
for (UserEntity user : userList) {
    userDao.updateById(user);
}
// 1000条数据耗时: 约12秒

// ✅ 优化方案: CASE WHEN批量更新
@Mapper
public interface UserDao extends BaseMapper<UserEntity> {

    @Update("""
        <script>
        UPDATE t_user
        <trim prefix="SET" suffixOverrides=",">
            <trim prefix="username = CASE" suffix="END,">
                <foreach collection="list" item="item">
                    WHEN user_id = #{item.userId} THEN #{item.username}
                </foreach>
            </trim>
            <trim prefix="phone = CASE" suffix="END,">
                <foreach collection="list" item="item">
                    WHEN user_id = #{item.userId} THEN #{item.phone}
                </foreach>
            </trim>
            update_time = NOW()
        </trim>
        WHERE user_id IN
        <foreach collection="list" item="item" open="(" separator="," close=")">
            #{item.userId}
        </foreach>
        </script>
    """)
    int updateBatch(@Param("list") List<UserEntity> userList);
}
// 1000条数据耗时: 约0.3秒

// ✅ 分批更新(大数据量)
@Service
public class UserServiceImpl {

    private static final int BATCH_SIZE = 500;

    public void batchUpdateUsers(List<UserEntity> userList) {
        if (userList == null || userList.isEmpty()) {
            return;
        }

        int total = userList.size();
        for (int i = 0; i < total; i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, total);
            List<UserEntity> batch = userList.subList(i, end);
            userDao.updateBatch(batch);
            log.info("[批量更新] 已更新: {}/{}", end, total);
        }
    }
}
// 10000条数据耗时: 约2秒

// 性能提升: 40-60倍
```

### 4.3 批量DELETE优化

**问题**: 逐条删除性能差

```java
// ❌ 优化前: 逐条删除
for (Long userId : userIds) {
    userDao.deleteById(userId);
}
// 1000条数据耗时: 约10秒

// ✅ 优化方案1: MyBatis-Plus批量删除
userDao.deleteBatchIds(userIds);
// 1000条数据耗时: 约0.2秒

// ✅ 优化方案2: 自定义批量删除
@Mapper
public interface UserDao extends BaseMapper<UserEntity> {

    @Delete("""
        <script>
        DELETE FROM t_user
        WHERE user_id IN
        <foreach collection="list" item="item" open="(" separator="," close=")">
            #{item}
        </foreach>
        </script>
    """)
    int deleteBatch(@Param("list") List<Long> userIds);
}
// 1000条数据耗时: 约0.2秒

// ✅ 优化方案3: 条件批量删除
@Delete("""
        DELETE FROM t_user
        WHERE dept_id = #{deptId}
          AND create_time < #{beforeDate}
    """)
    int deleteByCondition(
        @Param("deptId") Long deptId,
        @Param("beforeDate") LocalDateTime beforeDate
    );
// 删除大量数据时性能更好

// 性能提升: 50-100倍
```

---

## 5. 分页查询优化示例

### 5.1 深度分页优化

**问题**: LIMIT offset, size在大offset时性能差

```sql
-- ❌ 优化前: 深度分页
SELECT * FROM t_access_record
ORDER BY pass_time DESC
LIMIT 10000, 20;
-- 执行过程: 1. 扫描前10020条记录
--          2. 丢弃前10000条
--          3. 返回后20条
-- 性能: offset越大,查询越慢

-- ✅ 优化方案1: 游标分页(推荐)
-- 第一页
SELECT * FROM t_access_record
ORDER BY pass_time DESC
LIMIT 20;

-- 第二页(使用上一页最后一条记录的pass_time作为游标)
SELECT * FROM t_access_record
WHERE pass_time < #{lastPassTime}
ORDER BY pass_time DESC
LIMIT 20;

-- 添加索引
CREATE INDEX idx_access_time_desc
ON t_access_record(pass_time DESC);

-- 性能: 恒定时间,不受深度影响

-- ✅ 优化方案2: 延迟关联
SELECT ar.*
FROM t_access_record ar
INNER JOIN (
    SELECT record_id FROM t_access_record
    ORDER BY pass_time DESC
    LIMIT 10000, 20
) tmp ON ar.record_id = tmp.record_id;

-- 性能: 比直接LIMIT快2-3倍

-- 性能提升: 深度分页性能提升10-100倍
```

### 5.2 count查询优化

**问题**: COUNT(*)在大表上很慢

```sql
-- ❌ 优化前: 直接COUNT
SELECT COUNT(*) FROM t_access_record;
-- 大表上执行很慢

-- ✅ 优化方案1: 使用覆盖索引
SELECT COUNT(record_id) FROM t_access_record;
-- 只统计索引字段,不回表

-- ✅ 优化方案2: 近似计数
SELECT TABLE_ROWS
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'ioedream_access'
  AND TABLE_NAME = 't_access_record';
-- 使用统计信息,速度快但不精确

-- ✅ 优化方案3: 缓存COUNT结果
@Cacheable(value = "count", key = "'access_record_count'")
public Long countAccessRecords() {
    return userDao.selectCount(null);
}

-- ✅ 优化方案4: 维护计数表
CREATE TABLE t_count_cache (
    table_name VARCHAR(100) PRIMARY KEY,
    row_count BIGINT,
    update_time DATETIME
);

-- 定时更新计数
-- CREATE EVENT update_count_cache ...
-- 或在INSERT/DELETE时更新计数

-- 性能提升: 查询速度提升10-1000倍
```

---

## 📊 性能对比总结

| 优化类型 | 优化前 | 优化后 | 提升幅度 |
|---------|--------|--------|----------|
| **索引优化** | 800ms | 150ms | **81%↑** |
| **查询重写** | 全表扫描 | 索引扫描 | **10-100倍↑** |
| **N+1查询** | 1+N次 | 1-2次 | **50-90%↓** |
| **批量INSERT** | 15秒 | 0.5秒 | **30倍↑** |
| **批量UPDATE** | 12秒 | 0.3秒 | **40倍↑** |
| **批量DELETE** | 10秒 | 0.2秒 | **50倍↑** |
| **深度分页** | 指数增长 | 恒定时间 | **10-100倍↑** |

---

## 🎯 最佳实践总结

1. **索引设计**
   - ✅ 为常用查询条件添加索引
   - ✅ 使用复合索引遵循最左前缀
   - ✅ 使用覆盖索引减少回表
   - ❌ 避免在索引列上使用函数

2. **查询编写**
   - ✅ 避免SELECT *,只查询需要的字段
   - ✅ 优化子查询为JOIN
   - ✅ 使用UNION代替OR
   - ✅ 使用EXPLAIN分析查询计划

3. **N+1查询**
   - ✅ 使用JOIN一次性查询关联数据
   - ✅ 使用批量IN查询
   - ✅ 在内存中分组和组装数据
   - ❌ 避免循环查询

4. **批量操作**
   - ✅ 使用批量INSERT/UPDATE/DELETE
   - ✅ 控制批量大小(500-1000条/批)
   - ✅ 大批量分批处理
   - ❌ 避免逐条操作

5. **分页查询**
   - ✅ 使用游标分页代替深度分页
   - ✅ 使用延迟关联优化
   - ✅ 缓存COUNT结果
   - ❌ 避免大offset的LIMIT

---

**文档版本**: v1.0.0
**创建日期**: 2025-12-26
**相关文档**:
- SQL_OPTIMIZATION_IMPLEMENTATION_GUIDE.md
- DATABASE_PERFORMANCE_OPTIMIZATION_SUMMARY.md
