package net.lab1024.sa.data.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.PageResult;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.data.dao.DashboardDao;
import net.lab1024.sa.data.domain.DataAnalysisDomain.*;
import net.lab1024.sa.data.domain.entity.DashboardEntity;
import net.lab1024.sa.data.service.DashboardService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 仪表板服务实现
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class DashboardServiceImpl implements DashboardService {

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private DashboardDao dashboardDao;

    /**
     * 分享链接存储（内存缓存）
     */
    private final Map<String, ShareLinkInfo> shareLinkStorage = new ConcurrentHashMap<>();

    // ==================== 仪表板管理 ====================

    @Override
    @CacheEvict(value = "dashboards", allEntries = true)
    public Long createDashboard(DashboardVO dashboard) {
        log.info("[仪表板] 创建仪表板: dashboardName={}", dashboard.getDashboardName());

        DashboardEntity entity = convertToEntity(dashboard);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        entity.setRefreshTime(LocalDateTime.now());
        entity.setStatus("draft");
        entity.setDeletedFlag(0);
        entity.setVersion(0);

        dashboardDao.insert(entity);

        log.info("[仪表板] 仪表板创建成功: dashboardId={}", entity.getDashboardId());
        return entity.getDashboardId();
    }

    @Override
    @CacheEvict(value = "dashboards", key = "#dashboardId")
    public void updateDashboard(Long dashboardId, DashboardVO dashboard) {
        log.info("[仪表板] 更新仪表板: dashboardId={}", dashboardId);

        DashboardEntity existingEntity = dashboardDao.selectById(dashboardId);
        if (existingEntity == null) {
            throw new BusinessException("DASHBOARD_NOT_FOUND", "仪表板不存在: " + dashboardId);
        }

        DashboardEntity entity = convertToEntity(dashboard);
        entity.setDashboardId(dashboardId);
        entity.setUpdateTime(LocalDateTime.now());
        entity.setRefreshTime(LocalDateTime.now());
        entity.setCreatorId(existingEntity.getCreatorId());
        entity.setCreateTime(existingEntity.getCreateTime());
        entity.setVersion(existingEntity.getVersion());

        int updated = dashboardDao.updateById(entity);

        if (updated == 0) {
            throw new BusinessException("DASHBOARD_UPDATE_FAILED", "仪表板更新失败，可能是版本冲突");
        }

        log.info("[仪表板] 仪表板更新成功: dashboardId={}", dashboardId);
    }

    @Override
    @CacheEvict(value = "dashboards", key = "#dashboardId")
    public void deleteDashboard(Long dashboardId) {
        log.info("[仪表板] 删除仪表板: dashboardId={}", dashboardId);

        DashboardEntity entity = dashboardDao.selectById(dashboardId);
        if (entity == null) {
            throw new BusinessException("DASHBOARD_NOT_FOUND", "仪表板不存在: " + dashboardId);
        }

        // 使用逻辑删除
        entity.setDeletedFlag(1);
        dashboardDao.updateById(entity);

        log.info("[仪表板] 仪表板删除成功: dashboardId={}", dashboardId);
    }

    @Override
    @Cacheable(value = "dashboards", key = "#dashboardId")
    public DashboardVO getDashboardById(Long dashboardId) {
        log.debug("[仪表板] 查询仪表板: dashboardId={}", dashboardId);

        DashboardEntity entity = dashboardDao.selectById(dashboardId);
        if (entity == null) {
            throw new BusinessException("DASHBOARD_NOT_FOUND", "仪表板不存在: " + dashboardId);
        }

        return convertToVO(entity);
    }

    @Override
    public PageResult<DashboardVO> listDashboards(String status, Integer pageNum, Integer pageSize) {
        log.info("[仪表板] 查询仪表板列表: status={}, pageNum={}, pageSize={}",
                 status, pageNum, pageSize);

        LambdaQueryWrapper<DashboardEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(status != null, DashboardEntity::getStatus, status)
                   .orderByDesc(DashboardEntity::getCreateTime);

        Page<DashboardEntity> page = new Page<>(pageNum, pageSize);
        IPage<DashboardEntity> resultPage = dashboardDao.selectPage(page, queryWrapper);

        List<DashboardVO> dashboardList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(dashboardList, resultPage.getTotal(), pageNum, pageSize);
    }

    // ==================== 组件数据查询 ====================

    @Override
    public Map<String, Object> getComponentData(Long dashboardId) {
        log.info("[仪表板] 获取组件数据: dashboardId={}", dashboardId);

        DashboardVO dashboard = getDashboardById(dashboardId);

        Map<String, Object> componentData = new HashMap<>();

        DashboardConfig layout = dashboard.getLayout();
        if (layout != null && layout.getComponents() != null) {
            for (DashboardComponent component : layout.getComponents()) {
                Object data = generateComponentData(component);
                componentData.put(component.getComponentId(), data);
            }
        }

        log.info("[仪表板] 组件数据生成完成: dashboardId={}, components={}",
                 dashboardId, componentData.size());

        return componentData;
    }

    @Override
    public DashboardVO refreshDashboard(Long dashboardId) {
        log.info("[仪表板] 刷新仪表板: dashboardId={}", dashboardId);

        DashboardVO dashboard = getDashboardById(dashboardId);

        // 更新刷新时间
        DashboardEntity entity = dashboardDao.selectById(dashboardId);
        entity.setRefreshTime(LocalDateTime.now());
        dashboardDao.updateById(entity);

        dashboard.setRefreshTime(LocalDateTime.now());

        // 重新生成组件数据
        Map<String, Object> componentData = new HashMap<>();

        DashboardConfig layout = dashboard.getLayout();
        if (layout != null && layout.getComponents() != null) {
            for (DashboardComponent component : layout.getComponents()) {
                Object data = generateComponentData(component);
                componentData.put(component.getComponentId(), data);
            }
        }

        dashboard.setComponentData(componentData);

        log.info("[仪表板] 仪表板刷新完成: dashboardId={}", dashboardId);
        return dashboard;
    }

    // ==================== 仪表板模板 ====================

    @Override
    public DashboardVO getTemplateById(Long templateId) {
        log.debug("[仪表板] 获取模板: templateId={}", templateId);

        DashboardEntity entity = dashboardDao.selectById(templateId);
        if (entity == null || !"template".equals(entity.getStatus())) {
            throw new BusinessException("TEMPLATE_NOT_FOUND", "模板不存在: " + templateId);
        }

        return convertToVO(entity);
    }

    @Override
    public PageResult<DashboardVO> listTemplates(Integer pageNum, Integer pageSize) {
        log.info("[仪表板] 查询模板列表: pageNum={}, pageSize={}", pageNum, pageSize);

        LambdaQueryWrapper<DashboardEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DashboardEntity::getStatus, "template")
                   .orderByDesc(DashboardEntity::getCreateTime);

        Page<DashboardEntity> page = new Page<>(pageNum, pageSize);
        IPage<DashboardEntity> resultPage = dashboardDao.selectPage(page, queryWrapper);

        List<DashboardVO> templateList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(templateList, resultPage.getTotal(), pageNum, pageSize);
    }

    @Override
    public Long createDashboardFromTemplate(Long templateId, String dashboardName, Long creatorId) {
        log.info("[仪表板] 从模板创建仪表板: templateId={}, dashboardName={}",
                 templateId, dashboardName);

        DashboardVO template = getTemplateById(templateId);

        DashboardVO dashboard = new DashboardVO();
        dashboard.setDashboardName(dashboardName);
        dashboard.setDashboardCode(generateDashboardCode(dashboardName));
        dashboard.setLayout(template.getLayout());
        dashboard.setPermission(template.getPermission());
        dashboard.setCreatorId(creatorId);
        dashboard.setCreatorName("用户" + creatorId);
        dashboard.setDescription("基于模板【" + template.getDashboardName() + "】创建");

        return createDashboard(dashboard);
    }

    // ==================== 分享链接 ====================

    @Override
    public String generateShareLink(Long dashboardId, Integer expireHours) {
        log.info("[仪表板] 生成分享链接: dashboardId={}, expireHours={}",
                 dashboardId, expireHours);

        String shareToken = UUID.randomUUID().toString().replace("-", "");

        ShareLinkInfo shareInfo = ShareLinkInfo.builder()
                .dashboardId(dashboardId)
                .shareToken(shareToken)
                .createTime(LocalDateTime.now())
                .expireTime(LocalDateTime.now().plusHours(expireHours))
                .build();

        shareLinkStorage.put(shareToken, shareInfo);

        String shareLink = "/dashboard/share/" + shareToken;

        log.info("[仪表板] 分享链接生成成功: shareToken={}, expireHours={}",
                 shareToken, expireHours);

        return shareLink;
    }

    @Override
    public DashboardVO getDashboardByShareToken(String shareToken) {
        log.debug("[仪表板] 通过分享令牌获取仪表板: shareToken={}", shareToken);

        ShareLinkInfo shareInfo = shareLinkStorage.get(shareToken);
        if (shareInfo == null) {
            throw new BusinessException("SHARE_LINK_NOT_FOUND", "分享链接不存在或已过期");
        }

        if (LocalDateTime.now().isAfter(shareInfo.getExpireTime())) {
            shareLinkStorage.remove(shareToken);
            throw new BusinessException("SHARE_LINK_EXPIRED", "分享链接已过期");
        }

        return getDashboardById(shareInfo.getDashboardId());
    }

    @Override
    public void disableShareLink(String shareToken) {
        log.info("[仪表板] 禁用分享链接: shareToken={}", shareToken);

        ShareLinkInfo removed = shareLinkStorage.remove(shareToken);
        if (removed == null) {
            throw new BusinessException("SHARE_LINK_NOT_FOUND", "分享链接不存在");
        }

        log.info("[仪表板] 分享链接已禁用: shareToken={}", shareToken);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * Entity转VO
     */
    private DashboardVO convertToVO(DashboardEntity entity) {
        DashboardVO vo = new DashboardVO();
        vo.setDashboardId(entity.getDashboardId());
        vo.setDashboardName(entity.getDashboardName());
        vo.setDashboardCode(entity.getDashboardCode());

        try {
            if (entity.getLayoutConfig() != null) {
                DashboardConfig layout = objectMapper.readValue(entity.getLayoutConfig(), DashboardConfig.class);
                vo.setLayout(layout);
            }

            if (entity.getPermissionConfig() != null) {
                DataPermission permission = objectMapper.readValue(entity.getPermissionConfig(), DataPermission.class);
                vo.setPermission(permission);
            }
        } catch (Exception e) {
            log.error("[仪表板] JSON反序列化失败", e);
        }

        vo.setCreatorId(entity.getCreatorId());
        vo.setCreatorName(entity.getCreatorName());
        vo.setStatus(entity.getStatus());
        vo.setDescription(entity.getDescription());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        vo.setRefreshTime(entity.getRefreshTime());

        return vo;
    }

    /**
     * VO转Entity
     */
    private DashboardEntity convertToEntity(DashboardVO vo) {
        DashboardEntity entity = new DashboardEntity();
        entity.setDashboardId(vo.getDashboardId());
        entity.setDashboardName(vo.getDashboardName());
        entity.setDashboardCode(vo.getDashboardCode());

        if (vo.getLayout() != null) {
            try {
                entity.setLayoutConfig(objectMapper.writeValueAsString(vo.getLayout()));
            } catch (Exception e) {
                log.error("[仪表板] JSON序列化失败", e);
            }
        }

        if (vo.getPermission() != null) {
            try {
                entity.setPermissionConfig(objectMapper.writeValueAsString(vo.getPermission()));
            } catch (Exception e) {
                log.error("[仪表板] JSON序列化失败", e);
            }
        }

        entity.setCreatorId(vo.getCreatorId());
        entity.setCreatorName(vo.getCreatorName());
        entity.setStatus(vo.getStatus());
        entity.setDescription(vo.getDescription());

        return entity;
    }

    /**
     * 生成仪表板编码
     */
    private String generateDashboardCode(String dashboardName) {
        String code = "DASH_" + dashboardName.toUpperCase()
                .replaceAll(" ", "_")
                .replaceAll("[^A-Z0-9_]", "");

        code += "_" + System.currentTimeMillis();

        return code;
    }

    /**
     * 生成组件数据
     */
    private Object generateComponentData(DashboardComponent component) {
        String componentType = component.getType();

        switch (componentType) {
            case "stat-card":
                return generateStatCardData(component);

            case "chart":
                return generateChartData(component);

            case "table":
                return generateTableData(component);

            case "progress":
                return generateProgressData(component);

            default:
                return Collections.emptyMap();
        }
    }

    /**
     * 生成统计卡片数据
     */
    private Map<String, Object> generateStatCardData(DashboardComponent component) {
        Map<String, Object> data = new HashMap<>();

        String title = component.getTitle() != null ? component.getTitle() : "统计卡片";
        String value = String.valueOf(new Random().nextInt(10000));
        String trend = new Random().nextBoolean() ? "up" : "down";
        double changeRate = new Random().nextDouble() * 20 - 10;

        data.put("title", title);
        data.put("value", value);
        data.put("trend", trend);
        data.put("changeRate", String.format("%.1f%%", changeRate));

        return data;
    }

    /**
     * 生成图表数据
     */
    private Map<String, Object> generateChartData(DashboardComponent component) {
        Map<String, Object> data = new HashMap<>();

        ChartConfig chartConfig = component.getChartConfig();
        String chartType = (chartConfig != null && chartConfig.getType() != null)
                ? chartConfig.getType() : "line";

        List<String> xAxis = Arrays.asList("1月", "2月", "3月", "4月", "5月", "6月");

        List<SeriesData> series = new ArrayList<>();
        series.add(SeriesData.builder()
                .name("数据系列1")
                .data(Arrays.asList(120, 200, 150, 180, 220, 280))
                .build());
        series.add(SeriesData.builder()
                .name("数据系列2")
                .data(Arrays.asList(80, 120, 100, 140, 160, 200))
                .build());

        data.put("chartType", chartType);
        data.put("xAxis", xAxis);
        data.put("series", series);

        return data;
    }

    /**
     * 生成表格数据
     */
    private Map<String, Object> generateTableData(DashboardComponent component) {
        Map<String, Object> data = new HashMap<>();

        List<Map<String, Object>> tableData = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("序号", i);
            row.put("项目", "项目" + i);
            row.put("数值", new Random().nextInt(1000));
            row.put("状态", i % 3 == 0 ? "异常" : "正常");
            tableData.add(row);
        }

        data.put("columns", Arrays.asList("序号", "项目", "数值", "状态"));
        data.put("data", tableData);
        data.put("total", tableData.size());

        return data;
    }

    /**
     * 生成进度条数据
     */
    private Map<String, Object> generateProgressData(DashboardComponent component) {
        Map<String, Object> data = new HashMap<>();

        int percentage = new Random().nextInt(100);
        String status = percentage >= 80 ? "success" : percentage >= 50 ? "normal" : "warning";

        data.put("percentage", percentage);
        data.put("status", status);
        data.put("text", percentage + "%");

        return data;
    }
}
