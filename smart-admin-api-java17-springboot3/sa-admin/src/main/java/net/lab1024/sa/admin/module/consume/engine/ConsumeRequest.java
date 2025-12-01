package net.lab1024.sa.admin.module.consume.engine;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 消费请求类
 * 封装消费处理所需的全部信息
 *
 * @author SmartAdmin Team
 * @date 2025/11/16
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConsumeRequest {

    /**
     * 请求ID（唯一标识）
     */
    private String requestId;

    /**
     * 人员ID
     */
    private Long personId;

    /**
     * 人员姓名
     */
    private String personName;

    /**
     * 账户ID
     */
    private Long accountId;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备编号
     */
    private String deviceNo;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 消费模式
     */
    private String consumptionMode;

    /**
     * 消费金额（用户输入或系统计算）
     */
    private BigDecimal amount;

    /**
     * 消费数量（计量模式使用）
     */
    private BigDecimal quantity;

    /**
     * 消费单位（如：小时、次、份等）
     */
    private String unit;

    /**
     * 支付方式
     */
    private String payMethod;

    /**
     * 币种代码
     */
    private String currency;

    /**
     * 区域ID
     */
    private String regionId;

    /**
     * 区域名称
     */
    private String regionName;

    /**
     * 消费时间
     */
    private LocalDateTime consumeTime;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 第三方订单号
     */
    private String thirdPartyOrderNo;

    /**
     * 终端IP地址
     */
    private String clientIp;

    /**
     * 设备位置
     */
    private String location;

    /**
     * 操作员ID
     */
    private Long operatorId;

    /**
     * 操作员姓名
     */
    private String operatorName;

    /**
     * 模式配置参数
     */
    private Map<String, Object> modeConfig;

    /**
     * 设备配置参数
     */
    private Map<String, Object> deviceConfig;

    /**
     * 扩展数据
     */
    private Map<String, Object> extendData;

    /**
     * 消费商品信息（商品模式使用）
     */
    private String productInfo;

    /**
     * 消费类型细分
     */
    private String consumeType;

    /**
     * 是否离线模式
     */
    private Boolean offlineMode;

    /**
     * 请求来源（WEB/MOBILE/API/DEVICE等）
     */
    private String source;

    /**
     * 验证令牌
     */
    private String authToken;

    /**
     * 生物特征数据
     */
    private String biometricData;

    /**
     * 卡片信息
     */
    private String cardInfo;

    /**
     * 备注
     */
    private String remark;

    /**
     * 验证请求的基本信息
     *
     * @return 验证结果
     */
    public boolean isValid() {
        return personId != null && deviceId != null &&
               consumptionMode != null && !consumptionMode.trim().isEmpty();
    }

    /**
     * 检查是否包含必要的支付信息
     *
     * @return 是否包含支付信息
     */
    public boolean hasPaymentInfo() {
        return payMethod != null && !payMethod.trim().isEmpty();
    }

    /**
     * 获取客户端信息
     *
     * @return 客户端信息字符串
     */
    public String getClientInfo() {
        return String.format("IP:%s, Source:%s, Device:%s(%s)",
                           clientIp != null ? clientIp : "Unknown",
                           source != null ? source : "Unknown",
                           deviceName != null ? deviceName : "Unknown",
                           deviceNo != null ? deviceNo : "Unknown");
    }
}