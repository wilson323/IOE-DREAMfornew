package net.lab1024.sa.access.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.form.AntiPassbackConfigForm;
import net.lab1024.sa.access.domain.form.AntiPassbackQueryForm;
import net.lab1024.sa.access.domain.vo.AntiPassbackConfigVO;
import net.lab1024.sa.access.domain.vo.AntiPassbackRecordVO;
import net.lab1024.sa.access.service.AntiPassbackService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

/**
 * 反潜回管理控制器
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@RestController注解
 * - 使用@Resource依赖注入
 * - 遵循四层架构：Controller → Service → Manager → DAO
 * - 使用ResponseDTO统一响应格式
 * </p>
 * <p>
 * 核心职责：
 * - 反潜回验证接口
 * - 反潜回记录查询接口
 * - 反潜回配置管理接口
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/access/anti-passback")
@Tag(name = "反潜回管理", description = "反潜回管理接口，包括反潜回验证、记录查询和配置管理")
public class AntiPassbackController {

    @Resource
    private AntiPassbackService antiPassbackService;

    /**
     * 反潜回验证
     * <p>
     * 检查同一用户是否从正确的门进出
     * </p>
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param inOutStatus 进出状态（1=进, 2=出）
     * @param areaId 区域ID（可选）
     * @return 是否通过验证
     */
    @PostMapping("/verify")
    @Operation(summary = "反潜回验证", description = "检查同一用户是否从正确的门进出")
    public ResponseDTO<Boolean> verifyAntiPassback(
            @Parameter(description = "用户ID", required = true)
            @RequestParam Long userId,
            @Parameter(description = "设备ID", required = true)
            @RequestParam Long deviceId,
            @Parameter(description = "进出状态（1=进, 2=出）", required = true)
            @RequestParam Integer inOutStatus,
            @Parameter(description = "区域ID（可选）")
            @RequestParam(required = false) Long areaId) {
        log.info("[反潜回] 验证请求: userId={}, deviceId={}, inOutStatus={}, areaId={}",
                userId, deviceId, inOutStatus, areaId);

        try {
            return antiPassbackService.verifyAntiPassback(userId, deviceId, inOutStatus, areaId);
        } catch (Exception e) {
            log.error("[反潜回] 验证异常: userId={}, deviceId={}, error={}", userId, deviceId, e.getMessage(), e);
            return ResponseDTO.error("VERIFY_ANTI_PASSBACK_ERROR", "反潜回验证失败: " + e.getMessage());
        }
    }

    /**
     * 记录反潜回验证结果
     * <p>
     * 验证通过后记录本次进出
     * </p>
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param areaId 区域ID
     * @param inOutStatus 进出状态（1=进, 2=出）
     * @param verifyType 验证方式（0=密码, 1=指纹, 2=卡, 11=面部）
     * @return 操作结果
     */
    @PostMapping("/record")
    @Operation(summary = "记录反潜回验证结果", description = "验证通过后记录本次进出")
    public ResponseDTO<Void> recordAntiPassback(
            @Parameter(description = "用户ID", required = true)
            @RequestParam Long userId,
            @Parameter(description = "设备ID", required = true)
            @RequestParam Long deviceId,
            @Parameter(description = "区域ID")
            @RequestParam(required = false) Long areaId,
            @Parameter(description = "进出状态（1=进, 2=出）", required = true)
            @RequestParam Integer inOutStatus,
            @Parameter(description = "验证方式（0=密码, 1=指纹, 2=卡, 11=面部）")
            @RequestParam(required = false) Integer verifyType) {
        log.info("[反潜回] 记录请求: userId={}, deviceId={}, areaId={}, inOutStatus={}, verifyType={}",
                userId, deviceId, areaId, inOutStatus, verifyType);

        try {
            return antiPassbackService.recordAntiPassback(userId, deviceId, areaId, inOutStatus, verifyType);
        } catch (Exception e) {
            log.error("[反潜回] 记录异常: userId={}, deviceId={}, error={}", userId, deviceId, e.getMessage(), e);
            return ResponseDTO.error("RECORD_ANTI_PASSBACK_ERROR", "记录反潜回验证结果失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询反潜回记录
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    @PostMapping("/records/query")
    @Operation(summary = "分页查询反潜回记录", description = "支持多条件查询：用户ID、设备ID、区域ID、进出状态、时间范围")
    public ResponseDTO<PageResult<AntiPassbackRecordVO>> queryRecords(
            @Valid @RequestBody AntiPassbackQueryForm queryForm) {
        log.info("[反潜回] 分页查询记录: pageNum={}, pageSize={}, userId={}, deviceId={}, areaId={}",
                queryForm.getPageNum(), queryForm.getPageSize(), queryForm.getUserId(),
                queryForm.getDeviceId(), queryForm.getAreaId());

        try {
            return antiPassbackService.queryRecords(queryForm);
        } catch (Exception e) {
            log.error("[反潜回] 分页查询异常: error={}", e.getMessage(), e);
            return ResponseDTO.error("QUERY_ANTI_PASSBACK_RECORDS_ERROR", "查询反潜回记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取反潜回配置
     *
     * @param areaId 区域ID
     * @return 反潜回配置
     */
    @GetMapping("/config/{areaId}")
    @Operation(summary = "获取反潜回配置", description = "根据区域ID获取反潜回配置")
    public ResponseDTO<AntiPassbackConfigVO> getConfig(
            @Parameter(description = "区域ID", required = true)
            @PathVariable Long areaId) {
        log.info("[反潜回] 获取配置: areaId={}", areaId);

        try {
            return antiPassbackService.getConfig(areaId);
        } catch (Exception e) {
            log.error("[反潜回] 获取配置异常: areaId={}, error={}", areaId, e.getMessage(), e);
            return ResponseDTO.error("GET_ANTI_PASSBACK_CONFIG_ERROR", "获取反潜回配置失败: " + e.getMessage());
        }
    }

    /**
     * 更新反潜回配置
     *
     * @param configForm 配置表单
     * @return 操作结果
     */
    @PostMapping("/config/update")
    @Operation(summary = "更新反潜回配置", description = "更新区域的反潜回配置（启用状态、时间窗口）")
    public ResponseDTO<Void> updateConfig(
            @Valid @RequestBody AntiPassbackConfigForm configForm) {
        log.info("[反潜回] 更新配置: areaId={}, enabled={}, timeWindow={}",
                configForm.getAreaId(), configForm.getEnabled(), configForm.getTimeWindow());

        try {
            return antiPassbackService.updateConfig(configForm);
        } catch (Exception e) {
            log.error("[反潜回] 更新配置异常: areaId={}, error={}", configForm.getAreaId(), e.getMessage(), e);
            return ResponseDTO.error("UPDATE_ANTI_PASSBACK_CONFIG_ERROR", "更新反潜回配置失败: " + e.getMessage());
        }
    }
}
