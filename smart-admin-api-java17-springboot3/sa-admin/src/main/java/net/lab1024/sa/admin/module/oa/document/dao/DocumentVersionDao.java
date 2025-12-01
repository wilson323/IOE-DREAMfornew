package net.lab1024.sa.admin.module.oa.document.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.oa.document.domain.entity.DocumentVersionEntity;

/**
 * 文档版本DAO
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
public interface DocumentVersionDao extends BaseMapper<DocumentVersionEntity> {

    /**
     * 查询文档的所有版本
     *
     * @param documentId 文档ID
     * @return 版本列表
     */
    List<DocumentVersionEntity> selectByDocumentId(Long documentId);

    /**
     * 查询文档的最新版本
     *
     * @param documentId 文档ID
     * @return 最新版本
     */
    DocumentVersionEntity selectLatestVersion(Long documentId);

    /**
     * 查询文档的当前版本
     *
     * @param documentId 文档ID
     * @return 当前版本
     */
    DocumentVersionEntity selectCurrentVersion(Long documentId);

    /**
     * 查询指定版本的下一个版本
     *
     * @param documentId 文档ID
     * @param versionNumber 当前版本号
     * @return 下一个版本
     */
    DocumentVersionEntity selectNextVersion(Long documentId, Integer versionNumber);

    /**
     * 查询指定版本的上一个版本
     *
     * @param documentId 文档ID
     * @param versionNumber 当前版本号
     * @return 上一个版本
     */
    DocumentVersionEntity selectPreviousVersion(Long documentId, Integer versionNumber);

    /**
     * 查询指定版本号范围的版本
     *
     * @param documentId 文档ID
     * @param startVersion 开始版本号
     * @param endVersion 结束版本号
     * @return 版本列表
     */
    List<DocumentVersionEntity> selectByVersionRange(Long documentId, Integer startVersion,
            Integer endVersion);

    /**
     * 查询用户创建的版本
     *
     * @param createdBy 创建人ID
     * @param documentId 文档ID(可选)
     * @param limit 限制数量
     * @return 版本列表
     */
    List<DocumentVersionEntity> selectByCreatedBy(Long createdBy, Long documentId, Integer limit);

    /**
     * 查询待审批的版本
     *
     * @param approvalStatus 审批状态
     * @param limit 限制数量
     * @return 版本列表
     */
    List<DocumentVersionEntity> selectPendingApproval(String approvalStatus, Integer limit);

    /**
     * 查询指定时间范围内的版本
     *
     * @param documentId 文档ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 版本列表
     */
    List<DocumentVersionEntity> selectByTimeRange(Long documentId, LocalDateTime startTime,
            LocalDateTime endTime);

    /**
     * 查询主要版本
     *
     * @param documentId 文档ID
     * @return 主要版本列表
     */
    List<DocumentVersionEntity> selectMajorVersions(Long documentId);

    /**
     * 统计文档版本数量
     *
     * @param documentId 文档ID
     * @return 版本数量
     */
    Integer countVersionsByDocument(Long documentId);

    /**
     * 获取文档最大版本号
     *
     * @param documentId 文档ID
     * @return 最大版本号
     */
    Integer selectMaxVersionNumber(Long documentId);

    /**
     * 检查版本号是否已存在
     *
     * @param documentId 文档ID
     * @param versionNumber 版本号
     * @param excludeVersionId 排除的版本ID
     * @return 是否存在
     */
    boolean existsVersionNumber(Long documentId, Integer versionNumber, Long excludeVersionId);

    /**
     * 更新版本审批状态
     *
     * @param versionId 版本ID
     * @param approvalStatus 审批状态
     * @param approvedBy 审批人ID
     * @param approvedName 审批人姓名
     * @param approvalTime 审批时间
     * @param approvalComment 审批意见
     * @return 更新数量
     */
    Integer updateApprovalStatus(Long versionId, String approvalStatus, Long approvedBy,
            String approvedName, LocalDateTime approvedTime, String approvalComment);

    /**
     * 设置当前版本
     *
     * @param documentId 文档ID
     * @param versionId 要设置为当前的版本ID
     * @return 更新数量
     */
    Integer setCurrentVersion(Long documentId, Long versionId);

    /**
     * 清除当前版本标识
     *
     * @param documentId 文档ID
     * @return 更新数量
     */
    Integer clearCurrentVersion(Long documentId);

    /**
     * 归档版本
     *
     * @param versionId 版本ID
     * @param archivedTime 归档时间
     * @return 更新数量
     */
    Integer archiveVersion(Long versionId, LocalDateTime archivedTime);

    /**
     * 批量删除文档的所有版本
     *
     * @param documentId 文档ID
     * @return 删除数量
     */
    Integer deleteByDocumentId(Long documentId);

    /**
     * 获取版本大小统计
     *
     * @param documentId 文档ID
     * @return 大小统计
     */
    Map<String, Object> getVersionSizeStatistics(Long documentId);

    /**
     * 查询版本变更类型统计
     *
     * @param documentId 文档ID
     * @return 变更类型统计
     */
    Map<String, Integer> getChangeTypeStatistics(Long documentId);

    /**
     * 查询用户版本活动统计
     *
     * @param createdBy 创建人ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 活动统计
     */
    Map<String, Integer> getUserVersionActivity(Long createdBy, LocalDateTime startDate,
            LocalDateTime endDate);
}
