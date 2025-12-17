package net.lab1024.sa.oa.workflow.config.wrapper;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.flowable.engine.HistoryService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Flowable历史服务包装器
 *
 * @author IOE-DREAM Team
 * @version 2.0.0 - Flowable 7.2.0兼容版
 * @since 2025-01-17
 */
@Slf4j
@Service
public class FlowableHistoryService {

    private static final Logger log = LoggerFactory.getLogger(FlowableHistoryService.class);
    private final HistoryService historyService;

    public FlowableHistoryService(HistoryService historyService) {
        this.historyService = historyService;
        log.info("[Flowable] HistoryService包装器初始化完成");
    }

    public HistoricProcessInstance getHistoricProcessInstance(String processInstanceId) {
        log.debug("[Flowable] 获取历史流程实例: processInstanceId={}", processInstanceId);
        return historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
    }

    public List<HistoricProcessInstance> getHistoricProcessInstancesByBusinessKey(String businessKey) {
        return historyService.createHistoricProcessInstanceQuery()
                .processInstanceBusinessKey(businessKey)
                .orderByProcessInstanceStartTime().desc()
                .list();
    }

    public List<HistoricTaskInstance> getHistoricTasksByProcessInstance(String processInstanceId) {
        return historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByTaskCreateTime().desc()
                .list();
    }

    public List<HistoricActivityInstance> getHistoricActivityInstances(String processInstanceId) {
        return historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByActivityId().asc()
                .list();
    }

    public List<HistoricVariableInstance> getHistoricVariableInstances(String processInstanceId) {
        return historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByVariableName().asc()
                .list();
    }

    public Map<String, Object> createHistoricTaskReport(String processInstanceId) {
        Map<String, Object> report = new HashMap<>();
        HistoricProcessInstance historicInstance = getHistoricProcessInstance(processInstanceId);
        if (historicInstance != null) {
            report.put("processInstanceId", historicInstance.getId());
            report.put("startTime", historicInstance.getStartTime());
            report.put("endTime", historicInstance.getEndTime());
        }
        List<HistoricTaskInstance> tasks = getHistoricTasksByProcessInstance(processInstanceId);
        report.put("tasks", tasks);
        report.put("taskCount", tasks.size());
        return report;
    }

    public HistoryService getNativeService() {
        return historyService;
    }
}
