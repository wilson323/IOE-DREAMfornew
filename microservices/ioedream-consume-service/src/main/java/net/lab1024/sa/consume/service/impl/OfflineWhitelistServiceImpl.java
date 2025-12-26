package net.lab1024.sa.consume.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.dao.OfflineWhitelistDao;
import net.lab1024.sa.consume.entity.OfflineWhitelistEntity;
import net.lab1024.sa.consume.service.OfflineWhitelistService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 离线消费白名单Service实现
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Service
@Slf4j
public class OfflineWhitelistServiceImpl extends ServiceImpl<OfflineWhitelistDao, OfflineWhitelistEntity>
        implements OfflineWhitelistService {

    @Resource
    private OfflineWhitelistDao whitelistDao;

    @Override
    public List<OfflineWhitelistEntity> getValidWhitelistByUserId(Long userId) {
        log.debug("[离线白名单] 查询用户有效白名单: userId={}", userId);
        return whitelistDao.selectValidByUserId(userId, LocalDateTime.now());
    }

    @Override
    public List<OfflineWhitelistEntity> getValidWhitelistByDeviceId(Long deviceId) {
        log.debug("[离线白名单] 查询设备有效白名单: deviceId={}", deviceId);
        return whitelistDao.selectValidByDeviceId(deviceId, LocalDateTime.now());
    }

    @Override
    public List<OfflineWhitelistEntity> getValidWhitelistByAreaId(Long areaId) {
        log.debug("[离线白名单] 查询区域有效白名单: areaId={}", areaId);
        return whitelistDao.selectValidByAreaId(areaId, LocalDateTime.now());
    }

    @Override
    public boolean isValidWhitelistUser(Long userId, Long deviceId) {
        log.debug("[离线白名单] 验证白名单用户: userId={}, deviceId={}", userId, deviceId);

        List<OfflineWhitelistEntity> whitelist = whitelistDao.selectValidByUserId(userId, LocalDateTime.now());
        boolean isValid = whitelist.stream()
                .anyMatch(w -> w.getDeviceId().equals(deviceId) || w.getDeviceId() == 0);

        log.debug("[离线白名单] 白名单验证结果: userId={}, deviceId={}, valid={}", userId, deviceId, isValid);
        return isValid;
    }

    @Override
    public Integer batchAddWhitelist(List<OfflineWhitelistEntity> whitelist) {
        log.info("[离线白名单] 批量添加白名单: count={}", whitelist.size());

        int successCount = 0;
        for (OfflineWhitelistEntity item : whitelist) {
            try {
                // 设置默认值
                if (item.getEnabled() == null) {
                    item.setEnabled(1);
                }
                if (item.getEffectiveDate() == null) {
                    item.setEffectiveDate(LocalDateTime.now());
                }

                this.save(item);
                successCount++;
            } catch (Exception e) {
                log.error("[离线白名单] 添加白名单失败: userId={}, deviceId={}, error={}",
                        item.getUserId(), item.getDeviceId(), e.getMessage(), e);
            }
        }

        log.info("[离线白名单] 批量添加完成: total={}, success={}", whitelist.size(), successCount);
        return successCount;
    }
}
