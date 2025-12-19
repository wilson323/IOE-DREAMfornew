package net.lab1024.sa.video.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.video.dao.VideoDecoderDao;
import net.lab1024.sa.video.entity.VideoDecoderEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 解码器管理器
 * <p>
 * 符合CLAUDE.md规范 - Manager层
 * - 纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 在微服务中通过配置类注册为Spring Bean
 * </p>
 * <p>
 * 职责：
 * - 解码器管理编排
 * - 解码器状态监控
 * - 解码通道分配和释放
 * - 解码器能力查询
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class DecoderManager {

    private final VideoDecoderDao videoDecoderDao;

    /**
     * 构造函数
     * <p>
     * 符合CLAUDE.md规范：Manager类通过构造函数接收依赖
     * </p>
     *
     * @param videoDecoderDao 解码器数据访问层
     */
    public DecoderManager(VideoDecoderDao videoDecoderDao) {
        this.videoDecoderDao = videoDecoderDao;
        log.info("[DecoderManager] 初始化解码器管理器");
    }

    /**
     * 注册解码器
     * <p>
     * 解码器首次接入时注册到系统
     * </p>
     *
     * @param decoder 解码器实体
     * @return 解码器ID
     */
    public Long registerDecoder(VideoDecoderEntity decoder) {
        log.info("[解码器管理] 注册解码器: decoderCode={}, decoderName={}, ipAddress={}",
                decoder.getDecoderCode(), decoder.getDecoderName(), decoder.getIpAddress());

        // 检查解码器编码是否已存在
        VideoDecoderEntity existing = videoDecoderDao.selectByCode(decoder.getDecoderCode());
        if (existing != null) {
            log.warn("[解码器管理] 解码器编码已存在: decoderCode={}", decoder.getDecoderCode());
            throw new IllegalArgumentException("解码器编码已存在: " + decoder.getDecoderCode());
        }

        // 设置默认值
        if (decoder.getDeviceStatus() == null) {
            decoder.setDeviceStatus(2); // 默认离线
        }
        if (decoder.getAvailableChannels() == null && decoder.getMaxChannels() != null) {
            decoder.setAvailableChannels(decoder.getMaxChannels());
            decoder.setUsedChannels(0);
        }

        // 保存解码器
        videoDecoderDao.insert(decoder);

        log.info("[解码器管理] 解码器注册成功: decoderId={}", decoder.getDecoderId());
        return decoder.getDecoderId();
    }

    /**
     * 监控解码器状态
     * <p>
     * 更新解码器的在线状态和最后在线时间
     * </p>
     *
     * @param decoderId 解码器ID
     * @param deviceStatus 设备状态：1-在线，2-离线，3-故障
     */
    public void monitorDecoderStatus(Long decoderId, Integer deviceStatus) {
        log.debug("[解码器管理] 更新解码器状态: decoderId={}, deviceStatus={}", decoderId, deviceStatus);

        LocalDateTime lastOnlineTime = (deviceStatus == 1) ? LocalDateTime.now() : null;
        videoDecoderDao.updateStatus(decoderId, deviceStatus, lastOnlineTime);

        log.debug("[解码器管理] 解码器状态更新成功: decoderId={}, deviceStatus={}", decoderId, deviceStatus);
    }

    /**
     * 分配解码通道
     * <p>
     * 为窗口分配可用的解码通道
     * </p>
     *
     * @param wallId 电视墙ID
     * @param windowId 窗口ID
     * @return 解码器ID和通道号的数组 [decoderId, channelNo]，如果分配失败返回null
     */
    public Long[] allocateDecoderChannel(Long wallId, Long windowId) {
        log.info("[解码器管理] 分配解码通道: wallId={}, windowId={}", wallId, windowId);

        // 查询在线且有空闲通道的解码器
        List<VideoDecoderEntity> onlineDecoders = videoDecoderDao.selectOnlineDecoders();

        for (VideoDecoderEntity decoder : onlineDecoders) {
            // 检查是否有可用通道
            if (decoder.getAvailableChannels() != null && decoder.getAvailableChannels() > 0) {
                // 分配第一个可用通道（简化实现，实际应该更智能地选择）
                int channelNo = decoder.getMaxChannels() - decoder.getAvailableChannels() + 1;

                // 更新通道使用情况
                int newUsedChannels = (decoder.getUsedChannels() != null ? decoder.getUsedChannels() : 0) + 1;
                int newAvailableChannels = decoder.getAvailableChannels() - 1;
                videoDecoderDao.updateChannelUsage(decoder.getDecoderId(), newUsedChannels, newAvailableChannels);

                log.info("[解码器管理] 通道分配成功: decoderId={}, channelNo={}", decoder.getDecoderId(), channelNo);
                return new Long[]{decoder.getDecoderId(), (long) channelNo};
            }
        }

        log.warn("[解码器管理] 无可用解码通道: wallId={}, windowId={}", wallId, windowId);
        return null;
    }

    /**
     * 释放解码通道
     * <p>
     * 窗口停止播放时释放解码通道
     * </p>
     *
     * @param decoderId 解码器ID
     * @param channelNo 通道号
     */
    public void releaseDecoderChannel(Long decoderId, Integer channelNo) {
        log.info("[解码器管理] 释放解码通道: decoderId={}, channelNo={}", decoderId, channelNo);

        VideoDecoderEntity decoder = videoDecoderDao.selectById(decoderId);
        if (decoder == null) {
            log.warn("[解码器管理] 解码器不存在: decoderId={}", decoderId);
            return;
        }

        // 更新通道使用情况
        int newUsedChannels = Math.max(0, (decoder.getUsedChannels() != null ? decoder.getUsedChannels() : 0) - 1);
        int newAvailableChannels = Math.min(decoder.getMaxChannels(),
                (decoder.getAvailableChannels() != null ? decoder.getAvailableChannels() : 0) + 1);
        videoDecoderDao.updateChannelUsage(decoderId, newUsedChannels, newAvailableChannels);

        log.info("[解码器管理] 通道释放成功: decoderId={}, channelNo={}", decoderId, channelNo);
    }

    /**
     * 查询解码器详情
     *
     * @param decoderId 解码器ID
     * @return 解码器实体
     */
    public VideoDecoderEntity getDecoderById(Long decoderId) {
        return videoDecoderDao.selectById(decoderId);
    }

    /**
     * 查询在线解码器列表
     *
     * @return 在线解码器列表
     */
    public List<VideoDecoderEntity> getOnlineDecoders() {
        return videoDecoderDao.selectOnlineDecoders();
    }
}
