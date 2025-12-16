package net.lab1024.sa.common.video.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.video.entity.VideoFaceEntity;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 人脸库DAO接口
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Mapper
public interface VideoFaceDao extends BaseMapper<VideoFaceEntity> {

    /**
     * 根据人员ID查询人脸
     */
    @Select("SELECT * FROM t_video_face WHERE person_id = #{personId} AND deleted_flag = 0")
    List<VideoFaceEntity> selectByPersonId(@Param("personId") Long personId);

    /**
     * 根据人员编号查询人脸
     */
    @Select("SELECT * FROM t_video_face WHERE person_code = #{personCode} AND deleted_flag = 0")
    List<VideoFaceEntity> selectByPersonCode(@Param("personCode") String personCode);

    /**
     * 根据证件信息查询人脸
     */
    @Select("SELECT * FROM t_video_face WHERE id_card_type = #{idCardType} AND id_card_number = #{idCardNumber} AND deleted_flag = 0")
    List<VideoFaceEntity> selectByIdCard(@Param("idCardType") Integer idCardType, @Param("idCardNumber") String idCardNumber);

    /**
     * 根据手机号码查询人脸
     */
    @Select("SELECT * FROM t_video_face WHERE phone_number = #{phoneNumber} AND deleted_flag = 0")
    List<VideoFaceEntity> selectByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    /**
     * 根据部门ID查询人脸
     */
    @Select("SELECT * FROM t_video_face WHERE department_id = #{departmentId} AND deleted_flag = 0")
    List<VideoFaceEntity> selectByDepartmentId(@Param("departmentId") Long departmentId);

    /**
     * 根据人员类型查询人脸
     */
    @Select("SELECT * FROM t_video_face WHERE person_type = #{personType} AND deleted_flag = 0")
    List<VideoFaceEntity> selectByPersonType(@Param("personType") Integer personType);

    /**
     * 根据性别查询人脸
     */
    @Select("SELECT * FROM t_video_face WHERE gender = #{gender} AND deleted_flag = 0")
    List<VideoFaceEntity> selectByGender(@Param("gender") Integer gender);

    /**
     * 根据算法类型查询人脸
     */
    @Select("SELECT * FROM t_video_face WHERE algorithm_type = #{algorithmType} AND deleted_flag = 0")
    List<VideoFaceEntity> selectByAlgorithmType(@Param("algorithmType") Integer algorithmType);

    /**
     * 根据人脸状态查询人脸
     */
    @Select("SELECT * FROM t_video_face WHERE face_status = #{faceStatus} AND deleted_flag = 0")
    List<VideoFaceEntity> selectByFaceStatus(@Param("faceStatus") Integer faceStatus);

    /**
     * 根据同步状态查询人脸
     */
    @Select("SELECT * FROM t_video_face WHERE sync_flag = #{syncFlag} AND deleted_flag = 0")
    List<VideoFaceEntity> selectBySyncFlag(@Param("syncFlag") Integer syncFlag);

    /**
     * 查询有效期内的人脸
     */
    @Select("SELECT * FROM t_video_face WHERE valid_start_time <= #{currentTime} AND valid_end_time >= #{currentTime} AND deleted_flag = 0")
    List<VideoFaceEntity> selectValidFaces(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 查询过期的人脸
     */
    @Select("SELECT * FROM t_video_face WHERE valid_end_time < #{currentTime} AND face_status = 1 AND deleted_flag = 0")
    List<VideoFaceEntity> selectExpiredFaces(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 根据姓名模糊查询人脸
     */
    @Select("SELECT * FROM t_video_face WHERE person_name LIKE CONCAT('%', #{keyword}, '%') AND deleted_flag = 0")
    List<VideoFaceEntity> selectByNameKeyword(@Param("keyword") String keyword);

    /**
     * 统计各人员类型的人脸数量
     */
    @Select("SELECT person_type, COUNT(*) as count FROM t_video_face WHERE deleted_flag = 0 GROUP BY person_type")
    List<Map<String, Object>> countByPersonType();

    /**
     * 统计各部门的人脸数量
     */
    @Select("SELECT department_id, department_name, COUNT(*) as count FROM t_video_face WHERE deleted_flag = 0 GROUP BY department_id, department_name")
    List<Map<String, Object>> countByDepartment();

    /**
     * 统计各算法类型的人脸数量
     */
    @Select("SELECT algorithm_type, algorithm_version, COUNT(*) as count FROM t_video_face WHERE deleted_flag = 0 GROUP BY algorithm_type, algorithm_version")
    List<Map<String, Object>> countByAlgorithm();

    /**
     * 统计各人脸状态的数量
     */
    @Select("SELECT face_status, COUNT(*) as count FROM t_video_face WHERE deleted_flag = 0 GROUP BY face_status")
    List<Map<String, Object>> countByFaceStatus();

    /**
     * 查询最近添加的人脸
     */
    @Select("SELECT * FROM t_video_face WHERE deleted_flag = 0 ORDER BY create_time DESC LIMIT #{limit}")
    List<VideoFaceEntity> selectRecentFaces(@Param("limit") Integer limit);

    /**
     * 查询质量分数高于指定阈值的人脸
     */
    @Select("SELECT * FROM t_video_face WHERE face_quality_score >= #{qualityThreshold} AND deleted_flag = 0")
    List<VideoFaceEntity> selectHighQualityFaces(@Param("qualityThreshold") Double qualityThreshold);

    /**
     * 查询通过活体检测的人脸
     */
    @Select("SELECT * FROM t_video_face WHERE liveness_check = 1 AND deleted_flag = 0")
    List<VideoFaceEntity> selectLivenessVerifiedFaces();

    /**
     * 批量更新人脸状态
     */
    @Update("UPDATE t_video_face SET face_status = #{faceStatus}, update_time = NOW() WHERE face_id IN (${faceIds})")
    int batchUpdateFaceStatus(@Param("faceIds") String faceIds, @Param("faceStatus") Integer faceStatus);

    /**
     * 批量更新同步状态
     */
    @Update("UPDATE t_video_face SET sync_flag = #{syncFlag}, last_sync_time = NOW() WHERE face_id IN (${faceIds})")
    int batchUpdateSyncFlag(@Param("faceIds") String faceIds, @Param("syncFlag") Integer syncFlag);

    /**
     * 删除指定人员的所有人脸
     */
    @Update("UPDATE t_video_face SET deleted_flag = 1, update_time = NOW() WHERE person_id = #{personId}")
    int deleteByPersonId(@Param("personId") Long personId);

    /**
     * 检查人员是否已存在人脸
     */
    @Select("SELECT COUNT(*) FROM t_video_face WHERE person_id = #{personId} AND deleted_flag = 0")
    int countByPersonId(@Param("personId") Long personId);

    /**
     * 检查人员编号是否已存在
     */
    @Select("SELECT COUNT(*) FROM t_video_face WHERE person_code = #{personCode} AND deleted_flag = 0")
    int countByPersonCode(@Param("personCode") String personCode);

    /**
     * 检查证件号码是否已存在
     */
    @Select("SELECT COUNT(*) FROM t_video_face WHERE id_card_type = #{idCardType} AND id_card_number = #{idCardNumber} AND deleted_flag = 0")
    int countByIdCard(@Param("idCardType") Integer idCardType, @Param("idCardNumber") String idCardNumber);

    /**
     * 查询采集设备最近的人脸
     */
    @Select("SELECT * FROM t_video_face WHERE capture_device_id = #{deviceId} AND deleted_flag = 0 ORDER BY capture_time DESC LIMIT #{limit}")
    List<VideoFaceEntity> selectRecentFacesByDevice(@Param("deviceId") Long deviceId, @Param("limit") Integer limit);

    /**
     * 查询指定时间段内采集的人脸
     */
    @Select("SELECT * FROM t_video_face WHERE capture_time >= #{startTime} AND capture_time <= #{endTime} AND deleted_flag = 0")
    List<VideoFaceEntity> selectFacesByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 查询需要同步的人脸
     */
    @Select("SELECT * FROM t_video_face WHERE sync_flag != 1 AND deleted_flag = 0 ORDER BY create_time")
    List<VideoFaceEntity> selectUnsyncedFaces();

    /**
     * 查询人脸重复数据（同一人员多个人脸）
     */
    @Select("SELECT person_id, person_name, COUNT(*) as face_count FROM t_video_face WHERE deleted_flag = 0 GROUP BY person_id, person_name HAVING COUNT(*) > 1")
    List<Map<String, Object>> selectDuplicateFaces();

    /**
     * 清理过期人脸
     */
    @Update("UPDATE t_video_face SET face_status = 3, update_time = NOW() WHERE valid_end_time < #{currentTime} AND face_status = 1 AND deleted_flag = 0")
    int cleanExpiredFaces(@Param("currentTime") LocalDateTime currentTime);
}