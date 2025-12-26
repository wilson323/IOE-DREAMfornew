package net.lab1024.sa.visitor.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import net.lab1024.sa.visitor.entity.SelfCheckOutEntity;
import net.lab1024.sa.visitor.service.SelfCheckOutService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 自助签离控制器
 * <p>
 * 提供访客自助签离相关API
 * 严格遵循CLAUDE.md规范：
 * - 使用@RestController注解
 * - 使用@Resource依赖注入
 * - 使用@Valid参数校验
 * - 返回统一ResponseDTO格式
 * </p>
 * <p>
 * 业务场景：
 * - 访客自助签离
 * - 人工签离处理
 * - 访客卡归还管理
 * - 满意度评价
 * - 访问时长统计
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/visitor/self-check-out")
@Tag(name = "自助签离管理", description = "访客自助签离、访客卡归还、满意度评价API")
@PermissionCheck(value = "VISITOR_MANAGE", description = "访客管理模块权限")
public class SelfCheckOutController {

    @Resource
    private SelfCheckOutService selfCheckOutService;

    /**
     * 执行自助签离
     *
     * @param visitorCode 访客码
     * @param terminalId 终端ID
     * @param terminalLocation 终端位置
     * @param cardReturnStatus 访客卡归还状态
     * @param visitorCard 访客卡号
     * @return 签离记录
     */
    @PostMapping("/perform")
    @Operation(summary = "执行自助签离", description = "访客在自助终端通过访客码完成签离")
    @PermissionCheck(value = "VISITOR_SELF_SERVICE", description = "访客自助服务权限")
    public ResponseDTO<SelfCheckOutEntity> performCheckOut(
            @RequestParam @NotNull String visitorCode,
            @RequestParam @NotNull String terminalId,
            @RequestParam String terminalLocation,
            @RequestParam @NotNull Integer cardReturnStatus,
            @RequestParam String visitorCard) {
        log.info("[自助签离] 执行自助签离: visitorCode={}, terminalId={}, location={}",
                visitorCode, terminalId, terminalLocation);

        try {
            SelfCheckOutEntity checkOut = selfCheckOutService.performCheckOut(
                    visitorCode, terminalId, terminalLocation, cardReturnStatus, visitorCard);
            return ResponseDTO.ok(checkOut);
        } catch (Exception e) {
            log.error("[自助签离] 执行签离异常", e);
            return ResponseDTO.error("PERFORM_CHECKOUT_ERROR", "执行签离失败: " + e.getMessage());
        }
    }

    /**
     * 人工签离
     *
     * @param visitorCode 访客码
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @param reason 签离原因
     * @return 签离记录
     */
    @PostMapping("/manual")
    @Operation(summary = "人工签离", description = "管理员手动执行访客签离")
    @PermissionCheck(value = "VISITOR_MANAGE", description = "访客管理权限")
    public ResponseDTO<SelfCheckOutEntity> manualCheckOut(
            @RequestParam @NotNull String visitorCode,
            @RequestParam @NotNull Long operatorId,
            @RequestParam @NotNull String operatorName,
            @RequestParam(required = false, defaultValue = "人工签离") String reason) {
        log.info("[自助签离] 人工签离: visitorCode={}, operator={}, reason={}",
                visitorCode, operatorName, reason);

        try {
            SelfCheckOutEntity checkOut = selfCheckOutService.manualCheckOut(
                    visitorCode, operatorId, operatorName, reason);
            return ResponseDTO.ok(checkOut);
        } catch (Exception e) {
            log.error("[自助签离] 人工签离异常", e);
            return ResponseDTO.error("MANUAL_CHECKOUT_ERROR", "人工签离失败: " + e.getMessage());
        }
    }

    /**
     * 查询签离记录
     *
     * @param visitorCode 访客码
     * @return 签离记录
     */
    @GetMapping("/query")
    @Operation(summary = "查询签离记录", description = "根据访客码查询签离记录")
    @PermissionCheck(value = "VISITOR_QUERY", description = "访客查询权限")
    public ResponseDTO<SelfCheckOutEntity> getByVisitorCode(
            @RequestParam @NotNull String visitorCode) {
        log.info("[自助签离] 查询签离记录: visitorCode={}", visitorCode);

        try {
            SelfCheckOutEntity checkOut = selfCheckOutService.getCheckOutByVisitorCode(visitorCode);
            return ResponseDTO.ok(checkOut);
        } catch (Exception e) {
            log.error("[自助签离] 查询签离记录异常", e);
            return ResponseDTO.error("QUERY_CHECKOUT_ERROR", "查询签离记录失败: " + e.getMessage());
        }
    }

    /**
     * 查询超时签离记录
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 超时签离记录列表
     */
    @GetMapping("/overtime")
    @Operation(summary = "查询超时签离记录", description = "查询超过预计离开时间的签离记录")
    @PermissionCheck(value = "VISITOR_QUERY", description = "访客查询权限")
    public ResponseDTO<List<SelfCheckOutEntity>> getOvertimeRecords(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("[自助签离] 查询超时签离记录: startDate={}, endDate={}", startDate, endDate);

        try {
            List<SelfCheckOutEntity> records = selfCheckOutService.getOvertimeRecords(startDate, endDate);
            return ResponseDTO.ok(records);
        } catch (Exception e) {
            log.error("[自助签离] 查询超时记录异常", e);
            return ResponseDTO.error("QUERY_OVERTIME_ERROR", "查询超时记录失败: " + e.getMessage());
        }
    }

    /**
     * 查询未归还访客卡记录
     *
     * @return 未归还访客卡的记录列表
     */
    @GetMapping("/unreturned-cards")
    @Operation(summary = "查询未归还访客卡", description = "查询未归还临时访客卡的签离记录")
    @PermissionCheck(value = "VISITOR_MANAGE", description = "访客管理权限")
    public ResponseDTO<List<SelfCheckOutEntity>> getUnreturnedCards() {
        log.info("[自助签离] 查询未归还访客卡记录");

        try {
            List<SelfCheckOutEntity> records = selfCheckOutService.getUnreturnedCards();
            return ResponseDTO.ok(records);
        } catch (Exception e) {
            log.error("[自助签离] 查询未归还访客卡异常", e);
            return ResponseDTO.error("QUERY_UNRETURNED_ERROR", "查询未归还访客卡失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询签离记录
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param visitorName 访客姓名（可选）
     * @param intervieweeName 被访人姓名（可选）
     * @param isOvertime 是否超时（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询签离记录", description = "根据条件分页查询签离记录")
    @PermissionCheck(value = "VISITOR_QUERY", description = "访客查询权限")
    public ResponseDTO<PageResult<SelfCheckOutEntity>> queryPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String visitorName,
            @RequestParam(required = false) String intervieweeName,
            @RequestParam(required = false) Integer isOvertime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("[自助签离] 分页查询签离记录: pageNum={}, pageSize={}, visitorName={}, intervieweeName={}, isOvertime={}",
                pageNum, pageSize, visitorName, intervieweeName, isOvertime);

        try {
            PageResult<SelfCheckOutEntity> pageResult = selfCheckOutService.queryPage(
                    pageNum, pageSize, visitorName, intervieweeName, isOvertime, startDate, endDate);
            return ResponseDTO.ok(pageResult);
        } catch (Exception e) {
            log.error("[自助签离] 分页查询异常", e);
            return ResponseDTO.error("QUERY_PAGE_ERROR", "分页查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取签离统计信息
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取签离统计信息", description = "获取签离数量、超时统计等数据")
    @PermissionCheck(value = "VISITOR_QUERY", description = "访客查询权限")
    public ResponseDTO<Map<String, Object>> getStatistics(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("[自助签离] 查询统计信息: startDate={}, endDate={}", startDate, endDate);

        try {
            Map<String, Object> statistics = selfCheckOutService.getStatistics(startDate, endDate);
            return ResponseDTO.ok(statistics);
        } catch (Exception e) {
            log.error("[自助签离] 查询统计信息异常", e);
            return ResponseDTO.error("GET_STATISTICS_ERROR", "获取统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 更新满意度评价
     *
     * @param checkOutId 签离记录ID
     * @param satisfactionScore 满意度评分（1-5分）
     * @param visitorFeedback 访客反馈
     * @return 操作结果
     */
    @PostMapping("/satisfaction")
    @Operation(summary = "更新满意度评价", description = "访客对本次访问进行满意度评分")
    @PermissionCheck(value = "VISITOR_SELF_SERVICE", description = "访客自助服务权限")
    public ResponseDTO<Void> updateSatisfaction(
            @RequestParam @NotNull Long checkOutId,
            @RequestParam @NotNull Integer satisfactionScore,
            @RequestParam(required = false) String visitorFeedback) {
        log.info("[自助签离] 更新满意度: checkOutId={}, score={}", checkOutId, satisfactionScore);

        try {
            selfCheckOutService.updateSatisfaction(checkOutId, satisfactionScore, visitorFeedback);
            return ResponseDTO.ok();
        } catch (Exception e) {
            log.error("[自助签离] 更新满意度异常", e);
            return ResponseDTO.error("UPDATE_SATISFACTION_ERROR", "更新满意度失败: " + e.getMessage());
        }
    }

    /**
     * 更新访客卡归还状态
     *
     * @param checkOutId 签离记录ID
     * @param cardReturnStatus 归还状态（0-未归还 1-已归还 2-卡遗失）
     * @param visitorCard 访客卡号
     * @return 操作结果
     */
    @PostMapping("/card-return")
    @Operation(summary = "更新访客卡归还状态", description = "更新临时访客卡的归还状态")
    @PermissionCheck(value = "VISITOR_SELF_SERVICE", description = "访客自助服务权限")
    public ResponseDTO<Void> updateCardReturnStatus(
            @RequestParam @NotNull Long checkOutId,
            @RequestParam @NotNull Integer cardReturnStatus,
            @RequestParam(required = false) String visitorCard) {
        log.info("[自助签离] 更新访客卡归还状态: checkOutId={}, status={}", checkOutId, cardReturnStatus);

        try {
            selfCheckOutService.updateCardReturnStatus(checkOutId, cardReturnStatus, visitorCard);
            return ResponseDTO.ok();
        } catch (Exception e) {
            log.error("[自助签离] 更新访客卡归还状态异常", e);
            return ResponseDTO.error("UPDATE_CARD_RETURN_ERROR", "更新归还状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取访问时长统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 时长统计信息
     */
    @GetMapping("/duration-statistics")
    @Operation(summary = "获取访问时长统计", description = "获取平均访问时长、最大时长、最小时长等数据")
    @PermissionCheck(value = "VISITOR_QUERY", description = "访客查询权限")
    public ResponseDTO<Map<String, Object>> getDurationStatistics(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("[自助签离] 查询访问时长统计: startDate={}, endDate={}", startDate, endDate);

        try {
            Map<String, Object> statistics = selfCheckOutService.getDurationStatistics(startDate, endDate);
            return ResponseDTO.ok(statistics);
        } catch (Exception e) {
            log.error("[自助签离] 查询访问时长统计异常", e);
            return ResponseDTO.error("GET_DURATION_STATS_ERROR", "获取访问时长统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取满意度统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 满意度统计信息
     */
    @GetMapping("/satisfaction-statistics")
    @Operation(summary = "获取满意度统计", description = "获取满意度评分统计和分布情况")
    @PermissionCheck(value = "VISITOR_QUERY", description = "访客查询权限")
    public ResponseDTO<Map<String, Object>> getSatisfactionStatistics(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("[自助签离] 查询满意度统计: startDate={}, endDate={}", startDate, endDate);

        try {
            Map<String, Object> statistics = selfCheckOutService.getSatisfactionStatistics(startDate, endDate);
            return ResponseDTO.ok(statistics);
        } catch (Exception e) {
            log.error("[自助签离] 查询满意度统计异常", e);
            return ResponseDTO.error("GET_SATISFACTION_STATS_ERROR", "获取满意度统计失败: " + e.getMessage());
        }
    }

    /**
     * 查询指定被访人的签离记录
     *
     * @param intervieweeId 被访人ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 签离记录列表
     */
    @GetMapping("/by-interviewee")
    @Operation(summary = "查询被访人签离记录", description = "查询指定被访人的签离记录")
    @PermissionCheck(value = "VISITOR_QUERY", description = "访客查询权限")
    public ResponseDTO<List<SelfCheckOutEntity>> getByInterviewee(
            @RequestParam @NotNull Long intervieweeId,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("[自助签离] 查询被访人签离记录: intervieweeId={}, startDate={}, endDate={}",
                intervieweeId, startDate, endDate);

        try {
            List<SelfCheckOutEntity> records = selfCheckOutService.getByInterviewee(
                    intervieweeId, startDate, endDate);
            return ResponseDTO.ok(records);
        } catch (Exception e) {
            log.error("[自助签离] 查询被访人签离记录异常", e);
            return ResponseDTO.error("QUERY_BY_INTERVIEWEE_ERROR", "查询被访人签离记录失败: " + e.getMessage());
        }
    }

    /**
     * 查询指定终端的签离记录
     *
     * @param terminalId 终端ID
     * @param limit 限制数量
     * @return 签离记录列表
     */
    @GetMapping("/by-terminal")
    @Operation(summary = "查询终端签离记录", description = "查询指定自助终端的签离记录")
    @PermissionCheck(value = "VISITOR_QUERY", description = "访客查询权限")
    public ResponseDTO<List<SelfCheckOutEntity>> getByTerminal(
            @RequestParam @NotNull String terminalId,
            @RequestParam(defaultValue = "100") Integer limit) {
        log.info("[自助签离] 查询终端签离记录: terminalId={}, limit={}", terminalId, limit);

        try {
            List<SelfCheckOutEntity> records = selfCheckOutService.getCheckOutByTerminal(terminalId, limit);
            return ResponseDTO.ok(records);
        } catch (Exception e) {
            log.error("[自助签离] 查询终端签离记录异常", e);
            return ResponseDTO.error("QUERY_BY_TERMINAL_ERROR", "查询终端签离记录失败: " + e.getMessage());
        }
    }
}
