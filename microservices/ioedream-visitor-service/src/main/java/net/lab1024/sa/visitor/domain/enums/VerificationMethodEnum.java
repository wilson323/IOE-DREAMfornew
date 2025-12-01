package net.lab1024.sa.visitor.domain.enums;

import lombok.Getter;

/**
 * 验证方式枚举
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @date 2025-01-27
 */
@Getter
public enum VerificationMethodEnum {

    /**
     * 人脸识别
     */
    FACE_RECOGNITION(1, "人脸识别"),

    /**
     * 指纹识别
     */
    FINGERPRINT(2, "指纹识别"),

    /**
     * 虹膜识别
     */
    IRIS_RECOGNITION(3, "虹膜识别"),

    /**
     * 身份证识别
     */
    ID_CARD(4, "身份证识别"),

    /**
     * 二维码验证
     */
    QR_CODE(5, "二维码验证"),

    /**
     * 手工验证
     */
    MANUAL(6, "手工验证");

    private final Integer code;
    private final String desc;

    VerificationMethodEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举
     */
    public static VerificationMethodEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (VerificationMethodEnum method : values()) {
            if (method.getCode().equals(code)) {
                return method;
            }
        }
        return null;
    }
}