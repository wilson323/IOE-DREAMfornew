package net.lab1024.sa.admin.module.oa.document.manager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.oa.document.dao.DocumentDao;
import net.lab1024.sa.admin.module.oa.document.dao.DocumentPermissionDao;
import net.lab1024.sa.admin.module.oa.document.dao.DocumentVersionDao;
import net.lab1024.sa.admin.module.oa.document.domain.entity.DocumentEntity;
import net.lab1024.sa.admin.module.oa.document.domain.entity.DocumentPermissionEntity;
import net.lab1024.sa.admin.module.oa.document.domain.entity.DocumentVersionEntity;
import net.lab1024.sa.base.common.manager.BaseCacheManager;

/**
 * 文档管理缓存管理器
 *
 * 基于BaseCacheManager实现的多级缓存: - L1: Caffeine本地缓存(10分钟过期) - L2:
 * Redis分布式缓存(60分钟过期)
 *
 * 核心职责: - 文档基本信息缓存 - 文档权限缓存和快速验证 - 版本历史缓存管理 - 用户可访问文档列表缓存 - 文档搜索结果缓存
 *
 * 缓存Key规范: - document:info:{documentId} - 文档基本信息 -
 * document:content:{documentId} - 文档内容缓存 -
 * document:permission:{documentId} - 文档权限配置 - document:user:docs:{userId} -
 * 用户可访问文档列表 -
 * document:version:history:{documentId} - 文档版本历史 -
 * document:search:{keywordHash} - 搜索结果缓存
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Slf4j
@Component
public class DocumentCacheManager extends BaseCacheManager {

    @Resource
    private DocumentDao documentDao;

    @Resource
    private DocumentVersionDao documentVersionDao;

    @Resource
    private DocumentPermissionDao documentPermissionDao;

    // 权限检查结果缓存，减少数据库查询
    private final Map<String, Boolean> permissionCheckCache = new ConcurrentHashMap<>();

    @Override
    protected String getCachePrefix() {
        return "document:";
    }

    /**
     * 获取文档基本信息(带缓存)
     *
     * @param documentId 文档ID
     * @return 文档信息
     */
    public DocumentEntity getDocument(Long documentId) {
        String cacheKey = buildCacheKey(documentId, ":info");
        return this.getCache(cacheKey, () -> documentDao.selectById(documentId));
    }

    /**
     * 设置文档基本信息缓存
     *
     * @param document 文档信息
     */
    public void setDocument(DocumentEntity document) {
        String cacheKey = buildCacheKey(document.getDocumentId(), ":info");
        this.setCache(cacheKey, document);

        // 同时更新内容缓存
        if (document.getContent() != null && !document.getContent().isEmpty()) {
            String contentCacheKey = buildCacheKey(document.getDocumentId(), ":content");
            this.setCache(contentCacheKey, document.getContent());
        }
    }

    /**
     * 获取文档内容(带缓存)
     *
     * @param documentId 文档ID
     * @return 文档内容
     */
    public String getDocumentContent(Long documentId) {
        String cacheKey = buildCacheKey(documentId, ":content");
        return this.getCache(cacheKey, () -> {
            DocumentEntity document = documentDao.selectById(documentId);
            return document != null ? document.getContent() : null;
        });
    }

    /**
     * 获取用户可访问的文档列表(带缓存)
     *
     * @param userId     用户ID
     * @param permission 权限类型
     * @return 文档列表
     */
    public List<DocumentEntity> getUserAccessibleDocuments(Long userId, String permission) {
        String cacheKey = buildCacheKey(userId, ":user:docs:" + permission);

        return this.getCache(cacheKey, () -> {
            // 通过权限表查询用户可访问的文档
            List<Long> accessibleDocumentIds = documentPermissionDao.selectAccessibleDocumentIds(userId, permission);
            if (accessibleDocumentIds.isEmpty()) {
                return List.of();
            }

            // 批量查询文档信息
            List<DocumentEntity> documents = documentDao.selectBatchByIds(accessibleDocumentIds);
            log.debug("从数据库查询用户可访问文档: userId={}, permission={}, docCount={}", userId, permission,
                    documents.size());
            return documents;
        });
    }

    /**
     * 获取文档版本历史(带缓存)
     *
     * @param documentId 文档ID
     * @return 版本列表
     */
    public List<DocumentVersionEntity> getDocumentVersions(Long documentId) {
        String cacheKey = buildCacheKey(documentId, ":version:history");

        return this.getCache(cacheKey, () -> {
            List<DocumentVersionEntity> versions = documentVersionDao.selectByDocumentId(documentId);
            log.debug("从数据库查询文档版本历史: documentId={}, versionCount={}", documentId, versions.size());
            return versions;
        });
    }

    /**
     * 获取最新版本(带缓存)
     *
     * @param documentId 文档ID
     * @return 最新版本信息
     */
    public DocumentVersionEntity getLatestVersion(Long documentId) {
        String cacheKey = buildCacheKey(documentId, ":version:latest");

        return this.getCache(cacheKey, () -> {
            DocumentVersionEntity version = documentVersionDao.selectLatestVersion(documentId);
            log.debug("从数据库查询文档最新版本: documentId={}", documentId);
            return version;
        });
    }

    /**
     * 获取文档权限配置(带缓存)
     *
     * @param documentId 文档ID
     * @return 权限配置列表
     */
    public List<DocumentPermissionEntity> getDocumentPermissions(Long documentId) {
        String cacheKey = buildCacheKey(documentId, ":permission");

        return this.getCache(cacheKey, () -> {
            List<DocumentPermissionEntity> permissions = documentPermissionDao.selectByDocumentId(documentId);
            log.debug("从数据库查询文档权限: documentId={}, permissionCount={}", documentId,
                    permissions.size());
            return permissions;
        });
    }

    /**
     * 检查用户权限(带缓存)
     *
     * @param documentId 文档ID
     * @param userId     用户ID
     * @param permission 权限类型
     * @return 是否有权限
     */
    public boolean checkUserPermission(Long documentId, Long userId, String permission) {
        String cacheKey = String.format("permission:%d:%d:%s", documentId, userId, permission);

        // 优先从内存缓存获取
        Boolean cachedResult = permissionCheckCache.get(cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }

        // 从数据库查询
        boolean hasPermission = documentPermissionDao.checkUserPermission(documentId, userId, permission);

        // 缓存结果（5分钟）
        permissionCheckCache.put(cacheKey, hasPermission);

        // 异步清理过期缓存
        if (permissionCheckCache.size() > 10000) {
            // 当缓存过大时，清理一半最旧的条目
            permissionCheckCache.entrySet().removeIf(entry -> {
                return permissionCheckCache.size() > 5000;
            });
        }

        log.debug("权限检查: documentId={}, userId={}, permission={}, result={}", documentId, userId,
                permission, hasPermission);
        return hasPermission;
    }

    /**
     * 获取搜索结果缓存
     *
     * @param keywordHash 关键词哈希
     * @return 搜索结果
     */
    public List<DocumentEntity> getSearchResult(String keywordHash) {
        String cacheKey = buildCacheKey("search", ":" + keywordHash);
        return this.getCache(cacheKey, () -> List.of());
    }

    /**
     * 设置搜索结果缓存
     *
     * @param keywordHash 关键词哈希
     * @param documents   搜索结果
     */
    public void setSearchResult(String keywordHash, List<DocumentEntity> documents) {
        String cacheKey = buildCacheKey("search", ":" + keywordHash);
        this.setCache(cacheKey, documents);
        log.debug("缓存搜索结果: keywordHash={}, resultCount={}", keywordHash, documents.size());
    }

    /**
     * 获取文档统计信息(带缓存)
     *
     * @param categoryId 分类ID(可选)
     * @return 统计信息
     */
    public Map<String, Object> getDocumentStatistics(Long categoryId) {
        String cacheKey = categoryId != null ? buildCacheKey("statistics", ":category:" + categoryId)
                : buildCacheKey("statistics", ":all");

        return this.getCache(cacheKey, () -> {
            Map<String, Object> statistics = documentDao.selectStatistics(categoryId);
            log.debug("从数据库查询文档统计: categoryId={}", categoryId);
            return statistics;
        });
    }

    /**
     * 清除文档相关缓存
     *
     * @param documentId 文档ID
     */
    public void clearDocumentCache(Long documentId) {
        // 清除基本信息缓存
        this.removeCache(buildCacheKey(documentId, ":info"));

        // 清除内容缓存
        this.removeCache(buildCacheKey(documentId, ":content"));

        // 清除权限缓存
        this.removeCache(buildCacheKey(documentId, ":permission"));

        // 清除版本相关缓存
        this.removeCache(buildCacheKey(documentId, ":version:history"));
        this.removeCache(buildCacheKey(documentId, ":version:latest"));

        log.debug("清除文档缓存: documentId={}", documentId);
    }

    /**
     * 清除用户文档权限缓存
     *
     * @param userId 用户ID
     */
    public void clearUserPermissionCache(Long userId) {
        // 清除用户可访问文档缓存
        this.removeCacheByPattern(buildCacheKey(userId, ":user:docs:*"));

        // 清除权限检查缓存
        permissionCheckCache.entrySet()
                .removeIf(entry -> entry.getKey().contains(":" + userId + ":"));

        log.debug("清除用户权限缓存: userId={}", userId);
    }

    /**
     * 清除文档权限缓存
     *
     * @param documentId 文档ID
     */
    public void clearDocumentPermissionCache(Long documentId) {
        // 清除权限配置缓存
        this.removeCache(buildCacheKey(documentId, ":permission"));

        // 清除相关的权限检查缓存
        permissionCheckCache.entrySet()
                .removeIf(entry -> entry.getKey().startsWith("permission:" + documentId + ":"));

        log.debug("清除文档权限缓存: documentId={}", documentId);
    }

    /**
     * 清除文档版本相关缓存
     *
     * @param documentId 文档ID
     */
    public void clearDocumentVersionCache(Long documentId) {
        // 清除版本历史缓存
        this.removeCache(buildCacheKey(documentId, ":version:history"));

        // 清除最新版本缓存
        this.removeCache(buildCacheKey(documentId, ":version:latest"));

        log.debug("清除文档版本缓存: documentId={}", documentId);
    }

    /**
     * 清除统计缓存
     *
     * @param categoryId 分类ID(可选)
     */
    public void clearStatisticsCache(Long categoryId) {
        if (categoryId != null) {
            this.removeCache(buildCacheKey("statistics", ":category:" + categoryId));
        } else {
            this.removeCache(buildCacheKey("statistics", ":all"));
        }
        log.debug("清除统计缓存: categoryId={}", categoryId);
    }

    /**
     * 清除搜索缓存
     *
     * @param keywordHashes 关键词哈希列表
     */
    public void clearSearchCache(List<String> keywordHashes) {
        for (String hash : keywordHashes) {
            this.removeCache(buildCacheKey("search", ":" + hash));
        }
        log.debug("清除搜索缓存: count={}", keywordHashes.size());
    }

    /**
     * 清除用户所有相关缓存
     *
     * @param userId 用户ID
     */
    public void clearUserAllCache(Long userId) {
        clearUserPermissionCache(userId);
        this.removeCacheByPattern(buildCacheKey(userId, ":*"));
        log.debug("清除用户所有缓存: userId={}", userId);
    }

    /**
     * 预热文档缓存
     *
     * @param documentIds 文档ID列表
     */
    public void warmUpDocumentCache(List<Long> documentIds) {
        log.info("开始预热文档缓存: count={}", documentIds.size());

        for (Long documentId : documentIds) {
            try {
                // 预热基本信息
                DocumentEntity document = documentDao.selectById(documentId);
                if (document != null) {
                    setDocument(document);
                }

                // 预热版本历史（仅最近10个文档）
                if (documentIds.indexOf(documentId) < 10) {
                    getDocumentVersions(documentId);
                    getDocumentPermissions(documentId);
                }

            } catch (Exception e) {
                log.warn("预热文档缓存失败: documentId={}, error={}", documentId, e.getMessage());
            }
        }

        log.info("文档缓存预热完成: count={}", documentIds.size());
    }

    /**
     * 获取缓存统计信息
     */
    @Override
    public CacheStats getCacheStats() {
        CacheStats stats = super.getCacheStats();
        return stats;
    }
}
