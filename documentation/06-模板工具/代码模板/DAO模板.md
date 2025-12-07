# DAO层代码模板

**用途**: 标准的DAO层数据访问代码模板
**适用场景**: 所有业务模块的DAO层开发
**版本**: v2.0.0 (基于IOE-DREAM架构规范)

---

## 📋 基础DAO模板

### 标准MyBatis-Plus DAO模板

```java
package net.lab1024.sa.{module}.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.{module}.domain.entity.{Entity}Entity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * {模块名称}DAO接口
 *
 * @author IOE-DREAM Team
 * @since 2025-12-02
 */
@Mapper
public interface {Entity}Dao extends BaseMapper<{Entity}Entity> {

    /**
     * 根据用户ID查询{实体名称}
     *
     * @param userId 用户ID
     * @return {实体名称}列表
     */
    @Select("SELECT * FROM t_{module}_{entity} " +
            "WHERE user_id = #{userId} AND deleted_flag = false " +
            "ORDER BY create_time DESC")
    List<{Entity}Entity> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据状态查询{实体名称}
     *
     * @param status 状态
     * @return {实体名称}列表
     */
    @Select("SELECT * FROM t_{module}_{entity} " +
            "WHERE status = #{status} AND deleted_flag = false " +
            "ORDER BY create_time DESC")
    List<{Entity}Entity> selectByStatus(@Param("status") Integer status);

    /**
     * 根据名称模糊查询{实体名称}
     *
     * @param name 名称关键词
     * @return {实体名称}列表
     */
    @Select("SELECT * FROM t_{module}_{entity} " +
            "WHERE name LIKE CONCAT('%', #{name}, '%') AND deleted_flag = false " +
            "ORDER BY create_time DESC " +
            "LIMIT 100")
    List<{Entity}Entity> selectByNameLike(@Param("name") String name);

    /**
     * 查询指定时间范围内的{实体名称}
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return {实体名称}列表
     */
    @Select("SELECT * FROM t_{module}_{entity} " +
            "WHERE create_time >= #{startTime} AND create_time <= #{endTime} " +
            "AND deleted_flag = false " +
            "ORDER BY create_time DESC")
    List<{Entity}Entity> selectByTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 根据条件查询{实体名称}数量
     *
     * @param userId 用户ID (可选)
     * @param status 状态 (可选)
     * @return 数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM t_{module}_{entity} WHERE deleted_flag = false " +
            "<if test='userId != null'> AND user_id = #{userId} </if>" +
            "<if test='status != null'> AND status = #{status} </if>" +
            "</script>")
    Long countByCondition(@Param("userId") Long userId,
                           @Param("status") Integer status);

    /**
     * 批量更新{实体名称}状态
     *
     * @param ids ID列表
     * @param status 新状态
     * @param updateUserId 更新人ID
     * @return 更新数量
     */
    @Update("<script>" +
            "UPDATE t_{module}_{entity} " +
            "SET status = #{status}, " +
            "    update_time = NOW(), " +
            "    update_user_id = #{updateUserId} " +
            "WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "AND deleted_flag = false" +
            "</script>")
    int batchUpdateStatus(@Param("ids") List<Long> ids,
                         @Param("status") Integer status,
                         @Param("updateUserId") Long updateUserId);

    /**
     * 批量软删除{实体名称}
     *
     * @param ids ID列表
     * @return 删除数量
     */
    @Update("<script>" +
            "UPDATE t_{module}_{entity} " +
            "SET deleted_flag = true, " +
            "    update_time = NOW() " +
            "WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "AND deleted_flag = false" +
            "</script>")
    int batchDelete(@Param("ids") List<Long> ids);

    /**
     * 检查名称是否存在
     *
     * @param name 名称
     * @param excludeId 排除的ID (用于更新时检查)
     * @return 数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM t_{module}_{entity} " +
            "WHERE name = #{name} AND deleted_flag = false " +
            "<if test='excludeId != null'> AND id != #{excludeId} </if>" +
            "</script>")
    Long countByName(@Param("name") String name,
                     @Param("excludeId") Long excludeId);

    /**
     * 获取最新的{实体名称}
     *
     * @param limit 数量限制
     * @return {实体名称}列表
     */
    @Select("SELECT * FROM t_{module}_{entity} " +
            "WHERE deleted_flag = false " +
            "ORDER BY create_time DESC " +
            "LIMIT #{limit}")
    List<{Entity}Entity> selectLatest(@Param("limit") Integer limit);

    /**
     * 统计指定时间内的{实体名称}
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计数量
     */
    @Select("SELECT COUNT(*) FROM t_{module}_{entity} " +
            "WHERE create_time >= #{startTime} AND create_time <= #{endTime} " +
            "AND deleted_flag = false")
    Long statisticsByTimeRange(@Param("startTime") LocalDateTime startTime,
                                @Param("endTime") LocalDateTime endTime);
}
```

---

## 🔧 高级DAO模板
## ⚠️ IOE-DREAM零容忍规则（强制执行）

**必须遵守的架构规则**:
- ✅ **必须使用 @Resource 注入依赖**
- ✅ **必须使用 @Mapper 注解** (禁止@Repository)
- ✅ **必须使用 Dao 后缀** (禁止Repository)
- ✅ **必须使用 @RestController 注解**
- ✅ **必须使用 @Valid 参数校验**
- ✅ **必须返回统一ResponseDTO格式**
- ✅ **必须遵循四层架构边界**

**严格禁止事项**:
- ❌ **禁止使用 @Autowired 注入**
- ❌ **禁止使用 @Repository 注解**
- ❌ **禁止使用 Repository 后缀命名**
- ❌ **禁止跨层访问**
- ❌ **禁止在Controller中包含业务逻辑**
- ❌ **禁止直接访问数据库**

**违规后果**: P0级问题，立即修复，禁止合并！
 
## 🏗️ 四层架构规范

**标准架构模式**:
```
Controller (接口控制层)
    ↓
Service (核心业务层)
    ↓
Manager (流程管理层)
    ↓
DAO (数据访问层)
```

**层级职责**:
- **Controller层**: HTTP请求处理、参数验证、权限控制
- **Service层**: 核心业务逻辑、事务管理、业务规则验证
- **Manager层**: 复杂流程编排、多数据组装、第三方服务集成
- **DAO层**: 数据库CRUD操作、SQL查询实现、数据访问边界

**严格禁止跨层访问**: Controller不能直接调用Manager/DAO！
 
## 📋 IOE-DREAM七微服务架构

**核心架构组成**:
- **Gateway Service (8080)**: API网关
- **Common Service (8088)**: 公共模块微服务
- **DeviceComm Service (8087)**: 设备通讯微服务
- **OA Service (8089)**: OA微服务
- **Access Service (8090)**: 门禁服务
- **Attendance Service (8091)**: 考勤服务
- **Video Service (8092)**: 视频服务
- **Consume Service (8094)**: 消费服务
- **Visitor Service (8095)**: 访客服务

**架构特点**:
- 基于Spring Boot 3.5.8 + Java 17
- 严格遵循企业级微服务规范
- 支持高并发、高可用、水平扩展

### 带复杂查询的DAO模板

```java
package net.lab1024.sa.{module}.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.{module}.domain.entity.{Entity}Entity;
import net.lab1024.sa.{module}.domain.vo.{Entity}StatisticsVO;
import net.lab1024.sa.{module}.domain.query.{Entity}QueryForm;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * {模块名称}DAO接口 (高级查询)
 *
 * @author IOE-DREAM Team
 * @since 2025-12-02
 */
@Mapper
public interface {Entity}Dao extends BaseMapper<{Entity}Entity> {

    /**
     * 复杂条件分页查询
     *
     * @param page 分页对象
     * @param queryForm 查询条件
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT * FROM t_{module}_{entity} " +
            "WHERE deleted_flag = false " +
            "<if test='queryForm.name != null and queryForm.name != \"\"'>" +
            "AND name LIKE CONCAT('%', #{queryForm.name}, '%') " +
            "</if>" +
            "<if test='queryForm.status != null'>" +
            "AND status = #{queryForm.status} " +
            "</if>" +
            "<if test='queryForm.userId != null'>" +
            "AND user_id = #{queryForm.userId} " +
            "</if>" +
            "<if test='queryForm.startTime != null'>" +
            "AND create_time >= #{queryForm.startTime} " +
            "</if>" +
            "<if test='queryForm.endTime != null'>" +
            "AND create_time <= #{queryForm.endTime} " +
            "</if>" +
            "ORDER BY " +
            "<choose>" +
            "<when test='queryForm.orderBy != null'>" +
            "#{queryForm.orderBy} " +
            "<if test='queryForm.orderDirection != null'>" +
            "#{queryForm.orderDirection} " +
            "</if>" +
            "</when>" +
            "<otherwise>" +
            "create_time DESC " +
            "</otherwise>" +
            "</choose>" +
            "</script>")
    IPage<{Entity}Entity> selectPageByCondition(Page<{Entity}Entity> page,
                                                   @Param("queryForm") {Entity}QueryForm queryForm);

    /**
     * 统计{实体名称}数据
     *
     * @param queryForm 查询条件
     * @return 统计结果
     */
    @Select("<script>" +
            "SELECT " +
            "COUNT(*) as totalCount, " +
            "COUNT(CASE WHEN status = 1 THEN 1 END) as activeCount, " +
            "COUNT(CASE WHEN status = 2 THEN 1 END) as inactiveCount, " +
            "COUNT(CASE WHEN status = 3 THEN 1 END) as suspendedCount " +
            "FROM t_{module}_{entity} " +
            "WHERE deleted_flag = false " +
            "<if test='queryForm.userId != null'>" +
            "AND user_id = #{queryForm.userId} " +
            "</if>" +
            "<if test='queryForm.startTime != null'>" +
            "AND create_time >= #{queryForm.startTime} " +
            "</if>" +
            "<if test='queryForm.endTime != null'>" +
            "AND create_time <= #{queryForm.endTime} " +
            "</if>" +
            "</script>")
    @Results(id = "statisticsResult", value = {
        @Result(property = "totalCount", column = "totalCount"),
        @Result(property = "activeCount", column = "activeCount"),
        @Result(property = "inactiveCount", column = "inactiveCount"),
        @Result(property = "suspendedCount", column = "suspendedCount")
    })
    {Entity}StatisticsVO selectStatistics(@Param("queryForm") {Entity}QueryForm queryForm);

    /**
     * 查询{实体名称}关联数据
     *
     * @param entityId 实体ID
     * @return 关联数据列表
     */
    @Select("SELECT " +
            "e.*, " +
            "u.user_name as create_user_name, " +
            "u.real_name as create_user_real_name " +
            "FROM t_{module}_{entity} e " +
            "LEFT JOIN t_user u ON e.create_user_id = u.user_id " +
            "WHERE e.id = #{entityId} AND e.deleted_flag = false " +
            "AND u.deleted_flag = false")
    Map<String, Object> selectWithUser(@Param("entityId") Long entityId);

    /**
     * 查询用户的{实体名称}统计
     *
     * @param userId 用户ID
     * @return 统计信息
     */
    @Select("SELECT " +
            "COUNT(*) as total_count, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as active_count, " +
            "SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) as inactive_count " +
            "FROM t_{module}_{entity} " +
            "WHERE user_id = #{userId} AND deleted_flag = false")
    Map<String, Object> selectUserStatistics(@Param("userId") Long userId);

    /**
     * 查询最近创建的{实体名称}
     *
     * @param days 天数
     * @param limit 数量限制
     * @return {实体名称}列表
     */
    @Select("SELECT * FROM t_{module}_{entity} " +
            "WHERE deleted_flag = false " +
            "AND create_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "ORDER BY create_time DESC " +
            "LIMIT #{limit}")
    List<{Entity}Entity> selectRecentCreated(@Param("days") Integer days,
                                              @Param("limit") Integer limit);

    /**
     * 更新{实体名称}状态和时间
     *
     * @param id 实体ID
     * @param status 新状态
     * @param updateTime 更新时间
     * @param updateUserId 更新人ID
     * @return 更新数量
     */
    @Update("UPDATE t_{module}_{entity} " +
            "SET status = #{status}, " +
            "    update_time = #{updateTime}, " +
            "    update_user_id = #{updateUserId} " +
            "WHERE id = #{id} AND deleted_flag = false")
    int updateStatus(@Param("id") Long id,
                     @Param("status") Integer status,
                     @Param("updateTime") LocalDateTime updateTime,
                     @Param("updateUserId") Long updateUserId);

    /**
     * 检查{实体名称}是否可删除
     *
     * @param id 实体ID
     * @return 关联数量
     */
    @Select("SELECT COUNT(*) FROM t_related_table " +
            "WHERE {entity}_id = #{id} AND deleted_flag = false")
    Long checkRelatedCount(@Param("id") Long id);

    /**
     * 查询{实体名称}操作日志
     *
     * @param entityId 实体ID
     * @param limit 数量限制
     * @return 操作日志列表
     */
    @Select("SELECT " +
            "ol.operation_type, " +
            "ol.operation_desc, " +
            "ol.create_time, " +
            "u.user_name as operator_name " +
            "FROM t_operation_log ol " +
            "LEFT JOIN t_user u ON ol.operator_id = u.user_id " +
            "WHERE ol.target_type = '{ENTITY}' " +
            "AND ol.target_id = #{entityId} " +
            "ORDER BY ol.create_time DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectOperationLogs(@Param("entityId") Long entityId,
                                                 @Param("limit") Integer limit);

    /**
     * 软删除{实体名称}及相关数据
     *
     * @param id 实体ID
     * @param updateTime 更新时间
     * @param updateUserId 更新人ID
     * @return 影响行数
     */
    @Transactional(rollbackFor = Exception.class)
    default int softDeleteWithRelated(Long id, LocalDateTime updateTime, Long updateUserId) {
        int affectedRows = 0;

        // 删除主实体
        LambdaQueryWrapper<{Entity}Entity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq({Entity}Entity::getId, id)
               .eq({Entity}Entity::getDeletedFlag, false);

        {Entity}Entity entity = new {Entity}Entity();
        entity.setDeletedFlag(true);
        entity.setUpdateTime(updateTime);
        entity.setUpdateUserId(updateUserId);

        affectedRows += this.update(entity, wrapper);

        // 删除相关数据（如果有的话）
        // 这里可以调用其他DAO的方法删除相关数据

        return affectedRows;
    }
}
```

---

## 📝 XML映射文件模板

### 基础XML映射文件模板

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="net.lab1024.sa.{module}.dao.{Entity}Dao">

    <!-- 结果映射 -->
    <resultMap id="BaseResultMap" type="net.lab1024.sa.{module}.domain.entity.{Entity}Entity">
        <id column="id" property="id" jdbcType="BIGINT"/>
        <result column="name" property="name" jdbcType="VARCHAR"/>
        <result column="code" property="code" jdbcType="VARCHAR"/>
        <result column="status" property="status" jdbcType="INTEGER"/>
        <result column="description" property="description" jdbcType="VARCHAR"/>
        <result column="user_id" property="userId" jdbcType="BIGINT"/>
        <result column="create_time" property="createTime" jdbcType="TIMESTAMP"/>
        <result column="create_user_id" property="createUserId" jdbcType="BIGINT"/>
        <result column="update_time" property="updateTime" jdbcType="TIMESTAMP"/>
        <result column="update_user_id" property="updateUserId" jdbcType="BIGINT"/>
        <result column="deleted_flag" property="deletedFlag" jdbcType="BOOLEAN"/>
    </resultMap>

    <!-- 关联查询结果映射 -->
    <resultMap id="WithUserResultMap" type="net.lab1024.sa.{module}.domain.entity.{Entity}Entity" extends="BaseResultMap">
        <result column="create_user_name" property="createUserName" jdbcType="VARCHAR"/>
        <result column="create_user_real_name" property="createUserRealName" jdbcType="VARCHAR"/>
    </resultMap>

    <!-- 基础列定义 -->
    <sql id="Base_Column_List">
        id, name, code, status, description, user_id, create_time, create_user_id,
        update_time, update_user_id, deleted_flag
    </sql>

    <!-- 带用户的列定义 -->
    <sql id="WithUser_Column_List">
        e.id, e.name, e.code, e.status, e.description, e.user_id, e.create_time, e.create_user_id,
        e.update_time, e.update_user_id, e.deleted_flag,
        u.user_name as create_user_name, u.real_name as create_user_real_name
    </sql>

    <!-- 插入语句 -->
    <insert id="insert" parameterType="net.lab1024.sa.{module}.domain.entity.{Entity}Entity" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO t_{module}_{entity} (
            name, code, status, description, user_id, create_time, create_user_id,
            update_time, update_user_id, deleted_flag
        ) VALUES (
            #{name}, #{code}, #{status}, #{description}, #{userId},
            #{createTime}, #{createUserId}, #{updateTime}, #{updateUserId}, false
        )
    </insert>

    <!-- 根据ID查询 -->
    <select id="selectById" resultMap="BaseResultMap">
        SELECT <include refid="Base_Column_List"/>
        FROM t_{module}_{entity}
        WHERE id = #{id} AND deleted_flag = false
    </select>

    <!-- 根据名称查询 -->
    <select id="selectByName" resultMap="BaseResultMap">
        SELECT <include refid="Base_Column_List"/>
        FROM t_{module}_{entity}
        WHERE name = #{name} AND deleted_flag = false
        ORDER BY create_time DESC
    </select>

    <!-- 根据用户ID查询 -->
    <select id="selectByUserId" resultMap="BaseResultMap">
        SELECT <include refid="Base_Column_List"/>
        FROM t_{module}_{entity}
        WHERE user_id = #{userId} AND deleted_flag = false
        ORDER BY create_time DESC
    </select>

    <!-- 根据状态查询 -->
    <select id="selectByStatus" resultMap="BaseResultMap">
        SELECT <include refid="Base_Column_List"/>
        FROM t_{module}_{entity}
        WHERE status = #{status} AND deleted_flag = false
        ORDER BY create_time DESC
    </select>

    <!-- 模糊查询 -->
    <select id="selectByNameLike" resultMap="BaseResultMap">
        SELECT <include refid="Base_Column_List"/>
        FROM t_{module}_{entity}
        WHERE name LIKE CONCAT('%', #{name}, '%') AND deleted_flag = false
        ORDER BY create_time DESC
        LIMIT 100
    </select>

    <!-- 带用户的查询 -->
    <select id="selectWithUser" resultMap="WithUserResultMap">
        SELECT <include refid="WithUser_Column_List"/>
        FROM t_{module}_{entity} e
        LEFT JOIN t_user u ON e.create_user_id = u.user_id
        WHERE e.id = #{id} AND e.deleted_flag = false
        AND u.deleted_flag = false
    </select>

    <!-- 更新语句 -->
    <update id="updateById" parameterType="net.lab1024.sa.{module}.domain.entity.{Entity}Entity">
        UPDATE t_{module}_{entity}
        <set>
            <if test="name != null">name = #{name},</if>
            <if test="code != null">code = #{code},</if>
            <if test="status != null">status = #{status},</if>
            <if test="description != null">description = #{description},</if>
            <if test="userId != null">user_id = #{userId},</if>
            update_time = #{updateTime},
            <if test="updateUserId != null">update_user_id = #{updateUserId},</if>
        </set>
        WHERE id = #{id} AND deleted_flag = false
    </update>

    <!-- 批量更新状态 -->
    <update id="batchUpdateStatus">
        UPDATE t_{module}_{entity}
        SET status = #{status},
            update_time = NOW(),
            update_user_id = #{updateUserId}
        WHERE id IN
        <foreach collection="ids" item="id" open="(" separator="," close=")">
            #{id}
        </foreach>
        AND deleted_flag = false
    </update>

    <!-- 批量软删除 -->
    <update id="batchDelete">
        UPDATE t_{module}_{entity}
        SET deleted_flag = true,
            update_time = NOW()
        WHERE id IN
        <foreach collection="ids" item="id" open="(" separator="," close=")">
            #{id}
        </foreach>
        AND deleted_flag = false
    </update>

    <!-- 统计查询 -->
    <select id="countByCondition" resultType="java.lang.Long">
        SELECT COUNT(*) FROM t_{module}_{entity}
        WHERE deleted_flag = false
        <if test="userId != null">AND user_id = #{userId}</if>
        <if test="status != null">AND status = #{status}</if>
        <if test="startTime != null">AND create_time >= #{startTime}</if>
        <if test="endTime != null">AND create_time &lt;= #{endTime}</if>
    </select>

    <!-- 检查名称重复 -->
    <select id="countByName" resultType="java.lang.Long">
        SELECT COUNT(*) FROM t_{module}_{entity}
        WHERE name = #{name} AND deleted_flag = false
        <if test="excludeId != null">AND id != #{excludeId}</if>
    </select>

    <!-- 关联数据检查 -->
    <select id="checkRelatedCount" resultType="java.lang.Long">
        SELECT COUNT(*) FROM t_related_table
        WHERE {entity}_id = #{id} AND deleted_flag = false
    </select>

</mapper>
```

---

## 📝 使用说明

### 1. 模板替换规则

**替换变量**:
- `{module}`: 模块名称 (如: access, attendance, consume)
- `{Entity}`: 实体类名称 (如: AccessDevice, AttendanceRecord)
- `{entity}`: 数据库表名 (小写，如: access_device, attendance_record)

### 2. 必需依赖

**Maven依赖**:
```xml
<dependencies>
    <!-- MyBatis-Plus -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-boot-starter</artifactId>
        <version>3.5.3</version>
    </dependency>

    <!-- MySQL驱动 -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.33</version>
    </dependency>

    <!-- 数据库连接池 -->
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>druid-spring-boot-starter</artifactId>
        <version>1.2.18</version>
    </dependency>
</dependencies>
```

### 3. 代码规范检查清单

**DAO层检查清单**:
- [ ] 使用 `@Mapper` 注解
- [ ] 继承 `BaseMapper<Entity>`
- [ ] 使用 Dao 后缀命名
- [ ] 禁止使用 `@Repository` 注解
- [ ] 使用 `@Transactional` 注解
- [ ] 参数使用 `@Param` 注解
- [ ] SQL语句参数化防止注入
- [ ] 复杂查询使用XML映射文件

---

## 🚨 注意事项

### 1. 命名规范
- **接口名称**: `{Entity}Dao`
- **XML文件**: `{Entity}Dao.xml`
- **表名**: `t_{module}_{entity}`
- **字段名**: 下划线命名

### 2. SQL安全
- **禁止SQL注入**: 必须使用参数化查询
- **避免SELECT ***: 明确指定需要查询的字段
- **索引优化**: 查询条件要使用索引
- **分页查询**: 大数据量必须分页

### 3. 事务管理
- **读操作**: 使用 `@Transactional(readOnly = true)`
- **写操作**: 使用 `@Transactional(rollbackFor = Exception.class)`
- **批量操作**: 注意事务大小和性能

---

## 📚 相关文档

- [全局架构规范](../../01-核心规范/架构规范/全局架构规范.md)
- [Java编码规范](../../01-核心规范/开发规范/Java编码规范.md)
- [Controller层模板](./Controller模板.md)
- [Service层模板](./Service模板.md)
- [Manager层模板](./Manager模板.md)

---

**模板版本**: v2.0.0
**最后更新**: 2025-12-02
**维护团队**: IOE-DREAM架构委员会

**🎯 使用此模板可以确保DAO层代码的规范性和数据访问的安全性！**