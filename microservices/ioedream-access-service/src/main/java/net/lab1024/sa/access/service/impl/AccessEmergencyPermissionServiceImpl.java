package net.lab1024.sa.access.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.dao.AccessPermissionApplyDao;
import net.lab1024.sa.access.dao.AreaPersonDao;
import net.lab1024.sa.common.access.entity.AccessPermissionApplyEntity;
import net.lab1024.sa.access.domain.form.AccessPermissionApplyForm;
import net.lab1024.sa.access.service.AccessEmergencyPermissionService;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.organization.entity.AreaPersonEntity;
import net.lab1024.sa.common.workflow.manager.WorkflowApprovalManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 绱ф€ユ潈闄愮敵璇锋湇鍔″疄鐜扮被
 * <p>
 * 鎻愪緵绱ф€ユ潈闄愮敵璇风浉鍏充笟鍔″姛鑳藉疄鐜? * 鐗圭偣锛? * - 绠€鍖栧鎵规祦绋嬶紙蹇€熷鎵癸級
 * - 涓存椂鏉冮檺鐢熸晥
 * - 鑷姩杩囨湡鏈哄埗锛?4灏忔椂鍚庤嚜鍔ㄥけ鏁堬級
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛? * - 浣跨敤@Service娉ㄨВ鏍囪瘑鏈嶅姟绫? * - 浣跨敤@Transactional绠＄悊浜嬪姟
 * - 浣跨敤@Resource娉ㄥ叆渚濊禆
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AccessEmergencyPermissionServiceImpl implements AccessEmergencyPermissionService {

    @Resource
    private AccessPermissionApplyDao accessPermissionApplyDao;

    @Resource
    private AreaPersonDao areaPersonDao;

    @Resource
    private WorkflowApprovalManager workflowApprovalManager;

    /**
     * 鎻愪氦绱ф€ユ潈闄愮敵璇凤紙鍚姩蹇€熷鎵规祦绋嬶級
     * <p>
     * 瀹炵幇姝ラ锛?     * 1. 鍒涘缓鐢宠瀹炰綋骞朵繚瀛樺埌鏁版嵁搴?     * 2. 閫氳繃缃戝叧璋冪敤宸ヤ綔娴佹湇鍔″惎鍔ㄥ揩閫熷鎵规祦绋?     * 3. 杩斿洖鍖呭惈workflowInstanceId鐨勭敵璇峰疄浣?     * </p>
     *
     * @param form 绱ф€ユ潈闄愮敵璇疯〃鍗?     * @return 鏉冮檺鐢宠瀹炰綋锛堝寘鍚玾orkflowInstanceId锛?     */
    @Override
    public AccessPermissionApplyEntity submitEmergencyPermissionApply(AccessPermissionApplyForm form) {
        log.info("[绱ф€ユ潈闄愮敵璇穄 鎻愪氦鐢宠锛宎pplicantId={}, reason={}", form.getApplicantId(), form.getApplyReason());

        // 鍒涘缓鐢宠瀹炰綋
        AccessPermissionApplyEntity entity = new AccessPermissionApplyEntity();
        entity.setApplyNo("EMERGENCY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        entity.setApplicantId(form.getApplicantId());
        entity.setAreaId(form.getAreaId());
        entity.setApplyType("EMERGENCY");
        entity.setApplyReason(form.getApplyReason());
        entity.setStatus("PENDING");
        entity.setCreateTime(LocalDateTime.now());
        entity.setStartTime(LocalDateTime.now());
        entity.setEndTime(LocalDateTime.now().plusHours(24)); // 24灏忔椂鍚庤繃鏈?
        // 淇濆瓨鍒版暟鎹簱
        accessPermissionApplyDao.insert(entity);
        log.info("[绱ф€ユ潈闄愮敵璇穄 鐢宠宸蹭繚瀛樺埌鏁版嵁搴擄紝id={}, applyNo={}", entity.getId(), entity.getApplyNo());

        // 鍚姩蹇€熷鎵规祦绋?        Map<String, Object> formData = new HashMap<>();
        formData.put("applyNo", entity.getApplyNo());
        formData.put("applicantId", entity.getApplicantId());
        formData.put("areaId", entity.getAreaId());
        formData.put("applyType", "EMERGENCY");
        formData.put("applyReason", entity.getApplyReason());
        formData.put("startTime", entity.getStartTime());
        formData.put("endTime", entity.getEndTime());
        formData.put("priority", "HIGH");

        ResponseDTO<Long> workflowResult = workflowApprovalManager.startApprovalProcess(
            null, // definitionId涓簄ull锛屼粠瀹℃壒閰嶇疆涓幏鍙?            entity.getApplyNo(), // businessKey浣跨敤鐢宠缂栧彿
            "绱ф€ユ潈闄愮敵璇?" + entity.getApplyNo(), // instanceName
            entity.getApplicantId(), // initiatorId
            "ACCESS_EMERGENCY_PERMISSION_APPLY", // businessType
            formData, // formData
            null // variables
        );

        if (workflowResult.getCode() == 200 && workflowResult.getData() != null) {
            entity.setWorkflowInstanceId(workflowResult.getData());
            accessPermissionApplyDao.updateById(entity);
            log.info("[绱ф€ユ潈闄愮敵璇穄 宸ヤ綔娴佸凡鍚姩锛寃orkflowInstanceId={}, applyNo={}",
                workflowResult.getData(), entity.getApplyNo());
        } else {
            log.warn("[绱ф€ユ潈闄愮敵璇穄 宸ヤ綔娴佸惎鍔ㄥけ璐ワ紝applyNo={}, error={}",
                entity.getApplyNo(), workflowResult.getMessage());
            // 涓嶆姏鍑哄紓甯革紝鍏佽鐢宠璁板綍淇濆瓨锛屽悗缁彲鎵嬪姩澶勭悊
        }

        log.info("[绱ф€ユ潈闄愮敵璇穄 鐢宠宸插垱寤猴紝applyNo={}", entity.getApplyNo());
        return entity;
    }

    /**
     * 鏇存柊绱ф€ユ潈闄愮敵璇风姸鎬侊紙鐢卞鎵圭粨鏋滅洃鍚櫒璋冪敤锛?     * <p>
     * 瀹炵幇姝ラ锛?     * 1. 浠庢暟鎹簱鏌ヨ鐢宠璁板綍
     * 2. 鏇存柊鐢宠鐘舵€佸拰瀹℃壒淇℃伅
     * 3. 濡傛灉瀹℃壒閫氳繃锛屾縺娲讳复鏃舵潈闄愶紙娣诲姞AreaPersonEntity璁板綍锛?     * </p>
     *
     * @param applyNo 鐢宠缂栧彿
     * @param status 瀹℃壒鐘舵€?     * @param approvalComment 瀹℃壒鎰忚
     */
    @Override
    public void updateEmergencyPermissionStatus(String applyNo, String status, String approvalComment) {
        log.info("[绱ф€ユ潈闄愮敵璇穄 鏇存柊鐘舵€侊紝applyNo={}, status={}, comment={}", applyNo, status, approvalComment);

        // 浠庢暟鎹簱鏌ヨ鐢宠璁板綍
        AccessPermissionApplyEntity entity = accessPermissionApplyDao.selectByApplyNo(applyNo);
        if (entity == null) {
            log.warn("[绱ф€ユ潈闄愮敵璇穄 鐢宠璁板綍涓嶅瓨鍦紝applyNo={}", applyNo);
            return;
        }

        // 鏇存柊鐘舵€?        entity.setStatus(status);
        entity.setApprovalComment(approvalComment);
        entity.setApprovalTime(LocalDateTime.now());
        accessPermissionApplyDao.updateById(entity);
        log.info("[绱ф€ユ潈闄愮敵璇穄 鐢宠鐘舵€佸凡鏇存柊锛宎pplyNo={}, status={}", applyNo, status);

        // 濡傛灉瀹℃壒閫氳繃锛屾縺娲讳复鏃舵潈闄?        if ("APPROVED".equals(status)) {
            log.info("[绱ф€ユ潈闄愮敵璇穄 瀹℃壒閫氳繃锛屾縺娲讳复鏃舵潈闄愶紝applyNo={}", applyNo);
            activateTemporaryPermission(entity);
        } else if ("REJECTED".equals(status)) {
            log.info("[绱ф€ユ潈闄愮敵璇穄 瀹℃壒鎷掔粷锛宎pplyNo={}", applyNo);
        }
    }

    /**
     * 妫€鏌ュ苟鍥炴敹杩囨湡鏉冮檺
     * <p>
     * 瀹炵幇姝ラ锛?     * 1. 浠庢暟鎹簱鏌ヨ鐢宠璁板綍
     * 2. 妫€鏌ユ槸鍚﹁繃鏈燂紙endTime < 褰撳墠鏃堕棿锛?     * 3. 濡傛灉杩囨湡锛屽洖鏀舵潈闄愶紙鍒犻櫎AreaPersonEntity璁板綍锛?     * </p>
     *
     * @param applyNo 鐢宠缂栧彿
     * @return 鏄惁宸茶繃鏈熷苟鍥炴敹
     */
    @Override
    public boolean checkAndRevokeExpiredPermission(String applyNo) {
        log.info("[绱ф€ユ潈闄愮敵璇穄 妫€鏌ヨ繃鏈熸潈闄愶紝applyNo={}", applyNo);

        // 浠庢暟鎹簱鏌ヨ鐢宠璁板綍
        AccessPermissionApplyEntity entity = accessPermissionApplyDao.selectByApplyNo(applyNo);
        if (entity == null) {
            log.warn("[绱ф€ユ潈闄愮敵璇穄 鐢宠璁板綍涓嶅瓨鍦紝applyNo={}", applyNo);
            return false;
        }

        // 妫€鏌ユ槸鍚﹁繃鏈?        boolean isExpired = LocalDateTime.now().isAfter(entity.getEndTime());

        if (isExpired && "APPROVED".equals(entity.getStatus())) {
            log.info("[绱ф€ユ潈闄愮敵璇穄 鏉冮檺宸茶繃鏈燂紝鎵ц鍥炴敹锛宎pplyNo={}", applyNo);
            revokeTemporaryPermission(entity);
            return true;
        }

        return false;
    }

    /**
     * 婵€娲婚棬绂佷复鏃舵潈闄?     * <p>
     * 閫氳繃娣诲姞AreaPersonEntity璁板綍鏉ユ縺娲讳复鏃舵潈闄?     * 浣跨敤accessReason瀛楁瀛樺偍鐢宠缂栧彿锛岀敤浜庡悗缁潈闄愬洖鏀?     * </p>
     *
     * @param entity 鏉冮檺鐢宠瀹炰綋
     */
    private void activateTemporaryPermission(AccessPermissionApplyEntity entity) {
        try {
            // 妫€鏌ユ槸鍚﹀凡瀛樺湪鐩稿悓鐢宠鐨勬潈闄愯褰曪紙閫氳繃accessReason鍒ゆ柇锛?            List<AreaPersonEntity> existingPermissions = areaPersonDao.selectList(
                    com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(AreaPersonEntity.class)
                            .eq(AreaPersonEntity::getPersonId, entity.getApplicantId())
                            .eq(AreaPersonEntity::getAreaId, entity.getAreaId())
                            .eq(AreaPersonEntity::getAccessReason, "EMERGENCY_APPLY_" + entity.getApplyNo())
            );

            if (!existingPermissions.isEmpty()) {
                log.info("[绱ф€ユ潈闄愮敵璇穄 涓存椂鏉冮檺宸插瓨鍦紝鏃犻渶閲嶅娣诲姞锛宎pplicantId={}, areaId={}, applyNo={}",
                        entity.getApplicantId(), entity.getAreaId(), entity.getApplyNo());
                return;
            }

            // 鍒涘缓涓存椂鏉冮檺璁板綍
            AreaPersonEntity areaPerson = new AreaPersonEntity();
            areaPerson.setPersonId(entity.getApplicantId());
            areaPerson.setAreaId(entity.getAreaId());
            areaPerson.setValidStartTime(entity.getStartTime());
            areaPerson.setValidEndTime(entity.getEndTime());
            areaPerson.setExpireTime(entity.getEndTime());
            areaPerson.setAccessReason("EMERGENCY_APPLY_" + entity.getApplyNo()); // 浣跨敤accessReason瀛樺偍鐢宠缂栧彿
            areaPerson.setStatus(1); // 1-鏈夋晥
            areaPerson.setCreateTime(LocalDateTime.now());
            areaPerson.setUpdateTime(LocalDateTime.now());

            areaPersonDao.insert(areaPerson);
            log.info("[绱ф€ユ潈闄愮敵璇穄 涓存椂鏉冮檺宸叉縺娲伙紝applicantId={}, areaId={}, applyNo={}",
                    entity.getApplicantId(), entity.getAreaId(), entity.getApplyNo());
        } catch (Exception e) {
            log.error("[绱ф€ユ潈闄愮敵璇穄 婵€娲讳复鏃舵潈闄愬け璐ワ紝applyNo={}, error={}", entity.getApplyNo(), e.getMessage(), e);
            throw new RuntimeException("婵€娲讳复鏃舵潈闄愬け璐?, e);
        }
    }

    /**
     * 鍥炴敹闂ㄧ鏉冮檺
     * <p>
     * 閫氳繃鍒犻櫎AreaPersonEntity璁板綍鏉ュ洖鏀朵复鏃舵潈闄?     * 鏍规嵁accessReason瀛楁鍖归厤鐢宠缂栧彿
     * </p>
     *
     * @param entity 鏉冮檺鐢宠瀹炰綋
     */
    private void revokeTemporaryPermission(AccessPermissionApplyEntity entity) {
        try {
            // 鍒犻櫎涓存椂鏉冮檺璁板綍锛堟牴鎹產ccessReason鍖归厤鐢宠缂栧彿锛?            int deletedCount = areaPersonDao.delete(
                    com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(AreaPersonEntity.class)
                            .eq(AreaPersonEntity::getPersonId, entity.getApplicantId())
                            .eq(AreaPersonEntity::getAreaId, entity.getAreaId())
                            .eq(AreaPersonEntity::getAccessReason, "EMERGENCY_APPLY_" + entity.getApplyNo())
            );
            log.info("[绱ф€ユ潈闄愮敵璇穄 涓存椂鏉冮檺宸插洖鏀讹紝applicantId={}, areaId={}, applyNo={}, deletedCount={}",
                    entity.getApplicantId(), entity.getAreaId(), entity.getApplyNo(), deletedCount);
        } catch (Exception e) {
            log.error("[绱ф€ユ潈闄愮敵璇穄 鍥炴敹涓存椂鏉冮檺澶辫触锛宎pplyNo={}, error={}", entity.getApplyNo(), e.getMessage(), e);
            throw new RuntimeException("鍥炴敹涓存椂鏉冮檺澶辫触", e);
        }
    }
}
