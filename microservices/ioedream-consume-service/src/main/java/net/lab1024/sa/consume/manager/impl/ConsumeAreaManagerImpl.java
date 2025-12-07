package net.lab1024.sa.consume.manager.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.cache.CacheService;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.consume.dao.ConsumeAreaDao;
import net.lab1024.sa.consume.domain.entity.ConsumeAreaEntity;
import net.lab1024.sa.consume.manager.AccountManager;
import net.lab1024.sa.consume.manager.ConsumeAreaManager;

/**
 * 消费区域管理Manager实现类
 * <p>
 * 实现区域相关的复杂业务逻辑编排
 * 严格遵循CLAUDE.md规范：
 * - Manager实现类在ioedream-consume-service中
 * - 通过构造函数注入依赖
 * - 保持为纯Java类（不使用Spring注解）
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
@Slf4j
public class ConsumeAreaManagerImpl implements ConsumeAreaManager {

    private final ConsumeAreaDao consumeAreaDao;
    private final ObjectMapper objectMapper;
    private final AccountManager accountManager;
    private final GatewayServiceClient gatewayServiceClient;
    private final CacheService cacheService;

    /**
     * 构造函数注入依赖
     * <p>
     * 符合CLAUDE.md规范：通过构造函数接收依赖
     * </p>
     *
     * @param consumeAreaDao 区域DAO
     * @param objectMapper JSON对象映射器
     * @param accountManager 账户管理器
     * @param gatewayServiceClient 网关服务客户端
     * @param cacheService 缓存服务
     */
    public ConsumeAreaManagerImpl(
            ConsumeAreaDao consumeAreaDao,
            ObjectMapper objectMapper,
            AccountManager accountManager,
            GatewayServiceClient gatewayServiceClient,
            CacheService cacheService) {
        this.consumeAreaDao = consumeAreaDao;
        this.objectMapper = objectMapper;
        this.accountManager = accountManager;
        this.gatewayServiceClient = gatewayServiceClient;
        this.cacheService = cacheService;
    }

    /**
     * 根据区域ID获取区域信息
     *
     * @param areaId 区域ID
     * @return 区域信息
     */
    @Override
    public ConsumeAreaEntity getAreaById(String areaId) {
        log.debug("[区域管理] 根据区域ID获取区域信息，areaId={}", areaId);
        try {
            return consumeAreaDao.selectById(areaId);
        } catch (Exception e) {
            log.error("[区域管理] 获取区域信息失败，areaId={}", areaId, e);
            return null;
        }
    }

    /**
     * 根据区域编号获取区域信息
     *
     * @param areaCode 区域编号
     * @return 区域信息
     */
    @Override
    public ConsumeAreaEntity getAreaByCode(String areaCode) {
        log.debug("[区域管理] 根据区域编号获取区域信息，areaCode={}", areaCode);
        try {
            return consumeAreaDao.selectByCode(areaCode);
        } catch (Exception e) {
            log.error("[区域管理] 获取区域信息失败，areaCode={}", areaCode, e);
            return null;
        }
    }

    /**
     * 验证用户是否有权限在指定区域消费
     * <p>
     * 基于账户类别的area_config JSON配置进行权限验证
     * 严格遵循业务文档04-账户类别区域权限设计.md的验证流程
     * </p>
     * <p>
     * 验证流程：
     * 1. 获取账户信息，读取accountKindId
     * 2. 通过网关调用公共服务获取账户类别信息（包含area_config）
     * 3. 解析area_config JSON数组
     * 4. 遍历配置项，检查区域ID是否匹配（直接匹配或子区域匹配）
     * 5. 使用缓存优化性能（30分钟过期）
     * </p>
     *
     * @param accountId 账户ID
     * @param areaId 区域ID
     * @return 是否有权限
     */
    @Override
    public boolean validateAreaPermission(Long accountId, String areaId) {
        log.debug("[区域管理] 验证区域权限，accountId={}, areaId={}", accountId, areaId);
        try {
            // 1. 检查缓存
            String cacheKey = String.format("perm:area:%d:%s", accountId, areaId);
            Boolean cachedResult = cacheService.get(cacheKey, Boolean.class);
            if (cachedResult != null) {
                log.debug("[区域管理] 缓存命中，accountId={}, areaId={}, result={}", accountId, areaId, cachedResult);
                return cachedResult;
            }

            // 2. 获取账户信息
            net.lab1024.sa.consume.domain.entity.AccountEntity account = accountManager.getAccountById(accountId);
            if (account == null) {
                log.warn("[区域管理] 账户不存在，accountId={}", accountId);
                cacheService.set(cacheKey, false, 30, TimeUnit.MINUTES);
                return false;
            }

            // 3. 检查账户状态
            if (account.getStatus() == null || account.getStatus() != 1) {
                log.warn("[区域管理] 账户状态异常，accountId={}, status={}", accountId, account.getStatus());
                cacheService.set(cacheKey, false, 30, TimeUnit.MINUTES);
                return false;
            }

            // 4. 获取账户类别ID
            Long accountKindId = account.getAccountKindId();
            if (accountKindId == null) {
                log.warn("[区域管理] 账户类别ID为空，accountId={}", accountId);
                cacheService.set(cacheKey, false, 30, TimeUnit.MINUTES);
                return false;
            }

            // 5. 通过网关调用公共服务获取账户类别信息
            ResponseDTO<Map<String, Object>> accountKindResponse = gatewayServiceClient.callCommonService(
                    "/api/v1/account-kind/" + accountKindId,
                    org.springframework.http.HttpMethod.GET,
                    null,
                    new TypeReference<ResponseDTO<Map<String, Object>>>() {}
            );

            if (accountKindResponse == null || !accountKindResponse.isSuccess() || accountKindResponse.getData() == null) {
                log.warn("[区域管理] 获取账户类别信息失败，accountKindId={}", accountKindId);
                cacheService.set(cacheKey, false, 30, TimeUnit.MINUTES);
                return false;
            }

            Map<String, Object> accountKind = accountKindResponse.getData();

            // 6. 获取area_config JSON字段
            Object areaConfigObj = accountKind.get("area_config");
            if (areaConfigObj == null) {
                log.warn("[区域管理] 账户类别未配置区域权限，accountKindId={}", accountKindId);
                cacheService.set(cacheKey, false, 30, TimeUnit.MINUTES);
                return false;
            }

            // 7. 解析area_config JSON数组
            List<Map<String, Object>> areaConfigList;
            if (areaConfigObj instanceof String) {
                // JSON字符串，需要解析
                areaConfigList = objectMapper.readValue((String) areaConfigObj, new TypeReference<List<Map<String, Object>>>() {});
            } else if (areaConfigObj instanceof List) {
                // 已经是List类型
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> list = (List<Map<String, Object>>) areaConfigObj;
                areaConfigList = list;
            } else {
                log.warn("[区域管理] area_config格式不正确，accountKindId={}", accountKindId);
                cacheService.set(cacheKey, false, 30, TimeUnit.MINUTES);
                return false;
            }

            if (areaConfigList == null || areaConfigList.isEmpty()) {
                log.warn("[区域管理] area_config为空数组，accountKindId={}", accountKindId);
                cacheService.set(cacheKey, false, 30, TimeUnit.MINUTES);
                return false;
            }

            // 8. 获取目标区域信息
            ConsumeAreaEntity targetArea = consumeAreaDao.selectById(areaId);
            if (targetArea == null) {
                log.warn("[区域管理] 目标区域不存在，areaId={}", areaId);
                cacheService.set(cacheKey, false, 30, TimeUnit.MINUTES);
                return false;
            }

            // 9. 遍历area_config，检查区域权限
            for (Map<String, Object> areaConfig : areaConfigList) {
                String configAreaId = (String) areaConfig.get("areaId");
                if (configAreaId == null) {
                    continue;
                }

                // 9.1 直接匹配
                if (configAreaId.equals(areaId)) {
                    log.debug("[区域管理] 区域权限验证通过（直接匹配），accountId={}, areaId={}", accountId, areaId);
                    cacheService.set(cacheKey, true, 30, TimeUnit.MINUTES);
                    return true;
                }

                // 9.2 检查是否包含子区域
                Object includeSubAreasObj = areaConfig.get("includeSubAreas");
                boolean includeSubAreas = includeSubAreasObj != null && Boolean.parseBoolean(includeSubAreasObj.toString());

                if (includeSubAreas) {
                    // 检查目标区域是否为配置区域的子区域
                    // 通过fullPath判断：如果目标区域的fullPath包含配置区域ID，则为子区域
                    String targetFullPath = targetArea.getFullPath();
                    if (targetFullPath != null && targetFullPath.contains(configAreaId)) {
                        log.debug("[区域管理] 区域权限验证通过（子区域匹配），accountId={}, areaId={}, parentAreaId={}",
                                accountId, areaId, configAreaId);
                        cacheService.set(cacheKey, true, 30, TimeUnit.MINUTES);
                        return true;
                    }

                    // 或者通过parentId递归检查
                    if (isSubArea(areaId, configAreaId)) {
                        log.debug("[区域管理] 区域权限验证通过（子区域递归匹配），accountId={}, areaId={}, parentAreaId={}",
                                accountId, areaId, configAreaId);
                        cacheService.set(cacheKey, true, 30, TimeUnit.MINUTES);
                        return true;
                    }
                }
            }

            // 10. 所有配置项都不匹配，权限验证失败
            log.warn("[区域管理] 区域权限验证失败，账户无权在该区域消费，accountId={}, areaId={}", accountId, areaId);
            cacheService.set(cacheKey, false, 30, TimeUnit.MINUTES);
            return false;

        } catch (Exception e) {
            log.error("[区域管理] 验证区域权限失败，accountId={}, areaId={}", accountId, areaId, e);
            return false;
        }
    }

    /**
     * 检查目标区域是否为配置区域的子区域（递归检查）
     *
     * @param targetAreaId 目标区域ID
     * @param configAreaId 配置区域ID
     * @return 是否为子区域
     */
    private boolean isSubArea(String targetAreaId, String configAreaId) {
        if (targetAreaId == null || configAreaId == null || targetAreaId.equals(configAreaId)) {
            return false;
        }

        ConsumeAreaEntity targetArea = consumeAreaDao.selectById(targetAreaId);
        if (targetArea == null) {
            return false;
        }

        String parentId = targetArea.getParentId();
        if (parentId == null) {
            return false;
        }

        if (parentId.equals(configAreaId)) {
            return true;
        }

        // 递归检查父区域
        return isSubArea(parentId, configAreaId);
    }

    /**
     * 获取区域的完整路径
     *
     * @param areaId 区域ID
     * @return 完整路径（如：/园区/楼栋/楼层/区域）
     */
    @Override
    public String getAreaFullPath(String areaId) {
        log.debug("[区域管理] 获取区域完整路径，areaId={}", areaId);
        try {
            ConsumeAreaEntity area = consumeAreaDao.selectById(areaId);
            if (area == null) {
                return null;
            }
            return area.getFullPath();
        } catch (Exception e) {
            log.error("[区域管理] 获取区域完整路径失败，areaId={}", areaId, e);
            return null;
        }
    }

    /**
     * 解析区域定值配置
     *
     * @param areaId 区域ID
     * @return 定值配置Map
     */
    @Override
    public Map<String, Object> parseFixedValueConfig(String areaId) {
        log.debug("[区域管理] 解析区域定值配置，areaId={}", areaId);
        try {
            ConsumeAreaEntity area = consumeAreaDao.selectById(areaId);
            if (area == null) {
                log.warn("[区域管理] 区域不存在，areaId={}", areaId);
                return new HashMap<>();
            }

            // 从扩展属性中获取定值配置
            String extendedAttributes = area.getExtendedAttributes();
            if (extendedAttributes == null || extendedAttributes.trim().isEmpty()) {
                log.warn("[区域管理] 区域定值配置为空，areaId={}", areaId);
                return new HashMap<>();
            }

            // 解析JSON配置
            @SuppressWarnings("unchecked")
            Map<String, Object> config = objectMapper.readValue(extendedAttributes, Map.class);
            return config;

        } catch (Exception e) {
            log.error("[区域管理] 解析区域定值配置失败，areaId={}", areaId, e);
            return new HashMap<>();
        }
    }
}
