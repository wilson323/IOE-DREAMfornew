package net.lab1024.sa.access.service;

import net.lab1024.sa.common.access.entity.AccessPermissionApplyEntity;
import net.lab1024.sa.access.domain.form.AccessPermissionApplyForm;

/**
 * 绱ф€ユ潈闄愮敵璇锋湇鍔℃帴鍙? * <p>
 * 鎻愪緵绱ф€ユ潈闄愮敵璇风浉鍏充笟鍔″姛鑳? * 鐗圭偣锛? * - 绠€鍖栧鎵规祦绋嬶紙蹇€熷鎵癸級
 * - 涓存椂鏉冮檺鐢熸晥
 * - 鑷姩杩囨湡鏈哄埗锛?4灏忔椂鍚庤嚜鍔ㄥけ鏁堬級
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛? * - Service鎺ュ彛瀹氫箟鍦ㄤ笟鍔℃湇鍔℃ā鍧椾腑
 * - 鏂规硶杩斿洖涓氬姟瀵硅薄锛屼笉杩斿洖ResponseDTO
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface AccessEmergencyPermissionService {

    /**
     * 鎻愪氦绱ф€ユ潈闄愮敵璇凤紙鍚姩蹇€熷鎵规祦绋嬶級
     * <p>
     * 绱ф€ユ潈闄愮敵璇风壒鐐癸細
     * - 绠€鍖栧鎵规祦绋嬶紝鍙渶閮ㄩ棬缁忕悊蹇€熷鎵?     * - 瀹℃壒閫氳繃鍚庣珛鍗崇敓鏁?     * - 鏉冮檺鏈夋晥鏈?4灏忔椂锛堣嚜鍔ㄨ繃鏈燂級
     * </p>
     *
     * @param form 绱ф€ユ潈闄愮敵璇疯〃鍗?     * @return 鏉冮檺鐢宠瀹炰綋锛堝寘鍚玾orkflowInstanceId锛?     */
    AccessPermissionApplyEntity submitEmergencyPermissionApply(AccessPermissionApplyForm form);

    /**
     * 鏇存柊绱ф€ユ潈闄愮敵璇风姸鎬侊紙鐢卞鎵圭粨鏋滅洃鍚櫒璋冪敤锛?     *
     * @param applyNo 鐢宠缂栧彿
     * @param status 瀹℃壒鐘舵€?     * @param approvalComment 瀹℃壒鎰忚
     */
    void updateEmergencyPermissionStatus(String applyNo, String status, String approvalComment);

    /**
     * 妫€鏌ュ苟鍥炴敹杩囨湡鏉冮檺
     * <p>
     * 绱ф€ユ潈闄愭湁鏁堟湡24灏忔椂锛岃繃鏈熷悗鑷姩鍥炴敹
     * </p>
     *
     * @param applyNo 鐢宠缂栧彿
     * @return 鏄惁宸茶繃鏈熷苟鍥炴敹
     */
    boolean checkAndRevokeExpiredPermission(String applyNo);
}


