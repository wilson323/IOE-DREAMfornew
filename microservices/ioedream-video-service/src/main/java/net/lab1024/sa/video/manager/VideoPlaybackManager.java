package net.lab1024.sa.video.manager;

import net.lab1024.sa.video.util.RedisTemplateUtil;
import net.lab1024.sa.video.dao.VideoPlaybackDao;
import net.lab1024.sa.video.domain.entity.VideoRecordingEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Video Playback Manager - Handle complex video playback business logic
 *
 * This manager handles complex video playback operations that require:
 * - Stream processing and transcoding
 * - Recording management
 * - Storage optimization
 * - Bandwidth management
 * - Multi-format support
 *
 * Architecture: Service → Manager → DAO
 * Complex video processing logic should be implemented here to avoid
 * code redundancy across multiple video services.
 *
 * @author Smart Admin 2025
 * @version 1.0
 * @since 2025-11-18
 */
@Slf4j
@Service
public class VideoPlaybackManager {

    @Resource
    private VideoPlaybackDao videoPlaybackDao;

    @Resource
    private RedisTemplateUtil redisTemplateUtil;

    private static final String PLAYBACK_CACHE_KEY = "video:playback:";
    private static final String STREAM_CACHE_KEY = "video:stream:";
    private static final String RECORDING_CACHE_KEY = "video:recording:";
    private static final int CACHE_EXPIRE_MINUTES = 10;

    /**
     * Generate playback URL for recorded video
     *
     * @param deviceId Device ID
     * @param recordingId Recording ID
     * @param quality Video quality (HD, SD, etc.)
     * @return Playback URL
     */
    public String getPlaybackUrl(Long deviceId, Long recordingId, String quality) {
        log.info("Generating playback URL: deviceId={}, recordingId={}, quality={}", deviceId, recordingId, quality);

        try {
            // Check cache first
            String cacheKey = PLAYBACK_CACHE_KEY + deviceId + ":" + recordingId + ":" + quality;
            Object cachedObj = redisTemplateUtil.get(cacheKey);
            String cachedUrl = cachedObj != null ? cachedObj.toString() : null;

            if (StringUtils.hasText(cachedUrl)) {
                log.info("Returning cached playback URL for recording: {}", recordingId);
                return cachedUrl;
            }

            // Generate playback URL based on recording information
            VideoRecordingEntity recording = getRecordingInfo(recordingId);
            if (recording == null || !recording.getDeviceId().equals(deviceId)) {
                log.error("Recording not found or device mismatch: recordingId={}, deviceId={}", recordingId, deviceId);
                return null;
            }

            String playbackUrl = generatePlaybackUrl(recording, quality);

            // Cache the URL
            redisTemplateUtil.set(cacheKey, playbackUrl, CACHE_EXPIRE_MINUTES * 60, TimeUnit.SECONDS);

            log.info("Generated playback URL: recordingId={}, url={}", recordingId, playbackUrl);
            return playbackUrl;

        } catch (Exception e) {
            log.error("Error generating playback URL: deviceId={}, recordingId={}, quality={}",
                     deviceId, recordingId, quality, e);
            return null;
        }
    }

    /**
     * Generate live stream URL for real-time viewing
     *
     * @param deviceId Device ID
     * @param quality Stream quality
     * @param streamType Stream type (HLS, RTMP, etc.)
     * @return Stream URL
     */
    public String getLiveStreamUrl(Long deviceId, String quality, String streamType) {
        log.info("Generating live stream URL: deviceId={}, quality={}, streamType={}", deviceId, quality, streamType);

        try {
            String cacheKey = STREAM_CACHE_KEY + deviceId + ":" + quality + ":" + streamType;
            Object cachedObj = redisTemplateUtil.get(cacheKey);
            String cachedUrl = cachedObj != null ? cachedObj.toString() : null;

            if (StringUtils.hasText(cachedUrl)) {
                log.info("Returning cached live stream URL for device: {}", deviceId);
                return cachedUrl;
            }

            // Generate live stream URL
            String streamUrl = generateLiveStreamUrl(deviceId, quality, streamType);

            // Cache the URL (shorter cache time for live streams)
            redisTemplateUtil.set(cacheKey, streamUrl, 2 * 60, TimeUnit.SECONDS);

            log.info("Generated live stream URL: deviceId={}, url={}", deviceId, streamUrl);
            return streamUrl;

        } catch (Exception e) {
            log.error("Error generating live stream URL: deviceId={}, quality={}, streamType={}",
                     deviceId, quality, streamType, e);
            return null;
        }
    }

    /**
     * Start recording for a device
     *
     * @param deviceId Device ID
     * @param recordingType Recording type (MANUAL, SCHEDULED, MOTION)
     * @param duration Recording duration in seconds
     * @param operatorId Operator ID
     * @return Recording ID if successful, null otherwise
     */
    public Long startRecording(Long deviceId, String recordingType, Integer duration, Long operatorId) {
        log.info("Starting recording: deviceId={}, recordingType={}, duration={}, operatorId={}",
                deviceId, recordingType, duration, operatorId);

        try {
            // Create recording entity
            VideoRecordingEntity recording = new VideoRecordingEntity();
            recording.setDeviceId(deviceId);
            recording.setRecordingType(recordingType);
            recording.setStartTime(LocalDateTime.now());
            recording.setStatus("RECORDING");
            recording.setDuration(duration);
            recording.setCreateUserId(operatorId);
            recording.setCreateTime(LocalDateTime.now());
            // Note: BaseEntity does not have tenantId field, tenant context is handled by framework

            // Insert recording
            int result = videoPlaybackDao.insert(recording);
            if (result <= 0) {
                log.error("Failed to create recording: deviceId={}", deviceId);
                return null;
            }

            Long recordingId = recording.getRecordingId();

            // Cache recording info
            String cacheKey = RECORDING_CACHE_KEY + recordingId;
            redisTemplateUtil.set(cacheKey, recording, CACHE_EXPIRE_MINUTES * 60, TimeUnit.SECONDS);

            log.info("Recording started successfully: recordingId={}, deviceId={}", recordingId, deviceId);
            return recordingId;

        } catch (Exception e) {
            log.error("Error starting recording: deviceId={}, recordingType={}", deviceId, recordingType, e);
            return null;
        }
    }

    /**
     * Stop recording
     *
     * @param recordingId Recording ID
     * @param operatorId Operator ID
     * @return true if successful, false otherwise
     */
    public boolean stopRecording(Long recordingId, Long operatorId) {
        log.info("Stopping recording: recordingId={}, operatorId={}", recordingId, operatorId);

        try {
            VideoRecordingEntity recording = getRecordingInfo(recordingId);
            if (recording == null) {
                log.error("Recording not found: recordingId={}", recordingId);
                return false;
            }

            if (!"RECORDING".equals(recording.getStatus())) {
                log.warn("Recording is not active: recordingId={}, status={}", recordingId, recording.getStatus());
                return false;
            }

            // Update recording
            recording.setEndTime(LocalDateTime.now());
            recording.setStatus("COMPLETED");
            recording.setUpdateUserId(operatorId);
            recording.setUpdateTime(LocalDateTime.now());

            int result = videoPlaybackDao.updateById(recording);
            if (result <= 0) {
                log.error("Failed to update recording: recordingId={}", recordingId);
                return false;
            }

            // Clear cache
            String cacheKey = RECORDING_CACHE_KEY + recordingId;
            redisTemplateUtil.del(cacheKey);

            log.info("Recording stopped successfully: recordingId={}", recordingId);
            return true;

        } catch (Exception e) {
            log.error("Error stopping recording: recordingId={}", recordingId, e);
            return false;
        }
    }

    /**
     * Download recording file
     *
     * @param recordingId Recording ID
     * @param downloadFormat Download format (MP4, AVI, etc.)
     * @return Download URL
     */
    public String downloadRecording(Long recordingId, String downloadFormat) {
        log.info("Downloading recording: recordingId={}, format={}", recordingId, downloadFormat);

        try {
            VideoRecordingEntity recording = getRecordingInfo(recordingId);
            if (recording == null) {
                log.error("Recording not found: recordingId={}", recordingId);
                return null;
            }

            if (!"COMPLETED".equals(recording.getStatus())) {
                log.error("Recording is not completed: recordingId={}, status={}", recordingId, recording.getStatus());
                return null;
            }

            // Generate download URL
            String downloadUrl = generateDownloadUrl(recording, downloadFormat);

            log.info("Generated download URL: recordingId={}, url={}", recordingId, downloadUrl);
            return downloadUrl;

        } catch (Exception e) {
            log.error("Error downloading recording: recordingId={}, format={}", recordingId, downloadFormat, e);
            return null;
        }
    }

    /**
     * Get recording information from cache or database
     *
     * @param recordingId Recording ID
     * @return Recording entity
     */
    private VideoRecordingEntity getRecordingInfo(Long recordingId) {
        try {
            // Check cache first
            String cacheKey = RECORDING_CACHE_KEY + recordingId;
            @SuppressWarnings("unchecked")
            VideoRecordingEntity cached = (VideoRecordingEntity) redisTemplateUtil.get(cacheKey);

            if (cached != null) {
                return cached;
            }

            // Get from database
            VideoRecordingEntity recording = videoPlaybackDao.selectById(recordingId);

            if (recording != null) {
                // Cache the result
                redisTemplateUtil.set(cacheKey, recording, CACHE_EXPIRE_MINUTES * 60, TimeUnit.SECONDS);
            }

            return recording;

        } catch (Exception e) {
            log.error("Error getting recording info: recordingId={}", recordingId, e);
            return null;
        }
    }

    /**
     * Generate playback URL based on recording information
     *
     * @param recording Recording entity
     * @param quality Video quality
     * @return Playback URL
     */
    private String generatePlaybackUrl(VideoRecordingEntity recording, String quality) {
        // This would typically involve complex URL generation logic
        // For demonstration, return a constructed URL
        String baseUrl = "https://example.com/recordings";
        String recordingPath = recording.getDeviceId() + "/" + recording.getRecordingId();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        return String.format("%s/%s/%s/%s.m3u8", baseUrl, recordingPath, quality, timestamp);
    }

    /**
     * Generate live stream URL
     *
     * @param deviceId Device ID
     * @param quality Stream quality
     * @param streamType Stream type
     * @return Stream URL
     */
    private String generateLiveStreamUrl(Long deviceId, String quality, String streamType) {
        String baseUrl = "https://example.com/live";

        switch (streamType.toUpperCase()) {
            case "HLS":
                return String.format("%s/%s/%s/playlist.m3u8", baseUrl, deviceId, quality);
            case "RTMP":
                return String.format("rtmp://example.com/live/%s_%s", deviceId, quality);
            case "WEBRTC":
                return String.format("wss://example.com/webrtc/%s/%s", deviceId, quality);
            default:
                return String.format("%s/%s/%s/stream", baseUrl, deviceId, quality);
        }
    }

    /**
     * Generate download URL
     *
     * @param recording Recording entity
     * @param downloadFormat Download format
     * @return Download URL
     */
    private String generateDownloadUrl(VideoRecordingEntity recording, String downloadFormat) {
        String baseUrl = "https://example.com/downloads";
        String recordingPath = recording.getDeviceId() + "/" + recording.getRecordingId();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        return String.format("%s/%s/%s.%s?t=%s", baseUrl, recordingPath, recording.getRecordingId(),
                           downloadFormat.toLowerCase(), timestamp);
    }

    /**
     * Check if device supports recording
     *
     * @param deviceId Device ID
     * @return true if supported, false otherwise
     */
    public boolean supportsRecording(Long deviceId) {
        try {
            // This would typically check device capabilities
            // For demonstration, assume all devices support recording
            return true;

        } catch (Exception e) {
            log.error("Error checking recording support: deviceId={}", deviceId, e);
            return false;
        }
    }

    /**
     * Get recording statistics
     *
     * @param deviceId Device ID (optional, null for all devices)
     * @param startDate Start date
     * @param endDate End date
     * @return Recording statistics
     */
    public Map<String, Object> getRecordingStatistics(Long deviceId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting recording statistics: deviceId={}, startDate={}, endDate={}",
                deviceId, startDate, endDate);

        try {
            Map<String, Object> statistics = new HashMap<>();

            // This would typically query the database for recording statistics
            // For demonstration, return placeholder data
            statistics.put("totalRecordings", 0);
            statistics.put("totalDuration", 0);
            statistics.put("storageUsed", "0 MB");
            statistics.put("averageDuration", 0);

            return statistics;

        } catch (Exception e) {
            log.error("Error getting recording statistics: deviceId={}", deviceId, e);
            return Collections.emptyMap();
        }
    }
}