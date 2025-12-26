package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 反潜回检测记录VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "反潜回检测记录")
public class AntiPassbackRecordVO {

    /**
     * 记录ID
     */
    @Schema(description = "记录ID", example = "1")
    private Long recordId;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    /**
     * 用户姓名
     */
    @Schema(description = "用户姓名", example = "张三")
    private String userName;

    /**
     * 卡号
     */
    @Schema(description = "卡号", example = "1234567890")
    private String userCardNo;

    /**
     * 设备ID
     */
    @Schema(description = "设备ID", example = "2001")
    private Long deviceId;

    /**
     * 设备名称
     */
    @Schema(description = "设备名称", example = "A栋1楼门禁")
    private String deviceName;

    /**
     * 设备编码
     */
    @Schema(description = "设备编码", example = "AC-001")
    private String deviceCode;

    /**
     * 区域ID
     */
    @Schema(description = "区域ID", example = "101")
    private Long areaId;

    /**
     * 区域名称
     */
    @Schema(description = "区域名称", example = "A栋1楼大厅")
    private String areaName;

    /**
     * 检测结果
     * 1-正常通行
     * 2-软反潜回（告警但允许）
     * 3-硬反潜回（阻止通行）
     */
    @Schema(description = "检测结果", example = "1")
    private Integer result;

    /**
     * 结果名称
     */
    @Schema(description = "结果名称", example = "正常通行")
    private String resultName;

    /**
     * 违规类型
     * 1-时间窗口内重复
     * 2-跨区域异常
     * 3-频次超限
     */
    @Schema(description = "违规类型", example = "1")
    private Integer violationType;

    /**
     * 违规类型名称
     */
    @Schema(description = "违规类型名称", example = "时间窗口内重复")
    private String violationTypeName;

    /**
     * 通行时间
     */
    @Schema(description = "通行时间", example = "1706584800000")
    private LocalDateTime passTime;

    /**
     * 检测时间
     */
    @Schema(description = "检测时间", example = "1706584800000")
    private LocalDateTime detectedTime;

    /**
     * 处理状态
     * 0-未处理
     * 1-已处理
     * 2-已忽略
     */
    @Schema(description = "处理状态", example = "0")
    private Integer handled;

    /**
     * 处理状态名称
     */
    @Schema(description = "处理状态名称", example = "未处理")
    private String handledName;

    /**
     * 处理备注
     */
    @Schema(description = "处理备注")
    private String handleRemark;

    /**
     * 处理人ID
     */
    @Schema(description = "处理人ID", example = "1")
    private Long handledBy;

    /**
     * 处理时间
     */
    @Schema(description = "处理时间", example = "1706584800000")
    private LocalDateTime handledTime;

    /**
     * 详细信息（JSON格式）
     */
    @Schema(description = "详细信息（JSON）")
    private String detailInfo;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "1706584800000")
    private LocalDateTime createdTime;
}
