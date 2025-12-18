package net.lab1024.sa.access.controller;

import io.micrometer.observation.annotation.Observed;
import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.access.entity.AccessPermissionApplyEntity;
import net.lab1024.sa.access.domain.form.AccessPermissionApplyForm;
import net.lab1024.sa.access.service.AccessEmergencyPermissionService;

/**
 * 绱ф€ユ潈闄愮敵璇锋帶鍒跺櫒
 * <p>
 * 鎻愪緵绱ф€ユ潈闄愮敵璇风浉鍏矨PI鎺ュ彛
 * 鐗圭偣锛? * - 绠€鍖栧鎵规祦绋嬶紙蹇€熷鎵癸級
 * - 涓存椂鏉冮檺鐢熸晥
 * - 鑷姩杩囨湡鏈哄埗锛?4灏忔椂鍚庤嚜鍔ㄥけ鏁堬級
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛? * - Controller灞傝礋璐ｆ帴鏀惰姹傘€佸弬鏁伴獙璇併€佽繑鍥炲搷搴? * - 浣跨敤@Resource娉ㄥ叆Service
 * - 浣跨敤@Valid杩涜鍙傛暟楠岃瘉
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/access/emergency-permission")
@Tag(name = "绱ф€ユ潈闄愮敵璇风鐞?)
public class AccessEmergencyPermissionController {

    @Resource
    private AccessEmergencyPermissionService accessEmergencyPermissionService;

    /**
     * 鎻愪氦绱ф€ユ潈闄愮敵璇?     * <p>
     * 绱ф€ユ潈闄愮敵璇风壒鐐癸細
     * - 绠€鍖栧鎵规祦绋嬶紝鍙渶閮ㄩ棬缁忕悊蹇€熷鎵?     * - 瀹℃壒閫氳繃鍚庣珛鍗崇敓鏁?     * - 鏉冮檺鏈夋晥鏈?4灏忔椂锛堣嚜鍔ㄨ繃鏈燂級
     * </p>
     *
     * @param form 绱ф€ユ潈闄愮敵璇疯〃鍗?     * @return 鏉冮檺鐢宠瀹炰綋
     */
    @Observed(name = "accessEmergencyPermission.submitEmergencyPermissionApply", contextualName = "access-emergency-permission-submit")
    @PostMapping("/submit")
    @Operation(summary = "鎻愪氦绱ф€ユ潈闄愮敵璇?, description = "鎻愪氦绱ф€ユ潈闄愮敵璇峰苟鍚姩蹇€熷鎵规祦绋嬶紝鏉冮檺鏈夋晥鏈?4灏忔椂")
    public ResponseDTO<AccessPermissionApplyEntity> submitEmergencyPermissionApply(
            @Valid @RequestBody AccessPermissionApplyForm form) {
        log.info("[绱ф€ユ潈闄愮敵璇穄 鎺ユ敹绱ф€ユ潈闄愮敵璇疯姹傦紝applicantId={}, areaId={}, reason={}",
                form.getApplicantId(), form.getAreaId(), form.getApplyReason());

        // 楠岃瘉鐢宠绫诲瀷
        if (form.getApplyType() != null && !"EMERGENCY".equals(form.getApplyType())) {
            return ResponseDTO.error("鐢宠绫诲瀷蹇呴』鏄疎MERGENCY");
        }

        // 寮哄埗璁剧疆涓虹揣鎬ユ潈闄愮被鍨?        form.setApplyType("EMERGENCY");

        AccessPermissionApplyEntity entity = accessEmergencyPermissionService.submitEmergencyPermissionApply(form);
        return ResponseDTO.ok(entity);
    }

    /**
     * 鏇存柊绱ф€ユ潈闄愮敵璇风姸鎬侊紙渚涘鎵圭粨鏋滅洃鍚櫒璋冪敤锛?     *
     * @param applyNo 鐢宠缂栧彿
     * @param requestParams 璇锋眰鍙傛暟锛堝寘鍚玸tatus鍜宎pprovalComment锛?     * @return 鎿嶄綔缁撴灉
     */
    @Observed(name = "accessEmergencyPermission.updateEmergencyPermissionStatus", contextualName = "access-emergency-permission-update-status")
    @PutMapping("/{applyNo}/status")
    @Operation(summary = "鏇存柊绱ф€ユ潈闄愮敵璇风姸鎬?, description = "鐢卞鎵圭粨鏋滅洃鍚櫒璋冪敤锛屾洿鏂扮揣鎬ユ潈闄愮敵璇风姸鎬?)
    public ResponseDTO<Void> updateEmergencyPermissionStatus(
            @PathVariable String applyNo,
            @RequestBody Map<String, Object> requestParams) {
        log.info("[绱ф€ユ潈闄愮敵璇穄 鎺ユ敹鐘舵€佹洿鏂拌姹傦紝applyNo={}", applyNo);
        String status = (String) requestParams.get("status");
        String approvalComment = (String) requestParams.get("approvalComment");
        accessEmergencyPermissionService.updateEmergencyPermissionStatus(applyNo, status, approvalComment);
        return ResponseDTO.ok();
    }

    /**
     * 妫€鏌ュ苟鍥炴敹杩囨湡鏉冮檺
     * <p>
     * 绱ф€ユ潈闄愭湁鏁堟湡24灏忔椂锛岃繃鏈熷悗鑷姩鍥炴敹
     * </p>
     *
     * @param applyNo 鐢宠缂栧彿
     * @return 鏄惁宸茶繃鏈熷苟鍥炴敹
     */
    @Observed(name = "accessEmergencyPermission.checkAndRevokeExpiredPermission", contextualName = "access-emergency-permission-revoke-expired")
    @PostMapping("/{applyNo}/revoke-expired")
    @Operation(summary = "妫€鏌ュ苟鍥炴敹杩囨湡鏉冮檺", description = "妫€鏌ョ揣鎬ユ潈闄愭槸鍚﹀凡杩囨湡锛屽鏋滆繃鏈熷垯鑷姩鍥炴敹")
    public ResponseDTO<Boolean> checkAndRevokeExpiredPermission(@PathVariable String applyNo) {
        log.info("[绱ф€ユ潈闄愮敵璇穄 妫€鏌ヨ繃鏈熸潈闄愯姹傦紝applyNo={}", applyNo);
        boolean revoked = accessEmergencyPermissionService.checkAndRevokeExpiredPermission(applyNo);
        return ResponseDTO.ok(revoked);
    }
}


