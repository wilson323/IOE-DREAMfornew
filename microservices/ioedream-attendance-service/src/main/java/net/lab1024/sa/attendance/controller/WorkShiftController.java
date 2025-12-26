package net.lab1024.sa.attendance.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.lab1024.sa.attendance.entity.WorkShiftEntity;
import net.lab1024.sa.attendance.util.CrossDayShiftUtil;
import net.lab1024.sa.attendance.dao.WorkShiftDao;
import net.lab1024.sa.common.dto.ResponseDTO;

import java.time.LocalTime;
import java.util.List;

/**
 * 班次配置控制器
 * <p>
 * 提供班次配置管理相关API接口，支持跨天班次
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/attendance/workshift")
@Tag(name = "班次配置管理")
public class WorkShiftController {

    @Resource
    private WorkShiftDao workShiftDao;

    /**
     * 查询所有班次
     *
     * @return 班次列表
     */
    @Observed(name = "workShift.getAll", contextualName = "get-all-workshifts")
    @GetMapping("/list")
    @Operation(summary = "查询所有班次", description = "查询所有激活的班次配置")
    public ResponseDTO<List<WorkShiftEntity>> getAllWorkShifts() {
        log.info("[班次管理] 查询所有班次");
        List<WorkShiftEntity> workShifts = workShiftDao.selectAllActive();
        log.info("[班次管理] 查询所有班次成功: count={}", workShifts.size());
        return ResponseDTO.ok(workShifts);
    }

    /**
     * 根据班次类型查询班次
     *
     * @param shiftType 班次类型
     * @return 班次列表
     */
    @Observed(name = "workShift.getByType", contextualName = "get-workshifts-by-type")
    @GetMapping("/type/{shiftType}")
    @Operation(summary = "根据类型查询班次", description = "根据班次类型查询班次列表")
    public ResponseDTO<List<WorkShiftEntity>> getWorkShiftsByType(
            @PathVariable @Parameter(description = "班次类型", required = true) Integer shiftType) {
        log.info("[班次管理] 根据类型查询班次: shiftType={}", shiftType);
        List<WorkShiftEntity> workShifts = workShiftDao.selectByShiftType(shiftType);
        log.info("[班次管理] 根据类型查询班次成功: shiftType={}, count={}", shiftType, workShifts.size());
        return ResponseDTO.ok(workShifts);
    }

    /**
     * 查询班次详情
     *
     * @param shiftId 班次ID
     * @return 班次详情
     */
    @Observed(name = "workShift.getDetail", contextualName = "get-workshift-detail")
    @GetMapping("/{shiftId}")
    @Operation(summary = "查询班次详情", description = "查询指定班次的详细信息")
    public ResponseDTO<WorkShiftEntity> getWorkShiftDetail(
            @PathVariable @Parameter(description = "班次ID", required = true) Long shiftId) {
        log.info("[班次管理] 查询班次详情: shiftId={}", shiftId);
        WorkShiftEntity workShift = workShiftDao.selectById(shiftId);
        if (workShift == null) {
            log.warn("[班次管理] 班次不存在: shiftId={}", shiftId);
            return ResponseDTO.error("WORKSHIFT_NOT_FOUND", "班次不存在");
        }
        log.info("[班次管理] 查询班次详情成功: shiftId={}, shiftName={}", shiftId, workShift.getShiftName());
        return ResponseDTO.ok(workShift);
    }

    /**
     * 创建班次
     * <p>
     * 自动检测并设置跨天标识和默认规则
     * </p>
     *
     * @param workShift 班次实体
     * @return 班次ID
     */
    @Observed(name = "workShift.create", contextualName = "create-workshift")
    @PostMapping
    @Operation(summary = "创建班次", description = "创建新的班次配置，自动检测跨天")
    public ResponseDTO<Long> createWorkShift(@Valid @RequestBody WorkShiftEntity workShift) {
        log.info("[班次管理] 创建班次: shiftName={}, startTime={}, endTime={}",
                workShift.getShiftName(), workShift.getWorkStartTime(), workShift.getWorkEndTime());

        // 自动检测跨天并设置默认规则
        if (workShift.getWorkStartTime() != null && workShift.getWorkEndTime() != null) {
            boolean isCrossDay = workShift.getWorkEndTime().isBefore(workShift.getWorkStartTime());

            // 设置跨天标识（更新isOvernight字段以保持兼容）
            workShift.setIsCrossDay(isCrossDay ? 1 : 0);

            // 设置默认跨天规则
            if (workShift.getCrossDayRule() == null || workShift.getCrossDayRule().isEmpty()) {
                workShift.setCrossDayRule(CrossDayShiftUtil.CROSS_DAY_RULE_START_DATE);
                log.info("[班次管理] 自动设置跨天规则: isCrossDay={}, crossDayRule=START_DATE", isCrossDay);
            }
        }

        // 保存到数据库
        workShiftDao.insert(workShift);
        log.info("[班次管理] 创建班次成功: shiftId={}, shiftName={}, isCrossDay={}",
                workShift.getShiftId(), workShift.getShiftName(), workShift.getIsCrossDay());

        return ResponseDTO.ok(workShift.getShiftId());
    }

    /**
     * 更新班次
     * <p>
     * 支持修改跨天规则，自动重新检测跨天标识
     * </p>
     *
     * @param shiftId 班次ID
     * @param workShift 班次实体
     * @return 更新结果
     */
    @Observed(name = "workShift.update", contextualName = "update-workshift")
    @PutMapping("/{shiftId}")
    @Operation(summary = "更新班次", description = "更新班次配置，支持修改跨天规则")
    public ResponseDTO<Void> updateWorkShift(
            @PathVariable @Parameter(description = "班次ID", required = true) Long shiftId,
            @Valid @RequestBody WorkShiftEntity workShift) {
        log.info("[班次管理] 更新班次: shiftId={}, shiftName={}, crossDayRule={}",
                shiftId, workShift.getShiftName(), workShift.getCrossDayRule());

        // 查询现有班次
        WorkShiftEntity existingWorkShift = workShiftDao.selectById(shiftId);
        if (existingWorkShift == null) {
            log.warn("[班次管理] 班次不存在: shiftId={}", shiftId);
            return ResponseDTO.error("WORKSHIFT_NOT_FOUND", "班次不存在");
        }

        // 自动检测跨天并更新标识
        if (workShift.getWorkStartTime() != null && workShift.getWorkEndTime() != null) {
            boolean isCrossDay = workShift.getWorkEndTime().isBefore(workShift.getWorkStartTime());
            workShift.setIsCrossDay(isCrossDay ? 1 : 0);
            log.info("[班次管理] 自动更新跨天标识: isCrossDay={}", isCrossDay);
        }

        // 更新到数据库
        workShift.setShiftId(shiftId);
        workShiftDao.updateById(workShift);
        log.info("[班次管理] 更新班次成功: shiftId={}, isCrossDay={}", shiftId, workShift.getIsCrossDay());

        return ResponseDTO.ok();
    }

    /**
     * 删除班次
     *
     * @param shiftId 班次ID
     * @return 删除结果
     */
    @Observed(name = "workShift.delete", contextualName = "delete-workshift")
    @DeleteMapping("/{shiftId}")
    @Operation(summary = "删除班次", description = "删除指定的班次配置")
    public ResponseDTO<Void> deleteWorkShift(
            @PathVariable @Parameter(description = "班次ID", required = true) Long shiftId) {
        log.info("[班次管理] 删除班次: shiftId={}", shiftId);

        WorkShiftEntity existingWorkShift = workShiftDao.selectById(shiftId);
        if (existingWorkShift == null) {
            log.warn("[班次管理] 班次不存在: shiftId={}", shiftId);
            return ResponseDTO.error("WORKSHIFT_NOT_FOUND", "班次不存在");
        }

        workShiftDao.deleteById(shiftId);
        log.info("[班次管理] 删除班次成功: shiftId={}", shiftId);

        return ResponseDTO.ok();
    }

    /**
     * 检测班次是否跨天
     * <p>
     * 根据上下班时间判断是否为跨天班次
     * </p>
     *
     * @param startTime 上班时间
     * @param endTime 下班时间
     * @return 检测结果
     */
    @Observed(name = "workShift.checkCrossDay", contextualName = "check-cross-day")
    @GetMapping("/check-cross-day")
    @Operation(summary = "检测跨天班次", description = "根据上下班时间判断是否为跨天班次")
    public ResponseDTO<Boolean> checkCrossDay(
            @Parameter(description = "上班时间", required = true) @RequestParam LocalTime startTime,
            @Parameter(description = "下班时间", required = true) @RequestParam LocalTime endTime) {
        log.info("[班次管理] 检测跨天班次: startTime={}, endTime={}", startTime, endTime);

        boolean isCrossDay = endTime.isBefore(startTime);
        log.info("[班次管理] 检测跨天班次结果: isCrossDay={}", isCrossDay);

        return ResponseDTO.ok(isCrossDay);
    }
}
