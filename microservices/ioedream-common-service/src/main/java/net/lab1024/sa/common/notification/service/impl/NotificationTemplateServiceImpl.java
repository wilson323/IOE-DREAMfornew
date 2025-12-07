package net.lab1024.sa.common.notification.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.notification.domain.entity.NotificationTemplateEntity;
import net.lab1024.sa.common.notification.domain.vo.NotificationTemplateVO;
import net.lab1024.sa.common.notification.manager.NotificationTemplateManager;
import net.lab1024.sa.common.notification.service.NotificationTemplateService;

/**
 * 通知模板服务实现类
 * <p>
 * 实现通知模板的CRUD操作和渲染功能
 * 严格遵循CLAUDE.md规范:
 * - 使用@Service注解标识
 * - 使用@Resource依赖注入
 * - 事务管理(@Transactional)
 * - 调用Manager层进行业务处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class NotificationTemplateServiceImpl implements NotificationTemplateService {

    @Resource
    private NotificationTemplateManager notificationTemplateManager;

    /**
     * 根据模板编码获取模板
     *
     * @param templateCode 模板编码
     * @return 通知模板实体
     */
    @Override
    @Transactional(readOnly = true)
    public NotificationTemplateEntity getTemplateByCode(String templateCode) {
        log.debug("[通知模板] 获取模板，templateCode：{}", templateCode);
        return notificationTemplateManager.getTemplateByCode(templateCode);
    }

    /**
     * 根据模板类型获取模板列表
     *
     * @param templateType 模板类型
     * @return 模板列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<NotificationTemplateVO> getTemplatesByType(Integer templateType) {
        log.debug("[通知模板] 获取模板列表（按类型），templateType：{}", templateType);
        List<NotificationTemplateEntity> templates = notificationTemplateManager.getTemplatesByType(templateType);
        return convertToVOList(templates);
    }

    /**
     * 根据模板类型和状态获取模板列表
     *
     * @param templateType 模板类型
     * @param status       状态（1-启用 2-禁用）
     * @return 模板列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<NotificationTemplateVO> getTemplatesByTypeAndStatus(Integer templateType, Integer status) {
        log.debug("[通知模板] 获取模板列表（按类型和状态），templateType：{}，status：{}", templateType, status);
        List<NotificationTemplateEntity> templates = notificationTemplateManager.getTemplatesByTypeAndStatus(templateType, status);
        return convertToVOList(templates);
    }

    /**
     * 渲染模板
     * <p>
     * 使用变量替换模板中的占位符
     * </p>
     *
     * @param templateCode 模板编码
     * @param variables     变量Map
     * @return 渲染后的内容
     */
    @Override
    @Transactional(readOnly = true)
    public String renderTemplate(String templateCode, Map<String, Object> variables) {
        log.debug("[通知模板] 渲染模板，templateCode：{}", templateCode);
        return notificationTemplateManager.renderTemplate(templateCode, variables);
    }

    /**
     * 渲染模板主题
     * <p>
     * 渲染邮件主题（如果模板有主题）
     * </p>
     *
     * @param templateCode 模板编码
     * @param variables    变量Map
     * @return 渲染后的主题
     */
    @Override
    @Transactional(readOnly = true)
    public String renderSubject(String templateCode, Map<String, Object> variables) {
        log.debug("[通知模板] 渲染模板主题，templateCode：{}", templateCode);
        return notificationTemplateManager.renderSubject(templateCode, variables);
    }

    /**
     * 获取模板变量列表
     *
     * @param templateCode 模板编码
     * @return 变量列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<String> getTemplateVariables(String templateCode) {
        log.debug("[通知模板] 获取模板变量列表，templateCode：{}", templateCode);
        return notificationTemplateManager.getTemplateVariables(templateCode);
    }

    /**
     * 清除模板缓存
     *
     * @param templateCode 模板编码
     */
    @Override
    public void evictCache(String templateCode) {
        log.info("[通知模板] 清除模板缓存，templateCode：{}", templateCode);
        notificationTemplateManager.evictCache(templateCode);
    }

    /**
     * 转换为VO列表
     *
     * @param templates 模板实体列表
     * @return VO列表
     */
    private List<NotificationTemplateVO> convertToVOList(List<NotificationTemplateEntity> templates) {
        return templates.stream().map(template -> {
            NotificationTemplateVO vo = new NotificationTemplateVO();
            vo.setTemplateId(template.getTemplateId());
            vo.setTemplateCode(template.getTemplateCode());
            vo.setTemplateName(template.getTemplateName());
            vo.setTemplateType(template.getTemplateType());
            vo.setSubject(template.getSubject());
            vo.setContent(template.getContent());
            vo.setVariables(template.getVariables());
            vo.setStatus(template.getStatus());
            vo.setRemark(template.getRemark());
            return vo;
        }).collect(Collectors.toList());
    }
}
