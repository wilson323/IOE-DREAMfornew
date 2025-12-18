package net.lab1024.sa.access.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.controller.AccessMobileController.MobileAccessRecord;
import net.lab1024.sa.access.dao.AccessRecordDao;
import net.lab1024.sa.common.access.entity.AccessRecordEntity;
import net.lab1024.sa.access.domain.form.AccessRecordAddForm;
import net.lab1024.sa.access.domain.form.AccessRecordQueryForm;
import net.lab1024.sa.access.domain.vo.AccessRecordStatisticsVO;
import net.lab1024.sa.access.domain.vo.AccessRecordVO;
import net.lab1024.sa.access.service.AccessEventService;
import net.lab1024.sa.common.audit.dao.AuditLogDao;
import net.lab1024.sa.common.audit.entity.AuditLogEntity;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 闂ㄧ浜嬩欢鏈嶅姟瀹炵幇绫? * <p>
 * 瀹炵幇闂ㄧ浜嬩欢绠＄悊鐩稿叧涓氬姟鍔熻兘
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛? * - 浣跨敤@Service娉ㄨВ鏍囪瘑鏈嶅姟绫? * - 浣跨敤@Resource娉ㄥ叆渚濊禆
 * - 浣跨敤@Transactional绠＄悊浜嬪姟
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AccessEventServiceImpl implements AccessEventService {

    @Resource
    private AccessRecordDao accessRecordDao;

    @Resource
    private AuditLogDao auditLogDao;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 鑾峰彇绉诲姩绔闂褰?     * <p>
     * 浠庨棬绂佽褰曡〃鏌ヨ鏈€杩戠殑闂ㄧ璁板綍锛岃浆鎹负绉诲姩绔牸寮?     * </p>
     *
     * @param userId 鐢ㄦ埛ID
     * @param size 璁板綍鏁伴噺
     * @return 璁块棶璁板綍鍒楄〃
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<List<MobileAccessRecord>> getMobileAccessRecords(Long userId, Integer size) {
        log.debug("[闂ㄧ浜嬩欢] 鑾峰彇绉诲姩绔闂褰曪紝userId={}, size={}", userId, size);

        // 鏌ヨ鏈€杩戠殑闂ㄧ璁板綍锛堟渶杩?0澶╋級
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(30);
        Integer querySize = size != null && size > 0 ? size : 20;

        List<AccessRecordEntity> records = accessRecordDao.selectByUserIdAndTimeRange(
            userId, startTime, endTime, querySize);

        // 杞崲涓虹Щ鍔ㄧ鏍煎紡
        List<MobileAccessRecord> mobileRecords = records.stream()
            .map(this::convertToMobileRecord)
            .collect(Collectors.toList());

        log.debug("[闂ㄧ浜嬩欢] 鏌ヨ鍒皗}鏉¤褰?, mobileRecords.size());
        return ResponseDTO.ok(mobileRecords);
    }

    /**
     * 鍒嗛〉鏌ヨ闂ㄧ璁板綍
     * <p>
     * 浠庨棬绂佽褰曡〃鍒嗛〉鏌ヨ锛屾敮鎸佸鏉′欢绛涢€?     * </p>
     *
     * @param queryForm 鏌ヨ琛ㄥ崟
     * @return 闂ㄧ璁板綍鍒嗛〉缁撴灉
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<PageResult<AccessRecordVO>> queryAccessRecords(AccessRecordQueryForm queryForm) {
        log.debug("[闂ㄧ浜嬩欢] 鍒嗛〉鏌ヨ闂ㄧ璁板綍锛宷ueryForm={}", queryForm);

        // 鏋勫缓鍒嗛〉瀵硅薄
        Integer pageNum = queryForm.getPageNum() != null ? queryForm.getPageNum() : 1;
        Integer pageSize = queryForm.getPageSize() != null ? queryForm.getPageSize() : 20;
        Page<AccessRecordEntity> page = new Page<>(pageNum, pageSize);

        // 鎵ц鍒嗛〉鏌ヨ
        Page<AccessRecordEntity> pageResult = accessRecordDao.selectPage(
            page,
            queryForm.getUserId(),
            queryForm.getDeviceId(),
            queryForm.getAreaId(),
            queryForm.getStartDate(),
            queryForm.getEndDate(),
            queryForm.getAccessResult()
        );

        // 杞崲涓篤O
        List<AccessRecordVO> voList = pageResult.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());

        // 鏋勫缓鍒嗛〉缁撴灉
        PageResult<AccessRecordVO> result = new PageResult<>();
        result.setList(voList);
        result.setTotal(pageResult.getTotal());
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setPages((int) pageResult.getPages());

        log.debug("[闂ㄧ浜嬩欢] 鍒嗛〉鏌ヨ瀹屾垚锛屾€绘暟={}, 褰撳墠椤?{}", result.getTotal(), pageNum);
        return ResponseDTO.ok(result);
    }

    /**
     * 鑾峰彇闂ㄧ璁板綍缁熻
     * <p>
     * 缁熻鎸囧畾鏃堕棿鑼冨洿鍐呯殑闂ㄧ璁板綍锛屽寘鎷€绘暟銆佹垚鍔熸暟銆佸け璐ユ暟
     * </p>
     *
     * @param startDate 寮€濮嬫棩鏈?     * @param endDate 缁撴潫鏃ユ湡
     * @param areaId 鍖哄煙ID锛堝彲閫夛級
     * @return 缁熻鏁版嵁
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<AccessRecordStatisticsVO> getAccessRecordStatistics(LocalDate startDate, LocalDate endDate, String areaId) {
        log.debug("[闂ㄧ浜嬩欢] 鑾峰彇闂ㄧ璁板綍缁熻锛宻tartDate={}, endDate={}, areaId={}", startDate, endDate, areaId);

        // 鎵ц缁熻鏌ヨ
        AccessRecordDao.AccessRecordStatistics statistics = accessRecordDao.selectStatistics(
            startDate, endDate, areaId);

        // 杞崲涓篤O
        AccessRecordStatisticsVO vo = new AccessRecordStatisticsVO();
        vo.setTotalCount(statistics != null ? statistics.getTotalCount() : 0L);
        vo.setSuccessCount(statistics != null ? statistics.getSuccessCount() : 0L);
        vo.setFailedCount(statistics != null ? statistics.getFailedCount() : 0L);

        log.debug("[闂ㄧ浜嬩欢] 缁熻瀹屾垚锛屾€绘暟={}, 鎴愬姛={}, 澶辫触={}",
            vo.getTotalCount(), vo.getSuccessCount(), vo.getFailedCount());
        return ResponseDTO.ok(vo);
    }

    /**
     * 鍒涘缓闂ㄧ璁板綍
     * <p>
     * 瀹炵幇姝ラ锛?     * 1. 鍒涘缓闂ㄧ璁板綍瀹炰綋骞朵繚瀛樺埌鏁版嵁搴?     * 2. 鍐欏叆瀹¤鏃ュ織
     * 3. 杩斿洖瀹¤鏃ュ織ID
     * </p>
     *
     * @param form 闂ㄧ璁板綍鍒涘缓琛ㄥ崟
     * @return 鍒涘缓鐨勯棬绂佽褰旾D锛堝璁℃棩蹇桰D锛?     */
    @Override
    public ResponseDTO<Long> createAccessRecord(AccessRecordAddForm form) {
        log.info("[闂ㄧ浜嬩欢] 鍒涘缓闂ㄧ璁板綍锛宖orm={}", form);

        // 1. 鍒涘缓闂ㄧ璁板綍瀹炰綋
        AccessRecordEntity entity = new AccessRecordEntity();
        entity.setUserId(form.getUserId());
        entity.setDeviceId(form.getDeviceId());
        entity.setAreaId(form.getAreaId());
        entity.setAccessResult(form.getAccessResult() != null ? form.getAccessResult() : 1);

        // 澶勭悊閫氳鏃堕棿
        LocalDateTime accessTime = LocalDateTime.now();
        if (form.getPassTime() != null) {
            if (form.getPassTime() instanceof LocalDateTime) {
                accessTime = (LocalDateTime) form.getPassTime();
            } else if (form.getPassTime() instanceof String) {
                try {
                    accessTime = LocalDateTime.parse((String) form.getPassTime(), DATE_TIME_FORMATTER);
                } catch (Exception e) {
                    log.warn("[闂ㄧ浜嬩欢] 瑙ｆ瀽閫氳鏃堕棿澶辫触锛屼娇鐢ㄥ綋鍓嶆椂闂达紝passTime={}", form.getPassTime());
                }
            }
        }
        entity.setAccessTime(accessTime);

        // 澶勭悊閫氳绫诲瀷
        if (form.getPassType() != null) {
            entity.setAccessType(form.getPassType() == 0 ? "IN" : "OUT");
        } else {
            entity.setAccessType("IN");
        }

        // 澶勭悊楠岃瘉鏂瑰紡
        if (form.getPassMethod() != null) {
            switch (form.getPassMethod()) {
                case 0:
                    entity.setVerifyMethod("CARD");
                    break;
                case 1:
                    entity.setVerifyMethod("FACE");
                    break;
                case 2:
                    entity.setVerifyMethod("FINGERPRINT");
                    break;
                default:
                    entity.setVerifyMethod("CARD");
            }
        }

        // 淇濆瓨鍒版暟鎹簱
        accessRecordDao.insert(entity);
        log.info("[闂ㄧ浜嬩欢] 闂ㄧ璁板綍宸蹭繚瀛橈紝recordId={}", entity.getRecordId());

        // 2. 鍐欏叆瀹¤鏃ュ織
        AuditLogEntity auditLog = new AuditLogEntity();
        auditLog.setUserId(form.getUserId());
        auditLog.setModuleName("ACCESS");
        auditLog.setOperationType(1); // 1-鏌ヨ锛?-鏂板锛?-淇敼锛?-鍒犻櫎
        auditLog.setOperation("CREATE_ACCESS_RECORD");
        auditLog.setOperationDesc("鍒涘缓闂ㄧ璁板綍");
        auditLog.setResourceType("ACCESS_RECORD");
        auditLog.setResourceId(String.valueOf(entity.getRecordId()));
        auditLog.setResultStatus(entity.getAccessResult() == 1 ? 1 : 0);
        auditLog.setCreateTime(LocalDateTime.now());

        auditLogDao.insert(auditLog);
        log.info("[闂ㄧ浜嬩欢] 瀹¤鏃ュ織宸插啓鍏ワ紝logId={}", auditLog.getLogId());

        // 杩斿洖瀹¤鏃ュ織ID
        return ResponseDTO.ok(auditLog.getLogId());
    }

    /**
     * 杞崲涓虹Щ鍔ㄧ璁板綍鏍煎紡
     *
     * @param entity 闂ㄧ璁板綍瀹炰綋
     * @return 绉诲姩绔褰?     */
    private MobileAccessRecord convertToMobileRecord(AccessRecordEntity entity) {
        MobileAccessRecord record = new MobileAccessRecord();
        record.setRecordId(entity.getRecordId());
        record.setUserId(entity.getUserId());
        record.setDeviceId(entity.getDeviceId());
        record.setDeviceName("璁惧" + entity.getDeviceId()); // 瀹為檯搴斾粠璁惧鏈嶅姟鑾峰彇
        record.setAccessTime(entity.getAccessTime() != null
            ? entity.getAccessTime().format(DATE_TIME_FORMATTER)
            : "");
        record.setAccessType(entity.getAccessType());
        record.setAccessResult(entity.getAccessResult() == 1);
        return record;
    }

    /**
     * 杞崲涓篤O鏍煎紡
     *
     * @param entity 闂ㄧ璁板綍瀹炰綋
     * @return 闂ㄧ璁板綍VO
     */
    private AccessRecordVO convertToVO(AccessRecordEntity entity) {
        AccessRecordVO vo = new AccessRecordVO();
        vo.setLogId(entity.getRecordId());
        vo.setUserId(entity.getUserId());
        vo.setUserName("鐢ㄦ埛" + entity.getUserId()); // 瀹為檯搴斾粠鐢ㄦ埛鏈嶅姟鑾峰彇
        vo.setDeviceId(String.valueOf(entity.getDeviceId()));
        vo.setDeviceName("璁惧" + entity.getDeviceId()); // 瀹為檯搴斾粠璁惧鏈嶅姟鑾峰彇
        vo.setAreaId(entity.getAreaId() != null ? String.valueOf(entity.getAreaId()) : null);
        vo.setAreaName(entity.getAreaId() != null ? "鍖哄煙" + entity.getAreaId() : null); // 瀹為檯搴斾粠鍖哄煙鏈嶅姟鑾峰彇
        vo.setOperation(entity.getVerifyMethod());
        vo.setResult(entity.getAccessResult());
        vo.setCreateTime(entity.getAccessTime());
        vo.setIpAddress(null); // 浠庡璁℃棩蹇楄幏鍙?        vo.setRemark(null);
        return vo;
    }
}
