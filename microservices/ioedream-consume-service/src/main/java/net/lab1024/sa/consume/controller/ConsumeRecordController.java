package net.lab1024.sa.consume.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
import net.lab1024.sa.consume.domain.form.ConsumeRecordAddForm;
import net.lab1024.sa.consume.domain.form.ConsumeRecordQueryForm;
import net.lab1024.sa.consume.domain.vo.ConsumeRecordVO;
import net.lab1024.sa.consume.domain.vo.ConsumeStatisticsVO;
import net.lab1024.sa.consume.service.ConsumeRecordService;
import lombok.extern.slf4j.Slf4j;

/**
 * 消费记录管理控制器
 * <p>
 * 提供消费记录的管理功能，包括：
 * 1. 消费记录查询
 * 2. 消费记录导出
 * 3. 消费统计分析
 * 4. 消费明细查询
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Slf4j
@RestController
@PermissionCheck(value = "CONSUME_RECORD_MANAGE", description = "消费记录管理权限")
@RequestMapping("/api/v1/consume/records")
@Tag(name = "消费记录管理", description = "消费记录查询、统计、导出等功能")
public class ConsumeRecordController {

    @Resource
    private ConsumeRecordService consumeRecordService;

    /**
     * 分页查询消费记录
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    @GetMapping("/query")
    @Operation(summary = "分页查询消费记录", description = "根据条件分页查询消费记录")
    public ResponseDTO<PageResult<ConsumeRecordVO>> queryRecords(ConsumeRecordQueryForm queryForm) {
        PageResult<ConsumeRecordVO> result = consumeRecordService.queryRecords(queryForm);
        return ResponseDTO.ok(result);
    }

    /**
     * 获取消费记录详情
     *
     * @param recordId 记录ID
     * @return 记录详情
     */
    @GetMapping("/{recordId}")
    @Operation(summary = "获取消费记录详情", description = "根据记录ID获取详细的消费信息")
    public ResponseDTO<ConsumeRecordVO> getRecordDetail(
            @Parameter(description = "记录ID", required = true) @PathVariable Long recordId) {
        ConsumeRecordVO record = consumeRecordService.getRecordDetail(recordId);
        return ResponseDTO.ok(record);
    }

    /**
     * 创建消费记录
     *
     * @param addForm 消费表单
     * @return 创建结果
     */
    @PostMapping("/add")
    @Operation(summary = "创建消费记录", description = "创建新的消费记录")
    public ResponseDTO<Long> addRecord(@Valid @RequestBody ConsumeRecordAddForm addForm) {
        Long recordId = consumeRecordService.addRecord(addForm);
        return ResponseDTO.ok(recordId);
    }

    /**
     * 获取用户的消费记录
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 页大小
     * @return 消费记录列表
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户消费记录", description = "获取指定用户的消费记录")
    public ResponseDTO<PageResult<ConsumeRecordVO>> getUserRecords(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        ConsumeRecordQueryForm queryForm = new ConsumeRecordQueryForm();
        queryForm.setUserId(userId);
        queryForm.setPageNum(pageNum);
        queryForm.setPageSize(pageSize);

        PageResult<ConsumeRecordVO> result = consumeRecordService.queryRecords(queryForm);
        return ResponseDTO.ok(result);
    }

    /**
     * 获取设备消费记录
     *
     * @param deviceId 设备ID
     * @param pageNum  页码
     * @param pageSize 页大小
     * @return 消费记录列表
     */
    @GetMapping("/device/{deviceId}")
    @Operation(summary = "获取设备消费记录", description = "获取指定设备的消费记录")
    public ResponseDTO<PageResult<ConsumeRecordVO>> getDeviceRecords(
            @Parameter(description = "设备ID", required = true) @PathVariable String deviceId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        ConsumeRecordQueryForm queryForm = new ConsumeRecordQueryForm();
        queryForm.setDeviceId(deviceId);
        queryForm.setPageNum(pageNum);
        queryForm.setPageSize(pageSize);

        PageResult<ConsumeRecordVO> result = consumeRecordService.queryRecords(queryForm);
        return ResponseDTO.ok(result);
    }

    /**
     * 获取今日消费记录
     *
     * @param userId 用户ID（可选）
     * @return 今日消费记录
     */
    @GetMapping("/today")
    @Operation(summary = "获取今日消费记录", description = "获取今日的消费记录列表")
    public ResponseDTO<List<ConsumeRecordVO>> getTodayRecords(
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId) {
        List<ConsumeRecordVO> records = consumeRecordService.getTodayRecords(userId);
        return ResponseDTO.ok(records);
    }

    /**
     * 获取消费统计信息
     *
     * @param userId    用户ID（可选）
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取消费统计信息", description = "获取指定时间段的消费统计")
    public ResponseDTO<ConsumeStatisticsVO> getStatistics(
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "开始日期") @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) LocalDateTime endDate) {
        ConsumeStatisticsVO statistics = consumeRecordService.getStatistics(userId, startDate, endDate);
        return ResponseDTO.ok(statistics);
    }

    /**
     * 获取消费趋势数据
     *
     * @param userId 用户ID（可选）
     * @param days   天数（默认7天）
     * @return 趋势数据
     */
    @GetMapping("/trend")
    @Operation(summary = "获取消费趋势", description = "获取消费金额趋势数据")
    public ResponseDTO<java.util.Map<String, Object>> getConsumeTrend(
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "天数") @RequestParam(defaultValue = "7") Integer days) {
        java.util.Map<String, Object> trend = consumeRecordService.getConsumeTrend(userId, days);
        return ResponseDTO.ok(trend);
    }

    /**
     * 导出消费记录
     *
     * @param queryForm 查询条件
     * @return 导出结果
     */
    @PostMapping("/export")
    @Operation(summary = "导出消费记录", description = "导出消费记录到Excel文件")
    public ResponseDTO<String> exportRecords(@RequestBody ConsumeRecordQueryForm queryForm) {
        String downloadUrl = consumeRecordService.exportRecords(queryForm);
        return ResponseDTO.ok(downloadUrl);
    }

    /**
     * 撤销消费记录
     *
     * @param recordId 记录ID
     * @param reason   撤销原因
     * @return 撤销结果
     */
    @PostMapping("/{recordId}/cancel")
    @Operation(summary = "撤销消费记录", description = "撤销指定的消费记录")
    public ResponseDTO<Void> cancelRecord(
            @Parameter(description = "记录ID", required = true) @PathVariable Long recordId,
            @Parameter(description = "撤销原因", required = true) @RequestParam String reason) {
        consumeRecordService.cancelRecord(recordId, reason);
        return ResponseDTO.ok();
    }

    /**
     * 退款处理
     *
     * @param recordId     记录ID
     * @param refundAmount 退款金额
     * @param reason       退款原因
     * @return 退款结果
     */
    @PostMapping("/{recordId}/refund")
    @Operation(summary = "退款处理", description = "对消费记录进行退款处理")
    public ResponseDTO<Void> refundRecord(
            @Parameter(description = "记录ID", required = true) @PathVariable Long recordId,
            @Parameter(description = "退款金额", required = true) @RequestParam BigDecimal refundAmount,
            @Parameter(description = "退款原因", required = true) @RequestParam String reason) {
        consumeRecordService.refundRecord(recordId, refundAmount, reason);
        return ResponseDTO.ok();
    }
}
