package net.lab1024.sa.admin.module.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 紧急联系人VO
 * 严格遵循repowiki规范：VO类用于数据传输
 *
 * @author SmartAdmin Team
 * @date 2025/11/21
 */




@Schema(description = "紧急联系人")
public class EmergencyContact {

    @Schema(description = "联系人ID")
    private Long contactId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "联系人姓名")
    private String contactName;

    @Schema(description = "联系人电话")
    private String contactPhone;

    @Schema(description = "联系人邮箱")
    private String contactEmail;

    @Schema(description = "关系：1-父母，2-配偶，3-子女，4-朋友")
    private Integer relationship;

    @Schema(description = "是否主要联系人")
    private Boolean isPrimary;

    // 手动添加的getter/setter方法 (Lombok失效备用)












    public Boolean isIsPrimary() {
        return isPrimary;
    }

}
