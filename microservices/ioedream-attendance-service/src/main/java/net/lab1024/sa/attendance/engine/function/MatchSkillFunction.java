package net.lab1024.sa.attendance.engine.function;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.List;
import java.util.Map;

/**
 * 技能匹配函数
 * <p>
 * 使用示例：
 * - match_skill(employeeId, requiredSkills)
 * - match_skill(123, ["Java", "Spring"])
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public class MatchSkillFunction extends AbstractFunction {

    @Override
    public String getName() {
        return "match_skill";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        // 获取员工ID
        Number employeeIdObj = (Number) arg1.getValue(env);
        Long employeeId = employeeIdObj != null ? employeeIdObj.longValue() : null;

        // 获取所需技能列表
        @SuppressWarnings("unchecked")
        List<String> requiredSkills = (List<String>) arg2.getValue(env);

        // 从上下文中获取员工技能
        @SuppressWarnings("unchecked")
        Map<Long, List<String>> employeeSkillsMap = (Map<Long, List<String>>) env.get("employeeSkills");

        if (employeeId == null || requiredSkills == null || requiredSkills.isEmpty()) {
            return AviatorBoolean.valueOf(false);
        }

        if (employeeSkillsMap == null) {
            return AviatorBoolean.valueOf(false);
        }

        List<String> employeeSkills = employeeSkillsMap.get(employeeId);
        if (employeeSkills == null || employeeSkills.isEmpty()) {
            return AviatorBoolean.valueOf(false);
        }

        // 检查员工是否具备所有所需技能
        boolean hasAllSkills = employeeSkills.containsAll(requiredSkills);

        return AviatorBoolean.valueOf(hasAllSkills);
    }
}
