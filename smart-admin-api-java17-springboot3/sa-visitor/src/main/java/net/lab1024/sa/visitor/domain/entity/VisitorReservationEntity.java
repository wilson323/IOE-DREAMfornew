package net.lab1024.sa.visitor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 访客预约实体类
 * <p>
 * 严格遵循repowiki规范：
 * - 继承BaseEntity，使用审计字段
 * - 使用jakarta包名
 * - 完整的字段注解和验证
 * - 日期时间格式化
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_visitor_reservation")
@Schema(description = "访客预约实体")

public class VisitorReservationEntity extends net.lab1024.sa.base.common.entity.BaseEntity {

    /**
     * 预约ID
     */
    @TableId(value = "reservation_id", type = IdType.AUTO)
    @Schema(description = "预约ID", example = "1")
    private Long reservationId;

    /**
     * 预约编号
     */
    @TableField("reservation_code")
    @NotBlank(message = "预约编号不能为空")
    @Schema(description = "预约编号", example = "VIS202511250001")
    private String reservationCode;

    /**
     * 访客姓名
     */
    @TableField("visitor_name")
    @NotBlank(message = "访客姓名不能为空")
    @Schema(description = "访客姓名", example = "张三")
    private String visitorName;

    /**
     * 访客手机号
     */
    @TableField("visitor_phone")
    @NotBlank(message = "访客手机号不能为空")
    @Schema(description = "访客手机号", example = "13800138000")
    private String visitorPhone;

    /**
     * 访客邮箱
     */
    @TableField("visitor_email")
    @Schema(description = "访客邮箱", example = "zhangsan@example.com")
    private String visitorEmail;

    /**
     * 访客单位
     */
    @TableField("visitor_company")
    @Schema(description = "访客单位", example = "ABC科技有限公司")
    private String visitorCompany;

    /**
     * 访客身份证号
     */
    @TableField("visitor_id_card")
    @Schema(description = "访客身份证号", example = "110101199001011234")
    private String visitorIdCard;

    /**
     * 访客照片URL
     */
    @TableField("visitor_photo")
    @Schema(description = "访客照片URL", example = "/upload/visitor/photo/20251125/001.jpg")
    private String visitorPhoto;

    /**
     * 来访事由
     */
    @TableField("visit_purpose")
    @NotBlank(message = "来访事由不能为空")
    @Schema(description = "来访事由", example = "商务洽谈")
    private String visitPurpose;

    /**
     * 来访日期
     */
    @TableField("visit_date")
    @NotNull(message = "来访日期不能为空")
    
    @Schema(description = "来访日期", example = "2025-11-25")
    private LocalDate visitDate;

    /**
     * 来访开始时间
     */
    @TableField("visit_start_time")
    @NotNull(message = "来访开始时间不能为空")
    
    @Schema(description = "来访开始时间", example = "09:00")
    private LocalTime visitStartTime;

    /**
     * 来访结束时间
     */
    @TableField("visit_end_time")
    @NotNull(message = "来访结束时间不能为空")
    
    @Schema(description = "来访结束时间", example = "18:00")
    private LocalTime visitEndTime;

    /**
     * 访问区域ID列表
     */
    @TableField("visit_area_ids")
    @Schema(description = "访问区域ID列表(JSON格式)", example = "[1,2,3]")
    private String visitAreaIds;

    /**
     * 接待人ID
     */
    @TableField("host_user_id")
    @NotNull(message = "接待人ID不能为空")
    @Schema(description = "接待人ID", example = "1001")
    private Long hostUserId;

    /**
     * 接待人姓名
     */
    @TableField("host_user_name")
    @NotBlank(message = "接待人姓名不能为空")
    @Schema(description = "接待人姓名", example = "李四")
    private String hostUserName;

    /**
     * 接待人部门
     */
    @TableField("host_department")
    @Schema(description = "接待人部门", example = "技术部")
    private String hostDepartment;

    /**
     * 审批状态: 0-待审批, 1-已通过, 2-已拒绝
     */
    @TableField("approval_status")
    @Schema(description = "审批状态", example = "0")
    private Integer approvalStatus;

    /**
     * 审批人ID
     */
    @TableField("approval_user_id")
    @Schema(description = "审批人ID", example = "2001")
    private Long approvalUserId;

    /**
     * 审批人姓名
     */
    @TableField("approval_user_name")
    @Schema(description = "审批人姓名", example = "王五")
    private String approvalUserName;

    /**
     * 审批时间
     */
    @TableField("approval_time")
    
    @Schema(description = "审批时间", example = "2025-11-25 10:30:00")
    private LocalDateTime approvalTime;

    /**
     * 审批意见
     */
    @TableField("approval_comment")
    @Schema(description = "审批意见", example = "同意访问")
    private String approvalComment;

    /**
     * 二维码内容
     */
    @TableField("qr_code")
    @Schema(description = "二维码内容", example = "QR_VIS_202511250001")
    private String qrCode;

    /**
     * 二维码过期时间
     */
    @TableField("qr_code_expire_time")
    
    @Schema(description = "二维码过期时间", example = "2025-11-25 18:00:00")
    private LocalDateTime qrCodeExpireTime;

    /**
     * 访问状态: 0-未开始, 1-进行中, 2-已完成, 3-已过期
     */
    @TableField("access_status")
    @Schema(description = "访问状态", example = "0")
    private Integer accessStatus;

    /**
     * 访问次数
     */
    @TableField("access_count")
    @Schema(description = "访问次数", example = "0")
    private Integer accessCount;

    /**
     * 最大访问次数
     */
    @TableField("max_access_count")
    @Schema(description = "最大访问次数", example = "1")
    private Integer maxAccessCount;

    /**
     * 备注信息
     */
    @TableField("remarks")
    @Schema(description = "备注信息", example = "需要在前台登记")
    private String remarks;

    /**
     * 获取访问区域ID列表
     */
    public List<Long> getVisitAreaIdList() {
        if (visitAreaIds == null || visitAreaIds.isEmpty()) {
            return null;
        }
        try {
            // 假设JSON格式为: [1,2,3]
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(visitAreaIds, List.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 设置访问区域ID列表
     */
    public void setVisitAreaIdList(List<Long> areaIds) {
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
     * 是否已审批通过
     */
    public boolean isApproved() {
        return Integer.valueOf(1).equals(approvalStatus);
    }

    /**
     * 是否已拒绝
     */
    public boolean isRejected() {
        return Integer.valueOf(2).equals(approvalStatus);
    }

    /**
     * 是否待审批
     */
    public boolean isPendingApproval() {
        return Integer.valueOf(0).equals(approvalStatus);
    }

    /**
     * 是否已过期
     */
    public boolean isExpired() {
        return Integer.valueOf(3).equals(accessStatus);
    }

    /**
     * 是否已开始访问
     */
    public boolean isAccessStarted() {
        return Integer.valueOf(1).equals(accessStatus);
    }

    /**
     * 是否已完成访问
     */
    public boolean isAccessCompleted() {
        return Integer.valueOf(2).equals(accessStatus);
    }

    /**
     * 检查二维码是否有效
     */
    public boolean isQrCodeValid() {
        if (qrCode == null || qrCode.isEmpty()) {
            return false;
        }
        if (qrCodeExpireTime == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(qrCodeExpireTime);
    }

    /**
     * 检查是否还可以访问
     */
    public boolean canAccess() {
        if (!isApproved()) {
            return false;
        }
        if (maxAccessCount != null && accessCount >= maxAccessCount) {
            return false;
        }
        return !isExpired();
    }
}