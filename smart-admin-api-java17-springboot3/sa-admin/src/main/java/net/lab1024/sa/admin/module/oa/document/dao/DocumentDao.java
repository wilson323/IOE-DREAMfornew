package net.lab1024.sa.admin.module.oa.document.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.oa.document.domain.entity.DocumentEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 文档DAO
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
public interface DocumentDao extends BaseMapper<DocumentEntity> {

    /**
     * 分页查询文档
     *
     * @param categoryId 分类ID
     * @param documentType 文档类型
     * @param status 状态
     * @param keyword 关键词
     * @param tags 标签
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param createdById 创建人ID
     * @return 文档列表
     */
    List<DocumentEntity> selectDocumentsByCondition(Long categoryId, String documentType, String status,
                                                      String keyword, String tags,
                                                      LocalDateTime startDate, LocalDateTime endDate,
                                                      Long createdById);

    /**
     * 批量查询文档
     *
     * @param documentIds 文档ID列表
     * @return 文档列表
     */
    List<DocumentEntity> selectBatchByIds(List<Long> documentIds);

    /**
     * 获取文档统计信息
     *
     * @param categoryId 分类ID
     * @return 统计信息
     */
    Map<String, Object> selectStatistics(Long categoryId);

    /**
     * 查询用户创建的文档
     *
     * @param userId 用户ID
     * @param status 状态
     * @param limit 限制数量
     * @return 文档列表
     */
    List<DocumentEntity> selectByCreatedBy(Long userId, String status, Integer limit);

    /**
     * 查询最新文档
     *
     * @param limit 限制数量
     * @return 文档列表
     */
    List<DocumentEntity> selectLatestDocuments(Integer limit);

    /**
     * 查询热门文档(按访问量排序)
     *
     * @param limit 限制数量
     * @return 文档列表
     */
    List<DocumentEntity> selectPopularDocuments(Integer limit);

    /**
     * 查询收藏文档
     *
     * @param userId 用户ID
     * @return 文档ID列表
     */
    List<Long> selectFavoriteDocumentIds(Long userId);

    /**
     * 更新文档访问信息
     *
     * @param documentId 文档ID
     * @param userId 用户ID
     * @param userName 用户名
     * @return 更新数量
     */
    Integer updateAccessInfo(Long documentId, Long userId, String userName);

    /**
     * 更新文档统计信息
     *
     * @param documentId 文档ID
     * @param viewCountDelta 浏览次数增量
     * @param downloadCountDelta 下载次数增量
     * @return 更新数量
     */
    Integer updateStatistics(Long documentId, Integer viewCountDelta, Integer downloadCountDelta);

    /**
     * 更新文档状态
     *
     * @param documentId 文档ID
     * @param status 新状态
     * @param archivedTime 归档时间
     * @return 更新数量
     */
    Integer updateStatus(Long documentId, String status, LocalDateTime archivedTime);

    /**
     * 检查文档编号是否存在
     *
     * @param documentNo 文档编号
     * @param excludeId 排除的文档ID
     * @return 是否存在
     */
    boolean existsByDocumentNo(String documentNo, Long excludeId);

    /**
     * 查询即将到期的文档
     *
     * @param hours 小时数
     * @return 文档列表
     */
    List<DocumentEntity> selectExpiringDocuments(Integer hours);

    /**
     * 查询分类下的文档数量
     *
     * @param categoryId 分类ID
     * @return 数量
     */
    Integer countByCategory(Long categoryId);

    /**
     * 查询文档类型统计
     *
     * @return 类型统计
     */
    Map<String, Integer> countByDocumentType();

    /**
     * 查询用户权限统计
     *
     * @param userId 用户ID
     * @return 权限统计
     */
    Map<String, Integer> countByUserPermission(Long userId);

    /**
     * 全文搜索文档
     *
     * @param keyword 关键词
     * @param categoryId 分类ID
     * @param documentType 文档类型
     * @param tags 标签
     * @param limit 限制数量
     * @return 文档列表
     */
    List<DocumentEntity> fullTextSearch(String keyword, Long categoryId, String documentType,
                                         String tags, Integer limit);

    /**
     * 获取文档历史记录
     *
     * @param documentId 文档ID
     * @return 历史记录
     */
    List<Map<String, Object>> selectDocumentHistory(Long documentId);

    /**
     * 检查用户是否有任何文档权限
     *
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean hasAnyPermission(Long userId);

    /**
     * 获取文档存储空间统计
     *
     * @return 存储空间统计
     */
    Map<String, Object> getStorageSpaceStatistics();

    /**
     * 清理过期文档缓存标记
     *
     * @param expireDays 过期天数
     * @return 清理数量
     */
    Integer cleanExpiredCache(Integer expireDays);
}