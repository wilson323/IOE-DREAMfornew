package net.lab1024.sa.attendance.realtime.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 计算触发事件
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculationTriggerEvent {

    private String triggerId;

    private TriggerType triggerType;

    private LocalDateTime triggerTime;

    public enum TriggerType {
        EMPLOYEE_DAILY,
        DEPARTMENT_DAILY,
        COMPANY_DAILY,
        EMPLOYEE_MONTHLY,
        ANOMALY_DETECTION,
        ALERT_CHECKING,
        SCHEDULE_INTEGRATION
    }
}

