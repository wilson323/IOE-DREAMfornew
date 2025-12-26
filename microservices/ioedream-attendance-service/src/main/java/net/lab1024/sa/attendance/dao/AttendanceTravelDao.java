package net.lab1024.sa.attendance.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.attendance.AttendanceTravelEntity;

@Mapper
public interface AttendanceTravelDao extends BaseMapper<AttendanceTravelEntity> {
    AttendanceTravelEntity selectByTravelNo(@Param("travelNo") String travelNo);
}



