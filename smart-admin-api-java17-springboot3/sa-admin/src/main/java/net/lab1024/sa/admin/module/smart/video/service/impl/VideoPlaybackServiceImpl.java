package net.lab1024.sa.admin.module.smart.video.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.lab1024.sa.admin.module.smart.video.service.VideoPlaybackService;
import net.lab1024.sa.admin.module.smart.video.dao.VideoRecordDao;
import net.lab1024.sa.admin.module.smart.video.manager.VideoPlaybackManager;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.entity.VideoRecordEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Video Playback Service Implementation
 *
 * Handles video recording playback, query, download, backup and management functions
 * Follows repowiki architecture design: Service layer handles business logic and transaction management,
 * Manager layer handles complex playback operations and third-party integrations
 *
 * @author SmartAdmin Team
 * @since 2025-11-18
 */
@Slf4j
@Service
@Transactional(rollbackFor = Throwable.class)
public class VideoPlaybackServiceImpl extends ServiceImpl implements VideoPlaybackService {

    @Resource
    private VideoRecordDao videoRecordDao;

    @Resource
    private VideoPlaybackManager videoPlaybackManager;

    /**
     * Query video records with pagination
     *
     * @param pageParam pagination parameters
     * @param deviceId device ID (optional)
     * @param startTime start time (optional)
     * @param endTime end time (optional)
     * @param recordType record type (optional)
     * @return pagination result
     */
    @Override
    public PageResult<VideoRecordEntity> pageVideoRecords(PageParam pageParam, Long deviceId,
                                                      LocalDateTime startTime, LocalDateTime endTime,
                                                      String recordType) {
        try {
            log.info("Query video records: deviceId={}, startTime={}, endTime={}, recordType={}",
                    deviceId, startTime, endTime, recordType);

            if (pageParam == null) {
                log.warn("Page parameters cannot be null");
                return PageResult.error("Page parameters cannot be null");
            }

            // Use manager for complex query logic
            PageResult<VideoRecordEntity> result = videoPlaybackManager.queryVideoRecords(pageParam, deviceId, startTime, endTime, recordType);

            log.info("Video records queried successfully: totalCount={}", result.getTotalCount());
            return result;

        } catch (Exception e) {
            log.error("Failed to query video records: deviceId={}, recordType={}", deviceId, recordType, e);
            return PageResult.error("Failed to query video records: " + e.getMessage());
        }
    }

    /**
     * Get video record detail
     *
     * @param recordId record ID
     * @return record detail
     */
    @Override
    public VideoRecordEntity getVideoRecordDetail(Long recordId) {
        try {
            log.info("Get video record detail: recordId={}", recordId);

            if (recordId == null) {
                log.warn("Record ID cannot be null");
                return null;
            }

            VideoRecordEntity record = videoPlaybackManager.getVideoRecordDetail(recordId);
            if (record == null) {
                log.warn("Video record not found: recordId={}", recordId);
                return null;
            }

            log.info("Video record detail retrieved successfully: recordId={}", recordId);
            return record;

        } catch (Exception e) {
            log.error("Failed to get video record detail: recordId={}", recordId, e);
            return null;
        }
    }

    /**
     * Get video playback URL
     *
     * @param recordId record ID
     * @param startTime start time (optional, for segment playback)
     * @param endTime end time (optional, for segment playback)
     * @return playback URL
     */
    @Override
    public String getPlaybackUrl(Long recordId, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            log.info("Get video playback URL: recordId={}, startTime={}, endTime={}", recordId, startTime, endTime);

            if (recordId == null) {
                throw new IllegalArgumentException("Record ID cannot be null");
            }

            // Validate time range
            if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
                throw new IllegalArgumentException("Start time must be before end time");
            }

            // Use manager for complex playback URL generation
            String playbackUrl = videoPlaybackManager.generatePlaybackUrl(recordId, startTime, endTime);

            log.info("Video playback URL generated successfully: recordId={}", recordId);
            return playbackUrl;

        } catch (Exception e) {
            log.error("Failed to get video playback URL: recordId={}, startTime={}, endTime={}", recordId, startTime, endTime, e);
            return null;
        }
    }

    /**
     * Get record timeline
     *
     * @param recordId record ID
     * @return timeline data
     */
    @Override
    public Object getRecordTimeline(Long recordId) {
        try {
            log.info("Get record timeline: recordId={}", recordId);

            if (recordId == null) {
                throw new IllegalArgumentException("Record ID cannot be null");
            }

            // Use manager for complex timeline generation
            Object timeline = videoPlaybackManager.generateRecordTimeline(recordId);

            log.info("Record timeline generated successfully: recordId={}", recordId);
            return timeline;

        } catch (Exception e) {
            log.error("Failed to get record timeline: recordId={}", recordId, e);
            return Map.of("error", "Failed to generate timeline: " + e.getMessage());
        }
    }

    /**
     * Get record thumbnails
     *
     * @param recordId record ID
     * @return thumbnail list
     */
    @Override
    public List<Object> getRecordThumbnails(Long recordId) {
        try {
            log.info("Get record thumbnails: recordId={}", recordId);

            if (recordId == null) {
                throw new IllegalArgumentException("Record ID cannot be null");
            }

            // Use manager for thumbnail generation
            List<Object> thumbnails = videoPlaybackManager.generateRecordThumbnails(recordId);

            log.info("Record thumbnails generated successfully: recordId={}, count={}", recordId, thumbnails.size());
            return thumbnails;

        } catch (Exception e) {
            log.error("Failed to get record thumbnails: recordId={}", recordId, e);
            return List.of();
        }
    }

    /**
     * Mark record segment
     *
     * @param recordId record ID
     * @param markType mark type (BOOKMARK/EVENT/ALERT)
     * @param markTime mark time
     * @param markDesc mark description
     * @return mark result
     */
    @Override
    public boolean markRecordSegment(Long recordId, String markType, LocalDateTime markTime, String markDesc) {
        try {
            log.info("Mark record segment: recordId={}, markType={}, markTime={}, markDesc={}", recordId, markType, markTime, markDesc);

            if (recordId == null || markType == null || markTime == null) {
                throw new IllegalArgumentException("Record ID, mark type and mark time cannot be null");
            }

            // Validate mark type
            if (!markType.matches("BOOKMARK|EVENT|ALERT")) {
                throw new IllegalArgumentException("Invalid mark type. Supported types: BOOKMARK, EVENT, ALERT");
            }

            // Use manager for complex marking logic
            boolean result = videoPlaybackManager.markRecordSegment(recordId, markType, markTime, markDesc);

            log.info("Record segment marked successfully: recordId={}, markType={}, result={}", recordId, markType, result);
            return result;

        } catch (Exception e) {
            log.error("Failed to mark record segment: recordId={}, markType={}", recordId, markType, e);
            return false;
        }
    }

    /**
     * Get record marks
     *
     * @param recordId record ID
     * @return mark list
     */
    @Override
    public List<Object> getRecordMarks(Long recordId) {
        try {
            log.info("Get record marks: recordId={}", recordId);

            if (recordId == null) {
                throw new IllegalArgumentException("Record ID cannot be null");
            }

            // Use manager for mark retrieval
            List<Object> marks = videoPlaybackManager.getRecordMarks(recordId);

            log.info("Record marks retrieved successfully: recordId={}, count={}", recordId, marks.size());
            return marks;

        } catch (Exception e) {
            log.error("Failed to get record marks: recordId={}", recordId, e);
            return List.of();
        }
    }

    /**
     * Clip video record
     *
     * @param recordId original record ID
     * @param startTime clip start time
     * @param endTime clip end time
     * @param clipName clip name
     * @return clipped record ID
     */
    @Override
    public Long clipVideoRecord(Long recordId, LocalDateTime startTime, LocalDateTime endTime, String clipName) {
        try {
            log.info("Clip video record: recordId={}, startTime={}, endTime={}, clipName={}", recordId, startTime, endTime, clipName);

            if (recordId == null || startTime == null || endTime == null || clipName == null) {
                throw new IllegalArgumentException("Record ID, start time, end time and clip name cannot be null");
            }

            if (startTime.isAfter(endTime)) {
                throw new IllegalArgumentException("Start time must be before end time");
            }

            // Use manager for complex clipping logic
            Long clippedRecordId = videoPlaybackManager.clipVideoRecord(recordId, startTime, endTime, clipName);

            log.info("Video record clipped successfully: recordId={}, clippedRecordId={}", recordId, clippedRecordId);
            return clippedRecordId;

        } catch (Exception e) {
            log.error("Failed to clip video record: recordId={}, clipName={}", recordId, clipName, e);
            return null;
        }
    }

    /**
     * Batch download records
     *
     * @param recordIds record ID list
     * @param format download format (MP4/AVI/MOV)
     * @return download task ID
     */
    @Override
    public String batchDownloadRecords(List<Long> recordIds, String format) {
        try {
            log.info("Batch download video records: recordCount={}, format={}", recordIds != null ? recordIds.size() : 0, format);

            if (recordIds == null || recordIds.isEmpty()) {
                throw new IllegalArgumentException("Record ID list cannot be empty");
            }

            if (format == null || format.trim().isEmpty()) {
                throw new IllegalArgumentException("Format cannot be empty");
            }

            // Validate format
            if (!format.matches("MP4|AVI|MOV")) {
                throw new IllegalArgumentException("Invalid format. Supported formats: MP4, AVI, MOV");
            }

            // Use manager for batch download logic
            String downloadTaskId = videoPlaybackManager.batchDownloadRecords(recordIds, format);

            log.info("Video records batch download started: recordCount={}, format={}, taskId={}",
                    recordIds.size(), format, downloadTaskId);
            return downloadTaskId;

        } catch (Exception e) {
            log.error("Failed to batch download video records: format={}", format, e);
            return null;
        }
    }

    /**
     * Get download status
     *
     * @param downloadTaskId download task ID
     * @return download status information
     */
    @Override
    public Object getDownloadStatus(String downloadTaskId) {
        try {
            log.info("Get download status: downloadTaskId={}", downloadTaskId);

            if (downloadTaskId == null || downloadTaskId.trim().isEmpty()) {
                throw new IllegalArgumentException("Download task ID cannot be empty");
            }

            // Use manager for download status retrieval
            Object downloadStatus = videoPlaybackManager.getDownloadStatus(downloadTaskId);

            log.info("Download status retrieved successfully: downloadTaskId={}", downloadTaskId);
            return downloadStatus;

        } catch (Exception e) {
            log.error("Failed to get download status: downloadTaskId={}", downloadTaskId, e);
            return Map.of("error", "Failed to get download status: " + e.getMessage());
        }
    }

    /**
     * Backup records
     *
     * @param recordIds record ID list
     * @param backupType backup type (CLOUD/LOCAL)
     * @return backup task ID
     */
    @Override
    public String backupRecords(List<Long> recordIds, String backupType) {
        try {
            log.info("Backup video records: recordCount={}, backupType={}", recordIds != null ? recordIds.size() : 0, backupType);

            if (recordIds == null || recordIds.isEmpty()) {
                throw new IllegalArgumentException("Record ID list cannot be empty");
            }

            if (backupType == null || backupType.trim().isEmpty()) {
                throw new IllegalArgumentException("Backup type cannot be empty");
            }

            // Validate backup type
            if (!backupType.matches("CLOUD|LOCAL")) {
                throw new IllegalArgumentException("Invalid backup type. Supported types: CLOUD, LOCAL");
            }

            // Use manager for backup logic
            String backupTaskId = videoPlaybackManager.backupRecords(recordIds, backupType);

            log.info("Video records backup started: recordCount={}, backupType={}, taskId={}",
                    recordIds.size(), backupType, backupTaskId);
            return backupTaskId;

        } catch (Exception e) {
            log.error("Failed to backup video records: backupType={}", backupType, e);
            return null;
        }
    }

    /**
     * Get backup status
     *
     * @param backupTaskId backup task ID
     * @return backup status information
     */
    @Override
    public Object getBackupStatus(String backupTaskId) {
        try {
            log.info("Get backup status: backupTaskId={}", backupTaskId);

            if (backupTaskId == null || backupTaskId.trim().isEmpty()) {
                throw new IllegalArgumentException("Backup task ID cannot be empty");
            }

            // Use manager for backup status retrieval
            Object backupStatus = videoPlaybackManager.getBackupStatus(backupTaskId);

            log.info("Backup status retrieved successfully: backupTaskId={}", backupTaskId);
            return backupStatus;

        } catch (Exception e) {
            log.error("Failed to get backup status: backupTaskId={}", backupTaskId, e);
            return Map.of("error", "Failed to get backup status: " + e.getMessage());
        }
    }

    /**
     * Export records
     *
     * @param recordIds record ID list
     * @param exportType export type (REPORT/EVIDENCE/ARCHIVE)
     * @return export task ID
     */
    @Override
    public String exportRecords(List<Long> recordIds, String exportType) {
        try {
            log.info("Export video records: recordCount={}, exportType={}", recordIds != null ? recordIds.size() : 0, exportType);

            if (recordIds == null || recordIds.isEmpty()) {
                throw new IllegalArgumentException("Record ID list cannot be empty");
            }

            if (exportType == null || exportType.trim().isEmpty()) {
                throw new IllegalArgumentException("Export type cannot be empty");
            }

            // Validate export type
            if (!exportType.matches("REPORT|EVIDENCE|ARCHIVE")) {
                throw new IllegalArgumentException("Invalid export type. Supported types: REPORT, EVIDENCE, ARCHIVE");
            }

            // Use manager for export logic
            String exportTaskId = videoPlaybackManager.exportRecords(recordIds, exportType);

            log.info("Video records export started: recordCount={}, exportType={}, taskId={}",
                    recordIds.size(), exportType, exportTaskId);
            return exportTaskId;

        } catch (Exception e) {
            log.error("Failed to export video records: exportType={}", exportType, e);
            return null;
        }
    }

    /**
     * Get export status
     *
     * @param exportTaskId export task ID
     * @return export status information
     */
    @Override
    public Object getExportStatus(String exportTaskId) {
        try {
            log.info("Get export status: exportTaskId={}", exportTaskId);

            if (exportTaskId == null || exportTaskId.trim().isEmpty()) {
                throw new IllegalArgumentException("Export task ID cannot be empty");
            }

            // Use manager for export status retrieval
            Object exportStatus = videoPlaybackManager.getExportStatus(exportTaskId);

            log.info("Export status retrieved successfully: exportTaskId={}", exportTaskId);
            return exportStatus;

        } catch (Exception e) {
            log.error("Failed to get export status: exportTaskId={}", exportTaskId, e);
            return Map.of("error", "Failed to get export status: " + e.getMessage());
        }
    }

    /**
     * Share record
     *
     * @param recordId record ID
     * @param shareType share type (LINK/TOKEN/EMBED)
     * @param expireTime expire time
     * @return share link or token
     */
    @Override
    public String shareRecord(Long recordId, String shareType, LocalDateTime expireTime) {
        try {
            log.info("Share video record: recordId={}, shareType={}, expireTime={}", recordId, shareType, expireTime);

            if (recordId == null || shareType == null || shareType.trim().isEmpty()) {
                throw new IllegalArgumentException("Record ID and share type cannot be null or empty");
            }

            // Validate share type
            if (!shareType.matches("LINK|TOKEN|EMBED")) {
                throw new IllegalArgumentException("Invalid share type. Supported types: LINK, TOKEN, EMBED");
            }

            // Validate expire time
            if (expireTime != null && expireTime.isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Expire time must be in the future");
            }

            // Use manager for sharing logic
            String shareResult = videoPlaybackManager.shareRecord(recordId, shareType, expireTime);

            log.info("Video record shared successfully: recordId={}, shareType={}", recordId, shareType);
            return shareResult;

        } catch (Exception e) {
            log.error("Failed to share video record: recordId={}, shareType={}", recordId, shareType, e);
            return null;
        }
    }

    /**
     * Cancel record share
     *
     * @param shareId share ID
     * @return cancel result
     */
    @Override
    public boolean cancelRecordShare(String shareId) {
        try {
            log.info("Cancel video record share: shareId={}", shareId);

            if (shareId == null || shareId.trim().isEmpty()) {
                throw new IllegalArgumentException("Share ID cannot be null or empty");
            }

            // Use manager for share cancellation logic
            boolean result = videoPlaybackManager.cancelRecordShare(shareId);

            log.info("Video record share cancelled successfully: shareId={}, result={}", shareId, result);
            return result;

        } catch (Exception e) {
            log.error("Failed to cancel video record share: shareId={}", shareId, e);
            return false;
        }
    }

    /**
     * Get record statistics
     *
     * @param deviceId device ID (optional)
     * @param startTime start time
     * @param endTime end time
     * @return statistical analysis data
     */
    @Override
    public Object getRecordStatistics(Long deviceId, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            log.info("Get record statistics: deviceId={}, startTime={}, endTime={}", deviceId, startTime, endTime);

            if (startTime == null || endTime == null) {
                throw new IllegalArgumentException("Start time and end time cannot be null");
            }

            if (startTime.isAfter(endTime)) {
                throw new IllegalArgumentException("Start time must be before end time");
            }

            // Use manager for statistics generation
            Object statistics = videoPlaybackManager.getRecordStatistics(deviceId, startTime, endTime);

            log.info("Record statistics generated successfully: deviceId={}", deviceId);
            return statistics;

        } catch (Exception e) {
            log.error("Failed to get record statistics: deviceId={}, startTime={}, endTime={}", deviceId, startTime, endTime, e);
            return Map.of("error", "Failed to generate statistics: " + e.getMessage());
        }
    }

    /**
     * Query storage space
     *
     * @return storage space information
     */
    @Override
    public Object getStorageSpaceInfo() {
        try {
            log.info("Get storage space information");

            // Use manager for storage space analysis
            Object storageInfo = videoPlaybackManager.getStorageSpaceInfo();

            log.info("Storage space information retrieved successfully");
            return storageInfo;

        } catch (Exception e) {
            log.error("Failed to get storage space information", e);
            return Map.of("error", "Failed to get storage space info: " + e.getMessage());
        }
    }
}