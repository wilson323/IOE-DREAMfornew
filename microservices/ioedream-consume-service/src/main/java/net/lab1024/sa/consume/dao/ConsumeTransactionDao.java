package net.lab1024.sa.consume.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import net.lab1024.sa.common.consume.entity.ConsumeTransactionEntity;

/**
 * 消费交易DAO接口
 * <p>
 * 用于消费交易的数据访问操作
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
public interface ConsumeTransactionDao extends BaseMapper<ConsumeTransactionEntity> {

    /**
     * 根据用户ID查询交易记录
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 交易记录列表
     */
    List<ConsumeTransactionEntity> selectByUserId(@Param("userId") String userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 根据账户ID查询交易记录
     *
     * @param accountId 账户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 交易记录列表
     */
    List<ConsumeTransactionEntity> selectByAccountId(@Param("accountId") String accountId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 根据交易流水号查询交易记录
     *
     * @param transactionNo 交易流水号
     * @return 交易记录
     */
    ConsumeTransactionEntity selectByTransactionNo(@Param("transactionNo") String transactionNo);

    /**
     * 根据区域ID查询交易记录
     *
     * @param areaId 区域ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 交易记录列表
     */
    List<ConsumeTransactionEntity> selectByAreaId(@Param("areaId") String areaId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 根据时间范围查询交易记录
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 交易记录列表
     */
    List<ConsumeTransactionEntity> selectByTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 根据用户ID和时间范围查询交易记录
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 交易记录列表
     */
    List<ConsumeTransactionEntity> selectByUserIdAndTimeRange(
            @Param("userId") String userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 分页查询交易记录
     * <p>
     * 支持多条件组合查询：
     * - 用户ID筛选
     * - 区域ID筛选
     * - 时间范围筛选
     * - 消费模式筛选
     * - 交易状态筛选
     * </p>
	     *
	     * @param page 分页参数
	     * @param userId 用户ID（可选）
	     * @param transactionNo 交易流水号（可选）
	     * @param deviceId 设备ID（可选）
	     * @param areaId 区域ID（可选）
	     * @param startTime 开始时间（可选）
	     * @param endTime 结束时间（可选）
	     * @param consumeMode 消费模式（可选）
	     * @param status 交易状态（可选）
	     * @return 分页结果
	     */
	    IPage<ConsumeTransactionEntity> queryTransactions(
	            Page<ConsumeTransactionEntity> page,
	            @Param("userId") Long userId,
	            @Param("transactionNo") String transactionNo,
	            @Param("deviceId") Long deviceId,
	            @Param("areaId") String areaId,
	            @Param("startTime") LocalDateTime startTime,
	            @Param("endTime") LocalDateTime endTime,
	            @Param("consumeMode") String consumeMode,
	            @Param("status") String status);
}



