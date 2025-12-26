package net.lab1024.sa.video.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.video.dao.VideoRecordingTaskDao;
import net.lab1024.sa.video.service.VideoStorageMonitorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class VideoStorageMonitorServiceImpl implements VideoStorageMonitorService {

    @Resource
    private VideoRecordingTaskDao videoRecordingTaskDao;

    @Value("${video.recording.storage-path:/recordings}")
    private String storagePath;

    @Value("${video.monitor.alert-enabled:true}")
    private Boolean alertEnabled;

    @Value("${video.monitor.storage-threshold:80}")
    private Double storageThreshold;

    @Value("${video.monitor.critical-threshold:90}")
    private Double criticalThreshold;

    @Value("${video.monitor.alert-emails:}")
    private String alertEmails;

    @Override
    @Scheduled(cron = "0 0 */6 * * ?")
    public void checkStorageSpace() {
        log.info("[存储监控] 开始检查存储空间");

        try {
            File storageDir = new File(storagePath);
            if (!storageDir.exists() || !storageDir.isDirectory()) {
                log.warn("[存储监控] 存储目录不存在: {}", storagePath);
                return;
            }

            long totalSpace = storageDir.getTotalSpace();
            long usableSpace = storageDir.getUsableSpace();
            long usedSpace = totalSpace - usableSpace;
            double usagePercent = (double) usedSpace / totalSpace * 100;

            log.info("[存储监控] 存储使用情况: 总空间={}, 已使用={}, 可用={}, 使用率={}%", 
                    formatFileSize(totalSpace), 
                    formatFileSize(usedSpace), 
                    formatFileSize(usableSpace), 
                    String.format("%.2f", usagePercent));

            if (usagePercent >= criticalThreshold) {
                Map<String, Object> alertData = new HashMap<>();
                alertData.put("usagePercent", usagePercent);
                alertData.put("usedSpace", usedSpace);
                alertData.put("totalSpace", totalSpace);
                alertData.put("storagePath", storagePath);

                sendAlert("CRITICAL", 
                        String.format("存储空间严重不足！使用率: %.2f%%", usagePercent), 
                        alertData);

            } else if (usagePercent >= storageThreshold) {
                Map<String, Object> alertData = new HashMap<>();
                alertData.put("usagePercent", usagePercent);
                alertData.put("usedSpace", usedSpace);
                alertData.put("totalSpace", totalSpace);
                alertData.put("storagePath", storagePath);

                sendAlert("WARNING", 
                        String.format("存储空间即将耗尽！使用率: %.2f%%", usagePercent), 
                        alertData);
            }

        } catch (Exception e) {
            log.error("[存储监控] 检查存储空间异常: error={}", e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getStorageStatus() {
        Map<String, Object> status = new HashMap<>();

        try {
            File storageDir = new File(storagePath);
            if (!storageDir.exists() || !storageDir.isDirectory()) {
                status.put("error", "存储目录不存在");
                return status;
            }

            long totalSpace = storageDir.getTotalSpace();
            long usableSpace = storageDir.getUsableSpace();
            long usedSpace = totalSpace - usableSpace;
            double usagePercent = (double) usedSpace / totalSpace * 100;

            status.put("totalSpace", totalSpace);
            status.put("totalSpaceFormatted", formatFileSize(totalSpace));
            status.put("usedSpace", usedSpace);
            status.put("usedSpaceFormatted", formatFileSize(usedSpace));
            status.put("usableSpace", usableSpace);
            status.put("usableSpaceFormatted", formatFileSize(usableSpace));
            status.put("usagePercent", usagePercent);
            status.put("usagePercentFormatted", String.format("%.2f%%", usagePercent));
            status.put("threshold", storageThreshold);
            status.put("criticalThreshold", criticalThreshold);
            status.put("alertLevel", getAlertLevel(usagePercent));
            status.put("storagePath", storagePath);

        } catch (Exception e) {
            log.error("[存储监控] 获取存储状态失败: error={}", e.getMessage(), e);
            status.put("error", e.getMessage());
        }

        return status;
    }

    @Override
    public void sendAlert(String alertType, String message, Map<String, Object> data) {
        log.warn("[存储监控] 告警[{}]: {}", alertType, message);

        if (data != null) {
            log.warn("[存储监控] 告警数据: {}", data);
        }

        if (!alertEnabled) {
            log.info("[存储监控] 告警功能已禁用，跳过发送");
            return;
        }

        try {
            if (alertEmails != null && !alertEmails.isEmpty()) {
                sendEmailAlert(alertEmails, alertType + ": 存储空间告警", message, data);
            }

            sendSystemNotification(alertType, message, data);

        } catch (Exception e) {
            log.error("[存储监控] 发送告警失败: error={}", e.getMessage(), e);
        }
    }

    @Override
    public boolean isAlertEnabled() {
        return alertEnabled;
    }

    @Override
    public double getStorageThreshold() {
        return storageThreshold;
    }

    private String getAlertLevel(double usagePercent) {
        if (usagePercent >= criticalThreshold) {
            return "CRITICAL";
        } else if (usagePercent >= storageThreshold) {
            return "WARNING";
        } else if (usagePercent >= storageThreshold * 0.8) {
            return "NOTICE";
        } else {
            return "NORMAL";
        }
    }

    private void sendEmailAlert(String emails, String subject, String message, Map<String, Object> data) {
        log.info("[存储监控] 发送邮件告警: to={}, subject={}", emails, subject);
    }

    private void sendSystemNotification(String alertType, String message, Map<String, Object> data) {
        log.info("[存储监控] 发送系统通知: type={}, message={}", alertType, message);
    }

    private String formatFileSize(long bytes) {
        if (bytes <= 0) {
            return "0 B";
        }

        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.2f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }
}
