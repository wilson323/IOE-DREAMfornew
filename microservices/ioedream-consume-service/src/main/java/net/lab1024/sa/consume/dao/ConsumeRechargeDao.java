package net.lab1024.sa.consume.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import net.lab1024.sa.consume.domain.entity.ConsumeRechargeEntity;

/**
 * 消费充值记录数据访问层
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Mapper
public interface ConsumeRechargeDao extends BaseMapper<ConsumeRechargeEntity> {

    /**
     * 根据用户ID查询充值记录
     *
     * @param userId 用户ID
     * @return 充值记录列表
     */
    List<ConsumeRechargeEntity> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据交易流水号查询
     *
     * @param transactionNo 交易流水号
     * @return 充值记录
     */
    ConsumeRechargeEntity selectByTransactionNo(@Param("transactionNo") String transactionNo);

    /**
     * 根据第三方交易号查询
     *
     * @param thirdPartyNo 第三方交易号
     * @return 充值记录
     */
    ConsumeRechargeEntity selectByThirdPartyNo(@Param("thirdPartyNo") String thirdPartyNo);

    /**
     * 根据批次号查询充值记录
     *
     * @param batchNo 批次号
     * @return 充值记录列表
     */
    List<ConsumeRechargeEntity> selectByBatchNo(@Param("batchNo") String batchNo);

    /**
     * 根据充值状态查询
     *
     * @param rechargeStatus 充值状态
     * @return 充值记录列表
     */
    List<ConsumeRechargeEntity> selectByStatus(@Param("rechargeStatus") Integer rechargeStatus);

    /**
     * 根据充值方式查询
     *
     * @param rechargeWay 充值方式
     * @return 充值记录列表
     */
    List<ConsumeRechargeEntity> selectByRechargeWay(@Param("rechargeWay") Integer rechargeWay);

    /**
     * 根据充值渠道查询
     *
     * @param rechargeChannel 充值渠道
     * @return 充值记录列表
     */
    List<ConsumeRechargeEntity> selectByRechargeChannel(@Param("rechargeChannel") Integer rechargeChannel);

    /**
     * 统计交易流水号数量（用于唯一性检查）
     *
     * @param transactionNo 交易流水号
     * @param excludeId 排除的记录ID
     * @return 数量
     */
    int countByTransactionNo(@Param("transactionNo") String transactionNo, @Param("excludeId") Long excludeId);

    /**
     * 统计第三方交易号数量（用于唯一性检查）
     *
     * @param thirdPartyNo 第三方交易号
     * @param excludeId 排除的记录ID
     * @return 数量
     */
    int countByThirdPartyNo(@Param("thirdPartyNo") String thirdPartyNo, @Param("excludeId") Long excludeId);

    // ==================== 统计相关方法 ====================

    /**
     * 获取基础统计信息
     *
     * @param userId 用户ID（可选）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 基础统计信息
     */
    Map<String, Object> getBasicStatistics(@Param("userId") Long userId,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    /**
     * 获取平均统计信息
     *
     * @param userId 用户ID（可选）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 平均统计信息
     */
    Map<String, Object> getAverageStatistics(@Param("userId") Long userId,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    /**
     * 获取今日统计信息
     *
     * @param userId 用户ID（可选）
     * @return 今日统计信息
     */
    Map<String, Object> getTodayStatistics(@Param("userId") Long userId);

    /**
     * 获取本月统计信息
     *
     * @param userId 用户ID（可选）
     * @return 本月统计信息
     */
    Map<String, Object> getMonthStatistics(@Param("userId") Long userId);

    /**
     * 获取充值方式统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 充值方式统计
     */
    List<Map<String, Object>> getRechargeWayStatistics(@Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);

    /**
     * 获取充值渠道统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 充值渠道统计
     */
    List<Map<String, Object>> getRechargeChannelStatistics(@Param("startDate") LocalDateTime startDate,
                                                           @Param("endDate") LocalDateTime endDate);

    /**
     * 获取充值趋势数据
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 充值趋势数据
     */
    List<Map<String, Object>> getRechargeTrend(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    /**
     * 获取用户充值排行
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param limit 限制数量
     * @return 用户充值排行
     */
    List<Map<String, Object>> getUserRechargeRanking(@Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate,
                                                    @Param("limit") Integer limit);

    /**
     * 获取部门充值统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 部门充值统计
     */
    List<Map<String, Object>> getDepartmentRechargeStatistics(@Param("startDate") LocalDateTime startDate,
                                                              @Param("endDate") LocalDateTime endDate);

    /**
     * 获取充值时间段统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 时间段统计
     */
    List<Map<String, Object>> getRechargeTimeDistribution(@Param("startDate") LocalDateTime startDate,
                                                         @Param("endDate") LocalDateTime endDate);

    // ==================== 批量操作相关方法 ====================

    /**
     * 统计后续交易数量
     *
     * @param userId 用户ID
     * @param rechargeTime 充值时间
     * @return 后续交易数量
     */
    int countSubsequentTransactions(@Param("userId") Long userId,
                                   @Param("rechargeTime") LocalDateTime rechargeTime);

    /**
     * 批量更新充值状态
     *
     * @param recordIds 记录ID列表
     * @param rechargeStatus 充值状态
     * @param operatorId 操作员ID
     * @return 更新条数
     */
    int batchUpdateStatus(@Param("recordIds") List<Long> recordIds,
                         @Param("rechargeStatus") Integer rechargeStatus,
                         @Param("operatorId") Long operatorId);

    /**
     * 批量审核
     *
     * @param recordIds 记录ID列表
     * @param auditStatus 审核状态
     * @param auditorId 审核人ID
     * @param auditorName 审核人姓名
     * @param auditRemark 审核意见
     * @return 更新条数
     */
    int batchAudit(@Param("recordIds") List<Long> recordIds,
                   @Param("auditStatus") Integer auditStatus,
                   @Param("auditorId") Long auditorId,
                   @Param("auditorName") String auditorName,
                   @Param("auditRemark") String auditRemark);

    // ==================== 审核相关方法 ====================

    /**
     * 根据审核状态查询
     *
     * @param auditStatus 审核状态
     * @return 充值记录列表
     */
    List<ConsumeRechargeEntity> selectByAuditStatus(@Param("auditStatus") Integer auditStatus);

    /**
     * 查询待审核记录
     *
     * @return 待审核记录列表
     */
    List<ConsumeRechargeEntity> selectPendingAudit();

    /**
     * 统计审核状态分布
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 审核状态分布
     */
    List<Map<String, Object>> getAuditStatusDistribution(@Param("startDate") LocalDateTime startDate,
                                                       @Param("endDate") LocalDateTime endDate);

    // ==================== 异常监控相关方法 ====================

    /**
     * 查询异常充值记录
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 异常充值记录列表
     */
    List<ConsumeRechargeEntity> selectAbnormalRecharges(@Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);

    /**
     * 查询失败充值记录
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 失败充值记录列表
     */
    List<ConsumeRechargeEntity> selectFailedRecharges(@Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);

    /**
     * 统计失败原因分布
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 失败原因分布
     */
    List<Map<String, Object>> getFailureReasonDistribution(@Param("startDate") LocalDateTime startDate,
                                                          @Param("endDate") LocalDateTime endDate);

    // ==================== 设备相关方法 ====================

    /**
     * 根据设备ID查询充值记录
     *
     * @param deviceId 设备ID
     * @return 充值记录列表
     */
    List<ConsumeRechargeEntity> selectByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 根据设备编码查询充值记录
     *
     * @param deviceCode 设备编码
     * @return 充值记录列表
     */
    List<ConsumeRechargeEntity> selectByDeviceCode(@Param("deviceCode") String deviceCode);

    /**
     * 获取设备充值统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 设备充值统计
     */
    List<Map<String, Object>> getDeviceRechargeStatistics(@Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);

    // ==================== 操作员相关方法 ====================

    /**
     * 根据操作员ID查询充值记录
     *
     * @param operatorId 操作员ID
     * @return 充值记录列表
     */
    List<ConsumeRechargeEntity> selectByOperatorId(@Param("operatorId") Long operatorId);

    /**
     * 获取操作员充值统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 操作员充值统计
     */
    List<Map<String, Object>> getOperatorRechargeStatistics(@Param("startDate") LocalDateTime startDate,
                                                          @Param("endDate") LocalDateTime endDate);

    // ==================== 时间范围查询方法 ====================

    /**
     * 查询指定日期范围的充值记录
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 充值记录列表
     */
    List<ConsumeRechargeEntity> selectByDateRange(@Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    /**
     * 查询今日充值记录
     *
     * @return 今日充值记录列表
     */
    List<ConsumeRechargeEntity> selectTodayRecharges();

    /**
     * 查询本周充值记录
     *
     * @return 本周充值记录列表
     */
    List<ConsumeRechargeEntity> selectWeekRecharges();

    /**
     * 查询本月充值记录
     *
     * @return 本月充值记录列表
     */
    List<ConsumeRechargeEntity> selectMonthRecharges();

    // ==================== 其他实用方法 ====================

    /**
     * 查询最近的充值记录
     *
     * @param limit 限制数量
     * @return 最近的充值记录列表
     */
    List<ConsumeRechargeEntity> selectRecentRecharges(@Param("limit") Integer limit);

    /**
     * 查询大额充值记录
     *
     * @param minAmount 最小金额
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 大额充值记录列表
     */
    List<ConsumeRechargeEntity> selectLargeAmountRecharges(@Param("minAmount") java.math.BigDecimal minAmount,
                                                           @Param("startDate") LocalDateTime startDate,
                                                           @Param("endDate") LocalDateTime endDate);

    /**
     * 统计充值记录总数
     *
     * @param userId 用户ID（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 充值记录总数
     */
    Long countRechargeRecords(@Param("userId") Long userId,
                              @Param("startDate") LocalDateTime startDate,
                              @Param("endDate") LocalDateTime endDate);
}