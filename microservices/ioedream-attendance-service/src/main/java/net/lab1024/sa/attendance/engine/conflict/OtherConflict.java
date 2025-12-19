package net.lab1024.sa.attendance.engine.conflict;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 其他类型冲突
 * <p>
 * 描述排班中的其他类型冲突情况
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
public class OtherConflict {

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
     * 排班记录ID
     */
    private Long scheduleRecordId;

    /**
     * 班次ID
     */
    private Long shiftId;

    /**
     * 冲突严重程度（1-5，5最严重）
     */
    private Integer severity;

    /**
     * 冲突原因
     */
    private String conflictReason;

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
     * 扩展属性
     */
    private Map<String, Object> extendedAttributes;
}
