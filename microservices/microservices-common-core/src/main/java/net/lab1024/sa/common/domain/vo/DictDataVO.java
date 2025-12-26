package net.lab1024.sa.common.domain.vo;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 字典数据VO
 * <p>
 * 用于缓存和数据传输的字典数据信息
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictDataVO {

    /**
     * 字典数据ID
     */
    private Long dictDataId;

    /**
     * 字典类型编码
     */
    private String dictTypeCode;

    /**
     * 字典类型名称
     */
    private String dictTypeName;

    /**
     * 字典数据值
     */
    private String dictDataValue;

    /**
     * 字典数据标签
     */
    private String dictDataLabel;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 状态：1-启用 0-禁用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * CSS类名
     */
    private String cssClass;

    /**
     * 列表背景
     */
    private String listClass;

    /**
     * 是否默认：1-是 0-否
     */
    private Integer isDefault;
}
