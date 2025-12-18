package net.lab1024.sa.access.service;

import java.time.LocalDate;
import java.util.List;

import net.lab1024.sa.access.controller.AccessMobileController.MobileAccessRecord;
import net.lab1024.sa.access.domain.form.AccessRecordQueryForm;
import net.lab1024.sa.access.domain.vo.AccessRecordStatisticsVO;
import net.lab1024.sa.access.domain.vo.AccessRecordVO;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 闂ㄧ浜嬩欢鏈嶅姟鎺ュ彛
 * <p>
 * 鎻愪緵闂ㄧ浜嬩欢绠＄悊鐩稿叧涓氬姟鍔熻兘
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛? * - Service鎺ュ彛瀹氫箟鍦ㄤ笟鍔℃湇鍔℃ā鍧椾腑
 * - 鏂规硶杩斿洖ResponseDTO缁熶竴鏍煎紡
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface AccessEventService {

    /**
     * 鑾峰彇绉诲姩绔闂褰?     *
     * @param userId 鐢ㄦ埛ID
     * @param size 璁板綍鏁伴噺
     * @return 璁块棶璁板綍鍒楄〃
     */
    ResponseDTO<List<MobileAccessRecord>> getMobileAccessRecords(Long userId, Integer size);

    /**
     * 鍒嗛〉鏌ヨ闂ㄧ璁板綍
     *
     * @param queryForm 鏌ヨ琛ㄥ崟
     * @return 闂ㄧ璁板綍鍒嗛〉缁撴灉
     */
    ResponseDTO<PageResult<AccessRecordVO>> queryAccessRecords(AccessRecordQueryForm queryForm);

    /**
     * 鑾峰彇闂ㄧ璁板綍缁熻
     *
     * @param startDate 寮€濮嬫棩鏈?     * @param endDate 缁撴潫鏃ユ湡
     * @param areaId 鍖哄煙ID锛堝彲閫夛級
     * @return 缁熻鏁版嵁
     */
    ResponseDTO<AccessRecordStatisticsVO> getAccessRecordStatistics(LocalDate startDate, LocalDate endDate, String areaId);

    /**
     * 鍒涘缓闂ㄧ璁板綍
     * <p>
     * 鐢ㄤ簬璁惧鍗忚鎺ㄩ€侀棬绂佽褰?     * 閫氳繃瀹¤鏃ュ織璁板綍闂ㄧ浜嬩欢
     * </p>
     *
     * @param form 闂ㄧ璁板綍鍒涘缓琛ㄥ崟
     * @return 鍒涘缓鐨勯棬绂佽褰旾D锛堝璁℃棩蹇桰D锛?     */
    ResponseDTO<Long> createAccessRecord(net.lab1024.sa.access.domain.form.AccessRecordAddForm form);
}

