package net.lab1024.sa.attendance.engine.conflict;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 时间冲突
 * <p>
 * 描述排班中的时间重叠和冲突情况
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
public class TimeConflict {

    /**
     * 冲突ID
     */
    private String conflictId;

    /**
     * 冲突类型
     */
    private String conflictType;

    /**
     * 冲突描述
     */
    private String conflictDescription;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 冲突的排班记录1 ID
     */
    private Long scheduleRecordId1;

    /**
     * 冲突的排班记录2 ID
     */
    private Long scheduleRecordId2;

    /**
     * 冲突的班次1 ID
     */
    private Long shiftId1;

    /**
     * 冲突的班次2 ID
     */
    private Long shiftId2;

    /**
     * 冲突的开始时间1
     */
    private LocalDateTime conflictStartTime1;

    /**
     * 冲突的结束时间1
     */
    private LocalDateTime conflictEndTime1;

    /**
     * 冲突的开始时间2
     */
    private LocalDateTime conflictStartTime2;

    /**
     * 冲突的结束时间2
     */
    private LocalDateTime conflictEndTime2;

    /**
     * 冲突时长（分钟）
     */
    private Integer conflictDuration;

    /**
     * 冲突严重程度（1-5，5最严重）
     */
    private Integer severity;

    /**
     * 冲突原因
     */
    private String conflictReason;

    /**
     * 冲突发生位置/部门
     */
    private String conflictLocation;

    /**
     * 是否可自动解决
     */
    private Boolean autoResolvable;

    /**
     * 建议解决方案
     */
    private String suggestedResolution;

    /**
     * 冲突检测时间
     */
    private LocalDateTime detectionTime;

    /**
     * 检测算法
     */
    private String detectionAlgorithm;

    /**
     * 冲突状态
     */
    private ConflictStatus status;

    /**
     * 冲突状态枚举
     */
    public enum ConflictStatus {
        PENDING("待处理"),
        RESOLVED("已解决"),
        IGNORED("已忽略"),
        ESCALATED("已上报");

        private final String description;

        ConflictStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}