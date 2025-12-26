package net.lab1024.sa.device.comm.protocol.zkteco;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 中控智慧消费协议V1.0消息实体
 * <p>
 * 严格按照中控智慧消费PUSH通讯协议V1.0规范实现
 * 支持消费记录上传、设备状态监控、账户管理等功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
public class ConsumeZktecoV10Message {

    // ==================== 协议头信息 ====================

    /**
     * 协议版本号
     * 固定值：V1.0
     */
    private String protocolVersion = "V1.0";

    /**
     * 设备唯一标识
     * 设备序列号或编号
     */
    private String deviceId;

    /**
     * 消息类型代码
     * 0x01 - 消费记录上传
     * 0x02 - 设备状态上报
     * 0x03 - 心跳包
     * 0x04 - 账户查询请求
     * 0x05 - 账户查询响应
     * 0x06 - 充值记录上传
     * 0x07 - 补贴记录上传
     * 0x08 - 错误报告
     * 0x09 - 设备配置请求
     * 0x0A - 设备配置响应
     */
    private Integer messageTypeCode;

    /**
     * 消息类型名称
     */
    private String messageTypeName;

    /**
     * 命令代码
     * 具体业务操作的命令代码
     */
    private Integer commandCode;

    /**
     * 消息序号
     * 用于消息去重和排序
     */
    private Long sequenceNumber;

    /**
     * 时间戳
     * 设备端时间戳（Unix时间戳）
     */
    private Long timestamp;

    /**
     * 会话ID
     * 用于关联请求和响应
     */
    private String sessionId;

    // ==================== 消费记录相关字段 ====================

    /**
     * 消费记录编号
     * 设备端生成的唯一记录编号
     */
    private String consumeRecordNumber;

    /**
     * 用户ID
     * 消费用户的ID
     */
    private Long userId;

    /**
     * 用户卡号
     * 刷卡消费的卡号
     */
    private String cardNumber;

    /**
     * 用户姓名
     */
    private String userName;

    /**
     * 消费方式
     * CARD-刷卡, FACE-人脸, FINGERPRINT-指纹, QR-二维码, NFC-NFC支付, OFFLINE-离线消费
     */
    private String consumeMethod;

    /**
     * 消费金额
     */
    private BigDecimal consumeAmount;

    /**
     * 消费类型
     * MEAL-餐费, SNACK-零食, DRINK-饮料, GROCERY-日用品, OTHER-其他
     */
    private String consumeType;

    /**
     * 消费类别
     * BREAKFAST-早餐, LUNCH-午餐, DINNER-晚餐, SUPPER-夜宵, SNACK-零食
     */
    private String consumeCategory;

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 商户名称
     */
    private String merchantName;

    /**
     * 终端号
     * 消费终端的编号
     */
    private String terminalNumber;

    /**
     * 操作员ID
     */
    private Long operatorId;

    /**
     * 操作员姓名
     */
    private String operatorName;

    /**
     * 消费时间
     */
    private LocalDateTime consumeTime;

    // ==================== 账户相关字段 ====================

    /**
     * 账户余额（消费前）
     */
    private BigDecimal balanceBefore;

    /**
     * 账户余额（消费后）
     */
    private BigDecimal balanceAfter;

    /**
     * 账户类型
     * PERSONAL-个人账户, SUBSIDY-补贴账户, COMPANY-公司账户, TEMP-临时账户
     */
    private String accountType;

    /**
     * 账户状态
     * ACTIVE-活跃, FROZEN-冻结, EXPIRED-过期, DISABLED-禁用
     */
    private String accountStatus;

    /**
     * 信用额度
     */
    private BigDecimal creditLimit;

    /**
     * 当前透支金额
     */
    private BigDecimal overdraftAmount;

    /**
     * 可用余额
     */
    private BigDecimal availableBalance;

    // ==================== 补贴相关字段 ====================

    /**
     * 补贴ID
     */
    private Long subsidyId;

    /**
     * 补贴类型
     * MEAL-餐补, TRANSPORT-交通补, HOUSING-住房补, WELFARE-福利补
     */
    private String subsidyType;

    /**
     * 补贴金额
     */
    private BigDecimal subsidyAmount;

    /**
     * 补贴余额
     */
    private BigDecimal subsidyBalance;

    /**
     * 补贴发放时间
     */
    private LocalDateTime subsidyGrantTime;

    /**
     * 补贴有效期
     */
    private LocalDateTime subsidyExpireTime;

    /**
     * 本次使用补贴金额
     */
    private BigDecimal usedSubsidyAmount;

    // ==================== 充值相关字段 ====================

    /**
     * 充值记录编号
     */
    private String rechargeRecordNumber;

    /**
     * 充值金额
     */
    private BigDecimal rechargeAmount;

    /**
     * 充值方式
     * CASH-现金, BANK_CARD-银行卡, MOBILE-手机支付, TRANSFER-转账, SUBSIDY-补贴发放
     */
    private String rechargeMethod;

    /**
     * 充值时间
     */
    private LocalDateTime rechargeTime;

    /**
     * 充值前余额
     */
    private BigDecimal balanceBeforeRecharge;

    /**
     * 充值后余额
     */
    private BigDecimal balanceAfterRecharge;

    /**
     * 充值操作员
     */
    private String rechargeOperator;

    // ==================== 设备相关字段 ====================

    /**
     * 设备类型
     * POS_MACHINE-POS机, SELF_SERVICE-自助消费机, CANTEEN-食堂终端, VENDING-自动售货机
     */
    private String deviceType;

    /**
     * 设备型号
     */
    private String deviceModel;

    /**
     * 设备固件版本
     */
    private String firmwareVersion;

    /**
     * 设备IP地址
     */
    private String deviceIpAddress;

    /**
     * 设备MAC地址
     */
    private String deviceMacAddress;

    /**
     * 设备位置
     */
    private String deviceLocation;

    /**
     * 设备状态
     * ONLINE-在线, OFFLINE-离线, BUSY-忙碌, ERROR-错误, MAINTENANCE-维护中
     */
    private String deviceStatus;

    // ==================== 商品相关字段 ====================

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品编码
     */
    private String productCode;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品类别
     */
    private String productCategory;

    /**
     * 商品单价
     */
    private BigDecimal unitPrice;

    /**
     * 商品数量
     */
    private BigDecimal quantity;

    /**
     * 商品总价
     */
    private BigDecimal totalPrice;

    /**
     * 折扣金额
     */
    private BigDecimal discountAmount;

    /**
     * 优惠信息
     */
    private String discountInfo;

    // ==================== 交易相关字段 ====================

    /**
     * 交易流水号
     */
    private String transactionNumber;

    /**
     * 交易类型
     * CONSUME-消费, RECHARGE-充值, REFUND-退款, CANCEL-撤销, ADJUST-调整
     */
    private String transactionType;

    /**
     * 交易状态
     * SUCCESS-成功, FAILED-失败, PENDING-待处理, CANCELLED-已取消
     */
    private String transactionStatus;

    /**
     * 支付方式
     * ACCOUNT-账户余额, SUBSIDY-补贴, CASH-现金, BANK_CARD-银行卡, MOBILE-移动支付
     */
    private String paymentMethod;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;

    // ==================== 离线消费相关字段 ====================

    /**
     * 是否离线消费
     */
    private Boolean isOfflineConsume;

    /**
     * 离线消费记录ID
     */
    private String offlineRecordId;

    /**
     * 离线消费时间
     */
    private LocalDateTime offlineConsumeTime;

    /**
     * 离线消费同步状态
     * PENDING-待同步, SYNCED-已同步, FAILED-同步失败, CONFLICT-数据冲突
     */
    private String offlineSyncStatus;

    /**
     * 离线消费同步时间
     */
    private LocalDateTime offlineSyncTime;

    /**
     * 离线消费校验码
     * 用于离线数据完整性校验
     */
    private String offlineChecksum;

    // ==================== 异常和报警相关字段 ====================

    /**
     * 异常代码
     * 账户异常、设备异常、网络异常等
     */
    private Integer exceptionCode;

    /**
     * 异常描述
     */
    private String exceptionDescription;

    /**
     * 报警级别
     * LOW-低, MEDIUM-中, HIGH-高, CRITICAL-紧急
     */
    private String alarmLevel;

    /**
     * 报警类型
     * INSUFFICIENT_BALANCE-余额不足, ACCOUNT_FROZEN-账户冻结,
     * DEVICE_OFFLINE-设备离线, COMMUNICATION_ERROR-通信错误
     */
    private String alarmType;

    // ==================== 扩展字段 ====================

    /**
     * 自定义数据字段（JSON格式）
     * 用于存储协议扩展和厂商自定义字段
     */
    private String customData;

    /**
     * 签名数据
     * 用于消息完整性验证
     */
    private String signature;

    /**
     * 校验码
     * 用于数据完整性校验
     */
    private String checksum;

    /**
     * 协议扩展标志
     * 标识是否包含扩展字段
     */
    private Boolean hasExtendedFields;

    /**
     * 扩展字段数量
     */
    private Integer extendedFieldCount;

    /**
     * 扩展字段数据（Map格式）
     */
    private Map<String, Object> extendedFields;

    // ==================== 处理相关字段 ====================

    /**
     * 消息接收时间
     */
    private LocalDateTime receiveTime;

    /**
     * 消息处理时间
     */
    private LocalDateTime processTime;

    /**
     * 处理状态
     * PENDING-待处理, PROCESSING-处理中, SUCCESS-成功, FAILED-失败
     */
    private String processStatus;

    /**
     * 处理结果描述
     */
    private String processResult;

    /**
     * 处理耗时（毫秒）
     */
    private Long processDuration;

    /**
     * 响应消息ID
     * 用于关联请求和响应
     */
    private String responseMessageId;

    /**
     * 是否需要重试
     */
    private Boolean needRetry;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount;

    // ==================== 统计相关字段 ====================

    /**
     * 日消费笔数
     */
    private Integer dailyConsumeCount;

    /**
     * 日消费总额
     */
    private BigDecimal dailyConsumeAmount;

    /**
     * 月消费笔数
     */
    private Integer monthlyConsumeCount;

    /**
     * 月消费总额
     */
    private BigDecimal monthlyConsumeAmount;

    /**
     * 当日最高消费金额
     */
    private BigDecimal dailyMaxConsumeAmount;

    /**
     * 消费限额（单笔）
     */
    private BigDecimal singleConsumeLimit;

    /**
     * 消费限额（每日）
     */
    private BigDecimal dailyConsumeLimit;

    /**
     * 消费限额（每月）
     */
    private BigDecimal monthlyConsumeLimit;
}
