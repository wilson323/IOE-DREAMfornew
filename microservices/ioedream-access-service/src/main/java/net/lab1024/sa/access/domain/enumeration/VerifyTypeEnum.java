package net.lab1024.sa.access.domain.enumeration;

import lombok.Getter;

/**
 * 认证方式枚举
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 枚举类统一管理所有认证方式
 * - 支持9种认证方式：人脸、指纹、掌纹、虹膜、声纹、IC卡、二维码、密码、NFC
 * - 提供类型转换和描述方法
 * </p>
 * <p>
 * 认证方式分类：
 * - 生物识别认证：人脸、指纹、掌纹、虹膜、声纹
 * - 非生物识别认证：IC卡、二维码、密码、NFC
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Getter
public enum VerifyTypeEnum {

    /**
     * 密码认证
     * 类型：非生物识别
     * 值：0
     */
    PASSWORD(0, "PASSWORD", "密码", "非生物识别"),

    /**
     * 指纹认证
     * 类型：生物识别
     * 值：1
     */
    FINGERPRINT(1, "FINGERPRINT", "指纹", "生物识别"),

    /**
     * IC卡认证
     * 类型：非生物识别
     * 值：2
     */
    CARD(2, "CARD", "IC卡", "非生物识别"),

    /**
     * 人脸认证
     * 类型：生物识别
     * 值：11
     */
    FACE(11, "FACE", "人脸", "生物识别"),

    /**
     * 掌纹认证
     * 类型：生物识别
     * 值：12
     */
    PALM(12, "PALM", "掌纹", "生物识别"),

    /**
     * 虹膜认证
     * 类型：生物识别
     * 值：13
     */
    IRIS(13, "IRIS", "虹膜", "生物识别"),

    /**
     * 声纹认证
     * 类型：生物识别
     * 值：14
     */
    VOICE(14, "VOICE", "声纹", "生物识别"),

    /**
     * 二维码认证
     * 类型：非生物识别
     * 值：20
     */
    QR_CODE(20, "QR_CODE", "二维码", "非生物识别"),

    /**
     * NFC认证
     * 类型：非生物识别
     * 值：21
     */
    NFC(21, "NFC", "NFC", "非生物识别");

    /**
     * 认证方式代码（与设备协议保持一致）
     */
    private final Integer code;

    /**
     * 认证方式英文名称
     */
    private final String name;

    /**
     * 认证方式中文描述
     */
    private final String description;

    /**
     * 认证类型（生物识别/非生物识别）
     */
    private final String category;

    /**
     * 构造函数
     *
     * @param code        认证方式代码
     * @param name        认证方式英文名称
     * @param description 认证方式中文描述
     * @param category    认证类型
     */
    VerifyTypeEnum(Integer code, String name, String description, String category) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.category = category;
    }

    /**
     * 根据代码获取枚举
     *
     * @param code 认证方式代码
     * @return 认证方式枚举，如果不存在则返回null
     */
    public static VerifyTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (VerifyTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据名称获取枚举
     *
     * @param name 认证方式英文名称
     * @return 认证方式枚举，如果不存在则返回null
     */
    public static VerifyTypeEnum getByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        for (VerifyTypeEnum type : values()) {
            if (type.getName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据验证方法字符串获取枚举
     * <p>
     * 兼容现有代码中的verifyMethod字段（字符串类型）
     * </p>
     *
     * @param verifyMethod 验证方法字符串（如"FACE"、"FINGERPRINT"等）
     * @return 认证方式枚举，如果不存在则返回null
     */
    public static VerifyTypeEnum getByVerifyMethod(String verifyMethod) {
        return getByName(verifyMethod);
    }

    /**
     * 是否为生物识别认证
     *
     * @return 是否为生物识别
     */
    public boolean isBiometric() {
        return "生物识别".equals(this.category);
    }

    /**
     * 是否为非生物识别认证
     *
     * @return 是否为非生物识别
     */
    public boolean isNonBiometric() {
        return "非生物识别".equals(this.category);
    }
}
