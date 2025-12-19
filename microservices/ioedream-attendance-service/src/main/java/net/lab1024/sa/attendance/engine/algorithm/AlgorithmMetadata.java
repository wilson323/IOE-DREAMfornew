package net.lab1024.sa.attendance.engine.algorithm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 算法元数据
 * <p>
 * 封装算法的元数据信息
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlgorithmMetadata {

    /**
     * 算法名称
     */
    private String name;

    /**
     * 算法版本
     */
    private String version;

    /**
     * 算法描述
     */
    private String description;

    /**
     * 算法作者
     */
    private String author;

    /**
     * 创建日期
     */
    private LocalDateTime createdDate;

    /**
     * 算法参数列表
     */
    private List<AlgorithmParameter> parameters;

    /**
     * 算法参数
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlgorithmParameter {
        private String name;
        private String displayName;
        private String type;
        private Object defaultValue;
        private Object minValue;
        private Object maxValue;
        private String description;
        private Boolean required;
    }
}


