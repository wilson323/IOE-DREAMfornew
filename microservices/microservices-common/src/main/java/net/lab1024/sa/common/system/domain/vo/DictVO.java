package net.lab1024.sa.common.system.domain.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 字典VO
 * 整合自ioedream-system-service
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02（整合自system-service）
 */
@Data
public class DictVO {

    private Long dictDataId;
    private Long dictTypeId;
    private String dictTypeCode;
    private String dictLabel;
    private String dictValue;
    private String cssClass;
    private String listClass;
    private Integer isDefault;
    private Integer sortOrder;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
}

