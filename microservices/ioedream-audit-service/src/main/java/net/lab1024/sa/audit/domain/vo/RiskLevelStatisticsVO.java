package net.lab1024.sa.audit.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 风险等级统计VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
@Accessors(chain = true)
public class RiskLevelStatisticsVO {

    /**
     * 风险等级：1-低 2-中 3-高
     */
    private Integer riskLevel;

    /**
     * 风险等级文本
     */
    private String riskLevelText;

    /**
     * 数量
     */
    private Long count;
}
