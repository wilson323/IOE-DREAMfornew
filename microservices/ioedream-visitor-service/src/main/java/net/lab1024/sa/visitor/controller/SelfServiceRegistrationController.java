package net.lab1024.sa.visitor.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import net.lab1024.sa.common.entity.visitor.SelfServiceRegistrationEntity;
import net.lab1024.sa.visitor.service.SelfServiceRegistrationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 自助登记终端控制器
 * <p>
 * 提供访客自助登记相关API
 * 严格遵循CLAUDE.md规范：
 * - 使用@RestController注解
 * - 使用@Resource依赖注入
 * - 使用@Valid参数校验
 * - 返回统一ResponseDTO格式
 * </p>
 * <p>
 * 业务场景：
 * - 访客自助登记
 * - 身份证识别
 * - 人脸采集
 * - 访客码生成
 * - 审批管理
 * - 签到签离
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/visitor/self-service")
@Tag(name = "自助登记终端管理", description = "访客自助登记、审批、签到签离API")
@PermissionCheck(value = "VISITOR_MANAGE", description = "访客管理模块权限")
public class SelfServiceRegistrationController {

    @Resource
    private SelfServiceRegistrationService selfServiceRegistrationService;

    /**
     * 创建自助登记
     *
     * @param registration 登记信息
     * @return 登记记录
     */
    @PostMapping("/registration")
    @Operation(summary = "创建自助登记", description = "访客在自助终端填写信息并提交登记申请")
    @PermissionCheck(value = "VISITOR_SELF_SERVICE", description = "访客自助登记权限")
    public ResponseDTO<SelfServiceRegistrationEntity> createRegistration(
            @RequestBody @Valid SelfServiceRegistrationEntity registration) {
        log.info("[自助登记] 创建自助登记: visitorName={}, phone={}",
                registration.getVisitorName(), registration.getPhone());

        try {
            SelfServiceRegistrationEntity result = selfServiceRegistrationService.createRegistration(registration);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[自助登记] 创建登记异常", e);
            return ResponseDTO.error("CREATE_REGISTRATION_ERROR", "创建登记失败: " + e.getMessage());
        }
    }

    /**
     * 根据访客码查询登记记录
     *
     * @param visitorCode 访客码
     * @return 登记记录
     */
    @GetMapping("/registration/visitor-code/{visitorCode}")
    @Operation(summary = "查询登记记录", description = "根据访客码查询登记记录")
    @PermissionCheck(value = "VISITOR_QUERY", description = "访客查询权限")
    public ResponseDTO<SelfServiceRegistrationEntity> getByVisitorCode(
            @PathVariable @NotNull String visitorCode) {
        log.info("[自助登记] 查询登记记录: visitorCode={}", visitorCode);

        try {
            SelfServiceRegistrationEntity registration = selfServiceRegistrationService.getRegistrationByVisitorCode(visitorCode);
            return ResponseDTO.ok(registration);
        } catch (Exception e) {
            log.error("[自助登记] 查询登记异常", e);
            return ResponseDTO.error("QUERY_REGISTRATION_ERROR", "查询登记失败: " + e.getMessage());
        }
    }

    /**
     * 查询待审批的登记记录
     *
     * @param intervieweeId 被访人ID（可选）
     * @param limit 限制数量
     * @return 待审批记录列表
     */
    @GetMapping("/registration/pending")
    @Operation(summary = "查询待审批记录", description = "查询待审批的登记申请列表")
    @PermissionCheck(value = "VISITOR_APPROVAL", description = "访客审批权限")
    public ResponseDTO<List<SelfServiceRegistrationEntity>> getPendingApprovals(
            @RequestParam(required = false) Long intervieweeId,
            @RequestParam(defaultValue = "50") Integer limit) {
        log.info("[自助登记] 查询待审批记录: intervieweeId={}, limit={}", intervieweeId, limit);

        try {
            List<SelfServiceRegistrationEntity> approvals =
                    selfServiceRegistrationService.getPendingApprovals(intervieweeId, limit);
            return ResponseDTO.ok(approvals);
        } catch (Exception e) {
            log.error("[自助登记] 查询待审批记录异常", e);
            return ResponseDTO.error("QUERY_PENDING_ERROR", "查询待审批记录失败: " + e.getMessage());
        }
    }

    /**
     * 审批登记申请
     *
     * @param registrationId 登记ID
     * @param approverId 审批人ID
     * @param approverName 审批人姓名
     * @param approved 是否批准
     * @param approvalComment 审批意见
     * @return 审批后的记录
     */
    @PostMapping("/registration/{registrationId}/approve")
    @Operation(summary = "审批登记申请", description = "被访人或管理员审批访客登记申请")
    @PermissionCheck(value = "VISITOR_APPROVAL", description = "访客审批权限")
    public ResponseDTO<SelfServiceRegistrationEntity> approveRegistration(
            @PathVariable @NotNull Long registrationId,
            @RequestParam @NotNull Long approverId,
            @RequestParam @NotNull String approverName,
            @RequestParam @NotNull Boolean approved,
            @RequestParam(required = false) String approvalComment) {
        log.info("[自助登记] 审批登记申请: registrationId={}, approver={}, approved={}",
                registrationId, approverName, approved);

        try {
            SelfServiceRegistrationEntity result = selfServiceRegistrationService.approveRegistration(
                    registrationId, approverId, approverName, approved, approvalComment);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[自助登记] 审批登记异常", e);
            return ResponseDTO.error("APPROVE_REGISTRATION_ERROR", "审批登记失败: " + e.getMessage());
        }
    }

    /**
     * 批量审批登记申请
     *
     * @param registrationIds 登记ID列表
     * @param approverId 审批人ID
     * @param approverName 审批人姓名
     * @param approved 是否批准
     * @param approvalComment 审批意见
     * @return 审批结果
     */
    @PostMapping("/registration/batch-approve")
    @Operation(summary = "批量审批登记申请", description = "批量审批多个登记申请")
    @PermissionCheck(value = "VISITOR_APPROVAL", description = "访客审批权限")
    public ResponseDTO<Map<String, Object>> batchApprove(
            @RequestBody @NotNull List<Long> registrationIds,
            @RequestParam @NotNull Long approverId,
            @RequestParam @NotNull String approverName,
            @RequestParam @NotNull Boolean approved,
            @RequestParam(required = false) String approvalComment) {
        log.info("[自助登记] 批量审批登记: count={}, approver={}, approved={}",
                registrationIds.size(), approverName, approved);

        try {
            Map<String, Object> result = selfServiceRegistrationService.batchApprove(
                    registrationIds, approverId, approverName, approved, approvalComment);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[自助登记] 批量审批异常", e);
            return ResponseDTO.error("BATCH_APPROVE_ERROR", "批量审批失败: " + e.getMessage());
        }
    }

    /**
     * 访客签到
     *
     * @param visitorCode 访客码
     * @return 签到后的记录
     */
    @PostMapping("/registration/check-in")
    @Operation(summary = "访客签到", description = "访客使用访客码在自助终端签到")
    @PermissionCheck(value = "VISITOR_SELF_SERVICE", description = "访客自助服务权限")
    public ResponseDTO<SelfServiceRegistrationEntity> checkIn(
            @RequestParam @NotNull String visitorCode) {
        log.info("[自助登记] 访客签到: visitorCode={}", visitorCode);

        try {
            SelfServiceRegistrationEntity result = selfServiceRegistrationService.checkIn(visitorCode);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[自助登记] 签到异常", e);
            return ResponseDTO.error("CHECK_IN_ERROR", "签到失败: " + e.getMessage());
        }
    }

    /**
     * 访客签离
     *
     * @param visitorCode 访客码
     * @return 签离后的记录
     */
    @PostMapping("/registration/check-out")
    @Operation(summary = "访客签离", description = "访客在自助终端签离，归还访客卡")
    @PermissionCheck(value = "VISITOR_SELF_SERVICE", description = "访客自助服务权限")
    public ResponseDTO<SelfServiceRegistrationEntity> checkOut(
            @RequestParam @NotNull String visitorCode) {
        log.info("[自助登记] 访客签离: visitorCode={}", visitorCode);

        try {
            SelfServiceRegistrationEntity result = selfServiceRegistrationService.checkOut(visitorCode);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[自助登记] 签离异常", e);
            return ResponseDTO.error("CHECK_OUT_ERROR", "签离失败: " + e.getMessage());
        }
    }

    /**
     * 查询在场访客
     *
     * @return 在场访客列表
     */
    @GetMapping("/registration/active")
    @Operation(summary = "查询在场访客", description = "查询已签到但未签离的访客")
    @PermissionCheck(value = "VISITOR_QUERY", description = "访客查询权限")
    public ResponseDTO<List<SelfServiceRegistrationEntity>> getActiveVisitors() {
        log.info("[自助登记] 查询在场访客");

        try {
            List<SelfServiceRegistrationEntity> visitors = selfServiceRegistrationService.getActiveVisitors();
            return ResponseDTO.ok(visitors);
        } catch (Exception e) {
            log.error("[自助登记] 查询在场访客异常", e);
            return ResponseDTO.error("QUERY_ACTIVE_ERROR", "查询在场访客失败: " + e.getMessage());
        }
    }

    /**
     * 查询超时未签离访客
     *
     * @return 超时访客列表
     */
    @GetMapping("/registration/overdue")
    @Operation(summary = "查询超时访客", description = "查询超过预计离开时间未签离的访客")
    @PermissionCheck(value = "VISITOR_QUERY", description = "访客查询权限")
    public ResponseDTO<List<SelfServiceRegistrationEntity>> getOverdueVisitors() {
        log.info("[自助登记] 查询超时访客");

        try {
            List<SelfServiceRegistrationEntity> visitors = selfServiceRegistrationService.getOverdueVisitors();
            return ResponseDTO.ok(visitors);
        } catch (Exception e) {
            log.error("[自助登记] 查询超时访客异常", e);
            return ResponseDTO.error("QUERY_OVERDUE_ERROR", "查询超时访客失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询登记记录
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param visitorName 访客姓名（可选）
     * @param phone 手机号（可选）
     * @param status 登记状态（可选）
     * @param visitDateStart 访问日期开始（可选）
     * @param visitDateEnd 访问日期结束（可选）
     * @return 分页结果
     */
    @GetMapping("/registration/query")
    @Operation(summary = "分页查询登记记录", description = "根据条件分页查询自助登记记录")
    @PermissionCheck(value = "VISITOR_QUERY", description = "访客查询权限")
    public ResponseDTO<PageResult<SelfServiceRegistrationEntity>> queryPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String visitorName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate visitDateStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate visitDateEnd) {
        log.info("[自助登记] 分页查询登记记录: pageNum={}, pageSize={}, visitorName={}, phone={}, status={}",
                pageNum, pageSize, visitorName, phone, status);

        try {
            PageResult<SelfServiceRegistrationEntity> pageResult = selfServiceRegistrationService.queryPage(
                    pageNum, pageSize, visitorName, phone, status, visitDateStart, visitDateEnd);
            return ResponseDTO.ok(pageResult);
        } catch (Exception e) {
            log.error("[自助登记] 分页查询异常", e);
            return ResponseDTO.error("QUERY_PAGE_ERROR", "分页查询失败: " + e.getMessage());
        }
    }

    /**
     * 取消登记
     *
     * @param registrationId 登记ID
     * @param cancelReason 取消原因
     * @return 取消后的记录
     */
    @PostMapping("/registration/{registrationId}/cancel")
    @Operation(summary = "取消登记", description = "取消未签到或未审批的登记申请")
    @PermissionCheck(value = "VISITOR_MANAGE", description = "访客管理权限")
    public ResponseDTO<SelfServiceRegistrationEntity> cancelRegistration(
            @PathVariable @NotNull Long registrationId,
            @RequestParam @NotNull String cancelReason) {
        log.info("[自助登记] 取消登记: registrationId={}, cancelReason={}", registrationId, cancelReason);

        try {
            SelfServiceRegistrationEntity result = selfServiceRegistrationService.cancelRegistration(
                    registrationId, cancelReason);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[自助登记] 取消登记异常", e);
            return ResponseDTO.error("CANCEL_REGISTRATION_ERROR", "取消登记失败: " + e.getMessage());
        }
    }

    /**
     * 更新访客卡打印状态
     *
     * @param registrationId 登记ID
     * @param printStatus 打印状态
     * @param visitorCard 访客卡号
     * @return 更新后的记录
     */
    @PostMapping("/registration/{registrationId}/card-print")
    @Operation(summary = "更新访客卡打印状态", description = "更新访客卡的打印状态和卡号")
    @PermissionCheck(value = "VISITOR_SELF_SERVICE", description = "访客自助服务权限")
    public ResponseDTO<SelfServiceRegistrationEntity> updateCardPrintStatus(
            @PathVariable @NotNull Long registrationId,
            @RequestParam @NotNull Integer printStatus,
            @RequestParam(required = false) String visitorCard) {
        log.info("[自助登记] 更新访客卡打印状态: registrationId={}, printStatus={}, visitorCard={}",
                registrationId, printStatus, visitorCard);

        try {
            SelfServiceRegistrationEntity result = selfServiceRegistrationService.updateCardPrintStatus(
                    registrationId, printStatus, visitorCard);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[自助登记] 更新打印状态异常", e);
            return ResponseDTO.error("UPDATE_PRINT_STATUS_ERROR", "更新打印状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取统计信息
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计信息
     */
    @GetMapping("/registration/statistics")
    @Operation(summary = "获取统计信息", description = "获取自助登记的统计数据")
    @PermissionCheck(value = "VISITOR_QUERY", description = "访客查询权限")
    public ResponseDTO<Map<String, Object>> getStatistics(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("[自助登记] 查询统计信息: startDate={}, endDate={}", startDate, endDate);

        try {
            Map<String, Object> statistics = selfServiceRegistrationService.getStatistics(startDate, endDate);
            return ResponseDTO.ok(statistics);
        } catch (Exception e) {
            log.error("[自助登记] 查询统计信息异常", e);
            return ResponseDTO.error("GET_STATISTICS_ERROR", "获取统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 查询指定被访人的登记记录
     *
     * @param intervieweeId 被访人ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 登记记录列表
     */
    @GetMapping("/registration/by-interviewee")
    @Operation(summary = "查询被访人登记记录", description = "查询指定被访人的自助登记记录")
    @PermissionCheck(value = "VISITOR_QUERY", description = "访客查询权限")
    public ResponseDTO<List<SelfServiceRegistrationEntity>> getByInterviewee(
            @RequestParam @NotNull Long intervieweeId,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("[自助登记] 查询被访人登记记录: intervieweeId={}, startDate={}, endDate={}",
                intervieweeId, startDate, endDate);

        try {
            List<SelfServiceRegistrationEntity> registrations =
                    selfServiceRegistrationService.getByInterviewee(intervieweeId, startDate, endDate);
            return ResponseDTO.ok(registrations);
        } catch (Exception e) {
            log.error("[自助登记] 查询被访人登记记录异常", e);
            return ResponseDTO.error("QUERY_BY_INTERVIEWEE_ERROR", "查询被访人登记记录失败: " + e.getMessage());
        }
    }

    /**
     * 查询指定终端的登记记录
     *
     * @param terminalId 终端ID
     * @param limit 限制数量
     * @return 登记记录列表
     */
    @GetMapping("/registration/by-terminal")
    @Operation(summary = "查询终端登记记录", description = "查询指定自助终端的登记记录")
    @PermissionCheck(value = "VISITOR_QUERY", description = "访客查询权限")
    public ResponseDTO<List<SelfServiceRegistrationEntity>> getByTerminal(
            @RequestParam @NotNull String terminalId,
            @RequestParam(defaultValue = "100") Integer limit) {
        log.info("[自助登记] 查询终端登记记录: terminalId={}, limit={}", terminalId, limit);

        try {
            List<SelfServiceRegistrationEntity> registrations =
                    selfServiceRegistrationService.getByTerminal(terminalId, limit);
            return ResponseDTO.ok(registrations);
        } catch (Exception e) {
            log.error("[自助登记] 查询终端登记记录异常", e);
            return ResponseDTO.error("QUERY_BY_TERMINAL_ERROR", "查询终端登记记录失败: " + e.getMessage());
        }
    }
}
