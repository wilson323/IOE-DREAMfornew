package net.lab1024.sa.attendance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.attendance.domain.entity.ShiftsEntity;
import net.lab1024.sa.attendance.domain.query.ShiftsQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * 班次表DAO接口
 *
 * 基于MyBatis Plus的数据访问层，提供班次管理功能的数据库操作
 * 严格遵循四层架构：Controller→Service→Manager→DAO
 *
 * @author IOE-DREAM Team
 * @since 2025-11-25
 */
@Mapper
@Repository
public interface ShiftsDao extends BaseMapper<ShiftsEntity> {

    /**
     * 根据班次编码查询
     *
     * @param shiftCode 班次编码
     * @return 班次信息
     */
    ShiftsEntity selectByShiftCode(@Param("shiftCode") String shiftCode);

    /**
     * 分页查询班次列表
     *
     * @param page 分页参数
     * @param query 查询条件
     * @return 班次列表
     */
    Page<ShiftsEntity> selectPageByQuery(Page<ShiftsEntity> page, @Param("query") ShiftsQuery query);

    /**
     * 根据班次类型查询列表
     *
     * @param shiftType 班次类型
     * @return 班次列表
     */
    List<ShiftsEntity> selectByShiftType(@Param("shiftType") String shiftType);

    /**
     * 查询启用的班次列表
     *
     * @return 启用的班次列表
     */
    List<ShiftsEntity> selectEnabledShifts();

    /**
     * 根据工作时长范围查询班次
     *
     * @param minHours 最小工作时长
     * @param maxHours 最大工作时长
     * @return 班次列表
     */
    List<ShiftsEntity> selectByWorkHoursRange(@Param("minHours") BigDecimal minHours, @Param("maxHours") BigDecimal maxHours);

    /**
     * 查询弹性班次列表
     *
     * @return 弹性班次列表
     */
    List<ShiftsEntity> selectFlexibleShifts();

    /**
     * 查询轮班班次列表
     *
     * @return 轮班班次列表
     */
    List<ShiftsEntity> selectRotatingShifts();

    /**
     * 根据排序值查询班次列表
     *
     * @return 班次列表
     */
    List<ShiftsEntity> selectOrderBySortOrder();

    /**
     * 查询班次使用统计
     *
     * @return 班次使用统计
     */
    List<ShiftsEntity> selectUsageStatistics();

    /**
     * 批量插入班次
     *
     * @param shifts 班次列表
     * @return 插入记录数
     */
    int batchInsert(@Param("shifts") List<ShiftsEntity> shifts);

    /**
     * 批量更新班次状态
     *
     * @param shiftIds 班次ID列表
     * @param status 状态
     * @return 更新记录数
     */
    int batchUpdateStatus(@Param("shiftIds") List<Long> shiftIds, @Param("status") Boolean status);

    /**
     * 统计各类型班次数量
     *
     * @return 班次类型统计
     */
    List<ShiftsEntity> countByShiftType();

    /**
     * 查询班次最大排序值
     *
     * @return 最大排序值
     */
    Integer selectMaxSortOrder();

    /**
     * 更新班次排序
     *
     * @param shiftId 班次ID
     * @param sortOrder 排序值
     * @return 更新记录数
     */
    int updateSortOrder(@Param("shiftId") Long shiftId, @Param("sortOrder") Integer sortOrder);

    /**
     * 查询班次的时间段关联信息
     *
     * @param shiftId 班次ID
     * @return 时间段关联信息
     */
    List<ShiftsEntity> selectShiftPeriodRelations(@Param("shiftId") Long shiftId);

    /**
     * 检查班次编码是否存在
     *
     * @param shiftCode 班次编码
     * @param excludeShiftId 排除的班次ID
     * @return 存在数量
     */
    int countByShiftCodeExclude(@Param("shiftCode") String shiftCode, @Param("excludeShiftId") Long excludeShiftId);
}