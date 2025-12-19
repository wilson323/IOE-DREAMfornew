package net.lab1024.sa.oa.workflow.designer.service;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.lab1024.sa.oa.workflow.designer.domain.ProcessDefinitionDTO;
import net.lab1024.sa.oa.workflow.designer.entity.ProcessTemplateEntity;
import net.lab1024.sa.oa.workflow.designer.dao.ProcessTemplateDao;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程设计器服务
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-17
 */
@Service
public class ProcessDesignerService {

    private static final Logger log = LoggerFactory.getLogger(ProcessDesignerService.class);

    @Resource
    private ProcessTemplateDao processTemplateDao;

    @Resource
    private RepositoryService repositoryService;

    /**
     * 保存流程模板
     */
    @Transactional(rollbackFor = Exception.class)
    public Long saveProcessTemplate(ProcessDefinitionDTO dto) {
        // 验证BPMN
        Map<String, Object> validation = validateBpmn(dto.getBpmnXml());
        if (!(Boolean) validation.get("valid")) {
            throw new RuntimeException("BPMN XML格式无效: " + validation.get("errors"));
        }

        // 获取最新版本号
        int latestVersion = processTemplateDao.findLatestVersionByKey(dto.getTemplateKey());
        int newVersion = latestVersion + 1;

        // 创建新版本
        ProcessTemplateEntity entity = new ProcessTemplateEntity();
        entity.setTemplateKey(dto.getTemplateKey());
        entity.setTemplateName(dto.getTemplateName());
        entity.setBpmnXml(dto.getBpmnXml());
        entity.setFormKey(dto.getFormKey());
        entity.setCategory(dto.getCategory());
        entity.setIcon(dto.getIcon());
        entity.setDescription(dto.getDescription());
        entity.setVersion(newVersion);
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());

        processTemplateDao.insert(entity);
        log.info("[流程设计器] 保存流程模板成功: key={}, version={}", dto.getTemplateKey(), newVersion);
        return entity.getId();
    }

    /**
     * 获取流程模板
     */
    public ProcessDefinitionDTO getProcessTemplate(String templateKey) {
        ProcessTemplateEntity entity = processTemplateDao.findLatestByKey(templateKey);
        if (entity == null) {
            return null;
        }
        return convertToDTO(entity);
    }

    /**
     * 获取流程模板列表
     */
    public List<ProcessDefinitionDTO> listProcessTemplates(String category) {
        List<ProcessTemplateEntity> entities;
        if (category != null && !category.isEmpty()) {
            entities = processTemplateDao.findByCategory(category);
        } else {
            entities = processTemplateDao.findLatestVersions();
        }
        return entities.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * 部署流程
     */
    @Transactional(rollbackFor = Exception.class)
    public String deployProcess(String templateKey) {
        ProcessTemplateEntity entity = processTemplateDao.findLatestByKey(templateKey);
        if (entity == null) {
            throw new RuntimeException("流程模板不存在: " + templateKey);
        }

        // 部署到Flowable
        InputStream bpmnStream = new ByteArrayInputStream(
                entity.getBpmnXml().getBytes(StandardCharsets.UTF_8));

        Deployment deployment = repositoryService.createDeployment()
                .name(entity.getTemplateName())
                .key(entity.getTemplateKey())
                .category(entity.getCategory())
                .addInputStream(templateKey + ".bpmn20.xml", bpmnStream)
                .deploy();

        // 获取流程定义ID
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();

        // 更新模板记录
        entity.setDeploymentId(deployment.getId());
        entity.setProcessDefinitionId(processDefinition != null ? processDefinition.getId() : null);
        entity.setUpdateTime(LocalDateTime.now());
        processTemplateDao.updateById(entity);

        log.info("[流程设计器] 部署流程成功: key={}, deploymentId={}", templateKey, deployment.getId());
        return deployment.getId();
    }

    /**
     * 验证BPMN XML
     */
    public Map<String, Object> validateBpmn(String bpmnXml) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();

        try {
            if (bpmnXml == null || bpmnXml.trim().isEmpty()) {
                errors.add("BPMN XML不能为空");
            } else if (!bpmnXml.contains("definitions") || !bpmnXml.contains("process")) {
                errors.add("BPMN XML必须包含definitions和process元素");
            }

            result.put("valid", errors.isEmpty());
            result.put("errors", errors);

        } catch (Exception e) {
            result.put("valid", false);
            errors.add("BPMN解析失败: " + e.getMessage());
            result.put("errors", errors);
        }

        return result;
    }

    /**
     * 获取流程图SVG
     */
    public String getProcessDiagram(String processDefinitionId) {
        try {
            InputStream diagramStream = repositoryService.getProcessDiagram(processDefinitionId);
            if (diagramStream != null) {
                return new String(diagramStream.readAllBytes(), StandardCharsets.UTF_8);
            }
            return null;
        } catch (Exception e) {
            log.error("[流程设计器] 获取流程图失败: {}", e.getMessage());
            return null;
        }
    }

    private ProcessDefinitionDTO convertToDTO(ProcessTemplateEntity entity) {
        ProcessDefinitionDTO dto = new ProcessDefinitionDTO();
        dto.setId(entity.getId());
        dto.setTemplateKey(entity.getTemplateKey());
        dto.setTemplateName(entity.getTemplateName());
        dto.setBpmnXml(entity.getBpmnXml());
        dto.setFormKey(entity.getFormKey());
        dto.setCategory(entity.getCategory());
        dto.setIcon(entity.getIcon());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());
        dto.setVersion(entity.getVersion());
        dto.setDeploymentId(entity.getDeploymentId());
        dto.setProcessDefinitionId(entity.getProcessDefinitionId());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateTime(entity.getUpdateTime());
        return dto;
    }
}
