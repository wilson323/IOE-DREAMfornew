package net.lab1024.sa.oa.workflow.config.wrapper;

import lombok.extern.slf4j.Slf4j;
import org.flowable.dmn.api.DmnRepositoryService;
import org.flowable.dmn.api.DmnDecision;
import org.flowable.dmn.api.DmnDecisionQuery;
import org.flowable.dmn.api.DmnDeployment;
import org.flowable.dmn.api.DmnDeploymentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * DMN决策表定义服务包装器
 * <p>
 * 封装Flowable DMN定义服务，提供决策表部署和管理功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
public class FlowableDmnRepositoryService {

    private static final Logger log = LoggerFactory.getLogger(FlowableDmnRepositoryService.class);
    private final DmnRepositoryService dmnRepositoryService;

    public FlowableDmnRepositoryService(DmnRepositoryService dmnRepositoryService) {
        this.dmnRepositoryService = dmnRepositoryService;
    }

    /**
     * 创建部署构建器
     *
     * @return 部署构建器
     */
    public DmnDeploymentBuilder createDeployment() {
        return dmnRepositoryService.createDeployment();
    }

    /**
     * 部署决策表定义
     *
     * @param resourceName 资源名称
     * @param inputStream 输入流
     * @return 部署结果
     */
    public DmnDeployment deploy(String resourceName, InputStream inputStream) {
        log.info("[DMN服务] 部署决策表定义: resourceName={}", resourceName);
        return dmnRepositoryService.createDeployment()
                .addInputStream(resourceName, inputStream)
                .deploy();
    }

    /**
     * 创建决策表查询
     *
     * @return 决策表查询
     */
    public DmnDecisionQuery createDecisionQuery() {
        return dmnRepositoryService.createDecisionQuery();
    }

    /**
     * 获取决策表定义
     *
     * @param decisionId 决策表ID
     * @return 决策表定义
     */
    public DmnDecision getDecision(String decisionId) {
        return dmnRepositoryService.getDecision(decisionId);
    }

    /**
     * 删除部署
     *
     * @param deploymentId 部署ID
     */
    public void deleteDeployment(String deploymentId) {
        log.info("[DMN服务] 删除部署: deploymentId={}", deploymentId);
        dmnRepositoryService.deleteDeployment(deploymentId);
    }

    /**
     * 获取原始服务
     *
     * @return 原始DMN定义服务
     */
    public DmnRepositoryService getNativeService() {
        return dmnRepositoryService;
    }
}
