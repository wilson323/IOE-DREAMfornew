package net.lab1024.sa.consume.domain.form;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 移动端人脸识别消费表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class ConsumeMobileFaceForm {

    /**
     * 设备ID
     */
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    /**
     * 人脸特征（Base64编码）
     */
    @NotBlank(message = "人脸特征不能为空")
    private String faceFeatures;

    /**
     * 消费金额
     */
    @NotNull(message = "消费金额不能为空")
    @Positive(message = "消费金额必须大于0")
    private BigDecimal amount;

    /**
     * 区域ID
     */
    private Long areaId;
}



