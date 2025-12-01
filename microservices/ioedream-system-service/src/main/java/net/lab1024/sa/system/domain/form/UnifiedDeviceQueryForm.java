package net.lab1024.sa.system.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.page.PageForm;

/**
 * 统一设备查询表单
 * <p>
 * 用于设备列表查询的数据传输对象
 * 严格遵循repowiki规范：完整的参数验证和业务规则
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "统一设备查询表单")
public class UnifiedDeviceQueryForm extends PageForm {

    /**
     * 设备编码（精确查询）
     */
    @Schema(description = "设备编码（精确查询）", example = "DEV001")
    private String deviceCode;

    /**
     * 设备名称（模糊查询）
     */
    @Schema(description = "设备名称（模糊查询）", example = "门禁")
    private String deviceName;

    /**
     * 设备类型
     * ACCESS-门禁设备, VIDEO-视频设备, CONSUME-消费设备, ATTENDANCE-考勤设备, SMART-智能设备
     */
    @Schema(description = "设备类型", example = "ACCESS")
    private String deviceType;

    /**
     * 设备状态
     * ONLINE-在线, OFFLINE-离线, FAULT-故障, MAINTENANCE-维护中, DELETED-已删除
     */
    @Schema(description = "设备状态", example = "ONLINE")
    private String deviceStatus;

    /**
     * 设备厂商（模糊查询）
     */
    @Schema(description = "设备厂商（模糊查询）", example = "海康")
    private String manufacturer;

    /**
     * 所属区域ID
     */
    @Schema(description = "所属区域ID", example = "1")
    private Long areaId;

    /**
     * 设备IP地址（精确查询）
     */
    @Schema(description = "设备IP地址（精确查询）", example = "192.168.1.100")
    private String ipAddress;

    /**
     * 是否在线
     */
    @Schema(description = "是否在线", example = "true")
    private Boolean isOnline;
}
