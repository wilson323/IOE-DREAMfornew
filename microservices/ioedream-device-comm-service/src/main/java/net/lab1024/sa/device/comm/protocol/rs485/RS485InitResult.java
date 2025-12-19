package net.lab1024.sa.device.comm.protocol.rs485;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * RS485设备初始化结果
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
public class RS485InitResult {
    /** 是否成功 */
    private boolean success;
    /** 设备ID */
    private Long deviceId;
    /** 设备序列号 */
    private String serialNumber;
    /** 协议版本 */
    private String protocolVersion;
    /** 初始化时间 */
    private LocalDateTime initTime;
    /** 错误消息 */
    private String errorMessage;
    /** 错误代码 */
    private String errorCode;
    /** 消息 */
    private String message;

    public static RS485InitResult success(Long deviceId, String serialNumber) {
        RS485InitResult result = new RS485InitResult();
        result.setSuccess(true);
        result.setDeviceId(deviceId);
        result.setSerialNumber(serialNumber);
        result.setInitTime(LocalDateTime.now());
        return result;
    }

    public static RS485InitResult failure(String errorCode, String errorMessage) {
        RS485InitResult result = new RS485InitResult();
        result.setSuccess(false);
        result.setErrorCode(errorCode);
        result.setErrorMessage(errorMessage);
        return result;
    }
}
