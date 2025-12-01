package net.lab1024.sa.enterprise.file.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.enterprise.file.entity.FileEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件管理 DAO
 *
 * @author 老王
 * @since 2025-11-30
 */
@Mapper
@Repository
public interface FileDao extends BaseMapper<FileEntity> {

    /**
     * 根据MD5查询文件
     */
    FileEntity selectByMd5(@Param("fileMd5") String fileMd5, @Param("fileStatus") Integer fileStatus);

    /**
     * 更新下载次数
     */
    int updateDownloadCount(@Param("fileId") Long fileId, @Param("downloadCount") Integer downloadCount,
                           @Param("lastDownloadTime") LocalDateTime lastDownloadTime);

    /**
     * 根据业务模块和业务ID查询文件列表
     */
    List<FileEntity> selectByBusiness(@Param("businessModule") String businessModule,
                                     @Param("businessId") String businessId,
                                     @Param("fileStatus") Integer fileStatus);

    /**
     * 根据上传用户查询文件列表
     */
    List<FileEntity> selectByUploadUser(@Param("uploadUserId") Long uploadUserId,
                                       @Param("fileStatus") Integer fileStatus);

    /**
     * 批量更新文件状态
     */
    int batchUpdateStatus(@Param("fileIds") List<Long> fileIds, @Param("fileStatus") Integer fileStatus);

    /**
     * 查询过期文件
     */
    List<FileEntity> selectExpiredFiles(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 根据文件类型查询文件列表
     */
    List<FileEntity> selectByFileType(@Param("fileType") String fileType,
                                     @Param("fileStatus") Integer fileStatus);

}