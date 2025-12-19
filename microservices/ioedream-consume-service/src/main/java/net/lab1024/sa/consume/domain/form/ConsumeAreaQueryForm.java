package net.lab1024.sa.consume.domain.form;

import lombok.Data;

/**
 * 消费区域查询表单
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用Form后缀命名
 * - 支持分页查询
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class ConsumeAreaQueryForm {

    /**
     * 区域名称（模糊查询）
     */
    private String areaName;

    /**
     * 区域编码（精确查询）
     */
    private String areaCode;

    /**
     * 父级区域ID
     */
    private Long parentId;

    /**
     * 区域类型
     */
    private Integer areaType;

    /**
     * 经营模式
     */
    private Integer manageMode;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;

    /**
     * 页码（从1开始）
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 20;
}
