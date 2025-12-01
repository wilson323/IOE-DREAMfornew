package net.lab1024.sa.admin.module.consume.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.code.SystemErrorCode;
import net.lab1024.sa.base.common.domain.ResponseDTO;

/**
 * 数据库索引优化管理Controller - 暂时禁用
 * 提供索引分析、性能监控和优化建议功能
 * TODO: 修复DatabaseIndexAnalyzer和IndexAnalysisResult后重新启用
 *
 * @author SmartAdmin Team
 * @date 2025-11-17
 */
@Tag(name = "数据库索引优化管理", description = "数据库索引优化相关接口")
@Slf4j
@RestController
@RequestMapping("/api/consume/index-optimization")
public class IndexOptimizationController {

    /**
     * 分析数据库索引使用情况 - 暂时禁用
     */
    @Operation(summary = "分析数据库索引使用情况", description = "功能暂时禁用，等待修复")
    @GetMapping("/analyze")
    public ResponseDTO<String> analyzeIndexUsage() {
        log.info("数据库索引分析功能暂时禁用，等待修复DatabaseIndexAnalyzer");
        return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "数据库索引分析功能暂时禁用，正在修复中");
    }
}