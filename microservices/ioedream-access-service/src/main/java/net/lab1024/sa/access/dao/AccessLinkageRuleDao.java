package net.lab1024.sa.access.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.access.AccessLinkageRuleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 门禁联动规则数据访问接口
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Mapper
public interface AccessLinkageRuleDao extends BaseMapper<AccessLinkageRuleEntity> {

    /**
     * 根据触发设备ID查询启用的联动规则
     *
     * @param triggerDeviceId 触发设备ID
     * @return 联动规则列表
     */
    @Select("SELECT * FROM t_access_linkage_rule " +
            "WHERE trigger_device_id = #{triggerDeviceId} " +
            "AND enabled = 1 " +
            "AND deleted_flag = 0 " +
            "ORDER BY priority ASC")
    List<AccessLinkageRuleEntity> selectEnabledRulesByTriggerDevice(@Param("triggerDeviceId") Long triggerDeviceId);

    /**
     * 根据触发类型查询启用的联动规则
     *
     * @param triggerType 触发类型
     * @return 联动规则列表
     */
    @Select("SELECT * FROM t_access_linkage_rule " +
            "WHERE trigger_type = #{triggerType} " +
            "AND enabled = 1 " +
            "AND deleted_flag = 0 " +
            "ORDER BY priority ASC")
    List<AccessLinkageRuleEntity> selectEnabledRulesByTriggerType(@Param("triggerType") String triggerType);

    /**
     * 查询所有启用的联动规则
     *
     * @return 联动规则列表
     */
    @Select("SELECT * FROM t_access_linkage_rule " +
            "WHERE enabled = 1 " +
            "AND deleted_flag = 0 " +
            "ORDER BY priority ASC")
    List<AccessLinkageRuleEntity> selectAllEnabledRules();

    /**
     * 根据规则类型查询联动规则
     *
     * @param ruleType 规则类型
     * @return 联动规则列表
     */
    @Select("SELECT * FROM t_access_linkage_rule " +
            "WHERE rule_type = #{ruleType} " +
            "AND enabled = 1 " +
            "AND deleted_flag = 0 " +
            "ORDER BY priority ASC")
    List<AccessLinkageRuleEntity> selectByRuleType(@Param("ruleType") String ruleType);
}
