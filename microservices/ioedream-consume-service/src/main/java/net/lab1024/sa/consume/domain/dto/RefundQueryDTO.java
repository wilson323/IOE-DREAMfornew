package net.lab1024.sa.consume.domain.dto;

import lombok.Data;

/**
 * 退款查询DTO
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Data
public class RefundQueryDTO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 退款状态
     */
    private String status;

    /**
     * 退款单号
     */
    private String refundNo;

    /**
     * 开始时间
     */
    private String startDate;

    /**
     * 结束时间
     */
    private String endDate;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 页大小
     */
    private Integer pageSize = 20;
}