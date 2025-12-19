package net.lab1024.sa.access.domain.vo;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 门禁通行事件视图对象
 * <p>
 * 用于实时监控模块显示通行事件
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class AccessEventVO {

    /**
     * 记录ID
     */
    private Long recordId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 区域名称
     */
    private String areaName;

    /**
     * 通行结果
     * <p>
     * 1-成功
     * 2-失败
     * </p>
     */
    private Integer accessResult;

    /**
     * 通行结果名称
     */
    private String accessResultName;

    /**
     * 通行类型
     * <p>
     * IN - 进入
     * OUT - 离开
     * </p>
     */
    private String accessType;

    /**
     * 通行类型名称
     */
    private String accessTypeName;

    /**
     * 验证方式
     * <p>
     * FACE - 人脸
     * FINGERPRINT - 指纹
     * CARD - 卡片
     * PASSWORD - 密码
     * QR_CODE - 二维码
     * </p>
     */
    private String verifyMethod;

    /**
     * 验证方式名称
     */
    private String verifyMethodName;

    /**
     * 通行时间
     */
    private LocalDateTime accessTime;

    /**
     * 抓拍照片路径
     */
    private String photoPath;
}
