package net.lab1024.sa.consume.exception;

import lombok.Getter;

/**
 * 双写验证异常
 *
 * 当新旧表数据不一致时抛出此异常
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-23
 */
@Getter
public class DualWriteException extends RuntimeException {

    private final String dataType;
    private final Long dataId;

    public DualWriteException(String dataType, Long dataId, String message) {
        super(message);
        this.dataType = dataType;
        this.dataId = dataId;
    }

    public DualWriteException(String dataType, Long dataId, String message, Throwable cause) {
        super(message, cause);
        this.dataType = dataType;
        this.dataId = dataId;
    }
}
