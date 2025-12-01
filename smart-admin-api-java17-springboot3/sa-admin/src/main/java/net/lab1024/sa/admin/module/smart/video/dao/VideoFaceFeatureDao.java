package net.lab1024.sa.admin.module.smart.video.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.base.common.domain.entity.FaceFeatureEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 人脸特征DAO
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Mapper
public interface VideoFaceFeatureDao extends BaseMapper<FaceFeatureEntity> {

    /**
     * 统计今日人脸识别数量
     *
     * @param deviceId 设备ID
     * @return 今日识别数量
     */
    Long getTodayFaceCount(@Param("deviceId") Long deviceId);

    /**
     * 根据设备ID查询人脸特征
     *
     * @param deviceId 设备ID
     * @param limit 限制数量
     * @return 人脸特征列表
     */
    List<FaceFeatureEntity> selectByDeviceId(@Param("deviceId") Long deviceId, @Param("limit") Integer limit);

    /**
     * 根据人员ID查询人脸特征
     *
     * @param personId 人员ID
     * @return 人脸特征列表
     */
    List<FaceFeatureEntity> selectByPersonId(@Param("personId") Long personId);

    /**
     * 根据处理状态查询人脸特征
     *
     * @param processStatus 处理状态
     * @return 人脸特征列表
     */
    List<FaceFeatureEntity> selectByProcessStatus(@Param("processStatus") String processStatus);

    /**
     * 根据时间范围查询人脸特征
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 人脸特征列表
     */
    List<FaceFeatureEntity> selectByTimeRange(@Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);

    /**
     * 查询高置信度人脸特征
     *
     * @param minConfidence 最小置信度
     * @return 人脸特征列表
     */
    List<FaceFeatureEntity> selectByMinConfidence(@Param("minConfidence") Double minConfidence);

    /**
     * 删除指定时间之前的人脸特征
     *
     * @param beforeTime 时间点
     * @return 删除行数
     */
    int deleteBeforeTime(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 批量更新处理状态
     *
     * @param featureIds 特征ID列表
     * @param processStatus 新状态
     * @return 更新行数
     */
    int batchUpdateProcessStatus(@Param("featureIds") List<Long> featureIds,
                                @Param("processStatus") String processStatus);
}