package net.lab1024.sa.attendance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.lab1024.sa.attendance.domain.entity.ShiftsEntity;
import net.lab1024.sa.attendance.domain.query.ShiftsQuery;

import java.util.List;

/**
 * 班次管理服务接口
 *
 * 基于现有ShiftsDao的Service层，提供班次管理的业务逻辑
 * 遵循四层架构：Controller→Service→Manager→DAO
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-25
 */
public interface ShiftsService extends IService<ShiftsEntity> {

    /**
     * 根据班次编码查询班次信息
     *
     * @param shiftCode 班次编码
     * @return 班次信息，不存在时返回null
     */
    ShiftsEntity getByShiftCode(String shiftCode);

    /**
     * 分页查询班次列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    List<ShiftsEntity> queryByPage(ShiftsQuery query);

    /**
     * 根据班次类型查询班次列表
     *
     * @param shiftType 班次类型
     * @return 班次列表
     */
    List<ShiftsEntity> getByShiftType(String shiftType);

    /**
     * 获取所有启用的班次
     *
     * @return 启用的班次列表
     */
    List<ShiftsEntity> getAllEnabledShifts();

    /**
     * 保存或更新班次信息
     *
     * @param shifts 班次信息
     * @return 是否成功
     */
    boolean saveOrUpdateShifts(ShiftsEntity shifts);

    /**
     * 删除班次
     *
     * @param shiftId 班次ID
     * @return 是否成功
     */
    boolean deleteShifts(Long shiftId);

    /**
     * 批量删除班次
     *
     * @param shiftIds 班次ID列表
     * @return 删除数量
     */
    int batchDeleteShifts(List<Long> shiftIds);

    /**
     * 启用或禁用班次
     *
     * @param shiftId 班次ID
     * @param status 状态（true-启用，false-禁用）
     * @return 是否成功
     */
    boolean updateShiftsStatus(Long shiftId, Boolean status);

    /**
     * 批量更新班次状态
     *
     * @param shiftIds 班次ID列表
     * @param status 状态
     * @return 更新数量
     */
    int batchUpdateShiftsStatus(List<Long> shiftIds, Boolean status);

    /**
     * 检查班次编码是否存在
     *
     * @param shiftCode 班次编码
     * @param excludeShiftId 排除的班次ID（用于更新时检查）
     * @return 是否存在
     */
    boolean existsByShiftCode(String shiftCode, Long excludeShiftId);
}