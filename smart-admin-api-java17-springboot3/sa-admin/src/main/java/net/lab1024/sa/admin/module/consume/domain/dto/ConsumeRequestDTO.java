package net.lab1024.sa.admin.module.consume.domain.dto;

import lombok.Data;
import net.lab1024.sa.admin.module.consume.domain.entity.ProductEntity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 消费请求DTO
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Data
public class ConsumeRequestDTO {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 人员姓名
     */
    @NotBlank(message = "人员姓名不能为空")
    private String personName;

    /**
     * 消费金额
     */
    private BigDecimal amount;

    /**
     * 消费模式
     */
    private String consumeMode;

    /**
     * 支付方式
     */
    @NotBlank(message = "支付方式不能为空")
    private String payMethod;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 商品编码（商品扫码模式使用）
     */
    private String productCode;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 数量
     */
    @Positive(message = "数量必须大于0")
    private Integer quantity = 1;

    /**
     * 订餐项目列表（订餐模式使用）
     */
    private List<OrderItem> orderItems;

    /**
     * 计量值（计量模式使用）
     */
    private BigDecimal meteringValue;

    /**
     * 计量单位（计量模式使用）
     */
    private String meteringUnit;

    /**
     * 单价（计量模式使用）
     */
    private BigDecimal unitPrice;

    /**
     * 智能类型（智能模式使用）
     */
    private String smartType;

    /**
     * 建议金额（智能模式使用）
     */
    private BigDecimal suggestedAmount;

    /**
     * 选中的建议（智能模式使用）
     */
    private String selectedSuggestion;

    /**
     * 支付密码（智能模式自动支付使用）
     */
    private String paymentPassword;

    /**
     * 备注
     */
    private String remark;

    /**
     * 订餐项目内部类
     */
    @Data
    public static class OrderItem {
        /**
         * 商品ID
         */
        @NotNull(message = "商品ID不能为空")
        private Long productId;

        /**
         * 数量
         */
        @Positive(message = "数量必须大于0")
        private Integer quantity;

        /**
         * 备注
         */
        private String remark;
    }
}