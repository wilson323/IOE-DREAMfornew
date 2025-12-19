package net.lab1024.sa.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 移动端消费统计VO
 * 移动端展示的消费统计信息
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "移动端消费统计")
public class MobileConsumeStatisticsVO {

    @Schema(description = "今日消费次数", example = "15")
    private Integer todayConsumeCount;

    @Schema(description = "今日消费金额", example = "320.50")
    private BigDecimal todayConsumeAmount;

    @Schema(description = "本周消费次数", example = "89")
    private Integer weekConsumeCount;

    @Schema(description = "本周消费金额", example = "1250.80")
    private BigDecimal weekConsumeAmount;

    @Schema(description = "本月消费次数", example = "312")
    private Integer monthConsumeCount;

    @Schema(description = "本月消费金额", example = "5630.20")
    private BigDecimal monthConsumeAmount;

    @Schema(description = "消费类型统计")
    private Object consumeTypeStats;

    @Schema(description = "消费趋势图表数据")
    private Object consumeTrendData;

    @Schema(description = "统计更新时间")
    private String statisticsTime;

    // ==================== 显式 Getter/Setter（避免 Lombok 在构建时未生效） ====================

    /**
     * 获取今日消费次数
     *
     * @return 今日消费次数
     */
    public Integer getTodayConsumeCount() {
        return this.todayConsumeCount;
    }

    /**
     * 设置今日消费次数
     *
     * @param todayConsumeCount 今日消费次数
     */
    public void setTodayConsumeCount(Integer todayConsumeCount) {
        this.todayConsumeCount = todayConsumeCount;
    }

    /**
     * 获取今日消费金额
     *
     * @return 今日消费金额
     */
    public BigDecimal getTodayConsumeAmount() {
        return this.todayConsumeAmount;
    }

    /**
     * 设置今日消费金额
     *
     * @param todayConsumeAmount 今日消费金额
     */
    public void setTodayConsumeAmount(BigDecimal todayConsumeAmount) {
        this.todayConsumeAmount = todayConsumeAmount;
    }

    /**
     * 获取本周消费次数
     *
     * @return 本周消费次数
     */
    public Integer getWeekConsumeCount() {
        return this.weekConsumeCount;
    }

    /**
     * 设置本周消费次数
     *
     * @param weekConsumeCount 本周消费次数
     */
    public void setWeekConsumeCount(Integer weekConsumeCount) {
        this.weekConsumeCount = weekConsumeCount;
    }

    /**
     * 获取本周消费金额
     *
     * @return 本周消费金额
     */
    public BigDecimal getWeekConsumeAmount() {
        return this.weekConsumeAmount;
    }

    /**
     * 设置本周消费金额
     *
     * @param weekConsumeAmount 本周消费金额
     */
    public void setWeekConsumeAmount(BigDecimal weekConsumeAmount) {
        this.weekConsumeAmount = weekConsumeAmount;
    }

    /**
     * 获取本月消费次数
     *
     * @return 本月消费次数
     */
    public Integer getMonthConsumeCount() {
        return this.monthConsumeCount;
    }

    /**
     * 设置本月消费次数
     *
     * @param monthConsumeCount 本月消费次数
     */
    public void setMonthConsumeCount(Integer monthConsumeCount) {
        this.monthConsumeCount = monthConsumeCount;
    }

    /**
     * 获取本月消费金额
     *
     * @return 本月消费金额
     */
    public BigDecimal getMonthConsumeAmount() {
        return this.monthConsumeAmount;
    }

    /**
     * 设置本月消费金额
     *
     * @param monthConsumeAmount 本月消费金额
     */
    public void setMonthConsumeAmount(BigDecimal monthConsumeAmount) {
        this.monthConsumeAmount = monthConsumeAmount;
    }
}


