package net.lab1024.sa.video.service;

import java.util.Map;

public interface VideoFFmpegService {

    byte[] generateThumbnail(String filePath, Integer timeOffset);

    byte[] generatePreviewClip(String filePath, Integer startTime, Integer duration);

    Map<String, Object> getVideoInfo(String filePath);

    boolean isVideoFileValid(String filePath);

    String extractFrame(String filePath, Integer timeOffset, String outputPath);

    String cutVideoClip(String filePath, Integer startTime, Integer duration, String outputPath);
}
