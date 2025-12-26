package net.lab1024.sa.attendance.engine.conflict;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 冲突检测结果
 * <p>
 * 封装冲突检测的完整结果信息
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConflictDetectionResult {

    /**
     * 检测ID
     */
    private String detectionId;

    /**
     * 是否存在冲突
     */
    private Boolean hasConflicts;

    /**
     * 冲突总数
     */
    private Integer totalConflicts;

    /**
     * 严重冲突数量
     */
    private Integer severeConflicts;

    /**
     * 一般冲突数量
     */
    private Integer minorConflicts;

    /**
     * 冲突类型统计
     */
    private Map<String, Integer> conflictTypeStatistics;

    /**
     * 时间冲突列表
     */
    private List<TimeConflict> timeConflicts;

    /**
     * 技能冲突列表
     */
    private List<SkillConflict> skillConflicts;

    /**
     * 工作时长冲突列表
     */
    private List<WorkHourConflict> workHourConflicts;

    /**
     * 班次容量冲突列表
     */
    private List<CapacityConflict> capacityConflicts;

    /**
     * 其他冲突列表
     */
    private List<OtherConflict> otherConflicts;

    /**
     * 检测开始时间
     */
    private LocalDateTime detectionStartTime;

    /**
     * 检测结束时间
     */
    private LocalDateTime detectionEndTime;

    /**
     * 检测耗时（毫秒）
     */
    private Long detectionDuration;

    /**
     * 检测算法版本
     */
    private String algorithmVersion;

    /**
     * 检测参数
     */
    private Map<String, Object> detectionParameters;

    /**
     * 修复建议
     */
    private List<String> resolutionSuggestions;

    /**
     * 冲突严重程度评分（0-100，分数越高越严重）
     */
    private Double conflictSeverityScore;

    /**
     * 添加时间冲突
     */
    public void addTimeConflict(TimeConflict conflict) {
        this.timeConflicts.add(conflict);
        this.totalConflicts = (this.totalConflicts != null ? this.totalConflicts : 0) + 1;
        if (conflict.getSeverity() >= 3) {
            this.severeConflicts = (this.severeConflicts != null ? this.severeConflicts : 0) + 1;
        } else {
            this.minorConflicts = (this.minorConflicts != null ? this.minorConflicts : 0) + 1;
        }
        this.hasConflicts = true;
    }

    /**
     * 添加技能冲突
     */
    public void addSkillConflict(SkillConflict conflict) {
        this.skillConflicts.add(conflict);
        this.totalConflicts = (this.totalConflicts != null ? this.totalConflicts : 0) + 1;
        if (conflict.getSeverity() >= 3) {
            this.severeConflicts = (this.severeConflicts != null ? this.severeConflicts : 0) + 1;
        } else {
            this.minorConflicts = (this.minorConflicts != null ? this.minorConflicts : 0) + 1;
        }
        this.hasConflicts = true;
    }

    /**
     * 添加工作时长冲突
     */
    public void addWorkHourConflict(WorkHourConflict conflict) {
        this.workHourConflicts.add(conflict);
        this.totalConflicts = (this.totalConflicts != null ? this.totalConflicts : 0) + 1;
        if (conflict.getSeverity() >= 3) {
            this.severeConflicts = (this.severeConflicts != null ? this.severeConflicts : 0) + 1;
        } else {
            this.minorConflicts = (this.minorConflicts != null ? this.minorConflicts : 0) + 1;
        }
        this.hasConflicts = true;
    }

    /**
     * 添加班次容量冲突
     */
    public void addCapacityConflict(CapacityConflict conflict) {
        this.capacityConflicts.add(conflict);
        this.totalConflicts = (this.totalConflicts != null ? this.totalConflicts : 0) + 1;
        if (conflict.getSeverity() >= 3) {
            this.severeConflicts = (this.severeConflicts != null ? this.severeConflicts : 0) + 1;
        } else {
            this.minorConflicts = (this.minorConflicts != null ? this.minorConflicts : 0) + 1;
        }
        this.hasConflicts = true;
    }

    /**
     * 添加其他冲突
     */
    public void addOtherConflict(OtherConflict conflict) {
        this.otherConflicts.add(conflict);
        this.totalConflicts = (this.totalConflicts != null ? this.totalConflicts : 0) + 1;
        if (conflict.getSeverity() >= 3) {
            this.severeConflicts = (this.severeConflicts != null ? this.severeConflicts : 0) + 1;
        } else {
            this.minorConflicts = (this.minorConflicts != null ? this.minorConflicts : 0) + 1;
        }
        this.hasConflicts = true;
    }
}
