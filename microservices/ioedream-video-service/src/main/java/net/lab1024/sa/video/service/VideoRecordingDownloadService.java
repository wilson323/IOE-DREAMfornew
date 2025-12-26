package net.lab1024.sa.video.service;

import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 视频录像下载服务接口
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
public interface VideoRecordingDownloadService {

    /**
     * 下载录像文件（支持断点续传）
     *
     * @param taskId 任务ID
     * @param request HTTP请求（用于解析Range头）
     * @return 文件响应实体
     */
    ResponseEntity<byte[]> downloadRecording(Long taskId, HttpServletRequest request);

    /**
     * 获取录像文件下载URL
     *
     * @param taskId 任务ID
     * @param expireSeconds URL有效期（秒）
     * @return 下载URL
     */
    String getDownloadUrl(Long taskId, Integer expireSeconds);

    /**
     * 批量下载录像文件（打包为ZIP）
     *
     * @param taskIds 任务ID列表
     * @param request HTTP请求
     * @return ZIP文件响应实体
     */
    ResponseEntity<byte[]> batchDownloadRecordings(List<Long> taskIds, HttpServletRequest request);

    /**
     * 验证下载权限
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean validateDownloadPermission(Long taskId, Long userId);

    /**
     * 记录下载日志
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @param ipAddress IP地址
     */
    void recordDownloadLog(Long taskId, Long userId, String ipAddress);

    /**
     * 获取录像文件信息
     *
     * @param taskId 任务ID
     * @return 文件信息（大小、类型、创建时间）
     */
    Map<String, Object> getRecordingFileInfo(Long taskId);

    /**
     * 检查文件是否存在
     *
     * @param taskId 任务ID
     * @return 文件是否存在
     */
    boolean checkFileExists(Long taskId);

    /**
     * 删除录像文件
     *
     * @param taskId 任务ID
     * @return 是否删除成功
     */
    boolean deleteRecordingFile(Long taskId);
}
