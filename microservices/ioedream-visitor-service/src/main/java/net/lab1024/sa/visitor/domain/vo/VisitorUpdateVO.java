package net.lab1024.sa.visitor.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 访客更新VO
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
@Data
@Schema(description = "访客更新请求")
public class VisitorUpdateVO {

    @NotNull(message = "访客ID不能为空")
    @Schema(description = "访客ID", example = "1", required = true)
    private Long visitorId;

    @Size(max = 50, message = "访客姓名长度不能超过50")
    @Schema(description = "访客姓名", example = "张三")
    private String visitorName;

    @Schema(description = "性别 1-男 2-女", example = "1")
    private Integer gender;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13812345678")
    private String phoneNumber;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100")
    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    @Size(max = 100, message = "公司名称长度不能超过100")
    @Schema(description = "公司名称", example = "某某科技有限公司")
    private String company;

    @Schema(description = "被访人ID", example = "1001")
    private Long visiteeId;

    @Size(max = 50, message = "被访人姓名长度不能超过50")
    @Schema(description = "被访人姓名", example = "李四")
    private String visiteeName;

    @Size(max = 100, message = "被访人部门长度不能超过100")
    @Schema(description = "被访人部门", example = "技术部")
    private String visiteeDepartment;

    @Size(max = 500, message = "访问事由长度不能超过500")
    @Schema(description = "访问事由", example = "商务洽谈")
    private String visitPurpose;

    @Schema(description = "预计到达时间", example = "2025-11-28T09:00:00")
    private LocalDateTime expectedArrivalTime;

    @Schema(description = "预计离开时间", example = "2025-11-28T18:00:00")
    private LocalDateTime expectedDepartureTime;

    @Schema(description = "访问区域ID列表", example = "[1, 2, 3]")
    private List<Long> visitAreas;

    @Schema(description = "访问区域名称列表", example = "[\"办公区\", \"会议室\"]")
    private List<String> visitAreaNames;

    @Schema(description = "紧急程度 1-普通 2-紧急 3-特急", example = "1")
    private Integer urgencyLevel;

    @Schema(description = "访客照片路径", example = "/photos/visitor_123.jpg")
    private String photoPath;

    @Schema(description = "身份证正面照片路径", example = "/photos/id_front_123.jpg")
    private String idCardFrontPath;

    @Schema(description = "身份证背面照片路径", example = "/photos/id_back_123.jpg")
    private String idCardBackPath;

    @Size(max = 1000, message = "备注长度不能超过1000")
    @Schema(description = "备注", example = "需要准备会议室")
    private String notes;
}