package net.lab1024.sa.common.notification.service;

import java.util.List;
import java.util.Map;

import net.lab1024.sa.common.notification.domain.entity.NotificationTemplateEntity;
import net.lab1024.sa.common.notification.domain.vo.NotificationTemplateVO;

/**
 * 通知模板服务接口
 * <p>
 * 提供通知模板的CRUD操作和渲染功能
 * 严格遵循CLAUDE.md规范:
 * - Service接口定义业务方法
 * - 使用@Valid进行参数验证
 * - 返回统一ResponseDTO格式
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface NotificationTemplateService {

    /**
     * 根据模板编码获取模板
     *
     * @param templateCode 模板编码
     * @return 通知模板实体
     */
    NotificationTemplateEntity getTemplateByCode(String templateCode);

    /**
     * 根据模板类型获取模板列表
     *
     * @param templateType 模板类型
     * @return 模板列表
     */
    List<NotificationTemplateVO> getTemplatesByType(Integer templateType);

    /**
     * 根据模板类型和状态获取模板列表
     *
     * @param templateType 模板类型
     * @param status       状态（1-启用 2-禁用）
     * @return 模板列表
     */
    List<NotificationTemplateVO> getTemplatesByTypeAndStatus(Integer templateType, Integer status);

    /**
     * 渲染模板
     * <p>
     * 使用变量替换模板中的占位符
     * </p>
     *
     * @param templateCode 模板编码
     * @param variables     变量Map（key为变量名，value为变量值）
     * @return 渲染后的内容
     */
    String renderTemplate(String templateCode, Map<String, Object> variables);

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
    String renderSubject(String templateCode, Map<String, Object> variables);

    /**
     * 获取模板变量列表
     *
     * @param templateCode 模板编码
     * @return 变量列表
     */
    List<String> getTemplateVariables(String templateCode);

    /**
     * 清除模板缓存
     *
     * @param templateCode 模板编码
     */
    void evictCache(String templateCode);
}
