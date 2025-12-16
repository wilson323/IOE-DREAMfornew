package net.lab1024.sa.consume.domain.form;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订餐查询表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Data
public class MealOrderQueryForm {

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 20;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 餐别ID
     */
    private Long mealTypeId;

    /**
     * 订单状态
     */
    private String status;

    /**
     * 开始日期
     */
    private LocalDateTime startDate;

    /**
     * 结束日期
     */
    private LocalDateTime endDate;

    /**
     * 订单编号
     */
    private String orderNo;
}
