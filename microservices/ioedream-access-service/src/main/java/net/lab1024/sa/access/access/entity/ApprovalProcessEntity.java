package net.lab1024.sa.access.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 瀹℃壒娴佺▼瀹炰綋绫? * <p>
 * 鐢ㄤ簬绠＄悊闂ㄧ瀹℃壒娴佺▼
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛? * - 缁ф壙BaseEntity鑾峰彇瀹¤瀛楁
 * - 浣跨敤@TableName鎸囧畾鏁版嵁搴撹〃鍚? * - 瀛楁鏁版帶鍒跺湪30涓互鍐? * - 琛屾暟鎺у埗鍦?00琛屼互鍐? * </p>
 * <p>
 * 涓氬姟鍦烘櫙锛? * - 闂ㄧ瀹℃壒娴佺▼绠＄悊
 * - 瀹℃壒鐘舵€佽窡韪? * - 瀹℃壒鍘嗗彶璁板綍
 * </p>
 * <p>
 * 鏁版嵁搴撹〃锛歛ccess_approval_process锛堟牴鎹瓺AO涓殑SQL鎺ㄦ柇锛? * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("access_approval_process")
@SuppressWarnings("PMD.ShortVariable")
public class ApprovalProcessEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 娴佺▼ID锛堜富閿級
     * <p>
     * 缁熶竴浣跨敤id浣滀负涓婚敭瀛楁鍚嶏紝閫氳繃@TableId鐨剉alue灞炴€ф槧灏勫埌鏁版嵁搴撳垪process_id
     * 绗﹀悎瀹炰綋绫讳富閿懡鍚嶈鑼冿細缁熶竴浣跨敤id锛岄伩鍏嶆柟娉曞紩鐢ㄩ敊璇?     * </p>
     */
    @TableId(value = "process_id", type = IdType.AUTO)
    private Long id;

    /**
     * 娴佺▼缂栧彿
     */
    @TableField("process_no")
    private String processNo;

    /**
     * 鐢宠浜篒D
     */
    @TableField("applicant_id")
    private Long applicantId;

    /**
     * 鐢宠浜哄鍚?     */
    @TableField("applicant_name")
    private String applicantName;

    /**
     * 鐢宠绫诲瀷
     */
    @TableField("apply_type")
    private String applyType;

    /**
     * 鐢宠鍐呭
     */
    @TableField("apply_content")
    private String applyContent;

    /**
     * 褰撳墠姝ラ
     */
    @TableField("current_step")
    private Integer currentStep;

    /**
     * 鐘舵€侊紙PENDING-寰呭鎵癸紝IN_PROGRESS-瀹℃壒涓紝APPROVED-宸查€氳繃锛孯EJECTED-宸叉嫆缁濓級
     */
    @TableField("status")
    private String status;

    /**
     * 浼樺厛绾?     */
    @TableField("priority")
    private Integer priority;

    /**
     * 瀹℃壒鎰忚
     */
    @TableField("approval_comment")
    private String approvalComment;

    /**
     * 鏈€缁堝鎵逛汉ID
     */
    @TableField("final_approver_id")
    private Long finalApproverId;

    /**
     * 鏈€缁堝鎵逛汉濮撳悕
     */
    @TableField("final_approver_name")
    private String finalApproverName;

    /**
     * 瀹℃壒鏃堕棿
     */
    @TableField("approval_time")
    private LocalDateTime approvalTime;

    /**
     * 鍒涘缓鏃堕棿锛坈reated_time锛?     */
    @TableField("created_time")
    private LocalDateTime createdTime;

    /**
     * 杩囨湡鏃堕棿
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;
}


