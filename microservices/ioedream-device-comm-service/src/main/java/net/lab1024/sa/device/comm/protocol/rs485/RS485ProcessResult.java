package net.lab1024.sa.device.comm.protocol.rs485;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * RS485消息处理结果
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
public class RS485ProcessResult {
    /** 是否成功 */
    private boolean success;
    /** 设备ID */
    private Long deviceId;
    /** 消息类型 */
    private String messageType;
    /** 处理时间 */
    private LocalDateTime processTime;
    /** 响应数据 */
    private Map<String, Object> responseData;
    /** 错误消息 */
    private String errorMessage;
    /** 错误代码 */
    private String errorCode;
    /** 消息 */
    private String message;

    public static RS485ProcessResult success(Long deviceId, String messageType, Map<String, Object> data) {
        RS485ProcessResult result = new RS485ProcessResult();
        result.setSuccess(true);
        result.setDeviceId(deviceId);
        result.setMessageType(messageType);
        result.setResponseData(data);
        result.setProcessTime(LocalDateTime.now());
        return result;
    }

    public static RS485ProcessResult failure(String errorCode, String errorMessage) {
        RS485ProcessResult result = new RS485ProcessResult();
        result.setSuccess(false);
        result.setErrorCode(errorCode);
        result.setErrorMessage(errorMessage);
        return result;
    }
}
