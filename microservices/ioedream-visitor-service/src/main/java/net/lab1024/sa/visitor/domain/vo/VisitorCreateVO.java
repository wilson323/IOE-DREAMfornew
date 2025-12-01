package net.lab1024.sa.visitor.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 访客创建VO
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
@Data
@Schema(description = "访客创建请求")
public class VisitorCreateVO {

    @NotBlank(message = "访客姓名不能为空")
    @Size(max = 50, message = "访客姓名长度不能超过50")
    @Schema(description = "访客姓名", example = "张三", required = true)
    private String visitorName;

    @NotNull(message = "性别不能为空")
    @Schema(description = "性别 1-男 2-女", example = "1", required = true)
    private Integer gender;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13812345678", required = true)
    private String phoneNumber;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100")
    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]$",
             message = "身份证号格式不正确")
    @Schema(description = "身份证号", example = "110101199001011234", required = true)
    private String idNumber;

    @Size(max = 100, message = "公司名称长度不能超过100")
    @Schema(description = "公司名称", example = "某某科技有限公司")
    private String company;

    @NotNull(message = "被访人ID不能为空")
    @Schema(description = "被访人ID", example = "1001", required = true)
    private Long visiteeId;

    @NotBlank(message = "被访人姓名不能为空")
    @Size(max = 50, message = "被访人姓名长度不能超过50")
    @Schema(description = "被访人姓名", example = "李四", required = true)
    private String visiteeName;

    @Size(max = 100, message = "被访人部门长度不能超过100")
    @Schema(description = "被访人部门", example = "技术部")
    private String visiteeDepartment;

    @NotBlank(message = "访问事由不能为空")
    @Size(max = 500, message = "访问事由长度不能超过500")
    @Schema(description = "访问事由", example = "商务洽谈", required = true)
    private String visitPurpose;

    @NotNull(message = "预计到达时间不能为空")
    @Schema(description = "预计到达时间", example = "2025-11-28T09:00:00", required = true)
    private LocalDateTime expectedArrivalTime;

    @Schema(description = "预计离开时间", example = "2025-11-28T18:00:00")
    private LocalDateTime expectedDepartureTime;

    @Schema(description = "访问区域ID列表", example = "[1, 2, 3]")
    private List<Long> visitAreas;

    @Schema(description = "访问区域名称列表", example = "[\"办公区\", \"会议室\"]")
    private List<String> visitAreaNames;

    @NotNull(message = "紧急程度不能为空")
    @Schema(description = "紧急程度 1-普通 2-紧急 3-特急", example = "1", required = true)
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