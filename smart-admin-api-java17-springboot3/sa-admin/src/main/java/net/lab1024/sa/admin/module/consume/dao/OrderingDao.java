package net.lab1024.sa.admin.module.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.consume.domain.entity.OrderingEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 订餐DAO
 *
 * 严格遵循repowiki规范:
 * - 继承BaseMapper，提供基础CRUD操作
 * - 使用@Mapper注解标记
 * - 提供复杂查询接口
 * - 支持报表统计功能
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-17
 */
@Mapper
public interface OrderingDao extends BaseMapper<OrderingEntity> {

    /**
     * 根据日期范围统计订单数量
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计结果
     */
    List<Map<String, Object>> statisticsByDateRange(@Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);

    /**
     * 根据用户ID统计订单数量
     *
     * @param userId 用户ID
     * @return 统计结果
     */
    List<Map<String, Object>> statisticsByUserId(@Param("userId") Long userId);

    /**
     * 根据产品统计销售数量
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计结果
     */
    List<Map<String, Object>> statisticsByProduct(@Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    /**
     * 统计订单状态分布
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 状态分布统计
     */
    List<Map<String, Object>> statisticsByStatus(@Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    /**
     * 查询热销产品排行
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param limit     限制数量
     * @return 热销产品列表
     */
    List<Map<String, Object>> getTopSellingProducts(@Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate,
                                                   @Param("limit") Integer limit);

    /**
     * 查询用户消费排行
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param limit     限制数量
     * @return 用户消费排行
     */
    List<Map<String, Object>> getTopConsumers(@Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate,
                                             @Param("limit") Integer limit);

    /**
     * 按小时统计订单数量
     *
     * @param date 统计日期
     * @return 小时统计结果
     */
    List<Map<String, Object>> statisticsByHour(@Param("date") LocalDate date);

    /**
     * 按月统计订单趋势
     *
     * @param startDate 开始月份
     * @param endDate   结束月份
     * @return 月度趋势统计
     */
    List<Map<String, Object>> monthlyTrendStatistics(@Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    // ==================== 点餐服务需要的方法 ====================

    /**
     * 根据用户ID查询订单
     *
     * @param userId 用户ID
     * @return 订单列表
     */
    List<OrderingEntity> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和状态查询订单
     *
     * @param userId 用户ID
     * @param status 状态
     * @return 订单列表
     */
    List<OrderingEntity> selectByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    /**
     * 更新订单状态
     *
     * @param orderingId 订单ID
     * @param status 状态
     * @return 更新行数
     */
    int updateStatus(@Param("orderingId") Long orderingId, @Param("status") String status);

    /**
     * 取消订单
     *
     * @param orderingId 订单ID
     * @param cancelReason 取消原因
     * @return 更新行数
     */
    int cancelOrdering(@Param("orderingId") Long orderingId, @Param("cancelReason") String cancelReason);

    /**
     * 统计用户订单数量
     *
     * @param userId 用户ID
     * @return 订单数量
     */
    Integer countByUserId(@Param("userId") Long userId);

    /**
     * 统计用户订单总金额
     *
     * @param userId 用户ID
     * @return 总金额
     */
    BigDecimal sumAmountByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和状态统计订单数量
     *
     * @param userId 用户ID
     * @param status 状态
     * @return 订单数量
     */
    Integer countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    /**
     * 统计所有订单数量
     *
     * @return 订单数量
     */
    Integer countAll();

    /**
     * 统计所有订单总金额
     *
     * @return 总金额
     */
    BigDecimal sumAllAmount();

    /**
     * 按状态统计订单数量
     *
     * @return 状态统计
     */
    Map<String, Integer> countByStatus();
}