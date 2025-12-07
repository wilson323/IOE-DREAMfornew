package net.lab1024.sa.common.system.service;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.system.domain.dto.ConfigCreateDTO;
import net.lab1024.sa.common.system.domain.dto.ConfigUpdateDTO;
import net.lab1024.sa.common.system.domain.dto.DictCreateDTO;
import net.lab1024.sa.common.system.domain.vo.DictVO;

import java.util.List;
import java.util.Map;

/**
 * 系统服务接口
 * 整合自ioedream-system-service
 *
 * 功能职责：
 * - 系统配置管理
 * - 数据字典管理
 * - 参数配置管理
 * - 系统信息查询
 *
 * 企业级特性：
 * - 配置动态刷新
 * - 配置版本管理
 * - 配置变更审计
 * - 配置加密存储
 * - 多环境配置隔离
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02（整合自system-service）
 */
public interface SystemService {

    // ==================== 系统配置管理 ====================

    /**
     * 创建系统配置
     *
     * 企业级特性：
     * - 配置验证
     * - 配置加密
     * - 变更审计
     */
    ResponseDTO<Long> createConfig(ConfigCreateDTO dto);

    /**
     * 更新系统配置
     *
     * 企业级特性：
     * - 配置版本管理
     * - 动态刷新
     * - 变更通知
     */
    ResponseDTO<Void> updateConfig(Long configId, ConfigUpdateDTO dto);

    /**
     * 删除系统配置
     */
    ResponseDTO<Void> deleteConfig(Long configId);

    /**
     * 获取配置值
     *
     * 企业级特性：
     * - 多级缓存
     * - 自动解密
     * - 默认值支持
     */
    String getConfigValue(String configKey);

    /**
     * 刷新配置缓存
     */
    ResponseDTO<Void> refreshConfigCache();

    // ==================== 数据字典管理 ====================

    /**
     * 创建数据字典
     */
    ResponseDTO<Long> createDict(DictCreateDTO dto);

    /**
     * 获取字典列表
     */
    ResponseDTO<List<DictVO>> getDictList(String dictType);

    /**
     * 获取字典树
     */
    ResponseDTO<List<DictVO>> getDictTree(String dictType);

    /**
     * 刷新字典缓存
     */
    ResponseDTO<Void> refreshDictCache();

    // ==================== 系统信息查询 ====================

    /**
     * 获取系统信息
     *
     * 企业级特性：
     * - JVM信息
     * - 操作系统信息
     * - 应用信息
     * - 运行时信息
     */
    ResponseDTO<Map<String, Object>> getSystemInfo();

    /**
     * 获取系统统计
     */
    ResponseDTO<Map<String, Object>> getSystemStatistics();
}

