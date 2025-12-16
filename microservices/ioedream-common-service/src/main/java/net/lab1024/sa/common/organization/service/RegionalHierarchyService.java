package net.lab1024.sa.common.organization.service;

import net.lab1024.sa.common.organization.manager.RegionalHierarchyManager;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import net.lab1024.sa.common.organization.dao.AreaDao;
import net.lab1024.sa.common.response.ResponseDTO;

import java.util.List;
import java.util.Map;

/**
 * 区域层级管理服务接口
 * 提供五级层级架构：园区→建筑→楼层→区域→房间
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
public interface RegionalHierarchyService {

    /**
     * 获取区域完整层级路径
     *
     * @param areaId 区域ID
     * @return 层级路径（如：园区A→建筑B→楼层1→东区→101房间）
     */
    ResponseDTO<String> getAreaHierarchyPath(Long areaId);

    /**
     * 获取指定父区域下的所有子区域（按层级）
     *
     * @param parentAreaId 父区域ID，null表示获取顶级园区
     * @param areaLevel 区域级别
     * @return 子区域列表
     */
    ResponseDTO<List<AreaEntity>> getChildAreas(Long parentAreaId, Integer areaLevel);

    /**
     * 获取区域的所有下级区域（递归）
     *
     * @param parentAreaId 父区域ID
     * @return 所有下级区域列表
     */
    ResponseDTO<List<AreaEntity>> getAllChildAreas(Long parentAreaId);

    /**
     * 获取区域的所有上级区域（递归）
     *
     * @param areaId 区域ID
     * @return 所有上级区域列表（从直接上级到最顶级）
     */
    ResponseDTO<List<AreaEntity>> getAllParentAreas(Long areaId);

    /**
     * 检查区域层级结构是否合法
     *
     * @param areaId 区域ID
     * @return 检查结果
     */
    ResponseDTO<RegionalHierarchyManager.HierarchyValidationResult> validateHierarchy(Long areaId);

    /**
     * 获取区域的同级区域
     *
     * @param areaId 区域ID
     * @return 同级区域列表
     */
    ResponseDTO<List<AreaEntity>> getSiblingAreas(Long areaId);

    /**
     * 获取区域层级树结构
     *
     * @param rootParentId 根父区域ID，null表示从所有园区开始
     * @return 层级树结构
     */
    ResponseDTO<List<RegionalHierarchyManager.AreaHierarchyTree>> getAreaHierarchyTree(Long rootParentId);

    /**
     * 获取区域统计信息
     *
     * @param parentAreaId 父区域ID
     * @return 统计信息
     */
    ResponseDTO<RegionalHierarchyManager.AreaStatistics> getAreaStatistics(Long parentAreaId);

    /**
     * 检查是否可以删除区域（无子区域）
     *
     * @param areaId 区域ID
     * @return 是否可以删除
     */
    ResponseDTO<Boolean> canDeleteArea(Long areaId);

    /**
     * 获取区域类型的所有区域
     *
     * @param areaType 区域类型
     * @return 区域列表
     */
    ResponseDTO<List<AreaEntity>> getAreasByType(Integer areaType);

    /**
     * 根据区域编码查找区域
     *
     * @param areaCode 区域编码
     * @return 区域实体
     */
    ResponseDTO<AreaEntity> getAreaByCode(String areaCode);

    /**
     * 搜索区域（按名称或编码）
     *
     * @param keyword 关键词
     * @return 搜索结果
     */
    ResponseDTO<List<AreaEntity>> searchAreas(String keyword);

    /**
     * 创建区域（自动验证层级结构）
     *
     * @param areaEntity 区域实体
     * @return 创建的区域ID
     */
    ResponseDTO<Long> createArea(AreaEntity areaEntity);

    /**
     * 更新区域信息
     *
     * @param areaEntity 区域实体
     * @return 更新结果
     */
    ResponseDTO<Void> updateArea(AreaEntity areaEntity);

    /**
     * 删除区域（检查子区域）
     *
     * @param areaId 区域ID
     * @return 删除结果
     */
    ResponseDTO<Void> deleteArea(Long areaId);

    /**
     * 移动区域到新的父级下
     *
     * @param areaId 区域ID
     * @param newParentId 新的父区域ID
     * @param newLevel 新的层级级别
     * @return 移动结果
     */
    ResponseDTO<Void> moveArea(Long areaId, Long newParentId, Integer newLevel);

    /**
     * 批量操作区域
     *
     * @param areaIds 区域ID列表
     * @param operation 操作类型
     * @return 操作结果
     */
    ResponseDTO<Void> batchOperation(List<Long> areaIds, String operation);

    /**
     * 获取区域层级配置信息
     *
     * @return 层级配置信息
     */
    ResponseDTO<Map<String, Object>> getHierarchyConfig();

    /**
     * 验证区域名称在同一级别下的唯一性
     *
     * @param areaName 区域名称
     * @param parentAreaId 父区域ID
     * @param areaLevel 区域级别
     * @param excludeAreaId 排除的区域ID
     * @return 验证结果
     */
    ResponseDTO<Boolean> validateAreaNameUnique(String areaName, Long parentAreaId, Integer areaLevel, Long excludeAreaId);

    /**
     * 验证区域编码的唯一性
     *
     * @param areaCode 区域编码
     * @param excludeAreaId 排除的区域ID
     * @return 验证结果
     */
    ResponseDTO<Boolean> validateAreaCodeUnique(String areaCode, Long excludeAreaId);

    /**
     * 获取区域的完整路径ID列表
     *
     * @param areaId 区域ID
     * @return 路径ID列表（从顶级到当前区域）
     */
    ResponseDTO<List<Long>> getAreaPathIds(Long areaId);

    /**
     * 检查用户对指定区域的访问权限
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @return 是否有权限
     */
    ResponseDTO<Boolean> checkUserAreaPermission(Long userId, Long areaId);

    /**
     * 获取用户可访问的区域列表
     *
     * @param userId 用户ID
     * @return 可访问的区域列表
     */
    ResponseDTO<List<AreaEntity>> getUserAccessibleAreas(Long userId);
}