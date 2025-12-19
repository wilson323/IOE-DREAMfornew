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
     * 上传文件
     *
     * @param file MultipartFile文件对象
     * @param folder 文件夹路径(如: "access/snapshots/device001")
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file, String folder);

    /**
     * 上传输入流
     *
     * @param inputStream 输入流
     * @param fileName 文件名
     * @param contentType 内容类型(如: image/jpeg)
     * @param size 文件大小(字节)
     * @param folder 文件夹路径
     * @return 文件访问URL
     */
    String uploadStream(InputStream inputStream, String fileName, String contentType, long size, String folder);

    /**
     * 生成预签名上传URL
     * <p>
     * 设备可直接使用此URL上传文件,无需经过应用服务器
     * 适用于大文件上传,减轻服务器压力
     * </p>
     *
     * @param folder 文件夹路径
     * @param fileName 文件名
     * @param expirySeconds 有效期(秒)
     * @return 预签名上传URL和相关信息
     */
    Map<String, String> getPresignedUploadUrl(String folder, String fileName, int expirySeconds);

    /**
     * 获取文件访问URL
     *
     * @param filePath 文件路径
     * @return 文件访问URL
     */
    String getFileUrl(String filePath);

    /**
     * 删除文件
     *
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    boolean deleteFile(String filePath);

    /**
     * 检查文件是否存在
     *
     * @param filePath 文件路径
     * @return 文件是否存在
     */
    boolean fileExists(String filePath);

    /**
     * 获取存储类型
     *
     * @return 存储类型(minio/local/aliyun-oss)
     */
    String getStorageType();
}
