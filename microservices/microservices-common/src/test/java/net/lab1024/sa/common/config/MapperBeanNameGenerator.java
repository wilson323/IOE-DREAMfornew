package net.lab1024.sa.common.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.lang.NonNull;

/**
 * Mapper Bean名称生成器
 * <p>
 * 解决同名DAO接口的Bean名称冲突问题
 * 生成规则：
 * - 对于冲突的DAO（UserDao、EmployeeDao），使用模块前缀生成唯一名称
 * - 其他DAO使用默认命名规则
 * </p>
 * <p>
 * Bean名称映射规则：
 * - net.lab1024.sa.common.auth.dao.UserDao -> authUserDao
 * - net.lab1024.sa.common.security.dao.UserDao -> securityUserDao
 * - net.lab1024.sa.common.hr.dao.EmployeeDao -> hrEmployeeDao
 * - net.lab1024.sa.common.system.employee.dao.EmployeeDao -> systemEmployeeDao
 * - 其他DAO -> 默认名称（类名首字母小写）
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-05
 */
public class MapperBeanNameGenerator extends AnnotationBeanNameGenerator {

    @Override
    @NonNull
    public String generateBeanName(@NonNull BeanDefinition definition, @NonNull BeanDefinitionRegistry registry) {
        String defaultName = super.generateBeanName(definition, registry);

        // 获取Bean的类名
        String className = definition.getBeanClassName();
        if (className == null) {
            return defaultName;
        }

        // 检查是否是冲突的DAO（UserDao或EmployeeDao）
        String[] parts = className.split("\\.");
        if (parts.length < 2) {
            return defaultName;
        }

        String simpleClassName = parts[parts.length - 1];

        // 只处理冲突的DAO：UserDao和EmployeeDao
        if (!"UserDao".equals(simpleClassName) && !"EmployeeDao".equals(simpleClassName)) {
            return defaultName; // 其他DAO使用默认命名
        }

        // 找到"dao"包的位置
        int daoIndex = -1;
        for (int i = 0; i < parts.length; i++) {
            if ("dao".equals(parts[i])) {
                daoIndex = i;
                break;
            }
        }

        if (daoIndex > 0 && daoIndex < parts.length - 1) {
            // 提取dao前面的最后一个包名（模块名）
            // 跳过 net.lab1024.sa.common (索引0-3)
            int startIndex = 4; // 跳过 net.lab1024.sa.common

            if (daoIndex > startIndex) {
                // 取dao前面的最后一个包名作为模块前缀
                String moduleName = parts[daoIndex - 1];

                // 生成唯一Bean名称：模块名 + 类名（首字母小写）
                String beanName = moduleName + simpleClassName;
                return Character.toLowerCase(beanName.charAt(0)) + beanName.substring(1);
            }
        }

        return defaultName;
    }
}
