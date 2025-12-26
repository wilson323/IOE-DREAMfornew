package net.lab1024.sa.access.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.access.AccessInterlockRuleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 门禁全局互锁规则 DAO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Mapper
public interface AccessInterlockRuleDao extends BaseMapper<AccessInterlockRuleEntity> {

    /**
     * 查询区域关联的所有启用的互锁规则
     *
     * @param areaId 区域ID
     * @return 互锁规则列表
     */
    @Select("SELECT * FROM t_access_interlock_rule WHERE enabled = 1 " +
            "AND (area_a_id = #{areaId} OR area_b_id = #{areaId}) " +
            "ORDER BY priority DESC")
    List<AccessInterlockRuleEntity> selectEnabledRulesByArea(@Param("areaId") Long areaId);

    /**
     * 查询两个区域之间的互锁规则
     *
     * @param areaAId 区域A ID
     * @param areaBId 区域B ID
     * @return 互锁规则
     */
    @Select("SELECT * FROM t_access_interlock_rule WHERE enabled = 1 " +
            "AND ((area_a_id = #{areaAId} AND area_b_id = #{areaBId}) " +
            "OR (area_a_id = #{areaBId} AND area_b_id = #{areaAId}))")
    List<AccessInterlockRuleEntity> selectRulesBetweenAreas(@Param("areaAId") Long areaAId,
                                                            @Param("areaBId") Long areaBId);

    /**
     * 查询区域A作为起始区域的所有互锁规则
     *
     * @param areaAId 区域A ID
     * @return 互锁规则列表
     */
    @Select("SELECT * FROM t_access_interlock_rule WHERE area_a_id = #{areaAId} AND enabled = 1")
    List<AccessInterlockRuleEntity> selectRulesByAreaA(@Param("areaAId") Long areaAId);
}
