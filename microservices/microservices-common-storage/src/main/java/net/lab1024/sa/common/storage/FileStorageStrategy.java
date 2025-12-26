package net.lab1024.sa.common.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;

/**
 * 文件存储策略接口
 * <p>
 * 定义统一的文件存储操作接口,支持多种存储实现(MinIO/本地文件/阿里云OSS等)
 * 通过策略模式实现存储方式的灵活切换
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-18
 */
public interface FileStorageStrategy {

    /**
     * 存储文件
     *
     * @param file 上传的文件
     * @param path 存储路径
     * @return 文件访问URL
     */
    String storeFile(MultipartFile file, String path);

    /**
     * 获取文件输入流
     *
     * @param path 文件路径
     * @return 文件输入流
     */
    InputStream getFileStream(String path);

    /**
     * 删除文件
     *
     * @param path 文件路径
     * @return 是否删除成功
     */
    boolean deleteFile(String path);

    /**
     * 检查文件是否存在
     *
     * @param path 文件路径
     * @return 文件是否存在
     */
    boolean fileExists(String path);

    /**
     * 获取文件信息
     *
     * @param path 文件路径
     * @return 文件信息
     */
    Map<String, Object> getFileInfo(String path);

    /**
     * 上传文件（兼容旧调用）
     *
     * @param file 上传的文件
     * @param path 存储路径
     * @return 文件访问URL
     */
    default String uploadFile(MultipartFile file, String path) {
        return storeFile(file, path);
    }

    /**
     * 获取预签名上传URL（默认不支持）
     * <p>
     * 返回Map包含：
     * - uploadUrl: 预签名上传URL
     * - method: HTTP方法（PUT）
     * - expirySeconds: 过期秒数（字符串格式）
     * </p>
     *
     * @param folder        文件夹路径（如 "access/snapshots"）
     * @param fileName      文件名（如 "snapshot.jpg"）
     * @param expireSeconds 过期秒数
     * @return 预签名URL信息Map，如果存储类型不支持则返回null
     */
    default Map<String, String> getPresignedUploadUrl(String folder, String fileName, int expireSeconds) {
        return null;
    }
}
