package net.lab1024.sa.common.notification.service;

import java.util.List;
import java.util.Map;
import net.lab1024.sa.common.notification.domain.vo.NotificationConfigVO;

/**
 * 通知配置服务接口
 * <p>
 * 提供通知配置的CRUD操作
 * 严格遵循CLAUDE.md规范:
 * - Service接口定义业务方法
 * - 使用@Valid进行参数验证
 * - 返回统一ResponseDTO格式
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface NotificationConfigService {

    /**
     * 根据配置键获取配置值
     * <p>
     * 自动解密加密配置
     * </p>
     *
     * @param configKey 配置键
     * @return 配置值（已解密）
     */
    String getConfigValue(String configKey);

    /**
     * 根据配置键获取配置值（带默认值）
     *
     * @param configKey   配置键
     * @param defaultValue 默认值
     * @return 配置值或默认值
     */
    String getConfigValue(String configKey, String defaultValue);

    /**
     * 根据配置类型获取所有配置
     *
     * @param configType 配置类型
     * @return 配置Map（key为configKey，value为configValue）
     */
    Map<String, String> getConfigsByType(String configType);

    /**
     * 更新配置值
     *
     * @param configKey   配置键
     * @param configValue 配置值
     * @return 是否更新成功
     */
    boolean updateConfigValue(String configKey, String configValue);

    /**
     * 更新配置状态
     *
     * @param configKey 配置键
     * @param status    状态（1-启用 2-禁用）
     * @return 是否更新成功
     */
    boolean updateConfigStatus(String configKey, Integer status);

    /**
     * 获取配置列表（按类型）
     *
     * @param configType 配置类型
     * @return 配置列表
     */
    List<NotificationConfigVO> getConfigListByType(String configType);

    /**
     * 清除配置缓存
     *
     * @param configKey 配置键
     */
    void evictCache(String configKey);
}
