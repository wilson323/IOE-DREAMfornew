package net.lab1024.sa.common.system.domain.dto;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 系统配置创建DTO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-21
 */
@Data
@Schema(description = "系统配置创建DTO")
public class ConfigCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "配置键", required = true, example = "system.name")
    @NotBlank(message = "配置键不能为空")
    private String configKey;

    @Schema(description = "配置值", example = "IOE-DREAM")
    private String configValue;

    @Schema(description = "配置名称", example = "系统名称")
    private String configName;

    @Schema(description = "配置描述", example = "系统名称")
    private String configDesc;

    @Schema(description = "配置类型", example = "STRING")
    private String configType;

    @Schema(description = "是否加密", example = "0")
    private Integer isEncrypted;
}
