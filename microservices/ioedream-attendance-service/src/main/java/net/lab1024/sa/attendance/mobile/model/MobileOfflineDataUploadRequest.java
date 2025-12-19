package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 移动端离线数据上传请求
 * <p>
 * 封装移动端离线数据上传的请求参数
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "移动端离线数据上传请求")
public class MobileOfflineDataUploadRequest {

    /**
     * 离线记录列表
     */
    @NotEmpty(message = "离线记录列表不能为空")
    @Schema(description = "离线记录列表")
    private List<MobileOfflineRecord> offlineRecords;
}


