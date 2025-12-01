package net.lab1024.sa.admin.module.smart.biometric.domain.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.lab1024.sa.admin.module.smart.biometric.constant.BiometricTypeEnum;

/**
 * 鐢熺墿识别楠岃瘉请求
 *
 * @author AI
 */
@Data
public class BiometricVerifyRequest {

    @NotNull(message = "人脸憳ID涓嶈兘涓虹┖")
    private Long personId;

    @NotNull(message = "设备ID涓嶈兘涓虹┖")
    private Long deviceId;

    @NotNull(message = "鐢熺墿识别类型涓嶈兘涓虹┖")
    private BiometricTypeEnum biometricType;

    @NotBlank(message = "楠岃瘉鍑瘉涓嶈兘涓虹┖")
    private String credential;

    private boolean requireLiveness = true;

    private String offlineToken;

    private String location;
}


