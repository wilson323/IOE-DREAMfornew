package net.lab1024.sa.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.consume.domain.entity.ConsumeRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 消费分析DAO
 * <p>
 * 提供消费数据分析的数据访问操作
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-24
 */
@Mapper
public interface ConsumeAnalysisDao extends BaseMapper<ConsumeRecordEntity> {

    // ==================== 消费总览统计 ====================

    /**
     * 查询用户总消费金额
     *
     * @param userId     用户ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 总消费金额
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM t_consume_record" +
            " WHERE user_id = #{userId}" +
            "   AND deleted_flag = 0" +
            "   AND transaction_status = 1" +
            "   AND create_time BETWEEN #{startTime} AND #{endTime}")
    BigDecimal selectTotalAmount(@Param("userId") Long userId,
                                 @Param("startTime") LocalDateTime startTime,
                                 @Param("endTime") LocalDateTime endTime);

    /**
     * 查询用户消费次数
     *
     * @param userId     用户ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 消费次数
     */
    @Select("SELECT COUNT(*) FROM t_consume_record" +
            " WHERE user_id = #{userId}" +
            "   AND deleted_flag = 0" +
            "   AND transaction_status = 1" +
            "   AND create_time BETWEEN #{startTime} AND #{endTime}")
    Integer selectTotalCount(@Param("userId") Long userId,
                            @Param("startTime") LocalDateTime startTime,
                            @Param("endTime") LocalDateTime endTime);

    /**
     * 查询用户消费天数
     *
     * @param userId     用户ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 消费天数
     */
    @Select("SELECT COUNT(DISTINCT DATE(create_time)) FROM t_consume_record" +
            " WHERE user_id = #{userId}" +
            "   AND deleted_flag = 0" +
            "   AND transaction_status = 1" +
            "   AND create_time BETWEEN #{startTime} AND #{endTime}")
    Integer selectConsumeDays(@Param("userId") Long userId,
                             @Param("startTime") LocalDateTime startTime,
                             @Param("endTime") LocalDateTime endTime);

    // ==================== 趋势数据 ====================

    /**
     * 查询每日消费趋势
     *
     * @param userId     用户ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 每日消费数据列表（日期, 金额, 次数）
     */
    @Select("SELECT DATE(create_time) as date," +
            "       COALESCE(SUM(amount), 0) as amount," +
            "       COUNT(*) as count" +
            " FROM t_consume_record" +
            " WHERE user_id = #{userId}" +
            "   AND deleted_flag = 0" +
            "   AND transaction_status = 1" +
            "   AND create_time BETWEEN #{startTime} AND #{endTime}" +
            " GROUP BY DATE(create_time)" +
            " ORDER BY date")
    List<Map<String, Object>> selectDailyTrend(@Param("userId") Long userId,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);

    // ==================== 分类统计 ====================

    /**
     * 查询分类消费统计
     *
     * @param userId     用户ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 分类消费数据列表（分类ID, 分类名称, 金额, 次数）
     */
    @Select("SELECT cr.meal_category_id as categoryId," +
            "       mc.category_name as categoryName," +
            "       COALESCE(SUM(cr.amount), 0) as amount," +
            "       COUNT(*) as count" +
            " FROM t_consume_record cr" +
            " LEFT JOIN t_consume_meal_category mc ON cr.meal_category_id = mc.category_id" +
            " WHERE cr.user_id = #{userId}" +
            "   AND cr.deleted_flag = 0" +
            "   AND cr.transaction_status = 1" +
            "   AND cr.create_time BETWEEN #{startTime} AND #{endTime}" +
            " GROUP BY cr.meal_category_id, mc.category_name" +
            " ORDER BY amount DESC")
    List<Map<String, Object>> selectCategoryStats(@Param("userId") Long userId,
                                                  @Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime);

    // ==================== 消费习惯分析 ====================

    /**
     * 查询最常消费时段
     *
     * @param userId     用户ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 时段消费数据列表（小时, 消费次数）
     */
    @Select("SELECT HOUR(create_time) as hour, COUNT(*) as count" +
            " FROM t_consume_record" +
            " WHERE user_id = #{userId}" +
            "   AND deleted_flag = 0" +
            "   AND transaction_status = 1" +
            "   AND create_time BETWEEN #{startTime} AND #{endTime}" +
            " GROUP BY HOUR(create_time)" +
            " ORDER BY count DESC" +
            " LIMIT 1")
    Map<String, Object> selectMostFrequentTime(@Param("userId") Long userId,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);

    /**
     * 查询最喜欢的品类
     *
     * @param userId     用户ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 最喜欢的品类（分类ID, 分类名称, 消费次数）
     */
    @Select("SELECT cr.meal_category_id as categoryId," +
            "       mc.category_name as categoryName," +
            "       COUNT(*) as count" +
            " FROM t_consume_record cr" +
            " LEFT JOIN t_consume_meal_category mc ON cr.meal_category_id = mc.category_id" +
            " WHERE cr.user_id = #{userId}" +
            "   AND cr.deleted_flag = 0" +
            "   AND cr.transaction_status = 1" +
            "   AND cr.create_time BETWEEN #{startTime} AND #{endTime}" +
            " GROUP BY cr.meal_category_id, mc.category_name" +
            " ORDER BY count DESC" +
            " LIMIT 1")
    Map<String, Object> selectFavoriteCategory(@Param("userId") Long userId,
                                             @Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime);

    /**
     * 查询平均单笔消费
     *
     * @param userId     用户ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 平均单笔消费
     */
    @Select("SELECT COALESCE(AVG(amount), 0) FROM t_consume_record" +
            " WHERE user_id = #{userId}" +
            "   AND deleted_flag = 0" +
            "   AND transaction_status = 1" +
            "   AND create_time BETWEEN #{startTime} AND #{endTime}")
    BigDecimal selectAveragePerOrder(@Param("userId") Long userId,
                                    @Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime);

    /**
     * 查询最大单笔消费
     *
     * @param userId     用户ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 最大单笔消费
     */
    @Select("SELECT COALESCE(MAX(amount), 0) FROM t_consume_record" +
            " WHERE user_id = #{userId}" +
            "   AND deleted_flag = 0" +
            "   AND transaction_status = 1" +
            "   AND create_time BETWEEN #{startTime} AND #{endTime}")
    BigDecimal selectMaxOrderAmount(@Param("userId") Long userId,
                                    @Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime);

    /**
     * 查询最小单笔消费
     *
     * @param userId     用户ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 最小单笔消费
     */
    @Select("SELECT COALESCE(MIN(amount), 0) FROM t_consume_record" +
            " WHERE user_id = #{userId}" +
            "   AND deleted_flag = 0" +
            "   AND transaction_status = 1" +
            "   AND create_time BETWEEN #{startTime} AND #{endTime}")
    BigDecimal selectMinOrderAmount(@Param("userId") Long userId,
                                    @Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime);
}
