package net.lab1024.sa.admin.module.video.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;

import net.lab1024.sa.base.common.domain.PageParam;

/**
 * 视频设备查询表单
 *
 * @author SmartAdmin Team
 * @version 1.0
 * @since 2025-11-16
 */

@Schema(description = "视频设备查询表单")
public class VideoDeviceQueryForm extends PageParam {

    /**
     * 设备名称
     */
    @Schema(description = "设备名称", example = "主入口")
    private String deviceName;

    /**
     * 设备类型
     */
    @Schema(description = "设备类型", example = "CAMERA", allowableValues = {"CAMERA", "NVR", "DVR"})
    private String deviceType;

    /**
     * 设备状态
     */
    @Schema(description = "设备状态", example = "ONLINE", allowableValues = {"ONLINE", "OFFLINE", "FAULT"})
    private String deviceStatus;

    /**
     * 所属区域ID
     */
    @Schema(description = "所属区域ID", example = "1")
    private Long areaId;

    /**
     * 制造商
     */
    @Schema(description = "制造商", example = "海康威视")
    private String manufacturer;

    /**
     * 开始创建时间
     */
    @Schema(description = "开始创建时间", example = "2025-01-01")
    private String createTimeBegin;

    /**
     * 结束创建时间
     */
    @Schema(description = "结束创建时间", example = "2025-01-31")
    private String createTimeEnd;

    /**
     * 是否启用云台控制
     */
    @Schema(description = "是否启用云台控制", example = "1", allowableValues = {"0", "1"})
    private Integer ptzEnabled;

    /**
     * 是否启用录像
     */
    @Schema(description = "是否启用录像", example = "1", allowableValues = {"0", "1"})
    private Integer recordEnabled;
}