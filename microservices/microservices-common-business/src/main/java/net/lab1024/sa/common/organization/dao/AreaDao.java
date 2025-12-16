package net.lab1024.sa.common.organization.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 区域数据访问层
 * <p>
 * 区域信息的数据库访问接口
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface AreaDao extends BaseMapper<AreaEntity> {

    /**
     * 根据区域编码查询区域
     *
     * @param areaCode 区域编码
     * @return 区域信息
     */
    @Select("SELECT * FROM t_area WHERE area_code = #{areaCode} AND deleted_flag = 0")
    AreaEntity selectByAreaCode(@Param("areaCode") String areaCode);

    /**
     * 根据区域类型查询区域列表
     *
     * @param areaType 区域类型
     * @return 区域列表
     */
    @Select("SELECT * FROM t_area WHERE area_type = #{areaType} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<AreaEntity> selectByAreaType(@Param("areaType") Integer areaType);

    /**
     * 根据父级区域ID查询子区域列表
     *
     * @param parentId 父级区域ID
     * @return 子区域列表
     */
    @Select("SELECT * FROM t_area WHERE parent_id = #{parentId} AND deleted_flag = 0 ORDER BY sort_order ASC")
    List<AreaEntity> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 根据状态查询区域列表
     *
     * @param status 状态
     * @return 区域列表
     */
    @Select("SELECT * FROM t_area WHERE status = #{status} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<AreaEntity> selectByStatus(@Param("status") Integer status);

    /**
     * 根据区域名称模糊查询区域列表
     *
     * @param areaName 区域名称
     * @return 区域列表
     */
    @Select("SELECT * FROM t_area WHERE area_name LIKE CONCAT('%', #{areaName}, '%') AND deleted_flag = 0 ORDER BY create_time DESC")
    List<AreaEntity> selectByAreaName(@Param("areaName") String areaName);

    /**
     * 查询用户可访问的区域ID列表
     *
     * @param userId 用户ID
     * @return 可访问的区域ID列表
     */
    @Select("SELECT DISTINCT area_id FROM t_area_user WHERE user_id = #{userId} AND deleted_flag = 0")
    List<Long> selectAccessibleAreaIds(@Param("userId") Long userId);

    /**
     * 检查用户是否直接可访问指定区域
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @return 是否可访问
     */
    @Select("SELECT COUNT(1) > 0 FROM t_area_user WHERE user_id = #{userId} AND area_id = #{areaId} AND deleted_flag = 0")
    boolean hasDirectAccess(@Param("userId") Long userId, @Param("areaId") Long areaId);

    /**
     * 根据编码查询区域（别名方法）
     *
     * @param code 区域编码
     * @return 区域信息
     */
    @Select("SELECT * FROM t_area WHERE area_code = #{code} AND deleted_flag = 0")
    AreaEntity selectByCode(@Param("code") String code);

    /**
     * 查询子区域列表
     *
     * @param parentId 父级区域ID
     * @return 子区域列表
     */
    @Select("SELECT * FROM t_area WHERE parent_id = #{parentId} AND deleted_flag = 0 ORDER BY sort_order ASC")
    List<AreaEntity> selectChildAreas(@Param("parentId") Long parentId);

    /**
     * 查询区域业务属性
     *
     * @param areaId 区域ID
     * @param attributeKey 属性键
     * @return 业务属性值
     */
    @Select("SELECT business_attributes FROM t_area WHERE area_id = #{areaId} AND deleted_flag = 0")
    String selectBusinessAttributes(@Param("areaId") Long areaId, @Param("attributeKey") String attributeKey);

    /**
     * 更新区域业务属性
     *
     * @param areaId 区域ID
     * @param attributeKey 属性键
     * @param attributeValue 属性值
     * @return 更新结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("UPDATE t_area SET business_attributes = #{attributeValue} WHERE area_id = #{areaId} AND deleted_flag = 0")
    int updateBusinessAttributes(@Param("areaId") Long areaId, @Param("attributeKey") String attributeKey, @Param("attributeValue") String attributeValue);

    /**
     * 查询区域下的设备列表
     *
     * @param areaId 区域ID
     * @param deviceType 设备类型
     * @return 设备列表
     */
    @Select("SELECT d.* FROM t_device d INNER JOIN t_area_device ad ON d.device_id = ad.device_id WHERE ad.area_id = #{areaId} AND d.deleted_flag = 0 AND ad.deleted_flag = 0")
    List<Long> selectAreaDevices(@Param("areaId") Long areaId, @Param("deviceType") String deviceType);

    /**
     * 统计子区域数量
     *
     * @param parentId 父级区域ID
     * @return 子区域数量
     */
    @Select("SELECT COUNT(1) FROM t_area WHERE parent_id = #{parentId} AND deleted_flag = 0")
    int countChildAreas(@Param("parentId") Long parentId);

    /**
     * 根据业务类型查询区域列表
     *
     * @param businessType 业务类型
     * @return 区域列表
     */
    @Select("SELECT * FROM t_area WHERE JSON_EXTRACT(business_attributes, '$.businessType') = #{businessType} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<AreaEntity> selectAreasByBusinessType(@Param("businessType") String businessType);

    /**
     * 统计区域门禁设备数量
     *
     * @param areaId 区域ID
     * @return 门禁设备数量
     */
    @Select("SELECT COUNT(1) FROM t_device d INNER JOIN t_area_device ad ON d.device_id = ad.device_id WHERE ad.area_id = #{areaId} AND d.device_type = 'ACCESS' AND d.deleted_flag = 0 AND ad.deleted_flag = 0")
    int countAccessDevices(@Param("areaId") Long areaId);

    /**
     * 统计区域门禁记录数量
     *
     * @param areaId 区域ID
     * @return 门禁记录数量
     */
    @Select("SELECT COUNT(1) FROM t_access_record WHERE area_id = #{areaId} AND deleted_flag = 0")
    int countAccessRecords(@Param("areaId") Long areaId);

    /**
     * 统计区域有效门禁权限数量
     *
     * @param areaId 区域ID
     * @return 有效门禁权限数量
     */
    @Select("SELECT COUNT(1) FROM t_access_permission WHERE area_id = #{areaId} AND status = 1 AND deleted_flag = 0")
    int countActiveAccessPermissions(@Param("areaId") Long areaId);

    /**
     * 统计区域消费设备数量
     *
     * @param areaId 区域ID
     * @return 消费设备数量
     */
    @Select("SELECT COUNT(1) FROM t_device d INNER JOIN t_area_device ad ON d.device_id = ad.device_id WHERE ad.area_id = #{areaId} AND d.device_type = 'CONSUME' AND d.deleted_flag = 0 AND ad.deleted_flag = 0")
    int countConsumeDevices(@Param("areaId") Long areaId);

    /**
     * 汇总区域消费金额
     *
     * @param areaId 区域ID
     * @return 消费金额总和
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM t_consume_record WHERE area_id = #{areaId} AND deleted_flag = 0")
    java.math.BigDecimal sumConsumeAmount(@Param("areaId") Long areaId);

    /**
     * 统计区域消费记录数量
     *
     * @param areaId 区域ID
     * @return 消费记录数量
     */
    @Select("SELECT COUNT(1) FROM t_consume_record WHERE area_id = #{areaId} AND deleted_flag = 0")
    int countConsumeRecords(@Param("areaId") Long areaId);

    /**
     * 统计区域考勤设备数量
     *
     * @param areaId 区域ID
     * @return 考勤设备数量
     */
    @Select("SELECT COUNT(1) FROM t_device d INNER JOIN t_area_device ad ON d.device_id = ad.device_id WHERE ad.area_id = #{areaId} AND d.device_type = 'ATTENDANCE' AND d.deleted_flag = 0 AND ad.deleted_flag = 0")
    int countAttendanceDevices(@Param("areaId") Long areaId);

    /**
     * 统计区域考勤记录数量
     *
     * @param areaId 区域ID
     * @return 考勤记录数量
     */
    @Select("SELECT COUNT(1) FROM t_attendance_record WHERE area_id = #{areaId} AND deleted_flag = 0")
    int countAttendanceRecords(@Param("areaId") Long areaId);

    /**
     * 统计区域用户数量
     *
     * @param areaId 区域ID
     * @return 区域用户数量
     */
    @Select("SELECT COUNT(DISTINCT user_id) FROM t_area_user WHERE area_id = #{areaId} AND deleted_flag = 0")
    int countAreaUsers(@Param("areaId") Long areaId);

    /**
     * 统计区域访客记录数量
     *
     * @param areaId 区域ID
     * @return 访客记录数量
     */
    @Select("SELECT COUNT(1) FROM t_visitor_record WHERE access_area_id = #{areaId} AND deleted_flag = 0")
    int countVisitorRecords(@Param("areaId") Long areaId);

    /**
     * 统计区域访客预约数量
     *
     * @param areaId 区域ID
     * @return 访客预约数量
     */
    @Select("SELECT COUNT(1) FROM t_visitor_appointment WHERE target_area_id = #{areaId} AND deleted_flag = 0")
    int countVisitorAppointments(@Param("areaId") Long areaId);

    /**
     * 统计区域视频设备数量
     *
     * @param areaId 区域ID
     * @return 视频设备数量
     */
    @Select("SELECT COUNT(1) FROM t_device d INNER JOIN t_area_device ad ON d.device_id = ad.device_id WHERE ad.area_id = #{areaId} AND d.device_type = 'VIDEO' AND d.deleted_flag = 0 AND ad.deleted_flag = 0")
    int countVideoDevices(@Param("areaId") Long areaId);

    /**
     * 获取区域视频录像大小
     *
     * @param areaId 区域ID
     * @return 视频录像大小（字节）
     */
    @Select("SELECT COALESCE(SUM(file_size), 0) FROM t_video_record WHERE area_id = #{areaId} AND deleted_flag = 0")
    long getVideoRecordSize(@Param("areaId") Long areaId);

    // ==================== 区域层级管理相关查询 ====================

    /**
     * 根据父级区域ID和区域级别查询子区域列表
     *
     * @param parentId 父级区域ID，null表示查询顶级区域
     * @param areaLevel 区域级别，null表示查询所有级别
     * @return 子区域列表
     */
    @Select("<script>" +
            "SELECT * FROM t_area WHERE deleted_flag = 0 " +
            "<if test='parentId != null'> AND parent_id = #{parentId} </if>" +
            "<if test='parentId == null'> AND (parent_id IS NULL OR parent_id = 0) </if>" +
            "<if test='areaLevel != null'> AND area_level = #{areaLevel} </if>" +
            "ORDER BY area_level ASC, sort_order ASC, area_name ASC" +
            "</script>")
    List<AreaEntity> selectByParentIdAndLevel(@Param("parentId") Long parentId, @Param("areaLevel") Integer areaLevel);

    /**
     * 根据区域级别查询区域列表
     *
     * @param areaLevel 区域级别
     * @return 区域列表
     */
    @Select("SELECT * FROM t_area WHERE area_level = #{areaLevel} AND deleted_flag = 0 ORDER BY sort_order ASC, area_name ASC")
    List<AreaEntity> selectByLevel(@Param("areaLevel") Integer areaLevel);

    /**
     * 根据关键词搜索区域（按名称或编码）
     *
     * @param keyword 关键词
     * @return 搜索结果
     */
    @Select("SELECT * FROM t_area WHERE (area_name LIKE CONCAT('%', #{keyword}, '%') OR area_code LIKE CONCAT('%', #{keyword}, '%')) AND deleted_flag = 0 ORDER BY area_level ASC, area_name ASC")
    List<AreaEntity> selectByKeyword(@Param("keyword") String keyword);

    /**
     * 获取区域完整路径信息
     * 通过递归CTE查询获取从顶级到当前区域的完整路径
     *
     * @param areaId 区域ID
     * @return 路径区域列表（从顶级到当前区域）
     */
    @Select("WITH RECURSIVE area_path AS (" +
            "SELECT area_id, parent_id, area_name, area_code, area_level, 1 as path_level " +
            "FROM t_area WHERE area_id = #{areaId} AND deleted_flag = 0 " +
            "UNION ALL " +
            "SELECT a.area_id, a.parent_id, a.area_name, a.area_code, a.area_level, ap.path_level + 1 " +
            "FROM t_area a " +
            "INNER JOIN area_path ap ON a.area_id = ap.parent_id " +
            "WHERE a.deleted_flag = 0" +
            ") " +
            "SELECT * FROM area_path ORDER BY path_level DESC")
    List<AreaEntity> selectAreaPath(@Param("areaId") Long areaId);

    /**
     * 检查区域层级结构是否合法
     * 验证父区域级别是否为当前区域级别-1
     *
     * @param areaId 区域ID
     * @return 验证结果：1表示合法，0表示不合法
     */
    @Select("SELECT CASE " +
            "WHEN parent_id IS NULL OR parent_id = 0 THEN 1 " +
            "WHEN EXISTS (SELECT 1 FROM t_area parent WHERE parent.area_id = t_area.parent_id AND parent.area_level = t_area.area_level - 1 AND parent.deleted_flag = 0) THEN 1 " +
            "ELSE 0 " +
            "END as is_valid " +
            "FROM t_area WHERE area_id = #{areaId} AND deleted_flag = 0")
    int validateAreaHierarchy(@Param("areaId") Long areaId);

    /**
     * 获取同级区域列表
     *
     * @param areaId 区域ID
     * @return 同级区域列表
     */
    @Select("SELECT * FROM t_area WHERE parent_id = (SELECT parent_id FROM t_area WHERE area_id = #{areaId} AND deleted_flag = 0) AND area_id != #{areaId} AND deleted_flag = 0 ORDER BY sort_order ASC, area_name ASC")
    List<AreaEntity> selectSiblingAreas(@Param("areaId") Long areaId);

    /**
     * 按区域级别统计区域数量
     *
     * @param parentAreaId 父区域ID，null表示统计所有
     * @return 级别统计结果
     */
    @Select("<script>" +
            "SELECT " +
            "area_level, " +
            "COUNT(1) as count " +
            "FROM t_area " +
            "WHERE deleted_flag = 0 " +
            "<if test='parentAreaId != null'> AND parent_id = #{parentAreaId} </if>" +
            "<if test='parentAreaId == null'> AND (parent_id IS NULL OR parent_id = 0) </if>" +
            "GROUP BY area_level " +
            "ORDER BY area_level" +
            "</script>")
    List<java.util.Map<String, Object>> countAreasByLevel(@Param("parentAreaId") Long parentAreaId);

    /**
     * 按状态统计区域数量
     *
     * @param parentAreaId 父区域ID，null表示统计所有
     * @return 状态统计结果
     */
    @Select("<script>" +
            "SELECT " +
            "status, " +
            "COUNT(1) as count " +
            "FROM t_area " +
            "WHERE deleted_flag = 0 " +
            "<if test='parentAreaId != null'> AND parent_id = #{parentAreaId} </if>" +
            "<if test='parentAreaId == null'> AND (parent_id IS NULL OR parent_id = 0) </if>" +
            "GROUP BY status " +
            "ORDER BY status" +
            "</script>")
    List<java.util.Map<String, Object>> countAreasByStatus(@Param("parentAreaId") Long parentAreaId);

    /**
     * 检查区域编码是否存在
     *
     * @param areaCode 区域编码
     * @param excludeAreaId 排除的区域ID（用于更新时检查）
     * @return 是否存在
     */
    @Select("<script>" +
            "SELECT COUNT(1) > 0 FROM t_area WHERE area_code = #{areaCode} AND deleted_flag = 0 " +
            "<if test='excludeAreaId != null'> AND area_id != #{excludeAreaId} </if>" +
            "</script>")
    boolean existsByAreaCode(@Param("areaCode") String areaCode, @Param("excludeAreaId") Long excludeAreaId);

    /**
     * 检查区域名称在同一级别下是否存在
     *
     * @param areaName 区域名称
     * @param parentAreaId 父区域ID
     * @param areaLevel 区域级别
     * @param excludeAreaId 排除的区域ID
     * @return 是否存在
     */
    @Select("<script>" +
            "SELECT COUNT(1) > 0 FROM t_area " +
            "WHERE area_name = #{areaName} AND parent_id = #{parentAreaId} AND area_level = #{areaLevel} AND deleted_flag = 0 " +
            "<if test='excludeAreaId != null'> AND area_id != #{excludeAreaId} </if>" +
            "</script>")
    boolean existsByNameInLevel(@Param("areaName") String areaName, @Param("parentAreaId") Long parentAreaId, @Param("areaLevel") Integer areaLevel, @Param("excludeAreaId") Long excludeAreaId);
}
