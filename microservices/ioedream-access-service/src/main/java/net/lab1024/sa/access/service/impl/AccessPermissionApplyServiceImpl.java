package net.lab1024.sa.access.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.dao.AccessPermissionApplyDao;
import net.lab1024.sa.common.access.entity.AccessPermissionApplyEntity;
import net.lab1024.sa.access.domain.form.AccessPermissionApplyForm;
import net.lab1024.sa.access.service.AccessPermissionApplyService;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.workflow.manager.WorkflowApprovalManager;

/**
 * 闂ㄧ鏉冮檺鐢宠鏈嶅姟瀹炵幇绫? * <p>
 * 瀹炵幇闂ㄧ鏉冮檺鐢宠鐩稿叧涓氬姟鍔熻兘
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛? * - 浣跨敤@Service娉ㄨВ鏍囪瘑鏈嶅姟绫? * - 浣跨敤@Transactional绠＄悊浜嬪姟
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AccessPermissionApplyServiceImpl implements AccessPermissionApplyService {

    @Resource
    private AccessPermissionApplyDao accessPermissionApplyDao;

    @Resource
    private WorkflowApprovalManager workflowApprovalManager;

    /**
     * 鎻愪氦鏉冮檺鐢宠锛堝惎鍔ㄥ鎵规祦绋嬶級
     * <p>
     * 瀹炵幇姝ラ锛?     * 1. 鍒涘缓鐢宠瀹炰綋骞朵繚瀛樺埌鏁版嵁搴?     * 2. 鍚姩宸ヤ綔娴佸鎵规祦绋?     * 3. 鏇存柊鐢宠瀹炰綋鐨勫伐浣滄祦瀹炰緥ID
     * </p>
     *
     * @param form 鏉冮檺鐢宠琛ㄥ崟
     * @return 鏉冮檺鐢宠瀹炰綋锛堝寘鍚玾orkflowInstanceId锛?     */
    @Override
    public AccessPermissionApplyEntity submitPermissionApply(AccessPermissionApplyForm form) {
        log.info("[鏉冮檺鐢宠] 鎻愪氦鏉冮檺鐢宠锛宎pplicantId={}", form.getApplicantId());

        // 1. 鍒涘缓鐢宠瀹炰綋
        AccessPermissionApplyEntity entity = new AccessPermissionApplyEntity();
        entity.setApplyNo("AP" + System.currentTimeMillis());
        entity.setApplicantId(form.getApplicantId());
        entity.setAreaId(form.getAreaId());
        entity.setApplyType(form.getApplyType() != null ? form.getApplyType() : "NORMAL");
        entity.setApplyReason(form.getApplyReason());
        entity.setStartTime(form.getStartTime());
        entity.setEndTime(form.getEndTime());
        entity.setStatus("PENDING");
        entity.setCreateTime(LocalDateTime.now());

        // 2. 淇濆瓨鍒版暟鎹簱
        accessPermissionApplyDao.insert(entity);
        log.info("[鏉冮檺鐢宠] 鐢宠宸蹭繚瀛樺埌鏁版嵁搴擄紝applyNo={}, id={}", entity.getApplyNo(), entity.getId());

        // 3. 鍚姩瀹℃壒娴佺▼
        String businessType = "NORMAL".equals(entity.getApplyType())
            ? "ACCESS_PERMISSION_APPLY"
            : "ACCESS_EMERGENCY_PERMISSION_APPLY";

        Map<String, Object> formData = new HashMap<>();
        formData.put("applyNo", entity.getApplyNo());
        formData.put("applicantId", entity.getApplicantId());
        formData.put("areaId", entity.getAreaId());
        formData.put("applyType", entity.getApplyType());
        formData.put("applyReason", entity.getApplyReason());
        formData.put("startTime", entity.getStartTime());
        formData.put("endTime", entity.getEndTime());

        ResponseDTO<Long> workflowResult = workflowApprovalManager.startApprovalProcess(
            null, // definitionId涓簄ull锛屼粠瀹℃壒閰嶇疆涓幏鍙?            entity.getApplyNo(), // businessKey浣跨敤鐢宠缂栧彿
            "闂ㄧ鏉冮檺鐢宠-" + entity.getApplyNo(), // instanceName
            entity.getApplicantId(), // initiatorId
            businessType, // businessType
            formData, // formData
            null // variables
        );

        if (workflowResult.getCode() == 200 && workflowResult.getData() != null) {
            // 4. 鏇存柊宸ヤ綔娴佸疄渚婭D
            entity.setWorkflowInstanceId(workflowResult.getData());
            accessPermissionApplyDao.updateById(entity);
            log.info("[鏉冮檺鐢宠] 瀹℃壒娴佺▼宸插惎鍔紝applyNo={}, workflowInstanceId={}",
                entity.getApplyNo(), entity.getWorkflowInstanceId());
        } else {
            log.error("[鏉冮檺鐢宠] 鍚姩瀹℃壒娴佺▼澶辫触锛宎pplyNo={}, error={}",
                entity.getApplyNo(), workflowResult.getMessage());
            // 瀹℃壒娴佺▼鍚姩澶辫触涓嶅奖鍝嶇敵璇疯褰曚繚瀛橈紝浣嗛渶瑕佽褰曢敊璇?        }

        log.info("[鏉冮檺鐢宠] 鐢宠鎻愪氦鎴愬姛锛宎pplyNo={}", entity.getApplyNo());
        return entity;
    }

    /**
     * 鏇存柊鏉冮檺鐢宠鐘舵€侊紙鐢卞鎵圭粨鏋滅洃鍚櫒璋冪敤锛?     * <p>
     * 瀹炵幇姝ラ锛?     * 1. 鏍规嵁鐢宠缂栧彿鏌ヨ鐢宠璁板綍
     * 2. 鏇存柊鐢宠鐘舵€併€佸鎵规剰瑙併€佸鎵规椂闂?     * 3. 淇濆瓨鍒版暟鎹簱
     * </p>
     *
     * @param applyNo 鐢宠缂栧彿
     * @param status 瀹℃壒鐘舵€侊紙APPROVED/REJECTED锛?     * @param approvalComment 瀹℃壒鎰忚
     */
    @Override
    public void updatePermissionApplyStatus(String applyNo, String status, String approvalComment) {
        log.info("[鏉冮檺鐢宠] 鏇存柊鐢宠鐘舵€侊紝applyNo={}, status={}", applyNo, status);

        // 1. 鏍规嵁鐢宠缂栧彿鏌ヨ鐢宠璁板綍
        AccessPermissionApplyEntity entity = accessPermissionApplyDao.selectByApplyNo(applyNo);
        if (entity == null) {
            log.warn("[鏉冮檺鐢宠] 鐢宠璁板綍涓嶅瓨鍦紝applyNo={}", applyNo);
            return;
        }

        // 2. 鏇存柊鐢宠鐘舵€?        entity.setStatus(status);
        entity.setApprovalComment(approvalComment);
        entity.setApprovalTime(LocalDateTime.now());

        // 3. 淇濆瓨鍒版暟鎹簱
        accessPermissionApplyDao.updateById(entity);
        log.info("[鏉冮檺鐢宠] 鐢宠鐘舵€佸凡鏇存柊锛宎pplyNo={}, status={}", applyNo, status);
    }
}
