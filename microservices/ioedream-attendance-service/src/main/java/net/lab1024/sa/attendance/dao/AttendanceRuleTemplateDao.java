package net.lab1024.sa.attendance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.attendance.AttendanceRuleTemplateEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 考勤规则模板DAO
 * <p>
 * 负责考勤规则模板（t_attendance_rule_template）的数据访问。
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-26
 */
@Mapper
public interface AttendanceRuleTemplateDao extends BaseMapper<AttendanceRuleTemplateEntity> {

    /**
     * 查询系统模板列表
     *
     * @param category 模板分类（可选）
     * @return 系统模板列表
     */
    List<AttendanceRuleTemplateEntity> selectSystemTemplates(@Param("category") String category);

    /**
     * 查询用户自定义模板列表
     *
     * @param userId 用户ID
     * @param category 模板分类（可选）
     * @return 用户模板列表
     */
    List<AttendanceRuleTemplateEntity> selectUserTemplates(@Param("userId") Long userId, @Param("category") String category);

    /**
     * 根据模板编码查询模板
     *
     * @param templateCode 模板编码
     * @return 模板实体
     */
    AttendanceRuleTemplateEntity selectByTemplateCode(@Param("templateCode") String templateCode);

    /**
     * 更新模板使用次数
     *
     * @param templateId 模板ID
     * @return 更新行数
     */
    int incrementUseCount(@Param("templateId") Long templateId);
}
