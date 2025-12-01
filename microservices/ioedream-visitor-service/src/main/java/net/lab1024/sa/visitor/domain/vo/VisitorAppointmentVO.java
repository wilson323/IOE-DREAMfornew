package net.lab1024.sa.visitor.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 访客预约VO
 */
@Data
@Schema(description = "访客预约VO")
public class VisitorAppointmentVO {

    @Schema(description = "预约ID")
    private Long appointmentId;

    @Schema(description = "访客姓名")
    private String visitorName;

    @Schema(description = "手机号码")
    private String phone;

    @Schema(description = "手机号码（别名）")
    private String phoneNumber;

    @Schema(description = "身份证号")
    private String idNumber;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "性别")
    private Integer gender;

    @Schema(description = "被访人ID")
    private Long visiteeId;

    @Schema(description = "被访人姓名")
    private String visiteeName;

    @Schema(description = "来访事由")
    private String visitPurpose;

    @Schema(description = "预期到达时间")
    private LocalDateTime expectedArrivalTime;

    @Schema(description = "预期离开时间")
    private LocalDateTime expectedDepartureTime;

    @Schema(description = "紧急程度")
    private Integer urgencyLevel;

    @Schema(description = "访问区域")
    private List<Long> visitAreas;

    @Schema(description = "预约时间")
    private LocalDateTime appointmentTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}