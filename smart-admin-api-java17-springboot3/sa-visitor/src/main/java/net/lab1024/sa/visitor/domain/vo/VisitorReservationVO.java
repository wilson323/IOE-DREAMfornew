package net.lab1024.sa.visitor.domain.vo;



import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 访客预约展示对象
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

@Schema(description = "访客预约展示对象")
public class VisitorReservationVO {

    /**
     * 预约ID
     */
    @Schema(description = "预约ID", example = "1")
    private Long reservationId;

    /**
     * 预约编号
     */
    @Schema(description = "预约编号", example = "VIS202511250001")
    private String reservationCode;

    /**
     * 访客姓名
     */
    @Schema(description = "访客姓名", example = "张三")
    private String visitorName;

    /**
     * 访客手机号
     */
    @Schema(description = "访客手机号", example = "13800138000")
    private String visitorPhone;

    /**
     * 访客邮箱
     */
    @Schema(description = "访客邮箱", example = "zhangsan@example.com")
    private String visitorEmail;

    /**
     * 访客单位
     */
    @Schema(description = "访客单位", example = "ABC科技有限公司")
    private String visitorCompany;

    /**
     * 访客身份证号（脱敏显示）
     */
    @Schema(description = "访客身份证号", example = "110101********1234")
    private String visitorIdCardMasked;

    /**
     * 访客照片URL
     */
    @Schema(description = "访客照片URL", example = "/upload/visitor/photo/20251125/001.jpg")
    private String visitorPhoto;

    /**
     * 来访事由
     */
    @Schema(description = "来访事由", example = "商务洽谈")
    private String visitPurpose;

    /**
     * 来访日期
     */
    @Schema(description = "来访日期", example = "2025-11-25")
    
    private LocalDate visitDate;

    /**
     * 来访开始时间
     */
    @Schema(description = "来访开始时间", example = "09:00")
    
    private LocalTime visitStartTime;

    /**
     * 来访结束时间
     */
    @Schema(description = "来访结束时间", example = "18:00")
    
    private LocalTime visitEndTime;

    /**
     * 访问区域列表
     */
    @Schema(description = "访问区域列表")
    private List<AreaInfo> visitAreaList;

    /**
     * 接待人ID
     */
    @Schema(description = "接待人ID", example = "1001")
    private Long hostUserId;

    /**
     * 接待人姓名
     */
    @Schema(description = "接待人姓名", example = "李四")
    private String hostUserName;

    /**
     * 接待人部门
     */
    @Schema(description = "接待人部门", example = "技术部")
    private String hostDepartment;

    /**
     * 审批状态
     */
    @Schema(description = "审批状态", example = "0")
    private Integer approvalStatus;

    /**
     * 审批状态描述
     */
    @Schema(description = "审批状态描述", example = "待审批")
    private String approvalStatusDesc;

    /**
     * 审批人姓名
     */
    @Schema(description = "审批人姓名", example = "王五")
    private String approvalUserName;

    /**
     * 审批时间
     */
    @Schema(description = "审批时间", example = "2025-11-25 10:30:00")
    
    private LocalDateTime approvalTime;

    /**
     * 审批意见
     */
    @Schema(description = "审批意见", example = "同意访问")
    private String approvalComment;

    /**
     * 二维码内容
     */
    @Schema(description = "二维码内容", example = "QR_VIS_202511250001")
    private String qrCode;

    /**
     * 二维码过期时间
     */
    @Schema(description = "二维码过期时间", example = "2025-11-25 18:00:00")
    
    private LocalDateTime qrCodeExpireTime;

    /**
     * 二维码状态
     */
    @Schema(description = "二维码状态", example = "有效")
    private String qrCodeStatus;

    /**
     * 访问状态
     */
    @Schema(description = "访问状态", example = "0")
    private Integer accessStatus;

    /**
     * 访问状态描述
     */
    @Schema(description = "访问状态描述", example = "未开始")
    private String accessStatusDesc;

    /**
     * 访问次数
     */
    @Schema(description = "访问次数", example = "0")
    private Integer accessCount;

    /**
     * 最大访问次数
     */
    @Schema(description = "最大访问次数", example = "1")
    private Integer maxAccessCount;

    /**
     * 剩余访问次数
     */
    @Schema(description = "剩余访问次数", example = "1")
    private Integer remainingAccessCount;

    /**
     * 备注信息
     */
    @Schema(description = "备注信息", example = "需要在前台登记")
    private String remarks;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-11-25 08:00:00")
    
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2025-11-25 10:30:00")
    
    private LocalDateTime updateTime;

    /**
     * 区域信息
     */
    @Data
    @Schema(description = "区域信息")
    public static class AreaInfo {
        @Schema(description = "区域ID", example = "1")
        private Long areaId;

        @Schema(description = "区域名称", example = "办公楼A栋")
        private String areaName;

        @Schema(description = "区域类型", example = "办公区")
        private String areaType;
    }

    /**
     * 审批状态枚举
     */
    public enum ApprovalStatus {
        PENDING(0, "待审批"),
        APPROVED(1, "已通过"),
        REJECTED(2, "已拒绝");

        private final Integer code;
        private final String description;

        ApprovalStatus(Integer code, String description) {
            this.code = code;
            this.description = description;
        }

        public Integer getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static String getDescriptionByCode(Integer code) {
            for (ApprovalStatus status : values()) {
                if (status.code.equals(code)) {
                    return status.description;
                }
            }
            return "未知";
        }
    }

    /**
     * 访问状态枚举
     */
    public enum AccessStatus {
        NOT_STARTED(0, "未开始"),
        IN_PROGRESS(1, "进行中"),
        COMPLETED(2, "已完成"),
        EXPIRED(3, "已过期");

        private final Integer code;
        private final String description;

        AccessStatus(Integer code, String description) {
            this.code = code;
            this.description = description;
        }

        public Integer getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static String getDescriptionByCode(Integer code) {
            for (AccessStatus status : values()) {
                if (status.code.equals(code)) {
                    return status.description;
                }
            }
            return "未知";
        }
    }

    /**
     * 获取审批状态描述
     */
    public String getApprovalStatusDesc() {
        return ApprovalStatus.getDescriptionByCode(approvalStatus);
    }

    /**
     * 获取访问状态描述
     */
    public String getAccessStatusDesc() {
        return AccessStatus.getDescriptionByCode(accessStatus);
    }

    /**
     * 获取二维码状态
     */
    public String getQrCodeStatus() {
        if (qrCode == null || qrCode.isEmpty()) {
            return "未生成";
        }
        if (qrCodeExpireTime == null) {
            return "状态异常";
        }
        if (LocalDateTime.now().isAfter(qrCodeExpireTime)) {
            return "已过期";
        }
        return "有效";
    }

    /**
     * 获取剩余访问次数
     */
    public Integer getRemainingAccessCount() {
        if (maxAccessCount == null || maxAccessCount <= 0) {
            return null; // 无限制
        }
        if (accessCount == null) {
            accessCount = 0;
        }
        return Math.max(0, maxAccessCount - accessCount);
    }

    /**
     * 是否已审批通过
     */
    public boolean isApproved() {
        return ApprovalStatus.APPROVED.getCode().equals(approvalStatus);
    }

    /**
     * 是否已过期
     */
    public boolean isExpired() {
        return AccessStatus.EXPIRED.getCode().equals(accessStatus);
    }

    /**
     * 是否已完成访问
     */
    public boolean isCompleted() {
        return AccessStatus.COMPLETED.getCode().equals(accessStatus);
    }

    /**
     * 是否还能访问
     */
    public boolean canAccess() {
        if (!isApproved()) {
            return false;
        }
        if (isExpired() || isCompleted()) {
            return false;
        }
        Integer remaining = getRemainingAccessCount();
        return remaining == null || remaining > 0;
    }
}