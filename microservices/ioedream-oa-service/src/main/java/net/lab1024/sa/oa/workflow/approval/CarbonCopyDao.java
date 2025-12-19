package net.lab1024.sa.oa.workflow.approval;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 抄送记录数据访问层
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-17
 */
@Mapper
public interface CarbonCopyDao extends BaseMapper<CarbonCopyEntity> {

    @Select("<script>" +
            "SELECT * FROM t_workflow_carbon_copy " +
            "WHERE cc_user_id = #{userId} AND deleted_flag = 0 " +
            "<if test='readStatus != null'> AND read_status = #{readStatus} </if>" +
            "ORDER BY cc_time DESC" +
            "</script>")
    List<CarbonCopyEntity> findByUserIdAndStatus(@Param("userId") Long userId,
                                                  @Param("readStatus") Integer readStatus);

    @Select("SELECT * FROM t_workflow_carbon_copy " +
            "WHERE process_instance_id = #{processInstanceId} AND deleted_flag = 0 " +
            "ORDER BY cc_time DESC")
    List<CarbonCopyEntity> findByProcessInstanceId(@Param("processInstanceId") String processInstanceId);
}
