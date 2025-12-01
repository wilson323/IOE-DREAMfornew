package net.lab1024.sa.admin.module.attendance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceRuleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 考勤规则 DAO
 *
 * 严格遵循repowiki规范:
 * - 继承BaseMapper，提供基础CRUD操作
 * - 使用@Mapper注解标记
 * - 提供规则匹配和查询接口
 * - 支持优先级排序和生效状态管理
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Mapper
public interface AttendanceRuleDao extends BaseMapper<AttendanceRuleEntity> {

    /**
     * 查询员工的适用考勤规则
     * 按优先级排序，返回最匹配的规则
     *
     * @param employeeId 员工ID
     * @param departmentId 部门ID
     * @param employeeType 员工类型
     * @param queryDate 查询日期
     * @return 适用的规则列表，按优先级降序排列
     */
    List<AttendanceRuleEntity> selectApplicableRules(
            @Param("employeeId") Long employeeId,
            @Param("departmentId") Long departmentId,
            @Param("employeeType") String employeeType,
            @Param("queryDate") LocalDate queryDate
    );

    /**
     * 查询员工的个人考勤规则
     *
     * @param employeeId 员工ID
     * @param queryDate 查询日期
     * @return 个人规则列表
     */
    List<AttendanceRuleEntity> selectIndividualRules(
            @Param("employeeId") Long employeeId,
            @Param("queryDate") LocalDate queryDate
    );

    /**
     * 查询部门的考勤规则
     *
     * @param departmentId 部门ID
     * @param queryDate 查询日期
     * @return 部门规则列表
     */
    List<AttendanceRuleEntity> selectDepartmentRules(
            @Param("departmentId") Long departmentId,
            @Param("queryDate") LocalDate queryDate
    );

    /**
     * 查询全局考勤规则
     *
     * @param employeeType 员工类型（可选）
     * @param queryDate 查询日期
     * @return 全局规则列表
     */
    List<AttendanceRuleEntity> selectGlobalRules(
            @Param("employeeType") String employeeType,
            @Param("queryDate") LocalDate queryDate
    );

    /**
     * 根据规则类型查询规则
     *
     * @param ruleType 规则类型：GLOBAL/DEPARTMENT/INDIVIDUAL
     * @param status 规则状态（可选）
     * @param queryDate 查询日期（可选）
     * @return 规则列表
     */
    List<AttendanceRuleEntity> selectByRuleType(
            @Param("ruleType") String ruleType,
            @Param("status") String status,
            @Param("queryDate") LocalDate queryDate
    );

    /**
     * 查询生效的考勤规则
     *
     * @param queryDate 查询日期
     * @return 生效规则列表
     */
    List<AttendanceRuleEntity> selectActiveRules(@Param("queryDate") LocalDate queryDate);

    /**
     * 根据公司ID查询规则
     *
     * @param companyId 公司ID
     * @param status 规则状态（可选）
     * @param queryDate 查询日期（可选）
     * @return 规则列表
     */
    List<AttendanceRuleEntity> selectByCompany(
            @Param("companyId") Long companyId,
            @Param("status") String status,
            @Param("queryDate") LocalDate queryDate
    );

    /**
     * 查询即将过期的规则
     *
     * @param days 天数阈值
     * @return 即将过期的规则列表
     */
    List<AttendanceRuleEntity> selectExpiringRules(@Param("days") Integer days);

    /**
     * 查询已过期的规则
     *
     * @param queryDate 查询日期
     * @return 已过期的规则列表
     */
    List<AttendanceRuleEntity> selectExpiredRules(@Param("queryDate") LocalDate queryDate);

    /**
     * 根据优先级查询规则
     *
     * @param minPriority 最小优先级
     * @param maxPriority 最大优先级
     * @param status 规则状态（可选）
     * @return 规则列表
     */
    List<AttendanceRuleEntity> selectByPriorityRange(
            @Param("minPriority") Integer minPriority,
            @Param("maxPriority") Integer maxPriority,
            @Param("status") String status
    );

    /**
     * 按条件分页查询考勤规则
     *
     * @param companyId 公司ID（可选）
     * @param departmentId 部门ID（可选）
     * @param employeeId 员工ID（可选）
     * @param ruleType 规则类型（可选）
     * @param status 规则状态（可选）
     * @param employeeType 员工类型（可选）
     * @param ruleName 规则名称（模糊查询，可选）
     * @return 规则列表
     */
    List<AttendanceRuleEntity> selectByCondition(
            @Param("companyId") Long companyId,
            @Param("departmentId") Long departmentId,
            @Param("employeeId") Long employeeId,
            @Param("ruleType") String ruleType,
            @Param("status") String status,
            @Param("employeeType") String employeeType,
            @Param("ruleName") String ruleName
    );

    /**
     * 检查规则编码是否存在
     *
     * @param ruleCode 规则编码
     * @param excludeId 排除的规则ID（可选）
     * @return 是否存在
     */
    Boolean existsByRuleCode(
            @Param("ruleCode") String ruleCode,
            @Param("excludeId") Long excludeId
    );

    /**
     * 查询规则的优先级统计
     *
     * @param companyId 公司ID（可选）
     * @param departmentId 部门ID（可选）
     * @return 优先级统计数据
     */
    List<Map<String, Object>> selectPriorityStatistics(
            @Param("companyId") Long companyId,
            @Param("departmentId") Long departmentId
    );

    /**
     * 查询规则类型统计
     *
     * @param companyId 公司ID（可选）
     * @return 规则类型统计数据
     */
    List<Map<String, Object>> selectRuleTypeStatistics(@Param("companyId") Long companyId);

    /**
     * 查询规则状态统计
     *
     * @param companyId 公司ID（可选）
     * @return 规则状态统计数据
     */
    List<Map<String, Object>> selectRuleStatusStatistics(@Param("companyId") Long companyId);

    /**
     * 批量更新规则状态
     *
     * @param ruleIds 规则ID列表
     * @param status 新状态
     * @return 更新行数
     */
    int batchUpdateStatus(
            @Param("ruleIds") List<Long> ruleIds,
            @Param("status") String status
    );

    /**
     * 批量更新规则优先级
     *
     * @param rulePriorityMap 规则ID和优先级的映射
     * @return 更新行数
     */
    int batchUpdatePriority(@Param("rulePriorityMap") Map<Long, Integer> rulePriorityMap);

    /**
     * 查询员工今日适用规则
     *
     * @param employeeId 员工ID
     * @param departmentId 部门ID
     * @param employeeType 员工类型
     * @return 今日适用规则
     */
    AttendanceRuleEntity selectTodayRule(
            @Param("employeeId") Long employeeId,
            @Param("departmentId") Long departmentId,
            @Param("employeeType") String employeeType
    );

    /**
     * 查询规则冲突检查
     *
     * @param companyId 公司ID
     * @param departmentId 部门ID（可选）
     * @param employeeId 员工ID（可选）
     * @param employeeType 员工类型（可选）
     * @param queryDate 查询日期
     * @return 冲突的规则列表
     */
    List<Map<String, Object>> selectRuleConflicts(
            @Param("companyId") Long companyId,
            @Param("departmentId") Long departmentId,
            @Param("employeeId") Long employeeId,
            @Param("employeeType") String employeeType,
            @Param("queryDate") LocalDate queryDate
    );

    /**
     * 查询规则使用情况统计
     *
     * @param ruleId 规则ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 使用统计数据
     */
    Map<String, Object> selectRuleUsageStatistics(
            @Param("ruleId") Long ruleId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 查询需要GPS验证的规则
     *
     * @param queryDate 查询日期
     * @return 需要GPS验证的规则列表
     */
    List<AttendanceRuleEntity> selectGpsValidationRules(@Param("queryDate") LocalDate queryDate);

    /**
     * 查询需要人脸识别的规则
     *
     * @param queryDate 查询日期
     * @return 需要人脸识别的规则列表
     */
    List<AttendanceRuleEntity> selectFaceRecognitionRules(@Param("queryDate") LocalDate queryDate);

    /**
     * 查询自动审批规则的规则
     *
     * @param queryDate 查询日期
     * @return 自动审批规则列表
     */
    List<AttendanceRuleEntity> selectAutoApprovalRules(@Param("queryDate") LocalDate queryDate);

    /**
     * 获取规则配置详情
     *
     * @param ruleId 规则ID
     * @return 规则配置详情
     */
    Map<String, Object> selectRuleDetails(@Param("ruleId") Long ruleId);

    /**
     * 查询规则覆盖的员工数量
     *
     * @param ruleId 规则ID
     * @return 覆盖的员工数量
     */
    Integer selectRuleCoveredEmployeeCount(@Param("ruleId") Long ruleId);

    /**
     * 更新规则的最后使用时间
     *
     * @param ruleId 规则ID
     * @return 更新行数
     */
    int updateLastUsedTime(@Param("ruleId") Long ruleId);

    /**
     * 清理过期的规则
     *
     * @param beforeDate 清理日期
     * @return 清理行数
     */
    int cleanExpiredRules(@Param("beforeDate") LocalDate beforeDate);

    // ==================== 补充缺失的方法（修复编译错误） ====================

    /**
     * 查询最高优先级的规则
     * 兼容性方法，返回优先级最高的规则
     *
     * @param employeeId 员工ID
     * @param queryDate 查询日期
     * @return 最高优先级规则
     */
    AttendanceRuleEntity selectHighestPriorityRule(@Param("employeeId") Long employeeId,
                                                   @Param("queryDate") LocalDate queryDate);
}