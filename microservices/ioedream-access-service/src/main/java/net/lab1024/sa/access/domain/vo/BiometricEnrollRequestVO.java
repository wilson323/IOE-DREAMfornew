package net.lab1024.sa.access.domain.vo;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

/**
 * 生物特征注册请求VO
 * <p>
 * 用于移动端批量注册生物特征的请求对象
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@Data
public class BiometricEnrollRequestVO {

    /**
     * 人脸图片文件
     */
    private MultipartFile faceImage;

    /**
     * 指纹特征数据
     */
    private String fingerprintData;

    /**
     * 手指类型 1-拇指 2-食指 3-中指 4-无名指 5-小指
     */
    private Integer fingerType;

    /**
     * 虹膜图片文件（左眼）
     */
    private MultipartFile leftIrisImage;

    /**
     * 虹膜图片文件（右眼）
     */
    private MultipartFile rightIrisImage;

    /**
     * 备注信息
     */
    private String remark;
}
