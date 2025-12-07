package net.lab1024.sa.visitor.service;

import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 访客服务接口
 * <p>
 * 访客管理核心业务服务
 * 严格遵循CLAUDE.md规范：
 * - Service接口定义核心业务方法
 * - 实现类在service.impl包中
 * - 使用@Resource依赖注入
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface VisitorService {

    /**
     * 获取访客信息
     *
     * @param visitorId 访客ID
     * @return 访客信息
     */
    ResponseDTO<?> getVisitorInfo(Long visitorId);
}
