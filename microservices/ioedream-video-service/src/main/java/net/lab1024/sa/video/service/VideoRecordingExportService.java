package net.lab1024.sa.video.service;

import net.lab1024.sa.video.domain.entity.VideoRecordingExportTaskEntity;
import net.lab1024.sa.video.domain.form.VideoRecordingExportForm;
import org.springframework.http.ResponseEntity;

public interface VideoRecordingExportService {

    Long createExportTask(VideoRecordingExportForm exportForm, Long userId);

    VideoRecordingExportTaskEntity getExportTaskStatus(Long taskId);

    ResponseEntity<byte[]> downloadExportFile(Long taskId);

    void processExportTask(Long taskId);
}
