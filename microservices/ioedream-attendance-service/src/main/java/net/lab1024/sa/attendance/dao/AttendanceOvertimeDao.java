package net.lab1024.sa.attendance.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.attendance.AttendanceOvertimeEntity;

@Mapper
public interface AttendanceOvertimeDao extends BaseMapper<AttendanceOvertimeEntity> {
    AttendanceOvertimeEntity selectByOvertimeNo(@Param("overtimeNo") String overtimeNo);
}



