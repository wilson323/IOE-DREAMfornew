package net.lab1024.sa.access.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 门禁反潜回检测记录实体
 * <p>
 * 记录每次反潜回检测结果：
 * - 正常通行（result=1）
 * - 软反潜回（result=2）：告警但允许通行
 * - 硬反潜回（result=3）：阻止通行
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_access_anti_passback_record")
@Schema(description = "门禁反潜回检测记录")
public class AntiPassbackRecordEntity {

    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "记录ID", example = "1")
    private Long recordId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    /**
     * 用户姓名
     */
    @TableField("user_name")
    @Schema(description = "用户姓名", example = "张三")
    private String userName;

    /**
     * 卡号
     */
    @TableField("user_card_no")
    @Schema(description = "卡号", example = "1234567890")
    private String userCardNo;

    /**
     * 设备ID
     */
    @TableField("device_id")
    @Schema(description = "设备ID", example = "2001")
    private Long deviceId;

    /**
     * 设备名称
     */
    @TableField("device_name")
    @Schema(description = "设备名称", example = "A栋1楼门禁")
    private String deviceName;

    /**
     * 设备编码
     */
    @TableField("device_code")
    @Schema(description = "设备编码", example = "AC-001")
    private String deviceCode;

    /**
     * 区域ID
     */
    @TableField("area_id")
    @Schema(description = "区域ID", example = "101")
    private Long areaId;

    /**
     * 区域名称
     */
    @TableField("area_name")
    @Schema(description = "区域名称", example = "A栋1楼大厅")
    private String areaName;

    /**
     * 检测结果
     * 1-正常通行
     * 2-软反潜回（告警但允许）
     * 3-硬反潜回（阻止通行）
     */
    @TableField("result")
    @Schema(description = "检测结果", example = "1")
    private Integer result;

    /**
     * 违规类型
     * 1-时间窗口内重复
     * 2-跨区域异常
     * 3-频次超限
     */
    @TableField("violation_type")
    @Schema(description = "违规类型", example = "1")
    private Integer violationType;

    /**
     * 通行时间
     */
    @TableField("pass_time")
    @Schema(description = "通行时间", example = "1706584800000")
    private LocalDateTime passTime;

    /**
     * 检测时间
     */
    @TableField("detected_time")
    @Schema(description = "检测时间", example = "1706584800000")
    private LocalDateTime detectedTime;

    /**
     * 处理状态
     * 0-未处理
     * 1-已处理
     * 2-已忽略
     */
    @TableField("handled")
    @Schema(description = "处理状态", example = "0")
    private Integer handled;

    /**
     * 处理备注
     */
    @TableField("handle_remark")
    @Schema(description = "处理备注")
    private String handleRemark;

    /**
     * 处理人ID
     */
    @TableField("handled_by")
    @Schema(description = "处理人ID", example = "1")
    private Long handledBy;

    /**
     * 处理时间
     */
    @TableField("handled_time")
    @Schema(description = "处理时间", example = "1706584800000")
    private LocalDateTime handledTime;

    /**
     * 详细信息（JSON格式）
     */
    @TableField("detail_info")
    @Schema(description = "详细信息（JSON）")
    private String detailInfo;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间", example = "1706584800000")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间", example = "1706584800000")
    private LocalDateTime updatedTime;

    /**
     * 删除标记
     * 0-未删除 1-已删除
     */
    @TableLogic
    @TableField("deleted_flag")
    @Schema(description = "删除标记", example = "0")
    private Integer deletedFlag;
}
