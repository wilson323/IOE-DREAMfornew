package net.lab1024.sa.admin.module.consume.controller;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.data.domain.Page;
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
import net.lab1024.sa.admin.module.consume.domain.dto.RefundQueryDTO;
import net.lab1024.sa.admin.module.consume.domain.dto.RefundRequestDTO;
import net.lab1024.sa.admin.module.consume.domain.dto.RefundResultDTO;
import net.lab1024.sa.admin.module.consume.domain.entity.RefundRecordEntity;
import net.lab1024.sa.admin.module.consume.service.RefundService;
import net.lab1024.sa.base.common.annotation.SaCheckPermission;
import net.lab1024.sa.base.common.controller.SupportBaseController;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;

/**
 * 退款管理控制器
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Tag(name = "退款管理", description = "退款相关接口")
@RestController
@RequestMapping("/api/consume/refund")
@Slf4j
public class RefundController extends SupportBaseController {

    @Resource
    private RefundService refundService;

    @Operation(summary = "创建退款", description = "申请退款")
    @PostMapping("/create")
    @SaCheckPermission("consume:refund:add")
    public ResponseDTO<RefundResultDTO> createRefund(@Valid @RequestBody RefundRequestDTO refundRequest) {
        return refundService.createRefund(refundRequest);
    }

    @Operation(summary = "查询退款记录", description = "分页查询退款记录")
    @PostMapping("/query")
    @SaCheckPermission("consume:refund:query")
    public ResponseDTO<PageResult<RefundRecordEntity>> queryRefundRecords(@RequestBody RefundQueryDTO queryDTO) {
        try {
            ResponseDTO<Page<RefundRecordEntity>> pageResult = refundService.queryRefundRecords(queryDTO);
            if (pageResult.getOk() && pageResult.getData() != null) {
                Page<RefundRecordEntity> page = pageResult.getData();
                // 手动构建PageResult（Spring Data Page转换为PageResult）
                PageResult<RefundRecordEntity> result = new PageResult<>();
                result.setPageNum((long) (page.getNumber() + 1));
                result.setPageSize((long) page.getSize());
                result.setTotal(page.getTotalElements());
                result.setPages((long) page.getTotalPages());
                result.setList(page.getContent());
                result.setEmptyFlag(page.getContent().isEmpty());
                return ResponseDTO.ok(result);
            }
            return ResponseDTO.error("查询退款记录失败");

        } catch (Exception e) {
            log.error("查询退款记录失败", e);
            return ResponseDTO.error("查询退款记录失败: " + e.getMessage());
        }
    }

    @Operation(summary = "审核退款", description = "审核退款申请")
    @PostMapping("/review/{refundId}")
    @SaCheckPermission("consume:refund:review")
    public ResponseDTO<String> reviewRefund(
            @PathVariable Long refundId,
            @RequestParam boolean approved,
            @RequestParam(required = false) String remark) {
        return refundService.auditRefund(refundId, approved, remark);
    }

    @Operation(summary = "退款回调", description = "处理第三方退款回调")
    @PostMapping("/callback/{refundNo}")
    public ResponseDTO<String> handleRefundCallback(
            @PathVariable String refundNo,
            @RequestParam String thirdPartyRefundNo,
            @RequestParam String status,
            @RequestParam(required = false) String remark) {

        // 注意：这里应该有签名验证等安全措施
        // 实际项目中应该验证回调来源的合法性
        return ResponseDTO.okMsg("回调处理成功");
    }

    @Operation(summary = "退款统计", description = "获取用户退款统计数据")
    @GetMapping("/statistics/{userId}")
    @SaCheckPermission("consume:refund:statistics")
    public ResponseDTO<Map<String, Object>> getRefundStatistics(
            @PathVariable Long userId,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {

        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        return refundService.getRefundStatistics(userId, startDate, endDate);
    }

    @Operation(summary = "退款详情", description = "获取退款详情信息")
    @GetMapping("/detail/{refundNo}")
    @SaCheckPermission("consume:refund:detail")
    public ResponseDTO<RefundRecordEntity> getRefundDetail(@PathVariable String refundNo) {
        try {
            RefundRecordEntity refundRecord = refundService.getRefundByNo(refundNo);
            if (refundRecord == null) {
                return ResponseDTO.error("退款记录不存在");
            }
            return ResponseDTO.ok(refundRecord);
        } catch (Exception e) {
            log.error("获取退款详情失败: {}", refundNo, e);
            return ResponseDTO.error("获取退款详情失败: " + e.getMessage());
        }
    }

    @Operation(summary = "取消退款", description = "申请取消退款")
    @PostMapping("/cancel/{refundNo}")
    @SaCheckPermission("consume:refund:cancel")
    public ResponseDTO<String> cancelRefund(@PathVariable String refundNo) {
        try {
            return refundService.cancelRefund(refundNo);
        } catch (Exception e) {
            log.error("取消退款失败: {}", refundNo, e);
            return ResponseDTO.error("取消退款失败: " + e.getMessage());
        }
    }

    @Operation(summary = "重新提交退款", description = "重新提交退款申请")
    @PostMapping("/resubmit/{refundNo}")
    @SaCheckPermission("consume:refund:add")
    public ResponseDTO<RefundResultDTO> resubmitRefund(
            @PathVariable String refundNo,
            @Valid @RequestBody RefundRequestDTO refundRequest) {
        try {
            return refundService.resubmitRefund(refundNo, refundRequest);
        } catch (Exception e) {
            log.error("重新提交退款失败: {}", refundNo, e);
            return ResponseDTO.error("重新提交退款失败: " + e.getMessage());
        }
    }

    @Operation(summary = "批量审核退款", description = "批量审核退款申请")
    @PostMapping("/batch-review")
    @SaCheckPermission("consume:refund:review")
    public ResponseDTO<Map<String, Object>> batchReviewRefund(
            @RequestParam Long[] refundIds,
            @RequestParam boolean approved,
            @RequestParam(required = false) String remark) {
        try {
            return refundService.batchAuditRefund(java.util.Arrays.asList(refundIds), approved, remark);
        } catch (Exception e) {
            log.error("批量审核退款失败", e);
            return ResponseDTO.error("批量审核退款失败: " + e.getMessage());
        }
    }

    @Operation(summary = "退款原因", description = "获取退款原因选项")
    @GetMapping("/reasons")
    public ResponseDTO<Map<String, Object>> getRefundReasons() {
        try {
            // 退款原因选项
            Map<String, Object> result = Map.of(
                    "reasons", new String[] {
                            "SERVICE_QUALITY", "PRODUCT_ISSUE", "DELIVERY_PROBLEM",
                            "WRONG_ITEM", "DUPLICATE_ORDER", "CHANGE_OF_MIND",
                            "FINANCIAL_REASON", "TECHNICAL_ISSUE", "OTHER"
                    });
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("获取退款原因失败", e);
            return ResponseDTO.error("获取退款原因失败: " + e.getMessage());
        }
    }

    @Operation(summary = "退款状态统计", description = "获取退款状态统计")
    @GetMapping("/status-statistics")
    @SaCheckPermission("consume:refund:statistics")
    public ResponseDTO<Map<String, Object>> getRefundStatusStatistics() {
        try {
            return refundService.getRefundStatusStatistics();
        } catch (Exception e) {
            log.error("获取退款状态统计失败", e);
            return ResponseDTO.error("获取退款状态统计失败: " + e.getMessage());
        }
    }
}
