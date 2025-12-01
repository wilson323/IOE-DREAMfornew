package {package}.dao;

import java.util.List;
import java.util.Set;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import net.lab1024.sa.base.module.support.dict.domain.entity.DictEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;

import {package}.domain.entity.{BaseEntityName}{ModuleName}ExtEntity;

/**
 * {base_entity_comment}{module_name}扩展DAO
 * <p>
 * 提供扩展表的数据访问操作，基于现有成功DAO模式增强
 * 包含高效查询、批量操作、关联查询等功能
 *
 * @author {author}
 * @since {create_date}
 */
@Mapper
public interface {BaseEntityName}{ModuleName}ExtDao extends BaseMapper<{BaseEntityName}{ModuleName}ExtEntity> {

    // ==================== 基础查询方法 ====================

    /**
     * 根据{base_entity_comment}ID查询扩展信息
     *
     * @param {baseTableId} {base_entity_comment}ID
     * @return 扩展实体，如果不存在返回null
     */
    @Select("SELECT * FROM t_{base_table}_{module}_ext " +
            "WHERE {base_table}_id = #{ {baseTableId} } AND deleted_flag = 0")
    {BaseEntityName}{ModuleName}ExtEntity selectBy{BaseEntityName}Id(@Param("{baseTableId}") Long {baseTableId});

    /**
     * 批量查询扩展信息（基于现有批量操作优化）
     *
     * @param {baseTableId}s {base_entity_comment}ID列表
     * @return 扩展实体列表
     */
    @Select("<script>" +
            "SELECT * FROM t_{base_table}_{module}_ext " +
            "WHERE {base_table}_id IN " +
            "<foreach collection='{baseTableId}s' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "AND deleted_flag = 0" +
            "</script>")
    List<{BaseEntityName}{ModuleName}ExtEntity> selectBy{BaseEntityName}Ids(@Param("{baseTableId}s") List<Long> {baseTableId}s);

    /**
     * 查询所有有效的扩展信息
     *
     * @return 所有有效的扩展实体列表
     */
    @Select("SELECT * FROM t_{base_table}_{module}_ext " +
            "WHERE deleted_flag = 0 ORDER BY create_time DESC")
    List<{BaseEntityName}{ModuleName}ExtEntity> selectAllEffective();

    // ==================== 条件查询方法 ====================

    /**
     * 根据{module_name}等级查询
     *
     * @param {module}Level {module_name}等级
     * @return 符合条件的扩展实体列表
     */
    @Select("SELECT * FROM t_{base_table}_{module}_ext " +
            "WHERE {module}_level = #{ {module}Level } AND deleted_flag = 0 " +
            "ORDER BY priority DESC, create_time DESC")
    List<{BaseEntityName}{ModuleName}ExtEntity> selectBy{ModuleName}Level(@Param("{module}Level") Integer {module}Level);

    /**
     * 根据状态查询
     *
     * @param {module}Status {module_name}状态
     * @return 符合条件的扩展实体列表
     */
    @Select("SELECT * FROM t_{base_table}_{module}_ext " +
            "WHERE {module}_status = #{ {module}Status } AND deleted_flag = 0 " +
            "ORDER BY priority DESC, create_time DESC")
    List<{BaseEntityName}{ModuleName}ExtEntity> selectBy{ModuleName}Status(@Param("{module}Status") Integer {module}Status);

    /**
     * 根据模式查询（支持模糊匹配）
     *
     * @param {module}Mode {module_name}模式
     * @return 符合条件的扩展实体列表
     */
    @Select("SELECT * FROM t_{base_table}_{module}_ext " +
            "WHERE {module}_mode LIKE CONCAT('%', #{ {module}Mode }, '%') AND deleted_flag = 0 " +
            "ORDER BY priority DESC, create_time DESC")
    List<{BaseEntityName}{ModuleName}ExtEntity> selectBy{ModuleName}Mode(@Param("{module}Mode") String {module}Mode);

    /**
     * 查询需要特殊处理的扩展信息
     *
     * @return 需要特殊处理的扩展实体列表
     */
    @Select("SELECT * FROM t_{base_table}_{module}_ext " +
            "WHERE special_required = 1 AND deleted_flag = 0 " +
            "ORDER BY priority DESC, create_time DESC")
    List<{BaseEntityName}{ModuleName}ExtEntity> selectSpecialRequired();

    // ==================== 关联查询方法（基于现有JOIN查询优化） ====================

    /**
     * 关联查询基础信息和扩展信息
     *
     * @param status 基础实体状态（可选）
     * @param {module}Level {module_name}等级（可选）
     * @return 关联查询结果列表
     */
    @Select("<script>" +
            "SELECT " +
            "base.{base_table}_id, base.{entity_code_field}, base.{entity_name_field}, base.status as base_status, " +
            "ext.ext_id, ext.{module}_level, ext.{module}_mode, ext.special_required, ext.priority, " +
            "ext.time_restrictions, ext.location_rules, ext.alert_config, ext.device_linkage_rules " +
            "FROM t_{base_table} base " +
            "LEFT JOIN t_{base_table}_{module}_ext ext ON base.{base_table}_id = ext.{base_table}_id " +
            "WHERE base.deleted_flag = 0 " +
            "<if test='status != null'>" +
            "AND base.status = #{status} " +
            "</if>" +
            "<if test='{module}Level != null'>" +
            "AND (ext.{module}_level >= #{ {module}Level } OR ext.{module}_level IS NULL) " +
            "</if>" +
            "ORDER BY base.sort_order ASC, base.{base_table}_id ASC" +
            "</script>")
    List<{BaseEntityName}{ModuleName}VO> select{BaseEntityName}{ModuleName}List(
            @Param("status") Integer status,
            @Param("{module}Level") Integer {module}Level);

    /**
     * 分页关联查询（基于现有分页查询优化）
     *
     * @param page 分页参数
     * @param {baseEntityName}Name {base_entity_comment}名称（模糊查询）
     * @param {module}Level {module_name}等级
     * @return 分页查询结果
     */
    @Select("<script>" +
            "SELECT " +
            "base.{base_table}_id, base.{entity_code_field}, base.{entity_name_field}, base.status as base_status, " +
            "ext.ext_id, ext.{module}_level, ext.{module}_mode, ext.special_required, ext.priority " +
            "FROM t_{base_table} base " +
            "LEFT JOIN t_{base_table}_{module}_ext ext ON base.{base_table}_id = ext.{base_table}_id " +
            "WHERE base.deleted_flag = 0 " +
            "<if test='{baseEntityName}Name != null and {baseEntityName}Name != '''>" +
            "AND base.{entity_name_field} LIKE CONCAT('%', #{ {baseEntityName}Name }, '%') " +
            "</if>" +
            "<if test='{module}Level != null'>" +
            "AND ext.{module}_level = #{ {module}Level } " +
            "</if>" +
            "ORDER BY base.sort_order ASC, base.{base_table}_id ASC" +
            "</script>")
    IPage<{BaseEntityName}{ModuleName}VO> select{BaseEntityName}{ModuleName}Page(
            IPage<{BaseEntityName}{ModuleName}VO> page,
            @Param("{baseEntityName}Name") String {baseEntityName}Name,
            @Param("{module}Level") Integer {module}Level);

    // ==================== 统计查询方法（基于现有统计方法抽象） ====================

    /**
     * 统计{module_name}等级分布
     *
     * @return 等级统计结果
     */
    @Select("SELECT {module}_level, COUNT(*) as count " +
            "FROM t_{base_table}_{module}_ext " +
            "WHERE deleted_flag = 0 " +
            "GROUP BY {module}_level " +
            "ORDER BY {module}_level ASC")
    List<DictEntity> countBy{ModuleName}Level();

    /**
     * 统计{module_name}状态分布
     *
     * @return 状态统计结果
     */
    @Select("SELECT {module}_status, COUNT(*) as count " +
            "FROM t_{base_table}_{module}_ext " +
            "WHERE deleted_flag = 0 " +
            "GROUP BY {module}_status " +
            "ORDER BY {module}_status ASC")
    List<DictEntity> countBy{ModuleName}Status();

    /**
     * 统计需要特殊处理的数量
     *
     * @return 需要特殊处理的数量
     */
    @Select("SELECT COUNT(*) FROM t_{base_table}_{module}_ext " +
            "WHERE special_required = 1 AND deleted_flag = 0")
    int countSpecialRequired();

    /**
     * 统计配置复杂度分布
     *
     * @return 复杂度统计结果
     */
    @Select("SELECT " +
            "CASE " +
            "WHEN time_restrictions IS NOT NULL AND location_rules IS NOT NULL AND alert_config IS NOT NULL THEN 'high' " +
            "WHEN (time_restrictions IS NOT NULL OR location_rules IS NOT NULL OR alert_config IS NOT NULL) AND " +
            "     (time_restrictions IS NULL OR location_rules IS NULL OR alert_config IS NULL) THEN 'medium' " +
            "ELSE 'low' " +
            "END as complexity_level, " +
            "COUNT(*) as count " +
            "FROM t_{base_table}_{module}_ext " +
            "WHERE deleted_flag = 0 " +
            "GROUP BY complexity_level " +
            "ORDER BY complexity_level DESC")
    List<DictEntity> countByConfigComplexity();

    // ==================== 插入和更新方法 ====================

    /**
     * 插入或更新扩展信息（基于现有upsert模式）
     * 如果记录存在则更新，不存在则插入
     *
     * @param {baseTableId} {base_entity_comment}ID
     * @param {module}Level {module_name}等级
     * @param {module}Mode {module_name}模式
     * @param specialRequired 是否需要特殊处理
     * @param priority 优先级
     * @param {module}Status {module_name}状态
     * @param currentTime 当前时间戳
     * @param userId 用户ID
     * @return 影响行数
     */
    @Insert("<script>" +
            "INSERT INTO t_{base_table}_{module}_ext " +
            "({base_table}_id, {module}_level, {module}_mode, special_required, priority, {module}_status, " +
            " create_time, update_time, create_user_id, update_user_id) " +
            "VALUES (#{ {baseTableId} }, #{ {module}Level }, #{ {module}Mode }, #{ specialRequired }, " +
            " #{ priority }, #{ {module}Status }, " +
            " #{ currentTime }, #{ currentTime }, #{ userId }, #{ userId }) " +
            "ON DUPLICATE KEY UPDATE " +
            "{module}_level = VALUES({module}_level), " +
            "{module}_mode = VALUES({module}_mode), " +
            "special_required = VALUES(special_required), " +
            "priority = VALUES(priority), " +
            "{module}_status = VALUES({module}_status), " +
            "update_time = VALUES(update_time), " +
            "update_user_id = VALUES(update_user_id)" +
            "</script>")
    int insertOrUpdate{ModuleName}Extension(
            @Param("{baseTableId}") Long {baseTableId},
            @Param("{module}Level") Integer {module}Level,
            @Param("{module}Mode") String {module}Mode,
            @Param("specialRequired") Boolean specialRequired,
            @Param("priority") Integer priority,
            @Param("{module}Status") Integer {module}Status,
            @Param("currentTime") Long currentTime,
            @Param("userId") Long userId);

    /**
     * 批量插入扩展信息（基于现有批量插入优化）
     *
     * @param extensions 扩展实体列表
     * @return 插入行数
     */
    int batchInsert(@Param("extensions") List<{BaseEntityName}{ModuleName}ExtEntity> extensions);

    /**
     * 批量更新{module_name}状态
     *
     * @param {baseTableId}s {base_entity_comment}ID列表
     * @param {module}Status 新的{module_name}状态
     * @param userId 用户ID
     * @return 更新行数
     */
    @Update("<script>" +
            "UPDATE t_{base_table}_{module}_ext " +
            "SET {module}_status = #{ {module}Status }, update_time = UNIX_TIMESTAMP()*1000, update_user_id = #{ userId } " +
            "WHERE {base_table}_id IN " +
            "<foreach collection='{baseTableId}s' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "AND deleted_flag = 0" +
            "</script>")
    int batchUpdate{ModuleName}Status(
            @Param("{baseTableId}s") List<Long> {baseTableId}s,
            @Param("{module}Status") Integer {module}Status,
            @Param("userId") Long userId);

    // ==================== 删除方法 ====================

    /**
     * 根据{base_entity_comment}ID软删除扩展信息
     *
     * @param {baseTableId} {base_entity_comment}ID
     * @param userId 用户ID
     * @return 删除行数
     */
    @Update("UPDATE t_{base_table}_{module}_ext " +
            "SET deleted_flag = 1, update_time = UNIX_TIMESTAMP()*1000, update_user_id = #{ userId } " +
            "WHERE {base_table}_id = #{ {baseTableId} }")
    int deleteBy{BaseEntityName}Id(@Param("{baseTableId}") Long {baseTableId}, @Param("userId") Long userId);

    /**
     * 批量软删除扩展信息
     *
     * @param {baseTableId}s {base_entity_comment}ID列表
     * @param userId 用户ID
     * @return 删除行数
     */
    @Update("<script>" +
            "UPDATE t_{base_table}_{module}_ext " +
            "SET deleted_flag = 1, update_time = UNIX_TIMESTAMP()*1000, update_user_id = #{ userId } " +
            "WHERE {base_table}_id IN " +
            "<foreach collection='{baseTableId}s' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "AND deleted_flag = 0" +
            "</script>")
    int batchDeleteBy{BaseEntityName}Ids(@Param("{baseTableId}s") List<Long> {baseTableId}s, @Param("userId") Long userId);

    // ==================== 配置查询方法 ====================

    /**
     * 查询有时间限制的扩展信息
     *
     * @return 有时间限制的扩展实体列表
     */
    @Select("SELECT * FROM t_{base_table}_{module}_ext " +
            "WHERE time_restrictions IS NOT NULL AND time_restrictions != '' " +
            "AND deleted_flag = 0 ORDER BY priority DESC")
    List<{BaseEntityName}{ModuleName}ExtEntity> selectWithTimeRestrictions();

    /**
     * 查询有位置限制的扩展信息
     *
     * @return 有位置限制的扩展实体列表
     */
    @Select("SELECT * FROM t_{base_table}_{module}_ext " +
            "WHERE location_rules IS NOT NULL AND location_rules != '' " +
            "AND deleted_flag = 0 ORDER BY priority DESC")
    List<{BaseEntityName}{ModuleName}ExtEntity> selectWithLocationRules();

    /**
     * 查询启用了告警的扩展信息
     *
     * @return 启用告警的扩展实体列表
     */
    @Select("SELECT * FROM t_{base_table}_{module}_ext " +
            "WHERE alert_config LIKE '%\"enabled\":true%' " +
            "AND deleted_flag = 0 ORDER BY priority DESC")
    List<{BaseEntityName}{ModuleName}ExtEntity> selectWithAlertEnabled();

    // ==================== 维护和清理方法 ====================

    /**
     * 清理孤立的扩展记录（对应的基础记录已被删除）
     *
     * @return 清理的记录数
     */
    @Update("DELETE ext FROM t_{base_table}_{module}_ext ext " +
            "LEFT JOIN t_{base_table} base ON ext.{base_table}_id = base.{base_table}_id " +
            "WHERE base.{base_table}_id IS NULL")
    int cleanOrphanExtensions();

    /**
     * 统计扩展记录总数
     *
     * @return 扩展记录总数
     */
    @Select("SELECT COUNT(*) FROM t_{base_table}_{module}_ext WHERE deleted_flag = 0")
    int countTotal();

    /**
     * 获取优先级最高的N条记录
     *
     * @param limit 限制数量
     * @return 优先级最高的扩展记录列表
     */
    @Select("SELECT * FROM t_{base_table}_{module}_ext " +
            "WHERE deleted_flag = 0 AND {module}_status = 1 " +
            "ORDER BY priority DESC, create_time DESC LIMIT #{ limit }")
    List<{BaseEntityName}{ModuleName}ExtEntity> selectTopByPriority(@Param("limit") Integer limit);
}