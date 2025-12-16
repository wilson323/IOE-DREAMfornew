package net.lab1024.sa.video.sdk;

import lombok.Data;

/**
 * 活体检测结果
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Data
public class LivenessResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 是否为活体
     */
    private boolean isLive;

    /**
     * 活体置信度
     */
    private double livenessScore;

    /**
     * 攻击类型：NONE, PHOTO, VIDEO, MASK, 3D_MODEL
     */
    private String attackType;

    /**
     * 处理耗时（毫秒）
     */
    private long costTime;
}
