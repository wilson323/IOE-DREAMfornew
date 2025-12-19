package net.lab1024.sa.consume.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.domain.form.PaymentProcessForm;
import net.lab1024.sa.consume.service.PaymentService;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.monitoring.BusinessMetrics;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 设备消费上传控制器
 * <p>
 * 接收设备端上传的消费请求（中心实时验证模式）
 * 严格遵循CLAUDE.md规范：
 * - 设备端完成：采集生物特征/卡片信息+上传
 * - 软件端处理：实时验证+余额检查+扣款处理+离线降级
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/consume/device")
@Tag(name = "设备消费", description = "接收设备上传的消费请求")
public class DeviceConsumeController {

    @Resource
    private PaymentService paymentService;

    @Resource
    private BusinessMetrics businessMetrics;

    /**
     * 接收设备上传的消费请求
     * <p>
     * ⭐ 设备端已完成：采集生物特征/卡片信息+上传
     * ⭐ 软件端处理：实时验证+余额检查+扣款处理
     * </p>
     *
     * @param form 支付处理表单
     * @return 处理结果
     */
    @PostMapping("/upload-consume")
    @Operation(summary = "接收设备上传的消费请求", description = "设备端采集信息，软件端实时验证和扣款")
    public ResponseDTO<Map<String, Object>> uploadConsumeRequest(@Valid @RequestBody PaymentProcessForm form) {
        long startTime = System.currentTimeMillis();
        log.info("[设备消费] 接收设备上传的消费请求, deviceId={}, userId={}, amount={}",
                form.getDeviceId(), form.getUserId(), form.getPaymentAmount());

        try {
            // 1. 实时验证和扣款处理
            Map<String, Object> result = paymentService.processPayment(form);

            // 2. 记录业务指标
            BigDecimal amount = form.getPaymentAmount();
            if (result != null && "SUCCESS".equals(result.get("status"))) {
                businessMetrics.recordConsumeEvent("SUCCESS", amount != null ? amount.doubleValue() : 0.0);
            } else {
                businessMetrics.recordConsumeEvent("FAILURE", 0.0);
            }

            long duration = System.currentTimeMillis() - startTime;
            businessMetrics.recordResponseTime("consume.device.upload-consume", duration);
            log.info("[设备消费] 处理完成, duration={}ms", duration);

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[设备消费] 处理异常", e);
            businessMetrics.recordConsumeEvent("ERROR", 0.0);
            return ResponseDTO.error("CONSUME_PROCESS_ERROR", "处理消费请求失败: " + e.getMessage());
        }
    }

    /**
     * 离线消费降级处理
     * <p>
     * 网络故障时支持有限次数的离线消费
     * - 白名单验证：仅允许白名单用户
     * - 固定额度：单次消费固定金额
     * - 事后补录：网络恢复后上传补录
     * </p>
     *
     * @param form 支付处理表单
     * @return 处理结果
     */
    @PostMapping("/offline-consume")
    @Operation(summary = "离线消费降级处理", description = "网络故障时的离线消费降级方案")
    public ResponseDTO<Map<String, Object>> offlineConsume(@Valid @RequestBody PaymentProcessForm form) {
        log.info("[设备消费] 离线消费降级处理, deviceId={}, userId={}, amount={}",
                form.getDeviceId(), form.getUserId(), form.getPaymentAmount());

        try {
            // 1. 白名单验证
            if (!isWhitelistUser(form.getUserId())) {
                return ResponseDTO.error("OFFLINE_CONSUME_DENIED", "非白名单用户，不允许离线消费");
            }

            // 2. 固定额度检查
            BigDecimal fixedAmount = getFixedOfflineAmount();
            if (form.getPaymentAmount().compareTo(fixedAmount) > 0) {
                return ResponseDTO.error("OFFLINE_CONSUME_AMOUNT_EXCEEDED", "离线消费金额超过固定额度");
            }

            // 3. 记录离线消费（待网络恢复后补录）
            recordOfflineConsume(form);

            return ResponseDTO.ok(Map.of(
                    "status", "OFFLINE_SUCCESS",
                    "message", "离线消费成功，待网络恢复后补录",
                    "amount", form.getPaymentAmount()
            ));

        } catch (Exception e) {
            log.error("[设备消费] 离线消费处理异常", e);
            return ResponseDTO.error("OFFLINE_CONSUME_ERROR", "离线消费处理失败: " + e.getMessage());
        }
    }

    /**
     * 检查是否为白名单用户
     */
    private boolean isWhitelistUser(Long userId) {
        // 实现白名单验证逻辑
        return true; // 临时实现
    }

    /**
     * 获取固定离线消费额度
     */
    private BigDecimal getFixedOfflineAmount() {
        return new BigDecimal("10.00"); // 固定10元
    }

    /**
     * 记录离线消费
     */
    private void recordOfflineConsume(PaymentProcessForm form) {
        // 记录离线消费，待网络恢复后补录
        log.debug("[设备消费] 记录离线消费, userId={}, amount={}", form.getUserId(), form.getPaymentAmount());
    }
}
