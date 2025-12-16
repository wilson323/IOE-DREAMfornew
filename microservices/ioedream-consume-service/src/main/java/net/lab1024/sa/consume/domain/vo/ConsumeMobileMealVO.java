package net.lab1024.sa.consume.domain.vo;

import java.time.LocalTime;
import lombok.Data;

/**
 * 移动端餐别VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class ConsumeMobileMealVO {

    /**
     * 餐别ID
     */
    private Long mealId;

    /**
     * 餐别名称
     */
    private String mealName;

    /**
     * 开始时间
     */
    private LocalTime startTime;

    /**
     * 结束时间
     */
    private LocalTime endTime;

    /**
     * 是否启用
     */
    private Boolean enabled;
}



