package net.lab1024.sa.common.system.service;

import java.util.List;
import java.util.Map;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.system.domain.dto.ConfigCreateDTO;
import net.lab1024.sa.common.system.domain.dto.ConfigUpdateDTO;
import net.lab1024.sa.common.system.domain.dto.DictCreateDTO;
import net.lab1024.sa.common.system.domain.vo.DictVO;

/**
 * 系统服务接口
 * <p>
 * 提供系统配置和字典管理功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-02
 */
public interface SystemService {

    /**
     * 创建系统配置
     *
     * @param dto 配置创建DTO
     * @return 配置ID
     */
    ResponseDTO<Long> createConfig(ConfigCreateDTO dto);

    /**
     * 更新系统配置
     *
     * @param configId 配置ID
     * @param dto      配置更新DTO
     * @return 操作结果
     */
    ResponseDTO<Void> updateConfig(Long configId, ConfigUpdateDTO dto);

    /**
     * 删除系统配置
     *
     * @param configId 配置ID
     * @return 操作结果
     */
    ResponseDTO<Void> deleteConfig(Long configId);

    /**
     * 获取系统配置值
     *
     * @param configKey 配置键
     * @return 配置值
     */
    String getConfigValue(String configKey);

    /**
     * 获取系统配置（带ResponseDTO包装）
     *
     * @param configKey 配置键
     * @return 配置值
     */
    ResponseDTO<String> getConfig(String configKey);

    /**
     * 获取所有系统配置
     *
     * @return 所有配置的Map
     */
    ResponseDTO<Map<String, String>> getAllConfigs();

    /**
     * 刷新配置缓存
     *
     * @return 操作结果
     */
    ResponseDTO<Void> refreshConfigCache();

    /**
     * 创建字典
     *
     * @param dto 字典创建DTO
     * @return 字典ID
     */
    ResponseDTO<Long> createDict(DictCreateDTO dto);

    /**
     * 获取字典列表
     *
     * @param dictType 字典类型编码
     * @return 字典列表
     */
    ResponseDTO<List<DictVO>> getDictList(String dictType);

    /**
     * 获取字典树
     *
     * @param dictType 字典类型编码
     * @return 字典树
     */
    ResponseDTO<List<DictVO>> getDictTree(String dictType);

    /**
     * 刷新字典缓存
     *
     * @return 操作结果
     */
    ResponseDTO<Void> refreshDictCache();

    /**
     * 获取系统信息
     *
     * @return 系统信息
     */
    ResponseDTO<Map<String, Object>> getSystemInfo();

    /**
     * 获取系统统计
     *
     * @return 系统统计
     */
    ResponseDTO<Map<String, Object>> getSystemStatistics();
}
