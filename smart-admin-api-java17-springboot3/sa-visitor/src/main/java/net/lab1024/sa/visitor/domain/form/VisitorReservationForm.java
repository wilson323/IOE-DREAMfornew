package net.lab1024.sa.visitor.domain.form;



import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 访客预约表单对象
 * <p>
 * 严格遵循repowiki规范：
 * - 使用jakarta包名
 * - 完整的字段注解和验证
 * - 日期时间格式化
 * - Swagger文档注解
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Data

@Schema(description = "访客预约表单")
public class VisitorReservationForm {

    /**
     * 预约ID
     */
    @Schema(description = "预约ID", example = "1")
    private Long reservationId;

    /**
     * 访客姓名
     */
    @Schema(description = "访客姓名", example = "张三", required = true)
    @NotBlank(message = "访客姓名不能为空")
    @Pattern(regexp = "^[\u4e00-\u9fa5a-zA-Z\\s]{2,20}$", message = "访客姓名长度2-20位，只能包含中文、英文和空格")
    private String visitorName;

    /**
     * 访客手机号
     */
    @Schema(description = "访客手机号", example = "13800138000", required = true)
    @NotBlank(message = "访客手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String visitorPhone;

    /**
     * 访客邮箱
     */
    @Schema(description = "访客邮箱", example = "zhangsan@example.com")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "邮箱格式不正确")
    private String visitorEmail;

    /**
     * 访客单位
     */
    @Schema(description = "访客单位", example = "ABC科技有限公司")
    private String visitorCompany;

    /**
     * 访客身份证号
     */
    @Schema(description = "访客身份证号", example = "110101199001011234")
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]$", message = "身份证号格式不正确")
    private String visitorIdCard;

    /**
     * 访客照片URL
     */
    @Schema(description = "访客照片URL", example = "/upload/visitor/photo/20251125/001.jpg")
    private String visitorPhoto;

    /**
     * 来访事由
     */
    @Schema(description = "来访事由", example = "商务洽谈", required = true)
    @NotBlank(message = "来访事由不能为空")
    private String visitPurpose;

    /**
     * 来访日期
     */
    @Schema(description = "来访日期", example = "2025-11-25", required = true)
    @NotNull(message = "来访日期不能为空")
    
    private LocalDate visitDate;

    /**
     * 来访开始时间
     */
    @Schema(description = "来访开始时间", example = "09:00", required = true)
    @NotNull(message = "来访开始时间不能为空")
    
    private LocalTime visitStartTime;

    /**
     * 来访结束时间
     */
    @Schema(description = "来访结束时间", example = "18:00", required = true)
    @NotNull(message = "来访结束时间不能为空")
    
    private LocalTime visitEndTime;

    /**
     * 访问区域ID列表
     */
    @Schema(description = "访问区域ID列表(JSON格式)", example = "[1,2,3]")
    private String visitAreaIds;

    /**
     * 接待人ID
     */
    @Schema(description = "接待人ID", example = "1001", required = true)
    @NotNull(message = "接待人ID不能为空")
    private Long hostUserId;

    /**
     * 接待人姓名
     */
    @Schema(description = "接待人姓名", example = "李四", required = true)
    @NotBlank(message = "接待人姓名不能为空")
    private String hostUserName;

    /**
     * 接待人部门
     */
    @Schema(description = "接待人部门", example = "技术部")
    private String hostDepartment;

    /**
     * 最大访问次数
     */
    @Schema(description = "最大访问次数", example = "1")
    private Integer maxAccessCount;

    /**
     * 备注信息
     */
    @Schema(description = "备注信息", example = "需要在前台登记")
    private String remarks;

    /**
     * 获取访问区域ID列表
     */
    public java.util.List<Long> getVisitAreaIdList() {
        if (visitAreaIds == null || visitAreaIds.isEmpty()) {
            return null;
        }
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(visitAreaIds, java.util.List.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 设置访问区域ID列表
     */
    public void setVisitAreaIdList(java.util.List<Long> areaIds) {
        if (areaIds == null || areaIds.isEmpty()) {
            this.visitAreaIds = null;
            return;
        }
        try {
            this.visitAreaIds = new com.fasterxml.jackson.databind.ObjectMapper()
                    .writeValueAsString(areaIds);
        } catch (Exception e) {
            this.visitAreaIds = null;
        }
    }

    /**
     * 验证时间范围
     */
    public boolean isTimeRangeValid() {
        if (visitStartTime == null || visitEndTime == null) {
            return false;
        }
        return !visitStartTime.isAfter(visitEndTime);
    }

    /**
     * 验证访问日期不能是过去
     */
    public boolean isVisitDateValid() {
        if (visitDate == null) {
            return false;
        }
        return !visitDate.isBefore(LocalDate.now());
    }
}