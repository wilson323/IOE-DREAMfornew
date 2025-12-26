package net.lab1024.sa.attendance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import net.lab1024.sa.common.entity.attendance.DepartmentStatisticsEntity;

import java.util.List;

/**
 * 部门统计数据访问接口
 * <p>
 * 提供部门统计数据的CRUD操作
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Mapper
public interface DepartmentStatisticsDao extends BaseMapper<DepartmentStatisticsEntity> {

    /**
     * 查询部门指定月份的统计记录
     *
     * @param departmentId 部门ID
     * @param statisticsMonth 统计月份
     * @return 统计记录
     */
    @Select("SELECT * FROM t_department_statistics " +
            "WHERE department_id = #{departmentId} " +
            "AND statistics_month = #{statisticsMonth} " +
            "AND status = 1 " +
            "LIMIT 1")
    DepartmentStatisticsEntity selectByDepartmentAndMonth(
            @Param("departmentId") Long departmentId,
            @Param("statisticsMonth") String statisticsMonth
    );

    /**
     * 查询指定月份的所有部门统计记录
     *
     * @param statisticsMonth 统计月份
     * @return 统计记录列表
     */
    @Select("SELECT * FROM t_department_statistics " +
            "WHERE statistics_month = #{statisticsMonth} " +
            "AND status = 1")
    List<DepartmentStatisticsEntity> selectByMonth(
            @Param("statisticsMonth") String statisticsMonth
    );
}
