package net.lab1024.sa.attendance.rule.model.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 引擎启动结果
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EngineStartupResult {
    private boolean success;
    private String message;
    private String errorMessage;
    private String errorCode;
    private int loadedRulesCount;
    private long startupTime;
    private String engineId;
}

