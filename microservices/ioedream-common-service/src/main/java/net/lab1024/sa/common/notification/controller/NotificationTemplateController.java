package net.lab1024.sa.common.notification.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.notification.domain.entity.NotificationTemplateEntity;
import net.lab1024.sa.common.notification.domain.vo.NotificationTemplateVO;
import net.lab1024.sa.common.notification.service.NotificationTemplateService;

/**
 * 通知模板控制器
 * <p>
 * 提供通知模板管理的REST API
 * 严格遵循CLAUDE.md规范:
 * - 使用@RestController注解
 * - 使用@Resource依赖注入
 * - 使用@Valid参数验证
 * - 返回统一ResponseDTO格式
 * - 完整的Swagger文档注解
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/notification/template")
@Tag(name = "通知模板管理", description = "通知模板管理接口")
public class NotificationTemplateController {

    @Resource
    private NotificationTemplateService notificationTemplateService;

    /**
     * 根据模板编码获取模板
     *
     * @param templateCode 模板编码
     * @return 通知模板实体
     */
    @GetMapping("/code/{templateCode}")
    @Operation(summary = "获取模板", description = "根据模板编码获取模板信息")
    public ResponseDTO<NotificationTemplateEntity> getTemplateByCode(@PathVariable String templateCode) {
        log.info("[通知模板] 获取模板，templateCode：{}", templateCode);
        NotificationTemplateEntity template = notificationTemplateService.getTemplateByCode(templateCode);
        return ResponseDTO.ok(template);
    }

    /**
     * 根据模板类型获取模板列表
     *
     * @param templateType 模板类型
     * @return 模板列表
     */
    @GetMapping("/type/{templateType}")
    @Operation(summary = "获取模板列表（按类型）", description = "根据模板类型获取模板列表")
    public ResponseDTO<List<NotificationTemplateVO>> getTemplatesByType(@PathVariable Integer templateType) {
        log.info("[通知模板] 获取模板列表（按类型），templateType：{}", templateType);
        List<NotificationTemplateVO> templates = notificationTemplateService.getTemplatesByType(templateType);
        return ResponseDTO.ok(templates);
    }

    /**
     * 根据模板类型和状态获取模板列表
     *
     * @param templateType 模板类型
     * @param status       状态（1-启用 2-禁用）
     * @return 模板列表
     */
    @GetMapping("/type/{templateType}/status/{status}")
    @Operation(summary = "获取模板列表（按类型和状态）", description = "根据模板类型和状态获取模板列表")
    public ResponseDTO<List<NotificationTemplateVO>> getTemplatesByTypeAndStatus(
            @PathVariable Integer templateType,
            @PathVariable Integer status) {
        log.info("[通知模板] 获取模板列表（按类型和状态），templateType：{}，status：{}", templateType, status);
        List<NotificationTemplateVO> templates = notificationTemplateService.getTemplatesByTypeAndStatus(templateType, status);
        return ResponseDTO.ok(templates);
    }

    /**
     * 渲染模板
     * <p>
     * 使用变量替换模板中的占位符
     * </p>
     *
     * @param templateCode 模板编码
     * @param request      渲染请求（包含variables变量Map）
     * @return 渲染后的内容
     */
    @PostMapping("/render/{templateCode}")
    @Operation(summary = "渲染模板", description = "使用变量替换模板中的占位符，返回渲染后的内容")
    public ResponseDTO<String> renderTemplate(
            @PathVariable String templateCode,
            @Valid @RequestBody Map<String, Object> request) {
        log.info("[通知模板] 渲染模板，templateCode：{}", templateCode);
        @SuppressWarnings("unchecked")
        Map<String, Object> variables = (Map<String, Object>) request.get("variables");
        String renderedContent = notificationTemplateService.renderTemplate(templateCode, variables);
        return ResponseDTO.ok(renderedContent);
    }

    /**
     * 渲染模板主题
     *
     * @param templateCode 模板编码
     * @param request      渲染请求（包含variables变量Map）
     * @return 渲染后的主题
     */
    @PostMapping("/render-subject/{templateCode}")
    @Operation(summary = "渲染模板主题", description = "渲染邮件主题（如果模板有主题）")
    public ResponseDTO<String> renderSubject(
            @PathVariable String templateCode,
            @Valid @RequestBody Map<String, Object> request) {
        log.info("[通知模板] 渲染模板主题，templateCode：{}", templateCode);
        @SuppressWarnings("unchecked")
        Map<String, Object> variables = (Map<String, Object>) request.get("variables");
        String renderedSubject = notificationTemplateService.renderSubject(templateCode, variables);
        return ResponseDTO.ok(renderedSubject);
    }

    /**
     * 获取模板变量列表
     *
     * @param templateCode 模板编码
     * @return 变量列表
     */
    @GetMapping("/variables/{templateCode}")
    @Operation(summary = "获取模板变量列表", description = "获取模板中定义的变量列表")
    public ResponseDTO<List<String>> getTemplateVariables(@PathVariable String templateCode) {
        log.info("[通知模板] 获取模板变量列表，templateCode：{}", templateCode);
        List<String> variables = notificationTemplateService.getTemplateVariables(templateCode);
        return ResponseDTO.ok(variables);
    }

    /**
     * 清除模板缓存
     *
     * @param templateCode 模板编码
     * @return 操作结果
     */
    @PutMapping("/cache/evict/{templateCode}")
    @Operation(summary = "清除模板缓存", description = "清除指定模板的缓存，用于模板热更新")
    public ResponseDTO<Void> evictCache(@PathVariable String templateCode) {
        log.info("[通知模板] 清除模板缓存，templateCode：{}", templateCode);
        notificationTemplateService.evictCache(templateCode);
        return ResponseDTO.ok();
    }
}
