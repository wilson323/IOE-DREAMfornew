package net.lab1024.sa.oa.workflow.form.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 表单组件
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormComponent {

    /**
     * 组件ID
     */
    @NotBlank(message = "组件ID不能为空")
    private String componentId;

    /**
     * 组件类型
     * - 基础组件：input, textarea, number, password, select, checkbox, radio, date, time, file, image
     * - 高级组件：richtext, codeeditor, jsoneditor, treeselect, cascader, transfer, slider, rate, colorpicker, switch
     * - 业务组件：userselect, deptselect, employeeselect, areaselect, deviceSelect, orgselect
     */
    @NotBlank(message = "组件类型不能为空")
    private String componentType;

    /**
     * 组件分类（basic, advanced, business）
     */
    @NotBlank(message = "组件分类不能为空")
    private String category;

    /**
     * 字段名称
     */
    @NotBlank(message = "字段名称不能为空")
    private String fieldName;

    /**
     * 字标签
     */
    @NotBlank(message = "字段标签不能为空")
    private String label;

    /**
     * 占位符
     */
    private String placeholder;

    /**
     * 默认值
     */
    private Object defaultValue;

    /**
     * 组件配置
     */
    private ComponentConfig config;

    /**
     * 验证规则列表
     */
    private List<ValidationRuleConfig> validationRules;

    /**
     * 组件属性
     */
    private Map<String, Object> properties;

    /**
     * 组件样式
     */
    private Map<String, Object> style;

    /**
     * 组件CSS类
     */
    private List<String> cssClass;

    /**
     * 是否必填
     */
    @Builder.Default
    private Boolean required = false;

    /**
     * 是否禁用
     */
    @Builder.Default
    private Boolean disabled = false;

    /**
     * 是否只读
     */
    @Builder.Default
    private Boolean readonly = false;

    /**
     * 是否隐藏
     */
    @Builder.Default
    private Boolean hidden = false;

    /**
     * 占据的列数（24栅格系统）
     */
    @Builder.Default
    private Integer span = 24;

    /**
     * 偏移量
     */
    @Builder.Default
    private Integer offset = 0;

    /**
     * 组件顺序
     */
    @NotNull(message = "组件顺序不能为空")
    private Integer sortOrder;

    /**
     * 组件配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComponentConfig {
        /**
         * 选项列表（select, radio, checkbox）
         */
        private List<OptionItem> options;

        /**
         * 多选（select, treeselect, cascader）
         */
        private Boolean multiple;

        /**
         * 可搜索（select, treeselect）
         */
        private Boolean filterable;

        /**
         * 可清除
         */
        private Boolean clearable;

        /**
         * 最小值（number, slider）
         */
        private Double min;

        /**
         * 最大值（number, slider）
         */
        private Double max;

        /**
         * 步长（number, slider）
         */
        private Double step;

        /**
         * 小数位数（number）
         */
        private Integer precision;

        /**
         * 最小长度（input, textarea）
         */
        private Integer minLength;

        /**
         * 最大长度（input, textarea）
         */
        private Integer maxLength;

        /**
         * 正则表达式（input）
         */
        private String pattern;

        /**
         * 日期格式（date, time）
         */
        private String format;

        /**
         * 显示格式（date, time）
         */
        private String displayFormat;

        /**
         * 最小日期（date）
         */
        private String minDate;

        /**
         * 最大日期（date）
         */
        private String maxDate;

        /**
         * 文件类型（file）
         */
        private List<String> accept;

        /**
         * 最大文件大小（MB）
         */
        private Integer maxFileSize;

        /**
         * 最大文件数量
         */
        private Integer maxFileCount;

        /**
         * 图片裁剪（image）
         */
        private Boolean crop;

        /**
         * 图片压缩（image）
         */
        private Boolean compress;

        /**
         * 富文本工具栏配置（richtext）
         */
        private List<String> toolbar;

        /**
         * 代码编辑器语言（codeeditor）
         */
        private String language;

        /**
         * 代码编辑器主题（codeeditor）
         */
        private String theme;

        /**
         * 树形数据源（treeselect）
         */
        private List<TreeNode> treeData;

        /**
         * 级联数据源（cascader）
         */
        private List<CascaderItem> cascaderData;

        /**
         * 穿梭框数据源（transfer）
         */
        private List<TransferItem> transferData;

        /**
         * 滑块标记（slider）
         */
        private List<SliderMark> marks;

        /**
         * 评分星级（rate）
         */
        private Integer maxStar;

        /**
         * 是否允许半星（rate）
         */
        private Boolean allowHalf;

        /**
         * 颜色选择器格式（colorpicker）
         */
        private String colorFormat;

        /**
         * 开关尺寸（switch）
         */
        private String switchSize;

        /**
         * 开关打开值（switch）
         */
        private String activeValue;

        /**
         * 开关关闭值（switch）
         */
        private String inactiveValue;

        /**
         * 开关打开文本（switch）
         */
        private String activeText;

        /**
         * 开关关闭文本（switch）
         */
        private String inactiveText;
    }

    /**
     * 选项项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionItem {
        /**
         * 选项值
         */
        private String value;

        /**
         * 选项标签
         */
        private String label;

        /**
         * 是否禁用
         */
        private Boolean disabled;

        /**
         * 图标
         */
        private String icon;

        /**
         * 扩展属性
         */
        private Map<String, Object> extra;
    }

    /**
     * 树节点
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TreeNode {
        /**
         * 节点ID
         */
        private String id;

        /**
         * 节点标签
         */
        private String label;

        /**
         * 父节点ID
         */
        private String parentId;

        /**
         * 是否禁用
         */
        private Boolean disabled;

        /**
         * 子节点列表
         */
        private List<TreeNode> children;
    }

    /**
     * 级联项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CascaderItem {
        /**
         * 项值
         */
        private String value;

        /**
         * 项标签
         */
        private String label;

        /**
         * 子项列表
         */
        private List<CascaderItem> children;
    }

    /**
     * 穿梭框项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransferItem {
        /**
         * 项key
         */
        private String key;

        /**
         * 项标签
         */
        private String label;

        /**
         * 是否禁用
         */
        private Boolean disabled;
    }

    /**
     * 滑块标记
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SliderMark {
        /**
         * 标记值
         */
        private Double value;

        /**
         * 标记标签
         */
        private String label;
    }
}
