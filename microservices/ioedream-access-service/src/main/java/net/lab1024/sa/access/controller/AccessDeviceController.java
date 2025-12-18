package net.lab1024.sa.access.controller;

import io.micrometer.observation.annotation.Observed;
import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.access.service.AccessDeviceService;

/**
 * 闂ㄧ璁惧绠＄悊PC绔帶鍒跺櫒
 * <p>
 * 鎻愪緵PC绔棬绂佽澶囩鐞嗙浉鍏矨PI
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛? * - 浣跨敤@RestController娉ㄨВ
 * - 浣跨敤@Resource渚濊禆娉ㄥ叆
 * - 浣跨敤@Valid鍙傛暟鏍￠獙
 * - 杩斿洖缁熶竴ResponseDTO鏍煎紡
 * </p>
 * <p>
 * 涓氬姟鍦烘櫙锛? * - 璁惧CRUD鎿嶄綔
 * - 璁惧鐘舵€佺鐞? * - 璁惧鏌ヨ鍜岀粺璁? * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/access/device")
@Tag(name = "闂ㄧ璁惧绠＄悊PC绔?, description = "璁惧鏌ヨ銆佹坊鍔犮€佹洿鏂般€佸垹闄ゃ€佺姸鎬佺鐞嗙瓑API")
@PermissionCheck(value = "ACCESS_DEVICE", description = "闂ㄧ璁惧绠＄悊妯″潡鏉冮檺")
public class AccessDeviceController {

    @Resource
    private AccessDeviceService accessDeviceService;

    /**
     * 鍒嗛〉鏌ヨ璁惧
     * <p>
     * 鏀寔澶氭潯浠剁瓫閫夛細鍏抽敭璇嶏紙璁惧鍚嶇О銆佽澶囩紪鍙凤級銆佸尯鍩烮D銆佽澶囩姸鎬併€佸惎鐢ㄧ姸鎬佺瓑
     * 鏀寔鍒嗛〉鏌ヨ锛岄粯璁ゆ瘡椤?0鏉¤褰?     * </p>
     *
     * @param pageNum 椤电爜锛堜粠1寮€濮嬶紝榛樿1锛?     * @param pageSize 姣忛〉澶у皬锛堥粯璁?0锛?     * @param keyword 鍏抽敭璇嶏紙璁惧鍚嶇О銆佽澶囩紪鍙凤紝鍙€夛級
     * @param areaId 鍖哄煙ID锛堝彲閫夛級
     * @param deviceStatus 璁惧鐘舵€侊紙鍙€夛細ONLINE-鍦ㄧ嚎銆丱FFLINE-绂荤嚎銆丗AULT-鏁呴殰锛?     * @param enabledFlag 鍚敤鐘舵€侊紙鍙€夛細1-鍚敤銆?-绂佺敤锛?     * @return 鍒嗛〉鐨勮澶囧垪琛?     * @apiNote 绀轰緥璇锋眰锛?     * <pre>
     * GET /api/v1/access/device/query?pageNum=1&pageSize=20&keyword=闂ㄧ&areaId=4001&deviceStatus=ONLINE&enabledFlag=1
     * </pre>
     */
    @Observed(name = "accessDevice.queryDevices", contextualName = "access-device-query")
    @GetMapping("/query")
    @Operation(
        summary = "鍒嗛〉鏌ヨ璁惧",
        description = "鏍规嵁鏉′欢鍒嗛〉鏌ヨ闂ㄧ璁惧锛屾敮鎸佸鏉′欢绛涢€夛紙鍏抽敭璇嶃€佸尯鍩烮D銆佽澶囩姸鎬併€佸惎鐢ㄧ姸鎬佺瓑锛夈€傛敮鎸佸垎椤垫煡璇紝榛樿姣忛〉20鏉¤褰曘€備弗鏍奸伒寰猂ESTful瑙勮寖锛氭煡璇㈡搷浣滀娇鐢℅ET鏂规硶銆?,
        tags = {"闂ㄧ璁惧绠＄悊PC绔?}
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "鏌ヨ鎴愬姛",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PageResult.class)
        )
    )
    @PermissionCheck(value = "ACCESS_DEVICE_VIEW", description = "鏌ヨ闂ㄧ璁惧")
    public ResponseDTO<PageResult<net.lab1024.sa.access.domain.vo.AccessDeviceVO>> queryDevices(
            @Parameter(description = "椤电爜锛堜粠1寮€濮嬶級", example = "1")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "姣忛〉澶у皬", example = "20")
            @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "鍏抽敭璇嶏紙璁惧鍚嶇О銆佽澶囩紪鍙凤級", example = "闂ㄧ")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "鍖哄煙ID", example = "4001")
            @RequestParam(required = false) Long areaId,
            @Parameter(description = "璁惧鐘舵€侊紙ONLINE/OFFLINE/FAULT锛?, example = "ONLINE")
            @RequestParam(required = false) String deviceStatus,
            @Parameter(description = "鍚敤鐘舵€侊紙1-鍚敤銆?-绂佺敤锛?, example = "1")
            @RequestParam(required = false) Integer enabledFlag) {
        log.info("[闂ㄧ璁惧] 鍒嗛〉鏌ヨ璁惧锛宲ageNum={}, pageSize={}, keyword={}, areaId={}, deviceStatus={}, enabledFlag={}",
                pageNum, pageSize, keyword, areaId, deviceStatus, enabledFlag);
        try {
            // 鏋勫缓鏌ヨ琛ㄥ崟
            net.lab1024.sa.access.domain.form.AccessDeviceQueryForm queryForm = new net.lab1024.sa.access.domain.form.AccessDeviceQueryForm();
            queryForm.setPageNum(pageNum);
            queryForm.setPageSize(pageSize);
            queryForm.setKeyword(keyword);
            queryForm.setAreaId(areaId);
            queryForm.setDeviceStatus(deviceStatus);
            queryForm.setEnabledFlag(enabledFlag);

            // 璋冪敤Service灞傛煡璇?            return accessDeviceService.queryDevices(queryForm);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[闂ㄧ璁惧] 鍒嗛〉鏌ヨ璁惧鍙傛暟閿欒: pageNum={}, pageSize={}, error={}", pageNum, pageSize, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "鍙傛暟閿欒锛? + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[闂ㄧ璁惧] 鍒嗛〉鏌ヨ璁惧涓氬姟寮傚父: pageNum={}, pageSize={}, code={}, message={}", pageNum, pageSize, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[闂ㄧ璁惧] 鍒嗛〉鏌ヨ璁惧绯荤粺寮傚父: pageNum={}, pageSize={}, code={}, message={}", pageNum, pageSize, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("QUERY_DEVICES_SYSTEM_ERROR", "鏌ヨ璁惧澶辫触锛? + e.getMessage());
        } catch (Exception e) {
            log.error("[闂ㄧ璁惧] 鍒嗛〉鏌ヨ璁惧鏈煡寮傚父: pageNum={}, pageSize={}", pageNum, pageSize, e);
            return ResponseDTO.error("QUERY_DEVICES_ERROR", "鏌ヨ璁惧澶辫触: " + e.getMessage());
        }
    }

    /**
     * 鏌ヨ璁惧璇︽儏
     *
     * @param deviceId 璁惧ID
     * @return 璁惧璇︽儏
     */
    @Observed(name = "accessDevice.getDeviceDetail", contextualName = "access-device-get-detail")
    @GetMapping("/{deviceId}")
    @Operation(summary = "鏌ヨ璁惧璇︽儏", description = "鏍规嵁璁惧ID鏌ヨ璁惧璇︾粏淇℃伅")
    @PermissionCheck(value = "ACCESS_DEVICE_VIEW", description = "鏌ヨ璁惧璇︽儏")
    public ResponseDTO<net.lab1024.sa.access.domain.vo.AccessDeviceVO> getDeviceDetail(
            @Parameter(description = "璁惧ID", required = true) @PathVariable @NotNull Long deviceId) {
        log.info("[闂ㄧ璁惧] 鏌ヨ璁惧璇︽儏锛宒eviceId={}", deviceId);
        try {
            // 璋冪敤Service灞傛煡璇?            return accessDeviceService.getDeviceDetail(deviceId);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[闂ㄧ璁惧] 鏌ヨ璁惧璇︽儏鍙傛暟閿欒: deviceId={}, error={}", deviceId, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "鍙傛暟閿欒锛? + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[闂ㄧ璁惧] 鏌ヨ璁惧璇︽儏涓氬姟寮傚父: deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[闂ㄧ璁惧] 鏌ヨ璁惧璇︽儏绯荤粺寮傚父: deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("GET_DEVICE_SYSTEM_ERROR", "鏌ヨ璁惧璇︽儏澶辫触锛? + e.getMessage());
        } catch (Exception e) {
            log.error("[闂ㄧ璁惧] 鏌ヨ璁惧璇︽儏鏈煡寮傚父: deviceId={}", deviceId, e);
            return ResponseDTO.error("GET_DEVICE_ERROR", "鏌ヨ璁惧璇︽儏澶辫触: " + e.getMessage());
        }
    }

    /**
     * 娣诲姞璁惧
     *
     * @param form 璁惧娣诲姞琛ㄥ崟
     * @return 璁惧ID
     */
    @Observed(name = "accessDevice.addDevice", contextualName = "access-device-add")
    @PostMapping("/add")
    @Operation(summary = "娣诲姞璁惧", description = "娣诲姞鏂扮殑闂ㄧ璁惧")
    @PermissionCheck(value = "ACCESS_DEVICE_ADD", description = "娣诲姞闂ㄧ璁惧")
    public ResponseDTO<Long> addDevice(@Valid @RequestBody net.lab1024.sa.access.domain.form.AccessDeviceAddForm form) {
        log.info("[闂ㄧ璁惧] 娣诲姞璁惧锛宒eviceName={}, deviceCode={}", form.getDeviceName(), form.getDeviceCode());
        try {
            // 璋冪敤Service灞傛坊鍔?            return accessDeviceService.addDevice(form);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[闂ㄧ璁惧] 娣诲姞璁惧鍙傛暟閿欒: deviceName={}, deviceCode={}, error={}", form.getDeviceName(), form.getDeviceCode(), e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "鍙傛暟閿欒锛? + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[闂ㄧ璁惧] 娣诲姞璁惧涓氬姟寮傚父: deviceName={}, deviceCode={}, code={}, message={}", form.getDeviceName(), form.getDeviceCode(), e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[闂ㄧ璁惧] 娣诲姞璁惧绯荤粺寮傚父: deviceName={}, deviceCode={}, code={}, message={}", form.getDeviceName(), form.getDeviceCode(), e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("ADD_DEVICE_SYSTEM_ERROR", "娣诲姞璁惧澶辫触锛? + e.getMessage());
        } catch (Exception e) {
            log.error("[闂ㄧ璁惧] 娣诲姞璁惧鏈煡寮傚父: deviceName={}, deviceCode={}", form.getDeviceName(), form.getDeviceCode(), e);
            return ResponseDTO.error("ADD_DEVICE_ERROR", "娣诲姞璁惧澶辫触: " + e.getMessage());
        }
    }

    /**
     * 鏇存柊璁惧
     *
     * @param form 璁惧鏇存柊琛ㄥ崟
     * @return 鏄惁鎴愬姛
     */
    @Observed(name = "accessDevice.updateDevice", contextualName = "access-device-update")
    @PutMapping("/update")
    @Operation(summary = "鏇存柊璁惧", description = "鏇存柊闂ㄧ璁惧淇℃伅")
    @PermissionCheck(value = "ACCESS_DEVICE_UPDATE", description = "鏇存柊闂ㄧ璁惧")
    public ResponseDTO<Boolean> updateDevice(@Valid @RequestBody net.lab1024.sa.access.domain.form.AccessDeviceUpdateForm form) {
        log.info("[闂ㄧ璁惧] 鏇存柊璁惧锛宒eviceId={}", form.getDeviceId());
        try {
            // 璋冪敤Service灞傛洿鏂?            return accessDeviceService.updateDevice(form);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[闂ㄧ璁惧] 鏇存柊璁惧鍙傛暟閿欒: deviceId={}, error={}", form.getDeviceId(), e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "鍙傛暟閿欒锛? + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[闂ㄧ璁惧] 鏇存柊璁惧涓氬姟寮傚父: deviceId={}, code={}, message={}", form.getDeviceId(), e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[闂ㄧ璁惧] 鏇存柊璁惧绯荤粺寮傚父: deviceId={}, code={}, message={}", form.getDeviceId(), e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("UPDATE_DEVICE_SYSTEM_ERROR", "鏇存柊璁惧澶辫触锛? + e.getMessage());
        } catch (Exception e) {
            log.error("[闂ㄧ璁惧] 鏇存柊璁惧鏈煡寮傚父: deviceId={}", form.getDeviceId(), e);
            return ResponseDTO.error("UPDATE_DEVICE_ERROR", "鏇存柊璁惧澶辫触: " + e.getMessage());
        }
    }

    /**
     * 鍒犻櫎璁惧
     *
     * @param deviceId 璁惧ID
     * @return 鏄惁鎴愬姛
     */
    @Observed(name = "accessDevice.deleteDevice", contextualName = "access-device-delete")
    @DeleteMapping("/{deviceId}")
    @Operation(summary = "鍒犻櫎璁惧", description = "鍒犻櫎闂ㄧ璁惧锛堣蒋鍒犻櫎锛?)
    @PermissionCheck(value = "ACCESS_DEVICE_DELETE", description = "鍒犻櫎闂ㄧ璁惧")
    public ResponseDTO<Boolean> deleteDevice(
            @Parameter(description = "璁惧ID", required = true) @PathVariable @NotNull Long deviceId) {
        log.info("[闂ㄧ璁惧] 鍒犻櫎璁惧锛宒eviceId={}", deviceId);
        try {
            // 璋冪敤Service灞傚垹闄?            return accessDeviceService.deleteDevice(deviceId);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[闂ㄧ璁惧] 鍒犻櫎璁惧鍙傛暟閿欒: deviceId={}, error={}", deviceId, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "鍙傛暟閿欒锛? + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[闂ㄧ璁惧] 鍒犻櫎璁惧涓氬姟寮傚父: deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[闂ㄧ璁惧] 鍒犻櫎璁惧绯荤粺寮傚父: deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("DELETE_DEVICE_SYSTEM_ERROR", "鍒犻櫎璁惧澶辫触锛? + e.getMessage());
        } catch (Exception e) {
            log.error("[闂ㄧ璁惧] 鍒犻櫎璁惧鏈煡寮傚父: deviceId={}", deviceId, e);
            return ResponseDTO.error("DELETE_DEVICE_ERROR", "鍒犻櫎璁惧澶辫触: " + e.getMessage());
        }
    }

    /**
     * 鏇存柊璁惧鐘舵€?     *
     * @param deviceId 璁惧ID
     * @param status 璁惧鐘舵€?     * @return 鏄惁鎴愬姛
     */
    @Observed(name = "accessDevice.updateDeviceStatus", contextualName = "access-device-update-status")
    @PutMapping("/{deviceId}/status")
    @Operation(summary = "鏇存柊璁惧鐘舵€?, description = "鏇存柊闂ㄧ璁惧鐘舵€?)
    @PermissionCheck(value = "ACCESS_DEVICE_UPDATE", description = "鏇存柊璁惧鐘舵€?)
    public ResponseDTO<Boolean> updateDeviceStatus(
            @Parameter(description = "璁惧ID") @PathVariable @NotNull Long deviceId,
            @Parameter(description = "璁惧鐘舵€?) @RequestParam @NotNull Integer status) {
        log.info("[闂ㄧ璁惧] 鏇存柊璁惧鐘舵€侊紝deviceId={}, status={}", deviceId, status);
        try {
            // 璋冪敤Service灞傛洿鏂扮姸鎬?            return accessDeviceService.updateDeviceStatus(deviceId, status);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[闂ㄧ璁惧] 鏇存柊璁惧鐘舵€佸弬鏁伴敊璇? deviceId={}, status={}, error={}", deviceId, status, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "鍙傛暟閿欒锛? + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[闂ㄧ璁惧] 鏇存柊璁惧鐘舵€佷笟鍔″紓甯? deviceId={}, status={}, code={}, message={}", deviceId, status, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[闂ㄧ璁惧] 鏇存柊璁惧鐘舵€佺郴缁熷紓甯? deviceId={}, status={}, code={}, message={}", deviceId, status, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("UPDATE_DEVICE_STATUS_SYSTEM_ERROR", "鏇存柊璁惧鐘舵€佸け璐ワ細" + e.getMessage());
        } catch (Exception e) {
            log.error("[闂ㄧ璁惧] 鏇存柊璁惧鐘舵€佹湭鐭ュ紓甯? deviceId={}, status={}", deviceId, status, e);
            return ResponseDTO.error("UPDATE_DEVICE_STATUS_ERROR", "鏇存柊璁惧鐘舵€佸け璐? " + e.getMessage());
        }
    }
}


