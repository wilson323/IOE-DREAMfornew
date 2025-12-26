package net.lab1024.sa.common.domain;

import java.io.Serializable;

import lombok.Data;

/**
 * 分页参数基类
 * <p>
 * 所有分页查询DTO应继承此类
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-21
 */
@Data
public class PageParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码，从1开始
     */
    private Integer pageNum = 1;

    /**
     * 每页大小，默认20
     */
    private Integer pageSize = 20;
}
