package net.lab1024.sa.consume.domain.vo;

import java.util.List;

import lombok.Data;

/**
 * 消费设备配置
 *
 * @author IOE-DREAM Team
 * @since 2025-12-22
 */
@Data
public class ConsumeDeviceConfigVO {

    private Long deviceId;

    private String deviceName;

    private String areaId;

    private Integer manageMode;

    private List<String> consumeModes;

    private Boolean offlineEnabled;
}
