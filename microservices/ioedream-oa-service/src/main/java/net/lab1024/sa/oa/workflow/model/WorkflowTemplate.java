package net.lab1024.sa.oa.workflow.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * 工作流模板
 * <p>
 * 定义可重用的工作流模板
 * 支持参数化和动态配置
 * 提供标准化流程定义能力
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Data
@Accessors(chain = true)
public class WorkflowTemplate {

    /**
     * 模板ID
     */
    private String id;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 模板版本
     */
    private String version = "1.0";

    /**
     * 模板分类
     */
    private String category;

    /**
     * 模板节点列表
     */
    private List<TemplateNode> nodes;

    /**
     * 全局配置
     */
    private Map<String, Object> globalConfig;

    /**
     * 参数定义
     */
    private Map<String, ParameterDefinition> parameters;

    /**
     * 是否启用
     */
    private Boolean enabled = true;

    /**
     * 创建时间
     */
    private java.time.LocalDateTime createTime;

    /**
     * 更新时间
     */
    private java.time.LocalDateTime updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 添加节点
     */
    public WorkflowTemplate addNode(TemplateNode node) {
        if (nodes == null) {
            nodes = new java.util.ArrayList<>();
        }
        nodes.add(node);
        return this;
    }

    /**
     * 添加参数定义
     */
    public WorkflowTemplate addParameter(String name, String type, String description, Object defaultValue) {
        if (parameters == null) {
            parameters = new java.util.HashMap<>();
        }
        parameters.put(name, new ParameterDefinition()
                .setName(name)
                .setType(type)
                .setDescription(description)
                .setDefaultValue(defaultValue));
        return this;
    }

    /**
     * 模板节点
     */
    @Data
    @Accessors(chain = true)
    public static class TemplateNode {
        /**
         * 节点ID
         */
        private String id;

        /**
         * 节点名称
         */
        private String name;

        /**
         * 节点类型
         */
        private String type;

        /**
         * 节点描述
         */
        private String description;

        /**
         * 节点配置
         */
        private Map<String, Object> config;

        /**
         * 是否异步执行
         */
        private Boolean async = false;

        /**
         * 超时时间（秒）
         */
        private Integer timeoutSeconds;

        /**
         * 出错时是否继续
         */
        private Boolean continueOnError = false;

        /**
         * 添加配置
         */
        public TemplateNode addConfig(String key, Object value) {
            if (config == null) {
                config = new java.util.HashMap<>();
            }
            config.put(key, value);
            return this;
        }
    }

    /**
     * 参数定义
     */
    @Data
    @Accessors(chain = true)
    public static class ParameterDefinition {
        /**
         * 参数名称
         */
        private String name;

        /**
         * 参数类型
         */
        private String type;

        /**
         * 参数描述
         */
        private String description;

        /**
         * 默认值
         */
        private Object defaultValue;

        /**
         * 是否必需
         */
        private Boolean required = false;

        /**
         * 验证规则
         */
        private String validationRule;

        /**
         * 选项列表（用于枚举类型）
         */
        private List<OptionDefinition> options;

        /**
         * 添加选项
         */
        public ParameterDefinition addOption(String value, String label) {
            if (options == null) {
                options = new java.util.ArrayList<>();
            }
            options.add(new OptionDefinition().setValue(value).setLabel(label));
            return this;
        }

        /**
         * 选项定义
         */
        @Data
        @Accessors(chain = true)
        public static class OptionDefinition {
            private String value;
            private String label;
        }
    }
}



