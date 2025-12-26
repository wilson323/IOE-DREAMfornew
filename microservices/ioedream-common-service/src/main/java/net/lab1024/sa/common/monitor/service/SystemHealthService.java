package net.lab1024.sa.common.monitor.service;

import net.lab1024.sa.common.monitor.domain.vo.SystemHealthVO;

/**
 * 系统健康服务接口
 *
 * @author IOE-DREAM Team
 * @since 2025-12-21
 */
public interface SystemHealthService {

    /**
     * 获取系统健康概览
     *
     * @return 系统健康状态VO
     */
    SystemHealthVO getSystemHealthOverview();
}
