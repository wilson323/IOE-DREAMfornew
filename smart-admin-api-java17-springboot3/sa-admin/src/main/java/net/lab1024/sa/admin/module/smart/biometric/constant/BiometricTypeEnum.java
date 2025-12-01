package net.lab1024.sa.admin.module.smart.biometric.constant;

import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 鐢熺墿识别类型鏋氫妇
 *
 * @author SmartAdmin Team
 * @date 2025-01-15
 */
public enum BiometricTypeEnum implements BaseEnum {

    /**
     * 浜鸿劯识别
     */
    FACE("FACE", "浜鸿劯识别"),

    /**
     * 鎸囩汗识别
     */
    FINGERPRINT("FINGERPRINT", "鎸囩汗识别"),

    /**
     * 鎺岀汗识别
     */
    PALMPRINT("PALMPRINT", "鎺岀汗识别"),

    /**
     * 铏硅啘识别
     */
    IRIS("IRIS", "铏硅啘识别");

    private final String value;
    private final String description;

    BiometricTypeEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public String getValue() {
        return value;
    }

    
    @Override
    public String getDesc() {
        return description;
    }

    public static BiometricTypeEnum fromValue(String value) {
        for (BiometricTypeEnum type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }
}