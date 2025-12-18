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
 * 闂ㄧ鏉冮檺鐢宠DAO
 * <p>
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛? * - 浣跨敤@Mapper娉ㄨВ
 * - 缁ф壙BaseMapper
 * - 浣跨敤Dao鍚庣紑鍛藉悕
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface AccessPermissionApplyDao extends BaseMapper<AccessPermissionApplyEntity> {

    /**
     * 鏍规嵁鐢宠缂栧彿鏌ヨ
     * <p>
     * 浣跨敤MyBatis-Plus鐨凩ambdaQueryWrapper瀹炵幇
     * </p>
     *
     * @param applyNo 鐢宠缂栧彿
     * @return 鏉冮檺鐢宠瀹炰綋
     */
    default AccessPermissionApplyEntity selectByApplyNo(String applyNo) {
        LambdaQueryWrapper<AccessPermissionApplyEntity> wrapper = Wrappers.lambdaQuery(AccessPermissionApplyEntity.class)
                .eq(AccessPermissionApplyEntity::getApplyNo, applyNo)
                .eq(AccessPermissionApplyEntity::getDeletedFlag, false)
                .last("LIMIT 1");
        return selectOne(wrapper);
    }

    /**
     * 鏌ヨ鎵€鏈夊凡杩囨湡鐨勭揣鎬ユ潈闄愮敵璇?     * <p>
     * 鏌ヨ鏉′欢锛?     * - 鐢宠绫诲瀷涓篍MERGENCY
     * - 鐘舵€佷负APPROVED锛堝凡瀹℃壒閫氳繃锛?     * - 缁撴潫鏃堕棿灏忎簬褰撳墠鏃堕棿锛堝凡杩囨湡锛?     * - 鏈垹闄わ紙deleted_flag = 0锛?     * </p>
     *
     * @param currentTime 褰撳墠鏃堕棿
     * @return 宸茶繃鏈熺殑绱ф€ユ潈闄愮敵璇峰垪琛?     */
    default List<AccessPermissionApplyEntity> selectExpiredEmergencyPermissions(LocalDateTime currentTime) {
        LambdaQueryWrapper<AccessPermissionApplyEntity> wrapper = Wrappers.lambdaQuery(AccessPermissionApplyEntity.class)
                .eq(AccessPermissionApplyEntity::getApplyType, "EMERGENCY")
                .eq(AccessPermissionApplyEntity::getStatus, "APPROVED")
                .lt(AccessPermissionApplyEntity::getEndTime, currentTime)
                .eq(AccessPermissionApplyEntity::getDeletedFlag, false);
        return selectList(wrapper);
    }
}


