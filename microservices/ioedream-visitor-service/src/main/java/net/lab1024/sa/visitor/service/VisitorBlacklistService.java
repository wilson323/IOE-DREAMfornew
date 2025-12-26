package net.lab1024.sa.visitor.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.visitor.domain.form.BlacklistAddForm;
import net.lab1024.sa.visitor.domain.form.BlacklistQueryForm;
import net.lab1024.sa.visitor.domain.vo.BlacklistVO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 访客黑名单管理服务接口
 * <p>
 * 内存优化设计原则：
 * - 接口精简，职责单一
 * - 使用异步处理，提高并发性能
 * - 熔断器保护，防止级联故障
 * - 批量操作支持，减少IO开销
 * - 合理的缓存策略，减少数据库压力
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface VisitorBlacklistService {

    /**
     * 添加到黑名单
     * <p>
     * 将访客添加到黑名单中
     * 支持永久黑名单和临时黑名单
     * 自动生效，实时生效
     * </p>
     *
     * @param form 黑名单添加表单
     * @return 添加结果
     */
    @CircuitBreaker(name = "visitorBlacklist", fallbackMethod = "addToBlacklistFallback")
    @TimeLimiter(name = "visitorBlacklist")
    CompletableFuture<ResponseDTO<Long>> addToBlacklist(BlacklistAddForm form);

    /**
     * 从黑名单移除
     * <p>
     * 将访客从黑名单中移除
     * 立即生效，解除所有限制
     * 记录移除操作日志
     * </p>
     *
     * @param visitorId 访客ID
     * @param reason 移除原因
     * @return 移除结果
     */
    @CircuitBreaker(name = "visitorBlacklist", fallbackMethod = "removeFromBlacklistFallback")
    @TimeLimiter(name = "visitorBlacklist")
    CompletableFuture<ResponseDTO<Void>> removeFromBlacklist(Long visitorId, String reason);

    /**
     * 查询黑名单
     * <p>
     * 分页查询黑名单记录
     * 支持多种条件筛选
     * 按时间倒序排列
     * </p>
     *
     * @param form 查询表单
     * @return 黑名单分页列表
     */
    @CircuitBreaker(name = "visitorBlacklist")
    CompletableFuture<ResponseDTO<PageResult<BlacklistVO>>> queryBlacklist(BlacklistQueryForm form);

    /**
     * 检查黑名单状态
     * <p>
     * 检查访客是否在黑名单中
     * 支持身份证号和手机号查询
     * 实时检查，缓存优化
     * </p>
     *
     * @param visitorId 访客ID
     * @param idCard 身份证号
     * @param phone 手机号
     * @return 黑名单状态
     */
    @CircuitBreaker(name = "visitorBlacklist")
    CompletableFuture<ResponseDTO<Boolean>> checkBlacklistStatus(
            Long visitorId,
            String idCard,
            String phone
    );

    /**
     * 批量检查黑名单
     * <p>
     * 批量检查多个访客的黑名单状态
     * 高效批量查询，减少数据库访问
     * 适用于批量验证场景
     * </p>
     *
     * @param visitorIds 访客ID列表
     * @return 黑名单状态映射
     */
    @CircuitBreaker(name = "visitorBlacklist")
    CompletableFuture<ResponseDTO<List<Long>>> batchCheckBlacklistStatus(List<Long> visitorIds);

    /**
     * 更新黑名单状态
     * <p>
     * 更新黑名单记录的状态
     * 支持激活和停用操作
     * 自动清理过期记录
     * </p>
     *
     * @param blacklistId 黑名单记录ID
     * @param status 新状态
     * @return 更新结果
     */
    @CircuitBreaker(name = "visitorBlacklist", fallbackMethod = "updateBlacklistStatusFallback")
    @TimeLimiter(name = "visitorBlacklist")
    CompletableFuture<ResponseDTO<Void>> updateBlacklistStatus(Long blacklistId, Integer status);

    /**
     * 清理过期黑名单
     * <p>
     * 清理已过期的临时黑名单记录
     * 定时任务调用，维护数据一致性
     * 释放系统资源
     * </p>
     *
     * @return 清理统计结果
     */
    @CircuitBreaker(name = "visitorBlacklist")
    CompletableFuture<ResponseDTO<Integer>> cleanExpiredBlacklist();

    /**
     * 获取黑名单统计
     * <p>
     * 获取黑名单统计数据
     * 包括总数、分类统计、趋势分析
     * 用于管理决策支持
     * </p>
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计数据
     */
    @CircuitBreaker(name = "visitorBlacklist")
    CompletableFuture<ResponseDTO<Object>> getBlacklistStatistics(
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    // ==================== 降级处理方法 ====================

    /**
     * 添加黑名单降级处理
     */
    default CompletableFuture<ResponseDTO<Long>> addToBlacklistFallback(
            BlacklistAddForm form,
            Exception exception
    ) {
        return CompletableFuture.completedFuture(
                ResponseDTO.error("BLACKLIST_SERVICE_UNAVAILABLE", "黑名单服务暂时不可用，请稍后重试")
        );
    }

    /**
     * 移除黑名单降级处理
     */
    default CompletableFuture<ResponseDTO<Void>> removeFromBlacklistFallback(
            Long visitorId,
            String reason,
            Exception exception
    ) {
        return CompletableFuture.completedFuture(
                ResponseDTO.error("BLACKLIST_SERVICE_UNAVAILABLE", "黑名单服务暂时不可用，请稍后重试")
        );
    }

    /**
     * 更新黑名单状态降级处理
     */
    default CompletableFuture<ResponseDTO<Void>> updateBlacklistStatusFallback(
            Long blacklistId,
            Integer status,
            Exception exception
    ) {
        return CompletableFuture.completedFuture(
                ResponseDTO.error("BLACKLIST_SERVICE_UNAVAILABLE", "黑名单服务暂时不可用，请稍后重试")
        );
    }
}
