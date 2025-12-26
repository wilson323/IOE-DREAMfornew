package net.lab1024.sa.attendance.engine.rule;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
public class RuleExecutionContext {
    private Long employeeId;
    private LocalDate date;
    private Long shiftId;
    @Builder.Default
    private Integer consecutiveWorkDays = 0;
    @Builder.Default
    private Integer consecutiveRestDays = 0;
    @Builder.Default
    private Integer weeklyWorkDays = 0;
    @Builder.Default
    private Integer monthlyWorkDays = 0;
    private Map<String, Object> customData;

    public Object getVariable(String name) {
        switch (name) {
            case "employeeId": return employeeId;
            case "date": return date;
            case "shiftId": return shiftId;
            case "consecutiveWorkDays": return consecutiveWorkDays;
            case "consecutiveRestDays": return consecutiveRestDays;
            case "weeklyWorkDays": return weeklyWorkDays;
            case "monthlyWorkDays": return monthlyWorkDays;
            default:
                return customData != null ? customData.get(name) : null;
        }
    }
}
