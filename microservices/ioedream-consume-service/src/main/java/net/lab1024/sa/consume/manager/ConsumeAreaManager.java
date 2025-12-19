package net.lab1024.sa.consume.manager;

import net.lab1024.sa.consume.entity.ConsumeAreaEntity;

/**
 * 消费区域管理Manager接口
 * <p>
 * 用于区域相关的复杂业务逻辑编排
 * 严格遵循CLAUDE.md规范：
 * - Manager类在microservices-common中不使用Spring注解
 * - Manager类通过构造函数注入依赖
 * - 保持为纯Java类
 * </p>
 * <p>
 * 业务场景：
 * - 区域信息查询
 * - 区域权限验证
 * - 区域层级查询
 * - 区域经营模式配置
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface ConsumeAreaManager {

    /**
     * 根据区域ID获取区域信息
     *
     * @param areaId 区域ID
     * @return 区域信息
     */
    ConsumeAreaEntity getAreaById(String areaId);

    /**
     * 根据区域编号获取区域信息
     *
     * @param areaCode 区域编号
     * @return 区域信息
     */
    ConsumeAreaEntity getAreaByCode(String areaCode);

    /**
     * 验证用户是否有权限在指定区域消费
     *
     * @param accountId 账户ID
     * @param areaId 区域ID
     * @return 是否有权限
     */
    boolean validateAreaPermission(Long accountId, String areaId);

    /**
     * 获取区域的完整路径
     *
     * @param areaId 区域ID
     * @return 完整路径（如：/园区/楼栋/楼层/区域）
     */
    String getAreaFullPath(String areaId);

    /**
     * 解析区域定值配置
     *
     * @param areaId 区域ID
     * @return 定值配置Map
     */
    java.util.Map<String, Object> parseFixedValueConfig(String areaId);
}



