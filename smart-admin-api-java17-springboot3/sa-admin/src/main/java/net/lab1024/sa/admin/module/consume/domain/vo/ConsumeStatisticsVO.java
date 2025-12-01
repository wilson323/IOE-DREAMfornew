package net.lab1024.sa.admin.module.consume.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 消费统计VO
 * <p>
 * 严格遵循repowiki规范：
 * - 使用@Data注解自动生成getter/setter
 * - 使用@Accessors启用链式调用
 * - 提供完整的统计数据展示
 * - 包含趋势分析数据
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-27
 */
@Data
@Accessors(chain = true)
public class ConsumeStatisticsVO {

    /**
     * 统计ID
     */
    private Long statisticsId;

    /**
     * 统计日期
     */
    private LocalDateTime statisticsDate;

    /**
     * 统计周期代码
     */
    private String periodCode;

    /**
     * 统计周期名称
     */
    private String periodName;

    /**
     * 统计开始时间
     */
    private LocalDateTime startTime;

    /**
     * 统计结束时间
     */
    private LocalDateTime endTime;

    /**
     * 总消费金额
     */
    private BigDecimal totalAmount;

    /**
     * 总消费笔数
     */
    private Integer totalCount;

    /**
     * 平均每笔金额
     */
    private BigDecimal averageAmount;

    /**
     * 最大单笔金额
     */
    private BigDecimal maxAmount;

    /**
     * 最小单笔金额
     */
    private BigDecimal minAmount;

    /**
     * 消费人数
     */
    private Integer customerCount;

    /**
     * 人均消费金额
     */
    private BigDecimal perCapitaAmount;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款笔数
     */
    private Integer refundCount;

    /**
     * 净消费金额（扣除退款）
     */
    private BigDecimal netAmount;

    /**
     * 折扣金额
     */
    private BigDecimal discountAmount;

    /**
     * 优惠使用率
     */
    private BigDecimal discountRate;

    /**
     * 会员消费金额
     */
    private BigDecimal memberAmount;

    /**
     * 会员消费笔数
     */
    private Integer memberCount;

    /**
     * 非会员消费金额
     */
    private BigDecimal nonMemberAmount;

    /**
     * 非会员消费笔数
     */
    private Integer nonMemberCount;

    /**
     * 现金支付金额
     */
    private BigDecimal cashAmount;

    /**
     * 现金支付笔数
     */
    private Integer cashCount;

    /**
     * 移动支付金额
     */
    private BigDecimal mobileAmount;

    /**
     * 移动支付笔数
     */
    private Integer mobileCount;

    /**
     * 刷卡支付金额
     */
    private BigDecimal cardAmount;

    /**
     * 刷卡支付笔数
     */
    private Integer cardCount;

    /**
     * 优惠券使用金额
     */
    private BigDecimal couponAmount;

    /**
     * 优惠券使用次数
     */
    private Integer couponCount;

    /**
     * 积分抵扣金额
     */
    private BigDecimal pointsAmount;

    /**
     * 积分使用数量
     */
    private Integer pointsUsed;

    /**
     * 高峰时段消费金额
     */
    private BigDecimal peakAmount;

    /**
     * 高峰时段消费笔数
     */
    private Integer peakCount;

    /**
     * 低峰时段消费金额
     */
    private BigDecimal offPeakAmount;

    /**
     * 低峰时段消费笔数
     */
    private Integer offPeakCount;

    /**
     * 同比增长率
     */
    private BigDecimal yearOnYearGrowth;

    /**
     * 环比增长率
     */
    private BigDecimal monthOnMonthGrowth;

    /**
     * 日均消费金额
     */
    private BigDecimal dailyAverageAmount;

    /**
     * 日均消费笔数
     */
    private BigDecimal dailyAverageCount;

    /**
     * 设备数量
     */
    private Integer deviceCount;

    /**
     * 活跃设备数量
     */
    private Integer activeDeviceCount;

    /**
     * 设备利用率
     */
    private BigDecimal deviceUtilization;

    /**
     * 平均每设备消费金额
     */
    private BigDecimal perDeviceAmount;

    /**
     * 平均每设备消费笔数
     */
    private BigDecimal perDeviceCount;

    /**
     * 数据来源
     */
    private String dataSource;

    /**
     * 统计状态
     */
    private String statisticsStatus;

    /**
     * 统计说明
     */
    private String remarks;

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