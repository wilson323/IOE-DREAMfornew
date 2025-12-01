package net.lab1024.sa.admin.module.smart.biometric.domain.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.lab1024.sa.admin.module.smart.biometric.service.BiometricMonitorService;

/**
 * 鍛婅瑙﹀彂璇锋眰VO.
 *
 * @author AI
 */
@Data
public class BiometricAlertRequestVO {

    @NotBlank(message = "engineId涓嶈兘涓虹┖")
    private String engineId;

    @NotBlank(message = "alertCode涓嶈兘涓虹┖")
    private String alertCode;

    @NotBlank(message = "message涓嶈兘涓虹┖")
    private String message;

    @NotNull(message = "level涓嶈兘涓虹┖")
    private BiometricMonitorService.AlertLevel level;
}


