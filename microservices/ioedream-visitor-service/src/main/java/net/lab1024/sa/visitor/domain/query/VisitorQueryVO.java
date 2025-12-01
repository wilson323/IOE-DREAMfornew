package net.lab1024.sa.visitor.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 访客查询VO
 */
@Data
@Schema(description = "访客查询VO")
public class VisitorQueryVO {

    @Schema(description = "访客姓名")
    private String visitorName;

    @Schema(description = "手机号码")
    private String phone;

    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "来访公司")
    private String company;

    @Schema(description = "访问状态")
    private String status;

    @Schema(description = "访问开始时间")
    private LocalDateTime visitStartTime;

    @Schema(description = "访问结束时间")
    private LocalDateTime visitEndTime;

    @Schema(description = "创建开始时间")
    private LocalDateTime createTimeStart;

    @Schema(description = "创建结束时间")
    private LocalDateTime createTimeEnd;

    @Schema(description = "页码")
    private Integer pageNum = 1;

    @Schema(description = "每页大小")
    private Integer pageSize = 10;

    // 添加缺失的getter方法以满足编译需要
    public String getIdNumber() {
        return idCard;
    }

    public String getVisiteeName() {
        return visitorName; // 使用visitorName作为被访人姓名
    }
}