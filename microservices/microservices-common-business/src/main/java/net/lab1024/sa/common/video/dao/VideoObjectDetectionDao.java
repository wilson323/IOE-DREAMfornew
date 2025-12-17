package net.lab1024.sa.common.video.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.video.entity.VideoObjectDetectionEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
// 已移除Repository导入，统一使用@Mapper注解

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 视频目标检测记录DAO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Mapper
// @Repository  // 禁止使用@Repository注解，统一使用@Mapper
public interface VideoObjectDetectionDao extends BaseMapper<VideoObjectDetectionEntity> {

    /**
     * 根据设备ID查询检测记录
     *
     * @param deviceId 设备ID
     * @param limit 限制数量
     * @return 检测记录列表
     */
    @Select("SELECT * FROM t_video_object_detection WHERE device_id = #{deviceId} " +
            "AND deleted_flag = 0 ORDER BY detection_time DESC LIMIT #{limit}")
    List<VideoObjectDetectionEntity> selectByDeviceId(@Param("deviceId") Long deviceId, @Param("limit") Integer limit);

    /**
     * 根据设备编码查询检测记录
     *
     * @param deviceCode 设备编码
     * @param limit 限制数量
     * @return 检测记录列表
     */
    @Select("SELECT * FROM t_video_object_detection WHERE device_code = #{deviceCode} " +
            "AND deleted_flag = 0 ORDER BY detection_time DESC LIMIT #{limit}")
    List<VideoObjectDetectionEntity> selectByDeviceCode(@Param("deviceCode") String deviceCode, @Param("limit") Integer limit);

    /**
     * 根据目标类型查询检测记录
     *
     * @param objectType 目标类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 限制数量
     * @return 检测记录列表
     */
    @Select("SELECT * FROM t_video_object_detection WHERE object_type = #{objectType} " +
            "AND detection_time BETWEEN #{startTime} AND #{endTime} " +
            "AND deleted_flag = 0 ORDER BY detection_time DESC LIMIT #{limit}")
    List<VideoObjectDetectionEntity> selectByObjectType(
            @Param("objectType") Integer objectType,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("limit") Integer limit);

    /**
     * 根据区域ID查询检测记录
     *
     * @param areaId 区域ID
     * @param limit 限制数量
     * @return 检测记录列表
     */
    @Select("SELECT * FROM t_video_object_detection WHERE area_id = #{areaId} " +
            "AND deleted_flag = 0 ORDER BY detection_time DESC LIMIT #{limit}")
    List<VideoObjectDetectionEntity> selectByAreaId(@Param("areaId") Long areaId, @Param("limit") Integer limit);

    /**
     * 查询未处理的告警记录
     *
     * @param alertLevel 告警级别（可选）
     * @param limit 限制数量
     * @return 告警记录列表
     */
    @Select("SELECT * FROM t_video_object_detection WHERE alert_triggered = 1 " +
            "AND (#{alertLevel} IS NULL OR alert_level >= #{alertLevel}) " +
            "AND process_status = 0 " +
            "AND deleted_flag = 0 ORDER BY detection_time DESC LIMIT #{limit}")
    List<VideoObjectDetectionEntity> selectPendingAlerts(
            @Param("alertLevel") Integer alertLevel,
            @Param("limit") Integer limit);

    /**
     * 根据置信度查询检测记录
     *
     * @param minConfidence 最小置信度
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 限制数量
     * @return 检测记录列表
     */
    @Select("SELECT * FROM t_video_object_detection WHERE confidence_score >= #{minConfidence} " +
            "AND detection_time BETWEEN #{startTime} AND #{endTime} " +
            "AND deleted_flag = 0 ORDER BY confidence_score DESC, detection_time DESC LIMIT #{limit}")
    List<VideoObjectDetectionEntity> selectByConfidence(
            @Param("minConfidence") Double minConfidence,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("limit") Integer limit);

    /**
     * 根据目标ID查询跟踪记录
     *
     * @param objectId 目标ID
     * @param limit 限制数量
     * @return 跟踪记录列表
     */
    @Select("SELECT * FROM t_video_object_detection WHERE object_id = #{objectId} " +
            "AND deleted_flag = 0 ORDER BY detection_time ASC LIMIT #{limit}")
    List<VideoObjectDetectionEntity> selectByObjectId(@Param("objectId") String objectId, @Param("limit") Integer limit);

    /**
     * 查询需要验证的记录
     *
     * @param verificationResult 验证结果状态
     * @param limit 限制数量
     * @return 待验证记录列表
     */
    @Select("SELECT * FROM t_video_object_detection WHERE verification_result = #{verificationResult} " +
            "AND deleted_flag = 0 ORDER BY detection_time DESC LIMIT #{limit}")
    List<VideoObjectDetectionEntity> selectForVerification(
            @Param("verificationResult") Integer verificationResult,
            @Param("limit") Integer limit);

    /**
     * 根据时间范围查询检测记录
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param deviceId 设备ID（可选）
     * @param objectType 目标类型（可选）
     * @param limit 限制数量
     * @return 检测记录列表
     */
    @Select("<script>" +
            "SELECT * FROM t_video_object_detection " +
            "WHERE detection_time BETWEEN #{startTime} AND #{endTime} " +
            "AND deleted_flag = 0 " +
            "<if test='deviceId != null'>AND device_id = #{deviceId}</if> " +
            "<if test='objectType != null'>AND object_type = #{objectType}</if> " +
            "ORDER BY detection_time DESC " +
            "<if test='limit != null and limit > 0'>LIMIT #{limit}</if>" +
            "</script>")
    List<VideoObjectDetectionEntity> selectByTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("deviceId") Long deviceId,
            @Param("objectType") Integer objectType,
            @Param("limit") Integer limit);

    /**
     * 统计指定时间段内的检测数量
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param deviceId 设备ID（可选）
     * @param objectType 目标类型（可选）
     * @return 检测数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM t_video_object_detection " +
            "WHERE detection_time BETWEEN #{startTime} AND #{endTime} " +
            "AND deleted_flag = 0 " +
            "<if test='deviceId != null'>AND device_id = #{deviceId}</if> " +
            "<if test='objectType != null'>AND object_type = #{objectType}</if>" +
            "</script>")
    Long countByTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("deviceId") Long deviceId,
            @Param("objectType") Integer objectType);

    /**
     * 删除指定时间之前的记录
     *
     * @param beforeTime 时间点
     * @return 删除的记录数
     */
    @Transactional(rollbackFor = Exception.class)
    int deleteBeforeTime(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 批量更新处理状态
     *
     * @param detectionIds 检测记录ID列表
     * @param processStatus 处理状态
     * @param verifiedBy 验证人员ID
     * @return 更新的记录数
     */
    @Transactional(rollbackFor = Exception.class)
    int updateProcessStatusBatch(
            @Param("detectionIds") List<Long> detectionIds,
            @Param("processStatus") Integer processStatus,
            @Param("verifiedBy") Long verifiedBy);
}