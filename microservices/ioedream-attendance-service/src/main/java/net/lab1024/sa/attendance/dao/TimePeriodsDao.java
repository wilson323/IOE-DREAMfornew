package net.lab1024.sa.attendance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.attendance.domain.entity.TimePeriodsEntity;
import net.lab1024.sa.attendance.domain.query.TimePeriodsQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

/**
 * 时间段表DAO接口
 *
 * 基于MyBatis Plus的数据访问层，提供时间段管理功能的数据库操作
 * 严格遵循四层架构：Controller→Service→Manager→DAO
 *
 * @author IOE-DREAM Team
 * @since 2025-11-25
 */
@Mapper
@Repository
public interface TimePeriodsDao extends BaseMapper<TimePeriodsEntity> {

    /**
     * 根据时间段编码查询
     *
     * @param periodCode 时间段编码
     * @return 时间段信息
     */
    TimePeriodsEntity selectByPeriodCode(@Param("periodCode") String periodCode);

    /**
     * 分页查询时间段列表
     *
     * @param page 分页参数
     * @param query 查询条件
     * @return 时间段列表
     */
    Page<TimePeriodsEntity> selectPageByQuery(Page<TimePeriodsEntity> page, @Param("query") TimePeriodsQuery query);

    /**
     * 根据工作类型查询时间段列表
     *
     * @param workType 工作类型
     * @return 时间段列表
     */
    List<TimePeriodsEntity> selectByWorkType(@Param("workType") String workType);

    /**
     * 查询启用的工作时间段列表
     *
     * @return 工作时间段列表
     */
    List<TimePeriodsEntity> selectEnabledWorkPeriods();

    /**
     * 查询启用的时间段列表（按时间排序）
     *
     * @return 时间段列表
     */
    List<TimePeriodsEntity> selectEnabledPeriodsOrderByTime();

    /**
     * 查询跨日时间段列表
     *
     * @return 跨日时间段列表
     */
    List<TimePeriodsEntity> selectCrossDayPeriods();

    /**
     * 根据时间范围查询时间段
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 时间段列表
     */
    List<TimePeriodsEntity> selectByTimeRange(@Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime);

    /**
     * 查询时间段冲突
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param excludePeriodId 排除的时间段ID
     * @return 冲突的时间段数量
     */
    int countTimeConflicts(@Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime, @Param("excludePeriodId") Long excludePeriodId);

    /**
     * 批量插入时间段
     *
     * @param periods 时间段列表
     * @return 插入记录数
     */
    int batchInsert(@Param("periods") List<TimePeriodsEntity> periods);

    /**
     * 批量更新时间段状态
     *
     * @param periodIds 时间段ID列表
     * @param status 状态
     * @return 更新记录数
     */
    int batchUpdateStatus(@Param("periodIds") List<Long> periodIds, @Param("status") Boolean status);

    /**
     * 统计各工作类型时间段数量
     *
     * @return 工作类型统计
     */
    List<TimePeriodsEntity> countByWorkType();

    /**
     * 查询时间段最大排序值
     *
     * @return 最大排序值
     */
    Integer selectMaxSortOrder();

    /**
     * 更新时间段排序
     *
     * @param periodId 时间段ID
     * @param sortOrder 排序值
     * @return 更新记录数
     */
    int updateSortOrder(@Param("periodId") Long periodId, @Param("sortOrder") Integer sortOrder);
}