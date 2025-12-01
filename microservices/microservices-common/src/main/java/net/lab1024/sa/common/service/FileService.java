package net.lab1024.sa.common.service;

import net.lab1024.sa.common.domain.vo.FileVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * 文件服务接口
 *
 * @Author 1024创新实验室: 卓大
 * @Date 2023-10-01 00:00:00
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright <a href"https://1024lab.net">1024创新实验室</a>
 */
public interface FileService {

    /**
     * 文件上传
     *
     * @param multipartFile 文件
     * @return 文件key
     */
    String upload(MultipartFile multipartFile);

    /**
     * 获取文件下载URL
     *
     * @param fileKey 文件key
     * @return 下载URL
     */
    String getDownloadUrl(String fileKey);

    /**
     * 获取文件预览URL
     *
     * @param fileKey 文件key
     * @return 预览URL
     */
    String getPreviewUrl(String fileKey);

    /**
     * 获取文件URL（用于程序化）
     *
     * @param fileKey 文件key
     * @return 文件URL
     */
    String getFileUrl(String fileKey);

    /**
     * 批量获取文件信息
     *
     * @param fileKeys 文件key列表
     * @return 文件信息列表
     */
    List<FileVO> getFileList(List<String> fileKeys);

    /**
     * 下载文件
     *
     * @param fileKey 文件key
     * @param response HTTP响应
     */
    void download(String fileKey, HttpServletResponse response);

    /**
     * 删除文件
     *
     * @param fileKey 文件key
     * @return 是否删除成功
     */
    boolean delete(String fileKey);
}