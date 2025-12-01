package net.lab1024.sa.access.domain.query;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.page.PageForm;

/**
 * 生物识别查询表单
 * <p>
 * 用于生物识别相关的查询条件
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BiometricQueryForm extends PageForm {

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 生物特征类型
     */
    private String biometricType;

    /**
     * 操作类型
     */
    private Integer operationType;

    /**
     * 操作结果
     */
    private Integer operationResult;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;
}
