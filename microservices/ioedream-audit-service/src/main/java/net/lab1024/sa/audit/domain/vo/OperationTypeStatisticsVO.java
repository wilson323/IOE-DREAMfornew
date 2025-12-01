package net.lab1024.sa.audit.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 操作类型统计VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
@Accessors(chain = true)
public class OperationTypeStatisticsVO {

    /**
     * 操作类型
     */
    private Integer operationType;

    /**
     * 操作类型文本
     */
    private String operationTypeText;

    /**
     * 操作数量
     */
    private Long count;
}
