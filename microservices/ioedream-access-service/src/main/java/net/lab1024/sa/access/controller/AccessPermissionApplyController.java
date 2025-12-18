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
import net.lab1024.sa.access.service.AccessPermissionApplyService;

/**
 * 闂ㄧ鏉冮檺鐢宠鎺у埗鍣? * <p>
 * 鎻愪緵闂ㄧ鏉冮檺鐢宠鐩稿叧API鎺ュ彛
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
@RequestMapping("/api/v1/access/permission/apply")
@Tag(name = "闂ㄧ鏉冮檺鐢宠绠＄悊")
public class AccessPermissionApplyController {

    @Resource
    private AccessPermissionApplyService accessPermissionApplyService;

    /**
     * 鎻愪氦鏉冮檺鐢宠
     * <p>
     * 鍔熻兘璇存槑锛?     * 1. 鍒涘缓鏉冮檺鐢宠璁板綍
     * 2. 鍚姩宸ヤ綔娴佸鎵规祦绋?     * 3. 鍏宠仈瀹℃壒娴佺▼瀹炰緥ID
     * </p>
     *
     * @param form 鏉冮檺鐢宠琛ㄥ崟
     * @return 鏉冮檺鐢宠瀹炰綋锛堝寘鍚玾orkflowInstanceId锛?     */
    @Observed(name = "accessPermissionApply.submitPermissionApply", contextualName = "access-permission-apply-submit")
    @PostMapping("/submit")
    @Operation(
            summary = "鎻愪氦鏉冮檺鐢宠",
            description = "鎻愪氦鏉冮檺鐢宠骞跺惎鍔ㄥ鎵规祦绋嬶紝鏀寔涓存椂鏉冮檺鍜屾案涔呮潈闄愮敵璇?,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "鏉冮檺鐢宠琛ㄥ崟锛屽寘鍚敵璇蜂汉ID銆佸尯鍩烮D銆佺敵璇风被鍨嬨€佺敵璇峰師鍥犮€佹湁鏁堟湡绛?,
                    required = true
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "鎻愪氦鎴愬姛",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = AccessPermissionApplyEntity.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "鍙傛暟閿欒"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "500",
                            description = "宸ヤ綔娴佸惎鍔ㄥけ璐?
                    )
            }
    )
    public ResponseDTO<AccessPermissionApplyEntity> submitPermissionApply(
            @Valid @RequestBody AccessPermissionApplyForm form) {
        log.info("[鏉冮檺鐢宠] 鎺ユ敹鏉冮檺鐢宠璇锋眰锛宎pplicantId={}, areaId={}, applyType={}",
                form.getApplicantId(), form.getAreaId(), form.getApplyType());
        AccessPermissionApplyEntity entity = accessPermissionApplyService.submitPermissionApply(form);
        return ResponseDTO.ok(entity);
    }

    /**
     * 鏇存柊鏉冮檺鐢宠鐘舵€侊紙渚涘鎵圭粨鏋滅洃鍚櫒璋冪敤锛?     * <p>
     * 鍔熻兘璇存槑锛?     * 1. 鏇存柊鏉冮檺鐢宠鐘舵€侊紙PENDING/APPROVED/REJECTED锛?     * 2. 濡傛灉瀹℃壒閫氳繃锛屾墽琛屾潈闄愭巿浜堥€昏緫
     * 3. 璁板綍瀹℃壒鎰忚鍜屽鎵规椂闂?     * </p>
     *
     * @param applyNo 鐢宠缂栧彿
     * @param requestParams 璇锋眰鍙傛暟锛堝寘鍚玸tatus鍜宎pprovalComment锛?     * @return 鎿嶄綔缁撴灉
     */
    @Observed(name = "accessPermissionApply.updatePermissionApplyStatus", contextualName = "access-permission-apply-update-status")
    @PutMapping("/{applyNo}/status")
    @Operation(
            summary = "鏇存柊鏉冮檺鐢宠鐘舵€?,
            description = "鐢卞鎵圭粨鏋滅洃鍚櫒璋冪敤锛屾洿鏂版潈闄愮敵璇风姸鎬侊紝瀹℃壒閫氳繃鏃惰嚜鍔ㄦ巿浜堟潈闄?,
            parameters = {
                    @io.swagger.v3.oas.annotations.Parameter(
                            name = "applyNo",
                            description = "鐢宠缂栧彿",
                            required = true,
                            example = "APPLY20250130001"
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "鐘舵€佹洿鏂板弬鏁帮紝鍖呭惈status锛堝鎵圭姸鎬侊級鍜宎pprovalComment锛堝鎵规剰瑙侊級",
                    required = true
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "鏇存柊鎴愬姛"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "鐢宠涓嶅瓨鍦?
                    )
            }
    )
    public ResponseDTO<Void> updatePermissionApplyStatus(
            @PathVariable String applyNo,
            @RequestBody Map<String, Object> requestParams) {
        log.info("[鏉冮檺鐢宠] 鎺ユ敹鐘舵€佹洿鏂拌姹傦紝applyNo={}", applyNo);
        String status = (String) requestParams.get("status");
        String approvalComment = (String) requestParams.get("approvalComment");
        accessPermissionApplyService.updatePermissionApplyStatus(applyNo, status, approvalComment);
        return ResponseDTO.ok();
    }
}


