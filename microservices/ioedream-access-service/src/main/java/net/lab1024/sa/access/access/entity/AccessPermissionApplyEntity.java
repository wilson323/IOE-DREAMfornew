package net.lab1024.sa.common.access.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 闂ㄧ鏉冮檺鐢宠瀹炰綋绫? * <p>
 * 鐢ㄤ簬璁板綍闂ㄧ鏉冮檺鐢宠淇℃伅锛屾敮鎸佸伐浣滄祦瀹℃壒
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛? * - 缁ф壙BaseEntity鑾峰彇瀹¤瀛楁
 * - 浣跨敤@TableName鎸囧畾鏁版嵁搴撹〃鍚? * - 瀛楁鏁版帶鍒跺湪30涓互鍐? * - 琛屾暟鎺у埗鍦?00琛屼互鍐? * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("access_permission_apply")
public class AccessPermissionApplyEntity extends BaseEntity {

    /**
     * 鏉冮檺鐢宠ID锛堜富閿級
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 鐢宠缂栧彿锛堜笟鍔ey锛屽敮涓€锛?     */
    private String applyNo;

    /**
     * 鐢宠浜篒D
     */
    private Long applicantId;

    /**
     * 鐢宠浜哄鍚?     */
    private String applicantName;

    /**
     * 鍖哄煙ID
     */
    private Long areaId;

    /**
     * 鍖哄煙鍚嶇О
     */
    private String areaName;

    /**
     * 鐢宠绫诲瀷
     * <p>
     * NORMAL-鏅€氭潈闄愮敵璇?     * EMERGENCY-绱ф€ユ潈闄愮敵璇?     * </p>
     */
    private String applyType;

    /**
     * 鐢宠鍘熷洜
     */
    private String applyReason;

    /**
     * 鐢宠寮€濮嬫椂闂?     */
    private LocalDateTime startTime;

    /**
     * 鐢宠缁撴潫鏃堕棿
     */
    private LocalDateTime endTime;

    /**
     * 鐢宠鐘舵€?     * <p>
     * PENDING-寰呭鎵?     * APPROVED-宸查€氳繃
     * REJECTED-宸查┏鍥?     * CANCELLED-宸插彇娑?     * </p>
     */
    private String status;

    /**
     * 瀹℃壒鎰忚
     */
    private String approvalComment;

    /**
     * 瀹℃壒鏃堕棿
     */
    private LocalDateTime approvalTime;

    /**
     * 澶囨敞
     */
    private String remark;

    /**
     * 宸ヤ綔娴佸疄渚婭D
     * <p>
     * 鍏宠仈OA宸ヤ綔娴佹ā鍧楃殑娴佺▼瀹炰緥ID
     * 鐢ㄤ簬鏌ヨ瀹℃壒鐘舵€併€佸鎵瑰巻鍙茬瓑
     * </p>
     */
    @TableField("workflow_instance_id")
    private Long workflowInstanceId;
}


