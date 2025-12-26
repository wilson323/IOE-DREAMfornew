package net.lab1024.sa.consume.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.dao.ConsumeAnalysisDao;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 消费分析管理器
 * <p>
 * 负责消费数据分析的业务逻辑编排
 * 包括消费总览、趋势分析、分类统计、习惯分析、智能推荐等
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-24
 */
@Slf4j
@Component
public class ConsumeAnalysisManager {

    private final ConsumeAnalysisDao consumeAnalysisDao;

    /**
     * 构造函数注入依赖
     */
    public ConsumeAnalysisManager(ConsumeAnalysisDao consumeAnalysisDao) {
        this.consumeAnalysisDao = consumeAnalysisDao;
    }

    // ==================== 时间范围计算 ====================

    /**
     * 根据周期类型计算时间范围
     *
     * @param period 周期类型: week/month/quarter
     * @return [开始时间, 结束时间]
     */
    public LocalDateTime[] calculateTimeRange(String period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime;

        switch (period) {
            case "week":
                // 本周一
                startTime = now.minusDays(now.getDayOfWeek().getValue() - 1)
                        .with(LocalTime.MIN);
                break;
            case "month":
                // 本月1号
                startTime = now.withDayOfMonth(1).with(LocalTime.MIN);
                break;
            case "quarter":
                // 本季度第一天
                int currentMonth = now.getMonthValue();
                int quarterStartMonth = ((currentMonth - 1) / 3) * 3 + 1;
                startTime = now.withMonth(quarterStartMonth)
                        .withDayOfMonth(1)
                        .with(LocalTime.MIN);
                break;
            default:
                // 默认本周
                startTime = now.minusDays(now.getDayOfWeek().getValue() - 1)
                        .with(LocalTime.MIN);
        }

        return new LocalDateTime[]{startTime, now};
    }

    // ==================== 消费总览分析 ====================

    /**
     * 获取消费总览数据
     *
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return [总金额, 消费次数, 消费天数, 平均单笔]
     */
    public Object[] getConsumptionOverview(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("[消费分析] 查询消费总览: userId={}, startTime={}, endTime={}", userId, startTime, endTime);

        BigDecimal totalAmount = consumeAnalysisDao.selectTotalAmount(userId, startTime, endTime);
        Integer totalCount = consumeAnalysisDao.selectTotalCount(userId, startTime, endTime);
        Integer consumeDays = consumeAnalysisDao.selectConsumeDays(userId, startTime, endTime);
        BigDecimal averagePerOrder = consumeAnalysisDao.selectAveragePerOrder(userId, startTime, endTime);

        // 计算日均消费
        BigDecimal dailyAverage = BigDecimal.ZERO;
        if (consumeDays != null && consumeDays > 0) {
            dailyAverage = totalAmount.divide(BigDecimal.valueOf(consumeDays), 2, RoundingMode.HALF_UP);
        }

        return new Object[]{totalAmount, totalCount, consumeDays, averagePerOrder, dailyAverage};
    }

    // ==================== 趋势分析 ====================

    /**
     * 获取趋势数据
     *
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 趋势数据列表
     */
    public List<Map<String, Object>> getTrendData(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("[消费分析] 查询趋势数据: userId={}, startTime={}, endTime={}", userId, startTime, endTime);
        return consumeAnalysisDao.selectDailyTrend(userId, startTime, endTime);
    }

    // ==================== 分类统计 ====================

    /**
     * 获取分类统计数据
     *
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 分类统计列表
     */
    public List<Map<String, Object>> getCategoryStats(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("[消费分析] 查询分类统计: userId={}, startTime={}, endTime={}", userId, startTime, endTime);
        return consumeAnalysisDao.selectCategoryStats(userId, startTime, endTime);
    }

    /**
     * 计算分类占比
     *
     * @param categoryStats 分类统计列表
     * @param totalAmount   总金额
     * @return 分类占比列表（百分比）
     */
    public List<Integer> calculateCategoryPercents(List<Map<String, Object>> categoryStats, BigDecimal totalAmount) {
        List<Integer> percents = new ArrayList<>();

        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            // 总金额为0，所有分类占比为0
            for (int i = 0; i < categoryStats.size(); i++) {
                percents.add(0);
            }
            return percents;
        }

        for (Map<String, Object> stat : categoryStats) {
            BigDecimal amount = (BigDecimal) stat.get("amount");
            if (amount == null) {
                percents.add(0);
                continue;
            }

            // 计算占比：当前分类金额 / 总金额 * 100
            BigDecimal percent = amount.divide(totalAmount, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            percents.add(percent.intValue());
        }

        return percents;
    }

    // ==================== 消费习惯分析 ====================

    /**
     * 分析最常消费时段
     *
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 时段描述
     */
    public String analyzeMostFrequentTime(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("[消费分析] 分析最常消费时段: userId={}, startTime={}, endTime={}", userId, startTime, endTime);

        Map<String, Object> result = consumeAnalysisDao.selectMostFrequentTime(userId, startTime, endTime);
        if (result == null) {
            return "未知";
        }

        Integer hour = (Integer) result.get("hour");
        if (hour == null) {
            return "未知";
        }

        // 根据小时返回时段描述
        if (hour >= 6 && hour < 9) {
            return "早餐时段";
        } else if (hour >= 11 && hour < 13) {
            return "午餐时段";
        } else if (hour >= 17 && hour < 19) {
            return "晚餐时段";
        } else if (hour >= 22 || hour < 6) {
            return "夜宵时段";
        } else {
            return "其他时段";
        }
    }

    /**
     * 分析最喜欢的品类
     *
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 品类名称
     */
    public String analyzeFavoriteCategory(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("[消费分析] 分析最喜欢品类: userId={}, startTime={}, endTime={}", userId, startTime, endTime);

        Map<String, Object> result = consumeAnalysisDao.selectFavoriteCategory(userId, startTime, endTime);
        if (result == null) {
            return "未知";
        }

        String categoryName = (String) result.get("categoryName");
        return categoryName != null ? categoryName : "未知";
    }

    // ==================== 智能推荐生成 ====================

    /**
     * 生成智能推荐
     *
     * @param userId             用户ID
     * @param totalAmount        总金额
     * @param totalCount         总次数
     * @param averagePerOrder    平均单笔
     * @param favoriteCategory   最喜欢品类
     * @param mostFrequentTime   最常时段
     * @return 推荐列表
     */
    public List<Map<String, String>> generateRecommendations(
            Long userId,
            BigDecimal totalAmount,
            Integer totalCount,
            BigDecimal averagePerOrder,
            String favoriteCategory,
            String mostFrequentTime) {

        log.debug("[消费分析] 生成智能推荐: userId={}, totalAmount={}, totalCount={}, averagePerOrder={}",
                userId, totalAmount, totalCount, averagePerOrder);

        List<Map<String, String>> recommendations = new ArrayList<>();

        // 高消费用户 → 套餐推荐
        if (averagePerOrder != null && averagePerOrder.compareTo(BigDecimal.valueOf(50)) > 0) {
            Map<String, String> recommend = new java.util.HashMap<>();
            recommend.put("icon", "🍱️");
            recommend.put("title", "套餐优惠");
            recommend.put("description", "根据您的消费习惯，推荐购买套餐更实惠");
            recommend.put("action", "ordering");
            recommend.put("reason", "平均单笔消费超过50元");
            recommend.put("priority", "1");
            recommendations.add(recommend);
        }

        // 中餐用户 + 午餐时段 → 错峰优惠
        if ("中餐".equals(favoriteCategory) && "午餐时段".equals(mostFrequentTime)) {
            Map<String, String> recommend = new java.util.HashMap<>();
            recommend.put("icon", "⏰");
            recommend.put("title", "错峰优惠");
            recommend.put("description", "11:00前订餐享受9折优惠");
            recommend.put("action", "discount");
            recommend.put("reason", "您常在午餐时段消费中餐");
            recommend.put("priority", "2");
            recommendations.add(recommend);
        }

        // 高频用户 → VIP特权
        if (totalCount != null && totalCount > 20) {
            Map<String, String> recommend = new java.util.HashMap<>();
            recommend.put("icon", "🎁");
            recommend.put("title", "会员特权");
            recommend.put("description", "您已达到VIP等级，可享受专属优惠");
            recommend.put("action", "vip");
            recommend.put("reason", "本月消费次数超过20次");
            recommend.put("priority", "3");
            recommendations.add(recommend);
        }

        // 通用推荐 → 充值优惠
        Map<String, String> recommend = new java.util.HashMap<>();
        recommend.put("icon", "💳");
        recommend.put("title", "充值优惠");
        recommend.put("description", "当前充值满500送50，限时优惠");
        recommend.put("action", "recharge");
        recommend.put("reason", "通用推荐");
        recommend.put("priority", "4");
        recommendations.add(recommend);

        return recommendations;
    }

    // ==================== 辅助方法 ====================

    /**
     * 判断是否为高频用户
     *
     * @param totalCount 消费次数
     * @return true-高频用户, false-普通用户
     */
    public boolean isHighFrequencyUser(Integer totalCount) {
        return totalCount != null && totalCount > 20;
    }

    /**
     * 判断是否为高消费用户
     *
     * @param averagePerOrder 平均单笔消费
     * @return true-高消费用户, false-普通用户
     */
    public boolean isHighValueUser(BigDecimal averagePerOrder) {
        return averagePerOrder != null && averagePerOrder.compareTo(BigDecimal.valueOf(50)) > 0;
    }
}
