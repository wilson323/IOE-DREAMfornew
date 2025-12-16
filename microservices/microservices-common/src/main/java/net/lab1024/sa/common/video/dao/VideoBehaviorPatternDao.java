package net.lab1024.sa.common.video.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.video.entity.VideoBehaviorPatternEntity;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 行为模式分析DAO接口
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Mapper
public interface VideoBehaviorPatternDao extends BaseMapper<VideoBehaviorPatternEntity> {

    /**
     * 根据模式类型查询模式
     */
    @Select("SELECT * FROM t_video_behavior_pattern WHERE pattern_type = #{patternType} AND deleted_flag = 0 ORDER BY created_time DESC")
    List<VideoBehaviorPatternEntity> selectByPatternType(@Param("patternType") Integer patternType);

    /**
     * 根据行为类型查询模式
     */
    @Select("SELECT * FROM t_video_behavior_pattern WHERE behavior_type = #{behaviorType} AND deleted_flag = 0 ORDER BY created_time DESC")
    List<VideoBehaviorPatternEntity> selectByBehaviorType(@Param("behaviorType") Integer behaviorType);

    /**
     * 根据模式状态查询模式
     */
    @Select("SELECT * FROM t_video_behavior_pattern WHERE pattern_status = #{patternStatus} AND deleted_flag = 0 ORDER BY created_time DESC")
    List<VideoBehaviorPatternEntity> selectByPatternStatus(@Param("patternStatus") Integer patternStatus);

    /**
     * 查询启用中的模式
     */
    @Select("SELECT * FROM t_video_behavior_pattern WHERE pattern_status = 1 AND deleted_flag = 0 ORDER BY pattern_priority DESC, created_time DESC")
    List<VideoBehaviorPatternEntity> selectActivePatterns();

    /**
     * 查询需要重新训练的模式
     */
    @Select("SELECT * FROM t_video_behavior_pattern WHERE next_training_time <= #{currentTime} AND pattern_status = 1 AND deleted_flag = 0 ORDER BY next_training_time ASC")
    List<VideoBehaviorPatternEntity> selectPatternsNeedingRetraining(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 查询已过期的模式
     */
    @Select("SELECT * FROM t_video_behavior_pattern WHERE valid_end_time < #{currentTime} AND pattern_status = 1 AND deleted_flag = 0 ORDER BY valid_end_time ASC")
    List<VideoBehaviorPatternEntity> selectExpiredPatterns(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 根据算法模型ID查询模式
     */
    @Select("SELECT * FROM t_video_behavior_pattern WHERE algorithm_model_id = #{algorithmModelId} AND deleted_flag = 0 ORDER BY created_time DESC")
    List<VideoBehaviorPatternEntity> selectByAlgorithmModelId(@Param("algorithmModelId") String algorithmModelId);

    /**
     * 根据优先级查询模式
     */
    @Select("SELECT * FROM t_video_behavior_pattern WHERE pattern_priority = #{priority} AND deleted_flag = 0 ORDER BY created_time DESC")
    List<VideoBehaviorPatternEntity> selectByPriority(@Param("priority") Integer priority);

    /**
     * 查询指定准确率范围的模式
     */
    @Select("SELECT * FROM t_video_behavior_pattern WHERE validation_accuracy >= #{minAccuracy} AND validation_accuracy <= #{maxAccuracy} AND deleted_flag = 0 ORDER BY validation_accuracy DESC")
    List<VideoBehaviorPatternEntity> selectByAccuracyRange(@Param("minAccuracy") Double minAccuracy, @Param("maxAccuracy") Double maxAccuracy);

    /**
     * 根据设备类型查询适用模式
     */
    @Select("SELECT * FROM t_video_behavior_pattern WHERE FIND_IN_SET(#{deviceType}, applicable_device_types) AND pattern_status = 1 AND deleted_flag = 0 ORDER BY pattern_priority DESC")
    List<VideoBehaviorPatternEntity> selectByApplicableDeviceType(@Param("deviceType") Integer deviceType);

    /**
     * 统计各模式类型的数量
     */
    @Select("SELECT pattern_type, COUNT(*) as pattern_count FROM t_video_behavior_pattern WHERE deleted_flag = 0 GROUP BY pattern_type")
    List<Map<String, Object>> countByPatternType();

    /**
     * 统计各行为类型的模式数量
     */
    @Select("SELECT behavior_type, COUNT(*) as pattern_count FROM t_video_behavior_pattern WHERE deleted_flag = 0 GROUP BY behavior_type")
    List<Map<String, Object>> countByBehaviorType();

    /**
     * 统计各状态的模式数量
     */
    @Select("SELECT pattern_status, COUNT(*) as status_count FROM t_video_behavior_pattern WHERE deleted_flag = 0 GROUP BY pattern_status")
    List<Map<String, Object>> countByPatternStatus();

    /**
     * 统计各优先级的模式数量
     */
    @Select("SELECT pattern_priority, COUNT(*) as priority_count FROM t_video_behavior_pattern WHERE deleted_flag = 0 GROUP BY pattern_priority")
    List<Map<String, Object>> countByPriority();

    /**
     * 查询模式性能指标
     */
    @Select("SELECT pattern_id, pattern_name, training_accuracy, validation_accuracy, false_positive_rate, false_negative_rate, training_samples FROM t_video_behavior_pattern WHERE deleted_flag = 0 ORDER BY validation_accuracy DESC")
    List<Map<String, Object>> selectPatternPerformanceMetrics();

    /**
     * 查询最近创建的模式
     */
    @Select("SELECT * FROM t_video_behavior_pattern WHERE deleted_flag = 0 ORDER BY created_time DESC LIMIT #{limit}")
    List<VideoBehaviorPatternEntity> selectRecentPatterns(@Param("limit") Integer limit);

    /**
     * 查询高优先级模式
     */
    @Select("SELECT * FROM t_video_behavior_pattern WHERE pattern_priority >= #{minPriority} AND pattern_status = 1 AND deleted_flag = 0 ORDER BY pattern_priority DESC")
    List<VideoBehaviorPatternEntity> selectHighPriorityPatterns(@Param("minPriority") Integer minPriority);

    /**
     * 查询高性能模式
     */
    @Select("SELECT * FROM t_video_behavior_pattern WHERE validation_accuracy >= #{minAccuracy} AND false_positive_rate <= #{maxFalsePositiveRate} AND pattern_status = 1 AND deleted_flag = 0 ORDER BY validation_accuracy DESC")
    List<VideoBehaviorPatternEntity> selectHighPerformancePatterns(@Param("minAccuracy") Double minAccuracy, @Param("maxFalsePositiveRate") Double maxFalsePositiveRate);

    /**
     * 更新模式状态
     */
    @Update("UPDATE t_video_behavior_pattern SET pattern_status = #{patternStatus}, update_time = NOW() WHERE pattern_id = #{patternId}")
    int updatePatternStatus(@Param("patternId") Long patternId, @Param("patternStatus") Integer patternStatus);

    /**
     * 批量更新模式状态
     */
    @Update("UPDATE t_video_behavior_pattern SET pattern_status = #{patternStatus}, update_time = NOW() WHERE pattern_id IN (${patternIds})")
    int batchUpdatePatternStatus(@Param("patternIds") String patternIds, @Param("patternStatus") Integer patternStatus);

    /**
     * 更新模式训练信息
     */
    @Update("UPDATE t_video_behavior_pattern SET last_training_time = #{trainingTime}, training_accuracy = #{trainingAccuracy}, validation_accuracy = #{validationAccuracy}, false_positive_rate = #{falsePositiveRate}, false_negative_rate = #{falseNegativeRate}, training_samples = #{trainingSamples}, next_training_time = #{nextTrainingTime}, pattern_version = #{version} WHERE pattern_id = #{patternId}")
    int updateTrainingInfo(@Param("patternId") Long patternId, @Param("trainingTime") LocalDateTime trainingTime, @Param("trainingAccuracy") Double trainingAccuracy, @Param("validationAccuracy") Double validationAccuracy, @Param("falsePositiveRate") Double falsePositiveRate, @Param("falseNegativeRate") Double falseNegativeRate, @Param("trainingSamples") Long trainingSamples, @Param("nextTrainingTime") LocalDateTime nextTrainingTime, @Param("version") String version);

    /**
     * 查询模式使用统计
     */
    @Select("SELECT pattern_id, pattern_name, usage_statistics FROM t_video_behavior_pattern WHERE deleted_flag = 0 ORDER BY JSON_EXTRACT(usage_statistics, '$.usageCount') DESC")
    List<Map<String, Object>> selectPatternUsageStatistics();

    /**
     * 清理过期模式
     */
    @Update("UPDATE t_video_behavior_pattern SET pattern_status = 3, update_time = NOW() WHERE valid_end_time < #{currentTime} AND pattern_status = 1 AND deleted_flag = 0")
    int cleanExpiredPatterns(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 查询模式训练计划
     */
    @Select("SELECT pattern_id, pattern_name, last_training_time, training_interval_days, next_training_time FROM t_video_behavior_pattern WHERE pattern_status = 1 AND deleted_flag = 0 ORDER BY next_training_time ASC")
    List<Map<String, Object>> selectTrainingPlan();

    /**
     * 查询需要维护的模式
     */
    @Select("SELECT * FROM t_video_behavior_pattern WHERE (validation_accuracy < #{minAccuracy} OR false_positive_rate > #{maxFalsePositiveRate}) AND pattern_status = 1 AND deleted_flag = 0 ORDER BY validation_accuracy ASC")
    List<VideoBehaviorPatternEntity> selectPatternsNeedingMaintenance(@Param("minAccuracy") Double minAccuracy, @Param("maxFalsePositiveRate") Double maxFalsePositiveRate);

    /**
     * 查询模式版本分布
     */
    @Select("SELECT pattern_version, COUNT(*) as version_count FROM t_video_behavior_pattern WHERE deleted_flag = 0 GROUP BY pattern_version ORDER BY version_count DESC")
    List<Map<String, Object>> selectVersionDistribution();

    /**
     * 查询算法模型使用统计
     */
    @Select("SELECT algorithm_model_id, COUNT(*) as model_count, AVG(training_accuracy) as avg_accuracy FROM t_video_behavior_pattern WHERE deleted_flag = 0 GROUP BY algorithm_model_id ORDER BY model_count DESC")
    List<Map<String, Object>> selectAlgorithmModelUsage();

    /**
     * 更新模式性能指标
     */
    @Update("UPDATE t_video_behavior_pattern SET performance_metrics = #{performanceMetrics}, update_time = NOW() WHERE pattern_id = #{patternId}")
    int updatePerformanceMetrics(@Param("patternId") Long patternId, @Param("performanceMetrics") String performanceMetrics);

    /**
     * 更新模式使用统计
     */
    @Update("UPDATE t_video_behavior_pattern SET usage_statistics = #{usageStatistics}, update_time = NOW() WHERE pattern_id = #{patternId}")
    int updateUsageStatistics(@Param("patternId") Long patternId, @Param("usageStatistics") String usageStatistics);
}