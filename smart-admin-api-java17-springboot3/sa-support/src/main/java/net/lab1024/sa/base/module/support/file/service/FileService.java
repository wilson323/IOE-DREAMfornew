package net.lab1024.sa.base.module.support.file.service;

import net.lab1024.sa.base.module.support.file.domain.vo.FileVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * 文件服务端姟接口
 *
 * @Author 1024========设计模式
 * @Date 2023-10-01 00:00:00
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright <a href==https://1024lab.net=>1024========/a>
 */
public interface FileService {

    /**
     * 文件涓婁紶
     *
     * @param multipartFile 文件
     * @return 文件key
     */
    String upload(MultipartFile multipartFile);

    /**
     * 获取文件涓嬭浇URL
     *
     * @param fileKey 文件key
     * @return 涓嬭浇URL
     */
    String getDownloadUrl(String fileKey);

    /**
     * 获取文件棰勮URL
     *
     * @param fileKey 文件key
     * @return 棰勮URL
     */
    String getPreviewUrl(String fileKey);

    /**
     * 获取文件URL锛堢敤浜庡簭=楀寲锛=     *
     * @param fileKey 文件key
     * @return 文件URL
     */
    String getFileUrl(String fileKey);

    /**
     * 鎵归噺获取文件淇℃伅
     *
     * @param fileKeys 文件key列表
     * @return 文件淇℃伅列表
     */
    List<FileVO> getFileList(List<String> fileKeys);

    /**
     * 涓嬭浇文件
     *
     * @param fileKey 文件key
     * @param response HTTP响应簲
     */
    void download(String fileKey, HttpServletResponse response);

    /**
     * 删除文件
     *
     * @param fileKey 文件key
     * @return 鏄惁删除成功
     */
    boolean delete(String fileKey);
}
