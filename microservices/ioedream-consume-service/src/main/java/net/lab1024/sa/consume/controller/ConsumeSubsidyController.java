package net.lab1024.sa.consume.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import net.lab1024.sa.consume.domain.form.ConsumeSubsidyAddForm;
import net.lab1024.sa.consume.domain.form.ConsumeSubsidyQueryForm;
import net.lab1024.sa.consume.domain.form.ConsumeSubsidyUpdateForm;
import net.lab1024.sa.consume.domain.vo.ConsumeSubsidyStatisticsVO;
import net.lab1024.sa.consume.domain.vo.ConsumeSubsidyVO;
import net.lab1024.sa.consume.service.ConsumeSubsidyService;
import lombok.extern.slf4j.Slf4j;

/**
 * 消费补贴管理控制器
 * <p>
 * 提供消费补贴的管理功能，包括：
 * 1. 补贴政策管理
 * 2. 补贴发放管理
 * 3. 补贴使用查询
 * 4. 补贴统计分析
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Slf4j
@RestController
@PermissionCheck(value = "CONSUME_SUBSIDY_MANAGE", description = "消费补贴管理权限")
@RequestMapping("/api/v1/consume/subsidies")
@Tag(name = "消费补贴管理", description = "消费补贴政策、发放、统计等功能")
public class ConsumeSubsidyController {

    @Resource
    private ConsumeSubsidyService consumeSubsidyService;

    /**
     * 分页查询补贴列表
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    @PostMapping("/query")
    @Operation(summary = "分页查询补贴", description = "根据条件分页查询消费补贴列表")
    public ResponseDTO<PageResult<ConsumeSubsidyVO>> querySubsidies(@ModelAttribute ConsumeSubsidyQueryForm queryForm) {
        PageResult<ConsumeSubsidyVO> result = consumeSubsidyService.querySubsidyPage(queryForm);
        return ResponseDTO.ok(result);
    }

    /**
     * 获取补贴详情
     *
     * @param subsidyId 补贴ID
     * @return 补贴详情
     */
    @GetMapping("/{subsidyId}")
    @Operation(summary = "获取补贴详情", description = "根据补贴ID获取详细的补贴信息")
    public ResponseDTO<ConsumeSubsidyVO> getSubsidyDetail(
            @Parameter(description = "补贴ID", required = true) @PathVariable Long subsidyId) {
        ConsumeSubsidyVO result = consumeSubsidyService.getSubsidyById(subsidyId);
        return ResponseDTO.ok(result);
    }

    /**
     * 新增补贴
     *
     * @param addForm 补贴新增表单
     * @return 新增结果
     */
    @PostMapping("/add")
    @Operation(summary = "新增补贴", description = "创建新的消费补贴")
    public ResponseDTO<Long> addSubsidy(@Valid @RequestBody ConsumeSubsidyAddForm addForm) {
        Long subsidyId = consumeSubsidyService.addSubsidy(addForm);
        return ResponseDTO.ok(subsidyId);
    }

    /**
     * 更新补贴
     *
     * @param subsidyId  补贴ID
     * @param updateForm 更新表单
     * @return 更新结果
     */
    @PutMapping("/{subsidyId}")
    @Operation(summary = "更新补贴", description = "更新消费补贴的基本信息")
    public ResponseDTO<Void> updateSubsidy(
            @Parameter(description = "补贴ID", required = true) @PathVariable Long subsidyId,
            @Valid @RequestBody ConsumeSubsidyUpdateForm updateForm) {
        updateForm.setSubsidyId(subsidyId);
        consumeSubsidyService.updateSubsidy(updateForm);
        return ResponseDTO.ok();
    }

    /**
     * 删除补贴
     *
     * @param subsidyId 补贴ID
     * @return 删除结果
     */
    @DeleteMapping("/{subsidyId}")
    @Operation(summary = "删除补贴", description = "删除指定的消费补贴")
    public ResponseDTO<Void> deleteSubsidy(
            @Parameter(description = "补贴ID", required = true) @PathVariable Long subsidyId) {
        consumeSubsidyService.deleteSubsidy(subsidyId);
        return ResponseDTO.ok();
    }

    /**
     * 获取用户的补贴列表
     *
     * @param userId 用户ID
     * @return 补贴列表
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户补贴", description = "获取指定用户的消费补贴列表")
    public ResponseDTO<List<ConsumeSubsidyVO>> getUserSubsidies(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId) {
        List<ConsumeSubsidyVO> subsidies = consumeSubsidyService.getUserSubsidies(userId);
        return ResponseDTO.ok(subsidies);
    }

    /**
     * 获取用户可用补贴
     *
     * @param userId 用户ID
     * @return 可用补贴列表
     */
    @GetMapping("/user/{userId}/available")
    @Operation(summary = "获取用户可用补贴", description = "获取指定用户当前可用的消费补贴")
    public ResponseDTO<List<ConsumeSubsidyVO>> getUserAvailableSubsidies(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId) {
        List<ConsumeSubsidyVO> subsidies = consumeSubsidyService.getUserAvailableSubsidies(userId);
        return ResponseDTO.ok(subsidies);
    }

    /**
     * 发放补贴
     *
     * @param subsidyId 补贴ID
     * @param userIds   用户ID列表
     * @return 发放结果
     */
    @PostMapping("/{subsidyId}/distribute")
    @Operation(summary = "发放补贴", description = "向指定用户发放补贴")
    public ResponseDTO<Void> distributeSubsidy(
            @Parameter(description = "补贴ID", required = true) @PathVariable Long subsidyId,
            @Parameter(description = "用户ID列表", required = true) @RequestParam List<Long> userIds) {
        consumeSubsidyService.distributeSubsidy(subsidyId, userIds);
        return ResponseDTO.ok();
    }

    /**
     * 批量发放补贴
     *
     * @param addForm 补贴新增表单
     * @param userIds 用户ID列表
     * @return 发放结果
     */
    @PostMapping("/batch-distribute")
    @Operation(summary = "批量发放补贴", description = "创建补贴并批量发放给指定用户")
    public ResponseDTO<Void> batchDistributeSubsidy(
            @Valid @RequestBody ConsumeSubsidyAddForm addForm,
            @Parameter(description = "用户ID列表", required = true) @RequestParam List<Long> userIds) {
        consumeSubsidyService.batchDistributeSubsidy(addForm, userIds);
        return ResponseDTO.ok();
    }

    /**
     * 停用补贴
     *
     * @param subsidyId 补贴ID
     * @return 停用结果
     */
    @PutMapping("/{subsidyId}/disable")
    @Operation(summary = "停用补贴", description = "停用指定的消费补贴")
    public ResponseDTO<Void> disableSubsidy(
            @Parameter(description = "补贴ID", required = true) @PathVariable Long subsidyId) {
        consumeSubsidyService.disableSubsidy(subsidyId);
        return ResponseDTO.ok();
    }

    /**
     * 启用补贴
     *
     * @param subsidyId 补贴ID
     * @return 启用结果
     */
    @PutMapping("/{subsidyId}/enable")
    @Operation(summary = "启用补贴", description = "启用指定的消费补贴")
    public ResponseDTO<Void> enableSubsidy(
            @Parameter(description = "补贴ID", required = true) @PathVariable Long subsidyId) {
        consumeSubsidyService.enableSubsidy(subsidyId);
        return ResponseDTO.ok();
    }

    /**
     * 获取补贴统计信息
     *
     * @param userId    用户ID（可选）
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取补贴统计信息", description = "获取消费补贴的统计信息")
    public ResponseDTO<ConsumeSubsidyStatisticsVO> getStatistics(
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "开始日期") @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) LocalDateTime endDate) {
        ConsumeSubsidyStatisticsVO statistics = consumeSubsidyService.getStatistics(userId, startDate, endDate);
        return ResponseDTO.ok(statistics);
    }

    /**
     * 导出补贴记录
     *
     * @param queryForm 查询条件
     * @return 导出结果
     */
    @PostMapping("/export")
    @Operation(summary = "导出补贴记录", description = "导出补贴记录到Excel文件")
    public ResponseDTO<String> exportSubsidies(@ModelAttribute ConsumeSubsidyQueryForm queryForm) {
        String downloadUrl = consumeSubsidyService.exportSubsidies(queryForm);
        return ResponseDTO.ok(downloadUrl);
    }

    /**
     * 获取补贴使用记录
     *
     * @param subsidyId 补贴ID
     * @param pageNum   页码
     * @param pageSize  页大小
     * @return 使用记录
     */
    @GetMapping("/{subsidyId}/usage")
    @Operation(summary = "获取补贴使用记录", description = "获取指定补贴的使用记录")
    public ResponseDTO<PageResult<java.util.Map<String, Object>>> getSubsidyUsage(
            @Parameter(description = "补贴ID", required = true) @PathVariable Long subsidyId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        PageResult<java.util.Map<String, Object>> result = consumeSubsidyService.getSubsidyUsage(subsidyId, pageNum,
                pageSize);
        return ResponseDTO.ok(result);
    }

    /**
     * 手动调整补贴金额
     *
     * @param subsidyId        补贴ID
     * @param adjustmentAmount 调整金额
     * @param reason           调整原因
     * @return 调整结果
     */
    @PostMapping("/{subsidyId}/adjust")
    @Operation(summary = "调整补贴金额", description = "手动调整指定补贴的金额")
    public ResponseDTO<Void> adjustSubsidyAmount(
            @Parameter(description = "补贴ID", required = true) @PathVariable Long subsidyId,
            @Parameter(description = "调整金额", required = true) @RequestParam BigDecimal adjustmentAmount,
            @Parameter(description = "调整原因", required = true) @RequestParam String reason) {
        consumeSubsidyService.adjustSubsidyAmount(subsidyId, adjustmentAmount, reason);
        return ResponseDTO.ok();
    }

    /**
     * 获取待发放补贴列表
     *
     * @return 待发放补贴列表
     */
    @GetMapping("/pending")
    @Operation(summary = "获取待发放补贴", description = "获取待发放的消费补贴列表")
    public ResponseDTO<List<ConsumeSubsidyVO>> getPendingSubsidies() {
        List<ConsumeSubsidyVO> subsidies = consumeSubsidyService.getPendingSubsidies();
        return ResponseDTO.ok(subsidies);
    }

    /**
     * 批量操作补贴
     *
     * @param subsidyIds 补贴ID列表
     * @param operation  操作类型
     * @return 操作结果
     */
    @PostMapping("/batch")
    @Operation(summary = "批量操作补贴", description = "对多个补贴执行批量操作")
    public ResponseDTO<Void> batchOperateSubsidies(
            @Parameter(description = "补贴ID列表", required = true) @RequestParam List<Long> subsidyIds,
            @Parameter(description = "操作类型", required = true) @RequestParam String operation) {
        consumeSubsidyService.batchOperateSubsidies(subsidyIds, operation);
        return ResponseDTO.ok();
    }
}
