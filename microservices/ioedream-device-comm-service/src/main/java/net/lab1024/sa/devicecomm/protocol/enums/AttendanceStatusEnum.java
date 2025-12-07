package net.lab1024.sa.devicecomm.protocol.enums;

import lombok.Getter;

/**
 * 考勤状态枚举
 * <p>
 * 根据"考勤PUSH通讯协议 （熵基科技） V4.0-20210113"文档定义
 * 完整支持考勤状态和打卡类型
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Getter
public enum AttendanceStatusEnum {

    /**
     * 正常出勤
     */
    NORMAL(0, "正常", "NORMAL"),

    /**
     * 迟到
     */
    LATE(1, "迟到", "LATE"),

    /**
     * 早退
     */
    EARLY_LEAVE(2, "早退", "EARLY_LEAVE"),

    /**
     * 缺勤
     */
    ABSENT(3, "缺勤", "ABSENT"),

    /**
     * 加班
     */
    OVERTIME(4, "加班", "OVERTIME"),

    /**
     * 外出
     */
    OUT(5, "外出", "OUT"),

    /**
     * 请假
     */
    LEAVE(6, "请假", "LEAVE"),

    /**
     * 出差
     */
    BUSINESS_TRIP(7, "出差", "BUSINESS_TRIP"),

    /**
     * 调休
     */
    COMPENSATORY_LEAVE(8, "调休", "COMPENSATORY_LEAVE"),

    /**
     * 未知状态
     */
    UNKNOWN(-1, "未知", "UNKNOWN");

    /**
     * 状态代码
     */
    private final int code;

    /**
     * 状态中文名称
     */
    private final String name;

    /**
     * 状态英文名称
     */
    private final String englishName;

    AttendanceStatusEnum(int code, String name, String englishName) {
        this.code = code;
        this.name = name;
        this.englishName = englishName;
    }

    /**
     * 根据代码获取考勤状态枚举
     *
     * @param code 状态代码
     * @return 考勤状态枚举
     */
    public static AttendanceStatusEnum getByCode(int code) {
        for (AttendanceStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return UNKNOWN;
    }

    /**
     * 根据代码获取考勤状态名称
     *
     * @param code 状态代码
     * @return 考勤状态名称
     */
    public static String getNameByCode(int code) {
        AttendanceStatusEnum status = getByCode(code);
        return status != UNKNOWN ? status.name : "未知(" + code + ")";
    }
}

