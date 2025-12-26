package net.lab1024.sa.consume.domain.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 消费卡挂失表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@Data
@Schema(description = "消费卡挂失表单")
public class ConsumeCardLossForm {

    @NotNull(message = "卡ID不能为空")
    @Schema(description = "卡ID", example = "1001")
    private Long cardId;

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @NotBlank(message = "卡号不能为空")
    @Schema(description = "消费卡号", example = "CARD20231201001")
    private String cardNo;

    @NotBlank(message = "挂失原因不能为空")
    @Schema(description = "挂失原因", example = "卡片丢失", allowableValues = {"卡片丢失", "卡片被盗", "卡片损坏", "其他原因"})
    private String lossReason;

    @Schema(description = "详细描述", example = "在餐厅不慎丢失")
    private String description;

    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @Schema(description = "客户端IP", example = "192.168.1.100")
    private String clientIp;

    @Schema(description = "设备信息", example = "iPhone 14 Pro")
    private String deviceInfo;
}
