package net.lab1024.sa.oa.workflow.config.wrapper;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.List;

import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.springframework.stereotype.Service;

/**
 * Flowable流程定义服务包装器
 * <p>
 * 提供流程定义管理的完整功能封装 包括流程部署、版本管理、查询统计等功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
@Service
 public class FlowableRepositoryService {

    private final RepositoryService repositoryService;

    public FlowableRepositoryService (RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
        log.info ("[Flowable] RepositoryService包装器初始化完成");
    }

    /**
     * 部署流程定义
     *
     * @param resourceName
     *            资源名称
     * @param inputStream
     *            流程定义输入流
     * @return 部署ID
     */
    public String deployProcessDefinition (String resourceName, InputStream inputStream) {
        log.info ("[Flowable] 部署流程定义: resourceName={}", resourceName);

        try {
            Deployment deployment = repositoryService.createDeployment ().addInputStream (resourceName, inputStream)
                    .deploy ();

            log.info ("[Flowable] 流程部署成功: deploymentId={}, name={}", deployment.getId (), deployment.getName ());
            return deployment.getId ();

        } catch (Exception e) {
            log.error ("[Flowable] 流程部署失败: resourceName={}, error={}", resourceName, e.getMessage (), e);
            throw new RuntimeException ("流程部署失败: " + e.getMessage (), e);
        }
    }

    /**
     * 部署流程定义（带资源名称）
     *
     * @param resourceName
     *            资源名称
     * @param resourceText
     *            流程定义文本内容
     * @return 部署ID
     */
    public String deployProcessDefinitionText (String resourceName, String resourceText) {
        log.info ("[Flowable] 部署流程定义: resourceName={}", resourceName);

        try {
            Deployment deployment = repositoryService.createDeployment ().addString (resourceName, resourceText)
                    .deploy ();

            log.info ("[Flowable] 流程部署成功: deploymentId={}, name={}", deployment.getId (), deployment.getName ());
            return deployment.getId ();

        } catch (Exception e) {
            log.error ("[Flowable] 流程部署失败: resourceName={}, error={}", resourceName, e.getMessage (), e);
            throw new RuntimeException ("流程部署失败: " + e.getMessage (), e);
        }
    }

    /**
     * 完整部署流程定义（企业级方法）
     *
     * @param processKey
     *            流程Key
     * @param processName
     *            流程名称
     * @param category
     *            分类
     * @param bpmnXml
     *            BPMN XML内容
     * @param description
     *            描述
     * @return Deployment对象
     */
    public Deployment deployProcessDefinition (String processKey, String processName, String category, String bpmnXml,
            String description) {
        log.info ("[Flowable] 完整部署流程定义: processKey={}, processName={}", processKey, processName);

        try {
            String resourceName = processKey + ".bpmn20.xml";
            Deployment deployment = repositoryService.createDeployment ().name (processName).category (category)
                    .addString (resourceName, bpmnXml).deploy ();

            log.info ("[Flowable] 流程部署成功: deploymentId={}, name={}", deployment.getId (), deployment.getName ());
            return deployment;

        } catch (Exception e) {
            log.error ("[Flowable] 流程部署失败: processKey={}, error={}", processKey, e.getMessage (), e);
            throw new RuntimeException ("流程部署失败: " + e.getMessage (), e);
        }
    }

    /**
     * 获取最新流程定义
     *
     * @param processKey
     *            流程Key
     * @return 流程定义
     */
    public ProcessDefinition getLatestProcessDefinition (String processKey) {
        log.debug ("[Flowable] 获取最新流程定义: processKey={}", processKey);
        return getProcessDefinition (processKey, true);
    }

    /**
     * 获取流程定义
     *
     * @param processDefinitionKey
     *            流程定义键
     * @param latestVersion
     *            是否最新版本
     * @return 流程定义
     */
    public ProcessDefinition getProcessDefinition (String processDefinitionKey, boolean latestVersion) {
        log.debug ("[Flowable] 获取流程定义: processDefinitionKey={}, latestVersion={}", processDefinitionKey, latestVersion);

        try {
            ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery ()
                    .processDefinitionKey (processDefinitionKey);

            if (latestVersion) {
                query = query.latestVersion ();
            }

            return query.singleResult ();

        } catch (Exception e) {
            log.error ("[Flowable] 获取流程定义失败: processDefinitionKey={}, error={}", processDefinitionKey, e.getMessage (),
                    e);
            return null;
        }
    }

    /**
     * 获取流程定义（通过ID）
     *
     * @param processDefinitionId
     *            流程定义ID
     * @return 流程定义
     */
    public ProcessDefinition getProcessDefinitionById (String processDefinitionId) {
        log.debug ("[Flowable] 通过ID获取流程定义: processDefinitionId={}", processDefinitionId);

        try {
            return repositoryService.createProcessDefinitionQuery ().processDefinitionId (processDefinitionId)
                    .singleResult ();

        } catch (Exception e) {
            log.error ("[Flowable] 获取流程定义失败: processDefinitionId={}, error={}", processDefinitionId, e.getMessage (),
                    e);
            return null;
        }
    }

    /**
     * 获取所有流程定义
     *
     * @return 流程定义列表
     */
    public List<ProcessDefinition> getAllProcessDefinitions () {
        log.debug ("[Flowable] 获取所有流程定义");

        try {
            return repositoryService.createProcessDefinitionQuery ().latestVersion ().active ()
                    .orderByProcessDefinitionName ().asc ().list ();

        } catch (Exception e) {
            log.error ("[Flowable] 获取流程定义列表失败: error={}", e.getMessage (), e);
            return List.of ();
        }
    }

    /**
     * 分页查询流程定义
     *
     * @param pageNum
     *            页码
     * @param pageSize
     *            页大小
     * @param key
     *            流程键（可选）
     * @param name
     *            流程名称（可选）
     * @return 流程定义列表
     */
    public List<ProcessDefinition> getProcessDefinitionsPage (int pageNum, int pageSize, String key, String name) {
        log.debug ("[Flowable] 分页查询流程定义: pageNum={}, pageSize={}, key={}, name={}", pageNum, pageSize, key, name);

        try {
            ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery ().latestVersion ().active ();

            if (key != null && !key.trim ().isEmpty ()) {
                query = query.processDefinitionKeyLike ("%" + key + "%");
            }

            if (name != null && !name.trim ().isEmpty ()) {
                query = query.processDefinitionNameLike ("%" + name + "%");
            }

            return query.orderByProcessDefinitionName ().asc ().listPage (pageNum * pageSize, pageSize);

        } catch (Exception e) {
            log.error ("[Flowable] 分页查询流程定义失败: error={}", e.getMessage (), e);
            return List.of ();
        }
    }

    /**
     * 获取流程定义总数
     *
     * @param key
     *            流程键（可选）
     * @param name
     *            流程名称（可选）
     * @return 总数
     */
    public long getProcessDefinitionCount (String key, String name) {
        log.debug ("[Flowable] 获取流程定义总数: key={}, name={}", key, name);

        try {
            ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery ().latestVersion ().active ();

            if (key != null && !key.trim ().isEmpty ()) {
                query = query.processDefinitionKeyLike ("%" + key + "%");
            }

            if (name != null && !name.trim ().isEmpty ()) {
                query = query.processDefinitionNameLike ("%" + name + "%");
            }

            return query.count ();

        } catch (Exception e) {
            log.error ("[Flowable] 获取流程定义总数失败: error={}", e.getMessage (), e);
            return 0L;
        }
    }

    /**
     * 挂起流程定义
     *
     * @param processDefinitionId
     *            流程定义ID
     */
    public void suspendProcessDefinition (String processDefinitionId) {
        log.info ("[Flowable] 挂起流程定义: processDefinitionId={}", processDefinitionId);

        try {
            repositoryService.suspendProcessDefinitionById (processDefinitionId);
            log.info ("[Flowable] 流程定义挂起成功: processDefinitionId={}", processDefinitionId);

        } catch (Exception e) {
            log.error ("[Flowable] 挂起流程定义失败: processDefinitionId={}, error={}", processDefinitionId, e.getMessage (),
                    e);
            throw new RuntimeException ("挂起流程定义失败: " + e.getMessage (), e);
        }
    }

    /**
     * 激活流程定义
     *
     * @param processDefinitionId
     *            流程定义ID
     */
    public void activateProcessDefinition (String processDefinitionId) {
        log.info ("[Flowable] 激活流程定义: processDefinitionId={}", processDefinitionId);

        try {
            repositoryService.activateProcessDefinitionById (processDefinitionId);
            log.info ("[Flowable] 流程定义激活成功: processDefinitionId={}", processDefinitionId);

        } catch (Exception e) {
            log.error ("[Flowable] 激活流程定义失败: processDefinitionId={}, error={}", processDefinitionId, e.getMessage (),
                    e);
            throw new RuntimeException ("激活流程定义失败: " + e.getMessage (), e);
        }
    }

    /**
     * 删除流程部署
     *
     * @param deploymentId
     *            部署ID
     * @param cascade
     *            是否级联删除
     */
    public void deleteDeployment (String deploymentId, boolean cascade) {
        log.info ("[Flowable] 删除流程部署: deploymentId={}, cascade={}", deploymentId, cascade);

        try {
            repositoryService.deleteDeployment (deploymentId, cascade);
            log.info ("[Flowable] 流程部署删除成功: deploymentId={}", deploymentId);

        } catch (Exception e) {
            log.error ("[Flowable] 删除流程部署失败: deploymentId={}, error={}", deploymentId, e.getMessage (), e);
            throw new RuntimeException ("删除流程部署失败: " + e.getMessage (), e);
        }
    }

    /**
     * 获取流程定义的BPMN模型
     *
     * @param processDefinitionId
     *            流程定义ID
     * @return BPMN模型字符串
     */
    public String getBpmnModel (String processDefinitionId) {
        log.debug ("[Flowable] 获取BPMN模型: processDefinitionId={}", processDefinitionId);

        try {
            InputStream inputStream = repositoryService.getProcessModel (processDefinitionId);
            return new String (inputStream.readAllBytes (), java.nio.charset.StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error ("[Flowable] 获取BPMN模型失败: processDefinitionId={}, error={}", processDefinitionId, e.getMessage (),
                    e);
            return null;
        }
    }

    /**
     * 获取流程定义的流程图
     *
     * @param processDefinitionId
     *            流程定义ID
     * @return 流程图输入流
     */
    public InputStream getProcessDiagram (String processDefinitionId) {
        log.debug ("[Flowable] 获取流程图: processDefinitionId={}", processDefinitionId);

        try {
            return repositoryService.getProcessDiagram (processDefinitionId);

        } catch (Exception e) {
            log.error ("[Flowable] 获取流程图失败: processDefinitionId={}, error={}", processDefinitionId, e.getMessage (), e);
            return null;
        }
    }
}


