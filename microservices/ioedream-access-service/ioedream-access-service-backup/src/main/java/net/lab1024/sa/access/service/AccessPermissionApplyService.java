package net.lab1024.sa.access.service;

import net.lab1024.sa.common.access.entity.AccessPermissionApplyEntity;
import net.lab1024.sa.access.domain.form.AccessPermissionApplyForm;

/**
 * 闂ㄧ鏉冮檺鐢宠鏈嶅姟鎺ュ彛
 * <p>
 * 鎻愪緵闂ㄧ鏉冮檺鐢宠鐩稿叧涓氬姟鍔熻兘
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛? * - Service鎺ュ彛瀹氫箟鍦ㄤ笟鍔℃湇鍔℃ā鍧椾腑
 * - 鏂规硶杩斿洖涓氬姟瀵硅薄锛屼笉杩斿洖ResponseDTO
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface AccessPermissionApplyService {

    /**
     * 鎻愪氦鏉冮檺鐢宠锛堝惎鍔ㄥ鎵规祦绋嬶級
     *
     * @param form 鏉冮檺鐢宠琛ㄥ崟
     * @return 鏉冮檺鐢宠瀹炰綋锛堝寘鍚玾orkflowInstanceId锛?     */
    AccessPermissionApplyEntity submitPermissionApply(AccessPermissionApplyForm form);

    /**
     * 鏇存柊鏉冮檺鐢宠鐘舵€侊紙鐢卞鎵圭粨鏋滅洃鍚櫒璋冪敤锛?     *
     * @param applyNo 鐢宠缂栧彿
     * @param status 瀹℃壒鐘舵€?     * @param approvalComment 瀹℃壒鎰忚
     */
    void updatePermissionApplyStatus(String applyNo, String status, String approvalComment);
}


