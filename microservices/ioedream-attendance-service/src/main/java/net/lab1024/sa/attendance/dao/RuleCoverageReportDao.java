package net.lab1024.sa.attendance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.attendance.RuleCoverageReportEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 规则覆盖率报告DAO
 * <p>
 * 提供覆盖率报告的数据访问操作
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Mapper
public interface RuleCoverageReportDao extends BaseMapper<RuleCoverageReportEntity> {

    /**
     * 查询指定日期范围的趋势数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 趋势数据列表
     */
    @Select("""
            SELECT report_date AS date,
                   coverage_rate,
                   tested_rules,
                   total_rules,
                   total_tests,
                   success_rate
            FROM t_attendance_rule_coverage_report
            WHERE report_date BETWEEN #{startDate} AND #{endDate}
              AND deleted_flag = 0
              AND report_status = 'COMPLETED'
            ORDER BY report_date ASC
            """)
    List<RuleCoverageReportEntity> queryTrendData(@Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    /**
     * 查询最近的报告记录
     *
     * @param limit 限制数量
     * @return 报告列表
     */
    @Select("""
            SELECT *
            FROM t_attendance_rule_coverage_report
            WHERE deleted_flag = 0
              AND report_status = 'COMPLETED'
            ORDER BY create_time DESC
            LIMIT #{limit}
            """)
    List<RuleCoverageReportEntity> queryRecentReports(@Param("limit") int limit);

    /**
     * 查询指定日期的报告
     *
     * @param reportDate 报告日期
     * @return 报告实体
     */
    @Select("""
            SELECT *
            FROM t_attendance_rule_coverage_report
            WHERE report_date = #{reportDate}
              AND deleted_flag = 0
            ORDER BY create_time DESC
            LIMIT 1
            """)
    RuleCoverageReportEntity queryByReportDate(@Param("reportDate") LocalDate reportDate);

    /**
     * 检查是否存在指定日期的报告
     *
     * @param reportDate 报告日期
     * @return 报告数量
     */
    @Select("""
            SELECT COUNT(*)
            FROM t_attendance_rule_coverage_report
            WHERE report_date = #{reportDate}
              AND deleted_flag = 0
            """)
    int countByReportDate(@Param("reportDate") LocalDate reportDate);
}
