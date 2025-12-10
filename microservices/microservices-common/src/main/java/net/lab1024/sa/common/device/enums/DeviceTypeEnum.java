package net.lab1024.sa.common.device.enums;

import lombok.Getter;

/**
 * 设备类型枚举
 * <p>
 * 定义系统中支持的所有设备类型
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Getter
public enum DeviceTypeEnum {

    /**
     * 摄像头设备
     */
    CAMERA("CAMERA", "摄像头", "视频监控设备"),

    /**
     * 门禁设备
     */
    ACCESS("ACCESS", "门禁", "门禁控制设备"),

    /**
     * 消费机
     */
    CONSUME("CONSUME", "消费机", "消费支付设备"),

    /**
     * 考勤机
     */
    ATTENDANCE("ATTENDANCE", "考勤机", "考勤打卡设备"),

    /**
     * 生物识别设备
     */
    BIOMETRIC("BIOMETRIC", "生物识别", "生物识别设备"),

    /**
     * 闸机
     */
    TURNSTILE("TURNSTILE", "闸机", "通道控制设备"),

    /**
     * 报警器
     */
    ALARM("ALARM", "报警器", "报警设备"),

    /**
     * 传感器
     */
    SENSOR("SENSOR", "传感器", "环境检测设备"),

    /**
     * 对讲机
     */
    INTERCOM("INTERCOM", "对讲机", "对讲通讯设备"),

    /**
     * 显示屏
     */
    DISPLAY("DISPLAY", "显示屏", "信息展示设备"),

    /**
     * 读卡器
     */
    CARD_READER("CARD_READER", "读卡器", "卡片读取设备"),

    /**
     * 指纹仪
     */
    FINGERPRINT("FINGERPRINT", "指纹仪", "指纹识别设备"),

    /**
     * 人脸识别终端
     */
    FACE_RECOGNITION("FACE_RECOGNITION", "人脸识别终端", "人脸识别设备"),

    /**
     * 虹膜识别设备
     */
    IRIS_RECOGNITION("IRIS_RECOGNITION", "虹膜识别设备", "虹膜识别设备"),

    /**
     * 掌纹识别设备
     */
    PALM_RECOGNITION("PALM_RECOGNITION", "掌纹识别设备", "掌纹识别设备"),

    /**
     * 未知设备
     */
    UNKNOWN("UNKNOWN", "未知", "未知设备类型");

    /**
     * 设备类型代码
     */
    private final String code;

    /**
     * 设备类型名称
     */
    private final String name;

    /**
     * 设备类型描述
     */
    private final String description;

    DeviceTypeEnum(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    /**
     * 根据代码获取设备类型枚举
     *
     * @param code 设备类型代码
     * @return 设备类型枚举，如果不存在返回UNKNOWN
     */
    public static DeviceTypeEnum getByCode(String code) {
        if (code == null) {
            return UNKNOWN;
        }

        for (DeviceTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return UNKNOWN;
    }

    /**
     * 根据代码获取设备类型名称
     *
     * @param code 设备类型代码
     * @return 设备类型名称
     */
    public static String getNameByCode(String code) {
        DeviceTypeEnum type = getByCode(code);
        return type != UNKNOWN ? type.name : "未知(" + code + ")";
    }

    /**
     * 判断是否为视频设备
     *
     * @param code 设备类型代码
     * @return 是否为视频设备
     */
    public static boolean isVideoDevice(String code) {
        DeviceTypeEnum type = getByCode(code);
        return type == CAMERA || type == DISPLAY;
    }

    /**
     * 判断是否为门禁设备
     *
     * @param code 设备类型代码
     * @return 是否为门禁设备
     */
    public static boolean isAccessDevice(String code) {
        DeviceTypeEnum type = getByCode(code);
        return type == ACCESS || type == TURNSTILE;
    }

    /**
     * 判断是否为考勤设备
     *
     * @param code 设备类型代码
     * @return 是否为考勤设备
     */
    public static boolean isAttendanceDevice(String code) {
        DeviceTypeEnum type = getByCode(code);
        return type == ATTENDANCE || type == BIOMETRIC || type == CARD_READER;
    }

    /**
     * 判断是否为消费设备
     *
     * @param code 设备类型代码
     * @return 是否为消费设备
     */
    public static boolean isConsumeDevice(String code) {
        DeviceTypeEnum type = getByCode(code);
        return type == CONSUME || type == CARD_READER;
    }

    /**
     * 判断是否为生物识别设备
     *
     * @param code 设备类型代码
     * @return 是否为生物识别设备
     */
    public static boolean isBiometricDevice(String code) {
        DeviceTypeEnum type = getByCode(code);
        return type == BIOMETRIC || type == FINGERPRINT || type == FACE_RECOGNITION
                || type == IRIS_RECOGNITION || type == PALM_RECOGNITION;
    }

    /**
     * 判断是否为安全设备
     *
     * @param code 设备类型代码
     * @return 是否为安全设备
     */
    public static boolean isSecurityDevice(String code) {
        DeviceTypeEnum type = getByCode(code);
        return type == ACCESS || type == ALARM || type == SENSOR || type == TURNSTILE;
    }
}