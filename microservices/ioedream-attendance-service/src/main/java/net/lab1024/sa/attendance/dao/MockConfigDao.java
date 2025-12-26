package net.lab1024.sa.attendance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.attendance.MockConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * Mock配置DAO
 * <p>
 * 提供Mock配置的数据访问操作
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Mapper
public interface MockConfigDao extends BaseMapper<MockConfigEntity> {

    /**
     * 查询启用的Mock配置
     *
     * @param configType 配置类型
     * @return Mock配置列表
     */
    @Select("""
            SELECT *
            FROM t_attendance_mock_config
            WHERE config_type = #{configType}
              AND mock_status = 'ENABLED'
              AND deleted_flag = 0
            ORDER BY create_time DESC
            """)
    List<MockConfigEntity> queryEnabledByType(@Param("configType") String configType);

    /**
     * 查询所有启用的Mock配置
     *
     * @return Mock配置列表
     */
    @Select("""
            SELECT *
            FROM t_attendance_mock_config
            WHERE mock_status = 'ENABLED'
              AND deleted_flag = 0
            ORDER BY config_type, create_time DESC
            """)
    List<MockConfigEntity> queryAllEnabled();

    /**
     * 按配置类型查询
     *
     * @param configType 配置类型
     * @return Mock配置列表
     */
    @Select("""
            SELECT *
            FROM t_attendance_mock_config
            WHERE config_type = #{configType}
              AND deleted_flag = 0
            ORDER BY create_time DESC
            """)
    List<MockConfigEntity> queryByType(@Param("configType") String configType);

    /**
     * 更新使用次数
     *
     * @param configId 配置ID
     * @return 更新行数
     */
    @Update("""
            UPDATE t_attendance_mock_config
            SET usage_count = usage_count + 1,
                last_used_time = NOW()
            WHERE config_id = #{configId}
              AND deleted_flag = 0
            """)
    int incrementUsage(@Param("configId") Long configId);

    /**
     * 查询最近的配置
     *
     * @param limit 限制数量
     * @return 配置列表
     */
    @Select("""
            SELECT *
            FROM t_attendance_mock_config
            WHERE deleted_flag = 0
            ORDER BY create_time DESC
            LIMIT #{limit}
            """)
    List<MockConfigEntity> queryRecent(@Param("limit") int limit);
}
