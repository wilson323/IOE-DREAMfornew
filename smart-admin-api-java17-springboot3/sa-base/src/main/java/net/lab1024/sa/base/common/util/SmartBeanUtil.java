package net.lab1024.sa.base.common.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.springframework.beans.BeanUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Bean转换工具类
 *
 * @Author 1024创新实验室-创始人:卓大
 * @Date 2018-01-15 10:48:23
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright  <a href"https://1024lab.net">1024创新实验室</a>
 */
public class SmartBeanUtil {

    /**
     * 验证器
     */
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    /**
     * 拷贝bean属性
     *
     * @param source 源对象，需要拷贝属性的对象
     * @param target 目标对象，被拷贝属性的对象
     */
    public static void copyProperties(Object source, Object target) {
        BeanUtils.copyProperties(source, target);
    }

    /**
     * 拷贝对象
     *
     * @param source 源对象，需要拷贝属性的对象
     * @param target 目标类型，被拷贝属性的对象
     * @param <T>
     * @return
     */
    public static <T> T copy(Object source, Class<T> target) {
        if (source == null || target == null) {
            return null;
        }
        try {
            T newInstance = target.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, newInstance);
            return newInstance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 拷贝list
     *
     * @param source
     * @param target
     * @param <T>
     * @param <K>
     * @return
     */
    public static <T, K> List<K> copyList(List<T> source, Class<K> target) {
        if (null == source || source.isEmpty()) {
            return Collections.emptyList();
        }
        return source.stream().map(e -> copy(e, target)).collect(Collectors.toList());
    }

    /**
     * 验证对象 Model格式（常用）
     * 使用hibernate-validator 验证注解
     *
     * @param t
     * @return String 不为null时表示验证失败信息
     */
    public static <T> String verify(T t) {
        // 执行验证
        Set<ConstraintViolation<T>> validate = VALIDATOR.validate(t);
        if (validate.isEmpty()) {
            // 验证通过
            return null;
        }
        // 收集失败信息
        List<String> messageList = validate.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList());
        return messageList.toString();
    }
}