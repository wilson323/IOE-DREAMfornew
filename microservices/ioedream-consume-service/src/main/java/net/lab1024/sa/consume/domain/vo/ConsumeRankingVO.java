package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * 消费排名数据VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class ConsumeRankingVO {

    /**
     * 时间范围
     */
    private String timeRange;

    /**
     * 统计时间
     */
    private LocalDateTime statisticTime;

    /**
     * 排名类型
     * <p>
     * USER-用户排名
     * AREA-区域排名
     * DEVICE-设备排名
     * </p>
     */
    private String rankingType;

    /**
     * 排名列表
     */
    private List<RankingItem> rankingList;

    /**
     * 用户排名列表
     */
    private List<UserRankingItem> userRanking;

    /**
     * 区域排名列表
     */
    private List<RegionRankingItem> regionRanking;

    /**
     * 商品排名列表
     */
    private List<RankingItem> productRanking;

    /**
     * 排名项
     */
    @Data
    public static class RankingItem {
        /**
         * 排名
         */
        private Integer rank;

        /**
         * 名称（用户名称/区域名称/设备名称）
         */
        private String name;

        /**
         * 消费金额
         */
        private BigDecimal amount;

        /**
         * 消费笔数
         */
        private Integer count;
    }

    /**
     * 用户排名项
     */
    @Data
    public static class UserRankingItem {
        /**
         * 排名
         */
        private Integer rank;

        /**
         * 用户ID
         */
        private Long userId;

        /**
         * 用户名称
         */
        private String userName;

        /**
         * 用户编号
         */
        private String userNo;

        /**
         * 部门
         */
        private String department;

        /**
         * 消费金额
         */
        private BigDecimal amount;

        /**
         * 消费笔数
         */
        private Long count;

        /**
         * 平均消费金额
         */
        private BigDecimal averageAmount;
    }

    /**
     * 区域排名项
     */
    @Data
    public static class RegionRankingItem {
        /**
         * 排名
         */
        private Integer rank;

        /**
         * 区域ID
         */
        private Long regionId;

        /**
         * 区域名称
         */
        private String regionName;

        /**
         * 消费金额
         */
        private BigDecimal amount;

        /**
         * 消费笔数
         */
        private Long count;

        /**
         * 活跃用户数
         */
        private Integer activeUserCount;
    }
}
