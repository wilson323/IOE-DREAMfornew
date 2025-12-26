package net.lab1024.sa.oa.workflow.mobile;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.oa.workflow.mobile.domain.*;
import net.lab1024.sa.oa.workflow.mobile.service.MobileApprovalService;
import net.lab1024.sa.oa.workflow.mobile.service.SpeechToTextService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 移动端审批控制器
 *
 * 提供移动端专属的审批功能，包括：
 * 1. 待办列表（分页、下拉刷新）
 * 2. 快速审批（一键操作）
 * 3. 批量审批
 * 4. 语音审批（语音转文字）
 * 5. 移动端优化的详情页
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/oa/mobile/approval")
@Validated
public class MobileApprovalController {

    @Resource
    private MobileApprovalService mobileApprovalService;

    @Resource
    private SpeechToTextService speechToTextService;

    // ==================== 待办列表 ====================

    /**
     * 获取待办列表（移动端优化）
     * - 分页加载（每页20条）
     * - 只返回必要字段
     * - 支持下拉刷新
     * - 按优先级、时间排序
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param sortBy 排序字段（createTime, priority, dueTime）
     * @return 待办列表
     */
    @GetMapping("/pending-tasks")
    public ResponseDTO<PageResult<MobileTaskVO>> getPendingTasks(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(defaultValue = "createTime") String sortBy) {

        log.info("[移动端审批] 获取待办列表: pageNum={}, pageSize={}, sortBy={}",
                pageNum, pageSize, sortBy);

        PageResult<MobileTaskVO> result = mobileApprovalService.getPendingTasks(
                pageNum,
                pageSize,
                sortBy
        );

        return ResponseDTO.ok(result);
    }

    /**
     * 获取待办数量统计
     *
     * @return 统计信息
     */
    @GetMapping("/pending-count")
    public ResponseDTO<PendingTaskStatistics> getPendingCount() {

        PendingTaskStatistics stats = mobileApprovalService.getPendingCount();

        return ResponseDTO.ok(stats);
    }

    // ==================== 快速审批 ====================

    /**
     * 快速审批（一键操作）
     * - 同意
     * - 拒绝
     * - 转办
     *
     * @param request 快速审批请求
     * @return 操作结果
     */
    @PostMapping("/quick-approve")
    public ResponseDTO<QuickApprovalResult> quickApprove(
            @RequestBody @Valid QuickApprovalRequest request) {

        log.info("[移动端审批] 快速审批: taskId={}, action={}",
                request.getTaskId(), request.getAction());

        QuickApprovalResult result = mobileApprovalService.quickApprove(request);

        return ResponseDTO.ok(result);
    }

    /**
     * 批量审批
     * - 批量同意
     * - 批量拒绝
     * - 批量转办
     *
     * @param request 批量审批请求
     * @return 批量审批结果
     */
    @PostMapping("/batch-approve")
    public ResponseDTO<BatchApprovalResult> batchApprove(
            @RequestBody @Valid BatchApprovalRequest request) {

        log.info("[移动端审批] 批量审批: taskCount={}, action={}",
                request.getTaskIds().size(), request.getAction());

        BatchApprovalResult result = mobileApprovalService.batchApprove(request);

        return ResponseDTO.ok(result);
    }

    // ==================== 语音审批 ====================

    /**
     * 语音审批（语音转文字）
     * - 支持多种语言
     * - 自动识别审批意见
     * - 支持语音文件和实时语音
     *
     * @param taskId 任务ID
     * @param voiceFile 语音文件
     * @return 操作结果
     */
    @PostMapping("/voice-approve")
    public ResponseDTO<VoiceApprovalResult> voiceApprove(
            @RequestParam String taskId,
            @RequestParam MultipartFile voiceFile) {

        log.info("[移动端审批] 语音审批: taskId={}, fileName={}",
                taskId, voiceFile.getOriginalFilename());

        // 1. 语音转文字
        String text = speechToTextService.convert(voiceFile);

        log.info("[移动端审批] 语音识别结果: text={}", text);

        // 2. 解析审批意见
        ApprovalDecision decision = mobileApprovalService.parseApprovalDecision(text);

        // 3. 执行审批
        QuickApprovalRequest approvalRequest = QuickApprovalRequest.builder()
                .taskId(taskId)
                .action(decision.getApproved() ? "APPROVE" : "REJECT")
                .comment(decision.getComment())
                .build();

        QuickApprovalResult approvalResult = mobileApprovalService.quickApprove(approvalRequest);

        // 4. 返回结果
        VoiceApprovalResult result = VoiceApprovalResult.builder()
                .recognizedText(text)
                .decision(decision)
                .approvalResult(approvalResult)
                .build();

        return ResponseDTO.ok(result);
    }

    // ==================== 审批详情 ====================

    /**
     * 获取审批详情（移动端优化）
     * - 流程图（简化版）
     * - 表单数据（移动端适配）
     * - 审批历史（时间轴）
     * - 操作按钮配置
     *
     * @param taskId 任务ID
     * @return 审批详情
     */
    @GetMapping("/detail/{taskId}")
    public ResponseDTO<MobileApprovalDetailVO> getApprovalDetail(
            @PathVariable String taskId) {

        log.info("[移动端审批] 获取审批详情: taskId={}", taskId);

        MobileApprovalDetailVO detail = mobileApprovalService.getApprovalDetail(taskId);

        return ResponseDTO.ok(detail);
    }

    // ==================== 审批历史 ====================

    /**
     * 获取审批历史（时间轴）
     *
     * @param taskId 任务ID
     * @return 审批历史
     */
    @GetMapping("/history/{taskId}")
    public ResponseDTO<List<ApprovalHistoryItemVO>> getApprovalHistory(
            @PathVariable String taskId) {

        log.info("[移动端审批] 获取审批历史: taskId={}", taskId);

        List<ApprovalHistoryItemVO> history = mobileApprovalService.getApprovalHistory(taskId);

        return ResponseDTO.ok(history);
    }

    // ==================== 流程图 ====================

    /**
     * 获取流程图（移动端简化版）
     * - 只显示关键节点
     * - 高亮当前节点
     * - 移动端适配的样式
     *
     * @param taskId 任务ID
     * @return 流程图数据
     */
    @GetMapping("/process-diagram/{taskId}")
    public ResponseDTO<MobileProcessDiagram> getProcessDiagram(
            @PathVariable String taskId) {

        log.info("[移动端审批] 获取流程图: taskId={}", taskId);

        MobileProcessDiagram diagram = mobileApprovalService.getProcessDiagram(taskId);

        return ResponseDTO.ok(diagram);
    }

    // ==================== 已办列表 ====================

    /**
     * 获取已办列表（移动端优化）
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 已办列表
     */
    @GetMapping("/completed-tasks")
    public ResponseDTO<PageResult<MobileTaskVO>> getCompletedTasks(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        log.info("[移动端审批] 获取已办列表: pageNum={}, pageSize={}", pageNum, pageSize);

        PageResult<MobileTaskVO> result = mobileApprovalService.getCompletedTasks(
                pageNum,
                pageSize
        );

        return ResponseDTO.ok(result);
    }

    // ==================== 抄阅管理 ====================

    /**
     * 订阅流程通知
     * - 订阅指定流程的待办通知
     * - 支持多种通知方式
     *
     * @param request 订阅请求
     * @return 操作结果
     */
    @PostMapping("/subscribe")
    public ResponseDTO<Void> subscribe(
            @RequestBody @Valid SubscribeRequest request) {

        log.info("[移动端审批] 订阅流程通知: processKey={}", request.getProcessKey());

        mobileApprovalService.subscribe(request);

        return ResponseDTO.ok();
    }

    /**
     * 取消订阅
     *
     * @param subscriptionId 订阅ID
     * @return 操作结果
     */
    @PostMapping("/unsubscribe/{subscriptionId}")
    public ResponseDTO<Void> unsubscribe(
            @PathVariable Long subscriptionId) {

        log.info("[移动端审批] 取消订阅: subscriptionId={}", subscriptionId);

        mobileApprovalService.unsubscribe(subscriptionId);

        return ResponseDTO.ok();
    }

    /**
     * 获取订阅列表
     *
     * @return 订阅列表
     */
    @GetMapping("/subscriptions")
    public ResponseDTO<List<SubscriptionVO>> getSubscriptions() {

        List<SubscriptionVO> subscriptions = mobileApprovalService.getSubscriptions();

        return ResponseDTO.ok(subscriptions);
    }

    // ==================== 统计信息 ====================

    /**
     * 获取个人审批统计
     *
     * @return 统计信息
     */
    @GetMapping("/statistics")
    public ResponseDTO<PersonalApprovalStatistics> getStatistics() {

        PersonalApprovalStatistics stats = mobileApprovalService.getPersonalStatistics();

        return ResponseDTO.ok(stats);
    }

    /**
     * 获取工作量统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 工作量统计
     */
    @GetMapping("/workload")
    public ResponseDTO<WorkloadStatistics> getWorkload(
            @RequestParam String startDate,
            @RequestParam String endDate) {

        log.info("[移动端审批] 获取工作量统计: startDate={}, endDate={}", startDate, endDate);

        WorkloadStatistics stats = mobileApprovalService.getWorkload(startDate, endDate);

        return ResponseDTO.ok(stats);
    }
}
