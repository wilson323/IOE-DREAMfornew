package net.lab1024.sa.device.comm.protocol.handler;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.device.comm.protocol.domain.DeviceCommandRequest;
import net.lab1024.sa.device.comm.protocol.domain.ProtocolProcessResult;
import net.lab1024.sa.device.comm.protocol.exception.ProtocolProcessException;

/**
 * 基础协议处理器
 * <p>
 * 所有设备协议处理器的基类，提供通用的协议处理流程和异常处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-22
 */
@Slf4j
public abstract class BaseProtocolHandler {

    /**
     * 协议类型
     */
    protected final String protocolType;

    /**
     * 构造函数
     *
     * @param protocolType 协议类型
     */
    protected BaseProtocolHandler(String protocolType) {
        this.protocolType = protocolType;
    }

    /**
     * 处理设备命令
     * <p>
     * 模板方法，定义标准的协议处理流程
     * </p>
     *
     * @param request 设备命令请求
     * @return 协议处理结果
     * @throws ProtocolProcessException 协议处理异常
     */
    public final ProtocolProcessResult handleCommand(DeviceCommandRequest request) {
        log.info("[协议处理] 开始处理{}协议命令: deviceId={}, commandType={}",
                protocolType, request.getDeviceId(), request.getCommandType());

        try {
            // 1. 参数校验
            validateCommand(request);

            // 2. 执行具体协议处理逻辑
            ProtocolProcessResult result = processCommand(request);

            // 3. 结果校验
            validateResult(result);

            log.info("[协议处理] {}协议命令处理成功: deviceId={}, result={}",
                    protocolType, request.getDeviceId(), result.getMessage());

            return result;

        } catch (ProtocolProcessException e) {
            log.error("[协议处理] {}协议处理失败: deviceId={}, error={}",
                    protocolType, request.getDeviceId(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[协议处理] {}协议处理异常: deviceId={}, error={}",
                    protocolType, request.getDeviceId(), e.getMessage(), e);
            throw ProtocolProcessException.processFailed(
                    String.format("%s协议处理异常: %s", protocolType, e.getMessage()),
                    request.getDeviceId(),
                    protocolType,
                    e);
        }
    }

    /**
     * 校验设备命令请求
     * <p>
     * 子类可以重写此方法进行特定的参数校验
     * </p>
     *
     * @param request 设备命令请求
     * @throws ProtocolProcessException 校验失败时抛出异常
     */
    protected void validateCommand(DeviceCommandRequest request) {
        if (request == null) {
            throw ProtocolProcessException.processFailed("设备命令请求不能为空");
        }

        if (request.getDeviceId() == null) {
            throw ProtocolProcessException.processFailed("设备ID不能为空");
        }

        if (request.getCommandType() == null || request.getCommandType().trim().isEmpty()) {
            throw ProtocolProcessException.processFailed("命令类型不能为空");
        }

        // 子类特定的校验
        doValidateCommand(request);
    }

    /**
     * 子类特定的参数校验
     * <p>
     * 子类重写此方法实现特定的校验逻辑
     * </p>
     *
     * @param request 设备命令请求
     * @throws ProtocolProcessException 校验失败时抛出异常
     */
    protected abstract void doValidateCommand(DeviceCommandRequest request);

    /**
     * 执行具体的协议处理逻辑
     * <p>
     * 子类必须实现此方法完成具体的协议处理
     * </p>
     *
     * @param request 设备命令请求
     * @return 协议处理结果
     * @throws ProtocolProcessException 处理失败时抛出异常
     */
    protected abstract ProtocolProcessResult processCommand(DeviceCommandRequest request);

    /**
     * 校验处理结果
     * <p>
     * 子类可以重写此方法进行特定的结果校验
     * </p>
     *
     * @param result 处理结果
     * @throws ProtocolProcessException 校验失败时抛出异常
     */
    protected void validateResult(ProtocolProcessResult result) {
        if (result == null) {
            throw ProtocolProcessException.processFailed("协议处理结果不能为空");
        }

        if (!result.isSuccess() && (result.getErrorCode() == null || result.getMessage() == null)) {
            log.warn("[协议处理] {}协议处理失败但缺少错误信息: deviceId={}",
                    protocolType, "unknown");
        }
    }

    /**
     * 创建成功结果
     *
     * @param message 成功消息
     * @return 协议处理结果
     */
    protected ProtocolProcessResult createSuccessResult(String message) {
        return ProtocolProcessResult.builder()
                .success(true)
                .businessType(protocolType)
                .message(message)
                .build();
    }

    /**
     * 创建成功结果（带数据）
     *
     * @param message 成功消息
     * @param resultData 结果数据
     * @return 协议处理结果
     */
    protected ProtocolProcessResult createSuccessResult(String message, java.util.Map<String, Object> resultData) {
        return ProtocolProcessResult.builder()
                .success(true)
                .businessType(protocolType)
                .message(message)
                .resultData(resultData)
                .build();
    }

    /**
     * 创建失败结果
     *
     * @param errorCode 错误代码
     * @param message 失败消息
     * @return 协议处理结果
     */
    protected ProtocolProcessResult createFailureResult(String errorCode, String message) {
        return ProtocolProcessResult.builder()
                .success(false)
                .businessType(protocolType)
                .errorCode(errorCode)
                .message(message)
                .build();
    }

    /**
     * 获取协议类型
     *
     * @return 协议类型
     */
    public String getProtocolType() {
        return protocolType;
    }
}