package net.lab1024.sa.oa.workflow.config.wrapper;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.ManagementService;
import org.flowable.job.api.Job;
import org.flowable.job.api.TimerJobQuery;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Flowable管理服务包装器
 *
 * @author IOE-DREAM Team
 * @version 2.0.0 - Flowable 7.2.0兼容版
 * @since 2025-01-17
 */
@Slf4j
@Service
public class FlowableManagementService {

    private final ManagementService managementService;

    public FlowableManagementService(ManagementService managementService) {
        this.managementService = managementService;
        log.info("[Flowable] ManagementService包装器初始化完成");
    }

    public Map<String, Object> getEngineInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("status", "running");
        info.put("timestamp", System.currentTimeMillis());
        return info;
    }

    public List<Job> getDeadLetterJobs() {
        return managementService.createDeadLetterJobQuery().list();
    }

    public TimerJobQuery createTimerJobQuery() {
        return managementService.createTimerJobQuery();
    }

    public void deleteJob(String jobId) {
        log.info("[Flowable] 删除作业: jobId={}", jobId);
        managementService.deleteJob(jobId);
    }

    public ManagementService getNativeService() {
        return managementService;
    }
}
