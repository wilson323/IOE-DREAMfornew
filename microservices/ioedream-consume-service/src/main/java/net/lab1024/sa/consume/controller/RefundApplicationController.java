package net.lab1024.sa.consume.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.consume.entity.RefundApplicationEntity;
import net.lab1024.sa.consume.domain.form.RefundApplicationForm;
import net.lab1024.sa.consume.service.refund.RefundApplicationService;

/**
 * 退款申请控制器
 * <p>
 * 提供退款申请相关API接口
 * 严格遵循CLAUDE.md规范：
 * - Controller层负责接收请求、参数验证、返回响应
 * - 使用@Resource注入Service
 * - 使用@Valid进行参数验证
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/consume/refund")
@Tag(name = "退款申请管理")
public class RefundApplicationController {

    @Resource
    private RefundApplicationService refundApplicationService;

    /**
     * 提交退款申请
     *
     * @param form 退款申请表单
     * @return 退款申请实体
     */
    @PostMapping("/submit")
    @Observed(name = "refundApplication.submitRefundApplication", contextualName = "refund-application-submit")
    @Operation(summary = "提交退款申请", description = "提交退款申请并启动审批流程")
    public ResponseDTO<RefundApplicationEntity> submitRefundApplication(
            @Valid @RequestBody RefundApplicationForm form) {
        log.info("[退款申请] 接收退款申请请求，userId={}, paymentRecordId={}",
                form.getUserId(), form.getPaymentRecordId());
        RefundApplicationEntity entity = refundApplicationService.submitRefundApplication(form);
        return ResponseDTO.ok(entity);
    }

    /**
     * 更新退款申请状态（供审批结果监听器调用）
     *
     * @param refundNo 退款申请编号
     * @param requestParams 请求参数（包含status和approvalComment）
     * @return 操作结果
     */
    @PutMapping("/{refundNo}/status")
    @Observed(name = "refundApplication.updateRefundStatus", contextualName = "refund-application-update-status")
    @Operation(summary = "更新退款申请状态", description = "由审批结果监听器调用，更新退款申请状态")
    public ResponseDTO<Void> updateRefundStatus(
            @PathVariable String refundNo,
            @RequestBody Map<String, Object> requestParams) {
        log.info("[退款申请] 接收状态更新请求，refundNo={}", refundNo);
        String status = (String) requestParams.get("status");
        String approvalComment = (String) requestParams.get("approvalComment");
        refundApplicationService.updateRefundStatus(refundNo, status, approvalComment);
        return ResponseDTO.ok();
    }
}




