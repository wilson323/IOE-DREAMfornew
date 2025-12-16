package net.lab1024.sa.consume.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.transaction.annotation.Transactional;

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
    @Select("SELECT * FROM t_consume_record WHERE user_id = #{userId} " +
            "AND create_time >= #{startTime} AND create_time <= #{endTime} " +
            "AND deleted_flag = 0 ORDER BY create_time DESC")
    List<ConsumeRecordEntity> selectByUserIdAndTimeRange(@Param("userId") Long userId,
                                                         @Param("startTime") LocalDateTime startTime,
                                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 根据用户ID查询消费记录（兼容方法）
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 消费记录列表
     */
    @Select("SELECT * FROM t_consume_record WHERE user_id = #{userId} " +
            "AND create_time >= #{startTime} AND create_time <= #{endTime} " +
            "AND deleted_flag = 0 ORDER BY create_time DESC")
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

    // ==================== SAGA事务支持的业务方法 ====================

    /**
     * 根据订单号删除消费记录（SAGA补偿操作）
     *
     * @param orderNo 订单号
     * @return 删除的记录数
     */
    @Transactional(rollbackFor = Exception.class)
    @Delete("DELETE FROM t_consume_record WHERE order_no = #{orderNo} AND deleted_flag = 0")
    int deleteByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 根据订单号查询消费记录
     *
     * @param orderNo 订单号
     * @return 消费记录
     */
    @Select("SELECT * FROM t_consume_record WHERE order_no = #{orderNo} AND deleted_flag = 0")
    ConsumeRecordEntity selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 统计用户在指定时间范围内的消费次数
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 消费次数
     */
    @Select("SELECT COUNT(*) FROM t_consume_record WHERE user_id = #{userId} " +
            "AND create_time >= #{startTime} AND create_time <= #{endTime} AND deleted_flag = 0")
    int countByUserId(@Param("userId") Long userId,
                      @Param("startTime") LocalDateTime startTime,
                      @Param("endTime") LocalDateTime endTime);

    /**
     * 统计用户在指定时间范围内的消费总金额
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 消费总金额
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM t_consume_record WHERE user_id = #{userId} " +
            "AND create_time >= #{startTime} AND create_time <= #{endTime} AND deleted_flag = 0")
    java.math.BigDecimal sumAmountByUserId(@Param("userId") Long userId,
                                           @Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 查询用户最近的消费记录
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 消费记录列表
     */
    @Select("SELECT * FROM t_consume_record WHERE user_id = #{userId} AND deleted_flag = 0 " +
            "ORDER BY create_time DESC LIMIT #{limit}")
    List<ConsumeRecordEntity> selectRecentByUserId(@Param("userId") Long userId,
                                                  @Param("limit") Integer limit);

    /**
     * 根据状态查询消费记录
     *
     * @param status 状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 消费记录列表
     */
    @Select("SELECT * FROM t_consume_record WHERE status = #{status} " +
            "AND create_time >= #{startTime} AND create_time <= #{endTime} AND deleted_flag = 0")
    List<ConsumeRecordEntity> selectByStatus(@Param("status") String status,
                                           @Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);
}



