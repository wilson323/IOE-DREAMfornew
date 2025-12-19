package net.lab1024.sa.access.service;

import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 边缘验证离线记录补录服务接口
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 服务接口定义
 * - 使用ResponseDTO统一返回格式
 * </p>
 * <p>
 * 核心职责：
 * - 定时补录Redis中缓存的离线记录
 * - 网络恢复后自动同步离线记录到数据库
 * - 支持手动触发补录
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface EdgeOfflineRecordReplayService {

    /**
     * 补录离线记录
     * <p>
     * 从Redis中读取离线记录队列，批量补录到数据库
     * </p>
     *
     * @return 补录结果（成功数量、失败数量）
     */
    ResponseDTO<ReplayResult> replayOfflineRecords();

    /**
     * 获取离线记录统计
     * <p>
     * 统计Redis中缓存的离线记录数量
     * </p>
     *
     * @return 统计结果（总数量、最早记录时间、最晚记录时间）
     */
    ResponseDTO<OfflineRecordStatistics> getOfflineRecordStatistics();

    /**
     * 补录结果
     */
    interface ReplayResult {
        /**
         * 总记录数
         */
        int getTotalCount();

        /**
         * 成功数量
         */
        int getSuccessCount();

        /**
         * 失败数量
         */
        int getFailCount();

        /**
         * 重复数量（幂等性检查）
         */
        int getDuplicateCount();

        /**
         * 处理耗时（毫秒）
         */
        long getProcessTime();
    }

    /**
     * 离线记录统计
     */
    interface OfflineRecordStatistics {
        /**
         * 总记录数
         */
        int getTotalCount();

        /**
         * 设置总记录数
         */
        void setTotalCount(int totalCount);

        /**
         * 最早记录时间
         */
        String getEarliestRecordTime();

        /**
         * 设置最早记录时间
         */
        void setEarliestRecordTime(String earliestRecordTime);

        /**
         * 最晚记录时间
         */
        String getLatestRecordTime();

        /**
         * 设置最晚记录时间
         */
        void setLatestRecordTime(String latestRecordTime);
    }
}
