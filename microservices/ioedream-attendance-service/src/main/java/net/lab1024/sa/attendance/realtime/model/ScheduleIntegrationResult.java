package net.lab1024.sa.attendance.realtime.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 排班引擎集成计算结果
 *
 * <p>
 * 用于封装实时计算与排班引擎联动后的计算输出。
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleIntegrationResult {

    /**
     * 集成计算ID
     */
    private String integrationId;

    /**
     * 集成计算时间
     */
    private LocalDateTime integrationTime;

    /**
     * 是否集成成功
     */
    private boolean integrationSuccessful;

    /**
     * 错误信息（失败时）
     */
    private String errorMessage;
}
