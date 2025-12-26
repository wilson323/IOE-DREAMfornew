package net.lab1024.sa.attendance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import net.lab1024.sa.attendance.entity.AttendanceRuleConfigEntity;

import java.util.List;

/**
 * 考勤规则配置DAO接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface AttendanceRuleConfigDao extends BaseMapper<AttendanceRuleConfigEntity> {

    /**
     * 查询启用的规则配置列表
     *
     * @return 启用的规则配置列表
     */
    List<AttendanceRuleConfigEntity> selectEnabledRules();

    /**
     * 根据适用范围查询规则配置
     *
     * @param applyScope 适用范围
     * @param scopeId 适用范围ID
     * @return 规则配置列表
     */
    List<AttendanceRuleConfigEntity> selectByScope(@Param("applyScope") String applyScope,
                                                   @Param("scopeId") Long scopeId);

    /**
     * 查询用户适用的规则配置
     * <p>
     * 优先级：员工级别 > 部门级别 > 班次级别 > 全局级别
     * </p>
     *
     * @param userId 用户ID
     * @param departmentId 部门ID
     * @param shiftId 班次ID
     * @return 规则配置（优先级最高的）
     */
    AttendanceRuleConfigEntity selectApplicableRule(@Param("userId") Long userId,
                                                     @Param("departmentId") Long departmentId,
                                                     @Param("shiftId") Long shiftId);

    /**
     * 查询全局规则配置
     *
     * @return 全局规则配置
     */
    AttendanceRuleConfigEntity selectGlobalRule();

    /**
     * 根据部门ID查询规则配置
     *
     * @param departmentId 部门ID
     * @return 规则配置列表
     */
    List<AttendanceRuleConfigEntity> selectByDepartmentId(@Param("departmentId") Long departmentId);

    /**
     * 根据班次ID查询规则配置
     *
     * @param shiftId 班次ID
     * @return 规则配置列表
     */
    List<AttendanceRuleConfigEntity> selectByShiftId(@Param("shiftId") Long shiftId);

    /**
     * 根据规则名称查询
     *
     * @param ruleName 规则名称
     * @return 规则配置
     */
    AttendanceRuleConfigEntity selectByRuleName(@Param("ruleName") String ruleName);
}
