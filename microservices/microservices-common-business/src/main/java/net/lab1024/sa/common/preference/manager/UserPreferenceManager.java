package net.lab1024.sa.common.preference.manager;

import net.lab1024.sa.common.preference.dao.UserPreferenceDao;
import net.lab1024.sa.common.preference.entity.UserPreferenceEntity;
import lombok.extern.slf4j.Slf4j;


import java.util.List;

/**
 * 用户偏好设置管理器
 * <p>
 * 职责：
 * - 用户偏好设置的CRUD操作
 * - 偏好设置的验证和默认值处理
 * - 偏好设置的缓存管理
 * </p>
 * <p>
 * 注意：Manager类是纯Java类，不使用Spring注解，通过构造函数注入依赖。
 * 在微服务中通过配置类将Manager注册为Spring Bean。
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
@Slf4j
public class UserPreferenceManager {


    private final UserPreferenceDao userPreferenceDao;

    public UserPreferenceManager(UserPreferenceDao userPreferenceDao) {
        this.userPreferenceDao = userPreferenceDao;
        log.info("[UserPreferenceManager] 初始化用户偏好管理器");
    }

    /**
     * 获取用户偏好设置
     *
     * @param userId 用户ID
     * @return 偏好设置列表
     */
    public List<UserPreferenceEntity> getUserPreferences(Long userId) {
        return userPreferenceDao.selectByUserId(userId);
    }

    /**
     * 获取用户指定类别的偏好设置
     *
     * @param userId 用户ID
     * @param category 类别
     * @return 偏好设置列表
     */
    public List<UserPreferenceEntity> getUserPreferencesByCategory(Long userId, String category) {
        return userPreferenceDao.selectByUserIdAndCategory(userId, category);
    }

    /**
     * 获取用户指定键的偏好设置
     *
     * @param userId 用户ID
     * @param preferenceKey 偏好键
     * @return 偏好设置实体
     */
    public UserPreferenceEntity getUserPreference(Long userId, String preferenceKey) {
        return userPreferenceDao.selectByUserIdAndKey(userId, preferenceKey);
    }

    /**
     * 保存用户偏好设置
     *
     * @param preference 偏好设置实体
     * @return 是否成功
     */
    public boolean saveUserPreference(UserPreferenceEntity preference) {
        int result = userPreferenceDao.insert(preference);
        return result > 0;
    }

    /**
     * 更新用户偏好设置
     *
     * @param preference 偏好设置实体
     * @return 是否成功
     */
    public boolean updateUserPreference(UserPreferenceEntity preference) {
        int result = userPreferenceDao.updateById(preference);
        return result > 0;
    }

    /**
     * 删除用户偏好设置
     *
     * @param preferenceId 偏好设置ID
     * @return 是否成功
     */
    public boolean deleteUserPreference(Long preferenceId) {
        int result = userPreferenceDao.deleteById(preferenceId);
        return result > 0;
    }
}
