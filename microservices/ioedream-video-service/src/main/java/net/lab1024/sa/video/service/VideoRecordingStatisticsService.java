package net.lab1024.sa.video.service;

import net.lab1024.sa.video.domain.form.VideoRecordingStatisticsQueryForm;

import java.time.LocalDateTime;
import java.util.Map;

public interface VideoRecordingStatisticsService {

    Map<String, Object> getDeviceStatistics(String deviceId, LocalDateTime startTime, LocalDateTime endTime);

    Map<String, Object> getTimeStatistics(LocalDateTime startTime, LocalDateTime endTime, Integer granularity);

    Map<String, Object> getQualityStatistics(LocalDateTime startTime, LocalDateTime endTime);

    Map<String, Object> getStatusStatistics(LocalDateTime startTime, LocalDateTime endTime);

    Map<String, Object> getStorageStatistics();

    Map<String, Object> getOverviewStatistics(LocalDateTime startTime, LocalDateTime endTime);

    Map<String, Object> getCustomStatistics(VideoRecordingStatisticsQueryForm queryForm);
}
