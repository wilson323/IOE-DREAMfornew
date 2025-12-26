package net.lab1024.sa.consume.controller;

import java.math.BigDecimal;
import java.util.List;
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

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.domain.form.ConsumeRefundApplyForm;
import net.lab1024.sa.consume.domain.vo.ConsumeRefundRecordVO;
import net.lab1024.sa.consume.service.ConsumeRefundService;

/**
 * 移动端退款管理控制器
 * <p>
 * 提供移动端退款功能，包括：
 * 1. 退款申请
 * 2. 退款记录查询
 * 3. 退款状态跟踪
 * 4. 退款取消
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@RestController
@RequestMapping("/api/v1/consume/mobile/refund")
@Tag(name = "移动端退款管理", description = "移动端退款接口")
@Slf4j
public class ConsumeRefundMobileController {

    @Resource
    private ConsumeRefundService consumeRefundService;

    @Operation(summary = "申请退款", description = "用户对消费记录申请退款")
    @PostMapping("/apply")
    public ResponseDTO<Map<String, Object>> applyRefund(@Valid @RequestBody ConsumeRefundApplyForm form) {
        log.info("[移动端退款] 申请退款: userId={}, consumeId={}, amount={}",
            form.getUserId(), form.getConsumeId(), form.getRefundAmount());
        try {
            Map<String, Object> result = consumeRefundService.applyRefundWithForm(form);
            log.info("[移动端退款] 申请退款成功: userId={}, refundId={}",
                form.getUserId(), result.get("refundId"));
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[移动端退款] 申请退款异常: userId={}, error={}",
                form.getUserId(), e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "获取退款记录列表", description = "分页获取用户退款记录")
    @GetMapping("/records/{userId}")
    public ResponseDTO<PageResult<ConsumeRefundRecordVO>> getRefundRecords(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Integer refundStatus) {
        log.info("[移动端退款] 查询退款记录: userId={}, pageNum={}, pageSize={}, status={}",
            userId, pageNum, pageSize, refundStatus);
        try {
            PageResult<ConsumeRefundRecordVO> records = consumeRefundService.getRefundRecords(
                userId, pageNum, pageSize, refundStatus);
            log.info("[移动端退款] 查询退款记录成功: userId={}, total={}",
                userId, records.getTotal());
            return ResponseDTO.ok(records);
        } catch (Exception e) {
            log.error("[移动端退款] 查询退款记录异常: userId={}, error={}",
                userId, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "查询退款状态", description = "查询指定退款记录的状态")
    @GetMapping("/status/{refundId}")
    public ResponseDTO<Map<String, Object>> getRefundStatus(@PathVariable Long refundId) {
        log.info("[移动端退款] 查询退款状态: refundId={}", refundId);
        try {
            Map<String, Object> status = consumeRefundService.getRefundStatus(refundId);
            log.info("[移动端退款] 查询退款状态成功: refundId={}", refundId);
            return ResponseDTO.ok(status);
        } catch (Exception e) {
            log.error("[移动端退款] 查询退款状态异常: refundId={}, error={}",
                refundId, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "取消退款申请", description = "用户取消待审核的退款申请")
    @PostMapping("/cancel/{refundId}")
    public ResponseDTO<Void> cancelRefund(@PathVariable Long refundId) {
        log.info("[移动端退款] 取消退款: refundId={}", refundId);
        try {
            consumeRefundService.cancelRefund(refundId);
            log.info("[移动端退款] 取消退款成功: refundId={}", refundId);
            return ResponseDTO.ok();
        } catch (Exception e) {
            log.error("[移动端退款] 取消退款异常: refundId={}, error={}",
                refundId, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "获取可退款消费记录", description = "获取用户可申请退款的消费记录")
    @GetMapping("/available/{userId}")
    public ResponseDTO<List<Map<String, Object>>> getAvailableRefundRecords(@PathVariable Long userId) {
        log.info("[移动端退款] 查询可退款记录: userId={}", userId);
        try {
            List<Map<String, Object>> records = consumeRefundService.getAvailableRefundRecords(userId);
            log.info("[移动端退款] 查询可退款记录成功: userId={}, count={}",
                userId, records.size());
            return ResponseDTO.ok(records);
        } catch (Exception e) {
            log.error("[移动端退款] 查询可退款记录异常: userId={}, error={}",
                userId, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "获取退款统计信息", description = "获取用户退款统计")
    @GetMapping("/statistics/{userId}")
    public ResponseDTO<Map<String, Object>> getRefundStatistics(@PathVariable Long userId) {
        log.info("[移动端退款] 查询退款统计: userId={}", userId);
        try {
            Map<String, Object> statistics = consumeRefundService.getRefundStatistics(userId);
            log.info("[移动端退款] 查询退款统计成功: userId={}", userId);
            return ResponseDTO.ok(statistics);
        } catch (Exception e) {
            log.error("[移动端退款] 查询退款统计异常: userId={}, error={}",
                userId, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "获取退款详情", description = "获取退款申请详细信息")
    @GetMapping("/detail/{refundId}")
    public ResponseDTO<ConsumeRefundRecordVO> getRefundDetail(@PathVariable Long refundId) {
        log.info("[移动端退款] 查询退款详情: refundId={}", refundId);
        try {
            ConsumeRefundRecordVO detail = consumeRefundService.getRefundDetail(refundId);
            log.info("[移动端退款] 查询退款详情成功: refundId={}", refundId);
            return ResponseDTO.ok(detail);
        } catch (Exception e) {
            log.error("[移动端退款] 查询退款详情异常: refundId={}, error={}",
                refundId, e.getMessage(), e);
            throw e;
        }
    }
}
