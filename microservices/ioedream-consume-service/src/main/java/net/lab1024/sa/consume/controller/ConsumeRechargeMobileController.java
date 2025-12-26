package net.lab1024.sa.consume.controller;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.domain.form.ConsumeRechargeCreateForm;
import net.lab1024.sa.consume.domain.vo.ConsumeRechargeOrderVO;
import net.lab1024.sa.consume.service.ConsumeRechargeService;

/**
 * 移动端充值管理控制器
 * <p>
 * 提供移动端充值支付功能，包括：
 * 1. 创建充值订单
 * 2. 发起支付（微信/支付宝）
 * 3. 查询支付结果
 * 4. 支付回调处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@RestController
@RequestMapping("/api/v1/consume/mobile/recharge")
@Tag(name = "移动端充值管理", description = "移动端充值支付接口")
@Slf4j
public class ConsumeRechargeMobileController {

    @Resource
    private ConsumeRechargeService consumeRechargeService;

    @Operation(summary = "创建充值订单", description = "创建充值订单并返回支付参数")
    @PostMapping("/create")
    public ResponseDTO<Map<String, Object>> createRechargeOrder(@Valid @RequestBody ConsumeRechargeCreateForm form) {
        log.info("[移动端充值] 创建充值订单: userId={}, amount={}, paymentMethod={}",
            form.getUserId(), form.getRechargeAmount(), form.getPaymentMethod());
        try {
            Map<String, Object> result = consumeRechargeService.createRechargeOrder(form);
            log.info("[移动端充值] 创建充值订单成功: userId={}, orderId={}",
                form.getUserId(), result.get("orderId"));
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[移动端充值] 创建充值订单异常: userId={}, error={}",
                form.getUserId(), e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "查询支付结果", description = "查询订单支付状态")
    @GetMapping("/result/{orderId}")
    public ResponseDTO<Map<String, Object>> getPaymentResult(@PathVariable String orderId) {
        log.info("[移动端充值] 查询支付结果: orderId={}", orderId);
        try {
            Map<String, Object> result = consumeRechargeService.getPaymentResult(orderId);
            log.info("[移动端充值] 查询支付结果成功: orderId={}, status={}",
                orderId, result.get("status"));
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[移动端充值] 查询支付结果异常: orderId={}, error={}",
                orderId, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "取消充值订单", description = "取消未支付的充值订单")
    @PostMapping("/cancel/{orderId}")
    public ResponseDTO<Void> cancelRechargeOrder(@PathVariable String orderId) {
        log.info("[移动端充值] 取消充值订单: orderId={}", orderId);
        try {
            consumeRechargeService.cancelRechargeOrder(orderId);
            log.info("[移动端充值] 取消充值订单成功: orderId={}", orderId);
            return ResponseDTO.ok();
        } catch (Exception e) {
            log.error("[移动端充值] 取消充值订单异常: orderId={}, error={}",
                orderId, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "获取充值订单详情", description = "获取充值订单详细信息")
    @GetMapping("/detail/{orderId}")
    public ResponseDTO<ConsumeRechargeOrderVO> getRechargeOrderDetail(@PathVariable String orderId) {
        log.info("[移动端充值] 查询订单详情: orderId={}", orderId);
        try {
            ConsumeRechargeOrderVO order = consumeRechargeService.getRechargeOrderDetail(orderId);
            log.info("[移动端充值] 查询订单详情成功: orderId={}", orderId);
            return ResponseDTO.ok(order);
        } catch (Exception e) {
            log.error("[移动端充值] 查询订单详情异常: orderId={}, error={}",
                orderId, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "获取充值记录列表", description = "分页获取用户充值记录")
    @GetMapping("/records/{userId}")
    public ResponseDTO<Map<String, Object>> getRechargeRecords(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("[移动端充值] 查询充值记录: userId={}, pageNum={}, pageSize={}",
            userId, pageNum, pageSize);
        try {
            Map<String, Object> result = consumeRechargeService.getRechargeRecords(userId, pageNum, pageSize);
            log.info("[移动端充值] 查询充值记录成功: userId={}", userId);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[移动端充值] 查询充值记录异常: userId={}, error={}",
                userId, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "获取充值统计", description = "获取用户充值统计信息")
    @GetMapping("/statistics/{userId}")
    public ResponseDTO<Map<String, Object>> getRechargeStatistics(@PathVariable Long userId) {
        log.info("[移动端充值] 查询充值统计: userId={}", userId);
        try {
            Map<String, Object> statistics = consumeRechargeService.getRechargeStatistics(userId);
            log.info("[移动端充值] 查询充值统计成功: userId={}", userId);
            return ResponseDTO.ok(statistics);
        } catch (Exception e) {
            log.error("[移动端充值] 查询充值统计异常: userId={}, error={}",
                userId, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "微信支付回调", description = "处理微信支付回调通知")
    @PostMapping("/callback/wechat")
    public String wechatPayCallback(@RequestBody String notifyData) {
        log.info("[移动端充值] 微信支付回调: notifyData={}", notifyData);
        try {
            boolean success = consumeRechargeService.handleWechatPayCallback(notifyData);
            if (success) {
                // 返回微信要求的XML格式
                return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
            } else {
                return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[处理失败]]></return_msg></xml>";
            }
        } catch (Exception e) {
            log.error("[移动端充值] 微信支付回调异常: error={}", e.getMessage(), e);
            return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[系统异常]]></return_msg></xml>";
        }
    }

    @Operation(summary = "支付宝支付回调", description = "处理支付宝支付回调通知")
    @PostMapping("/callback/alipay")
    public String alipayCallback(@RequestBody String notifyData) {
        log.info("[移动端充值] 支付宝支付回调: notifyData={}", notifyData);
        try {
            boolean success = consumeRechargeService.handleAlipayCallback(notifyData);
            if (success) {
                return "success";
            } else {
                return "fail";
            }
        } catch (Exception e) {
            log.error("[移动端充值] 支付宝支付回调异常: error={}", e.getMessage(), e);
            return "fail";
        }
    }
}
