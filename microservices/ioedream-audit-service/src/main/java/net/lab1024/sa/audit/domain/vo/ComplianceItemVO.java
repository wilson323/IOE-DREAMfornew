package net.lab1024.sa.audit.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 合规检查项VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
@Accessors(chain = true)
public class ComplianceItemVO {

    /**
     * 检查项名称
     */
    private String itemName;

    /**
     * 检查项描述
     */
    private String description;

    /**
     * 合规得分
     */
    private Double score;

    /**
     * 合规状态：通过、不通过、警告
     */
    private String status;

    /**
     * 检查详情
     */
    private String details;

    /**
     * 检查时间
     */
    private java.time.LocalDateTime checkTime;
}
