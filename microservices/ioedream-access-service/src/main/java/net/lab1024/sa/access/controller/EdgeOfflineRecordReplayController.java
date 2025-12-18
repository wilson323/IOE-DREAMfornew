package net.lab1024.sa.access.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.service.EdgeOfflineRecordReplayService;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

/**
 * 边缘验证离线记录补录控制器
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@RestController注解
 * - 使用@Resource注入依赖
 * - 使用ResponseDTO统一返回格式
 * - 完整的API文档注解
 * </p>
 * <p>
 * 核心功能：
 * - 手动触发离线记录补录
 * - 查询离线记录统计信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/access/edge/offline")
@Tag(name = "边缘验证离线记录补录", description = "边缘验证离线记录补录相关API")
public class EdgeOfflineRecordReplayController {

    @Resource
    private EdgeOfflineRecordReplayService edgeOfflineRecordReplayService;

    /**
     * 手动触发离线记录补录
     *
     * @return 补录结果
     */
    @PostMapping("/replay")
    @Operation(summary = "手动触发离线记录补录", description = "从Redis中读取离线记录队列，批量补录到数据库")
    public ResponseDTO<EdgeOfflineRecordReplayService.ReplayResult> replayOfflineRecords() {
        log.info("[离线记录补录] 手动触发补录");
        return edgeOfflineRecordReplayService.replayOfflineRecords();
    }

    /**
     * 获取离线记录统计
     *
     * @return 统计结果
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取离线记录统计", description = "统计Redis中缓存的离线记录数量、最早记录时间、最晚记录时间")
    public ResponseDTO<EdgeOfflineRecordReplayService.OfflineRecordStatistics> getOfflineRecordStatistics() {
        log.debug("[离线记录补录] 查询离线记录统计");
        return edgeOfflineRecordReplayService.getOfflineRecordStatistics();
    }
}
