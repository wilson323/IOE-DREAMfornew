package net.lab1024.sa.video.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.video.dao.VideoWallDao;
import net.lab1024.sa.video.dao.VideoWallWindowDao;
import net.lab1024.sa.video.dao.VideoWallPresetDao;
import net.lab1024.sa.video.entity.VideoWallEntity;
import net.lab1024.sa.video.entity.VideoWallWindowEntity;

import java.util.List;

/**
 * 电视墙管理器
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - Manager层纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 在微服务中通过配置类注册为Spring Bean
 * </p>
 * <p>
 * 核心职责：
 * - 电视墙创建和管理
 * - 窗口布局配置
 * - 预案管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class VideoWallManager {

    private final VideoWallDao videoWallDao;
    private final VideoWallWindowDao videoWallWindowDao;
    private final VideoWallPresetDao videoWallPresetDao;

    /**
     * 构造函数注入依赖
     *
     * @param videoWallDao 电视墙数据访问层
     * @param videoWallWindowDao 窗口数据访问层
     * @param videoWallPresetDao 预案数据访问层
     */
    public VideoWallManager(VideoWallDao videoWallDao,
                           VideoWallWindowDao videoWallWindowDao,
                           VideoWallPresetDao videoWallPresetDao) {
        this.videoWallDao = videoWallDao;
        this.videoWallWindowDao = videoWallWindowDao;
        this.videoWallPresetDao = videoWallPresetDao;
    }

    /**
     * 创建电视墙并初始化窗口
     * <p>
     * 根据行数和列数自动创建窗口布局
     * </p>
     *
     * @param wall 电视墙实体
     * @return 电视墙ID
     */
    public Long createWallWithWindows(VideoWallEntity wall) {
        log.info("[电视墙管理] 创建电视墙: wallName={}, rows={}, cols={}",
                wall.getWallName(), wall.getRows(), wall.getCols());

        // 计算总屏幕数
        if (wall.getRows() != null && wall.getCols() != null) {
            wall.setTotalScreens(wall.getRows() * wall.getCols());
        }

        // 计算总尺寸（如果提供了单屏尺寸）
        if (wall.getScreenWidth() != null && wall.getScreenHeight() != null) {
            if (wall.getCols() != null) {
                wall.setTotalWidth(wall.getScreenWidth() * wall.getCols());
            }
            if (wall.getRows() != null) {
                wall.setTotalHeight(wall.getScreenHeight() * wall.getRows());
            }
        }

        // 保存电视墙
        videoWallDao.insert(wall);

        // 初始化窗口布局
        if (wall.getRows() != null && wall.getCols() != null) {
            initializeWindows(wall.getWallId(), wall.getRows(), wall.getCols(),
                    wall.getScreenWidth(), wall.getScreenHeight());
        }

        log.info("[电视墙管理] 创建电视墙成功: wallId={}, totalScreens={}",
                wall.getWallId(), wall.getTotalScreens());
        return wall.getWallId();
    }

    /**
     * 初始化窗口布局
     * <p>
     * 根据行数和列数创建窗口，自动计算窗口位置和大小
     * </p>
     *
     * @param wallId 电视墙ID
     * @param rows 行数
     * @param cols 列数
     * @param screenWidth 单屏宽度（像素）
     * @param screenHeight 单屏高度（像素）
     */
    private void initializeWindows(Long wallId, Integer rows, Integer cols,
                                   Integer screenWidth, Integer screenHeight) {
        log.info("[电视墙管理] 初始化窗口布局: wallId={}, rows={}, cols={}, screenWidth={}, screenHeight={}",
                wallId, rows, cols, screenWidth, screenHeight);

        int windowNo = 1;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                VideoWallWindowEntity window = new VideoWallWindowEntity();
                window.setWallId(wallId);
                window.setWindowNo(windowNo);
                window.setWindowName("窗口" + windowNo);

                // 计算窗口位置和大小
                if (screenWidth != null && screenHeight != null) {
                    window.setXPos(col * screenWidth);
                    window.setYPos(row * screenHeight);
                    window.setWidth(screenWidth);
                    window.setHeight(screenHeight);
                } else {
                    // 默认尺寸（如果未提供单屏尺寸）
                    window.setXPos(col * 1920);
                    window.setYPos(row * 1080);
                    window.setWidth(1920);
                    window.setHeight(1080);
                }

                window.setZIndex(0);
                window.setStatus(0); // 0-空闲

                videoWallWindowDao.insert(window);
                windowNo++;
            }
        }

        log.info("[电视墙管理] 初始化窗口布局完成: wallId={}, totalWindows={}", wallId, windowNo - 1);
    }

    /**
     * 更新电视墙布局
     * <p>
     * 更新行数和列数，重新初始化窗口布局
     * </p>
     *
     * @param wallId 电视墙ID
     * @param rows 新行数
     * @param cols 新列数
     * @param screenWidth 单屏宽度（像素）
     * @param screenHeight 单屏高度（像素）
     */
    public void updateWallLayout(Long wallId, Integer rows, Integer cols,
                                 Integer screenWidth, Integer screenHeight) {
        log.info("[电视墙管理] 更新电视墙布局: wallId={}, rows={}, cols={}",
                wallId, rows, cols);

        VideoWallEntity wall = videoWallDao.selectById(wallId);
        if (wall == null) {
            throw new RuntimeException("电视墙不存在: " + wallId);
        }

        // 删除旧窗口
        videoWallWindowDao.deleteByWallId(wallId);

        // 更新电视墙配置
        wall.setRows(rows);
        wall.setCols(cols);
        wall.setTotalScreens(rows * cols);
        if (screenWidth != null && screenHeight != null) {
            wall.setScreenWidth(screenWidth);
            wall.setScreenHeight(screenHeight);
            wall.setTotalWidth(screenWidth * cols);
            wall.setTotalHeight(screenHeight * rows);
        }
        videoWallDao.updateById(wall);

        // 重新初始化窗口
        initializeWindows(wallId, rows, cols, screenWidth, screenHeight);

        log.info("[电视墙管理] 更新电视墙布局完成: wallId={}", wallId);
    }

    /**
     * 配置窗口布局
     * <p>
     * 更新指定窗口的位置、大小等配置
     * </p>
     *
     * @param window 窗口实体
     */
    public void configureWindowLayout(VideoWallWindowEntity window) {
        log.info("[电视墙管理] 配置窗口布局: windowId={}, wallId={}, windowNo={}, xPos={}, yPos={}, width={}, height={}",
                window.getWindowId(), window.getWallId(), window.getWindowNo(),
                window.getXPos(), window.getYPos(), window.getWidth(), window.getHeight());

        videoWallWindowDao.updateById(window);

        log.info("[电视墙管理] 配置窗口布局完成: windowId={}", window.getWindowId());
    }

    /**
     * 获取电视墙的窗口列表
     *
     * @param wallId 电视墙ID
     * @return 窗口列表
     */
    public List<VideoWallWindowEntity> getWallWindows(Long wallId) {
        log.debug("[电视墙管理] 获取窗口列表: wallId={}", wallId);
        return videoWallWindowDao.selectByWallId(wallId);
    }
}
