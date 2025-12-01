package net.lab1024.sa.hr.service;

import net.lab1024.sa.hr.domain.request.PhotoQualityCheckRequest;
import net.lab1024.sa.hr.domain.response.PhotoQualityCheckResponse;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * 员工照片质量验证服务
 * 根据用户指导，仅实现照片质量验证，不实现生物识别算法
 * 集成在人力资源服务中，用于员工人脸照片质量检查
 *
 * @author IOE-DREAM Team
 */
public interface EmployeePhotoQualityService {

    /**
     * 检查员工照片质量
     *
     * @param request 包含照片数据(Base64)和用途的请求
     * @return 照片质量检查结果
     */
    PhotoQualityCheckResponse checkPhotoQuality(PhotoQualityCheckRequest request);

    /**
     * 验证照片格式是否有效
     *
     * @param imageBase64 Base64编码的图片数据
     * @return 是否为有效图片格式
     */
    boolean isValidImageFormat(String imageBase64);

    /**
     * 获取图片尺寸
     *
     * @param imageBase64 Base64编码的图片数据
     * @return 图片尺寸信息
     */
    ImageDimensions getImageDimensions(String imageBase64) throws IOException;

    /**
     * 检查图片分辨率是否符合要求
     *
     * @param width 图片宽度
     * @param height 图片高度
     * @param usageType 用途类型
     * @return 是否符合最低分辨率要求
     */
    boolean meetsMinimumResolution(int width, int height, PhotoQualityCheckRequest.PhotoUsageType usageType);

    /**
     * 检查图片大小是否合理
     *
     * @param imageSizeBytes 图片大小（字节）
     * @param usageType 用途类型
     * @return 是否在合理范围内
     */
    boolean isReasonableImageSize(long imageSizeBytes, PhotoQualityCheckRequest.PhotoUsageType usageType);

    /**
     * 图片尺寸信息
     */
    record ImageDimensions(int width, int height) {}
}