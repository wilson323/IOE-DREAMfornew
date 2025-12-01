package net.lab1024.sa.admin.module.access.domain.dto;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 时间段配置DTO
 * <p>
 * 用于解析权限的时间段配置JSON
 * 支持工作日/周末、节假日、特殊日期等复杂时间段规则
 *
 * @author IOE-DREAM Team
 * @since 2025-11-19
 */
public class TimeSlotConfig {

    /**
     * 时间段列表
     */
    @JsonProperty("timeSlots")
    private List<TimeSlot> timeSlots;

    public TimeSlotConfig() {
        this.timeSlots = new ArrayList<>();
    }

    public TimeSlotConfig(List<TimeSlot> timeSlots) {
        this.timeSlots = timeSlots != null ? timeSlots : new ArrayList<>();
    }

    public List<TimeSlot> getTimeSlots() {
        return timeSlots;
    }

    public void setTimeSlots(List<TimeSlot> timeSlots) {
        this.timeSlots = timeSlots != null ? timeSlots : new ArrayList<>();
    }

    /**
     * 单个时间段配置
     */
    public static class TimeSlot {
        /**
         * 时间段类型
         * weekday: 工作日
         * weekend: 周末
         * holiday: 节假日
         * specific: 特定日期
         */
        @JsonProperty("type")
        private String type;

        /**
         * 星期几 (1-7对应周一到周日)
         * 仅当type为weekday或weekend时使用
         */
        @JsonProperty("days")
        private List<Integer> days;

        /**
         * 特定日期列表 (格式: yyyy-MM-dd)
         * 仅当type为holiday或specific时使用
         */
        @JsonProperty("dates")
        private List<String> dates;

        /**
         * 时间范围列表
         */
        @JsonProperty("timeRanges")
        private List<TimeRange> timeRanges;

        public TimeSlot() {
            this.timeRanges = new ArrayList<>();
        }

        public TimeSlot(String type, List<Integer> days, List<String> dates, List<TimeRange> timeRanges) {
            this.type = type;
            this.days = days != null ? days : new ArrayList<>();
            this.dates = dates != null ? dates : new ArrayList<>();
            this.timeRanges = timeRanges != null ? timeRanges : new ArrayList<>();
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<Integer> getDays() {
            return days;
        }

        public void setDays(List<Integer> days) {
            this.days = days != null ? days : new ArrayList<>();
        }

        public List<String> getDates() {
            return dates;
        }

        public void setDates(List<String> dates) {
            this.dates = dates != null ? dates : new ArrayList<>();
        }

        public List<TimeRange> getTimeRanges() {
            return timeRanges;
        }

        public void setTimeRanges(List<TimeRange> timeRanges) {
            this.timeRanges = timeRanges != null ? timeRanges : new ArrayList<>();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TimeSlot timeSlot = (TimeSlot) o;
            return Objects.equals(type, timeSlot.type) &&
                   Objects.equals(days, timeSlot.days) &&
                   Objects.equals(dates, timeSlot.dates) &&
                   Objects.equals(timeRanges, timeSlot.timeRanges);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, days, dates, timeRanges);
        }

        @Override
        public String toString() {
            return "TimeSlot{" +
                    "type='" + type + '\'' +
                    ", days=" + days +
                    ", dates=" + dates +
                    ", timeRanges=" + timeRanges +
                    '}';
        }
    }

    /**
     * 时间范围配置
     */
    public static class TimeRange {
        /**
         * 开始时间 (格式: HH:mm)
         */
        @JsonProperty("start")
        private String start;

        /**
         * 结束时间 (格式: HH:mm)
         */
        @JsonProperty("end")
        private String end;

        public TimeRange() {}

        public TimeRange(String start, String end) {
            this.start = start;
            this.end = end;
        }

        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }

        /**
         * 验证时间范围格式是否正确
         *
         * @return 是否格式正确
         */
        public boolean isValid() {
            return start != null && end != null &&
                   isValidTimeFormat(start) && isValidTimeFormat(end);
        }

        /**
         * 验证时间格式 (HH:mm)
         *
         * @param timeString 时间字符串
         * @return 是否格式正确
         */
        private boolean isValidTimeFormat(String timeString) {
            try {
                LocalTime.parse(timeString);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        /**
         * 检查时间范围是否跨天
         *
         * @return 是否跨天
         */
        public boolean isCrossDay() {
            if (!isValid()) {
                return false;
            }
            try {
                LocalTime startTime = LocalTime.parse(start);
                LocalTime endTime = LocalTime.parse(end);
                return endTime.isBefore(startTime) || endTime.equals(startTime);
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TimeRange timeRange = (TimeRange) o;
            return Objects.equals(start, timeRange.start) &&
                   Objects.equals(end, timeRange.end);
        }

        @Override
        public int hashCode() {
            return Objects.hash(start, end);
        }

        @Override
        public String toString() {
            return "TimeRange{" +
                    "start='" + start + '\'' +
                    ", end='" + end + '\'' +
                    '}';
        }
    }

    /**
     * 检查配置是否有效
     *
     * @return 是否有效
     */
    public boolean isValid() {
        if (timeSlots == null || timeSlots.isEmpty()) {
            return true; // 空配置是有效的
        }

        for (TimeSlot timeSlot : timeSlots) {
            if (!isTimeSlotValid(timeSlot)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查单个时间段是否有效
     *
     * @param timeSlot 时间段
     * @return 是否有效
     */
    private boolean isTimeSlotValid(TimeSlot timeSlot) {
        if (timeSlot == null || timeSlot.getType() == null) {
            return false;
        }

        String type = timeSlot.getType().toLowerCase();
        switch (type) {
            case "weekday":
            case "weekend":
                return timeSlot.getTimeRanges() != null &&
                       timeSlot.getTimeRanges().stream().allMatch(TimeRange::isValid);
            case "holiday":
            case "specific":
                return (timeSlot.getDates() != null && !timeSlot.getDates().isEmpty()) &&
                       timeSlot.getTimeRanges() != null &&
                       timeSlot.getTimeRanges().stream().allMatch(TimeRange::isValid);
            default:
                return false;
        }
    }

    /**
     * 获取配置摘要信息
     *
     * @return 摘要信息
     */
    public String getSummary() {
        if (timeSlots == null || timeSlots.isEmpty()) {
            return "无时间段配置";
        }

        StringBuilder summary = new StringBuilder();
        summary.append("时间段配置: ").append(timeSlots.size()).append("个时间段\n");

        for (int i = 0; i < timeSlots.size(); i++) {
            TimeSlot timeSlot = timeSlots.get(i);
            summary.append(i + 1).append(". ").append(getTimeSlotDescription(timeSlot)).append("\n");
        }

        return summary.toString();
    }

    /**
     * 获取时间段描述信息
     *
     * @param timeSlot 时间段
     * @return 描述信息
     */
    private String getTimeSlotDescription(TimeSlot timeSlot) {
        StringBuilder desc = new StringBuilder();
        desc.append("类型: ").append(timeSlot.getType());

        if ("weekday".equals(timeSlot.getType()) || "weekend".equals(timeSlot.getType())) {
            if (timeSlot.getDays() != null && !timeSlot.getDays().isEmpty()) {
                desc.append(", 星期: ").append(timeSlot.getDays());
            }
        } else if ("holiday".equals(timeSlot.getType()) || "specific".equals(timeSlot.getType())) {
            if (timeSlot.getDates() != null && !timeSlot.getDates().isEmpty()) {
                desc.append(", 日期: ").append(timeSlot.getDates());
            }
        }

        if (timeSlot.getTimeRanges() != null && !timeSlot.getTimeRanges().isEmpty()) {
            desc.append(", 时间段: ");
            for (int i = 0; i < timeSlot.getTimeRanges().size(); i++) {
                TimeRange timeRange = timeSlot.getTimeRanges().get(i);
                if (i > 0) {
                    desc.append(", ");
                }
                desc.append(timeRange.getStart()).append("-").append(timeRange.getEnd());
            }
        }

        return desc.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeSlotConfig that = (TimeSlotConfig) o;
        return Objects.equals(timeSlots, that.timeSlots);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeSlots);
    }

    @Override
    public String toString() {
        return "TimeSlotConfig{" +
                "timeSlots=" + timeSlots +
                '}';
    }
}