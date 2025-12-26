package net.lab1024.sa.common.organization.manager;

import net.lab1024.sa.common.organization.dao.AreaUserDao;

/**
 * 区域用户关联管理器
 * <p>
 * 负责区域人员关联的业务逻辑处理
 * 严格遵循CLAUDE.md规范：Manager类是纯Java类，通过构造函数注入依赖
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-21
 */
public class AreaUserManager {

    private final AreaUserDao areaUserDao;

    /**
     * 构造函数
     *
     * @param areaUserDao 区域用户DAO
     */
    public AreaUserManager(AreaUserDao areaUserDao) {
        this.areaUserDao = areaUserDao;
    }

    /**
     * 获取区域用户DAO
     *
     * @return 区域用户DAO
     */
    public AreaUserDao getAreaUserDao() {
        return areaUserDao;
    }

    // TODO: 待实现业务逻辑方法
}
