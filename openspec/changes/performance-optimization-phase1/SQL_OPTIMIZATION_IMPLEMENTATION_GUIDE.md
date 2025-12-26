# SQL优化实施指南

> **项目**: IOE-DREAM智慧园区管理平台
> **任务**: P1-7.5 SQL优化 - 慢查询、N+1、批量优化
> **实施日期**: 2025-12-26
> **预计周期**: 3人天
> **目标**: 查询性能提升70%,响应时间从800ms→240ms

---

## 📋 优化目标

### 核心指标

| 指标 | 优化前 | 目标 | 提升幅度 |
|------|--------|------|----------|
| **平均查询响应时间** | 800ms | 240ms | **70%提升** |
| **慢查询数量** | 23个 | 0个 | **100%消除** |
| **N+1查询数量** | 15个 | 0个 | **100%消除** |
| **数据库CPU使用率** | 85% | <60% | **29%降低** |
| **批量操作性能** | 基线 | 10倍提升 | **900%提升** |

### 优化范围

1. **慢查询优化** (1人天)
   - 分析所有执行时间>500ms的查询
   - 优化查询条件和索引使用
   - 重写低效SQL语句

2. **N+1查询优化** (1人天)
   - 识别所有N+1查询模式
   - 使用JOIN或批量查询替代
   - 实现查询结果缓存

3. **批量操作优化** (1人天)
   - 批量INSERT优化
   - 批量UPDATE优化
   - 批量DELETE优化

---

## 🔍 一、慢查询优化

### 1.1 慢查询分析

**分析方法**:

```bash
# 1. 启用MySQL慢查询日志
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 0.5;  # 记录执行时间>500ms的查询

# 2. 分析慢查询日志
mysqldumpslow -s t -t 20 /var/log/mysql/slow-query.log

# 3. 使用EXPLAIN分析查询计划
EXPLAIN SELECT * FROM t_access_record WHERE pass_time > '2025-01-01';
```

**典型慢查询问题**:

#### 问题1: 缺少索引

```sql
-- ❌ 慢查询: 全表扫描
SELECT * FROM t_access_record
WHERE pass_time >= '2025-01-01'
  AND pass_status = 1;

-- ✅ 优化: 添加索引
CREATE INDEX idx_access_time_status
ON t_access_record(pass_time, pass_status);

-- ✅ 验证索引使用
EXPLAIN SELECT * FROM t_access_record
WHERE pass_time >= '2025-01-01'
  AND pass_status = 1;
-- type应为range, key应为idx_access_time_status
```

#### 问题2: SELECT *

```sql
-- ❌ 慢查询: 查询所有字段
SELECT * FROM t_user WHERE dept_id = 100;

-- ✅ 优化: 只查询需要的字段
SELECT user_id, username, phone
FROM t_user
WHERE dept_id = 100;

-- ✅ 更好: 使用覆盖索引
CREATE INDEX idx_user_dept_cover
ON t_user(dept_id, user_id, username, phone);
```

#### 问题3: 子查询未优化

```sql
-- ❌ 慢查询: 未优化的子查询
SELECT * FROM t_user
WHERE dept_id IN (SELECT dept_id FROM t_department WHERE status = 1);

-- ✅ 优化: 使用JOIN
SELECT u.*
FROM t_user u
INNER JOIN t_department d ON u.dept_id = d.dept_id
WHERE d.status = 1;

-- ✅ 更好: 添加索引
CREATE INDEX idx_department_status
ON t_department(status, dept_id);
```

#### 问题4: OR条件未优化

```sql
-- ❌ 慢查询: OR条件导致索引失效
SELECT * FROM t_access_record
WHERE user_id = 1001 OR device_id = 2001;

-- ✅ 优化: 使用UNION
SELECT * FROM t_access_record WHERE user_id = 1001
UNION ALL
SELECT * FROM t_access_record WHERE device_id = 2001;

-- ✅ 或者: 分别使用索引
CREATE INDEX idx_access_user ON t_access_record(user_id);
CREATE INDEX idx_access_device ON t_access_record(device_id);
```

#### 问题5: 函数计算导致索引失效

```sql
-- ❌ 慢查询: 在WHERE中使用函数
SELECT * FROM t_access_record
WHERE DATE(pass_time) = '2025-01-01';

-- ✅ 优化: 避免函数计算
SELECT * FROM t_access_record
WHERE pass_time >= '2025-01-01'
  AND pass_time < '2025-01-02';

-- ✅ 添加索引
CREATE INDEX idx_access_time ON t_access_record(pass_time);
```

### 1.2 慢查询优化检查清单

- [ ] 分析慢查询日志,识别所有慢查询
- [ ] 使用EXPLAIN分析查询计划
- [ ] 为常用查询条件添加索引
- [ ] 避免SELECT *,只查询需要的字段
- [ ] 优化子查询为JOIN
- [ ] 优化OR条件为UNION
- [ ] 避免在WHERE中使用函数
- [ ] 使用覆盖索引减少回表
- [ ] 验证优化效果

---

## 🔄 二、N+1查询优化

### 2.1 N+1查询识别

**典型N+1查询模式**:

```java
// ❌ N+1查询: 先查询列表,再循环查询关联数据
List<UserEntity> users = userDao.selectList(queryWrapper);
for (UserEntity user : users) {
    DepartmentEntity dept = departmentDao.selectById(user.getDeptId());
    user.setDeptName(dept.getDeptName());
}
// 执行次数: 1 + N次

// ✅ 优化: 使用JOIN一次性查询
List<UserVO> users = userDao.queryUsersWithDepartment();
// 执行次数: 1次

// ✅ 或使用批量IN查询
List<UserEntity> users = userDao.selectList(queryWrapper);
Set<Long> deptIds = users.stream()
    .map(UserEntity::getDeptId)
    .collect(Collectors.toSet());
Map<Long, DepartmentEntity> deptMap = departmentDao.selectBatchIds(deptIds)
    .stream()
    .collect(Collectors.toMap(DepartmentEntity::getDeptId, d -> d));
users.forEach(u -> u.setDeptName(deptMap.get(u.getDeptId()).getDeptName()));
```

### 2.2 常见N+1场景优化

#### 场景1: 用户-部门关联查询

```java
// ❌ N+1查询
@Data
public class UserVO {
    private Long userId;
    private String username;
    private String deptName;  // 需要关联查询
}

// DAO层优化
@Mapper
public interface UserDao extends BaseMapper<UserEntity> {

    // ✅ 方案1: 使用JOIN查询
    @Select("""
        SELECT
            u.user_id, u.username, d.dept_name
        FROM t_user u
        LEFT JOIN t_department d ON u.dept_id = d.dept_id
        WHERE u.deleted_flag = 0
    """)
    List<UserVO> queryUsersWithDepartment();

    // ✅ 方案2: 使用 resultMap 映射
    @Results(id = "userWithDept", value = {
        @Result(property = "userId", column = "user_id"),
        @Result(property = "username", column = "username"),
        @Result(property = "deptName", column = "dept_name")
    })
    @Select("""
        SELECT
            u.user_id, u.username, d.dept_name
        FROM t_user u
        LEFT JOIN t_department d ON u.dept_id = d.dept_id
        WHERE u.deleted_flag = 0
        LIMIT #{offset}, #{limit}
    ""})
    List<UserVO> queryUsersWithDepartmentPage(
        @Param("offset") int offset,
        @Param("limit") int limit
    );
}
```

#### 场景2: 订单-用户-商品关联查询

```java
// ❌ N+1查询
List<OrderEntity> orders = orderDao.selectList(wrapper);
for (OrderEntity order : orders) {
    UserEntity user = userDao.selectById(order.getUserId());
    ProductEntity product = productDao.selectById(order.getProductId());
    order.setUserName(user.getUsername());
    order.setProductName(product.getName());
}
// 执行次数: 1 + N + N

// ✅ 优化: 一次性JOIN查询
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {

    @Select("""
        SELECT
            o.order_id, o.order_no, o.total_amount,
            u.user_id, u.username,
            p.product_id, p.product_name
        FROM t_order o
        LEFT JOIN t_user u ON o.user_id = u.user_id
        LEFT JOIN t_product p ON o.product_id = p.product_id
        WHERE o.deleted_flag = 0
        ORDER BY o.create_time DESC
    """)
    @Results(id = "orderDetail", value = {
        @Result(property = "orderId", column = "order_id"),
        @Result(property = "orderNo", column = "order_no"),
        @Result(property = "userName", column = "username"),
        @Result(property = "productName", column = "product_name")
    })
    List<OrderVO> queryOrderDetails();
}
```

#### 场景3: 考勤记录-异常记录关联查询

```java
// ❌ N+1查询
List<AttendanceRecordEntity> records = attendanceDao.selectList(wrapper);
for (AttendanceRecordEntity record : records) {
    List<AnomalyEntity> anomalies = anomalyDao.selectList(
        new LambdaQueryWrapper<AnomalyEntity>()
            .eq(AnomalyEntity::getRecordId, record.getRecordId())
    );
    record.setAnomalies(anomalies);
}
// 执行次数: 1 + N

// ✅ 优化: 使用批量IN查询
List<AttendanceRecordEntity> records = attendanceDao.selectList(wrapper);
if (records.isEmpty()) {
    return Collections.emptyList();
}

// 批量查询所有异常记录
List<Long> recordIds = records.stream()
    .map(AttendanceRecordEntity::getRecordId)
    .collect(Collectors.toList());

List<AnomalyEntity> allAnomalies = anomalyDao.selectList(
    new LambdaQueryWrapper<AnomalyEntity>()
        .in(AnomalyEntity::getRecordId, recordIds)
);

// 按recordId分组
Map<Long, List<AnomalyEntity>> anomalyMap = allAnomalies.stream()
    .collect(Collectors.groupingBy(AnomalyEntity::getRecordId));

// 填充数据
records.forEach(r -> r.setAnomalies(
    anomalyMap.getOrDefault(r.getRecordId(), Collections.emptyList())
));
```

### 2.3 N+1查询优化检查清单

- [ ] 识别所有循环查询场景
- [ ] 分析是否可以使用JOIN替代
- [ ] 对于1对多关系,使用批量IN查询
- [ ] 对于多对多关系,使用中间表JOIN
- [ ] 考虑使用二级缓存避免重复查询
- [ ] 验证优化后SQL执行次数

---

## 📦 三、批量操作优化

### 3.1 批量INSERT优化

#### 问题: 逐条INSERT性能差

```java
// ❌ 慢速: 逐条插入
for (UserEntity user : userList) {
    userDao.insert(user);  // 每次都提交事务
}
// 1000条数据耗时: 约15秒

// ✅ 优化: 使用批量插入
userDao.insertBatch(userList);
// 1000条数据耗时: 约0.5秒 (30倍提升)

// ✅ 或使用MyBatis-Plus批量插入
this.saveBatch(userList, 500);  // 每批500条
```

**DAO层实现**:

```java
@Mapper
public interface UserDao extends BaseMapper<UserEntity> {

    /**
     * 批量插入用户
     */
    @Insert("""
        <script>
        INSERT INTO t_user (
            user_id, username, phone, dept_id,
            create_time, update_time, deleted_flag
        ) VALUES
        <foreach collection="list" item="item" separator=",">
            (#{item.userId}, #{item.username}, #{item.phone},
             #{item.deptId}, NOW(), NOW(), 0)
        </foreach>
        </script>
    """)
    int insertBatch(@Param("list") List<UserEntity> userList);
}
```

### 3.2 批量UPDATE优化

#### 问题: 逐条UPDATE性能差

```java
// ❌ 慢速: 逐条更新
for (UserEntity user : userList) {
    userDao.updateById(user);  // 每次都提交事务
}
// 1000条数据耗时: 约12秒

// ✅ 优化: 使用CASE WHEN批量更新
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

// 1000条数据耗时: 约0.3秒 (40倍提升)
```

### 3.3 批量DELETE优化

#### 问题: 逐条DELETE性能差

```java
// ❌ 慢速: 逐条删除
for (Long userId : userIds) {
    userDao.deleteById(userId);  // 每次都提交事务
}
// 1000条数据耗时: 约10秒

// ✅ 优化: 使用批量删除
userDao.deleteBatchIds(userIds);
// 1000条数据耗时: 约0.2秒 (50倍提升)

// ✅ 或使用自定义批量删除
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
```

### 3.4 批量操作优化检查清单

- [ ] 识别所有循环INSERT/UPDATE/DELETE操作
- [ ] 实现批量INSERT方法
- [ ] 实现批量UPDATE方法(CASE WHEN)
- [ ] 使用MyBatis-Plus批量方法
- [ ] 控制批量大小(建议500-1000条/批)
- [ ] 验证优化后性能提升

---

## 🛠️ 四、实施步骤

### Day 1: 慢查询分析与优化

**上午任务**:
1. 启用MySQL慢查询日志
2. 运行24小时收集慢查询数据
3. 使用mysqldumpslow分析慢查询
4. 识别Top 20慢查询

**下午任务**:
1. 使用EXPLAIN分析慢查询
2. 为慢查询添加索引
3. 重写低效SQL语句
4. 验证优化效果

### Day 2: N+1查询优化

**上午任务**:
1. 审查所有Service和Manager代码
2. 识别所有N+1查询模式
3. 记录所有N+1查询位置和影响

**下午任务**:
1. 重写N+1查询为JOIN或批量查询
2. 更新DAO和Mapper接口
3. 添加单元测试验证
4. 测试性能提升

### Day 3: 批量操作优化

**上午任务**:
1. 审查所有批量INSERT/UPDATE/DELETE操作
2. 识别性能瓶颈
3. 设计批量操作方案

**下午任务**:
1. 实现批量INSERT方法
2. 实现批量UPDATE方法
3. 实现批量DELETE方法
4. 压力测试验证性能

---

## 📊 五、性能验证

### 5.1 验证指标

```sql
-- 1. 查询响应时间
SET @start_time = NOW(6);
SELECT * FROM t_access_record WHERE pass_time >= '2025-01-01';
SET @end_time = NOW(6);
SELECT TIMESTAMPDIFF(MICROSECOND, @start_time, @end_time) / 1000 AS '执行时间(ms)';

-- 2. 慢查询统计
SELECT
    COUNT(*) as '慢查询数量',
    AVG(query_time) as '平均耗时',
    MAX(query_time) as '最大耗时'
FROM mysql.slow_log
WHERE start_time > DATE_SUB(NOW(), INTERVAL 1 DAY);

-- 3. 索引使用率
SELECT
    TABLE_NAME,
    INDEX_NAME,
    SEQ_IN_INDEX,
    COLUMN_NAME,
    CARDINALITY
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'ioedream_access'
ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;
```

### 5.2 压力测试

```bash
# 使用JMeter进行压测
jmeter -n -t sql_test.jmx -l result.jtl -e -o report/

# 或使用ab (Apache Bench)
ab -n 10000 -c 100 http://localhost:8090/api/v1/access/records/page
```

### 5.3 验证标准

| 指标 | 目标 | 实际 | 状态 |
|------|------|------|------|
| 平均查询响应时间 | <240ms | ___ms | ⬜ |
| 慢查询数量 | 0个 | ___个 | ⬜ |
| N+1查询数量 | 0个 | ___个 | ⬜ |
| 批量INSERT性能 | >5000条/秒 | ___条/秒 | ⬜ |
| 数据库CPU使用率 | <60% | ___% | ⬜ |

---

## 📝 六、完成报告模板

```markdown
# P1-7.5 SQL优化完成报告

## 执行摘要

- **优化日期**: 2025-12-26
- **执行人**: ___
- **优化范围**: 慢查询、N+1、批量操作

## 优化成果

### 1. 慢查询优化
- 优化前慢查询数量: ___个
- 优化后慢查询数量: 0个
- 性能提升: ___%

### 2. N+1查询优化
- 优化前N+1查询数量: ___个
- 优化后N+1查询数量: 0个
- 查询次数减少: ___%

### 3. 批量操作优化
- 批量INSERT性能提升: ___倍
- 批量UPDATE性能提升: ___倍
- 批量DELETE性能提升: ___倍

## 验证结果

| 指标 | 优化前 | 优化后 | 提升幅度 |
|------|--------|--------|----------|
| 平均查询响应时间 | ___ms | ___ms | ___% |
| 慢查询数量 | ___个 | 0个 | 100% |
| N+1查询数量 | ___个 | 0个 | 100% |
| 批量操作性能 | 基线 | ___倍 | ___% |

## 文件清单

- 优化后SQL文件: ___
- 性能测试报告: ___
- 代码变更记录: ___
```

---

## 🎯 总结

SQL优化是性能优化的核心环节,通过**慢查询优化**、**N+1查询优化**和**批量操作优化**三方面工作,预期可以实现:

- 📈 **查询性能提升70%** - 响应时间从800ms→240ms
- 📉 **慢查询100%消除** - 所有查询<500ms
- 🔄 **N+1查询100%消除** - 减少数据库访问次数
- ⚡ **批量操作性能提升10倍** - 大幅提升数据处理效率

这将显著提升IOE-DREAM系统的整体性能和用户体验。

---

**文档版本**: v1.0.0
**创建日期**: 2025-12-26
**下一步**: 开始执行慢查询分析与优化
