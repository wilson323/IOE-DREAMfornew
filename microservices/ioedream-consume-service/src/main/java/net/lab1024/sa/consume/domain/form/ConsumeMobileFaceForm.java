package net.lab1024.sa.consume.domain.form;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 移动端人脸识别消费表单
 *
 * @author IOE-DREAM Team
 * @since 2025-12-22
 */
@Data
public class ConsumeMobileFaceForm {

    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @NotBlank(message = "人脸特征不能为空")
    private String faceFeatures;

    @DecimalMin(value = "0.01", message = "消费金额必须大于0")
    private BigDecimal amount;
}
