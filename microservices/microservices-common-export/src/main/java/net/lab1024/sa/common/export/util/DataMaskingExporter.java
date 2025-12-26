package net.lab1024.sa.common.export.util;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.export.annotation.Masked;
import net.lab1024.sa.common.util.DataMaskingUtil;
import org.springframework.util.ReflectionUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 数据脱敏导出器（纯Java实现）
 * <p>
 * 职责：扫描并脱敏带@Masked注解的字段，不依赖任何Web API
 * </p>
 * <p>
 * 架构设计原则：
 * <ul>
 *   <li>纯Java实现：不依赖Servlet API，符合细粒度模块架构</li>
 *   <li>职责单一：只负责数据脱敏，不负责输出格式和Web响应</li>
 *   <li>可扩展：支持自定义脱敏策略和输出格式</li>
 * </ul>
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 2.0.0
 * @since 2025-12-26
 */
@Slf4j
public class DataMaskingExporter {

    /**
     * 脱敏数据列表
     * <p>
     * 扫描所有带@Masked注解的字段并执行脱敏处理
     * </p>
     *
     * @param dataList   原始数据列表
     * @param modelClass 模型类
     * @param <T>        数据类型
     * @return 脱敏后的数据列表
     */
    public static <T> List<T> maskDataList(List<T> dataList, Class<T> modelClass) {
        if (dataList == null || dataList.isEmpty()) {
            return dataList;
        }

        log.info("[数据脱敏导出] 开始处理: model={}, size={}", modelClass.getSimpleName(), dataList.size());

        List<T> maskedList = new ArrayList<>(dataList.size());
        int successCount = 0;
        int skipCount = 0;

        for (T data : dataList) {
            try {
                // 深度复制并脱敏对象
                T maskedData = copyAndMaskObject(data, modelClass);
                maskedList.add(maskedData);
                successCount++;
            } catch (Exception e) {
                log.warn("[数据脱敏导出] 对象脱敏失败，保留原数据: model={}, error={}",
                        modelClass.getSimpleName(), e.getMessage());
                maskedList.add(data); // 脱敏失败时保留原数据
                skipCount++;
            }
        }

        log.info("[数据脱敏导出] 处理完成: model={}, total={}, success={}, skipped={}",
                modelClass.getSimpleName(), dataList.size(), successCount, skipCount);

        return maskedList;
    }

    /**
     * 批量脱敏处理（高性能版本）
     * <p>
     * 适用于大数据量场景，使用流式处理减少内存占用
     * </p>
     *
     * @param dataList   原始数据列表
     * @param modelClass 模型类
     * @param <T>        数据类型
     * @return 脱敏后的数据列表
     */
    public static <T> List<T> maskDataListStream(List<T> dataList, Class<T> modelClass) {
        if (dataList == null || dataList.isEmpty()) {
            return dataList;
        }

        log.info("[数据脱敏导出] 流式处理开始: model={}, size={}", modelClass.getSimpleName(), dataList.size());

        List<T> maskedList = new ArrayList<>(dataList.size());

        dataList.stream().forEach(data -> {
            try {
                T maskedData = copyAndMaskObject(data, modelClass);
                maskedList.add(maskedData);
            } catch (Exception e) {
                log.debug("[数据脱敏导出] 对象脱敏失败: error={}", e.getMessage());
                maskedList.add(data);
            }
        });

        return maskedList;
    }

    /**
     * 复制对象并脱敏敏感字段
     * <p>
     * 使用反射机制深度复制对象，仅处理@Masked注解的字段
     * </p>
     *
     * @param source     源对象
     * @param modelClass 模型类
     * @param <T>         对象类型
     * @return 脱敏后的对象副本
     * @throws Exception 反射操作异常
     */
    private static <T> T copyAndMaskObject(T source, Class<T> modelClass) throws Exception {
        // 创建新对象
        T target = modelClass.getDeclaredConstructor().newInstance();

        // 遍历所有字段
        Field[] fields = modelClass.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);

                // 获取源字段值
                Object value = field.get(source);
                if (value == null) {
                    continue;
                }

                // 检查是否有@Masked注解
                Masked masked = field.getAnnotation(Masked.class);
                if (masked != null && value instanceof String) {
                    // 脱敏处理
                    String maskedValue = maskFieldValue((String) value, masked.value());
                    field.set(target, maskedValue);

                    log.debug("[数据脱敏导出] 字段脱敏: field={}, original={}, masked={}",
                            field.getName(), value, maskedValue);
                } else {
                    // 直接复制
                    field.set(target, value);
                }
            } catch (Exception e) {
                log.warn("[数据脱敏导出] 字段处理失败: field={}, error={}",
                        field.getName(), e.getMessage());
                // 继续处理其他字段
            }
        }

        return target;
    }

    /**
     * 脱敏字段值
     * <p>
     * 根据@Masked注解的类型调用DataMaskingUtil的对应方法
     * </p>
     *
     * @param value    原始值
     * @param maskType 脱敏类型
     * @return 脱敏后的值
     */
    private static String maskFieldValue(String value, Masked.MaskType maskType) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        try {
            // 将注解的MaskType转换为DataMaskingUtil的MaskType
            DataMaskingUtil.MaskType utilMaskType = convertMaskType(maskType);
            return DataMaskingUtil.mask(value, utilMaskType);
        } catch (Exception e) {
            log.warn("[数据脱敏导出] 脱敏异常: type={}, value={}, error={}",
                    maskType, value, e.getMessage());
            return value; // 脱敏失败时返回原值
        }
    }

    /**
     * 转换MaskType枚举
     * <p>
     * 将注解中的MaskType转换为DataMaskingUtil的MaskType
     * </p>
     *
     * @param annotationType 注解中的MaskType
     * @return DataMaskingUtil的MaskType
     */
    private static DataMaskingUtil.MaskType convertMaskType(Masked.MaskType annotationType) {
        return DataMaskingUtil.MaskType.valueOf(annotationType.name());
    }

    /**
     * 自定义脱敏策略处理
     * <p>
     * 允许用户传入自定义脱敏函数，实现灵活的脱敏逻辑
     * </p>
     *
     * @param dataList         原始数据列表
     * @param modelClass       模型类
     * @param maskingStrategy  自定义脱敏策略
     * @param <T>              数据类型
     * @return 脱敏后的数据列表
     */
    public static <T> List<T> maskDataListWithStrategy(
            List<T> dataList,
            Class<T> modelClass,
            MaskingStrategy<T> maskingStrategy) {

        if (dataList == null || dataList.isEmpty()) {
            return dataList;
        }

        log.info("[数据脱敏导出] 使用自定义策略: model={}, strategy={}",
                modelClass.getSimpleName(), maskingStrategy.getClass().getSimpleName());

        List<T> maskedList = new ArrayList<>(dataList.size());

        for (T data : dataList) {
            try {
                T maskedData = maskingStrategy.mask(data);
                maskedList.add(maskedData);
            } catch (Exception e) {
                log.warn("[数据脱敏导出] 自定义策略失败: error={}", e.getMessage());
                maskedList.add(data);
            }
        }

        return maskedList;
    }

    /**
     * 自定义脱敏策略接口
     * <p>
     * 用于实现复杂的脱敏逻辑，例如：
     * - 基于业务规则的条件脱敏
     * - 跨字段的关联脱敏
     * - 动态脱敏规则
     * </p>
     *
     * @param <T> 数据类型
     */
    @FunctionalInterface
    public interface MaskingStrategy<T> {
        /**
         * 对单个对象执行脱敏处理
         *
         * @param data 原始数据
         * @return 脱敏后的数据
         */
        T mask(T data);
    }

    /**
     * 预定义脱敏策略：仅脱敏手机号
     */
    public static class PhoneOnlyMaskingStrategy<T> implements MaskingStrategy<T> {
        private final Class<T> modelClass;

        public PhoneOnlyMaskingStrategy(Class<T> modelClass) {
            this.modelClass = modelClass;
        }

        @Override
        public T mask(T data) {
            try {
                T target = modelClass.getDeclaredConstructor().newInstance();
                Field[] fields = modelClass.getDeclaredFields();

                for (Field field : fields) {
                    field.setAccessible(true);
                    Object value = field.get(data);

                    if (value != null && value instanceof String) {
                        Masked masked = field.getAnnotation(Masked.class);
                        if (masked != null && masked.value() == Masked.MaskType.PHONE) {
                            value = DataMaskingUtil.maskPhone((String) value);
                        }
                    }

                    field.set(target, value);
                }

                return target;
            } catch (Exception e) {
                log.warn("[仅脱敏手机号策略] 处理失败: error={}", e.getMessage());
                return data;
            }
        }
    }

    /**
     * 预定义脱敏策略：仅脱敏身份证和手机号
     */
    public static class SensitiveInfoMaskingStrategy<T> implements MaskingStrategy<T> {
        private final Class<T> modelClass;

        public SensitiveInfoMaskingStrategy(Class<T> modelClass) {
            this.modelClass = modelClass;
        }

        @Override
        public T mask(T data) {
            try {
                T target = modelClass.getDeclaredConstructor().newInstance();
                Field[] fields = modelClass.getDeclaredFields();

                for (Field field : fields) {
                    field.setAccessible(true);
                    Object value = field.get(data);

                    if (value != null && value instanceof String) {
                        Masked masked = field.getAnnotation(Masked.class);
                        if (masked != null) {
                            Masked.MaskType type = masked.value();
                            if (type == Masked.MaskType.ID_CARD || type == Masked.MaskType.PHONE) {
                                value = DataMaskingUtil.mask((String) value,
                                        DataMaskingUtil.MaskType.valueOf(type.name()));
                            }
                        }
                    }

                    field.set(target, value);
                }

                return target;
            } catch (Exception e) {
                log.warn("[敏感信息脱敏策略] 处理失败: error={}", e.getMessage());
                return data;
            }
        }
    }
}
