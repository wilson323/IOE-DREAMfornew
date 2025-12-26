package net.lab1024.sa.access.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 离线同步数据VO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-24
 */
@Data
@Schema(description = "离线同步数据VO")
public class OfflineSyncDataVO {

    @Schema(description = "同步时间戳")
    private Long syncTimestamp;

    @Schema(description = "同步时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime syncTime;

    @Schema(description = "是否需要全量同步")
    private Boolean fullSync;

    @Schema(description = "权限数据")
    private List<AccessPermissionVO> permissions;

    @Schema(description = "权限数据版本")
    private Long dataVersion;

    @Schema(description = "删除的权限ID列表")
    private List<Long> deletedPermissionIds;

    @Schema(description = "数据变更起始时间戳")
    private Long changeSinceTimestamp;

    @Schema(description = "数据变更结束时间戳")
    private Long changeUntilTimestamp;
}
