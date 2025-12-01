package net.lab1024.sa.audit.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 失败原因统计VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
@Accessors(chain = true)
public class FailureReasonStatisticsVO {

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 失败次数
     */
    private Long count;

    /**
     * 失败占比（百分比）
     */
    private Double percentage;
}
