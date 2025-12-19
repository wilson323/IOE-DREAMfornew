package net.lab1024.sa.device.comm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 协议配置实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
@TableName("t_device_protocol_config")
public class ProtocolConfigEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 协议类型 */
    @TableField("protocol_type")
    private String protocolType;

    /** 协议版本 */
    @TableField("protocol_version")
    private String protocolVersion;

    /** 适配器类名 */
    @TableField("adapter_class")
    private String adapterClass;

    /** 配置参数(JSON) */
    @TableField("config_params")
    private String configParams;

    /** 是否启用 */
    @TableField("enabled")
    private Boolean enabled;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 删除标记 */
    @TableLogic
    @TableField("deleted_flag")
    private Boolean deletedFlag;
}
