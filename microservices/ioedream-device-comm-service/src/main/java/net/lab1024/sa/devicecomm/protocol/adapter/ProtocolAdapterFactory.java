package net.lab1024.sa.devicecomm.protocol.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.devicecomm.protocol.enums.ProtocolTypeEnum;
import net.lab1024.sa.devicecomm.protocol.handler.ProtocolHandler;

/**
 * 协议适配器工厂
 * <p>
 * 根据设备类型和厂商选择合适的协议处理器
 * 严格遵循CLAUDE.md规范：
 * - 使用@Component注解，由Spring管理
 * - 使用@Resource注入依赖
 * - 工厂模式实现
 * </p>
 * <p>
 * 功能：
 * - 协议处理器注册和管理
 * - 根据设备信息选择协议处理器
 * - 协议处理器缓存
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
public class ProtocolAdapterFactory {

    /**
     * 协议处理器映射表（协议类型 -> 处理器）
     */
    private final Map<String, ProtocolHandler> protocolHandlerMap = new HashMap<>();

    /**
     * 所有协议处理器（由Spring自动注入）
     */
    @Resource
    private List<ProtocolHandler> protocolHandlers;

    /**
     * 初始化协议处理器映射
     * <p>
     * 在Spring容器启动后自动调用，将所有协议处理器注册到映射表中
     * </p>
     */
    @PostConstruct
    public void init() {
        log.info("[协议工厂] 开始初始化协议处理器映射");

        if (protocolHandlers == null || protocolHandlers.isEmpty()) {
            log.warn("[协议工厂] 未找到任何协议处理器");
            return;
        }

        // 注册所有协议处理器
        for (ProtocolHandler handler : protocolHandlers) {
            String protocolType = handler.getProtocolType();
            protocolHandlerMap.put(protocolType, handler);
            log.info("[协议工厂] 注册协议处理器：{} - {} ({})",
                    protocolType, handler.getManufacturer(), handler.getVersion());
        }

        log.info("[协议工厂] 协议处理器初始化完成，共注册{}个处理器", protocolHandlerMap.size());
    }

    /**
     * 根据协议类型获取协议处理器
     * <p>
     * 根据协议类型代码（如"ATTENDANCE_ENTROPY_V4.0"）获取对应的协议处理器
     * </p>
     *
     * @param protocolType 协议类型代码
     * @return 协议处理器，如果不存在返回null
     */
    public ProtocolHandler getHandler(String protocolType) {
        if (protocolType == null || protocolType.isEmpty()) {
            log.warn("[协议工厂] 协议类型为空");
            return null;
        }

        ProtocolHandler handler = protocolHandlerMap.get(protocolType);
        if (handler == null) {
            log.warn("[协议工厂] 未找到协议处理器：{}", protocolType);
        } else {
            log.debug("[协议工厂] 获取协议处理器：{}", protocolType);
        }

        return handler;
    }

    /**
     * 根据设备类型和厂商获取协议处理器
     * <p>
     * 根据设备类型（ATTENDANCE、ACCESS、CONSUME）和厂商名称获取对应的协议处理器
     * </p>
     *
     * @param deviceType 设备类型（ATTENDANCE、ACCESS、CONSUME）
     * @param manufacturer 厂商名称（熵基科技、中控智慧）
     * @return 协议处理器，如果不存在返回null
     */
    public ProtocolHandler getHandler(String deviceType, String manufacturer) {
        if (deviceType == null || deviceType.isEmpty()) {
            log.warn("[协议工厂] 设备类型为空");
            return null;
        }

        if (manufacturer == null || manufacturer.isEmpty()) {
            log.warn("[协议工厂] 厂商名称为空");
            return null;
        }

        // 根据设备类型和厂商查找协议类型
        ProtocolTypeEnum protocolType = ProtocolTypeEnum.getByDeviceTypeAndManufacturer(deviceType, manufacturer);
        if (protocolType == null) {
            log.warn("[协议工厂] 未找到协议类型：设备类型={}, 厂商={}", deviceType, manufacturer);
            return null;
        }

        return getHandler(protocolType.getCode());
    }

    /**
     * 根据协议类型枚举获取协议处理器
     * <p>
     * 根据ProtocolTypeEnum枚举获取对应的协议处理器
     * </p>
     *
     * @param protocolType 协议类型枚举
     * @return 协议处理器，如果不存在返回null
     */
    public ProtocolHandler getHandler(ProtocolTypeEnum protocolType) {
        if (protocolType == null) {
            log.warn("[协议工厂] 协议类型枚举为空");
            return null;
        }

        return getHandler(protocolType.getCode());
    }

    /**
     * 获取所有已注册的协议处理器
     * <p>
     * 返回所有已注册的协议处理器映射表（只读）
     * </p>
     *
     * @return 协议处理器映射表（只读）
     */
    public Map<String, ProtocolHandler> getAllHandlers() {
        return new HashMap<>(protocolHandlerMap);
    }

    /**
     * 检查协议处理器是否存在
     * <p>
     * 检查指定协议类型的处理器是否已注册
     * </p>
     *
     * @param protocolType 协议类型代码
     * @return true-存在，false-不存在
     */
    public boolean hasHandler(String protocolType) {
        return protocolType != null && protocolHandlerMap.containsKey(protocolType);
    }
}

