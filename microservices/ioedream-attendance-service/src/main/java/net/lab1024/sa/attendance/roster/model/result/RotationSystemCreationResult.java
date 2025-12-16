package net.lab1024.sa.attendance.roster.model.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 轮班制度创建结果
 * <p>
 * 封装轮班制度创建操作的结果信息
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
public class RotationSystemCreationResult {

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 系统ID
     */
    private String systemId;

    /**
     * 消息
     */
    private String message;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 创建时间
     */
    private LocalDateTime creationTime;

    /**
     * 创建耗时（毫秒）
     */
    private Long processingTimeMs;

    /**
     * 扩展信息
     */
    private String extendedInfo;
}