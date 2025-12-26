package net.lab1024.sa.video.job;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.video.dao.VideoRecordingTaskDao;
import net.lab1024.sa.common.entity.video.VideoRecordingTaskEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 视频录像文件清理定时任务
 * 负责清理过期的录像文件和任务记录
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Component
@Slf4j
public class VideoRecordingCleanupJob {

    @Resource
    private VideoRecordingTaskDao videoRecordingTaskDao;

    @Value("${video.recording.retention-days:30}")
    private Integer retentionDays;

    @Value("${video.recording.storage-path:/recordings}")
    private String storagePath;

    /**
     * 清理过期的已完成录像任务
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredRecordingTasks() {
        try {
            log.info("[录像清理] 开始清理过期录像任务: retentionDays={}", retentionDays);

            // 计算清理截止时间
            LocalDateTime beforeDate = LocalDateTime.now().minusDays(retentionDays);

            // 删除数据库中的过期任务记录
            Integer deletedRows = videoRecordingTaskDao.deleteCompletedTasksBefore(beforeDate);

            log.info("[录像清理] 过期任务清理完成: beforeDate={}, deletedRows={}", beforeDate, deletedRows);

        } catch (Exception e) {
            log.error("[录像清理] 清理过期任务异常: error={}", e.getMessage(), e);
        }
    }

    /**
     * 清理孤立的录像文件
     * 每天凌晨3点执行
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupOrphanRecordingFiles() {
        try {
            log.info("[录像清理] 开始清理孤立录像文件: storagePath={}", storagePath);

            // 统计清理的文件数量和大小
            int deletedFiles = 0;
            long totalSize = 0;

            // 遍历存储目录
            File storageDir = new File(storagePath);
            if (storageDir.exists() && storageDir.isDirectory()) {
                File[] dateDirs = storageDir.listFiles();
                if (dateDirs != null) {
                    for (File dateDir : dateDirs) {
                        if (dateDir.isDirectory()) {
                            // 检查日期目录是否过期
                            if (isDateDirExpired(dateDir.getName())) {
                                long size = deleteDirectory(dateDir);
                                if (size > 0) {
                                    deletedFiles++;
                                    totalSize += size;
                                }
                            } else {
                                // 检查文件是否孤立（数据库中没有记录）
                                cleanupOrphanFilesInDirectory(dateDir);
                            }
                        }
                    }
                }
            }

            log.info("[录像清理] 孤立文件清理完成: deletedFiles={}, totalSize={}",
                    deletedFiles, formatFileSize(totalSize));

        } catch (Exception e) {
            log.error("[录像清理] 清理孤立文件异常: error={}", e.getMessage(), e);
        }
    }

    /**
     * 检查并清理目录中的孤立文件
     */
    private void cleanupOrphanFilesInDirectory(File directory) {
        try {
            File[] files = directory.listFiles();
            if (files == null) {
                return;
            }

            for (File file : files) {
                if (file.isFile()) {
                    // 检查文件是否在数据库中存在记录
                    // TODO: 通过文件路径查询数据库
                    // 如果没有记录，视为孤立文件，可以删除
                } else if (file.isDirectory()) {
                    cleanupOrphanFilesInDirectory(file);
                }
            }

        } catch (Exception e) {
            log.error("[录像清理] 清理目录文件失败: directory={}, error={}",
                    directory.getAbsolutePath(), e.getMessage());
        }
    }

    /**
     * 检查日期目录是否过期
     */
    private boolean isDateDirExpired(String dateDirName) {
        try {
            // 假设目录名格式为 yyyy或 yyyy/MM
            String[] parts = dateDirName.replace("\\", "/").split("/");
            if (parts.length == 1 && parts[0].matches("\\d{4}")) {
                // 格式: yyyy
                int year = Integer.parseInt(parts[0]);
                LocalDateTime dirDate = LocalDateTime.of(year, 12, 31, 23, 59, 59);
                LocalDateTime expireDate = LocalDateTime.now().minusDays(retentionDays);
                return dirDate.isBefore(expireDate);
            } else if (parts.length == 2 && parts[0].matches("\\d{4}") && parts[1].matches("\\d{2}")) {
                // 格式: yyyy/MM
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                // 获取该月最后一天
                int lastDay = java.time.YearMonth.of(year, month).lengthOfMonth();
                LocalDateTime dirDate = LocalDateTime.of(year, month, lastDay, 23, 59, 59);
                LocalDateTime expireDate = LocalDateTime.now().minusDays(retentionDays);
                return dirDate.isBefore(expireDate);
            }
            return false;
        } catch (Exception e) {
            log.warn("[录像清理] 解析日期目录失败: dateDirName={}, error={}",
                    dateDirName, e.getMessage());
            return false;
        }
    }

    /**
     * 递归删除目录
     */
    private long deleteDirectory(File directory) {
        long totalSize = 0;

        try {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        totalSize += file.length();
                        if (file.delete()) {
                            log.debug("[录像清理] 删除文件: {}", file.getAbsolutePath());
                        }
                    } else if (file.isDirectory()) {
                        totalSize += deleteDirectory(file);
                    }
                }
            }

            // 删除空目录
            if (directory.delete()) {
                log.debug("[录像清理] 删除目录: {}", directory.getAbsolutePath());
            }

        } catch (Exception e) {
            log.error("[录像清理] 删除目录失败: directory={}, error={}",
                    directory.getAbsolutePath(), e.getMessage());
        }

        return totalSize;
    }

    /**
     * 检查存储空间使用情况
     * 每6小时执行一次
     */
    @Scheduled(cron = "0 0 */6 * * ?")
    public void checkStorageSpace() {
        try {
            log.debug("[录像清理] 检查存储空间: storagePath={}", storagePath);

            File storageDir = new File(storagePath);
            if (!storageDir.exists()) {
                log.warn("[录像清理] 存储目录不存在: {}", storagePath);
                return;
            }

            // 获取磁盘可用空间
            long usableSpace = storageDir.getUsableSpace();
            long totalSpace = storageDir.getTotalSpace();
            double usagePercent = (double) (totalSpace - usableSpace) / totalSpace * 100;

            log.info("[录像清理] 存储空间使用情况: usableSpace={}, totalSpace={}, usage={}%",
                    formatFileSize(usableSpace), formatFileSize(totalSpace),
                    String.format("%.2f%%", usagePercent));

            // 如果存储空间使用超过80%，发送告警
            if (usagePercent > 80) {
                log.warn("[录像清理] 存储空间不足: usage={}%, 建议清理旧录像文件",
                        String.format("%.2f%%", usagePercent));
                // TODO: 发送告警通知
            }

        } catch (Exception e) {
            log.error("[录像清理] 检查存储空间异常: error={}", e.getMessage(), e);
        }
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long bytes) {
        if (bytes <= 0) {
            return "0 B";
        }

        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.2f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    /**
     * 手动触发清理录像文件
     *
     * @param beforeDate 清理此日期之前的文件
     * @return 清理的文件数量
     */
    public int manualCleanup(LocalDateTime beforeDate) {
        log.info("[录像清理] 手动触发清理: beforeDate={}", beforeDate);

        int deletedCount = 0;
        File storageDir = new File(storagePath);

        if (storageDir.exists() && storageDir.isDirectory()) {
            File[] dateDirs = storageDir.listFiles();
            if (dateDirs != null) {
                for (File dateDir : dateDirs) {
                    if (dateDir.isDirectory() && isDateDirExpired(dateDir.getName())) {
                        deleteDirectory(dateDir);
                        deletedCount++;
                    }
                }
            }
        }

        log.info("[录像清理] 手动清理完成: deletedCount={}", deletedCount);
        return deletedCount;
    }
}
