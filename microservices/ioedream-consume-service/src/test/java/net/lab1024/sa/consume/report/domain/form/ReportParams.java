package net.lab1024.sa.consume.report.domain.form;

import java.time.LocalDateTime;

/**
 * 测试用报表参数（最小实现）
 * <p>
 * 仅用于 consume-service 测试编译与单元测试场景。
 * </p>
 */
public class ReportParams {

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
