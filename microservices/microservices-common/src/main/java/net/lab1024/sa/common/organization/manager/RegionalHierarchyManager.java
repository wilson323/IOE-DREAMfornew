package net.lab1024.sa.common.organization.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import net.lab1024.sa.common.organization.dao.AreaDao;
import net.lab1024.sa.common.response.PageResult;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 区域层级管理器
 * 支持五级层级架构：园区→建筑→楼层→区域→房间
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegionalHierarchyManager {

    private final AreaDao areaDao;

    // 区域类型常量
    public static class AreaType {
        public static final Integer CAMPUS = 1;    // 园区
        public static final Integer BUILDING = 2;   // 建筑
        public static final Integer FLOOR = 3;      // 楼层
        public static final Integer AREA = 4;       // 区域
        public static final Integer ROOM = 5;       // 房间
    }

    // 区域级别映射
    private static final Map<Integer, Integer> AREA_LEVEL_MAP = Map.of(
        AreaType.CAMPUS, 1,
        AreaType.BUILDING, 2,
        AreaType.FLOOR, 3,
        AreaType.AREA, 4,
        AreaType.ROOM, 5
    );

    // 区域级别名称映射
    private static final Map<Integer, String> AREA_LEVEL_NAME_MAP = Map.of(
        1, "园区",
        2, "建筑",
        3, "楼层",
        4, "区域",
        5, "房间"
    );

    /**
     * 获取区域完整层级路径
     *
     * @param areaId 区域ID
     * @return 层级路径（如：园区A→建筑B→楼层1→东区→101房间）
     */
    public String getAreaHierarchyPath(Long areaId) {
        AreaEntity area = areaDao.selectById(areaId);
        if (area == null) {
            return "";
        }

        List<String> pathList = new ArrayList<>();
        buildHierarchyPath(area, pathList);

        // 反转路径，从顶级开始
        Collections.reverse(pathList);
        return String.join(" → ", pathList);
    }

    /**
     * 构建层级路径
     */
    private void buildHierarchyPath(AreaEntity area, List<String> pathList) {
        if (area == null) {
            return;
        }

        // 添加当前区域到路径
        String levelName = AREA_LEVEL_NAME_MAP.get(area.getAreaLevel());
        pathList.add(area.getAreaName() + "(" + levelName + ")");

        // 递归获取父区域
        if (area.getParentAreaId() != null && area.getParentAreaId() > 0) {
            AreaEntity parentArea = areaDao.selectById(area.getParentAreaId());
            buildHierarchyPath(parentArea, pathList);
        }
    }

    /**
     * 获取指定父区域下的所有子区域（按层级）
     *
     * @param parentAreaId 父区域ID，null表示获取顶级园区
     * @param areaLevel 区域级别
     * @return 子区域列表
     */
    public List<AreaEntity> getChildAreas(Long parentAreaId, Integer areaLevel) {
        return areaDao.selectByParentIdAndLevel(parentAreaId, areaLevel);
    }

    /**
     * 获取区域的所有下级区域（递归）
     *
     * @param parentAreaId 父区域ID
     * @return 所有下级区域列表
     */
    public List<AreaEntity> getAllChildAreas(Long parentAreaId) {
        List<AreaEntity> allChildren = new ArrayList<>();
        List<AreaEntity> directChildren = areaDao.selectByParentId(parentAreaId);

        for (AreaEntity child : directChildren) {
            allChildren.add(child);
            // 递归获取子区域的下级区域
            allChildren.addAll(getAllChildAreas(child.getAreaId()));
        }

        return allChildren;
    }

    /**
     * 获取区域的所有上级区域（递归）
     *
     * @param areaId 区域ID
     * @return 所有上级区域列表（从直接上级到最顶级）
     */
    public List<AreaEntity> getAllParentAreas(Long areaId) {
        List<AreaEntity> parentAreas = new ArrayList<>();
        AreaEntity area = areaDao.selectById(areaId);

        while (area != null && area.getParentAreaId() != null && area.getParentAreaId() > 0) {
            AreaEntity parentArea = areaDao.selectById(area.getParentAreaId());
            if (parentArea != null) {
                parentAreas.add(parentArea);
                area = parentArea;
            } else {
                break;
            }
        }

        return parentAreas;
    }

    /**
     * 检查区域层级结构是否合法
     *
     * @param areaId 区域ID
     * @return 检查结果
     */
    public HierarchyValidationResult validateHierarchy(Long areaId) {
        AreaEntity area = areaDao.selectById(areaId);
        if (area == null) {
            return HierarchyValidationResult.error("区域不存在");
        }

        // 检查区域级别
        Integer expectedLevel = AREA_LEVEL_MAP.get(area.getAreaType());
        if (!Objects.equals(expectedLevel, area.getAreaLevel())) {
            return HierarchyValidationResult.error(
                String.format("区域类型与级别不匹配：类型=%d，期望级别=%d，实际级别=%d",
                    area.getAreaType(), expectedLevel, area.getAreaLevel()));
        }

        // 检查父区域级别
        if (area.getParentAreaId() != null && area.getParentAreaId() > 0) {
            AreaEntity parentArea = areaDao.selectById(area.getParentAreaId());
            if (parentArea == null) {
                return HierarchyValidationResult.error("父区域不存在");
            }

            // 父区域级别必须比当前区域级别小1
            if (parentArea.getAreaLevel() != area.getAreaLevel() - 1) {
                return HierarchyValidationResult.error(
                    String.format("父区域级别错误：父区域级别=%d，当前区域级别=%d",
                        parentArea.getAreaLevel(), area.getAreaLevel()));
            }
        }

        return HierarchyValidationResult.success();
    }

    /**
     * 获取区域的同级区域
     *
     * @param areaId 区域ID
     * @return 同级区域列表
     */
    public List<AreaEntity> getSiblingAreas(Long areaId) {
        AreaEntity area = areaDao.selectById(areaId);
        if (area == null || area.getParentAreaId() == null) {
            return new ArrayList<>();
        }

        return areaDao.selectByParentIdAndLevel(area.getParentAreaId(), area.getAreaLevel());
    }

    /**
     * 获取区域层级树结构
     *
     * @param rootParentId 根父区域ID，null表示从所有园区开始
     * @return 层级树结构
     */
    public List<AreaHierarchyTree> getAreaHierarchyTree(Long rootParentId) {
        List<AreaEntity> rootAreas = areaDao.selectByParentId(rootParentId);
        List<AreaHierarchyTree> treeList = new ArrayList<>();

        for (AreaEntity rootArea : rootAreas) {
            AreaHierarchyTree treeNode = buildHierarchyTree(rootArea);
            treeList.add(treeNode);
        }

        return treeList;
    }

    /**
     * 构建层级树节点
     */
    private AreaHierarchyTree buildHierarchyTree(AreaEntity area) {
        AreaHierarchyTree node = new AreaHierarchyTree();
        node.setAreaId(area.getAreaId());
        node.setAreaName(area.getAreaName());
        node.setAreaCode(area.getAreaCode());
        node.setAreaType(area.getAreaType());
        node.setAreaLevel(area.getAreaLevel());
        node.setParentAreaId(area.getParentAreaId());
        node.setStatus(area.getStatus());

        // 递归构建子节点
        List<AreaEntity> children = areaDao.selectByParentIdAndLevel(area.getAreaId(), null);
        List<AreaHierarchyTree> childNodes = new ArrayList<>();

        for (AreaEntity child : children) {
            AreaHierarchyTree childNode = buildHierarchyTree(child);
            childNodes.add(childNode);
        }

        // 按区域名称排序
        childNodes.sort(Comparator.comparing(AreaHierarchyTree::getAreaName));
        node.setChildren(childNodes);

        return node;
    }

    /**
     * 获取区域统计信息
     *
     * @param parentAreaId 父区域ID
     * @return 统计信息
     */
    public AreaStatistics getAreaStatistics(Long parentAreaId) {
        AreaStatistics statistics = new AreaStatistics();

        // 获取所有下级区域
        List<AreaEntity> allAreas = getAllChildAreas(parentAreaId);

        // 按级别统计
        Map<Integer, Long> levelCountMap = allAreas.stream()
            .collect(Collectors.groupingBy(AreaEntity::getAreaLevel, Collectors.counting()));

        statistics.setTotalCount(allAreas.size());
        statistics.setCampusCount(levelCountMap.getOrDefault(1, 0L));
        statistics.setBuildingCount(levelCountMap.getOrDefault(2, 0L));
        statistics.setFloorCount(levelCountMap.getOrDefault(3, 0L));
        statistics.setAreaCount(levelCountMap.getOrDefault(4, 0L));
        statistics.setRoomCount(levelCountMap.getOrDefault(5, 0L));

        // 状态统计
        Map<Integer, Long> statusCountMap = allAreas.stream()
            .collect(Collectors.groupingBy(AreaEntity::getStatus, Collectors.counting()));

        statistics.setActiveCount(statusCountMap.getOrDefault(1, 0L));
        statistics.setInactiveCount(statusCountMap.getOrDefault(0, 0L));

        return statistics;
    }

    /**
     * 检查是否可以删除区域（无子区域）
     *
     * @param areaId 区域ID
     * @return 是否可以删除
     */
    public boolean canDeleteArea(Long areaId) {
        List<AreaEntity> children = areaDao.selectByParentId(areaId);
        return children.isEmpty();
    }

    /**
     * 获取区域类型的所有区域
     *
     * @param areaType 区域类型
     * @return 区域列表
     */
    public List<AreaEntity> getAreasByType(Integer areaType) {
        Integer areaLevel = AREA_LEVEL_MAP.get(areaType);
        if (areaLevel == null) {
            return new ArrayList<>();
        }
        return areaDao.selectByLevel(areaLevel);
    }

    /**
     * 根据区域编码查找区域
     *
     * @param areaCode 区域编码
     * @return 区域实体
     */
    public AreaEntity getAreaByCode(String areaCode) {
        return areaDao.selectByAreaCode(areaCode);
    }

    /**
     * 搜索区域（按名称或编码）
     *
     * @param keyword 关键词
     * @return 搜索结果
     */
    public List<AreaEntity> searchAreas(String keyword) {
        return areaDao.selectByKeyword(keyword);
    }

    /**
     * 验证区域移动是否合法
     * 检查是否会形成循环依赖，以及层级关系是否正确
     *
     * @param areaId 要移动的区域ID
     * @param newParentId 新的父区域ID
     * @return 验证结果
     */
    public MoveValidationResult validateAreaMove(Long areaId, Long newParentId) {
        if (areaId.equals(newParentId)) {
            return MoveValidationResult.error("不能将区域移动到自身下");
        }

        AreaEntity area = areaDao.selectById(areaId);
        if (area == null) {
            return MoveValidationResult.error("要移动的区域不存在");
        }

        // 如果新的父区域为空，则移动到顶级
        if (newParentId == null || newParentId == 0) {
            if (area.getAreaLevel() != 1) {
                return MoveValidationResult.error("只有园区级别的区域可以移动到顶级");
            }
            return MoveValidationResult.success();
        }

        AreaEntity newParent = areaDao.selectById(newParentId);
        if (newParent == null) {
            return MoveValidationResult.error("新的父区域不存在");
        }

        // 检查是否会形成循环依赖
        if (wouldCreateCircularReference(areaId, newParentId)) {
            return MoveValidationResult.error("移动后会形成循环依赖");
        }

        // 检查新的父区域是否在当前区域的子树中
        if (isDescendant(areaId, newParentId)) {
            return MoveValidationResult.error("不能将区域移动到其子区域下");
        }

        // 检查层级关系是否正确
        int expectedLevel = newParent.getAreaLevel() + 1;
        if (area.getAreaLevel() != expectedLevel) {
            return MoveValidationResult.error(
                String.format("区域级别不匹配：当前级别=%d，期望级别=%d", area.getAreaLevel(), expectedLevel));
        }

        return MoveValidationResult.success();
    }

    /**
     * 批量验证区域移动
     *
     * @param moveList 移动列表
     * @return 批量验证结果
     */
    public BatchMoveValidationResult validateBatchMove(List<AreaMoveRequest> moveList) {
        BatchMoveValidationResult batchResult = new BatchMoveValidationResult();

        for (AreaMoveRequest request : moveList) {
            MoveValidationResult result = validateAreaMove(request.getAreaId(), request.getNewParentId());
            batchResult.addResult(request.getAreaId(), result);
        }

        return batchResult;
    }

    /**
     * 获取区域的完整路径（带层级信息）
     *
     * @param areaId 区域ID
     * @return 路径信息
     */
    public AreaPathInfo getAreaPathInfo(Long areaId) {
        List<AreaEntity> pathAreas = areaDao.selectAreaPath(areaId);
        if (pathAreas.isEmpty()) {
            return null;
        }

        AreaPathInfo pathInfo = new AreaPathInfo();
        pathInfo.setAreaId(areaId);
        pathInfo.setPathAreas(pathAreas);
        pathInfo.setPathString(buildPathString(pathAreas));
        pathInfo.setLevelCount(pathAreas.size());
        pathInfo.setMaxLevel(pathAreas.get(0).getAreaLevel());

        return pathInfo;
    }

    /**
     * 计算两个区域之间的层级关系
     *
     * @param area1Id 区域1的ID
     * @param area2Id 区域2的ID
     * @return 层级关系
     */
    public AreaRelationship calculateAreaRelationship(Long area1Id, Long area2Id) {
        if (area1Id.equals(area2Id)) {
            return AreaRelationship.SAME;
        }

        if (isDescendant(area1Id, area2Id)) {
            return AreaRelationship.PARENT;
        }

        if (isDescendant(area2Id, area1Id)) {
            return AreaRelationship.CHILD;
        }

        if (areSiblings(area1Id, area2Id)) {
            return AreaRelationship.SIBLING;
        }

        return AreaRelationship.UNRELATED;
    }

    /**
     * 获取区域的公共父区域
     *
     * @param area1Id 区域1的ID
     * @param area2Id 区域2的ID
     * @return 公共父区域
     */
    public AreaEntity getCommonParent(Long area1Id, Long area2Id) {
        List<AreaEntity> path1 = areaDao.selectAreaPath(area1Id);
        List<AreaEntity> path2 = areaDao.selectAreaPath(area2Id);

        // 从最顶级开始找第一个不同的节点
        AreaEntity commonParent = null;
        int minLength = Math.min(path1.size(), path2.size());

        for (int i = 0; i < minLength; i++) {
            AreaEntity node1 = path1.get(path1.size() - 1 - i);
            AreaEntity node2 = path2.get(path2.size() - 1 - i);

            if (node1.getAreaId().equals(node2.getAreaId())) {
                commonParent = node1;
            } else {
                break;
            }
        }

        return commonParent;
    }

    /**
     * 检查移动是否会形成循环引用
     */
    private boolean wouldCreateCircularReference(Long areaId, Long newParentId) {
        List<Long> parentChain = new ArrayList<>();
        Long currentParent = newParentId;

        while (currentParent != null && currentParent > 0) {
            if (currentParent.equals(areaId)) {
                return true; // 找到循环引用
            }
            parentChain.add(currentParent);

            AreaEntity parentArea = areaDao.selectById(currentParent);
            currentParent = parentArea != null ? parentArea.getParentAreaId() : null;
        }

        return false;
    }

    /**
     * 检查目标区域是否是源区域的子孙
     */
    private boolean isDescendant(Long ancestorId, Long descendantId) {
        List<Long> childIds = getAllChildAreaIds(ancestorId);
        return childIds.contains(descendantId);
    }

    /**
     * 获取所有子区域ID（递归）
     */
    private List<Long> getAllChildAreaIds(Long parentId) {
        List<Long> childIds = new ArrayList<>();
        List<AreaEntity> children = areaDao.selectByParentId(parentId);

        for (AreaEntity child : children) {
            childIds.add(child.getAreaId());
            childIds.addAll(getAllChildAreaIds(child.getAreaId()));
        }

        return childIds;
    }

    /**
     * 检查两个区域是否是同级
     */
    private boolean areSiblings(Long area1Id, Long area2Id) {
        AreaEntity area1 = areaDao.selectById(area1Id);
        AreaEntity area2 = areaDao.selectById(area2Id);

        if (area1 == null || area2 == null) {
            return false;
        }

        return Objects.equals(area1.getParentAreaId(), area2.getParentAreaId()) &&
               Objects.equals(area1.getAreaLevel(), area2.getAreaLevel());
    }

    /**
     * 构建路径字符串
     */
    private String buildPathString(List<AreaEntity> pathAreas) {
        return pathAreas.stream()
            .sorted((a, b) -> Integer.compare(b.getAreaLevel(), a.getAreaLevel()))
            .map(area -> area.getAreaName() + "(" + AREA_LEVEL_NAME_MAP.get(area.getAreaLevel()) + ")")
            .collect(Collectors.joining(" → "));
    }

    /**
     * 区域移动请求
     */
    public static class AreaMoveRequest {
        private Long areaId;
        private Long newParentId;
        private Integer newLevel;

        // getters and setters
        public Long getAreaId() { return areaId; }
        public void setAreaId(Long areaId) { this.areaId = areaId; }
        public Long getNewParentId() { return newParentId; }
        public void setNewParentId(Long newParentId) { this.newParentId = newParentId; }
        public Integer getNewLevel() { return newLevel; }
        public void setNewLevel(Integer newLevel) { this.newLevel = newLevel; }
    }

    /**
     * 移动验证结果
     */
    public static class MoveValidationResult {
        private boolean valid;
        private String message;

        public static MoveValidationResult success() {
            MoveValidationResult result = new MoveValidationResult();
            result.setValid(true);
            result.setMessage("移动操作合法");
            return result;
        }

        public static MoveValidationResult error(String message) {
            MoveValidationResult result = new MoveValidationResult();
            result.setValid(false);
            result.setMessage(message);
            return result;
        }

        // getters and setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    /**
     * 批量移动验证结果
     */
    public static class BatchMoveValidationResult {
        private Map<Long, MoveValidationResult> results = new HashMap<>();
        private boolean allValid = true;

        public void addResult(Long areaId, MoveValidationResult result) {
            results.put(areaId, result);
            if (!result.isValid()) {
                allValid = false;
            }
        }

        // getters and setters
        public Map<Long, MoveValidationResult> getResults() { return results; }
        public void setResults(Map<Long, MoveValidationResult> results) { this.results = results; }
        public boolean isAllValid() { return allValid; }
        public void setAllValid(boolean allValid) { this.allValid = allValid; }
    }

    /**
     * 区域路径信息
     */
    public static class AreaPathInfo {
        private Long areaId;
        private List<AreaEntity> pathAreas;
        private String pathString;
        private Integer levelCount;
        private Integer maxLevel;

        // getters and setters
        public Long getAreaId() { return areaId; }
        public void setAreaId(Long areaId) { this.areaId = areaId; }
        public List<AreaEntity> getPathAreas() { return pathAreas; }
        public void setPathAreas(List<AreaEntity> pathAreas) { this.pathAreas = pathAreas; }
        public String getPathString() { return pathString; }
        public void setPathString(String pathString) { this.pathString = pathString; }
        public Integer getLevelCount() { return levelCount; }
        public void setLevelCount(Integer levelCount) { this.levelCount = levelCount; }
        public Integer getMaxLevel() { return maxLevel; }
        public void setMaxLevel(Integer maxLevel) { this.maxLevel = maxLevel; }
    }

    /**
     * 区域关系枚举
     */
    public enum AreaRelationship {
        SAME,           // 同一个区域
        PARENT,         // 父子关系（area1是area2的父级）
        CHILD,          // 子级关系（area1是area2的子级）
        SIBLING,        // 同级关系
        UNRELATED       // 无关关系
    }
}

    /**
     * 层级验证结果
     */
    public static class HierarchyValidationResult {
        private boolean valid;
        private String message;

        public static HierarchyValidationResult success() {
            HierarchyValidationResult result = new HierarchyValidationResult();
            result.setValid(true);
            result.setMessage("层级结构合法");
            return result;
        }

        public static HierarchyValidationResult error(String message) {
            HierarchyValidationResult result = new HierarchyValidationResult();
            result.setValid(false);
            result.setMessage(message);
            return result;
        }

        // getters and setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    /**
     * 区域层级树节点
     */
    public static class AreaHierarchyTree {
        private Long areaId;
        private String areaName;
        private String areaCode;
        private Integer areaType;
        private Integer areaLevel;
        private Long parentAreaId;
        private Integer status;
        private List<AreaHierarchyTree> children;

        // getters and setters
        public Long getAreaId() { return areaId; }
        public void setAreaId(Long areaId) { this.areaId = areaId; }
        public String getAreaName() { return areaName; }
        public void setAreaName(String areaName) { this.areaName = areaName; }
        public String getAreaCode() { return areaCode; }
        public void setAreaCode(String areaCode) { this.areaCode = areaCode; }
        public Integer getAreaType() { return areaType; }
        public void setAreaType(Integer areaType) { this.areaType = areaType; }
        public Integer getAreaLevel() { return areaLevel; }
        public void setAreaLevel(Integer areaLevel) { this.areaLevel = areaLevel; }
        public Long getParentAreaId() { return parentAreaId; }
        public void setParentAreaId(Long parentAreaId) { this.parentAreaId = parentAreaId; }
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
        public List<AreaHierarchyTree> getChildren() { return children; }
        public void setChildren(List<AreaHierarchyTree> children) { this.children = children; }
    }

    /**
     * 区域统计信息
     */
    public static class AreaStatistics {
        private Long totalCount;
        private Long campusCount;
        private Long buildingCount;
        private Long floorCount;
        private Long areaCount;
        private Long roomCount;
        private Long activeCount;
        private Long inactiveCount;

        // getters and setters
        public Long getTotalCount() { return totalCount; }
        public void setTotalCount(Long totalCount) { this.totalCount = totalCount; }
        public Long getCampusCount() { return campusCount; }
        public void setCampusCount(Long campusCount) { this.campusCount = campusCount; }
        public Long getBuildingCount() { return buildingCount; }
        public void setBuildingCount(Long buildingCount) { this.buildingCount = buildingCount; }
        public Long getFloorCount() { return floorCount; }
        public void setFloorCount(Long floorCount) { this.floorCount = floorCount; }
        public Long getAreaCount() { return areaCount; }
        public void setAreaCount(Long areaCount) { this.areaCount = areaCount; }
        public Long getRoomCount() { return roomCount; }
        public void setRoomCount(Long roomCount) { this.roomCount = roomCount; }
        public Long getActiveCount() { return activeCount; }
        public void setActiveCount(Long activeCount) { this.activeCount = activeCount; }
        public Long getInactiveCount() { return inactiveCount; }
        public void setInactiveCount(Long inactiveCount) { this.inactiveCount = inactiveCount; }
    }
}