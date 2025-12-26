package net.lab1024.sa.video.service;

import java.util.Map;

public interface VideoStorageMonitorService {

    void checkStorageSpace();

    Map<String, Object> getStorageStatus();

    void sendAlert(String alertType, String message, Map<String, Object> data);

    boolean isAlertEnabled();

    double getStorageThreshold();
}
