package net.lab1024.sa.consume.controller;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.domain.form.ConsumeMobileFaceForm;
import net.lab1024.sa.consume.domain.form.ConsumeMobileNfcForm;
import net.lab1024.sa.consume.domain.form.ConsumeMobileQuickForm;
import net.lab1024.sa.consume.domain.form.ConsumeMobileScanForm;
import net.lab1024.sa.consume.domain.form.ConsumeOfflineSyncForm;
import net.lab1024.sa.consume.domain.form.ConsumePermissionValidateForm;
import net.lab1024.sa.consume.domain.vo.ConsumeDeviceConfigVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileMealVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileResultVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileStatsVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileSummaryVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileUserInfoVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileUserVO;
import net.lab1024.sa.consume.domain.vo.ConsumeSyncDataVO;
import net.lab1024.sa.consume.domain.vo.ConsumeSyncResultVO;
import net.lab1024.sa.consume.domain.vo.ConsumeValidateResultVO;
import net.lab1024.sa.consume.service.ConsumeMobileService;

/**
 * 移动端消费控制器
 *
 * @author IOE-DREAM Team
 * @since 2025-12-22
 */
@RestController
@RequestMapping("/api/v1/consume/mobile")
@Tag(name = "移动端消费管理", description = "移动端消费交易、用户信息与同步接口")
@Slf4j
public class ConsumeMobileController {

    @Resource
    private ConsumeMobileService consumeMobileService;

    @Operation(summary = "快速消费", description = "移动端快速消费交易")
    @PostMapping("/transaction/quick")
    public ResponseDTO<ConsumeMobileResultVO> quickConsume(@Valid @RequestBody ConsumeMobileQuickForm form) {
        log.info("[移动端消费] 快速消费, deviceId={}, userId={}", form.getDeviceId(), form.getUserId());
        ConsumeMobileResultVO result = consumeMobileService.quickConsume(form);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "扫码消费", description = "移动端扫码消费交易")
    @PostMapping("/transaction/scan")
    public ResponseDTO<ConsumeMobileResultVO> scanConsume(@Valid @RequestBody ConsumeMobileScanForm form) {
        log.info("[移动端消费] 扫码消费, deviceId={}, qrCode={}", form.getDeviceId(), form.getQrCode());
        ConsumeMobileResultVO result = consumeMobileService.scanConsume(form);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "NFC消费", description = "移动端NFC消费交易")
    @PostMapping("/transaction/nfc")
    public ResponseDTO<ConsumeMobileResultVO> nfcConsume(@Valid @RequestBody ConsumeMobileNfcForm form) {
        log.info("[移动端消费] NFC消费, deviceId={}, cardNumber={}", form.getDeviceId(), form.getCardNumber());
        ConsumeMobileResultVO result = consumeMobileService.nfcConsume(form);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "人脸识别消费", description = "移动端人脸识别消费交易")
    @PostMapping("/transaction/face")
    public ResponseDTO<ConsumeMobileResultVO> faceConsume(@Valid @RequestBody ConsumeMobileFaceForm form) {
        log.info("[移动端消费] 人脸识别消费, deviceId={}", form.getDeviceId());
        ConsumeMobileResultVO result = consumeMobileService.faceConsume(form);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "快速用户查询", description = "根据类型快速查询用户信息")
    @GetMapping("/user/quick")
    public ResponseDTO<ConsumeMobileUserVO> quickUserInfo(
            @RequestParam String queryType,
            @RequestParam String queryValue) {
        log.info("[移动端消费] 快速用户查询, queryType={}, queryValue={}", queryType, queryValue);
        ConsumeMobileUserVO result = consumeMobileService.quickUserInfo(queryType, queryValue);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "获取用户消费信息", description = "获取指定用户消费信息")
    @GetMapping("/user/consume-info/{userId}")
    public ResponseDTO<ConsumeMobileUserInfoVO> getUserConsumeInfo(@PathVariable Long userId) {
        log.info("[移动端消费] 获取用户消费信息, userId={}", userId);
        ConsumeMobileUserInfoVO result = consumeMobileService.getUserConsumeInfo(userId);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "获取有效餐别", description = "获取当前有效餐别列表")
    @GetMapping("/meal/available")
    public ResponseDTO<List<ConsumeMobileMealVO>> getAvailableMeals() {
        log.info("[移动端消费] 获取有效餐别");
        List<ConsumeMobileMealVO> result = consumeMobileService.getAvailableMeals();
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "获取设备配置", description = "获取指定设备配置")
    @GetMapping("/device/config/{deviceId}")
    public ResponseDTO<ConsumeDeviceConfigVO> getDeviceConfig(@PathVariable Long deviceId) {
        log.info("[移动端消费] 获取设备配置, deviceId={}", deviceId);
        ConsumeDeviceConfigVO result = consumeMobileService.getDeviceConfig(deviceId);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "获取设备今日统计", description = "获取指定设备今日消费统计")
    @GetMapping("/device/today-stats/{deviceId}")
    public ResponseDTO<ConsumeMobileStatsVO> getDeviceTodayStats(@PathVariable Long deviceId) {
        log.info("[移动端消费] 获取设备今日统计, deviceId={}", deviceId);
        ConsumeMobileStatsVO result = consumeMobileService.getDeviceTodayStats(deviceId);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "获取实时交易汇总", description = "获取指定区域实时交易汇总")
    @GetMapping("/transaction/summary")
    public ResponseDTO<ConsumeMobileSummaryVO> getTransactionSummary(
            @RequestParam(required = false) String areaId) {
        log.info("[移动端消费] 获取实时交易汇总, areaId={}", areaId);
        ConsumeMobileSummaryVO result = consumeMobileService.getTransactionSummary(areaId);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "离线交易同步", description = "同步移动端离线消费交易")
    @PostMapping("/sync/offline")
    public ResponseDTO<ConsumeSyncResultVO> syncOfflineTransactions(@Valid @RequestBody ConsumeOfflineSyncForm form) {
        log.info("[移动端消费] 离线交易同步, deviceId={}", form.getDeviceId());
        ConsumeSyncResultVO result = consumeMobileService.syncOfflineTransactions(form);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "获取同步数据", description = "获取移动端同步数据")
    @GetMapping("/sync/data/{deviceId}")
    public ResponseDTO<ConsumeSyncDataVO> getSyncData(
            @PathVariable Long deviceId,
            @RequestParam(required = false) String syncTime) {
        log.info("[移动端消费] 获取同步数据, deviceId={}, syncTime={}", deviceId, syncTime);
        ConsumeSyncDataVO result = consumeMobileService.getSyncData(deviceId, syncTime);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "权限验证", description = "验证用户消费权限")
    @PostMapping("/validate/permission")
    public ResponseDTO<ConsumeValidateResultVO> validatePermission(
            @Valid @RequestBody ConsumePermissionValidateForm form) {
        log.info("[移动端消费] 权限验证, userId={}, areaId={}", form.getUserId(), form.getAreaId());
        ConsumeValidateResultVO result = consumeMobileService.validateConsumePermission(form);
        return ResponseDTO.ok(result);
    }
}