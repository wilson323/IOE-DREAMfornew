package net.lab1024.sa.consume.domain.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 消费请求DTO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@Accessors(chain = true)
public class ConsumeRequestDTO {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 账户ID
     */
    @NotNull(message = "账户ID不能为空")
    private Long accountId;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 设备编码
     */
    private String deviceCode;

    /**
     * 消费金额（手动模式时使用）
     */
    private BigDecimal amount;

    /**
     * 消费模式：FIXED-定值，MANUAL-手动
     */
    private String consumeMode;

    /**
     * 订单ID（关联订单）
     */
    private String orderId;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 消费类型
     */
    private String consumeType;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 备注
     */
    private String remark;

    // ==================== 兼容方法和显式Getter/Setter ====================
    // 注意：Lombok @Data应该自动生成getter/setter，但为了确保编译通过，这里显式添加关键方法

    /**
     * 获取用户ID
     *
     * @return 用户ID
     */
    public Long getUserId() {
        return this.userId;
    }

    /**
     * 获取账户ID
     *
     * @return 账户ID
     */
    public Long getAccountId() {
        return this.accountId;
    }

    /**
     * 获取消费金额
     *
     * @return 消费金额
     */
    public BigDecimal getAmount() {
        return this.amount;
    }

    /**
     * 获取区域ID
     *
     * @return 区域ID
     */
    public Long getAreaId() {
        return this.areaId;
    }

    /**
     * 获取订单ID
     * <p>
     * Lombok @Data应该自动生成，但显式添加以确保编译通过
     * </p>
     *
     * @return 订单ID
     */
    public String getOrderId() {
        return this.orderId;
    }

    /**
     * 设置订单ID
     *
     * @param orderId 订单ID
     */
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /**
     * 获取设备ID
     * <p>
     * Lombok @Data应该自动生成，但显式添加以确保编译通过
     * </p>
     *
     * @return 设备ID
     */
    public Long getDeviceId() {
        return this.deviceId;
    }

    /**
     * 设置设备ID（Long类型）
     *
     * @param deviceId 设备ID
     */
    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * 设置设备ID（String兼容方法）
     * <p>
     * 根据chonggou.txt要求添加
     * 支持String类型的deviceId设置，自动转换为Long
     * </p>
     *
     * @param deviceIdStr 设备ID字符串
     */
    public void setDeviceId(String deviceIdStr) {
        if (deviceIdStr != null && !deviceIdStr.trim().isEmpty()) {
            try {
                this.deviceId = Long.parseLong(deviceIdStr);
            } catch (NumberFormatException e) {
                // 如果转换失败，设置为null
                this.deviceId = null;
            }
        } else {
            this.deviceId = null;
        }
    }

    /**
     * 获取消费类型
     * <p>
     * Lombok @Data应该自动生成，但显式添加以确保编译通过
     * </p>
     *
     * @return 消费类型
     */
    public String getConsumeType() {
        return this.consumeType;
    }

    /**
     * 设置消费类型
     *
     * @param consumeType 消费类型
     */
    public void setConsumeType(String consumeType) {
        this.consumeType = consumeType;
    }

    /**
     * 获取设备ID（String兼容方法）
     *
     * @return 设备ID字符串
     */
    public String getDeviceIdAsString() {
        return this.deviceId != null ? String.valueOf(this.deviceId) : null;
    }

    /**
     * 获取设备ID（Long类型）
     *
     * @return 设备ID
     */
    public Long getDeviceIdAsLong() {
        return this.deviceId;
    }
}
