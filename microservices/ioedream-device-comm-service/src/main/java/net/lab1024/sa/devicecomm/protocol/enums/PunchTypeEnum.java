package net.lab1024.sa.devicecomm.protocol.enums;

import lombok.Getter;

/**
 * 打卡类型枚举
 * <p>
 * 根据"考勤PUSH通讯协议 （熵基科技） V4.0-20210113"文档定义
 * 支持智能判断打卡类型（上班/下班）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Getter
public enum PunchTypeEnum {

    /**
     * 上班打卡
     */
    CHECK_IN(0, "上班", "CHECK_IN"),

    /**
     * 下班打卡
     */
    CHECK_OUT(1, "下班", "CHECK_OUT"),

    /**
     * 外出打卡
     */
    OUT(2, "外出", "OUT"),

    /**
     * 返回打卡
     */
    RETURN(3, "返回", "RETURN"),

    /**
     * 未知类型
     */
    UNKNOWN(-1, "未知", "UNKNOWN");

    /**
     * 打卡类型代码
     */
    private final int code;

    /**
     * 打卡类型中文名称
     */
    private final String name;

    /**
     * 打卡类型英文名称
     */
    private final String englishName;

    PunchTypeEnum(int code, String name, String englishName) {
        this.code = code;
        this.name = name;
        this.englishName = englishName;
    }

    /**
     * 根据代码获取打卡类型枚举
     *
     * @param code 打卡类型代码
     * @return 打卡类型枚举
     */
    public static PunchTypeEnum getByCode(int code) {
        for (PunchTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return UNKNOWN;
    }

    /**
     * 根据代码获取打卡类型名称
     *
     * @param code 打卡类型代码
     * @return 打卡类型名称
     */
    public static String getNameByCode(int code) {
        PunchTypeEnum type = getByCode(code);
        return type != UNKNOWN ? type.name : "未知(" + code + ")";
    }

    /**
     * 智能判断打卡类型
     * <p>
     * 根据打卡时间和业务规则智能判断是上班还是下班
     * </p>
     *
     * @param punchTime 打卡时间（Unix时间戳，秒）
     * @param status 考勤状态
     * @param verifyType 验证方式
     * @return 打卡类型代码
     */
    public static int smartDeterminePunchType(long punchTime, int status, int verifyType) {
        // 获取打卡时间的小时数（24小时制）
        java.time.LocalDateTime dateTime = java.time.LocalDateTime.ofEpochSecond(
                punchTime, 0, java.time.ZoneOffset.of("+8"));
        int hour = dateTime.getHour();

        // 根据时间智能判断
        // 上午时间段（6:00-14:00）通常为上班打卡
        if (hour >= 6 && hour < 14) {
            return CHECK_IN.getCode();
        }
        // 下午时间段（14:00-22:00）通常为下班打卡
        else if (hour >= 14 && hour < 22) {
            return CHECK_OUT.getCode();
        }
        // 其他时间段根据状态判断
        else {
            // 如果状态为加班，可能是下班打卡
            if (status == AttendanceStatusEnum.OVERTIME.getCode()) {
                return CHECK_OUT.getCode();
            }
            // 默认上班打卡
            return CHECK_IN.getCode();
        }
    }
}


