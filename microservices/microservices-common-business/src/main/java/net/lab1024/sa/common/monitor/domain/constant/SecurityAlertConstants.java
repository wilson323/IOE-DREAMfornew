package net.lab1024.sa.common.monitor.domain.constant;

/**
 * 安防告警常量定义
 * <p>
 * 统一管理安防告警系统的常量定义，确保全局一致性
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-20
 */
public class SecurityAlertConstants {

    /**
     * 告警类型
     */
    public static class AlertType {
        /**
         * 设备告警（门禁、视频等设备故障或异常）
         */
        public static final int DEVICE = 1;

        /**
         * AI告警（人脸识别、行为分析等AI检测告警）
         */
        public static final int AI = 2;

        /**
         * 系统告警（系统监控、性能告警等）
         */
        public static final int SYSTEM = 3;

        /**
         * 获取告警类型名称
         *
         * @param type 告警类型
         * @return 类型名称
         */
        public static String getName(Integer type) {
            if (type == null) {
                return "未知";
            }
            switch (type) {
                case DEVICE:
                    return "设备告警";
                case AI:
                    return "AI告警";
                case SYSTEM:
                    return "系统告警";
                default:
                    return "未知";
            }
        }
    }

    /**
     * 告警级别（P1-P4）
     */
    public static class AlertLevel {
        /**
         * P1紧急（需要立即处理，全通道推送）
         */
        public static final int P1_URGENT = 1;

        /**
         * P2重要（需要及时处理，APP+Web推送）
         */
        public static final int P2_IMPORTANT = 2;

        /**
         * P3普通（常规处理，仅Web推送）
         */
        public static final int P3_NORMAL = 3;

        /**
         * P4提示（信息提示，不推送）
         */
        public static final int P4_INFO = 4;

        /**
         * 获取告警级别名称
         *
         * @param level 告警级别
         * @return 级别名称
         */
        public static String getName(Integer level) {
            if (level == null) {
                return "未知";
            }
            switch (level) {
                case P1_URGENT:
                    return "P1紧急";
                case P2_IMPORTANT:
                    return "P2重要";
                case P3_NORMAL:
                    return "P3普通";
                case P4_INFO:
                    return "P4提示";
                default:
                    return "未知";
            }
        }

        /**
         * 将字符串级别转换为Integer
         * 支持：P1/P1_URGENT/URGENT/CRITICAL -> 1
         * 支持：P2/P2_IMPORTANT/IMPORTANT/ERROR -> 2
         * 支持：P3/P3_NORMAL/NORMAL/WARNING -> 3
         * 支持：P4/P4_INFO/INFO -> 4
         *
         * @param levelStr 级别字符串
         * @return 级别Integer
         */
        public static Integer fromString(String levelStr) {
            if (levelStr == null || levelStr.trim().isEmpty()) {
                return P3_NORMAL; // 默认P3
            }
            String upper = levelStr.toUpperCase().trim();
            if (upper.startsWith("P1") || upper.equals("URGENT") || upper.equals("CRITICAL") || upper.equals("EMERGENCY")) {
                return P1_URGENT;
            } else if (upper.startsWith("P2") || upper.equals("IMPORTANT") || upper.equals("ERROR")) {
                return P2_IMPORTANT;
            } else if (upper.startsWith("P3") || upper.equals("NORMAL") || upper.equals("WARNING")) {
                return P3_NORMAL;
            } else if (upper.startsWith("P4") || upper.equals("INFO")) {
                return P4_INFO;
            }
            return P3_NORMAL; // 默认P3
        }

        /**
         * 将Integer级别转换为字符串
         *
         * @param level 级别Integer
         * @return 级别字符串（P1/P2/P3/P4）
         */
        public static String toString(Integer level) {
            if (level == null) {
                return "P3";
            }
            switch (level) {
                case P1_URGENT:
                    return "P1";
                case P2_IMPORTANT:
                    return "P2";
                case P3_NORMAL:
                    return "P3";
                case P4_INFO:
                    return "P4";
                default:
                    return "P3";
            }
        }
    }

    /**
     * 告警状态
     */
    public static class AlertStatus {
        /**
         * 待处理
         */
        public static final int PENDING = 1;

        /**
         * 处理中
         */
        public static final int IN_PROGRESS = 2;

        /**
         * 已处理
         */
        public static final int RESOLVED = 3;

        /**
         * 已忽略
         */
        public static final int IGNORED = 4;

        /**
         * 获取告警状态名称
         *
         * @param status 告警状态
         * @return 状态名称
         */
        public static String getName(Integer status) {
            if (status == null) {
                return "未知";
            }
            switch (status) {
                case PENDING:
                    return "待处理";
                case IN_PROGRESS:
                    return "处理中";
                case RESOLVED:
                    return "已处理";
                case IGNORED:
                    return "已忽略";
                default:
                    return "未知";
            }
        }

        /**
         * 将字符串状态转换为Integer
         * 支持：PENDING/待处理 -> 1
         * 支持：IN_PROGRESS/处理中 -> 2
         * 支持：RESOLVED/已处理 -> 3
         * 支持：IGNORED/已忽略 -> 4
         *
         * @param statusStr 状态字符串
         * @return 状态Integer
         */
        public static Integer fromString(String statusStr) {
            if (statusStr == null || statusStr.trim().isEmpty()) {
                return PENDING; // 默认待处理
            }
            String upper = statusStr.toUpperCase().trim();
            if (upper.equals("PENDING") || upper.equals("待处理")) {
                return PENDING;
            } else if (upper.equals("IN_PROGRESS") || upper.equals("处理中")) {
                return IN_PROGRESS;
            } else if (upper.equals("RESOLVED") || upper.equals("已处理")) {
                return RESOLVED;
            } else if (upper.equals("IGNORED") || upper.equals("已忽略")) {
                return IGNORED;
            }
            return PENDING; // 默认待处理
        }
    }

    /**
     * 处理结果
     */
    public static class HandleResult {
        /**
         * 确认（告警属实）
         */
        public static final int CONFIRMED = 1;

        /**
         * 误报（告警不属实）
         */
        public static final int FALSE_ALARM = 2;

        /**
         * 无法处理（需要其他部门处理）
         */
        public static final int CANNOT_HANDLE = 3;

        /**
         * 获取处理结果名称
         *
         * @param result 处理结果
         * @return 结果名称
         */
        public static String getName(Integer result) {
            if (result == null) {
                return "未知";
            }
            switch (result) {
                case CONFIRMED:
                    return "确认";
                case FALSE_ALARM:
                    return "误报";
                case CANNOT_HANDLE:
                    return "无法处理";
                default:
                    return "未知";
            }
        }
    }
}

