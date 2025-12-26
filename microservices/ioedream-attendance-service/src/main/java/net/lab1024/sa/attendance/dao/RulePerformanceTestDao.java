package net.lab1024.sa.attendance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.attendance.entity.RulePerformanceTestEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 规则性能测试DAO
 * <p>
 * 提供性能测试数据访问操作
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Mapper
public interface RulePerformanceTestDao extends BaseMapper<RulePerformanceTestEntity> {

    /**
     * 查询最近性能测试列表
     *
     * @param limit 限制数量
     * @return 测试列表
     */
    @Select("SELECT * FROM t_attendance_rule_performance_test " +
            "WHERE deleted_flag = 0 " +
            "ORDER BY create_time DESC " +
            "LIMIT #{limit}")
    List<RulePerformanceTestEntity> queryRecentTests(@Param("limit") Integer limit);

    /**
     * 查询指定状态的测试
     *
     * @param status 测试状态
     * @return 测试列表
     */
    @Select("SELECT * FROM t_attendance_rule_performance_test " +
            "WHERE deleted_flag = 0 AND test_status = #{status} " +
            "ORDER BY create_time DESC")
    List<RulePerformanceTestEntity> queryByStatus(@Param("status") String status);

    /**
     * 查询时间范围内的测试
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 测试列表
     */
    @Select("SELECT * FROM t_attendance_rule_performance_test " +
            "WHERE deleted_flag = 0 " +
            "AND create_time BETWEEN #{startTime} AND #{endTime} " +
            "ORDER BY create_time DESC")
    List<RulePerformanceTestEntity> queryByTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 统计测试数量
     *
     * @param testType 测试类型（可选）
     * @return 测试数量
     */
    @Select("SELECT COUNT(*) FROM t_attendance_rule_performance_test " +
            "WHERE deleted_flag = 0 " +
            "#{testType != null ? 'AND test_type = #{testType}' : ''}")
    Integer countByType(@Param("testType") String testType);
}
