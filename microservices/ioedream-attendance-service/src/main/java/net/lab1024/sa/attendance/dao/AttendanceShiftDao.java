package net.lab1024.sa.attendance.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.attendance.AttendanceShiftEntity;

@Mapper
public interface AttendanceShiftDao extends BaseMapper<AttendanceShiftEntity> {
    AttendanceShiftEntity selectByShiftNo(@Param("shiftNo") String shiftNo);
}



