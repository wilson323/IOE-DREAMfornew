package net.lab1024.sa.base.common.tenant;

/**
 * 租户上下文持有者
 *
 * 提供租户信息的线程级别存储和管理
 * 支持多租架数据隔离
 *
 * @author SmartAdmin Team
 * @since 2025-11-18
 */
public class TenantContextHolder {

    private static final ThreadLocal<Long> TENANT_ID_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前租户ID
     *
     * @param tenantId 租户ID
     */
    public static void setTenantId(Long tenantId) {
        TENANT_ID_HOLDER.set(tenantId);
    }

    /**
     * 获取当前租户ID
     *
     * @return 租户ID，如果未设置则返回默认值
     */
    public static Long getTenantId() {
        Long tenantId = TENANT_ID_HOLDER.get();
        return tenantId != null ? tenantId : 0L; // 默认租户ID为0
    }

    /**
     * 清除当前租户信息
     */
    public static void clear() {
        TENANT_ID_HOLDER.remove();
    }

    /**
     * 检查是否有租户信息
     *
     * @return true如果有租户信息，false如果没有
     */
    public static boolean hasTenant() {
        return TENANT_ID_HOLDER.get() != null;
    }
}