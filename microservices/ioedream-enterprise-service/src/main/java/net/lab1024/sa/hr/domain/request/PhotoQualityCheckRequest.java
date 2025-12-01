package net.lab1024.sa.hr.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

/**
 * 照片质量检查请求
 *
 * @author IOE-DREAM Team
 */
@Data
public class PhotoQualityCheckRequest {

    /**
     * 照片Base64数据
     */
    @NotBlank(message = "照片数据不能为空")
    private String imageBase64;

    /**
     * 照片用途（用于不同的质量标准）
     */
    @NotNull(message = "照片用途不能为空")
    private PhotoUsageType usageType;

    /**
     * 照片用途类型
     */
    public enum PhotoUsageType {
        /**
         * 人脸识别
         */
        FACE_RECOGNITION,

        /**
         * 员工证件照
         */
        EMPLOYEE_PHOTO,

        /**
         * 访客照片
         */
        VISITOR_PHOTO
    }
}