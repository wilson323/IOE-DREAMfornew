package net.lab1024.sa.access.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.enumeration.VerifyTypeEnum;
import net.lab1024.sa.access.service.MultiModalAuthenticationService;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 多模态认证管理控制器
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@RestController注解
 * - 使用@Resource注入依赖
 * - 统一使用ResponseDTO响应格式
 * - 遵循RESTful API设计规范
 * </p>
 * <p>
 * 核心职责：
 * - 提供多模态认证管理REST API
 * - 支持认证方式查询和配置
 * - 提供认证方式统计和分析接口
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/access/multi-modal")
@Tag(name = "多模态认证管理", description = "多模态认证管理相关接口")
public class MultiModalAuthenticationController {

    @Resource
    private MultiModalAuthenticationService multiModalAuthenticationService;

    /**
     * 获取所有支持的认证方式
     */
    @GetMapping("/verify-types")
    @Operation(summary = "获取支持的认证方式列表", description = "返回所有支持的认证方式（9种）")
    public ResponseDTO<List<VerifyTypeEnum>> getSupportedVerifyTypes() {
        log.info("[多模态认证] 获取支持的认证方式列表");
        return multiModalAuthenticationService.getSupportedVerifyTypes();
    }

    /**
     * 获取认证方式详情
     */
    @GetMapping("/verify-types/{verifyType}")
    @Operation(summary = "获取认证方式详情", description = "根据认证方式代码获取详情")
    public ResponseDTO<VerifyTypeEnum> getVerifyTypeDetail(
            @Parameter(description = "认证方式代码", required = true, example = "11")
            @PathVariable Integer verifyType) {
        log.info("[多模态认证] 获取认证方式详情: verifyType={}", verifyType);
        return multiModalAuthenticationService.getVerifyTypeDetail(verifyType);
    }

    /**
     * 获取认证方式统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取认证方式统计", description = "获取各认证方式的使用统计信息")
    public ResponseDTO<Object> getVerifyTypeStatistics(
            @Parameter(description = "开始时间", example = "2025-01-01 00:00:00")
            @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间", example = "2025-01-31 23:59:59")
            @RequestParam(required = false) String endTime) {
        log.info("[多模态认证] 获取认证方式统计: startTime={}, endTime={}", startTime, endTime);
        return multiModalAuthenticationService.getVerifyTypeStatistics(startTime, endTime);
    }
}
