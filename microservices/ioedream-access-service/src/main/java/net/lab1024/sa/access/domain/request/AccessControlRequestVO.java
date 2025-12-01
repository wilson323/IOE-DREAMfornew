package net.lab1024.sa.access.domain.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 门禁控制请求VO
 * <p>
 * 用于智能门禁验证的请求对象
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@Data
public class AccessControlRequestVO {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 设备ID
     */
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    /**
     * 访问类型
     */
    private String accessType;

    /**
     * 验证方式
     */
    private String verifyMethod;

    /**
     * 验证数据
     */
    private String verifyData;

    /**
     * 访问时间
     */
    private LocalDateTime accessTime;

    /**
     * 访问区域ID
     */
    private Long areaId;

    /**
     * 附加信息（JSON格式）
     */
    private String extraInfo;
}
