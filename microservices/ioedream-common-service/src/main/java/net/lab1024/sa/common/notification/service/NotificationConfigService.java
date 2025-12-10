package net.lab1024.sa.common.notification.service;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.notification.domain.form.NotificationConfigAddForm;
import net.lab1024.sa.common.notification.domain.form.NotificationConfigQueryForm;
import net.lab1024.sa.common.notification.domain.form.NotificationConfigUpdateForm;
import net.lab1024.sa.common.notification.domain.vo.NotificationConfigVO;

/**
 * 通知配置服务接口
 * <p>
 * 提供通知配置的CRUD操作和配置管理功能
 * 严格遵循CLAUDE.md规范:
 * - Service接口定义在微服务中
 * - 使用ResponseDTO统一响应格式
 * - 支持分页查询
 * - 完整的异常处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface NotificationConfigService {

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
     * @return 响应结果
     */
    ResponseDTO<Long> add(NotificationConfigAddForm form);

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
    ResponseDTO<Void> update(NotificationConfigUpdateForm form);

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
    ResponseDTO<Void> delete(Long configId);

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
    ResponseDTO<NotificationConfigVO> getById(Long configId);

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
    ResponseDTO<NotificationConfigVO> getByConfigKey(String configKey);

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
    ResponseDTO<PageResult<NotificationConfigVO>> queryPage(NotificationConfigQueryForm form);

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
    ResponseDTO<Void> updateStatus(Long configId, Integer status);

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
    ResponseDTO<Void> refreshAlertConfig();
}

