package net.lab1024.sa.biometric.domain.entity;

/**
 * 生物识别类型枚举
 * <p>
 * 支持5大生物识别类型：人脸、指纹、虹膜、掌纹、声纹
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
public enum BiometricType {
    /**
     * 人脸识别
     */
    FACE(1, "人脸识别", 512),

    /**
     * 指纹识别
     */
    FINGERPRINT(2, "指纹识别", 128),

    /**
     * 虹膜识别
     */
    IRIS(3, "虹膜识别", 256),

    /**
     * 掌纹识别
     */
    PALM(4, "掌纹识别", 256),

    /**
     * 声纹识别
     */
    VOICE(5, "声纹识别", 128);

    /**
     * 类型代码
     */
    private final Integer code;

    /**
     * 类型名称
     */
    private final String name;

    /**
     * 特征向量维度
     */
    private final Integer dimension;

    BiometricType(Integer code, String name, Integer dimension) {
        this.code = code;
        this.name = name;
        this.dimension = dimension;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Integer getDimension() {
        return dimension;
    }

    /**
     * 根据代码获取类型
     *
     * @param code 类型代码
     * @return 生物识别类型
     */
    public static BiometricType fromCode(Integer code) {
        for (BiometricType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的生物识别类型代码: " + code);
    }
}
