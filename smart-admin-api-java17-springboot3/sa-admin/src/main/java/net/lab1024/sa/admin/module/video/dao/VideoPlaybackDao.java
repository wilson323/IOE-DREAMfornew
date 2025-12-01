package net.lab1024.sa.admin.module.video.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import net.lab1024.sa.admin.module.video.domain.entity.VideoRecordingEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Video Playback DAO - Data access layer for video recording and playback operations
 *
 * This DAO handles database operations for video recordings including:
 * - Recording metadata storage
 * - Playback history tracking
 * - Recording lifecycle management
 * - Storage optimization operations
 *
 * Architecture: Service → Manager → DAO → Database
 * Follows MyBatis-Plus conventions for enhanced productivity.
 *
 * @author Smart Admin 2025
 * @version 1.0
 * @since 2025-11-18
 */
@Mapper
public interface VideoPlaybackDao extends BaseMapper<VideoRecordingEntity> {

    /**
     * Query recordings by device ID and date range
     *
     * @param deviceId Device ID
     * @param startTime Start time
     * @param endTime End time
     * @return List of recordings
     */
    List<VideoRecordingEntity> selectRecordingsByDeviceAndTime(@Param("deviceId") Long deviceId,
                                                              @Param("startTime") LocalDateTime startTime,
                                                              @Param("endTime") LocalDateTime endTime);

    /**
     * Query recordings by status
     *
     * @param status Recording status
     * @param limit Maximum number of records
     * @return List of recordings
     */
    List<VideoRecordingEntity> selectRecordingsByStatus(@Param("status") String status,
                                                        @Param("limit") Integer limit);

    /**
     * Query active recordings for a device
     *
     * @param deviceId Device ID
     * @return Active recording if exists
     */
    VideoRecordingEntity selectActiveRecordingByDevice(@Param("deviceId") Long deviceId);

    /**
     * Query recordings by recording type
     *
     * @param recordingType Recording type (MANUAL, SCHEDULED, MOTION)
     * @param startTime Start time (optional)
     * @param endTime End time (optional)
     * @return List of recordings
     */
    List<VideoRecordingEntity> selectRecordingsByType(@Param("recordingType") String recordingType,
                                                      @Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime);

    /**
     * Get recording statistics by device
     *
     * @param deviceId Device ID
     * @param startTime Start time (optional)
     * @param endTime End time (optional)
     * @return Statistics map containing count, total duration, etc.
     */
    java.util.Map<String, Object> selectRecordingStatisticsByDevice(@Param("deviceId") Long deviceId,
                                                                    @Param("startTime") LocalDateTime startTime,
                                                                    @Param("endTime") LocalDateTime endTime);

    /**
     * Update recording status
     *
     * @param recordingId Recording ID
     * @param status New status
     * @param updateTime Update time
     * @param updateUserId Update user ID
     * @return Number of affected rows
     */
    int updateRecordingStatus(@Param("recordingId") Long recordingId,
                              @Param("status") String status,
                              @Param("updateTime") LocalDateTime updateTime,
                              @Param("updateUserId") Long updateUserId);

    /**
     * Update recording end time and status
     *
     * @param recordingId Recording ID
     * @param endTime End time
     * @param status New status
     * @param updateUserId Update user ID
     * @return Number of affected rows
     */
    int updateRecordingEnd(@Param("recordingId") Long recordingId,
                           @Param("endTime") LocalDateTime endTime,
                           @Param("status") String status,
                           @Param("updateUserId") Long updateUserId);

    /**
     * Query old recordings for cleanup
     *
     * @param beforeTime Time threshold
     * @param status Recording status filter (optional)
     * @param limit Maximum number of records
     * @return List of old recordings
     */
    List<VideoRecordingEntity> selectOldRecordingsForCleanup(@Param("beforeTime") LocalDateTime beforeTime,
                                                             @Param("status") String status,
                                                             @Param("limit") Integer limit);

    /**
     * Delete recordings older than specified time
     *
     * @param beforeTime Time threshold
     * @param status Recording status filter (optional)
     * @return Number of deleted records
     */
    int deleteOldRecordings(@Param("beforeTime") LocalDateTime beforeTime,
                            @Param("status") String status);

    /**
     * Query recordings by user
     *
     * @param createUserId User ID who created the recording
     * @param startTime Start time (optional)
     * @param endTime End time (optional)
     * @param offset Offset for pagination
     * @param limit Limit for pagination
     * @return List of recordings
     */
    List<VideoRecordingEntity> selectRecordingsByUser(@Param("createUserId") Long createUserId,
                                                      @Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime,
                                                      @Param("offset") Integer offset,
                                                      @Param("limit") Integer limit);

    /**
     * Count recordings by user
     *
     * @param createUserId User ID who created the recording
     * @param startTime Start time (optional)
     * @param endTime End time (optional)
     * @return Count of recordings
     */
    Long countRecordingsByUser(@Param("createUserId") Long createUserId,
                               @Param("startTime") LocalDateTime startTime,
                               @Param("endTime") LocalDateTime endTime);

    /**
     * Query recordings requiring processing
     *
     * @param status Current status
     * @param limit Maximum number of records
     * @return List of recordings to process
     */
    List<VideoRecordingEntity> selectRecordingsForProcessing(@Param("status") String status,
                                                             @Param("limit") Integer limit);

    /**
     * Get storage usage statistics
     *
     * @param startTime Start time (optional)
     * @param endTime End time (optional)
     * @return Storage statistics
     */
    java.util.Map<String, Object> selectStorageUsageStatistics(@Param("startTime") LocalDateTime startTime,
                                                               @Param("endTime") LocalDateTime endTime);

    /**
     * Query recordings with pagination
     *
     * @param deviceId Device ID filter (optional)
     * @param recordingType Recording type filter (optional)
     * @param status Status filter (optional)
     * @param startTime Start time filter (optional)
     * @param endTime End time filter (optional)
     * @param offset Offset for pagination
     * @param limit Limit for pagination
     * @return List of recordings
     */
    List<VideoRecordingEntity> selectRecordingsWithPagination(@Param("deviceId") Long deviceId,
                                                              @Param("recordingType") String recordingType,
                                                              @Param("status") String status,
                                                              @Param("startTime") LocalDateTime startTime,
                                                              @Param("endTime") LocalDateTime endTime,
                                                              @Param("offset") Integer offset,
                                                              @Param("limit") Integer limit);

    /**
     * Count recordings with filters
     *
     * @param deviceId Device ID filter (optional)
     * @param recordingType Recording type filter (optional)
     * @param status Status filter (optional)
     * @param startTime Start time filter (optional)
     * @param endTime End time filter (optional)
     * @return Count of recordings
     */
    Long countRecordingsWithFilters(@Param("deviceId") Long deviceId,
                                   @Param("recordingType") String recordingType,
                                   @Param("status") String status,
                                   @Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);

    /**
     * Update recording file information
     *
     * @param recordingId Recording ID
     * @param filePath File path
     * @param fileSize File size
     * @param fileFormat File format
     * @param updateTime Update time
     * @return Number of affected rows
     */
    int updateRecordingFileInfo(@Param("recordingId") Long recordingId,
                               @Param("filePath") String filePath,
                               @Param("fileSize") Long fileSize,
                               @Param("fileFormat") String fileFormat,
                               @Param("updateTime") LocalDateTime updateTime);

    /**
     * Get recordings by quality
     *
     * @param quality Video quality
     * @param startTime Start time (optional)
     * @param endTime End time (optional)
     * @return List of recordings
     */
    List<VideoRecordingEntity> selectRecordingsByQuality(@Param("quality") String quality,
                                                         @Param("startTime") LocalDateTime startTime,
                                                         @Param("endTime") LocalDateTime endTime);
}