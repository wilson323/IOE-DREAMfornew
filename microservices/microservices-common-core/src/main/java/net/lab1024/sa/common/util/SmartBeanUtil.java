package net.lab1024.sa.common.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 智能Bean拷贝工具类
 * <p>
 * 提供对象拷贝的便捷方法（纯Java实现，无Spring依赖）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public class SmartBeanUtil {

    /**
     * 拷贝单个对象
     *
     * @param source 源对象
     * @param targetClass 目标类
     * @param <T> 目标类型
     * @return 目标对象
     */
    public static <T> T copy(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("对象拷贝失败: " + e.getMessage(), e);
        }
    }

    /**
     * 拷贝列表
     *
     * @param sourceList 源列表
     * @param targetClass 目标类
     * @param <T> 目标类型
     * @return 目标列表
     */
    public static <T> List<T> copyList(List<?> sourceList, Class<T> targetClass) {
        if (sourceList == null || sourceList.isEmpty()) {
            return new ArrayList<>();
        }
        List<T> targetList = new ArrayList<>(sourceList.size());
        for (Object source : sourceList) {
            T target = copy(source, targetClass);
            if (target != null) {
                targetList.add(target);
            }
        }
        return targetList;
    }

    /**
     * 拷贝对象属性
     */
    private static void copyProperties(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }

        Class<?> sourceClass = source.getClass();
        Class<?> targetClass = target.getClass();

        Field[] sourceFields = sourceClass.getDeclaredFields();
        for (Field sourceField : sourceFields) {
            try {
                Field targetField = findField(targetClass, sourceField.getName());
                if (targetField != null && sourceField.getType().equals(targetField.getType())) {
                    sourceField.setAccessible(true);
                    targetField.setAccessible(true);
                    Object value = sourceField.get(source);
                    targetField.set(target, value);
                }
            } catch (IllegalAccessException e) {
                // 忽略无法访问的字段
            }
        }
    }

    /**
     * 查找字段（包括父类）
     */
    private static Field findField(Class<?> clazz, String fieldName) {
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            try {
                return currentClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                currentClass = currentClass.getSuperclass();
            }
        }
        return null;
    }
}
