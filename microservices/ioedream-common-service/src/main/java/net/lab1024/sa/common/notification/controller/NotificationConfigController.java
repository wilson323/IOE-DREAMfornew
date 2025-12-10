package net.lab1024.sa.common.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.notification.domain.form.NotificationConfigAddForm;
import net.lab1024.sa.common.notification.domain.form.NotificationConfigQueryForm;
import net.lab1024.sa.common.notification.domain.form.NotificationConfigUpdateForm;
import net.lab1024.sa.common.notification.domain.vo.NotificationConfigVO;
import net.lab1024.sa.common.notification.service.NotificationConfigService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 通知配置管理控制器
 * <p>
 * 提供通知配置的CRUD操作API接口
 * 严格遵循CLAUDE.md规范:
 * - 使用@RestController注解标识控制器
 * - 使用@Resource依赖注入（禁止@Autowired）
 * - 使用@Valid进行参数验证
 * - 完整的Swagger文档注解
 * - RESTful API设计规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/notification/config")
@Tag(name = "通知配置管理", description = "通知配置管理相关接口，支持管理员通过界面配置通知渠道启用状态")
@Validated
public class NotificationConfigController {

    @Resource
    private NotificationConfigService notificationConfigService;

    /**
     * 新增通知配置
     * <p>
     * 功能说明：
     * 1. 验证配置键唯一性
     * 2. 如果isEncrypted=1，加密配置值
     * 3. 保存到数据库
     * 4. 清除相关缓存
     * 5. 触发AlertManager配置刷新（如果是告警配置）
     * </p>
     *
     * @param form 新增表单
     * @return 响应结果（包含配置ID）
     */
    @PostMapping
    @Operation(summary = "新增通知配置", description = "新增通知配置，支持加密存储敏感信息")
    public ResponseDTO<Long> add(@RequestBody @Valid NotificationConfigAddForm form) {
        log.info("[通知配置管理] 新增通知配置，configKey={}", form.getConfigKey());
        return notificationConfigService.add(form);
    }

    /**
     * 更新通知配置
     * <p>
     * 功能说明：
     * 1. 验证配置是否存在
     * 2. 如果isEncrypted=1，加密配置值
     * 3. 更新到数据库
     * 4. 清除相关缓存
     * 5. 触发AlertManager配置刷新（如果是告警配置）
     * </p>
     *
     * @param form 更新表单
     * @return 响应结果
     */
    @PutMapping
    @Operation(summary = "更新通知配置", description = "更新通知配置，支持部分字段更新")
    public ResponseDTO<Void> update(@RequestBody @Valid NotificationConfigUpdateForm form) {
        log.info("[通知配置管理] 更新通知配置，configId={}", form.getConfigId());
        return notificationConfigService.update(form);
    }

    /**
     * 删除通知配置（软删除）
     * <p>
     * 功能说明：
     * 1. 验证配置是否存在
     * 2. 执行软删除（deleted_flag=1）
     * 3. 清除相关缓存
     * 4. 触发AlertManager配置刷新（如果是告警配置）
     * </p>
     *
     * @param configId 配置ID
     * @return 响应结果
     */
    @DeleteMapping("/{configId}")
    @Operation(summary = "删除通知配置", description = "删除通知配置（软删除）")
    public ResponseDTO<Void> delete(
            @Parameter(description = "配置ID", required = true)
            @PathVariable @NotNull(message = "配置ID不能为空") Long configId) {
        log.info("[通知配置管理] 删除通知配置，configId={}", configId);
        return notificationConfigService.delete(configId);
    }

    /**
     * 根据ID查询通知配置
     * <p>
     * 功能说明：
     * 1. 从数据库查询配置
     * 2. 如果isEncrypted=1，解密配置值
     * 3. 敏感信息脱敏处理
     * 4. 转换为VO返回
     * </p>
     *
     * @param configId 配置ID
     * @return 响应结果
     */
    @GetMapping("/{configId}")
    @Operation(summary = "根据ID查询通知配置", description = "根据配置ID查询通知配置详情")
    public ResponseDTO<NotificationConfigVO> getById(
            @Parameter(description = "配置ID", required = true)
            @PathVariable @NotNull(message = "配置ID不能为空") Long configId) {
        log.debug("[通知配置管理] 根据ID查询通知配置，configId={}", configId);
        return notificationConfigService.getById(configId);
    }

    /**
     * 根据配置键查询通知配置
     * <p>
     * 功能说明：
     * 1. 从缓存或数据库查询配置
     * 2. 如果isEncrypted=1，解密配置值
     * 3. 敏感信息脱敏处理
     * 4. 转换为VO返回
     * </p>
     *
     * @param configKey 配置键
     * @return 响应结果
     */
    @GetMapping("/key/{configKey}")
    @Operation(summary = "根据配置键查询通知配置", description = "根据配置键查询通知配置详情")
    public ResponseDTO<NotificationConfigVO> getByConfigKey(
            @Parameter(description = "配置键", required = true)
            @PathVariable @NotNull(message = "配置键不能为空") String configKey) {
        log.debug("[通知配置管理] 根据配置键查询通知配置，configKey={}", configKey);
        return notificationConfigService.getByConfigKey(configKey);
    }

    /**
     * 分页查询通知配置
     * <p>
     * 功能说明：
     * 1. 根据查询条件构建查询条件
     * 2. 执行分页查询
     * 3. 如果isEncrypted=1，解密配置值
     * 4. 敏感信息脱敏处理
     * 5. 转换为VO列表返回
     * </p>
     *
     * @param form 查询表单
     * @return 响应结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询通知配置", description = "分页查询通知配置列表，支持按配置键、配置类型、状态筛选")
    public ResponseDTO<PageResult<NotificationConfigVO>> queryPage(@Valid NotificationConfigQueryForm form) {
        log.info("[通知配置管理] 分页查询通知配置，pageNum={}, pageSize={}", form.getPageNum(), form.getPageSize());
        return notificationConfigService.queryPage(form);
    }

    /**
     * 更新配置状态（启用/禁用）
     * <p>
     * 功能说明：
     * 1. 验证配置是否存在
     * 2. 更新配置状态
     * 3. 清除相关缓存
     * 4. 触发AlertManager配置刷新（如果是告警配置）
     * </p>
     *
     * @param configId 配置ID
     * @param status   状态（1-启用 2-禁用）
     * @return 响应结果
     */
    @PutMapping("/{configId}/status")
    @Operation(summary = "更新配置状态", description = "启用或禁用通知配置，管理员可通过此接口控制通知渠道的启用状态")
    public ResponseDTO<Void> updateStatus(
            @Parameter(description = "配置ID", required = true)
            @PathVariable @NotNull(message = "配置ID不能为空") Long configId,
            @Parameter(description = "状态：1-启用 2-禁用", required = true)
            @RequestParam @NotNull(message = "状态不能为空") Integer status) {
        log.info("[通知配置管理] 更新配置状态，configId={}, status={}", configId, status);
        return notificationConfigService.updateStatus(configId, status);
    }

    /**
     * 刷新AlertManager配置
     * <p>
     * 功能说明：
     * 1. 调用AlertManager的refreshAlertConfig方法
     * 2. 使配置修改立即生效，无需重启服务
     * </p>
     *
     * @return 响应结果
     */
    @PostMapping("/refresh-alert")
    @Operation(summary = "刷新AlertManager配置", description = "刷新AlertManager配置，使配置修改立即生效，无需重启服务")
    public ResponseDTO<Void> refreshAlertConfig() {
        log.info("[通知配置管理] 刷新AlertManager配置");
        return notificationConfigService.refreshAlertConfig();
    }
}

