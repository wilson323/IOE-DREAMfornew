package net.lab1024.sa.access.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import net.lab1024.sa.common.access.entity.AccessPermissionApplyEntity;

/**
 * 门禁权限申请DAO
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Mapper注解
 * - 继承BaseMapper
 * - 使用Dao后缀命名
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface AccessPermissionApplyDao extends BaseMapper<AccessPermissionApplyEntity> {

    /**
     * 根据申请编号查询
     * <p>
     * 使用MyBatis-Plus的LambdaQueryWrapper实现
     * </p>
     *
     * @param applyNo 申请编号
     * @return 权限申请实体
     */
    default AccessPermissionApplyEntity selectByApplyNo(String applyNo) {
        LambdaQueryWrapper<AccessPermissionApplyEntity> wrapper = Wrappers.lambdaQuery(AccessPermissionApplyEntity.class)
                .eq(AccessPermissionApplyEntity::getApplyNo, applyNo)
                .eq(AccessPermissionApplyEntity::getDeletedFlag, false)
                .last("LIMIT 1");
        return selectOne(wrapper);
    }

    /**
     * 查询所有已过期的临时权限申请
     * <p>
     * 查询条件：
     * - 申请类型为EMERGENCY
     * - 状态为APPROVED（已审批通过）
     * - 结束时间小于当前时间（已过期）
     * - 未删除（deleted_flag = 0）
     * </p>
     *
     * @param currentTime 当前时间
     * @return 已过期的临时权限申请列表
     */
    default List<AccessPermissionApplyEntity> selectExpiredEmergencyPermissions(LocalDateTime currentTime) {
        LambdaQueryWrapper<AccessPermissionApplyEntity> wrapper = Wrappers.lambdaQuery(AccessPermissionApplyEntity.class)
                .eq(AccessPermissionApplyEntity::getApplyType, "EMERGENCY")
                .eq(AccessPermissionApplyEntity::getStatus, "APPROVED")
                .lt(AccessPermissionApplyEntity::getEndTime, currentTime)
                .eq(AccessPermissionApplyEntity::getDeletedFlag, false);
        return selectList(wrapper);
    }
}