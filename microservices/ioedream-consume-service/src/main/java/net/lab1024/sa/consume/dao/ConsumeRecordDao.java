package net.lab1024.sa.consume.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.common.consume.entity.ConsumeRecordEntity;

/**
 * 消费记录DAO接口
 * <p>
 * 用于消费记录的数据访问操作
 * 严格遵循CLAUDE.md规范：
 * - 使用@Mapper注解标识
 * - 继承BaseMapper<Entity>
 * - 使用Dao后缀命名
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@org.apache.ibatis.annotations.Mapper
public interface ConsumeRecordDao extends BaseMapper<ConsumeRecordEntity> {

    /**
     * 根据用户ID查询消费记录
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 消费记录列表
     */
    List<ConsumeRecordEntity> selectByUserId(@Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 根据账户ID查询消费记录
     *
     * @param accountId 账户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 消费记录列表
     */
    List<ConsumeRecordEntity> selectByAccountId(@Param("accountId") Long accountId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 根据交易流水号查询消费记录
     *
     * @param transactionNo 交易流水号
     * @return 消费记录
     */
    ConsumeRecordEntity selectByTransactionNo(@Param("transactionNo") String transactionNo);

    /**
     * 根据时间范围查询消费记录
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 消费记录列表
     */
    List<ConsumeRecordEntity> selectByTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 根据设备ID列表和时间范围查询消费记录
     *
     * @param deviceIds 设备ID列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 消费记录列表
     */
    List<ConsumeRecordEntity> selectByDeviceIdsAndTimeRange(
            @Param("deviceIds") List<Long> deviceIds,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
