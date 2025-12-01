package net.lab1024.sa.access.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 生物识别数据实体类
 * <p>
 * 作为BiometricTemplateEntity的别名，用于移动端API的返回对象
 * 提供生物识别相关的数据访问接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BiometricDataEntity extends BiometricTemplateEntity {
    // 继承所有BiometricTemplateEntity的字段和方法
    // 可以根据需要添加移动端特定的字段或方法
}
