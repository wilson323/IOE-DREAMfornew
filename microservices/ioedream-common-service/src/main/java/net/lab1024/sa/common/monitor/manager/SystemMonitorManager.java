package net.lab1024.sa.common.monitor.manager;

import java.util.HashMap;
import java.util.Map;

import net.lab1024.sa.common.monitor.dao.SystemMonitorDao;
import net.lab1024.sa.common.monitor.domain.vo.ResourceUsageVO;

public class SystemMonitorManager {

    private final SystemMonitorDao systemMonitorDao;

    public SystemMonitorManager(SystemMonitorDao dao) {
        this.systemMonitorDao = dao;
    }

    public ResourceUsageVO getResourceUsage() {
        return new ResourceUsageVO();
    }

    public Map<String, Object> getSystemMetrics() {
        return new HashMap<>();
    }
}
