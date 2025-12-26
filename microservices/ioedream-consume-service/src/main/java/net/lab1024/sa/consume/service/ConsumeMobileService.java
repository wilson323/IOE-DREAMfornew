package net.lab1024.sa.consume.service;

import java.util.List;
import net.lab1024.sa.consume.domain.form.ConsumeMobileFaceForm;
import net.lab1024.sa.consume.domain.form.ConsumeMobileNfcForm;
import net.lab1024.sa.consume.domain.form.ConsumeMobileQuickForm;
import net.lab1024.sa.consume.domain.form.ConsumeMobileScanForm;
import net.lab1024.sa.consume.domain.form.ConsumeOfflineSyncForm;
import net.lab1024.sa.consume.domain.form.ConsumePermissionValidateForm;
import net.lab1024.sa.consume.domain.vo.ConsumeDeviceConfigVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileMealVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileResultVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileStatsVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileSummaryVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileUserInfoVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileUserVO;
import net.lab1024.sa.consume.domain.vo.ConsumeSyncDataVO;
import net.lab1024.sa.consume.domain.vo.ConsumeSyncResultVO;
import net.lab1024.sa.consume.domain.vo.ConsumeValidateResultVO;

/**
 * 移动端消费服务
 *
 * @author IOE-DREAM Team
 * @since 2025-12-22
 */
public interface ConsumeMobileService {

    ConsumeMobileResultVO quickConsume(ConsumeMobileQuickForm form);

    ConsumeMobileResultVO scanConsume(ConsumeMobileScanForm form);

    ConsumeMobileResultVO nfcConsume(ConsumeMobileNfcForm form);

    ConsumeMobileResultVO faceConsume(ConsumeMobileFaceForm form);

    ConsumeMobileUserVO quickUserInfo(String queryType, String queryValue);

    ConsumeMobileUserInfoVO getUserConsumeInfo(Long userId);

    List<ConsumeMobileMealVO> getAvailableMeals();

    ConsumeDeviceConfigVO getDeviceConfig(Long deviceId);

    ConsumeMobileStatsVO getDeviceTodayStats(Long deviceId);

    ConsumeMobileSummaryVO getTransactionSummary(String areaId);

    ConsumeSyncResultVO syncOfflineTransactions(ConsumeOfflineSyncForm form);

    ConsumeSyncDataVO getSyncData(Long deviceId, String syncTime);

    ConsumeValidateResultVO validateConsumePermission(ConsumePermissionValidateForm form);
}
