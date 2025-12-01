package net.lab1024.sa.admin.module.smart.access.domain.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 时间段配置DTO
 * <p>
 * 用于解析权限的时间段配置JSON
 * 支持工作日/周末、节假日、特殊日期等复杂时间段规则
 *
 * @author IOE-DREAM Team
 * @since 2025-11-19
 */
@Data
public class TimeSlotConfig {

    /**
     * 时间段列表
     */
    @JsonProperty("timeSlots")
    private List<TimeSlot> timeSlots;

    /**
     * 单个时间段配置
     */
    @Data
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
    }

    /**
     * 时间范围配置
     */
    @Data
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
    }
}

