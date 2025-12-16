package net.lab1024.sa.video.sdk;

import lombok.Data;

import java.util.List;

/**
 * 行为检测结果
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Data
public class BehaviorDetectionResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 检测到的行为列表
     */
    private List<BehaviorInfo> behaviors;

    /**
     * 处理耗时（毫秒）
     */
    private long costTime;

    /**
     * 行为信息
     */
    @Data
    public static class BehaviorInfo {
        /**
         * 行为类型：LOITERING-徘徊, GATHERING-聚集, FALL-跌倒, RUNNING-奔跑, FIGHTING-打架
         */
        private String type;

        /**
         * 置信度
         */
        private double confidence;

        /**
         * 位置X
         */
        private int x;

        /**
         * 位置Y
         */
        private int y;

        /**
         * 区域宽度
         */
        private int width;

        /**
         * 区域高度
         */
        private int height;

        /**
         * 涉及人数
         */
        private int personCount;

        /**
         * 描述
         */
        private String description;
    }
}
