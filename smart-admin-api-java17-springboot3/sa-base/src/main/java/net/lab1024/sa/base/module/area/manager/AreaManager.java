package net.lab1024.sa.base.module.area.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.module.area.dao.AreaDao;
import net.lab1024.sa.base.module.area.domain.entity.AreaEntity;
import net.lab1024.sa.base.module.area.domain.vo.AreaTreeVO;
import net.lab1024.sa.base.module.area.enums.AreaTypeEnum;

/**
 * 区域管理器
 * 负责区域业务的复杂逻辑处理，包括树形结构构建、路径计算等
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
@Slf4j
@Component
public class AreaManager {

    @Resource
    private AreaDao areaDao;

    /**
     * 构建区域树形结构
     *
     * @param areaList 区域列表
     * @return 区域树形结构
     */
    public List<AreaTreeVO> buildAreaTree(List<AreaEntity> areaList) {
        if (CollectionUtils.isEmpty(areaList)) {
            return new ArrayList<>();
        }

        // 将区域列表转换为Map
        Map<Long, AreaTreeVO> areaMap = new HashMap<>();
        List<AreaTreeVO> rootAreas = new ArrayList<>();

        // 转换为TreeVO
        for (AreaEntity area : areaList) {
            AreaTreeVO treeVO = SmartBeanUtil.copy(area, AreaTreeVO.class);
            treeVO.setKey("area_" + treeVO.getAreaId());
            treeVO.setValue(treeVO.getAreaId().toString());
            treeVO.setTitle(treeVO.getAreaName());

            // 设置区域类型名称
            AreaTypeEnum typeEnum = AreaTypeEnum.fromValue(treeVO.getAreaType());
            if (typeEnum != null) {
                treeVO.setAreaTypeName(typeEnum.getDescription());
                treeVO.setAreaTypeEnglishName(typeEnum.getEnglishName());
            }

            areaMap.put(treeVO.getAreaId(), treeVO);

            if (treeVO.getParentId() == null || treeVO.getParentId() == 0) {
                rootAreas.add(treeVO);
            }
        }

        // 构建父子关系
        for (AreaTreeVO area : areaMap.values()) {
            Long parentId = area.getParentId();
            if (parentId != null && parentId != 0 && areaMap.containsKey(parentId)) {
                AreaTreeVO parent = areaMap.get(parentId);
                if (parent.getChildren() == null) {
                    parent.setChildren(new ArrayList<AreaTreeVO>());
                }
                parent.getChildren().add(area);

                // 设置父级名称
                area.setParentName(parent.getAreaName());

                // 设置前后节点关系
                setSiblingRelation(parent.getChildren());
            }
        }

        // 计算深度和统计信息
        calculateDepthAndStats(rootAreas);

        return rootAreas;
    }

    /**
     * 构建包含统计信息的区域树
     *
     * @return 区域树形结构
     */
    public List<AreaTreeVO> buildAreaTreeWithStats() {
        List<AreaEntity> areaList = areaDao.selectList(null);
        List<AreaTreeVO> treeList = buildAreaTree(areaList);

        // 为每个节点设置统计信息
        setStatistics(treeList);

        return treeList;
    }

    /**
     * 计算深度和统计信息
     *
     * @param areas 区域列表
     */
    private void calculateDepthAndStats(List<AreaTreeVO> areas) {
        for (AreaTreeVO area : areas) {
            calculateDepth(area, 0);
            if (area.getChildren() != null && !area.getChildren().isEmpty()) {
                calculateDepthAndStats(area.getChildren());
            }
        }
    }

    /**
     * 计算单个区域的深度
     *
     * @param area 区域对象
     * @param currentDepth 当前深度
     */
    private void calculateDepth(AreaTreeVO area, int currentDepth) {
        area.setDepth(currentDepth);
        if (area.getChildren() != null) {
            for (AreaTreeVO child : area.getChildren()) {
                calculateDepth(child, currentDepth + 1);
            }
        }
    }

    /**
     * 设置同级节点关系
     *
     * @param children 子节点列表
     */
    private void setSiblingRelation(List<AreaTreeVO> children) {
        if (children == null || children.isEmpty()) {
            return;
        }

        for (int i = 0; i < children.size(); i++) {
            AreaTreeVO current = children.get(i);
            current.setPreId(i > 0 ? children.get(i - 1).getAreaId() : null);
            current.setNextId(i < children.size() - 1 ? children.get(i + 1).getAreaId() : null);
        }
    }

    /**
     * 为区域树设置统计信息
     *
     * @param treeList 区域树列表
     */
    private void setStatistics(List<AreaTreeVO> treeList) {
        for (AreaTreeVO tree : treeList) {
            setAreaStatistics(tree);
        }
    }

    /**
     * 为单个区域设置统计信息
     *
     * @param area 区域对象
     */
    private void setAreaStatistics(AreaTreeVO area) {
        // 计算子区域数量
        int childrenCount = 0;
        if (area.getChildren() != null) {
            childrenCount = area.getChildren().size();
            area.setChildrenCount(childrenCount);
        }
        area.setHasChildren(childrenCount > 0);

        // 递归设置子区域统计信息
        if (area.getChildren() != null) {
            for (AreaTreeVO child : area.getChildren()) {
                setAreaStatistics(child);
            }
        }

        // 设置自引用ID列表
        List<Long> selfAndAllChildrenIds = new ArrayList<>();
        selfAndAllChildrenIds.add(area.getAreaId());
        if (area.getChildren() != null) {
            for (AreaTreeVO child : area.getChildren()) {
                if (child.getSelfAndAllChildrenIds() != null) {
                    selfAndAllChildrenIds.addAll(child.getSelfAndChildrenIds());
                }
            }
        }
        area.setSelfAndAllChildrenIds(selfAndAllChildrenIds);
        area.setSelfAndAllChildrenIdsStr(
            selfAndAllChildrenIds.stream().map(String::valueOf).collect(Collectors.joining(","))
        );

        // 设置节点类型
        area.setNodeType(area.getChildren() != null && !area.getChildren().isEmpty() ? "folder" : "item");

        // 设置默认图标（根据区域类型）
        if (area.getIcon() == null) {
            AreaTypeEnum typeEnum = AreaTypeEnum.fromValue(area.getAreaType());
            area.setIcon(getIconByType(typeEnum));
        }

        // 设置展开状态（默认展开前两层）
        area.setExpanded(area.getDepth() != null && area.getDepth() < 2);

        // 设置选中状态
        area.setSelected(false);

        // 设置禁用状态
        area.setDisabled(!area.isEnabled() || area.isMaintenance());
    }

    /**
     * 根据区域类型枚举获取图标
     *
     * @param areaTypeEnum 区域类型枚举
     * @return 图标名称
     */
    private String getIconByType(AreaTypeEnum areaTypeEnum) {
        if (areaTypeEnum == null) {
            return "folder";
        }
        switch (areaTypeEnum) {
            case CAMPUS:
                return "apartment";
            case BUILDING:
                return "building";
            case FLOOR:
                return "bank";
            case ROOM:
                return "home";
            case AREA:
                return "tags";
            default:
                return "folder";
        }
    }

    /**
     * 根据区域类型获取图标
     *
     * @param areaType 区域类型
     * @return 图标名称
     */
    private String getIconByType(Integer areaType) {
        AreaTypeEnum typeEnum = AreaTypeEnum.fromValue(areaType);
        switch (typeEnum) {
            case CAMPUS:
                return "apartment";
            case BUILDING:
                return "building";
            case FLOOR:
                return "bank";
            case ROOM:
                return "home";
            case AREA:
                return "tags";
            default:
                return "folder";
        }
    }

    /**
     * 计算区域路径
     *
     * @param area 区域信息
     * @return 路径
     */
    public String calculateAreaPath(AreaEntity area) {
        if (area == null) {
            return "";
        }

        if (area.getParentId() == null || area.getParentId() == 0) {
            return area.getAreaName();
        }

        // 递归获取父级路径
        List<Long> parentIds = areaDao.selectAllParentIds(area.getAreaId());
        if (parentIds.isEmpty()) {
            return area.getAreaName();
        }

        // 反转父级ID列表（从根到父级）
        List<String> pathNames = new ArrayList<>();
        for (int i = parentIds.size() - 1; i >= 0; i--) {
            Long parentId = parentIds.get(i);
            if (parentId != null && parentId != 0) {
                AreaEntity parentArea = areaDao.selectById(parentId);
                if (parentArea != null) {
                    pathNames.add(parentArea.getAreaName());
                }
            }
        }
        pathNames.add(area.getAreaName());

        return String.join(" > ", pathNames);
    }

    /**
     * 获取区域完整路径字符串
     *
     * @param area 区域信息
     * @return 路径字符串
     */
    public String getFullPath(AreaEntity area) {
        List<String> pathParts = areaDao.selectAreaPath(area.getAreaId());
        if (CollectionUtils.isEmpty(pathParts)) {
            return area.getAreaName();
        }

        // 取最后一部分作为完整路径
        return pathParts.get(pathParts.size() - 1);
    }

    /**
     * 验证区域层级关系
     *
     * @param area 区域信息
     * @return 验证结果
     */
    public boolean validateAreaHierarchy(AreaEntity area) {
        if (area == null) {
            return false;
        }

        // 根节点验证
        if (area.getParentId() == null || area.getParentId() == 0) {
            return true;
        }

        // 检查父区域是否存在
        AreaEntity parentArea = areaDao.selectById(area.getParentId());
        if (parentArea == null) {
            log.error("父区域不存在: parentId={}", area.getParentId());
            return false;
        }

        // 检查是否形成循环引用
        List<Long> parentIds = areaDao.selectAllParentIds(area.getParentId());
        if (parentIds.contains(area.getAreaId())) {
            log.error("检测到循环引用: areaId={}, parentId={}", area.getAreaId(), area.getParentId());
            return false;
        }

        // 检查层级深度是否超过限制（最多10层）
        int maxLevel = 10;
        if (parentIds.size() >= maxLevel) {
            log.error("区域层级过深: areaId={}, level={}", area.getAreaId(), parentIds.size());
            return false;
        }

        return true;
    }

    /**
     * 检查区域编码唯一性
     *
     * @param areaCode 区域编码
     * @param excludeId 排除的ID
     * @return 是否唯一
     */
    public boolean isAreaCodeUnique(String areaCode, Long excludeId) {
        int count = areaDao.countByAreaCodeExcludeId(areaCode, excludeId);
        return count == 0;
    }

    /**
     * 检查区域是否可以删除
     *
     * @param areaId 区域ID
     * @return 是否可以删除
     */
    public boolean canDelete(Long areaId) {
        int childCount = areaDao.countChildren(areaId);
        return childCount == 0;
    }

    /**
     * 获取区域的所有子区域ID
     *
     * @param areaId 区域ID
     * @return 子区域ID列表
     */
    public List<Long> getAllChildIds(Long areaId) {
        return areaDao.selectSelfAndAllChildrenIds(areaId);
    }

    /**
     * 获取区域的所有父区域ID
     *
     * @param areaId 区域ID
     * @return 父区域ID列表
     */
    public List<Long> getAllParentIds(Long areaId) {
        return areaDao.selectAllParentIds(areaId);
    }
}