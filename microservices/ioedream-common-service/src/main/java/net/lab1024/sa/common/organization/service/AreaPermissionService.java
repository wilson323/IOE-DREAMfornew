package net.lab1024.sa.common.organization.service;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.organization.domain.vo.AreaPermissionVO;

import java.util.List;

/**
 * 区域权限管理服务接口
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - Service层负责业务逻辑和事务管理
 * - 调用Manager层进行复杂流程编排
 * - 遵循四层架构：Controller → Service → Manager → DAO
 * </p>
 * <p>
 * 核心职责：
 * - 区域权限查询
 * - 区域权限删除
 * - 区域权限批量删除
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface AreaPermissionService {

    /**
     * 根据区域ID查询权限列表
     *
     * @param areaId 区域ID
     * @param type 权限类型（可选）
     * @return 权限列表
     */
    ResponseDTO<List<AreaPermissionVO>> listByArea(Long areaId, String type);

    /**
     * 删除权限
     *
     * @param permissionId 权限ID
     * @return 操作结果
     */
    ResponseDTO<Void> delete(String permissionId);

    /**
     * 批量删除权限
     *
     * @param permissionIds 权限ID列表
     * @return 操作结果
     */
    ResponseDTO<Void> batchDelete(List<String> permissionIds);
}
