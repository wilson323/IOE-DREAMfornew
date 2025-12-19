package net.lab1024.sa.device.comm.protocol.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 设备响应对象
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceResponse {

    /**
     * 响应是否成功
     */
    private boolean success;

    /**
     * 响应代码
     */
    private String responseCode;

    /**
     * 响应消息
     */
    private String responseMessage;

    /**
     * 响应数据
     */
    private Map<String, Object> data;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 错误消息
     */
    private String errorMessage;

    /**
     * 状态码(兼容旧代码)
     */
    private String code;

    /**
     * 消息(兼容旧代码)
     */
    private String message;

    /**
     * 时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 获取状态（别名）
     */
    public String getStatus() {
        return this.responseCode;
    }

    public void setStatus(String status) {
        this.responseCode = status;
        this.code = status;
    }

    public static DeviceResponse success(Map<String, Object> data) {
        return DeviceResponse.builder()
                .success(true)
                .responseCode("200")
                .responseMessage("成功")
                .data(data)
                .build();
    }

    public static DeviceResponse failure(String errorCode, String errorMessage) {
        return DeviceResponse.builder()
                .success(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }
}
