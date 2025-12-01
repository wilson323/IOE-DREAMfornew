package net.lab1024.sa.visitor.domain.vo;



import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 访客记录展示对象
 * <p>
 * 严格遵循repowiki规范：
 * - 用于前端数据展示的VO对象
 * - 敏感信息脱敏处理
 * - 日期时间格式化
 * - JSON序列化配置
 *
 * @author SmartAdmin Team
 * @since 2025-11-26
 */
@Data
@Schema(description = "访客记录VO")

public class VisitorRecordVO {

    @Schema(description = "访客记录ID")
    private Long visitorRecordId;

    @Schema(description = "访客姓名")
    private String visitorName;

    @Schema(description = "访客手机号（脱敏）")
    private String visitorPhone;

    @Schema(description = "访客身份证号（脱敏）")
    private String visitorIdCard;

    @Schema(description = "访问事由")
    private String visitPurpose;

    @Schema(description = "被访人姓名")
    private String visitedPersonName;

    @Schema(description = "被访部门")
    private String visitedDepartment;

    @Schema(description = "预约开始时间")
    
    private LocalDateTime appointmentStartTime;

    @Schema(description = "预约结束时间")
    
    private LocalDateTime appointmentEndTime;

    @Schema(description = "实际到达时间")
    
    private LocalDateTime actualArrivalTime;

    @Schema(description = "实际离开时间")
    
    private LocalDateTime actualLeaveTime;

    @Schema(description = "访问状态：0-预约中，1-进行中，2-已完成，3-已取消")
    private Integer visitStatus;

    @Schema(description = "访问状态描述")
    private String visitStatusDesc;

    @Schema(description = "通行证编号")
    private String accessCardNumber;

    @Schema(description = "陪同人员")
    private String escortPerson;

    @Schema(description = "备注信息")
    private String remark;

    @Schema(description = "创建时间")
    
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    
    private LocalDateTime updateTime;

    @Schema(description = "创建人员")
    private String createUserName;
}