package net.lab1024.sa.devicecomm.protocol.enums;

import lombok.Getter;

/**
 * 验证方式枚举
 * <p>
 * 根据"安防PUSH通讯协议 （熵基科技）V4.8-20240107"文档附录3定义
 * 完整支持0-29的所有验证方式
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Getter
public enum VerifyTypeEnum {

    /**
     * 密码验证
     */
    PASSWORD(0, "密码", "PASSWORD"),

    /**
     * 指纹验证
     */
    FINGERPRINT(1, "指纹", "FINGERPRINT"),

    /**
     * 卡片验证
     */
    CARD(2, "卡片", "CARD"),

    /**
     * 人脸验证
     */
    FACE(3, "人脸", "FACE"),

    /**
     * 掌纹验证
     */
    PALM(4, "掌纹", "PALM"),

    /**
     * 虹膜验证
     */
    IRIS(5, "虹膜", "IRIS"),

    /**
     * 声纹验证
     */
    VOICE(6, "声纹", "VOICE"),

    /**
     * 静脉验证
     */
    VEIN(7, "静脉", "VEIN"),

    /**
     * 指静脉验证
     */
    FINGER_VEIN(8, "指静脉", "FINGER_VEIN"),

    /**
     * 掌静脉验证
     */
    PALM_VEIN(9, "掌静脉", "PALM_VEIN"),

    /**
     * 二维码验证
     */
    QR_CODE(10, "二维码", "QR_CODE"),

    /**
     * 条形码验证
     */
    BARCODE(11, "条形码", "BARCODE"),

    /**
     * 手机APP验证
     */
    MOBILE_APP(12, "手机APP", "MOBILE_APP"),

    /**
     * 蓝牙验证
     */
    BLUETOOTH(13, "蓝牙", "BLUETOOTH"),

    /**
     * NFC验证
     */
    NFC(14, "NFC", "NFC"),

    /**
     * 混合验证（多种方式组合）
     */
    MIXED(15, "混合验证", "MIXED"),

    /**
     * 指纹+密码
     */
    FINGERPRINT_PASSWORD(16, "指纹+密码", "FINGERPRINT_PASSWORD"),

    /**
     * 卡片+密码
     */
    CARD_PASSWORD(17, "卡片+密码", "CARD_PASSWORD"),

    /**
     * 人脸+密码
     */
    FACE_PASSWORD(18, "人脸+密码", "FACE_PASSWORD"),

    /**
     * 指纹+人脸
     */
    FINGERPRINT_FACE(19, "指纹+人脸", "FINGERPRINT_FACE"),

    /**
     * 卡片+指纹
     */
    CARD_FINGERPRINT(20, "卡片+指纹", "CARD_FINGERPRINT"),

    /**
     * 卡片+人脸
     */
    CARD_FACE(21, "卡片+人脸", "CARD_FACE"),

    /**
     * 指纹+人脸+密码
     */
    FINGERPRINT_FACE_PASSWORD(22, "指纹+人脸+密码", "FINGERPRINT_FACE_PASSWORD"),

    /**
     * 卡片+指纹+密码
     */
    CARD_FINGERPRINT_PASSWORD(23, "卡片+指纹+密码", "CARD_FINGERPRINT_PASSWORD"),

    /**
     * 卡片+人脸+密码
     */
    CARD_FACE_PASSWORD(24, "卡片+人脸+密码", "CARD_FACE_PASSWORD"),

    /**
     * 指纹+掌纹
     */
    FINGERPRINT_PALM(25, "指纹+掌纹", "FINGERPRINT_PALM"),

    /**
     * 人脸+虹膜
     */
    FACE_IRIS(26, "人脸+虹膜", "FACE_IRIS"),

    /**
     * 静脉+密码
     */
    VEIN_PASSWORD(27, "静脉+密码", "VEIN_PASSWORD"),

    /**
     * 指静脉+密码
     */
    FINGER_VEIN_PASSWORD(28, "指静脉+密码", "FINGER_VEIN_PASSWORD"),

    /**
     * 掌静脉+密码
     */
    PALM_VEIN_PASSWORD(29, "掌静脉+密码", "PALM_VEIN_PASSWORD"),

    /**
     * 未知验证方式
     */
    UNKNOWN(-1, "未知", "UNKNOWN");

    /**
     * 验证方式代码
     */
    private final int code;

    /**
     * 验证方式中文名称
     */
    private final String name;

    /**
     * 验证方式英文名称
     */
    private final String englishName;

    VerifyTypeEnum(int code, String name, String englishName) {
        this.code = code;
        this.name = name;
        this.englishName = englishName;
    }

    /**
     * 根据代码获取验证方式枚举
     *
     * @param code 验证方式代码
     * @return 验证方式枚举，如果不存在返回UNKNOWN
     */
    public static VerifyTypeEnum getByCode(int code) {
        for (VerifyTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return UNKNOWN;
    }

    /**
     * 根据代码获取验证方式名称
     *
     * @param code 验证方式代码
     * @return 验证方式名称
     */
    public static String getNameByCode(int code) {
        VerifyTypeEnum type = getByCode(code);
        return type != UNKNOWN ? type.name : "未知(" + code + ")";
    }

    /**
     * 判断是否为混合验证
     *
     * @param code 验证方式代码
     * @return 是否为混合验证
     */
    public static boolean isMixedVerify(int code) {
        return code >= 15 && code <= 29;
    }

    /**
     * 获取主要验证方式（对于混合验证，返回第一个验证方式）
     *
     * @param code 验证方式代码
     * @return 主要验证方式代码（0-卡片，1-指纹，2-人脸等）
     */
    public static int getPrimaryVerifyType(int code) {
        if (code >= 0 && code <= 14) {
            // 单一验证方式，直接返回
            return code;
        } else if (code == 15) {
            // 混合验证，默认返回人脸
            return 3;
        } else if (code >= 16 && code <= 29) {
            // 混合验证，提取第一个验证方式
            // 16-指纹+密码 -> 1
            // 17-卡片+密码 -> 2
            // 18-人脸+密码 -> 3
            // 19-指纹+人脸 -> 1
            // 20-卡片+指纹 -> 2
            // 21-卡片+人脸 -> 2
            // 22-指纹+人脸+密码 -> 1
            // 23-卡片+指纹+密码 -> 2
            // 24-卡片+人脸+密码 -> 2
            // 25-指纹+掌纹 -> 1
            // 26-人脸+虹膜 -> 3
            // 27-静脉+密码 -> 7
            // 28-指静脉+密码 -> 8
            // 29-掌静脉+密码 -> 9
            switch (code) {
                case 16: case 19: case 20: case 22: case 23: case 25:
                    return 1; // 指纹相关
                case 17: case 21: case 24:
                    return 2; // 卡片相关
                case 18: case 26:
                    return 3; // 人脸相关
                case 27:
                    return 7; // 静脉
                case 28:
                    return 8; // 指静脉
                case 29:
                    return 9; // 掌静脉
                default:
                    return 2; // 默认卡片
            }
        }
        return 2; // 默认卡片
    }
}

