package net.lab1024.sa.base.common.validator.enumeration;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 枚举值校验器
 */
public class EnumValidator implements ConstraintValidator<CheckEnum, Object> {

    private List<Object> enumValList;
    private boolean required;

    @Override
    public void initialize(CheckEnum constraintAnnotation) {
        required = constraintAnnotation.required();
        Class<? extends BaseEnum> enumClass = constraintAnnotation.value();
        enumValList = Stream.of(enumClass.getEnumConstants())
                .map(BaseEnum::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return !required;
        }
        if (value instanceof List<?>) {
            return checkList((List<?>) value);
        }
        return enumValList.contains(value);
    }

    private boolean checkList(List<?> list) {
        if (required && list.isEmpty()) {
            return false;
        }
        long count = list.stream().distinct().count();
        if (count != list.size()) {
            return false;
        }
        return enumValList.containsAll(list);
    }
}


