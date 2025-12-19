package net.lab1024.sa.visitor.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 实时访客状态响应
 *
 * @author IOE-DREAM团队
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@Schema(description = "实时访客状态响应")
public class RealtimeVisitorStatusResponse {

    @Schema(description = "预约ID")
    private Long appointmentId;

    @Schema(description = "访客姓名")
    private String visitorName;

    @Schema(description = "访客手机号")
    private String phoneNumber;

    @Schema(description = "访客身份证号")
    private String idCard;

    @Schema(description = "访问公司")
    private String visitCompany;

    @Schema(description = "被访人姓名")
    private String intervieweeName;

    @Schema(description = "被访人部门")
    private String intervieweeDepartment;

    @Schema(description = "访问事由")
    private String visitReason;

    @Schema(description = "当前状态")
    private String currentStatus;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "签到时间")
    private LocalDateTime checkInTime;

    @Schema(description = "预计离开时间")
    private LocalDateTime expectedLeaveTime;

    @Schema(description = "当前位置")
    private String currentLocation;

    @Schema(description = "访问区域权限")
    private List<String> accessAreas;

    @Schema(description = "是否超时")
    private Boolean isOvertime;

    @Schema(description = "车牌号")
    private String plateNumber;

    @Schema(description = "陪同人员")
    private String escortPerson;

    @Schema(description = "最后更新时间")
    private LocalDateTime lastUpdateTime;
}