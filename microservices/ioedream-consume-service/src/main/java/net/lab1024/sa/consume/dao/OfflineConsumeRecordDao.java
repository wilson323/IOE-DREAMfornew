package net.lab1024.sa.consume.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.consume.entity.OfflineConsumeRecordEntity;

/**
 * 离线消费记录DAO
 * <p>
 * 说明：
 * - 该DAO用于离线消费记录的存储与同步状态更新
 * - 由离线同步管理器（OfflineSyncManager）调用
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-19
 */
@Mapper
public interface OfflineConsumeRecordDao extends BaseMapper<OfflineConsumeRecordEntity> {

        /**
         * 按离线交易号查询离线记录
         *
         * @param offlineTransNo 离线交易号
         * @return 离线记录；不存在返回null
         */
        OfflineConsumeRecordEntity selectByOfflineTransNo(@Param("offlineTransNo") String offlineTransNo);

        /**
         * 查询待同步记录（分页）
         *
         * @param offset 偏移量
         * @param limit  条数
         * @return 待同步记录列表
         */
        List<OfflineConsumeRecordEntity> selectPendingRecords(@Param("offset") int offset, @Param("limit") int limit);

        /**
         * 更新同步状态
         *
         * @param id          离线记录ID
         * @param syncStatus  同步状态
         * @param syncTime    同步时间
         * @param syncMessage 同步消息
         * @param retryCount  重试次数
         * @return 更新行数
         */
        int updateSyncStatus(
                        @Param("id") Long id,
                        @Param("syncStatus") String syncStatus,
                        @Param("syncTime") LocalDateTime syncTime,
                        @Param("syncMessage") String syncMessage,
                        @Param("retryCount") Integer retryCount);

        /**
         * 标记冲突
         * <p>
         * 使用场景：余额不足、账户不存在、参数缺失等导致离线记录无法自动同步的情况
         * </p>
         *
         * @param id           离线记录ID
         * @param conflictType 冲突类型（如BALANCE/ACCOUNT/DEVICE等）
         * @param message      冲突描述
         * @return 更新行数
         */
        int markConflict(
                        @Param("id") Long id,
                        @Param("conflictType") String conflictType,
                        @Param("message") String message);
}
