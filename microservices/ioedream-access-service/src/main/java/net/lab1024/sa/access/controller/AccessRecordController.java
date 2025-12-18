package net.lab1024.sa.access.controller;

import io.micrometer.observation.annotation.Observed;
import org.springframework.format.annotation.DateTimeFormat;
import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.access.domain.form.AccessRecordAddForm;
import net.lab1024.sa.access.service.AccessEventService;

import java.time.LocalDate;

/**
 * 闂ㄧ璁板綍绠＄悊PC绔帶鍒跺櫒
 * <p>
 * 鎻愪緵PC绔棬绂佽褰曠鐞嗙浉鍏矨PI
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛? * - 浣跨敤@RestController娉ㄨВ
 * - 浣跨敤@Resource渚濊禆娉ㄥ叆
 * - 浣跨敤@Valid鍙傛暟鏍￠獙
 * - 杩斿洖缁熶竴ResponseDTO鏍煎紡
 * </p>
 * <p>
 * 涓氬姟鍦烘櫙锛? * - 闂ㄧ璁板綍鏌ヨ锛堝垎椤碉級
 * - 闂ㄧ璁板綍缁熻
 * - 闂ㄧ璁板綍瀵煎嚭
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/access/record")
@Tag(name = "闂ㄧ璁板綍绠＄悊PC绔?, description = "闂ㄧ璁板綍鏌ヨ銆佺粺璁°€佸鍑虹瓑API")
@PermissionCheck(value = "ACCESS_RECORD", description = "闂ㄧ璁板綍绠＄悊妯″潡鏉冮檺")
public class AccessRecordController {

    @Resource
    private AccessEventService accessEventService;

    /**
     * 鍒涘缓闂ㄧ璁板綍
     * <p>
     * 鐢ㄤ簬璁惧鍗忚鎺ㄩ€侀棬绂佽褰?     * 閫氳繃瀹¤鏃ュ織璁板綍闂ㄧ浜嬩欢
     * </p>
     *
     * @param form 闂ㄧ璁板綍鍒涘缓琛ㄥ崟
     * @return 鍒涘缓鐨勯棬绂佽褰旾D锛堝璁℃棩蹇桰D锛?     * @apiNote 绀轰緥璇锋眰锛?     * <pre>
     * POST /api/v1/access/record/create
     * {
     *   "userId": 1001,
     *   "deviceId": 1,
     *   "deviceCode": "DEV001",
     *   "passTime": 1706582400,
     *   "passType": 0,
     *   "doorNo": 1,
     *   "passMethod": 1,
     *   "accessResult": 1
     * }
     * </pre>
     */
    @Observed(name = "accessRecord.createAccessRecord", contextualName = "access-record-create")
    @PostMapping("/create")
    @Operation(
        summary = "鍒涘缓闂ㄧ璁板綍",
        description = "鍒涘缓闂ㄧ璁板綍锛岀敤浜庤澶囧崗璁帹閫侀棬绂佽褰曘€傞€氳繃瀹¤鏃ュ織璁板綍闂ㄧ浜嬩欢銆?,
        tags = {"闂ㄧ璁板綍绠＄悊PC绔?}
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "鍒涘缓鎴愬姛",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Long.class)
        )
    )
    public ResponseDTO<Long> createAccessRecord(
            @Valid @RequestBody AccessRecordAddForm form) {
        log.info("[闂ㄧ璁板綍] 鍒涘缓闂ㄧ璁板綍锛寀serId={}, deviceId={}, passType={}",
                form.getUserId(), form.getDeviceId(), form.getPassType());
        return accessEventService.createAccessRecord(form);
    }

    /**
     * 鍒嗛〉鏌ヨ闂ㄧ璁板綍
     * <p>
     * 涓ユ牸閬靛惊RESTful瑙勮寖锛氭煡璇㈡搷浣滀娇鐢℅ET鏂规硶
     * </p>
     *
     * @param pageNum 椤电爜
     * @param pageSize 姣忛〉澶у皬
     * @param userId 鐢ㄦ埛ID锛堝彲閫夛級
     * @param deviceId 璁惧ID锛堝彲閫夛級
     * @param areaId 鍖哄煙ID锛堝彲閫夛級
     * @param startDate 寮€濮嬫棩鏈燂紙鍙€夛級
     * @param endDate 缁撴潫鏃ユ湡锛堝彲閫夛級
     * @param accessResult 閫氳缁撴灉锛堝彲閫夛級
     * @return 闂ㄧ璁板綍鍒楄〃
     */
    @Observed(name = "accessRecord.queryAccessRecords", contextualName = "access-record-query")
    @GetMapping("/query")
    @Operation(summary = "鍒嗛〉鏌ヨ闂ㄧ璁板綍", description = "鏍规嵁鏉′欢鍒嗛〉鏌ヨ闂ㄧ璁板綍")
    @PermissionCheck(value = "ACCESS_RECORD_VIEW", description = "鏌ヨ闂ㄧ璁板綍")
    public ResponseDTO<PageResult<net.lab1024.sa.access.domain.vo.AccessRecordVO>> queryAccessRecords(
            @Parameter(description = "椤电爜锛屼粠1寮€濮?)
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "姣忛〉澶у皬")
            @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "鐢ㄦ埛ID锛堝彲閫夛級")
            @RequestParam(required = false) Long userId,
            @Parameter(description = "璁惧ID锛堝彲閫夛級")
            @RequestParam(required = false) Long deviceId,
            @Parameter(description = "鍖哄煙ID锛堝彲閫夛級")
            @RequestParam(required = false) String areaId,
            @Parameter(description = "寮€濮嬫棩鏈燂紝鏍煎紡锛歽yyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "缁撴潫鏃ユ湡锛屾牸寮忥細yyyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "閫氳缁撴灉锛堝彲閫夛級锛?-鎴愬姛锛?-澶辫触锛?-寮傚父")
            @RequestParam(required = false) Integer accessResult) {
        log.info("[闂ㄧ璁板綍] 鍒嗛〉鏌ヨ闂ㄧ璁板綍锛宲ageNum={}, pageSize={}, userId={}, deviceId={}, areaId={}, startDate={}, endDate={}, accessResult={}",
                pageNum, pageSize, userId, deviceId, areaId, startDate, endDate, accessResult);
        try {
            // 鏋勫缓鏌ヨ琛ㄥ崟
            net.lab1024.sa.access.domain.form.AccessRecordQueryForm queryForm = new net.lab1024.sa.access.domain.form.AccessRecordQueryForm();
            queryForm.setPageNum(pageNum);
            queryForm.setPageSize(pageSize);
            queryForm.setUserId(userId);
            queryForm.setDeviceId(deviceId);
            queryForm.setAreaId(areaId);
            queryForm.setStartDate(startDate);
            queryForm.setEndDate(endDate);
            queryForm.setAccessResult(accessResult);

            // 璋冪敤Service灞傛煡璇?            return accessEventService.queryAccessRecords(queryForm);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[闂ㄧ璁板綍] 鍒嗛〉鏌ヨ闂ㄧ璁板綍鍙傛暟閿欒: pageNum={}, pageSize={}, error={}", pageNum, pageSize, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "鍙傛暟閿欒锛? + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[闂ㄧ璁板綍] 鍒嗛〉鏌ヨ闂ㄧ璁板綍涓氬姟寮傚父: pageNum={}, pageSize={}, code={}, message={}", pageNum, pageSize, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[闂ㄧ璁板綍] 鍒嗛〉鏌ヨ闂ㄧ璁板綍绯荤粺寮傚父: pageNum={}, pageSize={}, code={}, message={}", pageNum, pageSize, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("QUERY_ACCESS_RECORDS_SYSTEM_ERROR", "鏌ヨ闂ㄧ璁板綍澶辫触锛? + e.getMessage());
        } catch (Exception e) {
            log.error("[闂ㄧ璁板綍] 鍒嗛〉鏌ヨ闂ㄧ璁板綍鏈煡寮傚父: pageNum={}, pageSize={}", pageNum, pageSize, e);
            return ResponseDTO.error("QUERY_ACCESS_RECORDS_ERROR", "鏌ヨ闂ㄧ璁板綍澶辫触: " + e.getMessage());
        }
    }

    /**
     * 鑾峰彇闂ㄧ璁板綍缁熻
     *
     * @param startDate 寮€濮嬫棩鏈?     * @param endDate 缁撴潫鏃ユ湡
     * @param areaId 鍖哄煙ID锛堝彲閫夛級
     * @return 缁熻鏁版嵁
     */
    @Observed(name = "accessRecord.getAccessRecordStatistics", contextualName = "access-record-statistics")
    @GetMapping("/statistics")
    @Operation(summary = "鑾峰彇闂ㄧ璁板綍缁熻", description = "鏍规嵁鏃堕棿鑼冨洿鍜屽尯鍩熻幏鍙栭棬绂佽褰曠粺璁℃暟鎹?)
    @PermissionCheck(value = "ACCESS_RECORD_VIEW", description = "鑾峰彇闂ㄧ璁板綍缁熻")
    public ResponseDTO<net.lab1024.sa.access.domain.vo.AccessRecordStatisticsVO> getAccessRecordStatistics(
            @Parameter(description = "寮€濮嬫棩鏈燂紝鏍煎紡锛歽yyy-MM-dd")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "缁撴潫鏃ユ湡锛屾牸寮忥細yyyy-MM-dd")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String areaId) {
        log.info("[闂ㄧ璁板綍] 鑾峰彇闂ㄧ璁板綍缁熻锛宻tartDate={}, endDate={}, areaId={}", startDate, endDate, areaId);
        try {
            // 璋冪敤Service灞傜粺璁?            return accessEventService.getAccessRecordStatistics(startDate, endDate, areaId);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[闂ㄧ璁板綍] 鑾峰彇闂ㄧ璁板綍缁熻鍙傛暟閿欒: startDate={}, endDate={}, areaId={}, error={}", startDate, endDate, areaId, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "鍙傛暟閿欒锛? + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[闂ㄧ璁板綍] 鑾峰彇闂ㄧ璁板綍缁熻涓氬姟寮傚父: startDate={}, endDate={}, areaId={}, code={}, message={}", startDate, endDate, areaId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[闂ㄧ璁板綍] 鑾峰彇闂ㄧ璁板綍缁熻绯荤粺寮傚父: startDate={}, endDate={}, areaId={}, code={}, message={}", startDate, endDate, areaId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("GET_STATISTICS_SYSTEM_ERROR", "鑾峰彇缁熻鏁版嵁澶辫触锛? + e.getMessage());
        } catch (Exception e) {
            log.error("[闂ㄧ璁板綍] 鑾峰彇闂ㄧ璁板綍缁熻鏈煡寮傚父: startDate={}, endDate={}, areaId={}", startDate, endDate, areaId, e);
            return ResponseDTO.error("GET_STATISTICS_ERROR", "鑾峰彇缁熻鏁版嵁澶辫触: " + e.getMessage());
        }
    }
}


