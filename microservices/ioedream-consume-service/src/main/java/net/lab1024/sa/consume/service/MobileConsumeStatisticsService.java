package net.lab1024.sa.consume.service;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.domain.vo.MobileConsumeStatisticsVO;
import net.lab1024.sa.consume.manager.MobileConsumeStatisticsManager;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 移动端消费统计Service
 * <p>
 * 严格遵循CLAUDE.md四层架构规范：Controller → Service → Manager → DAO
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@Tag(name = "移动端消费统计服务", description = "移动端消费统计相关功能")
public class MobileConsumeStatisticsService {

    @Resource
    private MobileConsumeStatisticsManager mobileConsumeStatisticsManager;

    /**
     * 获取移动端消费统计数据
     *
     * @param userId 用户ID
     * @param statisticsType 统计类型：daily(日)、weekly(周)、monthly(月)
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 消费统计数据
     */
    @Operation(summary = "获取移动端消费统计数据", description = "支持日、周、月统计，支持自定义时间范围")
    public ResponseDTO<MobileConsumeStatisticsVO> getConsumeStatistics(
            @Parameter(description = "用户ID", required = true) Long userId,
            @Parameter(description = "统计类型", required = false) String statisticsType,
            @Parameter(description = "开始日期", required = false) LocalDateTime startDate,
            @Parameter(description = "结束日期", required = false) LocalDateTime endDate) {

        log.info("[移动端消费统计Service] 开始统计, userId={}, statisticsType={}, startDate={}, endDate={}",
                userId, statisticsType, startDate, endDate);

        try {
            // 业务逻辑验证
            if (userId == null) {
                return ResponseDTO.error("USER_ID_REQUIRED", "用户ID不能为空");
            }

            // 调用Manager层执行复杂业务逻辑
            MobileConsumeStatisticsVO statistics = mobileConsumeStatisticsManager.getConsumeStatistics(
                    userId, statisticsType, startDate, endDate);

            log.info("[移动端消费统计Service] 统计完成, userId={}, 今日消费次数={}, 今日消费金额={}",
                    userId, statistics.getTodayConsumeCount(), statistics.getTodayConsumeAmount());

            return ResponseDTO.ok(statistics);

        } catch (IllegalArgumentException | net.lab1024.sa.common.exception.ParamException e) {
            log.warn("[移动端消费统计Service] 参数验证失败, userId={}, error={}", userId, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", e.getMessage());
        } catch (net.lab1024.sa.common.exception.BusinessException e) {
            log.warn("[移动端消费统计Service] 统计业务异常, userId={}, code={}, message={}", userId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (net.lab1024.sa.common.exception.SystemException e) {
            log.error("[移动端消费统计Service] 统计系统异常, userId={}, code={}, message={}", userId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("MOBILE_STATISTICS_SYSTEM_ERROR", "获取消费统计系统异常，请稍后重试");
        } catch (Exception e) {
            log.error("[移动端消费统计Service] 统计未知异常, userId={}, error={}", userId, e.getMessage(), e);
            return ResponseDTO.error("MOBILE_STATISTICS_ERROR", "获取消费统计异常：" + e.getMessage());
        }
    }
}



