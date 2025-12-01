package net.lab1024.sa.admin.module.smart.biometric.domain.vo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 离线浠ょ墝鐢宠请求
 *
 * @author AI
 */
@Data
public class BiometricOfflineTokenRequest {

    @NotNull(message = "人脸憳ID涓嶈兘涓虹┖")
    private Long personId;

    @NotNull(message = "设备ID涓嶈兘涓虹┖")
    private Long deviceId;

    @Min(value = 5, message = "鏈夋晥鏈熶笉鑳藉皬浜?鍒嗛挓")
    @Max(value = 720, message = "鏈夋晥鏈熶笉鑳借秴迁移?20鍒嗛挓")
    private Integer validMinutes = 30;
}


