package net.lab1024.sa.common.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务接口
 *
 * @Author 1024创新实验室-主任: 卓大
 * @Date 2023-10-01 00:00:00
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright <a href"https://1024lab.net">1024创新实验室</a>
 */
public interface IFileStorageService {

    /**
     * 存储文件
     *
     * @param file 文件
     * @return 文件key
     */
    String store(MultipartFile file);

    /**
     * 存储字节数组
     *
     * @param bytes    文件字节数组
     * @param fileName 文件名
     * @return 文件key
     */
    String store(byte[] bytes, String fileName);

    /**
     * 获取文件字节数组
     *
     * @param fileKey 文件key
     * @return 文件字节数组
     */
    byte[] getBytes(String fileKey);

    /**
     * 获取文件URL
     *
     * @param fileKey 文件key
     * @return 文件URL
     */
    String getUrl(String fileKey);

    /**
     * 删除文件
     *
     * @param fileKey 文件key
     * @return 是否删除成功
     */
    boolean delete(String fileKey);

    /**
     * 检查文件是否存在
     *
     * @param fileKey 文件key
     * @return 是否存在
     */
    boolean exists(String fileKey);
}