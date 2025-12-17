package net.lab1024.sa.common.util;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Bean工具类
 * <p>
 * 提供Bean对象的复制、转换、属性处理等功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
public class SmartBeanUtil {

    private SmartBeanUtil() {
        // 私有构造，禁止实例化
    }

    /**
     * 对象属性复制
     *
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copyProperties(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        BeanUtils.copyProperties(source, target);
    }

    /**
     * 对象属性复制（忽略null值）
     *
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copyPropertiesIgnoreNull(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
    }

    /**
     * 对象转换
     *
     * @param source 源对象
     * @param targetClass 目标类型
     * @param <T> 目标类型
     * @return 目标对象
     */
    public static <T> T copy(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("Bean copy failed", e);
        }
    }

    /**
     * 列表转换
     *
     * @param sourceList 源列表
     * @param targetClass 目标类型
     * @param <S> 源类型
     * @param <T> 目标类型
     * @return 目标列表
     */
    public static <S, T> List<T> copyList(List<S> sourceList, Class<T> targetClass) {
        if (sourceList == null || sourceList.isEmpty()) {
            return new ArrayList<>();
        }
        return sourceList.stream()
                .map(source -> copy(source, targetClass))
                .collect(Collectors.toList());
    }

    /**
     * 获取对象中值为null的属性名称
     *
     * @param source 源对象
     * @return null值属性名称数组
     */
    public static String[] getNullPropertyNames(Object source) {
        BeanWrapper wrapper = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = wrapper.getPropertyDescriptors();
        Set<String> emptyNames = new HashSet<>();
        for (PropertyDescriptor pd : pds) {
            Object srcValue = wrapper.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }
        return emptyNames.toArray(new String[0]);
    }

    /**
     * 对象转Map
     *
     * @param obj 对象
     * @return Map
     */
    public static Map<String, Object> toMap(Object obj) {
        if (obj == null) {
            return new HashMap<>();
        }
        Map<String, Object> map = new HashMap<>();
        BeanWrapper wrapper = new BeanWrapperImpl(obj);
        PropertyDescriptor[] pds = wrapper.getPropertyDescriptors();
        for (PropertyDescriptor pd : pds) {
            String name = pd.getName();
            if (!"class".equals(name)) {
                Object value = wrapper.getPropertyValue(name);
                if (value != null) {
                    map.put(name, value);
                }
            }
        }
        return map;
    }

    /**
     * Map转对象
     *
     * @param map Map
     * @param targetClass 目标类型
     * @param <T> 目标类型
     * @return 目标对象
     */
    public static <T> T fromMap(Map<String, Object> map, Class<T> targetClass) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanWrapper wrapper = new BeanWrapperImpl(target);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (wrapper.isWritableProperty(entry.getKey())) {
                    wrapper.setPropertyValue(entry.getKey(), entry.getValue());
                }
            }
            return target;
        } catch (Exception e) {
            throw new RuntimeException("Map to bean failed", e);
        }
    }
}
