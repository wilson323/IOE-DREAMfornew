package net.lab1024.sa.admin.module.oa.document.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.admin.module.oa.document.domain.entity.DocumentShareEntity;

/**
 * 文档分享DAO
 * <p>
 * 严格遵循repowiki规范：
 * - 继承BaseMapper，提供基础CRUD操作
 * - 方法命名遵循MyBatis Plus规范
 * - 完整的参数注释和返回值说明
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Mapper
public interface DocumentShareDao extends BaseMapper<DocumentShareEntity> {

    /**
     * 根据分享令牌查询分享记录
     *
     * @param shareToken 分享令牌
     * @return 分享记录
     */
    DocumentShareEntity selectByShareToken(@Param("shareToken") String shareToken);

    /**
     * 根据文档ID查询所有分享记录
     *
     * @param documentId 文档ID
     * @return 分享记录列表
     */
    List<DocumentShareEntity> selectByDocumentId(@Param("documentId") Long documentId);

    /**
     * 根据分享类型和目标ID查询分享记录
     *
     * @param shareType 分享类型
     * @param targetId  目标ID
     * @return 分享记录列表
     */
    List<DocumentShareEntity> selectByShareTypeAndTargetId(@Param("shareType") String shareType,
            @Param("targetId") Long targetId);

    /**
     * 更新分享状态
     *
     * @param shareId 分享ID
     * @param status  新状态
     * @return 更新行数
     */
    int updateStatus(@Param("shareId") Long shareId, @Param("status") String status);

    /**
     * 增加访问次数
     *
     * @param shareId 分享ID
     * @return 更新行数
     */
    int incrementAccessCount(@Param("shareId") Long shareId);

    /**
     * 增加下载次数
     *
     * @param shareId 分享ID
     * @return 更新行数
     */
    int incrementDownloadCount(@Param("shareId") Long shareId);

    /**
     * 更新最后访问时间
     *
     * @param shareId        分享ID
     * @param lastAccessTime 最后访问时间
     * @return 更新行数
     */
    int updateLastAccessTime(@Param("shareId") Long shareId, @Param("lastAccessTime") LocalDateTime lastAccessTime);

    /**
     * 查询已过期的分享记录
     *
     * @param currentTime 当前时间
     * @return 已过期的分享记录列表
     */
    List<DocumentShareEntity> selectExpiredShares(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 批量更新过期分享状态
     *
     * @param shareIds 分享ID列表
     * @return 更新行数
     */
    int batchUpdateExpiredStatus(@Param("shareIds") List<Long> shareIds);
}
