package net.lab1024.sa.admin.module.system.area.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 区域树形视图对象
 *
 * @author SmartAdmin
 * @date 2025-01-10
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AreaTreeVO extends AreaVO {

    @Schema(description = "同级上一个元素ID")
    private Long preId;

    @Schema(description = "同级下一个元素ID")
    private Long nextId;

    @Schema(description = "子区域列表")
    private List<AreaTreeVO> children;

    @Schema(description = "自己和所有递归子区域的ID集合")
    private List<Long> selfAndAllChildrenIdList;

    @Schema(description = "设备数量")
    private Integer deviceCount;

    @Schema(description = "人员数量")
    private Integer userCount;

}
