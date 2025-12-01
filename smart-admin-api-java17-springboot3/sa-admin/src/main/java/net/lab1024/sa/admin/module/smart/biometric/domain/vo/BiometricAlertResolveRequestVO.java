package net.lab1024.sa.admin.module.smart.biometric.domain.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 鍛婅瑙ｅ喅璇锋眰.
 *
 * @author AI
 */
@Data
public class BiometricAlertResolveRequestVO {

    @NotBlank(message = "resolution涓嶈兘涓虹┖")
    private String resolution;
}


