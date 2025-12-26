package net.lab1024.sa.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.consume.entity.OfflineConsumeRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 离线消费记录DAO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Mapper
public interface OfflineConsumeRecordDao extends BaseMapper<OfflineConsumeRecordEntity> {

    /**
     * 查询用户待同步记录
     */
    List<OfflineConsumeRecordEntity> selectPendingRecordsByUserId(@Param("userId") Long userId);

    /**
     * 查询待同步记录列表
     */
    List<OfflineConsumeRecordEntity> selectPendingRecords(@Param("limit") Integer limit);

    /**
     * 查询未解决冲突记录
     */
    List<OfflineConsumeRecordEntity> selectUnresolvedConflicts(@Param("limit") Integer limit);

    /**
     * 查询用户在指定时间范围内的消费记录
     */
    List<OfflineConsumeRecordEntity> selectByUserIdAndTimeRange(
        @Param("userId") Long userId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 统计用户今日离线消费次数
     */
    Integer countTodayOfflineConsumes(@Param("userId") Long userId);

    /**
     * 统计用户今日离线消费总额
     */
    java.math.BigDecimal sumTodayOfflineAmount(@Param("userId") Long userId);
}
