package net.lab1024.sa.base.module.area.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.module.area.domain.vo.AreaVO;

/**
 * 区域树形结构视图对象
 * 用于前端树形组件展示，包含层级关系和统计信息
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
@Data
public class AreaTreeVO {

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 区域名称
     */
    private String areaName;

    /**
     * 区域编码
     */
    private String areaCode;

    /**
     * 区域类型
     */
    private String areaType;

    /**
     * 区域类型名称（中文描述）
     */
    private String areaTypeName;

    /**
     * 区域类型英文名称
     */
    private String areaTypeEnglishName;

    /**
     * 上级区域ID
     */
    private Long parentId;

    /**
     * 上级区域名称
     */
    private String parentName;

    /**
     * 区域路径（逗号分隔的ID）
     */
    private String path;

    /**
     * 子节点数量
     */
    private Integer childrenCount;

    /**
     * 是否有子节点
     */
    private Boolean hasChildren;

    /**
     * 区域层级
     */
    private Integer level;

    /**
     * 区域状态
     */
    private String areaStatus;

    /**
     * 排序值
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;

    /**
     * 上一个兄弟节点ID（用于同级排序）
     */
    private Long preId;

    /**
     * 下一个兄弟节点ID（用于同级排序）
     */
    private Long nextId;

    /**
     * 子区域列表（树形结构核心字段）
     */
    private List<AreaTreeVO> children;

    /**
     * 包含自身和所有子区域的ID列表
     * 用于权限判断和批量操作
     */
    private List<Long> selfAndAllChildrenIds;

    /**
     * 包含自身和所有子区域的ID字符串（逗号分隔）
     * 用于SQL IN查询
     */
    private String selfAndAllChildrenIdsStr;

    /**
     * 层级深度（用于展示）
     */
    private Integer depth;

    /**
     * 是否展开（前端控制）
     */
    private Boolean expanded;

    /**
     * 是否选中（前端控制）
     */
    private Boolean selected;

    /**
     * 是否禁用（前端控制）
     */
    private Boolean disabled;

    /**
     * 图标（前端展示）
     */
    private String icon;

    /**
     * 节点类型（前端树形组件识别）
     */
    private String nodeType;

    /**
     * 节点Key（前端唯一标识）
     */
    private String key;

    /**
     * 节点Value（前端传递值）
     */
    private String value;

    /**
     * 标签（前端显示文本）
     */
    private String title;

    /**
     * 是否为叶子节点
     *
     * @return true 如果没有子节点
     */
    public boolean isLeaf() {
        return children == null || children.isEmpty();
    }

    /**
     * 是否可选中
     *
     * @return true 如果是正常状态
     */
    public boolean isSelectable() {
        return !isMaintenance() && "ACTIVE".equals(areaStatus);
    }

    /**
     * 是否启用状态
     *
     * @return true 如果区域启用且非维护状态
     */
    public boolean isEnabled() {
        return "ACTIVE".equals(areaStatus) && !isMaintenance();
    }

    /**
     * 是否维护状态
     *
     * @return true 如果区域处于维护状态
     */
    public boolean isMaintenance() {
        return "MAINTENANCE".equals(areaStatus);
    }

    /**
     * 获取节点层级
     *
     * @return 层级深度
     */
    public Integer getDepth() {
        if (depth == null && getParentId() != null) {
            // 如果没有设置深度且有父节点，计算深度
            return calculateDepth();
        }
        return depth;
    }

    /**
     * 计算节点深度
     *
     * @return 计算的深度
     */
    private Integer calculateDepth() {
        // 这里可以根据path字段计算深度
        if (getPath() != null && !getPath().isEmpty()) {
            String[] pathParts = getPath().split(",");
            return pathParts.length - 1; // 减1是因为path包含0
        }
        return 0;
    }

  
    /**
     * 获取前端Key
     *
     * @return 节点Key
     */
    public String getKey() {
        if (key == null) {
            key = "area_" + areaId;
        }
        return key;
    }

    /**
     * 获取前端Value
     *
     * @return 节点Value
     */
    public String getValue() {
        if (value == null) {
            value = areaId.toString();
        }
        return value;
    }

    /**
     * 获取前端Title
     *
     * @return 节点Title
     */
    public String getTitle() {
        if (title == null) {
            title = areaName;
        }
        return title;
    }

    /**
     * 获取包含自身和所有子区域的ID列表
     * 如果列表为空，则计算并返回
     *
     * @return 包含自身和所有子区域的ID列表
     */
    public List<Long> getSelfAndChildrenIds() {
        if (selfAndAllChildrenIds == null) {
            selfAndAllChildrenIds = new java.util.ArrayList<>();
            if (areaId != null) {
                selfAndAllChildrenIds.add(areaId);
                // 递归添加子区域ID
                if (children != null) {
                    for (AreaTreeVO child : children) {
                        List<Long> childIds = child.getSelfAndChildrenIds();
                        if (childIds != null) {
                            selfAndAllChildrenIds.addAll(childIds);
                        }
                    }
                }
            }
        }
        return selfAndAllChildrenIds;
    }
}