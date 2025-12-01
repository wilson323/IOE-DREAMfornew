package net.lab1024.sa.admin.module.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.consume.domain.entity.UserBehaviorBaselineEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户行为基线 DAO
 *
 * @author SmartAdmin Team
 * @since 2025-11-19
 */
@Mapper
public interface UserBehaviorBaselineDao extends BaseMapper<UserBehaviorBaselineEntity> {

    /**
     * 根据用户ID查询基线
     */
    UserBehaviorBaselineEntity selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和基线类型查询
     */
    UserBehaviorBaselineEntity selectByUserIdAndType(@Param("userId") Long userId, @Param("baselineType") String baselineType);

    /**
     * 根据用户ID列表批量查询
     */
    List<UserBehaviorBaselineEntity> selectByUserIds(@Param("userIds") List<Long> userIds);

    /**
     * 查询需要更新的基线
     */
    List<UserBehaviorBaselineEntity> selectNeedUpdate(@Param("thresholdHours") Integer thresholdHours);
}