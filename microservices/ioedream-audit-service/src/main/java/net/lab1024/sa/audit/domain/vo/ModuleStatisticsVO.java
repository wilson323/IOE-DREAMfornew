package net.lab1024.sa.audit.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 模块统计VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
@Accessors(chain = true)
public class ModuleStatisticsVO {

    /**
     * 模块名称
     */
    private String moduleName;

    /**
     * 操作数量
     */
    private Long count;
}
