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
import net.lab1024.sa.consume.entity.ReimbursementApplicationEntity;
import net.lab1024.sa.consume.domain.form.ReimbursementApplicationForm;
import net.lab1024.sa.consume.service.reimbursement.ReimbursementApplicationService;

/**
 * 报销申请控制器
 * <p>
 * 提供报销申请相关API接口
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
@RequestMapping("/api/v1/consume/reimbursement")
@Tag(name = "报销申请管理")
public class ReimbursementApplicationController {

    @Resource
    private ReimbursementApplicationService reimbursementApplicationService;

    /**
     * 提交报销申请
     *
     * @param form 报销申请表单
     * @return 报销申请实体
     */
    @PostMapping("/submit")
    @Observed(name = "reimbursement.submitReimbursementApplication", contextualName = "reimbursement-submit-application")
    @Operation(summary = "提交报销申请", description = "提交报销申请并启动审批流程")
    public ResponseDTO<ReimbursementApplicationEntity> submitReimbursementApplication(
            @Valid @RequestBody ReimbursementApplicationForm form) {
        log.info("[报销申请] 接收报销申请请求，userId={}, type={}",
                form.getUserId(), form.getReimbursementType());
        ReimbursementApplicationEntity entity = reimbursementApplicationService.submitReimbursementApplication(form);
        return ResponseDTO.ok(entity);
    }

    /**
     * 更新报销申请状态（供审批结果监听器调用）
     *
     * @param reimbursementNo 报销申请编号
     * @param requestParams 请求参数（包含status和approvalComment）
     * @return 操作结果
     */
    @PutMapping("/{reimbursementNo}/status")
    @Observed(name = "reimbursement.updateReimbursementStatus", contextualName = "reimbursement-update-status")
    @Operation(summary = "更新报销申请状态", description = "由审批结果监听器调用，更新报销申请状态")
    public ResponseDTO<Void> updateReimbursementStatus(
            @PathVariable String reimbursementNo,
            @RequestBody Map<String, Object> requestParams) {
        log.info("[报销申请] 接收状态更新请求，reimbursementNo={}", reimbursementNo);
        String status = (String) requestParams.get("status");
        String approvalComment = (String) requestParams.get("approvalComment");
        reimbursementApplicationService.updateReimbursementStatus(reimbursementNo, status, approvalComment);
        return ResponseDTO.ok();
    }
}




