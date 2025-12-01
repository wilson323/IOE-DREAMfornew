package net.lab1024.sa.admin.module.smart.biometric.domain.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 鐢熺墿识别类型涓嬪彂选项
 *
 * @author AI
 */
@Data
@Builder
public class BiometricTypeOption {

    private String value;

    private String label;

    private String description;

    private String icon;
}

