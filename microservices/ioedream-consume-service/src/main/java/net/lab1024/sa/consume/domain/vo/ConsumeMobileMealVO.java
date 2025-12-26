package net.lab1024.sa.consume.domain.vo;

import lombok.Data;

/**
 * 移动端餐别信息
 *
 * @author IOE-DREAM Team
 * @since 2025-12-22
 */
@Data
public class ConsumeMobileMealVO {

    private Long mealId;

    private String mealName;

    private String startTime;

    private String endTime;

    private Boolean isCurrent;
}
