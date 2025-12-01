package net.lab1024.sa.admin.module.consume.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 消费设备日报VO
 * <p>
 * 严格遵循repowiki规范：
 * - 使用@Data注解自动生成getter/setter
 * - 使用@Accessors启用链式调用
 * - 提供完整的设备日报数据
 * - 包含设备性能和异常统计
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-27
 */
@Data
@Accessors(chain = true)
public class ConsumeDeviceDailyReportVO {

    /**
     * 日报ID
     */
    private Long reportId;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备编号
     */
    private String deviceCode;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备类型代码
     */
    private String deviceTypeCode;

    /**
     * 设备类型名称
     */
    private String deviceTypeName;

    /**
     * 设备位置
     */
    private String location;

    /**
     * 日报日期
     */
    private LocalDate reportDate;

    /**
     * 当日总消费笔数
     */
    private Integer totalTransactions;

    /**
     * 当日总消费金额
     */
    private BigDecimal totalAmount;

    /**
     * 当日平均单笔金额
     */
    private BigDecimal averageAmount;

    /**
     * 当日最大单笔金额
     */
    private BigDecimal maxAmount;

    /**
     * 当日最小单笔金额
     */
    private BigDecimal minAmount;

    /**
     * 当日成功交易笔数
     */
    private Integer successTransactions;

    /**
     * 当日成功交易金额
     */
    private BigDecimal successAmount;

    /**
     * 当日失败交易笔数
     */
    private Integer failedTransactions;

    /**
     * 当日失败交易金额
     */
    private BigDecimal failedAmount;

    /**
     * 交易成功率
     */
    private BigDecimal successRate;

    /**
     * 当日退款笔数
     */
    private Integer refundTransactions;

    /**
     * 当日退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 当日现金支付笔数
     */
    private Integer cashTransactions;

    /**
     * 当日现金支付金额
     */
    private BigDecimal cashAmount;

    /**
     * 当日移动支付笔数
     */
    private Integer mobileTransactions;

    /**
     * 当日移动支付金额
     */
    private BigDecimal mobileAmount;

    /**
     * 当日刷卡支付笔数
     */
    private Integer cardTransactions;

    /**
     * 当日刷卡支付金额
     */
    private BigDecimal cardAmount;

    /**
     * 当日优惠券使用次数
     */
    private Integer couponUsage;

    /**
     * 当日优惠券优惠金额
     */
    private BigDecimal couponAmount;

    /**
     * 当日积分抵扣数量
     */
    private Integer pointsDeduction;

    /**
     * 当日积分抵扣金额
     */
    private BigDecimal pointsAmount;

    /**
     * 当日服务费总额
     */
    private BigDecimal serviceFeeAmount;

    /**
     * 当日第一笔交易时间
     */
    private LocalDateTime firstTransactionTime;

    /**
     * 当日最后一笔交易时间
     */
    private LocalDateTime lastTransactionTime;

    /**
     * 当日设备运行时长（分钟）
     */
    private Integer runningMinutes;

    /**
     * 当日设备在线时长（分钟）
     */
    private Integer onlineMinutes;

    /**
     * 设备在线率
     */
    private BigDecimal onlineRate;

    /**
     * 设备平均响应时间（毫秒）
     */
    private Long averageResponseTime;

    /**
     * 设备最大响应时间（毫秒）
     */
    private Long maxResponseTime;

    /**
     * 设备最小响应时间（毫秒）
     */
    private Long minResponseTime;

    /**
     * 当日异常次数
     */
    private Integer exceptionCount;

    /**
     * 当日故障次数
     */
    private Integer failureCount;

    /**
     * 当日维护次数
     */
    private Integer maintenanceCount;

    /**
     * 当日重启次数
     */
    private Integer restartCount;

    /**
     * 设备状态代码
     */
    private String statusCode;

    /**
     * 设备状态名称
     */
    private String statusName;

    /**
     * 设备健康评分（0-100）
     */
    private Integer healthScore;

    /**
     * 设备性能评分（0-100）
     */
    private Integer performanceScore;

    /**
     * 设备稳定性评分（0-100）
     */
    private Integer stabilityScore;

    /**
     * 当日用户数量
     */
    private Integer userCount;

    /**
     * 当日活跃用户数量
     */
    private Integer activeUserCount;

    /**
     * 当日新用户数量
     */
    private Integer newUserCount;

    /**
     * 当日老用户数量
     */
    private Integer oldUserCount;

    /**
     * 当日高峰时段交易笔数
     */
    private Integer peakHourTransactions;

    /**
     * 当日高峰时段交易金额
     */
    private BigDecimal peakHourAmount;

    /**
     * 当日低峰时段交易笔数
     */
    private Integer offPeakTransactions;

    /**
     * 当日低峰时段交易金额
     */
    private BigDecimal offPeakAmount;

    /**
     * 当日星期几
     */
    private Integer dayOfWeek;

    /**
     * 当日是否工作日
     */
    private Boolean isWorkday;

    /**
     * 当日是否节假日
     */
    private Boolean isHoliday;

    /**
     * 与昨日对比增长率
     */
    private BigDecimal dayOnDayGrowth;

    /**
     * 与上周同日对比增长率
     */
    private BigDecimal weekOnWeekGrowth;

    /**
     * 与上月同日对比增长率
     */
    private BigDecimal monthOnMonthGrowth;

    /**
     * 设备编号序列号
     */
    private String serialNumber;

    /**
     * 设备版本号
     */
    private String versionNumber;

    /**
     * 设备IP地址
     */
    private String ipAddress;

    /**
     * 设备MAC地址
     */
    private String macAddress;

    /**
     * 数据来源
     */
    private String dataSource;

    /**
     * 报表备注
     */
    private String remarks;

    /**
     * 报表生成时间
     */
    private LocalDateTime reportTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    private Long createUserId;

    /**
     * 创建人姓名
     */
    private String createUserName;

    /**
     * 更新人ID
     */
    private Long updateUserId;

    /**
     * 更新人姓名
     */
    private String updateUserName;

    /**
     * 删除标识（0-未删除，1-已删除）
     */
    private Integer deletedFlag;
}