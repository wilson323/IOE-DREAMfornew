package net.lab1024.sa.admin.module.video.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.admin.module.video.domain.entity.VideoRecordingEntity;

/**
 * 视频录像记录DAO
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Mapper
public interface VideoRecordDao extends BaseMapper<VideoRecordingEntity> {

    /**
     * 分页查询录像记录
     *
     * @param pageParam 分页参数
     * @param condition 查询条件
     * @return 分页结果
     */
    PageResult<VideoRecordingEntity> selectPage(PageParam pageParam, VideoRecordingEntity condition);

    /**
     * 统计今日录像数量
     *
     * @param deviceId 设备ID
     * @return 今日录像数量
     */
    Long getTodayRecordCount(@Param("deviceId") Long deviceId);

    /**
     * 统计今日录像时长（分钟）
     *
     * @param deviceId 设备ID
     * @return 今日录像时长
     */
    Long getTodayRecordDuration(@Param("deviceId") Long deviceId);

    /**
     * 获取正在进行的录像数量
     *
     * @param deviceId 设备ID
     * @return 进行中的录像数量
     */
    Long getActiveRecordingCount(@Param("deviceId") Long deviceId);

    /**
     * 根据设备ID查询录像记录
     *
     * @param deviceId 设备ID
     * @param limit    限制数量
     * @return 录像记录列表
     */
    List<VideoRecordingEntity> selectByDeviceId(@Param("deviceId") Long deviceId, @Param("limit") Integer limit);

    /**
     * 根据录像状态查询记录
     *
     * @param recordStatus 录像状态
     * @param limit        限制数量
     * @return 录像记录列表
     */
    List<VideoRecordingEntity> selectByRecordStatus(@Param("recordStatus") String recordStatus,
            @Param("limit") Integer limit);

    /**
     * 根据时间范围查询录像记录
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param limit     限制数量
     * @return 录像记录列表
     */
    List<VideoRecordingEntity> selectByTimeRange(@Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("limit") Integer limit);

    /**
     * 查询大文件录像记录
     *
     * @param minFileSize 最小文件大小（字节）
     * @param limit       限制数量
     * @return 录像记录列表
     */
    List<VideoRecordingEntity> selectLargeFiles(@Param("minFileSize") Long minFileSize, @Param("limit") Integer limit);

    /**
     * 删除指定时间之前的录像记录
     *
     * @param beforeTime 时间点
     * @return 删除行数
     */
    int deleteBeforeTime(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 批量更新录像状态
     *
     * @param recordIds    录像ID列表
     * @param recordStatus 新状态
     * @return 更新行数
     */
    int batchUpdateRecordStatus(@Param("recordIds") List<Long> recordIds,
            @Param("recordStatus") String recordStatus);

    /**
     * 统计各类型录像数量
     *
     * @param deviceId 设备ID（可选）
     * @return 统计结果
     */
    List<java.util.Map<String, Object>> countRecordsByType(@Param("deviceId") Long deviceId);

    /**
     * 按时间范围统计录像数量
     *
     * @param deviceId  设备ID（可选）
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @return 录像数量
     */
    Long countRecordsByTimeRange(@Param("deviceId") Long deviceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 按时间范围统计录像总时长（秒）
     *
     * @param deviceId  设备ID（可选）
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @return 总时长（秒）
     */
    Long sumRecordDurationByTimeRange(@Param("deviceId") Long deviceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 按时间范围统计录像总存储空间（字节）
     *
     * @param deviceId  设备ID（可选）
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @return 总存储空间（字节）
     */
    Long sumRecordFileSizeByTimeRange(@Param("deviceId") Long deviceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
