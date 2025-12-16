package net.lab1024.sa.consume.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 订餐核销表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Data
public class MealOrderVerifyForm {

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单编号（与订单ID二选一）
     */
    private String orderNo;

    /**
     * 取餐方式：QR_CODE-二维码, FACE-刷脸, CARD-刷卡
     */
    @NotNull(message = "取餐方式不能为空")
    private String pickupMethod;

    /**
     * 设备ID
     */
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    /**
     * 用户ID（刷脸/刷卡时使用）
     */
    private Long userId;

    /**
     * 卡号（刷卡时使用）
     */
    private String cardNo;
}
