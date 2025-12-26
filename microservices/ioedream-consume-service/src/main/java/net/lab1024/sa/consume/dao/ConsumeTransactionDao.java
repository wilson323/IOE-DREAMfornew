package net.lab1024.sa.consume.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.consume.domain.form.ConsumeTransactionQueryForm;
import net.lab1024.sa.consume.domain.vo.ConsumeTransactionVO;
import net.lab1024.sa.common.entity.consume.ConsumeTransactionEntity;

/**
 * 消费交易数据访问对象
 * <p>
 * 遵循MyBatis-Plus规范，使用@Mapper注解而非@Repository
 * 提供交易相关的数据访问操作
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Mapper
public interface ConsumeTransactionDao extends BaseMapper<ConsumeTransactionEntity> {

    /**
     * 分页查询交易记录
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    PageResult<ConsumeTransactionVO> queryPage(@Param("queryForm") ConsumeTransactionQueryForm queryForm);

    /**
     * 根据交易ID查询详细信息
     *
     * @param transactionId 交易ID
     * @return 交易详情
     */
    ConsumeTransactionVO selectDetailById(@Param("transactionId") String transactionId);

    /**
     * 根据日期范围查询交易记录
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param deviceId 设备ID（可选）
     * @return 交易记录列表
     */
    List<ConsumeTransactionVO> selectByDateRange(@Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime,
                                               @Param("deviceId") String deviceId);

    /**
     * 获取交易统计信息
     *
     * @param params 查询参数（包含userId、deviceId、startDate、endDate等）
     * @return 统计信息
     */
    Map<String, Object> getStatistics(@Param("params") Map<String, Object> params);

    /**
     * 获取交易趋势数据
     *
     * @param params 查询参数（包含userId、deviceId、startDate、endDate等）
     * @return 趋势数据
     */
    Map<String, Object> getTransactionTrend(@Param("params") Map<String, Object> params);

    /**
     * 获取账户交易统计信息
     *
     * @param accountId 账户ID
     * @return 统计信息
     */
    Map<String, Object> getAccountStatistics(@Param("accountId") Long accountId);

    /**
     * 更新交易状态
     *
     * @param transactionId 交易ID
     * @param status 新状态
     * @return 更新行数
     */
    int updateStatus(@Param("transactionId") String transactionId, @Param("status") String status);

    /**
     * 根据用户ID查询交易记录
     *
     * @param userId 用户ID
     * @param limit 限制条数
     * @return 交易记录列表
     */
    List<ConsumeTransactionVO> selectByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 根据设备ID查询交易记录
     *
     * @param deviceId 设备ID
     * @param limit 限制条数
     * @return 交易记录列表
     */
    List<ConsumeTransactionVO> selectByDeviceId(@Param("deviceId") String deviceId, @Param("limit") Integer limit);

    /**
     * 获取指定日期的交易汇总信息
     *
     * @param date 日期
     * @return 汇总信息
     */
    Map<String, Object> getTransactionSummary(@Param("date") String date);

    /**
     * 查询异常交易记录
     *
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 异常交易记录
     */
    PageResult<ConsumeTransactionVO> selectAbnormalTransactions(@Param("pageNum") Integer pageNum,
                                                              @Param("pageSize") Integer pageSize);
    /**
     * 统计指定日期范围内的交易数量
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 交易数量
     */
    int countByDateRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 汇总指定日期范围内的交易金额
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 交易总金额
     */
    java.math.BigDecimal sumAmountByDateRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}

