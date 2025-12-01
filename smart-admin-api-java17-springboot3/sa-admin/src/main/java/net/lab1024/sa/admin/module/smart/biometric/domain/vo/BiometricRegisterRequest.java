package net.lab1024.sa.admin.module.smart.biometric.domain.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import net.lab1024.sa.admin.module.smart.biometric.constant.BiometricTypeEnum;

/**
 * 生物特征注册请求
 *
 * @author AI
 */
@Data
public class BiometricRegisterRequest {

    @NotNull(message = "员工ID不能为空")
    private Long employeeId;

    @NotBlank(message = "员工姓名不能为空")
    private String employeeName;

    @Size(max = 64, message = "员工工号长度不能超过64个字符")
    private String employeeCode;

    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @NotNull(message = "生物识别类型不能为空")
    private BiometricTypeEnum biometricType;

    /**
     * 采集到的生物特征内容（可为文件路径或Base64）
     */
    @NotBlank(message = "生物特征数据不能为空")
    private String biometricPayload;

    /**
     * 采集元数据（JSON），包含采集通道、环境参数等
     */
    private String captureMetadata;

    /**
     * 采集通道，如 MOBILE_APP / CARD_READER
     */
    private String captureChannel;

    /**
     * 定位信息（JSON）
     */
    private String location;

    /**
     * 备注
     */
    private String remark;
}


