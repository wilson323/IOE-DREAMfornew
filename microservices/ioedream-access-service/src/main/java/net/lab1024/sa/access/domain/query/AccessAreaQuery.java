package net.lab1024.sa.access.domain.query;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.domain.PageParam;

/**
 * 门禁区域查询条件
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
public class AccessAreaQuery extends PageParam {

    /**
     * 区域名称
     */
    private String areaName;

    /**
     * 父级区域ID
     */
    private Long parentId;

    /**
     * 区域状态
     */
    private Integer status;

    /**
     * 区域类型
     */
    private String areaType;
}