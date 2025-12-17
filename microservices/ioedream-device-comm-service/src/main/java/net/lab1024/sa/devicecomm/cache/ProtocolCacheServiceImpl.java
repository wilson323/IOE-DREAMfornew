package net.lab1024.sa.devicecomm.cache;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.gateway.DirectServiceClient;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.organization.entity.DeviceEntity;

/**
 * 协议缓存服务实现类
 * <p>
 * 使用Spring Cache注解(Cacheable、CacheEvict、CachePut)替代ProtocolCacheManager
 * 严格遵循CLAUDE.md规范:
 * - 统一使用Spring Cache + Caffeine + Redis
 * - 禁止使用自定义CacheManager
 * - 使用CompositeCacheManager(L1本地缓存 + L2 Redis缓存)
 * </p>
 * <p>
 * 缓存配置:
 * - 缓存名称: protocol:device、protocol:device:code、protocol:user:card
 * - L1本地缓存: Caffeine,5分钟过期
 * - L2分布式缓存: Redis,30分钟过期
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class ProtocolCacheServiceImpl implements ProtocolCacheService {

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Resource(required = false)
    private DirectServiceClient directServiceClient;

    @Value("${ioedream.direct-call.enabled:false}")
    private boolean directEnabled;

    /**
     * 根据设备ID获取设备信息(使用Spring Cache)
     * <p>
     * 缓存策略:
     * - L1本地缓存: Caffeine,5分钟过期
     * - L2分布式缓存: Redis,30分钟过期
     * - 缓存键: protocol:device:{deviceId}
     * </p>
     *
     * @param deviceId 设备ID
     * @return 设备实体,如果不存在返回null
     */
    @Override
    @Observed(name = "protocol.cache.getDeviceById", contextualName = "protocol-cache-get-device-by-id")
    @Cacheable(value = "protocol:device", key = "#deviceId", unless = "#result == null")
    public DeviceEntity getDeviceById(Long deviceId) {
        if (deviceId == null) {
            return null;
        }

        log.debug("[协议缓存] 缓存未命中,从数据库查询设备信息,deviceId={}", deviceId);

        String path = "/api/v1/device/" + deviceId;

        // 优先直连公共服务查询设备信息(热路径),失败回退网关
        try {
            ResponseDTO<DeviceEntity> response = null;
            if (directEnabled && directServiceClient != null && directServiceClient.isEnabled()) {
                response = directServiceClient.callCommonService(
                        path,
                        HttpMethod.GET,
                        null,
                        DeviceEntity.class
                );
            }
            if (response == null || !response.isSuccess() || response.getData() == null) {
                response = gatewayServiceClient.callCommonService(
                        path,
                        HttpMethod.GET,
                        null,
                        DeviceEntity.class
                );
            }

            if (response != null && response.isSuccess() && response.getData() != null) {
                DeviceEntity device = response.getData();
                log.debug("[协议缓存] 从数据库查询到设备信息,deviceId={}, deviceCode={}",
                        deviceId, device.getDeviceCode());
                return device;
            }

            log.debug("[协议缓存] 数据库未查询到设备信息,deviceId={}", deviceId);
            return null;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[协议缓存] 查询设备信息参数错误: deviceId={}, error={}", deviceId, e.getMessage());
            return null;
        } catch (BusinessException e) {
            log.warn("[协议缓存] 查询设备信息业务异常: deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage());
            return null;
        } catch (SystemException e) {
            log.error("[协议缓存] 查询设备信息系统异常: deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.warn("[协议缓存] 查询设备信息未知异常: deviceId={}", deviceId, e);
            return null;
        }
    }

    /**
     * 根据设备编码获取设备信息(使用Spring Cache)
     * <p>
     * 缓存策略:
     * - L1本地缓存: Caffeine,5分钟过期
     * - L2分布式缓存: Redis,30分钟过期
     * - 缓存键: protocol:device:code:{deviceCode}
     * </p>
     *
     * @param deviceCode 设备编码(SN)
     * @return 设备实体,如果不存在返回null
     */
    @Override
    @Observed(name = "protocol.cache.getDeviceByCode", contextualName = "protocol-cache-get-device-by-code")
    @Cacheable(value = "protocol:device:code", key = "#deviceCode", unless = "#result == null")
    public DeviceEntity getDeviceByCode(String deviceCode) {
        if (deviceCode == null || deviceCode.isEmpty()) {
            return null;
        }

        log.debug("[协议缓存] 缓存未命中,从数据库查询设备信息,deviceCode={}", deviceCode);

        String path = "/api/v1/device/code/" + deviceCode;

        // 优先直连公共服务查询设备信息(热路径),失败回退网关
        try {
            ResponseDTO<DeviceEntity> response = null;
            if (directEnabled && directServiceClient != null && directServiceClient.isEnabled()) {
                response = directServiceClient.callCommonService(
                        path,
                        HttpMethod.GET,
                        null,
                        DeviceEntity.class
                );
            }
            if (response == null || !response.isSuccess() || response.getData() == null) {
                response = gatewayServiceClient.callCommonService(
                        path,
                        HttpMethod.GET,
                        null,
                        DeviceEntity.class
                );
            }

            if (response != null && response.isSuccess() && response.getData() != null) {
                DeviceEntity device = response.getData();
                log.debug("[协议缓存] 从数据库查询到设备信息,deviceCode={}, deviceId={}",
                        deviceCode, device.getId());
                return device;
            }

            log.debug("[协议缓存] 数据库未查询到设备信息,deviceCode={}", deviceCode);
            return null;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[协议缓存] 查询设备信息参数错误: deviceCode={}, error={}", deviceCode, e.getMessage());
            return null;
        } catch (BusinessException e) {
            log.warn("[协议缓存] 查询设备信息业务异常: deviceCode={}, code={}, message={}", deviceCode, e.getCode(), e.getMessage());
            return null;
        } catch (SystemException e) {
            log.error("[协议缓存] 查询设备信息系统异常: deviceCode={}, code={}, message={}", deviceCode, e.getCode(), e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.warn("[协议缓存] 查询设备信息未知异常: deviceCode={}", deviceCode, e);
            return null;
        }
    }

    /**
     * 缓存设备信息(使用Spring Cache)
     * <p>
     * 使用@CachePut确保缓存更新
     * 同时缓存设备ID和设备编码两个键
     * </p>
     *
     * @param device 设备实体
     * @return 设备实体
     */
    @Override
    @Observed(name = "protocol.cache.cacheDevice", contextualName = "protocol-cache-cache-device")
    @CachePut(value = "protocol:device", key = "#device.id")
    public DeviceEntity cacheDevice(DeviceEntity device) {
        if (device == null) {
            return null;
        }

        log.debug("[协议缓存] 缓存设备信息,deviceId={}, deviceCode={}",
                device.getId(), device.getDeviceCode());

        // 如果设备编码不为空,也缓存设备编码映射
        if (device.getDeviceCode() != null && !device.getDeviceCode().isEmpty()) {
            cacheDeviceByCode(device);
        }

        return device;
    }

    /**
     * 缓存设备信息(根据设备编码)
     *
     * @param device 设备实体
     * @return 设备实体
     */
    @Observed(name = "protocol.cache.cacheDeviceByCode", contextualName = "protocol-cache-cache-device-by-code")
    @CachePut(value = "protocol:device:code", key = "#device.deviceCode")
    public DeviceEntity cacheDeviceByCode(DeviceEntity device) {
        if (device == null) {
            return null;
        }

        log.debug("[协议缓存] 缓存设备编码映射,deviceCode={}, deviceId={}",
                device.getDeviceCode(), device.getId());

        return device;
    }

    /**
     * 根据卡号获取用户ID(使用Spring Cache)
     * <p>
     * 注意:此方法需要调用消费服务查询用户ID
     * 缓存策略:
     * - L1本地缓存: Caffeine,5分钟过期
     * - L2分布式缓存: Redis,30分钟过期
     * - 缓存键: protocol:user:card:{cardNumber}
     * </p>
     *
     * @param cardNumber 卡号
     * @return 用户ID,如果不存在返回null
     */
    @Override
    @Cacheable(value = "protocol:user:card", key = "#cardNumber", unless = "#result == null")
    public Long getUserIdByCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            return null;
        }

        log.debug("[协议缓存] 缓存未命中,从数据库查询用户ID,cardNumber={}", cardNumber);

        // 通过网关调用消费服务查询用户ID
        try {
            String url = "/api/v1/consume/mobile/user/quick?queryType=cardNumber&queryValue=" +
                         java.net.URLEncoder.encode(cardNumber, java.nio.charset.StandardCharsets.UTF_8);

            @SuppressWarnings("unchecked")
            ResponseDTO<Map<String, Object>> response = (ResponseDTO<Map<String, Object>>)
                    (ResponseDTO<?>) gatewayServiceClient.callConsumeService(
                            url,
                            HttpMethod.GET,
                            null,
                            Map.class
                    );

            if (response != null && response.isSuccess() && response.getData() != null) {
                Map<String, Object> data = response.getData();
                Object userIdObj = data.get("userId");
                if (userIdObj instanceof Number) {
                    Long userId = ((Number) userIdObj).longValue();
                    log.debug("[协议缓存] 从数据库查询到用户ID,cardNumber={}, userId={}",
                            cardNumber, userId);
                    return userId;
                }
            }

            log.debug("[协议缓存] 数据库未查询到用户ID,cardNumber={}", cardNumber);
            return null;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[协议缓存] 查询用户ID参数错误: cardNumber={}, error={}", cardNumber, e.getMessage());
            return null;
        } catch (BusinessException e) {
            log.warn("[协议缓存] 查询用户ID业务异常: cardNumber={}, code={}, message={}", cardNumber, e.getCode(), e.getMessage());
            return null;
        } catch (SystemException e) {
            log.error("[协议缓存] 查询用户ID系统异常: cardNumber={}, code={}, message={}", cardNumber, e.getCode(), e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.warn("[协议缓存] 查询用户ID未知异常: cardNumber={}", cardNumber, e);
            return null;
        }
    }

    /**
     * 缓存卡号到用户ID的映射(使用Spring Cache)
     *
     * @param cardNumber 卡号
     * @param userId 用户ID
     * @return 用户ID
     */
    @Override
    @CachePut(value = "protocol:user:card", key = "#cardNumber")
    public Long cacheUserCardMapping(String cardNumber, Long userId) {
        if (cardNumber == null || cardNumber.trim().isEmpty() || userId == null) {
            return null;
        }

        log.debug("[协议缓存] 缓存卡号映射,cardNumber={}, userId={}", cardNumber, userId);
        return userId;
    }

    /**
     * 清除设备缓存
     *
     * @param deviceId 设备ID
     */
    @Override
    @CacheEvict(value = "protocol:device", key = "#deviceId")
    public void evictDevice(Long deviceId) {
        if (deviceId == null) {
            return;
        }

        log.debug("[协议缓存] 清除设备缓存,deviceId={}", deviceId);
    }

    /**
     * 清除设备缓存(根据设备编码)
     *
     * @param deviceCode 设备编码
     */
    @Override
    @CacheEvict(value = "protocol:device:code", key = "#deviceCode")
    public void evictDeviceByCode(String deviceCode) {
        if (deviceCode == null || deviceCode.isEmpty()) {
            return;
        }

        log.debug("[协议缓存] 清除设备编码缓存,deviceCode={}", deviceCode);
    }
}
