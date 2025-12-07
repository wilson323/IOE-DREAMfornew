package net.lab1024.sa.consume.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.dev33.satoken.annotation.SaCheckLogin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.domain.form.ConsumeMobileFaceForm;
import net.lab1024.sa.consume.domain.form.ConsumeMobileNfcForm;
import net.lab1024.sa.consume.domain.form.ConsumeOfflineSyncForm;
import net.lab1024.sa.consume.domain.form.ConsumePermissionValidateForm;
import net.lab1024.sa.consume.domain.form.ConsumeMobileQuickForm;
import net.lab1024.sa.consume.domain.form.ConsumeMobileScanForm;
import net.lab1024.sa.consume.domain.vo.ConsumeDeviceConfigVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileMealVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileResultVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileStatsVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileSummaryVO;
import net.lab1024.sa.consume.domain.vo.ConsumeSyncDataVO;
import net.lab1024.sa.consume.domain.vo.ConsumeSyncResultVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileUserInfoVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileUserStatsVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileUserVO;
import net.lab1024.sa.consume.domain.vo.ConsumeValidateResultVO;
import net.lab1024.sa.consume.service.ConsumeMobileService;
import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 消费移动端控制器
 * <p>
 * 提供移动端消费管理相关API
 * 严格遵循CLAUDE.md规范：
 * - 使用@RestController注解
 * - 使用@Resource依赖注入
 * - 使用@Valid参数校验
 * - 返回统一ResponseDTO格式
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/consume/mobile")
@Tag(name = "移动端消费管理", description = "移动端消费交易、查询、同步等API")
public class ConsumeMobileController {

    @Resource
    private ConsumeMobileService consumeMobileService;

    /**
     * 快速消费
     *
     * @param form 消费表单
     * @return 消费结果
     */
    @PostMapping("/transaction/quick")
    @Operation(summary = "快速消费", description = "移动端快速消费交易")
    @SaCheckLogin
    public ResponseDTO<ConsumeMobileResultVO> quickConsume(@Valid @RequestBody ConsumeMobileQuickForm form) {
        log.info("快速消费: deviceId={}, userId={}, amount={}", 
                form.getDeviceId(), form.getUserId(), form.getAmount());
        ConsumeMobileResultVO result = consumeMobileService.quickConsume(form);
        return ResponseDTO.ok(result);
    }

    /**
     * 扫码消费
     *
     * @param form 消费表单
     * @return 消费结果
     */
    @PostMapping("/transaction/scan")
    @Operation(summary = "扫码消费", description = "移动端扫码消费交易")
    @SaCheckLogin
    public ResponseDTO<ConsumeMobileResultVO> scanConsume(@Valid @RequestBody ConsumeMobileScanForm form) {
        log.info("扫码消费: deviceId={}, qrCode={}, amount={}", 
                form.getDeviceId(), form.getQrCode(), form.getAmount());
        ConsumeMobileResultVO result = consumeMobileService.scanConsume(form);
        return ResponseDTO.ok(result);
    }

    /**
     * NFC消费
     *
     * @param form 消费表单
     * @return 消费结果
     */
    @PostMapping("/transaction/nfc")
    @Operation(summary = "NFC消费", description = "移动端NFC消费交易")
    @SaCheckLogin
    public ResponseDTO<ConsumeMobileResultVO> nfcConsume(@Valid @RequestBody ConsumeMobileNfcForm form) {
        log.info("NFC消费: deviceId={}, cardNumber={}, amount={}", 
                form.getDeviceId(), form.getCardNumber(), form.getAmount());
        ConsumeMobileResultVO result = consumeMobileService.nfcConsume(form);
        return ResponseDTO.ok(result);
    }

    /**
     * 人脸识别消费
     *
     * @param form 消费表单
     * @return 消费结果
     */
    @PostMapping("/transaction/face")
    @Operation(summary = "人脸识别消费", description = "移动端人脸识别消费交易")
    @SaCheckLogin
    public ResponseDTO<ConsumeMobileResultVO> faceConsume(@Valid @RequestBody ConsumeMobileFaceForm form) {
        log.info("人脸识别消费: deviceId={}, faceFeatures={}, amount={}", 
                form.getDeviceId(), form.getFaceFeatures(), form.getAmount());
        ConsumeMobileResultVO result = consumeMobileService.faceConsume(form);
        return ResponseDTO.ok(result);
    }

    /**
     * 快速用户查询
     *
     * @param queryType 查询类型
     * @param queryValue 查询值
     * @return 用户信息
     */
    @GetMapping("/user/quick")
    @Operation(summary = "快速用户查询", description = "根据类型快速查询用户信息")
    @SaCheckLogin
    public ResponseDTO<ConsumeMobileUserVO> quickUserInfo(
            @RequestParam String queryType,
            @RequestParam String queryValue) {
        log.info("快速用户查询: queryType={}, queryValue={}", queryType, queryValue);
        ConsumeMobileUserVO userVO = consumeMobileService.quickUserInfo(queryType, queryValue);
        return ResponseDTO.ok(userVO);
    }

    /**
     * 获取用户消费信息
     *
     * @param userId 用户ID
     * @return 用户消费信息
     */
    @GetMapping("/user/consume-info/{userId}")
    @Operation(summary = "获取用户消费信息", description = "获取指定用户的消费信息")
    @SaCheckLogin
    public ResponseDTO<ConsumeMobileUserInfoVO> getUserConsumeInfo(@PathVariable Long userId) {
        log.info("获取用户消费信息: userId={}", userId);
        ConsumeMobileUserInfoVO userInfo = consumeMobileService.getUserConsumeInfo(userId);
        return ResponseDTO.ok(userInfo);
    }

    /**
     * 获取有效餐别
     *
     * @return 餐别列表
     */
    @GetMapping("/meal/available")
    @Operation(summary = "获取有效餐别", description = "获取当前有效的餐别列表")
    @SaCheckLogin
    public ResponseDTO<List<ConsumeMobileMealVO>> getAvailableMeals() {
        log.info("获取有效餐别");
        List<ConsumeMobileMealVO> meals = consumeMobileService.getAvailableMeals();
        return ResponseDTO.ok(meals);
    }

    /**
     * 获取设备配置
     *
     * @param deviceId 设备ID
     * @return 设备配置
     */
    @GetMapping("/device/config/{deviceId}")
    @Operation(summary = "获取设备配置", description = "获取指定消费设备的配置信息")
    @SaCheckLogin
    public ResponseDTO<ConsumeDeviceConfigVO> getDeviceConfig(@PathVariable Long deviceId) {
        log.info("获取设备配置: deviceId={}", deviceId);
        ConsumeDeviceConfigVO config = consumeMobileService.getDeviceConfig(deviceId);
        return ResponseDTO.ok(config);
    }

    /**
     * 获取设备今日统计
     *
     * @param deviceId 设备ID
     * @return 统计数据
     */
    @GetMapping("/device/today-stats/{deviceId}")
    @Operation(summary = "获取设备今日统计", description = "获取指定设备今日的消费统计数据")
    @SaCheckLogin
    public ResponseDTO<ConsumeMobileStatsVO> getDeviceTodayStats(@PathVariable Long deviceId) {
        log.info("获取设备今日统计: deviceId={}", deviceId);
        ConsumeMobileStatsVO stats = consumeMobileService.getDeviceTodayStats(deviceId);
        return ResponseDTO.ok(stats);
    }

    /**
     * 获取实时交易汇总
     *
     * @param areaId 区域ID
     * @return 交易汇总
     */
    @GetMapping("/transaction/summary")
    @Operation(summary = "获取实时交易汇总", description = "获取指定区域的实时交易汇总")
    @SaCheckLogin
    public ResponseDTO<ConsumeMobileSummaryVO> getTransactionSummary(@RequestParam String areaId) {
        log.info("获取实时交易汇总: areaId={}", areaId);
        ConsumeMobileSummaryVO summary = consumeMobileService.getTransactionSummary(areaId);
        return ResponseDTO.ok(summary);
    }

    /**
     * 离线交易同步
     *
     * @param form 同步表单
     * @return 同步结果
     */
    @PostMapping("/sync/offline")
    @Operation(summary = "离线交易同步", description = "同步移动端离线消费交易数据")
    @SaCheckLogin
    public ResponseDTO<ConsumeSyncResultVO> syncOfflineTransactions(@Valid @RequestBody ConsumeOfflineSyncForm form) {
        log.info("离线交易同步: deviceId={}", form.getDeviceId());
        ConsumeSyncResultVO result = consumeMobileService.syncOfflineTransactions(form);
        return ResponseDTO.ok(result);
    }

    /**
     * 获取同步数据
     *
     * @param deviceId 设备ID
     * @return 同步数据
     */
    @GetMapping("/sync/data/{deviceId}")
    @Operation(summary = "获取同步数据", description = "获取需要同步到移动端的数据")
    @SaCheckLogin
    public ResponseDTO<ConsumeSyncDataVO> getSyncData(@PathVariable Long deviceId) {
        log.info("获取同步数据: deviceId={}", deviceId);
        ConsumeSyncDataVO syncData = consumeMobileService.getSyncData(deviceId, null);
        return ResponseDTO.ok(syncData);
    }

    /**
     * 权限验证
     *
     * @param form 验证表单
     * @return 验证结果
     */
    @PostMapping("/validate/permission")
    @Operation(summary = "权限验证", description = "验证用户消费权限")
    @SaCheckLogin
    public ResponseDTO<ConsumeValidateResultVO> validatePermission(@Valid @RequestBody ConsumePermissionValidateForm form) {
        log.info("权限验证: userId={}, areaId={}, amount={}", 
                form.getUserId(), form.getAreaId(), form.getAmount());
        ConsumeValidateResultVO result = consumeMobileService.validateConsumePermission(form);
        return ResponseDTO.ok(result);
    }

    /**
     * 获取用户统计
     * <p>
     * 获取指定用户的消费统计数据，包括总交易笔数、总金额、今日统计、本月统计
     * </p>
     *
     * @param userId 用户ID
     * @return 统计数据
     */
    @GetMapping("/stats/{userId}")
    @Operation(summary = "获取用户统计", description = "获取指定用户的消费统计数据")
    @SaCheckLogin
    public ResponseDTO<ConsumeMobileUserStatsVO> getUserStats(@PathVariable Long userId) {
        log.info("获取用户统计: userId={}", userId);
        ConsumeMobileUserStatsVO stats = consumeMobileService.getUserStats(userId);
        return ResponseDTO.ok(stats);
    }
}
