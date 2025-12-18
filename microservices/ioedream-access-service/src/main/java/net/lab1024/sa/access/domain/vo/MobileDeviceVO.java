package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 绉诲姩绔澶囦俊鎭疺O
 * 鐢ㄤ簬绉诲姩绔澶囩鐞嗙晫闈㈠睍绀鸿澶囦俊鎭?
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "绉诲姩绔澶囦俊鎭疺O")
public class MobileDeviceVO {

    @Schema(description = "璁惧ID", example = "1001")
    private Long deviceId;

    @Schema(description = "璁惧缂栫爜", example = "DEV001")
    private String deviceCode;

    @Schema(description = "璁惧鍚嶇О", example = "涓诲叆鍙ｉ棬绂?)
    private String deviceName;

    @Schema(description = "璁惧绫诲瀷", example = "1")
    private Integer deviceType;

    @Schema(description = "璁惧绫诲瀷鍚嶇О", example = "闂ㄧ鎺у埗鍣?)
    private String deviceTypeName;

    @Schema(description = "璁惧瀛愮被鍨?, example = "11")
    private Integer deviceSubType;

    @Schema(description = "璁惧瀛愮被鍨嬪悕绉?, example = "浜鸿劯璇嗗埆闂ㄧ")
    private String deviceSubTypeName;

    @Schema(description = "鎵€灞炲尯鍩烮D", example = "1001")
    private Long areaId;

    @Schema(description = "鎵€灞炲尯鍩熷悕绉?, example = "A鏍?妤煎ぇ鍘?)
    private String areaName;

    @Schema(description = "璁惧浣嶇疆", example = "A鏍?妤煎ぇ鍘呬富鍏ュ彛")
    private String location;

    @Schema(description = "璁惧鐘舵€?, example = "1")
    private Integer status;

    @Schema(description = "璁惧鐘舵€佸悕绉?, example = "鍦ㄧ嚎")
    private String statusName;

    @Schema(description = "璁惧IP鍦板潃", example = "192.168.1.100")
    private String ipAddress;

    @Schema(description = "璁惧MAC鍦板潃", example = "AA:BB:CC:DD:EE:FF")
    private String macAddress;

    @Schema(description = "璁惧鍨嬪彿", example = "IOE-ACCESS-2000")
    private String deviceModel;

    @Schema(description = "鍥轰欢鐗堟湰", example = "v2.1.0")
    private String firmwareVersion;

    @Schema(description = "璁惧鍘傚晢", example = "IOE绉戞妧")
    private String manufacturer;

    @Schema(description = "瀹夎鏃堕棿", example = "2024-01-15T10:30:00")
    private LocalDateTime installTime;

    @Schema(description = "鏈€鍚庣淮鎶ゆ椂闂?, example = "2024-12-01T09:00:00")
    private LocalDateTime lastMaintenanceTime;

    @Schema(description = "鏈€鍚庡湪绾挎椂闂?, example = "2025-12-16T14:30:00")
    private LocalDateTime lastOnlineTime;

    @Schema(description = "璁惧鎻忚堪", example = "涓诲叆鍙ｉ棬绂佹帶鍒跺櫒锛屾敮鎸佷汉鑴歌瘑鍒拰鍒峰崱")
    private String description;

    @Schema(description = "鏄惁鏀寔杩滅▼鎺у埗", example = "true")
    private Boolean supportRemoteControl;

    @Schema(description = "鏄惁鏀寔鍥轰欢鍗囩骇", example = "true")
    private Boolean supportFirmwareUpgrade;

    @Schema(description = "璁惧鎵╁睍灞炴€?JSON鏍煎紡)", example = "{\"accessMode\":\"card+face\",\"antiPassback\":true}")
    private String extendedAttributes;

    @Schema(description = "鏉冮檺绾у埆", example = "1")
    private Integer permissionLevel;

    @Schema(description = "涓氬姟妯″潡", example = "access")
    private String businessModule;

    @Schema(description = "鍒涘缓鏃堕棿", example = "2024-01-15T10:30:00")
    private LocalDateTime createTime;

    @Schema(description = "鏇存柊鏃堕棿", example = "2025-12-16T14:30:00")
    private LocalDateTime updateTime;

    // 绉诲姩绔壒鏈夊瓧娈?

    @Schema(description = "淇″彿寮哄害", example = "-45")
    private Integer signalStrength;

    @Schema(description = "鐢垫睜鐢甸噺(鐧惧垎姣旓紝閫傜敤浜庢棤绾胯澶?", example = "85")
    private Integer batteryLevel;

    @Schema(description = "娓╁害(鎽勬皬搴?", example = "25.5")
    private Double temperature;

    @Schema(description = "CPU浣跨敤鐜?鐧惧垎姣?", example = "15.2")
    private Double cpuUsage;

    @Schema(description = "鍐呭瓨浣跨敤鐜?鐧惧垎姣?", example = "32.8")
    private Double memoryUsage;

    @Schema(description = "瀛樺偍浣跨敤鐜?鐧惧垎姣?", example = "45.6")
    private Double storageUsage;

    @Schema(description = "浠婃棩閫氳娆℃暟", example = "156")
    private Long todayAccessCount;

    @Schema(description = "浠婃棩寮傚父娆℃暟", example = "2")
    private Long todayErrorCount;

    @Schema(description = "璁惧鍋ュ悍璇勫垎(0-100)", example = "95")
    private Integer healthScore;

    @Schema(description = "鏄惁闇€瑕佺淮鎶?, example = "false")
    private Boolean needsMaintenance;

    @Schema(description = "璺濈涓婃缁存姢澶╂暟", example = "15")
    private Integer daysSinceLastMaintenance;
}