package net.lab1024.sa.system.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 统一设备新增表单
 * <p>
 * 用于设备新增的数据验证和传输
 * 严格遵循repowiki规范：完整的参数验证和业务规则
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Data
@Schema(description = "统一设备新增表单")
public class UnifiedDeviceAddForm {

    /**
     * 设备编号（设备唯一标识）
     */
    @NotBlank(message = "设备编号不能为空")
    @Size(max = 50, message = "设备编号长度不能超过50个字符")
    @Schema(description = "设备编号", example = "DEV001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String deviceCode;

    /**
     * 设备名称
     */
    @NotBlank(message = "设备名称不能为空")
    @Size(max = 100, message = "设备名称长度不能超过100个字符")
    @Schema(description = "设备名称", example = "门禁设备01", requiredMode = Schema.RequiredMode.REQUIRED)
    private String deviceName;

    /**
     * 设备类型
     * ACCESS-门禁设备, VIDEO-视频设备, CONSUME-消费设备, ATTENDANCE-考勤设备, SMART-智能设备
     */
    @NotBlank(message = "设备类型不能为空")
    @Schema(description = "设备类型", example = "ACCESS", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {
            "ACCESS", "VIDEO", "CONSUME",
            "ATTENDANCE", "SMART" })
    private String deviceType;

    /**
     * 设备型号
     */
    @Size(max = 50, message = "设备型号长度不能超过50个字符")
    @Schema(description = "设备型号", example = "AC-100")
    private String deviceModel;

    /**
     * 设备厂商
     */
    @Size(max = 50, message = "设备厂商长度不能超过50个字符")
    @Schema(description = "设备厂商", example = "海康威视")
    private String manufacturer;

    /**
     * 设备序列号
     */
    @Size(max = 100, message = "设备序列号长度不能超过100个字符")
    @Schema(description = "设备序列号", example = "SN123456789")
    private String serialNumber;

    /**
     * 设备IP地址
     */
    @Pattern(regexp = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$", message = "IP地址格式不正确")
    @Schema(description = "设备IP地址", example = "192.168.1.100")
    private String ipAddress;

    /**
     * 设备端口号
     */
    @Schema(description = "设备端口号", example = "8000")
    private Integer port;

    /**
     * 设备MAC地址
     */
    @Pattern(regexp = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$", message = "MAC地址格式不正确")
    @Schema(description = "设备MAC地址", example = "00:11:22:33:44:55")
    private String macAddress;

    /**
     * 所属区域ID
     */
    @Schema(description = "所属区域ID", example = "1")
    private Long areaId;

    /**
     * 设备位置描述
     */
    @Size(max = 200, message = "设备位置描述长度不能超过200个字符")
    @Schema(description = "设备位置描述", example = "1号楼1层大厅")
    private String location;

    /**
     * 设备描述
     */
    @Size(max = 500, message = "设备描述长度不能超过500个字符")
    @Schema(description = "设备描述", example = "主要门禁设备")
    private String deviceDescription;

    /**
     * 工作模式（主要用于门禁设备）
     * 0-普通模式，1-刷卡模式，2-人脸模式，3-指纹模式，4-混合模式
     */
    @Schema(description = "工作模式", example = "4", allowableValues = { "0", "1", "2", "3", "4" })
    private Integer workMode;

    /**
     * 设备配置信息（JSON格式）
     */
    @Schema(description = "设备配置信息（JSON格式）", example = "{}")
    private String deviceConfig;

    /**
     * 扩展属性（JSON格式）
     */
    @Schema(description = "扩展属性（JSON格式）", example = "{}")
    private String extendProperties;
}
