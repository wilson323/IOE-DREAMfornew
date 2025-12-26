package net.lab1024.sa.common.organization.manager;

import net.lab1024.sa.common.organization.entity.AreaEntity;
import net.lab1024.sa.common.organization.dao.AreaDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 区域层级管理器
 * <p>
 * 职责：
 * - 区域层级结构管理
 * - 层级验证和校验
 * - 层级树构建
 * - 层级统计
 * </p>
 * <p>
 * 注意：Manager类是纯Java类，不直接依赖Spring注解，通过构造函数注入依赖。
 * 在微服务中通过配置类将Manager注册为Spring Bean。
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
public class RegionalHierarchyManager {

    private final AreaDao areaDao;

    /**
     * 构造函数注入依赖
     *
     * @param areaDao 区域数据访问对象
     */
    public RegionalHierarchyManager(AreaDao areaDao) {
        this.areaDao = areaDao;
    }

    /**
     * 区域类型常量
     */
    public static final class AreaType {
        public static final int CAMPUS = 1;    // 园区
        public static final int BUILDING = 2;  // 建筑
        public static final int FLOOR = 3;     // 楼层
        public static final int ROOM = 4;      // 房间
        public static final int ZONE = 5;      // 区域

        private AreaType() {
            // 私有构造函数，防止实例化
        }
    }

    /**
     * 层级验证结果
     */
    public static class HierarchyValidationResult {
        private boolean valid;
        private String message;

        public HierarchyValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * 区域层级树
     */
    public static class AreaHierarchyTree {
        private AreaEntity area;
        private List<AreaHierarchyTree> children;

        public AreaHierarchyTree(AreaEntity area, List<AreaHierarchyTree> children) {
            this.area = area;
            this.children = children;
        }

        public AreaEntity getArea() {
            return area;
        }

        public List<AreaHierarchyTree> getChildren() {
            return children;
        }
    }

    /**
     * 区域统计信息
     */
    public static class AreaStatistics {
        private Long totalAreas;
        private Long totalDevices;
        private Long totalUsers;

        public AreaStatistics(Long totalAreas, Long totalDevices, Long totalUsers) {
            this.totalAreas = totalAreas;
            this.totalDevices = totalDevices;
            this.totalUsers = totalUsers;
        }

        public Long getTotalAreas() {
            return totalAreas;
        }

        public Long getTotalDevices() {
            return totalDevices;
        }

        public Long getTotalUsers() {
            return totalUsers;
        }
    }

    /**
     * 获取区域层级路径
     *
     * @param areaId 区域ID
     * @return 层级路径字符串（如：园区/建筑/楼层/房间）
     */
    public String getAreaHierarchyPath(Long areaId) {
        if (areaId == null) {
            return "";
        }

        List<String> pathList = new ArrayList<>();
        AreaEntity current = areaDao.selectById(areaId);

        while (current != null) {
            pathList.add(0, current.getAreaName());
            if (current.getParentId() == null || current.getParentId() == 0) {
                break;
            }
            current = areaDao.selectById(current.getParentId());
        }

        return String.join("/", pathList);
    }

    /**
     * 获取子区域列表
     *
     * @param parentAreaId 父区域ID
     * @param areaLevel 区域层级（可选）
     * @return 子区域列表
     */
    public List<AreaEntity> getChildAreas(Long parentAreaId, Integer areaLevel) {
        if (parentAreaId == null) {
            return Collections.emptyList();
        }

        List<AreaEntity> children = areaDao.selectByParentId(parentAreaId);
        if (areaLevel != null) {
            children = children.stream()
                    .filter(area -> areaLevel.equals(area.getLevel()))
                    .collect(Collectors.toList());
        }

        return children;
    }

    /**
     * 获取所有下级区域（递归）
     *
     * @param parentAreaId 父区域ID
     * @return 所有下级区域列表
     */
    public List<AreaEntity> getAllChildAreas(Long parentAreaId) {
        if (parentAreaId == null) {
            return Collections.emptyList();
        }

        List<AreaEntity> allChildren = new ArrayList<>();
        List<AreaEntity> directChildren = areaDao.selectByParentId(parentAreaId);
        allChildren.addAll(directChildren);

        for (AreaEntity child : directChildren) {
            allChildren.addAll(getAllChildAreas(child.getAreaId()));
        }

        return allChildren;
    }

    /**
     * 获取所有上级区域
     *
     * @param areaId 区域ID
     * @return 所有上级区域列表（从根到当前）
     */
    public List<AreaEntity> getAllParentAreas(Long areaId) {
        if (areaId == null) {
            return Collections.emptyList();
        }

        List<AreaEntity> parents = new ArrayList<>();
        AreaEntity current = areaDao.selectById(areaId);

        while (current != null && current.getParentId() != null && current.getParentId() != 0) {
            AreaEntity parent = areaDao.selectById(current.getParentId());
            if (parent != null) {
                parents.add(0, parent);
                current = parent;
            } else {
                break;
            }
        }

        return parents;
    }

    /**
     * 验证区域层级结构
     *
     * @param areaId 区域ID
     * @return 验证结果
     */
    public HierarchyValidationResult validateHierarchy(Long areaId) {
        if (areaId == null) {
            return new HierarchyValidationResult(false, "区域ID不能为空");
        }

        AreaEntity area = areaDao.selectById(areaId);
        if (area == null) {
            return new HierarchyValidationResult(false, "区域不存在");
        }

        // 检查是否存在循环引用
        List<Long> visited = new ArrayList<>();
        AreaEntity current = area;
        while (current != null && current.getParentId() != null && current.getParentId() != 0) {
            if (visited.contains(current.getAreaId())) {
                return new HierarchyValidationResult(false, "检测到循环引用");
            }
            visited.add(current.getAreaId());
            current = areaDao.selectById(current.getParentId());
        }

        return new HierarchyValidationResult(true, "层级结构有效");
    }

    /**
     * 获取同级区域列表
     *
     * @param areaId 区域ID
     * @return 同级区域列表
     */
    public List<AreaEntity> getSiblingAreas(Long areaId) {
        if (areaId == null) {
            return Collections.emptyList();
        }

        AreaEntity area = areaDao.selectById(areaId);
        if (area == null || area.getParentId() == null || area.getParentId() == 0) {
            return Collections.emptyList();
        }

        List<AreaEntity> siblings = areaDao.selectByParentId(area.getParentId());
        return siblings.stream()
                .filter(sibling -> !sibling.getAreaId().equals(areaId))
                .collect(Collectors.toList());
    }

    /**
     * 获取区域层级树
     *
     * @param rootParentId 根父区域ID（null表示获取所有根节点）
     * @return 区域层级树列表
     */
    public List<AreaHierarchyTree> getAreaHierarchyTree(Long rootParentId) {
        List<AreaEntity> allAreas = areaDao.selectList(null);
        Map<Long, List<AreaEntity>> childrenMap = allAreas.stream()
                .filter(area -> area.getParentId() != null && area.getParentId() != 0)
                .collect(Collectors.groupingBy(AreaEntity::getParentId));

        List<AreaEntity> rootAreas;
        if (rootParentId == null) {
            rootAreas = allAreas.stream()
                    .filter(area -> area.getParentId() == null || area.getParentId() == 0)
                    .collect(Collectors.toList());
        } else {
            rootAreas = allAreas.stream()
                    .filter(area -> rootParentId.equals(area.getParentId()))
                    .collect(Collectors.toList());
        }

        return rootAreas.stream()
                .map(area -> buildTree(area, childrenMap))
                .collect(Collectors.toList());
    }

    /**
     * 递归构建树结构
     */
    private AreaHierarchyTree buildTree(AreaEntity area, Map<Long, List<AreaEntity>> childrenMap) {
        List<AreaHierarchyTree> children = childrenMap.getOrDefault(area.getAreaId(), Collections.emptyList())
                .stream()
                .map(child -> buildTree(child, childrenMap))
                .collect(Collectors.toList());

        return new AreaHierarchyTree(area, children);
    }

    /**
     * 获取区域统计信息
     *
     * @param parentAreaId 父区域ID
     * @return 统计信息
     */
    public AreaStatistics getAreaStatistics(Long parentAreaId) {
        List<AreaEntity> allChildren = getAllChildAreas(parentAreaId);
        Long totalAreas = (long) allChildren.size();
        Long totalDevices = 0L; // 需要从设备服务获取
        Long totalUsers = 0L;   // 需要从用户服务获取

        return new AreaStatistics(totalAreas, totalDevices, totalUsers);
    }

    /**
     * 检查区域是否可删除
     *
     * @param areaId 区域ID
     * @return 是否可删除
     */
    public boolean canDeleteArea(Long areaId) {
        if (areaId == null) {
            return false;
        }

        // 检查是否有子区域
        List<AreaEntity> children = areaDao.selectByParentId(areaId);
        if (!children.isEmpty()) {
            return false;
        }

        // 可以添加其他检查逻辑（如是否有设备、用户等）

        return true;
    }

    /**
     * 根据类型获取区域列表
     *
     * @param areaType 区域类型（整数）
     * @return 区域列表
     */
    public List<AreaEntity> getAreasByType(Integer areaType) {
        if (areaType == null) {
            return Collections.emptyList();
        }

        // 将整数类型转换为字符串类型进行查询
        String typeStr = convertAreaTypeToString(areaType);
        return areaDao.selectByAreaType(typeStr);
    }

    /**
     * 根据编码获取区域
     *
     * @param areaCode 区域编码
     * @return 区域实体
     */
    public AreaEntity getAreaByCode(String areaCode) {
        if (areaCode == null || areaCode.trim().isEmpty()) {
            return null;
        }

        return areaDao.selectByAreaCode(areaCode);
    }

    /**
     * 将整数区域类型转换为字符串
     *
     * @param areaType 区域类型整数
     * @return 区域类型字符串
     */
    private String convertAreaTypeToString(Integer areaType) {
        switch (areaType) {
            case 1:
                return "CAMPUS";
            case 2:
                return "BUILDING";
            case 3:
                return "FLOOR";
            case 4:
                return "ROOM";
            case 5:
                return "ZONE";
            default:
                return "UNKNOWN";
        }
    }
}

