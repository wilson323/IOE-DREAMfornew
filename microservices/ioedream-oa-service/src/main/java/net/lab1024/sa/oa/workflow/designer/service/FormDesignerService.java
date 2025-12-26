package net.lab1024.sa.oa.workflow.designer.service;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.lab1024.sa.oa.workflow.designer.domain.FormDefinitionDTO;
import net.lab1024.sa.oa.workflow.form.FormSchemaDao;
import net.lab1024.sa.oa.workflow.form.FormSchemaEntity;

/**
 * 表单设计器服务
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-17
 */
@Service
@Slf4j
 public class FormDesignerService {

    @Resource
    private FormSchemaDao formSchemaDao;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 保存表单定义
     */
    @Transactional(rollbackFor = Exception.class)
    public Long saveFormDefinition (FormDefinitionDTO dto) {
        // 验证Schema格式
        Map<String, Object> validation = validateSchema (dto.getFormSchema ());
        if (!(Boolean) validation.get ("valid")) {
            throw new RuntimeException ("表单Schema格式无效: " + validation.get ("errors"));
        }

        // 获取最新版本号
        int latestVersion = formSchemaDao.findLatestVersionByKey (dto.getFormKey ());
        int newVersion = latestVersion + 1;

        // 创建新版本
        FormSchemaEntity entity = new FormSchemaEntity ();
        entity.setFormKey (dto.getFormKey ());
        entity.setFormName (dto.getFormName ());
        entity.setFormSchema (dto.getFormSchema ());
        entity.setVersion (newVersion);
        entity.setStatus (dto.getStatus () != null ? dto.getStatus () : 1);
        entity.setCategory (dto.getCategory ());
        entity.setDescription (dto.getDescription ());
        entity.setCreateTime (LocalDateTime.now ());
        entity.setUpdateTime (LocalDateTime.now ());

        formSchemaDao.insert (entity);
        log.info ("[表单设计器] 保存表单定义成功: key={}, version={}", dto.getFormKey (), newVersion);
        return entity.getId ();
    }

    /**
     * 获取表单定义
     */
    public FormDefinitionDTO getFormDefinition (String formKey) {
        FormSchemaEntity entity = formSchemaDao.findLatestByKey (formKey);
        if (entity == null) {
            return null;
        }
        return convertToDTO (entity);
    }

    /**
     * 获取表单定义列表
     */
    public List<FormDefinitionDTO> listFormDefinitions (String category) {
        List<FormSchemaEntity> entities;
        if (category != null && !category.isEmpty ()) {
            entities = formSchemaDao.findByCategory (category);
        } else {
            entities = formSchemaDao.findLatestVersions ();
        }
        return entities.stream ().map (this::convertToDTO).collect (Collectors.toList ());
    }

    /**
     * 删除表单定义
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFormDefinition (String formKey) {
        FormSchemaEntity entity = formSchemaDao.findLatestByKey (formKey);
        if (entity == null) {
            return false;
        }
        entity.setDeletedFlag (1);
        entity.setUpdateTime (LocalDateTime.now ());
        formSchemaDao.updateById (entity);
        log.info ("[表单设计器] 删除表单定义: key={}", formKey);
        return true;
    }

    /**
     * 预览表单
     */
    public Map<String, Object> previewForm (Map<String, Object> formSchema) {
        Map<String, Object> previewData = new HashMap<> ();
        previewData.put ("schema", formSchema);
        previewData.put ("renderMode", "preview");
        previewData.put ("timestamp", System.currentTimeMillis ());
        return previewData;
    }

    /**
     * 验证表单Schema
     */
    public Map<String, Object> validateSchema (String schemaJson) {
        Map<String, Object> result = new HashMap<> ();
        List<String> errors = new ArrayList<> ();

        try {
            JsonNode schemaNode = objectMapper.readTree (schemaJson);

            // 必须是object类型
            if (!schemaNode.has ("type") || !"object".equals (schemaNode.get ("type").asText ())) {
                errors.add ("Schema根节点type必须为object");
            }

            // 必须有properties
            if (!schemaNode.has ("properties")) {
                errors.add ("Schema必须包含properties属性");
            }

            result.put ("valid", errors.isEmpty ());
            result.put ("errors", errors);

        } catch (Exception e) {
            result.put ("valid", false);
            errors.add ("JSON解析失败: " + e.getMessage ());
            result.put ("errors", errors);
        }

        return result;
    }

    private FormDefinitionDTO convertToDTO (FormSchemaEntity entity) {
        FormDefinitionDTO dto = new FormDefinitionDTO ();
        dto.setId (entity.getId ());
        dto.setFormKey (entity.getFormKey ());
        dto.setFormName (entity.getFormName ());
        dto.setFormSchema (entity.getFormSchema ());
        dto.setVersion (entity.getVersion ());
        dto.setStatus (entity.getStatus ());
        dto.setCategory (entity.getCategory ());
        dto.setDescription (entity.getDescription ());
        dto.setCreateTime (entity.getCreateTime ());
        dto.setUpdateTime (entity.getUpdateTime ());
        return dto;
    }
}



