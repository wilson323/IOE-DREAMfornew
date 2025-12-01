package net.lab1024.sa.admin.module.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.lab1024.sa.admin.module.attendance.dao.ShiftsDao;
import net.lab1024.sa.admin.module.attendance.domain.entity.ShiftsEntity;
import net.lab1024.sa.admin.module.attendance.domain.query.ShiftsQuery;
import net.lab1024.sa.admin.module.attendance.service.ShiftsService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 班次管理服务实现类
 *
 * 基于现有ShiftsDao的Service层实现，提供班次管理的完整业务逻辑
 * 遵循四层架构：Controller→Service→Manager→DAO
 * 严格遵循repowiki编码规范
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-25
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ShiftsServiceImpl extends ServiceImpl<ShiftsDao, ShiftsEntity> implements ShiftsService {

    private static final Logger log = LoggerFactory.getLogger(ShiftsServiceImpl.class);

    @Override
    public ShiftsEntity getByShiftCode(String shiftCode) {
        if (shiftCode == null || shiftCode.trim().isEmpty()) {
            log.warn("班次编码不能为空");
            return null;
        }

        try {
            log.debug("查询班次信息: 班次编码={}", shiftCode);
            ShiftsEntity shifts = baseMapper.selectByShiftCode(shiftCode);
            log.debug("查询班次信息完成: 班次编码={}, 是否存在={}", shiftCode, shifts != null);
            return shifts;
        } catch (Exception e) {
            log.error("查询班次信息异常: 班次编码" + shiftCode, e);
            throw new RuntimeException("查询班次信息失败", e);
        }
    }

    @Override
    public List<ShiftsEntity> queryByPage(ShiftsQuery query) {
        log.debug("分页查询班次列表: 查询条件={}", query);

        try {
            // 这里简化实现，实际应该使用Page对象
            QueryWrapper<ShiftsEntity> queryWrapper = new QueryWrapper<>();

            if (query.getShiftCode() != null && !query.getShiftCode().trim().isEmpty()) {
                queryWrapper.like("shift_code", query.getShiftCode());
            }

            if (query.getShiftName() != null && !query.getShiftName().trim().isEmpty()) {
                queryWrapper.like("shift_name", query.getShiftName());
            }

            if (query.getShiftType() != null && !query.getShiftType().trim().isEmpty()) {
                queryWrapper.eq("shift_type", query.getShiftType());
            }

            if (query.getStatus() != null) {
                queryWrapper.eq("status", query.getStatus());
            }

            queryWrapper.orderByAsc("sort_order").orderByDesc("create_time");
            queryWrapper.eq("deleted_flag", 0);

            List<ShiftsEntity> result = baseMapper.selectList(queryWrapper);
            log.debug("分页查询班次列表完成: 查询条件={}, 结果数量={}", query, result.size());
            return result;
        } catch (Exception e) {
            log.error("分页查询班次列表异常: 查询条件" + query, e);
            throw new RuntimeException("查询班次列表失败", e);
        }
    }

    @Override
    public List<ShiftsEntity> getByShiftType(String shiftType) {
        if (shiftType == null || shiftType.trim().isEmpty()) {
            log.warn("班次类型不能为空");
            return List.of();
        }

        try {
            log.debug("根据班次类型查询: 班次类型={}", shiftType);
            List<ShiftsEntity> shiftsList = baseMapper.selectByShiftType(shiftType);
            log.debug("根据班次类型查询完成: 班次类型={}, 数量={}", shiftType, shiftsList.size());
            return shiftsList;
        } catch (Exception e) {
            log.error("根据班次类型查询异常: 班次类型" + shiftType, e);
            throw new RuntimeException("根据班次类型查询失败", e);
        }
    }

    @Override
    public List<ShiftsEntity> getAllEnabledShifts() {
        log.debug("查询所有启用的班次");

        try {
            QueryWrapper<ShiftsEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", true);
            queryWrapper.eq("deleted_flag", 0);
            queryWrapper.orderByAsc("sort_order").orderByAsc("shift_name");

            List<ShiftsEntity> enabledShifts = baseMapper.selectList(queryWrapper);
            log.debug("查询所有启用的班次完成: 数量={}", enabledShifts.size());
            return enabledShifts;
        } catch (Exception e) {
            log.error("查询所有启用的班次异常", e);
            throw new RuntimeException("查询启用的班次失败", e);
        }
    }

    @Override
    public boolean saveOrUpdateShifts(ShiftsEntity shifts) {
        if (shifts == null) {
            log.warn("班次信息不能为空");
            return false;
        }

        try {
            log.debug("保存或更新班次: 班次ID={}, 班次编码={}", shifts.getShiftId(), shifts.getShiftCode());

            // 检查班次编码唯一性
            if (existsByShiftCode(shifts.getShiftCode(), shifts.getShiftId())) {
                log.warn("班次编码已存在: 班次编码={}", shifts.getShiftCode());
                throw new RuntimeException("班次编码已存在: " + shifts.getShiftCode());
            }

            // 设置更新时间
            shifts.setUpdateTime(LocalDateTime.now());

            boolean success;
            if (shifts.getShiftId() == null) {
                // 新增
                shifts.setCreateTime(LocalDateTime.now());
                success = save(shifts);
                log.info("新增班次成功: 班次ID={}, 班次编码={}", shifts.getShiftId(), shifts.getShiftCode());
            } else {
                // 更新
                success = updateById(shifts);
                log.info("更新班次成功: 班次ID={}, 班次编码={}", shifts.getShiftId(), shifts.getShiftCode());
            }

            return success;
        } catch (Exception e) {
            log.error("保存或更新班次异常: 班次编码" + shifts.getShiftCode(), e);
            throw new RuntimeException("保存或更新班次失败", e);
        }
    }

    @Override
    public boolean deleteShifts(Long shiftId) {
        if (shiftId == null) {
            log.warn("班次ID不能为空");
            return false;
        }

        try {
            log.info("删除班次: 班次ID={}", shiftId);

            ShiftsEntity shifts = getById(shiftId);
            if (shifts == null) {
                log.warn("班次不存在: 班次ID={}", shiftId);
                return false;
            }

            // 软删除
            shifts.setDeletedFlag(1);
            shifts.setUpdateTime(LocalDateTime.now());
            boolean success = updateById(shifts);

            if (success) {
                log.info("删除班次成功: 班次ID={}, 班次编码={}", shiftId, shifts.getShiftCode());
            } else {
                log.warn("删除班次失败: 班次ID={}", shiftId);
            }

            return success;
        } catch (Exception e) {
            log.error("删除班次异常: 班次ID" + shiftId, e);
            throw new RuntimeException("删除班次失败", e);
        }
    }

    @Override
    public int batchDeleteShifts(List<Long> shiftIds) {
        if (shiftIds == null || shiftIds.isEmpty()) {
            log.warn("班次ID列表不能为空");
            return 0;
        }

        try {
            log.info("批量删除班次: 班次数量={}", shiftIds.size());

            // 查询要删除的班次
            List<ShiftsEntity> shiftsList = listByIds(shiftIds);
            int deletedCount = 0;

            // 软删除
            for (ShiftsEntity shifts : shiftsList) {
                shifts.setDeletedFlag(1);
                shifts.setUpdateTime(LocalDateTime.now());
                if (updateById(shifts)) {
                    deletedCount++;
                }
            }

            log.info("批量删除班次完成: 删除数量={}, 请求数量={}", deletedCount, shiftIds.size());
            return deletedCount;
        } catch (Exception e) {
            log.error("批量删除班次异常: 班次数量" + shiftIds.size(), e);
            throw new RuntimeException("批量删除班次失败", e);
        }
    }

    @Override
    public boolean updateShiftsStatus(Long shiftId, Boolean status) {
        if (shiftId == null || status == null) {
            log.warn("班次ID和状态不能为空");
            return false;
        }

        try {
            log.info("更新班次状态: 班次ID={}, 状态={}", shiftId, status ? "启用" : "禁用");

            ShiftsEntity shifts = getById(shiftId);
            if (shifts == null) {
                log.warn("班次不存在: 班次ID={}", shiftId);
                return false;
            }

            shifts.setStatus(status);
            shifts.setUpdateTime(LocalDateTime.now());
            boolean success = updateById(shifts);

            if (success) {
                log.info("更新班次状态成功: 班次ID={}, 班次编码={}, 状态={}",
                    shiftId, shifts.getShiftCode(), status ? "启用" : "禁用");
            }

            return success;
        } catch (Exception e) {
            log.error("更新班次状态异常: 班次ID" + shiftId + ", 状态" + status, e);
            throw new RuntimeException("更新班次状态失败", e);
        }
    }

    @Override
    public int batchUpdateShiftsStatus(List<Long> shiftIds, Boolean status) {
        if (shiftIds == null || shiftIds.isEmpty() || status == null) {
            log.warn("班次ID列表和状态不能为空");
            return 0;
        }

        try {
            log.info("批量更新班次状态: 班次数量={}, 状态={}", shiftIds.size(), status ? "启用" : "禁用");

            // 查询要更新的班次
            List<ShiftsEntity> shiftsList = listByIds(shiftIds);
            int updatedCount = 0;

            for (ShiftsEntity shifts : shiftsList) {
                shifts.setStatus(status);
                shifts.setUpdateTime(LocalDateTime.now());
                if (updateById(shifts)) {
                    updatedCount++;
                }
            }

            log.info("批量更新班次状态完成: 更新数量={}, 请求数量={}", updatedCount, shiftIds.size());
            return updatedCount;
        } catch (Exception e) {
            log.error("批量更新班次状态异常: 班次数量" + shiftIds.size() + ", 状态" + status, e);
            throw new RuntimeException("批量更新班次状态失败", e);
        }
    }

    @Override
    public boolean existsByShiftCode(String shiftCode, Long excludeShiftId) {
        if (shiftCode == null || shiftCode.trim().isEmpty()) {
            return false;
        }

        try {
            log.debug("检查班次编码唯一性: 班次编码={}, 排除ID={}", shiftCode, excludeShiftId);

            QueryWrapper<ShiftsEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("shift_code", shiftCode);
            queryWrapper.eq("deleted_flag", 0);

            if (excludeShiftId != null) {
                queryWrapper.ne("shift_id", excludeShiftId);
            }

            long count = count(queryWrapper);
            boolean exists = count > 0;

            log.debug("检查班次编码唯一性完成: 班次编码={}, 排除ID={}, 存在={}",
                    shiftCode, excludeShiftId, exists);
            return exists;
        } catch (Exception e) {
            log.error("检查班次编码唯一性异常: 班次编码" + shiftCode + ", 排除ID" + excludeShiftId, e);
            return false;
        }
    }
}