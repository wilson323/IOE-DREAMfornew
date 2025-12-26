package net.lab1024.sa.attendance.solver.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 员工实体 - OptaPlanner Problem Fact
 *
 * 核心概念:
 * - Problem Fact: 不可变的问题事实
 * - 包含员工的基本信息、技能、工作约束等
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    /**
     * 员工ID
     */
    private Long id;

    /**
     * 员工姓名
     */
    private String name;

    /**
     * 员工工号
     */
    private String employeeCode;

    /**
     * 技能列表 - 技能名称列表 (例如: ["门禁操作", "监控室", "急救证书"])
     */
    private List<String> skills;

    /**
     * 每天最多班次数 (默认3次)
     */
    @Builder.Default
    private Integer maxShiftsPerDay = 3;

    /**
     * 最多连续班次数 (默认6天)
     */
    @Builder.Default
    private Integer maxConsecutiveShifts = 6;

    /**
     * 最少休息小时数 (默认11小时)
     */
    @Builder.Default
    private Integer minRestHours = 11;

    /**
     * 是否可用 (默认true)
     */
    @Builder.Default
    private Boolean available = true;

    /**
     * 员工类型 (FULL_TIME-全职, PART_TIME-兼职, CONTRACTOR-外包)
     */
    private String employeeType;

    /**
     * 部门ID
     */
    private Long departmentId;

    /**
     * 成本等级 (1-5, 5最高成本)
     */
    @Builder.Default
    private Integer costLevel = 3;

    /**
     * 班次偏好 (JSON格式: {"MORNING": 5, "AFTERNOON": 3, "NIGHT": 1})
     * 分数越高表示越偏好该班次
     */
    private String shiftPreferences;

    /**
     * 检查员工是否具备指定技能
     */
    public boolean hasSkill(String skill) {
        return skills != null && skills.contains(skill);
    }

    /**
     * 检查员工是否具备所有所需技能
     */
    public boolean hasAllSkills(List<String> requiredSkills) {
        if (requiredSkills == null || requiredSkills.isEmpty()) {
            return true;
        }
        return skills != null && skills.containsAll(requiredSkills);
    }

    /**
     * 获取班次偏好分数
     */
    public int getShiftPreferenceScore(String shiftType) {
        // TODO: 解析shiftPreferences JSON并返回对应分数
        // 简化实现：默认返回0
        return 0;
    }

    /**
     * 计算工作负荷
     */
    public double calculateWorkload() {
        // 基于成本等级和技能数量计算工作负荷
        int skillCount = (skills != null) ? skills.size() : 0;
        return (costLevel * 0.6) + (skillCount * 0.4);
    }
}
