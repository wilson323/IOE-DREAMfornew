package net.lab1024.sa.visitor.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * VisitorSearchVO
 */
@Data
@Schema(description = "VisitorSearchVO")
public class VisitorSearchVO {
    @Schema(description = "字段")
    private String field;
}
