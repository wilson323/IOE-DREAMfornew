package net.lab1024.sa.consume.domain.dto;

import java.math.BigDecimal;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 消费请求DTO
 * <p>
 * 用于消费请求数据传输
 * 严格遵循CLAUDE.md规范：
 * - 使用DTO后缀命名
 * - 包含完整的业务字段
 * - 符合企业级DTO设计
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Slf4j
public class ConsumeRequestDTO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 账户ID
     */
    private Long accountId;

    /**
     * 设备ID（支持设备编码和数字ID）
     */
    private String deviceId;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 消费金额（金额模式时使用）
     */
    private BigDecimal amount;

    /**
     * 消费模式
     */
    private String consumeMode;

    /**
     * 商品ID（商品模式时使用）
     */
    private String productId;

    /**
     * 商品数量（商品模式时使用）
     */
    private Integer quantity;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 消费类型
     */
    private String consumeType;

    // ========== 便捷方法：支持不同类型的数据转换 ==========

    /**
     * 获取设备ID（数字格式）
     * 如果deviceId是数字字符串，返回对应的Long值
     */
    public Long getDeviceIdAsLong() {
        if (this.deviceId == null) {
            return null;
        }
        try {
            return Long.parseLong(this.deviceId);
        } catch (NumberFormatException e) {
            log.debug("[消费请求DTO] deviceId不是数字格式，返回null: deviceId={}, error={}", this.deviceId, e.getMessage());
            return null;
        }
    }

    /**
     * 设置设备ID（支持数字和字符串）
     */
    public void setDeviceId(Object deviceId) {
        if (deviceId == null) {
            this.deviceId = null;
        } else if (deviceId instanceof String) {
            this.deviceId = (String) deviceId;
        } else if (deviceId instanceof Long) {
            this.deviceId = String.valueOf(deviceId);
        } else if (deviceId instanceof Integer) {
            this.deviceId = String.valueOf(deviceId);
        } else {
            this.deviceId = deviceId.toString();
        }
    }

    /**
     * 检查设备ID是否为数字格式
     */
    public boolean isDeviceIdNumeric() {
        return getDeviceIdAsLong() != null;
    }
}



