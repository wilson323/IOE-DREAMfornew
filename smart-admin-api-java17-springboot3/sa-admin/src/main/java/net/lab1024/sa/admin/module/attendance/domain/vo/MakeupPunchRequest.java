package net.lab1024.sa.admin.module.attendance.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 补卡请求VO (Value Object)
 *
 * <p>用于员工提交补卡申请的数据传输对象，包含补卡的完整信息，
 * 如原考勤日期、补卡原因、证明材料等。</p>
 *
 * <p>严格遵循repowiki规范：</p>
 * <ul>
 *   <li>使用完整的验证注解确保数据完整性</li>
 *   <li>包含详细的Swagger文档注解</li>
 *   <li>使用JsonInclude忽略null值</li>
 *   <li>提供Builder模式支持灵活构造</li>
 * </ul>
 *
 * @author SmartAdmin Team
 * @since 2025-11-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "补卡请求VO")
public class MakeupPunchRequest {

    /**
     * 补卡申请ID
     *
     * <p>新建申请时为空，后端自动生成</p>
     */
    @Schema(description = "补卡申请ID", example = "12345")
    private Long requestId;

    /**
     * 员工ID
     *
     * <p>申请人员工ID，必须填写</p>
     */
    @NotNull(message = "员工ID不能为空")
    @Positive(message = "员工ID必须为正数")
    @Schema(description = "员工ID", example = "10086", required = true)
    private Long employeeId;

    /**
     * 员工姓名
     *
     * <p>申请人姓名，用于显示确认</p>
     */
    @NotBlank(message = "员工姓名不能为空")
    @Size(max = 50, message = "员工姓名长度不能超过50个字符")
    @Schema(description = "员工姓名", example = "张三", required = true)
    private String employeeName;

    /**
     * 员工工号
     *
     * <p>申请人工号，用于唯一标识员工</p>
     */
    @NotBlank(message = "员工工号不能为空")
    @Size(max = 20, message = "员工工号长度不能超过20个字符")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "员工工号只能包含字母和数字")
    @Schema(description = "员工工号", example = "EMP001", required = true)
    private String employeeNo;

    /**
     * 原考勤日期
     *
     * <p>需要补卡的原始考勤日期，不能是未来日期</p>
     */
    @NotNull(message = "原考勤日期不能为空")
    @PastOrPresent(message = "原考勤日期不能是未来日期")
    @Schema(description = "原考勤日期", example = "2025-11-27", required = true)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate originalDate;

    /**
     * 补卡类型
     *
     * <p>补卡类型：PUNCH_IN-上班补卡, PUNCH_OUT-下班补卡, BOTH-上下班都补卡</p>
     */
    @NotBlank(message = "补卡类型不能为空")
    @Pattern(regexp = "^(PUNCH_IN|PUNCH_OUT|BOTH)$", message = "补卡类型只能是PUNCH_IN、PUNCH_OUT或BOTH")
    @Schema(description = "补卡类型", example = "PUNCH_IN", allowableValues = {"PUNCH_IN", "PUNCH_OUT", "BOTH"}, required = true)
    private String type;

    /**
     * 补卡原因
     *
     * <p>申请补卡的详细原因说明</p>
     */
    @NotBlank(message = "补卡原因不能为空")
    @Size(min = 5, max = 500, message = "补卡原因长度必须在5-500个字符之间")
    @Schema(description = "补卡原因", example = "因设备故障导致无法正常打卡", required = true)
    private String reason;

    /**
     * 补卡原因分类
     *
     * <p>原因分类：EQUIPMENT_FAILURE-设备故障, FORGOT_PUNCH-忘记打卡,
     * SYSTEM_ERROR-系统错误, SPECIAL_REASON-特殊原因</p>
     */
    @NotBlank(message = "补卡原因分类不能为空")
    @Pattern(regexp = "^(EQUIPMENT_FAILURE|FORGOT_PUNCH|SYSTEM_ERROR|SPECIAL_REASON)$",
             message = "补卡原因分类无效")
    @Schema(description = "补卡原因分类", example = "EQUIPMENT_FAILURE",
            allowableValues = {"EQUIPMENT_FAILURE", "FORGOT_PUNCH", "SYSTEM_ERROR", "SPECIAL_REASON"},
            required = true)
    private String reasonCategory;

    /**
     * 申请状态
     *
     * <p>DRAFT-草稿, SUBMITTED-已提交, UNDER_REVIEW-审核中, APPROVED-已通过,
     * REJECTED-已拒绝, CANCELLED-已取消</p>
     */
    @Schema(description = "申请状态", example = "SUBMITTED",
            allowableValues = {"DRAFT", "SUBMITTED", "UNDER_REVIEW", "APPROVED", "REJECTED", "CANCELLED"})
    private String status;

    /**
     * 期望补卡时间
     *
     * <p>申请人期望记录的打卡时间</p>
     */
    @Schema(description = "期望补卡时间", example = "2025-11-27 09:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expectedPunchTime;

    /**
     * 申请时间
     *
     * <p>提交补卡申请的时间</p>
     */
    @Schema(description = "申请时间", example = "2025-11-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestTime;

    /**
     * 审批人ID
     *
     * <p>审批该申请的管理员ID</p>
     */
    @Schema(description = "审批人ID", example = "20001")
    private Long approverId;

    /**
     * 审批人姓名
     *
     * <p>审批该申请的管理员姓名</p>
     */
    @Schema(description = "审批人姓名", example = "李四")
    private String approverName;

    /**
     * 审批时间
     *
     * <p>审批时间</p>
     */
    @Schema(description = "审批时间", example = "2025-11-28 14:20:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvalTime;

    /**
     * 审批意见
     *
     * <p>审批人的意见和备注</p>
     */
    @Size(max = 200, message = "审批意见长度不能超过200个字符")
    @Schema(description = "审批意见", example = "同意补卡申请")
    private String approvalComment;

    /**
     * 证明材料文件列表
     *
     * <p>支持补卡申请的证明材料，如故障照片、系统错误截图等</p>
     */
    @Valid
    @Schema(description = "证明材料文件列表")
    private List<AttachmentFile> attachments;

    /**
     * 备注
     *
     * <p>其他补充说明信息</p>
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Schema(description = "备注", example = "如有问题请联系IT部门")
    private String remark;

    /**
     * 部门ID
     *
     * <p>申请人所属部门ID</p>
     */
    @Schema(description = "部门ID", example = "30001")
    private Long departmentId;

    /**
     * 部门名称
     *
     * <p>申请人所属部门名称</p>
     */
    @Schema(description = "部门名称", example = "技术部")
    private String departmentName;

    /**
     * 是否紧急申请
     *
     * <p>标记是否为紧急补卡申请</p>
     */
    @Schema(description = "是否紧急申请", example = "false")
    private Boolean urgent;

    /**
     * 创建时间
     *
     * <p>记录创建时间</p>
     */
    @Schema(description = "创建时间", example = "2025-11-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     *
     * <p>记录最后更新时间</p>
     */
    @Schema(description = "更新时间", example = "2025-11-28 14:20:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 附件文件内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "附件文件")
    public static class AttachmentFile {

        /**
         * 文件ID
         */
        @NotNull(message = "文件ID不能为空")
        @Schema(description = "文件ID", example = "FILE_001")
        private String fileId;

        /**
         * 文件名
         */
        @NotBlank(message = "文件名不能为空")
        @Schema(description = "文件名", example = "device_error_screenshot.jpg")
        private String fileName;

        /**
         * 文件大小（字节）
         */
        @Positive(message = "文件大小必须为正数")
        @Schema(description = "文件大小（字节）", example = "1024000")
        private Long fileSize;

        /**
         * 文件类型
         */
        @NotBlank(message = "文件类型不能为空")
        @Schema(description = "文件类型", example = "image/jpeg")
        private String fileType;

        /**
         * 文件URL
         */
        @NotBlank(message = "文件URL不能为空")
        @Schema(description = "文件URL", example = "/api/file/download/FILE_001")
        private String fileUrl;

        /**
         * 上传时间
         */
        @Schema(description = "上传时间", example = "2025-11-28 10:25:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime uploadTime;
    }

    /**
     * 验证补卡请求数据完整性
     *
     * @return 验证结果
     */
    public boolean isValid() {
        if (employeeId == null || employeeName == null || employeeNo == null) {
            return false;
        }
        if (originalDate == null || reason == null || reasonCategory == null) {
            return false;
        }
        if (type == null || !type.matches("^(PUNCH_IN|PUNCH_OUT|BOTH)$")) {
            return false;
        }
        if (reasonCategory == null || !reasonCategory.matches("^(EQUIPMENT_FAILURE|FORGOT_PUNCH|SYSTEM_ERROR|SPECIAL_REASON)$")) {
            return false;
        }
        return true;
    }

    /**
     * 获取补卡类型描述
     *
     * @return 中文描述
     */
    public String getTypeDescription() {
        switch (type) {
            case "PUNCH_IN":
                return "上班补卡";
            case "PUNCH_OUT":
                return "下班补卡";
            case "BOTH":
                return "上下班补卡";
            default:
                return "未知类型";
        }
    }

    /**
     * 获取状态描述
     *
     * @return 中文描述
     */
    public String getStatusDescription() {
        switch (status) {
            case "DRAFT":
                return "草稿";
            case "SUBMITTED":
                return "已提交";
            case "UNDER_REVIEW":
                return "审核中";
            case "APPROVED":
                return "已通过";
            case "REJECTED":
                return "已拒绝";
            case "CANCELLED":
                return "已取消";
            default:
                return "未知状态";
        }
    }

    /**
     * 获取原因分类描述
     *
     * @return 中文描述
     */
    public String getReasonCategoryDescription() {
        switch (reasonCategory) {
            case "EQUIPMENT_FAILURE":
                return "设备故障";
            case "FORGOT_PUNCH":
                return "忘记打卡";
            case "SYSTEM_ERROR":
                return "系统错误";
            case "SPECIAL_REASON":
                return "特殊原因";
            default:
                return "未知原因";
        }
    }
}