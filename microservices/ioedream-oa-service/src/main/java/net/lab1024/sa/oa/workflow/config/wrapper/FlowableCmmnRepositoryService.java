package net.lab1024.sa.oa.workflow.config.wrapper;

import lombok.extern.slf4j.Slf4j;
import org.flowable.cmmn.api.CmmnRepositoryService;
import org.flowable.cmmn.api.repository.CaseDefinition;
import org.flowable.cmmn.api.repository.CaseDefinitionQuery;
import org.flowable.cmmn.api.repository.CmmnDeployment;
import org.flowable.cmmn.api.repository.CmmnDeploymentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;

/**
 * CMMN定义服务包装器
 * <p>
 * 封装Flowable CMMN定义服务，提供案例定义部署和管理功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
public class FlowableCmmnRepositoryService {

    private static final Logger log = LoggerFactory.getLogger(FlowableCmmnRepositoryService.class);
    private final CmmnRepositoryService cmmnRepositoryService;

    public FlowableCmmnRepositoryService(CmmnRepositoryService cmmnRepositoryService) {
        this.cmmnRepositoryService = cmmnRepositoryService;
    }

    /**
     * 创建部署构建器
     *
     * @return 部署构建器
     */
    public CmmnDeploymentBuilder createDeployment() {
        return cmmnRepositoryService.createDeployment();
    }

    /**
     * 部署案例定义
     *
     * @param resourceName 资源名称
     * @param inputStream 输入流
     * @return 部署结果
     */
    public CmmnDeployment deploy(String resourceName, InputStream inputStream) {
        log.info("[CMMN服务] 部署案例定义: resourceName={}", resourceName);
        return cmmnRepositoryService.createDeployment()
                .addInputStream(resourceName, inputStream)
                .deploy();
    }

    /**
     * 创建案例定义查询
     *
     * @return 案例定义查询
     */
    public CaseDefinitionQuery createCaseDefinitionQuery() {
        return cmmnRepositoryService.createCaseDefinitionQuery();
    }

    /**
     * 获取案例定义
     *
     * @param caseDefinitionId 案例定义ID
     * @return 案例定义
     */
    public CaseDefinition getCaseDefinition(String caseDefinitionId) {
        return cmmnRepositoryService.getCaseDefinition(caseDefinitionId);
    }

    /**
     * 删除部署
     *
     * @param deploymentId 部署ID
     * @param cascade 是否级联删除
     */
    public void deleteDeployment(String deploymentId, boolean cascade) {
        log.info("[CMMN服务] 删除部署: deploymentId={}, cascade={}", deploymentId, cascade);
        cmmnRepositoryService.deleteDeployment(deploymentId, cascade);
    }

    /**
     * 获取原始服务
     *
     * @return 原始CMMN定义服务
     */
    public CmmnRepositoryService getNativeService() {
        return cmmnRepositoryService;
    }
}
