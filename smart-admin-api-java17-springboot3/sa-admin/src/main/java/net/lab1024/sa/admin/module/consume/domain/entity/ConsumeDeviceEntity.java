package net.lab1024.sa.admin.module.consume.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.module.device.entity.SmartDeviceEntity;

import java.math.BigDecimal;

/**
 * 消费设备实体类
 *
 * 继承SmartDeviceEntity，包含消费特有字段
 *
 * @author SmartAdmin Team
 * @date 2025-11-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_consume_device")
public class ConsumeDeviceEntity extends SmartDeviceEntity {

    /**
     * 消费机类型 (POS-POS机, VENDING-售货机, CAFETERIA-食堂消费机, WATER-饮水机)
     */
    private String consumeDeviceType;

    /**
     * 支付方式 (CARD-刷卡, QR-二维码, FACE-人脸支付, FINGERPRINT-指纹支付, NFC-NFC支付)
     */
    private String paymentMethod;

    /**
     * 是否支持扫码支付 (0-不支持, 1-支持)
     */
    private Integer qrPaymentEnabled;

    /**
     * 是否支持人脸支付 (0-不支持, 1-支持)
     */
    private Integer facePaymentEnabled;

    /**
     * 是否支持指纹支付 (0-不支持, 1-支持)
     */
    private Integer fingerprintPaymentEnabled;

    /**
     * 是否支持NFC支付 (0-不支持, 1-支持)
     */
    private Integer nfcPaymentEnabled;

    /**
     * 单笔消费限额(元)
     */
    private BigDecimal singlePaymentLimit;

    /**
     * 日消费限额(元)
     */
    private BigDecimal dailyPaymentLimit;

    /**
     * 是否支持密码支付 (0-不支持, 1-支持)
     */
    private Integer passwordPaymentEnabled;

    /**
     * 最小密码长度
     */
    private Integer minPasswordLength;

    /**
     * 支付超时时间(秒)
     */
    private Integer paymentTimeout;

    /**
     * 是否支持小票打印 (0-不支持, 1-支持)
     */
    private Integer receiptPrintEnabled;

    /**
     * 打印机类型 (THERMAL-热敏, INKJET-喷墨)
     */
    private String printerType;

    /**
     * 是否支持语音播报 (0-不支持, 1-支持)
     */
    private Integer voiceBroadcastEnabled;

    /**
     * 播报音量(1-10)
     */
    private Integer voiceVolume;

    /**
     * 是否支持消费退款 (0-不支持, 1-支持)
     */
    private Integer refundEnabled;

    /**
     * 退款时限(分钟)
     */
    private Integer refundTimeLimit;

    /**
     * 是否支持离线消费 (0-不支持, 1-支持)
     */
    private Integer offlinePaymentEnabled;

    /**
     * 离线存储记录数
     */
    private Integer offlineRecordCount;

    /**
     * 数据同步方式 (REALTIME-实时同步, BATCH-批量同步, MANUAL-手动同步)
     */
    private String syncMode;

    /**
     * 自动同步间隔(分钟)
     */
    private Integer autoSyncInterval;

    /**
     * 是否支持网络支付 (0-不支持, 1-支持)
     */
    private Integer networkPaymentEnabled;

    /**
     * 网络支付方式 (ALIPAY-支付宝, WECHAT-微信, UNIONPAY-银联)
     */
    private String networkPaymentTypes;

    /**
     * 是否支持会员折扣 (0-不支持, 1-支持)
     */
    private Integer memberDiscountEnabled;

    /**
     * 默认折扣率 (%)
     */
    private BigDecimal defaultDiscountRate;

    /**
     * 是否支持套餐消费 (0-不支持, 1-支持)
     */
    private Integer packagePaymentEnabled;

    /**
     * 套餐配置(JSON格式)
     */
    private String packageConfig;

    /**
     * 是否支持积分累计 (0-不支持, 1-支持)
     */
    private Integer pointsAccumulationEnabled;

    /**
     * 积分比例 (消费金额:积分)
     */
    private BigDecimal pointsRatio;

    /**
     * 是否支持余额查询 (0-不支持, 1-支持)
     */
    private Integer balanceQueryEnabled;

    /**
     * 是否支持消费明细查询 (0-不支持, 1-支持)
     */
    private Integer detailQueryEnabled;

    /**
     * 消费区域ID
     */
    private Long consumeAreaId;

    /**
     * 设备管理员ID
     */
    private Long adminUserId;

    /**
     * 结算方式 (REALTIME-实时结算, DAILY-日结, WEEKLY-周结, MONTHLY-月结)
     */
    private String settlementMode;

    /**
     * 自动结算时间
     */
    private String settlementTime;
}