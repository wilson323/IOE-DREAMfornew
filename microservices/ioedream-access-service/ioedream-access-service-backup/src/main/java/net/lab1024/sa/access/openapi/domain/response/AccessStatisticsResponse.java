package net.lab1024.sa.access.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 闂ㄧ缁熻淇℃伅鍝嶅簲
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "闂ㄧ缁熻淇℃伅鍝嶅簲")
public class AccessStatisticsResponse {

    @Schema(description = "缁熻绫诲瀷", example = "daily", allowableValues = {"daily", "weekly", "monthly", "yearly", "custom"})
    private String statisticsType;

    @Schema(description = "缁熻绫诲瀷鍚嶇О", example = "鏃ョ粺璁?)
    private String statisticsTypeName;

    @Schema(description = "寮€濮嬫棩鏈?, example = "2025-12-16")
    private String startDate;

    @Schema(description = "缁撴潫鏃ユ湡", example = "2025-12-16")
    private String endDate;

    @Schema(description = "缁熻鏃堕棿", example = "2025-12-16T15:30:00")
    private LocalDateTime statisticsTime;

    @Schema(description = "鎬婚€氳娆℃暟", example = "1523")
    private Long totalAccessCount;

    @Schema(description = "鎴愬姛閫氳娆℃暟", example = "1498")
    private Long successAccessCount;

    @Schema(description = "澶辫触閫氳娆℃暟", example = "25")
    private Long failAccessCount;

    @Schema(description = "閫氳鎴愬姛鐜?, example = "98.36")
    private BigDecimal successRate;

    @Schema(description = "杩涘叆娆℃暟", example = "789")
    private Long enterCount;

    @Schema(description = "绂诲紑娆℃暟", example = "734")
    private Long exitCount;

    @Schema(description = "骞冲潎姣忔閫氳鑰楁椂锛堟绉掞級", example = "450")
    private Long averageProcessTime;

    @Schema(description = "鏈€澶ч€氳鑰楁椂锛堟绉掞級", example = "2000")
    private Long maxProcessTime;

    @Schema(description = "鏈€灏忛€氳鑰楁椂锛堟绉掞級", example = "120")
    private Long minProcessTime;

    @Schema(description = "娲昏穬鐢ㄦ埛鏁?, example = "234")
    private Integer activeUserCount;

    @Schema(description = "娲昏穬璁惧鏁?, example = "14")
    private Integer activeDeviceCount;

    @Schema(description = "寮傚父璁板綍鏁?, example = "12")
    private Integer abnormalRecordCount;

    @Schema(description = "浣撴俯寮傚父浜烘暟", example = "3")
    private Integer temperatureAbnormalCount;

    @Schema(description = "浣╂埓鍙ｇ僵浜烘暟", example = "1156")
    private Integer maskWearingCount;

    @Schema(description = "鍙ｇ僵浣╂埓鐜?, example = "75.92")
    private BigDecimal maskWearingRate;

    @Schema(description = "鏃舵缁熻")
    private List<TimeSlotStatistics> timeSlotStatistics;

    @Schema(description = "楠岃瘉鏂瑰紡缁熻")
    private List<VerifyMethodStatistics> verifyMethodStatistics;

    @Schema(description = "璁惧缁熻")
    private List<DeviceStatistics> deviceStatistics;

    @Schema(description = "鍖哄煙缁熻")
    private List<AreaStatistics> areaStatistics;

    @Schema(description = "鐢ㄦ埛缁熻")
    private List<UserStatistics> userStatistics;

    @Schema(description = "閮ㄩ棬缁熻")
    private List<DepartmentStatistics> departmentStatistics;

    @Schema(description = "瓒嬪娍鏁版嵁")
    private Map<String, Object> trendData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "鏃舵缁熻")
    public static class TimeSlotStatistics {

        @Schema(description = "鏃堕棿娈?, example = "08:00-09:00")
        private String timeSlot;

        @Schema(description = "閫氳娆℃暟", example = "156")
        private Long accessCount;

        @Schema(description = "鎴愬姛娆℃暟", example = "152")
        private Long successCount;

        @Schema(description = "澶辫触娆℃暟", example = "4")
        private Long failCount;

        @Schema(description = "鎴愬姛鐜?, example = "97.44")
        private BigDecimal successRate;

        @Schema(description = "杩涘叆娆℃暟", example = "89")
        private Long enterCount;

        @Schema(description = "绂诲紑娆℃暟", example = "67")
        private Long exitCount;

        @Schema(description = "骞冲潎鑰楁椂锛堟绉掞級", example = "420")
        private Long averageProcessTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "楠岃瘉鏂瑰紡缁熻")
    public static class VerifyMethodStatistics {

        @Schema(description = "楠岃瘉鏂瑰紡", example = "face")
        private String verifyMethod;

        @Schema(description = "楠岃瘉鏂瑰紡鍚嶇О", example = "浜鸿劯璇嗗埆")
        private String verifyMethodName;

        @Schema(description = "浣跨敤娆℃暟", example = "856")
        private Long usageCount;

        @Schema(description = "浣跨敤鐜?, example = "56.20")
        private BigDecimal usageRate;

        @Schema(description = "鎴愬姛娆℃暟", example = "845")
        private Long successCount;

        @Schema(description = "澶辫触娆℃暟", example = "11")
        private Long failCount;

        @Schema(description = "鎴愬姛鐜?, example = "98.71")
        private BigDecimal successRate;

        @Schema(description = "骞冲潎鑰楁椂锛堟绉掞級", example = "380")
        private Long averageProcessTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "璁惧缁熻")
    public static class DeviceStatistics {

        @Schema(description = "璁惧ID", example = "ACCESS_001")
        private String deviceId;

        @Schema(description = "璁惧鍚嶇О", example = "涓婚棬绂?)
        private String deviceName;

        @Schema(description = "璁惧绫诲瀷", example = "access")
        private String deviceType;

        @Schema(description = "閫氳娆℃暟", example = "234")
        private Long accessCount;

        @Schema(description = "鎴愬姛娆℃暟", example = "230")
        private Long successCount;

        @Schema(description = "澶辫触娆℃暟", example = "4")
        private Long failCount;

        @Schema(description = "鎴愬姛鐜?, example = "98.29")
        private BigDecimal successRate;

        @Schema(description = "骞冲潎鑰楁椂锛堟绉掞級", example = "410")
        private Long averageProcessTime;

        @Schema(description = "鍦ㄧ嚎鏃堕暱锛堝皬鏃讹級", example = "24.0")
        private Double onlineDuration;

        @Schema(description = "鏁呴殰娆℃暟", example = "0")
        private Integer faultCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "鍖哄煙缁熻")
    public static class AreaStatistics {

        @Schema(description = "鍖哄煙ID", example = "1")
        private Long areaId;

        @Schema(description = "鍖哄煙鍚嶇О", example = "涓€妤煎ぇ鍘?)
        private String areaName;

        @Schema(description = "鍖哄煙绫诲瀷", example = "entrance")
        private String areaType;

        @Schema(description = "閫氳娆℃暟", example = "567")
        private Long accessCount;

        @Schema(description = "鎴愬姛娆℃暟", example = "558")
        private Long successCount;

        @Schema(description = "澶辫触娆℃暟", example = "9")
        private Long failCount;

        @Schema(description = "鎴愬姛鐜?, example = "98.41")
        private BigDecimal successRate;

        @Schema(description = "杩涘叆娆℃暟", example = "298")
        private Long enterCount;

        @Schema(description = "绂诲紑娆℃暟", example = "269")
        private Long exitCount;

        @Schema(description = "宄板€兼椂娈?, example = "09:00-10:00")
        private String peakTimeSlot;

        @Schema(description = "宄板€奸€氳娆℃暟", example = "89")
        private Long peakAccessCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "鐢ㄦ埛缁熻")
    public static class UserStatistics {

        @Schema(description = "鐢ㄦ埛ID", example = "1001")
        private Long userId;

        @Schema(description = "鐢ㄦ埛鍚?, example = "admin")
        private String username;

        @Schema(description = "鐪熷疄濮撳悕", example = "绯荤粺绠＄悊鍛?)
        private String realName;

        @Schema(description = "閮ㄩ棬鍚嶇О", example = "鎶€鏈儴")
        private String departmentName;

        @Schema(description = "閫氳娆℃暟", example = "12")
        private Long accessCount;

        @Schema(description = "鎴愬姛娆℃暟", example = "12")
        private Long successCount;

        @Schema(description = "澶辫触娆℃暟", example = "0")
        private Long failCount;

        @Schema(description = "棣栨閫氳鏃堕棿", example = "2025-12-16T08:30:00")
        private LocalDateTime firstAccessTime;

        @Schema(description = "鏈€鍚庨€氳鏃堕棿", example = "2025-12-16T18:45:00")
        private LocalDateTime lastAccessTime;

        @Schema(description = "甯哥敤楠岃瘉鏂瑰紡", example = "face")
        private String primaryVerifyMethod;

        @Schema(description = "甯哥敤璁惧", example = "ACCESS_001")
        private String primaryDevice;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "閮ㄩ棬缁熻")
    public static class DepartmentStatistics {

        @Schema(description = "閮ㄩ棬ID", example = "1")
        private Long departmentId;

        @Schema(description = "閮ㄩ棬鍚嶇О", example = "鎶€鏈儴")
        private String departmentName;

        @Schema(description = "閮ㄩ棬浜烘暟", example = "25")
        private Integer employeeCount;

        @Schema(description = "娲昏穬浜烘暟", example = "23")
        private Integer activeCount;

        @Schema(description = "閫氳娆℃暟", example = "234")
        private Long accessCount;

        @Schema(description = "浜哄潎閫氳娆℃暟", example = "9.36")
        private BigDecimal averageAccessCount;

        @Schema(description = "鎴愬姛娆℃暟", example = "230")
        private Long successCount;

        @Schema(description = "鎴愬姛鐜?, example = "98.29")
        private BigDecimal successRate;

        @Schema(description = "鏈€鏃╅€氳鏃堕棿", example = "2025-12-16T08:15:00")
        private LocalDateTime earliestAccessTime;

        @Schema(description = "鏈€鏅氶€氳鏃堕棿", example = "2025-12-16T20:30:00")
        private LocalDateTime latestAccessTime;
    }
}