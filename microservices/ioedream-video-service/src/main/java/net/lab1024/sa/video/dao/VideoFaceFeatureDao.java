package net.lab1024.sa.video.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.video.domain.entity.FaceFeatureEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 视频人脸特征DAO
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Mapper
public interface VideoFaceFeatureDao extends BaseMapper<FaceFeatureEntity> {

    /**
     * 获取今日人脸识别数量
     *
     * @param deviceId 设备ID
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM video_face_feature WHERE device_id = #{deviceId} " +
            "AND DATE(create_time) = CURDATE()")
    Long getTodayFaceCount(@Param("deviceId") Long deviceId);

    /**
     * 根据人员ID查询人脸特征
     *
     * @param personId 人员ID
     * @return 人脸特征列表
     */
    @Select("SELECT * FROM video_face_feature WHERE person_id = #{personId} " +
            "ORDER BY create_time DESC")
    List<FaceFeatureEntity> selectByPersonId(@Param("personId") String personId);

    /**
     * 根据时间范围查询人脸特征
     *
     * @param deviceId 设备ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 人脸特征列表
     */
    @Select("SELECT * FROM video_face_feature WHERE device_id = #{deviceId} " +
            "AND detect_time BETWEEN #{startTime} AND #{endTime} " +
            "ORDER BY detect_time DESC")
    List<FaceFeatureEntity> selectByTimeRange(@Param("deviceId") Long deviceId,
                                              @Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime);

    /**
     * 根据置信度查询人脸特征
     *
     * @param deviceId 设备ID
     * @param minConfidence 最小置信度
     * @return 人脸特征列表
     */
    @Select("SELECT * FROM video_face_feature WHERE device_id = #{deviceId} " +
            "AND confidence_score >= #{minConfidence} " +
            "ORDER BY confidence_score DESC")
    List<FaceFeatureEntity> selectByConfidence(@Param("deviceId") Long deviceId,
                                              @Param("minConfidence") Double minConfidence);

    /**
     * 查询未处理的人脸特征
     *
     * @return 未处理的人脸特征列表
     */
    @Select("SELECT * FROM video_face_feature WHERE process_status = 'PENDING' " +
            "ORDER BY create_time ASC")
    List<FaceFeatureEntity> selectPendingFaces();

    /**
     * 更新处理状态
     *
     * @param featureId 特征ID
     * @param status 处理状态
     * @param personId 人员ID (可选)
     * @return 影响行数
     */
    default int updateProcessStatus(Long featureId, String status, String personId) {
        FaceFeatureEntity entity = new FaceFeatureEntity();
        entity.setFeatureId(featureId);
        entity.setProcessStatus(status);
        entity.setPersonId(personId);
        entity.setUpdateTime(LocalDateTime.now());
        return updateById(entity);
    }

    /**
     * 删除过期的人脸特征
     *
     * @param days 保留天数
     * @return 删除行数
     */
    @Select("DELETE FROM video_face_feature WHERE create_time < DATE_SUB(NOW(), INTERVAL #{days} DAY)")
    int deleteExpiredFeatures(@Param("days") Integer days);

    /**
     * 统计人脸识别数据
     *
     * @param deviceId 设备ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    @Select("SELECT " +
            "COUNT(*) as totalCount, " +
            "AVG(confidence_score) as avgConfidence, " +
            "MAX(confidence_score) as maxConfidence, " +
            "MIN(confidence_score) as minConfidence " +
            "FROM video_face_feature " +
            "WHERE device_id = #{deviceId} " +
            "AND detect_time BETWEEN #{startTime} AND #{endTime}")
    Map<String, Object> getFaceStatistics(@Param("deviceId") Long deviceId,
                                           @Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);
}