package net.lab1024.sa.common.monitor.manager;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.lab1024.sa.common.monitor.dao.SystemMonitorDao;

public class PerformanceMonitorManager {

    private final SystemMonitorDao systemMonitorDao;

    public PerformanceMonitorManager(SystemMonitorDao dao) {
        this.systemMonitorDao = dao;
    }

    public Map<String, Object> getJvmPerformanceMetrics() {
        return new HashMap<>();
    }

    public List<Map<String, Object>> getHealthTrends(Integer days) {
        return Collections.emptyList();
    }
}
