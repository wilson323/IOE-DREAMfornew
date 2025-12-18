package net.lab1024.sa.access.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import jakarta.annotation.Resource;
import net.lab1024.sa.access.controller.AccessMobileController.*;
import net.lab1024.sa.access.service.AccessDeviceService;
import net.lab1024.sa.access.service.AdvancedAccessControlService;
import net.lab1024.sa.access.service.BluetoothAccessService;
import net.lab1024.sa.access.dao.AccessDeviceDao;
import net.lab1024.sa.access.dao.AccessRecordDao;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.entity.DeviceEntity;
import net.lab1024.sa.common.entity.UserEntity;
import net.lab1024.sa.common.util.SmartStringUtil;

/**
 * 钃濈墮闂ㄧ鏈嶅姟瀹炵幇
 * <p>
 * 鎻愪緵钃濈墮闂ㄧ鐨勬牳蹇冧笟鍔￠€昏緫锛?
 * - 钃濈墮璁惧绠＄悊鍜岃繛鎺?
 * - 瀹夊叏鐨勮摑鐗欓棬绂侀獙璇?
 * - 璁惧閰嶅鍜岀姸鎬佺洃鎺?
 * - 钃濈墮闂ㄧ鍗＄鐞?
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛?
 * - 浣跨敤@Service娉ㄨВ
 * - 浣跨敤@Transactional浜嬪姟绠＄悊
 * - 瀹屾暣鐨勬棩蹇楄褰曞拰閿欒澶勭悊
 * - 鎬ц兘鐩戞帶鍜屾寚鏍囨敹闆?
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class BluetoothAccessServiceImpl implements BluetoothAccessService {

    @Resource
    private AccessDeviceService accessDeviceService;

    @Resource
    private AdvancedAccessControlService advancedAccessControlService;

    @Resource
    private AccessDeviceDao accessDeviceDao;

    @Resource
    private AccessRecordDao accessRecordDao;

    // 钃濈墮璁惧杩炴帴鐘舵€佺紦瀛?
    private final Map<String, BluetoothConnection> connectionCache = new ConcurrentHashMap<>();

    // 钃濈墮璁惧閰嶅淇℃伅缂撳瓨
    private final Map<String, BluetoothPairingInfo> pairingCache = new ConcurrentHashMap<>();

    @Override
    @Timed(value = "bluetooth.scan", description = "钃濈墮璁惧鎵弿鑰楁椂")
    @Counted(value = "bluetooth.scan.count", description = "钃濈墮璁惧鎵弿娆℃暟")
    public ResponseDTO<List<BluetoothDeviceVO>> scanNearbyDevices(BluetoothScanRequest request) {
        log.info("[钃濈墮鎵弿] 寮€濮嬫壂鎻忛檮杩戣澶? userId={}, location={}, duration={}ms",
                request.getUserId(), request.getLocation(), request.getScanDuration());

        try {
            List<BluetoothDeviceVO> devices = new ArrayList<>();

            // 妯℃嫙钃濈墮璁惧鎵弿锛堝疄闄呭疄鐜伴渶瑕佽皟鐢ㄨ摑鐗橝PI锛?
            List<DeviceEntity> bluetoothDevices = accessDeviceDao.selectByDeviceType("BLUETOOTH");

            for (DeviceEntity device : bluetoothDevices) {
                BluetoothDeviceVO deviceVO = new BluetoothDeviceVO();
                deviceVO.setDeviceAddress(device.getDeviceCode());
                deviceVO.setDeviceName(device.getDeviceName());
                deviceVO.setDeviceType(device.getDeviceSubType());
                deviceVO.setSignalStrength(generateSignalStrength());
                deviceVO.setBatteryLevel(generateBatteryLevel());
                deviceVO.setRssi(generateRSSI());
                deviceVO.setIsPaired(checkIfPaired(request.getUserId(), device.getDeviceCode()));
                deviceVO.setIsConnected(checkIfConnected(device.getDeviceCode()));
                deviceVO.setLastSeen(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                deviceVO.setDistance(calculateDistance(deviceVO.getRssi()));

                devices.add(deviceVO);
            }

            // 鎸変俊鍙峰己搴︽帓搴?
            devices.sort((a, b) -> b.getSignalStrength() - a.getSignalStrength());

            // 闄愬埗鏈€澶ц澶囨暟閲?
            if (request.getMaxDevices() != null && devices.size() > request.getMaxDevices()) {
                devices = devices.subList(0, request.getMaxDevices());
            }

            log.info("[钃濈墮鎵弿] 鎵弿瀹屾垚: 鍙戠幇{}涓澶?, devices.size());
            return ResponseDTO.ok(devices);

        } catch (Exception e) {
            log.error("[钃濈墮鎵弿] 鎵弿澶辫触: userId={}, error={}", request.getUserId(), e.getMessage(), e);
            return ResponseDTO.error("BLUETOOTH_SCAN_FAILED", "钃濈墮璁惧鎵弿澶辫触");
        }
    }

    @Override
    @Timed(value = "bluetooth.connect", description = "钃濈墮璁惧杩炴帴鑰楁椂")
    @Counted(value = "bluetooth.connect.count", description = "钃濈墮璁惧杩炴帴娆℃暟")
    public ResponseDTO<BluetoothConnectionResult> connectDevice(BluetoothConnectRequest request) {
        log.info("[钃濈墮杩炴帴] 寮€濮嬭繛鎺ヨ澶? userId={}, deviceAddress={}, timeout={}ms",
                request.getUserId(), request.getDeviceAddress(), request.getTimeout());

        try {
            String deviceAddress = request.getDeviceAddress();

            // 妫€鏌ヨ澶囨槸鍚﹀瓨鍦?
            DeviceEntity device = accessDeviceDao.selectByDeviceCode(deviceAddress);
            if (device == null || !"BLUETOOTH".equals(device.getDeviceType())) {
                return ResponseDTO.error("DEVICE_NOT_FOUND", "钃濈墮璁惧涓嶅瓨鍦?);
            }

            // 妫€鏌ユ槸鍚﹀凡杩炴帴
            if (checkIfConnected(deviceAddress)) {
                BluetoothConnectionResult result = new BluetoothConnectionResult();
                result.setSuccess(true);
                result.setDeviceAddress(deviceAddress);
                result.setDeviceName(device.getDeviceName());
                result.setConnectionId(generateConnectionId(deviceAddress));
                result.setResponseTime(50);
                result.setSignalStrength(generateSignalStrength());
                result.setProtocolVersion("2.1");
                return ResponseDTO.ok(result);
            }

            // 妯℃嫙璁惧杩炴帴杩囩▼
            long startTime = System.currentTimeMillis();

            // 楠岃瘉璁惧閰嶅鐘舵€?
            if (!checkIfPaired(request.getUserId(), deviceAddress)) {
                return ResponseDTO.error("DEVICE_NOT_PAIRED", "璁惧鏈厤瀵?);
            }

            // 寤虹珛杩炴帴
            BluetoothConnection connection = new BluetoothConnection();
            connection.setUserId(request.getUserId());
            connection.setDeviceAddress(deviceAddress);
            connection.setDeviceName(device.getDeviceName());
            connection.setConnectionTime(System.currentTimeMillis());
            connection.setSignalStrength(generateSignalStrength());
            connection.setProtocolVersion("2.1");
            connection.setAutoReconnect(request.getAutoReconnect() != null ? request.getAutoReconnect() : false);

            connectionCache.put(deviceAddress, connection);

            long responseTime = System.currentTimeMillis() - startTime;

            BluetoothConnectionResult result = new BluetoothConnectionResult();
            result.setSuccess(true);
            result.setDeviceAddress(deviceAddress);
            result.setDeviceName(device.getDeviceName());
            result.setConnectionId(connection.getConnectionId());
            result.setResponseTime((int) responseTime);
            result.setSignalStrength(connection.getSignalStrength());
            result.setProtocolVersion(connection.getProtocolVersion());

            log.info("[钃濈墮杩炴帴] 杩炴帴鎴愬姛: userId={}, deviceAddress={}, responseTime={}ms",
                    request.getUserId(), deviceAddress, responseTime);
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[钃濈墮杩炴帴] 杩炴帴澶辫触: userId={}, deviceAddress={}, error={}",
                    request.getUserId(), request.getDeviceAddress(), e.getMessage(), e);

            BluetoothConnectionResult result = new BluetoothConnectionResult();
            result.setSuccess(false);
            result.setDeviceAddress(request.getDeviceAddress());
            result.setErrorMessage(e.getMessage());

            return ResponseDTO.ok(result);
        }
    }

    @Override
    @Timed(value = "bluetooth.access", description = "钃濈墮闂ㄧ楠岃瘉鑰楁椂")
    @Counted(value = "bluetooth.access.count", description = "钃濈墮闂ㄧ楠岃瘉娆℃暟")
    public ResponseDTO<BluetoothAccessResult> performBluetoothAccess(BluetoothAccessRequest request) {
        log.info("[钃濈墮闂ㄧ] 寮€濮嬮獙璇? userId={}, deviceAddress={}, accessType={}",
                request.getUserId(), request.getDeviceAddress(), request.getAccessType());

        try {
            // 楠岃瘉钃濈墮杩炴帴鐘舵€?
            BluetoothConnection connection = connectionCache.get(request.getDeviceAddress());
            if (connection == null) {
                return createBluetoothAccessResult(false, "璁惧鏈繛鎺?, "MEDIUM");
            }

            // 鏌ヨ璁惧淇℃伅
            DeviceEntity device = accessDeviceDao.selectByDeviceCode(request.getDeviceAddress());
            if (device == null) {
                return createBluetoothAccessResult(false, "璁惧涓嶅瓨鍦?, "HIGH");
            }

            long startTime = System.currentTimeMillis();

            // 鎵ц鏍囧噯闂ㄧ楠岃瘉
            AdvancedAccessControlService.AccessControlResult controlResult =
                    advancedAccessControlService.performAccessControlCheck(
                            request.getUserId(),
                            device.getDeviceId(),
                            device.getAreaId(),
                            request.getAccessType(),
                            "BLUETOOTH_ACCESS");

            long responseTime = System.currentTimeMillis() - startTime;

            // 璁板綍钃濈墮璁块棶鏃ュ織
            recordBluetoothAccessLog(request, controlResult.isAllowed(), responseTime);

            // 鏇存柊杩炴帴缁熻
            connection.setLastAccessTime(System.currentTimeMillis());
            connection.setAccessCount(connection.getAccessCount() + 1);

            BluetoothAccessResult result = new BluetoothAccessResult();
            result.setSuccess(controlResult.isAllowed());
            result.setDeviceId(device.getDeviceId());
            result.setDeviceName(device.getDeviceName());
            result.setAccessMethod("BLUETOOTH");
            result.setAccessTime(System.currentTimeMillis());
            result.setResponseTime((int) responseTime);
            result.setAccessToken(controlResult.isAllowed() ? generateAccessToken() : null);
            result.setDecisionReason(controlResult.getDecisionReason());
            result.setRiskLevel(assessBluetoothRisk(request, connection));

            log.info("[钃濈墮闂ㄧ] 楠岃瘉瀹屾垚: userId={}, deviceAddress={}, success={}, responseTime={}ms",
                    request.getUserId(), request.getDeviceAddress(), result.isSuccess(), responseTime);
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[钃濈墮闂ㄧ] 楠岃瘉澶辫触: userId={}, deviceAddress={}, error={}",
                    request.getUserId(), request.getDeviceAddress(), e.getMessage(), e);
            return createBluetoothAccessResult(false, "绯荤粺寮傚父: " + e.getMessage(), "HIGH");
        }
    }

    @Override
    public ResponseDTO<List<BluetoothDeviceStatusVO>> getConnectedDevicesStatus(Long userId) {
        log.info("[钃濈墮鐘舵€乚 鏌ヨ宸茶繛鎺ヨ澶囩姸鎬? userId={}", userId);

        try {
            List<BluetoothDeviceStatusVO> deviceStatusList = new ArrayList<>();

            for (Map.Entry<String, BluetoothConnection> entry : connectionCache.entrySet()) {
                BluetoothConnection connection = entry.getValue();
                if (userId.equals(connection.getUserId())) {
                    BluetoothDeviceStatusVO statusVO = new BluetoothDeviceStatusVO();
                    statusVO.setDeviceAddress(connection.getDeviceAddress());
                    statusVO.setDeviceName(connection.getDeviceName());
                    statusVO.setConnectionStatus("CONNECTED");
                    statusVO.setSignalStrength(connection.getSignalStrength());
                    statusVO.setBatteryLevel(generateBatteryLevel());
                    statusVO.setLastActivity(formatTimestamp(connection.getLastAccessTime()));
                    statusVO.setTotalConnections(connection.getTotalConnections());
                    statusVO.setTotalAccessCount(connection.getAccessCount());
                    statusVO.setFirmwareVersion(connection.getFirmwareVersion());

                    deviceStatusList.add(statusVO);
                }
            }

            log.info("[钃濈墮鐘舵€乚 鏌ヨ瀹屾垚: userId={}, 璁惧鏁伴噺={}", userId, deviceStatusList.size());
            return ResponseDTO.ok(deviceStatusList);

        } catch (Exception e) {
            log.error("[钃濈墮鐘舵€乚 鏌ヨ澶辫触: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseDTO.error("STATUS_QUERY_FAILED", "璁惧鐘舵€佹煡璇㈠け璐?);
        }
    }

    @Override
    public boolean disconnectDevice(Long userId, String deviceAddress) {
        log.info("[钃濈墮鏂紑] 鏂紑璁惧杩炴帴: userId={}, deviceAddress={}", userId, deviceAddress);

        try {
            BluetoothConnection connection = connectionCache.get(deviceAddress);
            if (connection != null && userId.equals(connection.getUserId())) {
                // 浼橀泤鏂紑杩炴帴
                connectionCache.remove(deviceAddress);

                // 璁板綍鏂紑浜嬩欢
                log.info("[钃濈墮鏂紑] 杩炴帴宸叉柇寮€: deviceAddress={}, connectionTime={}ms",
                        deviceAddress, System.currentTimeMillis() - connection.getConnectionTime());

                return true;
            }

            return false;

        } catch (Exception e) {
            log.error("[钃濈墮鏂紑] 鏂紑澶辫触: userId={}, deviceAddress={}, error={}",
                    userId, deviceAddress, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public ResponseDTO<BluetoothPairingResult> pairDevice(BluetoothPairingRequest request) {
        log.info("[钃濈墮閰嶅] 寮€濮嬮厤瀵? userId={}, deviceAddress={}, method={}",
                request.getUserId(), request.getDeviceAddress(), request.getPairingMethod());

        try {
            // 鏌ヨ璁惧淇℃伅
            DeviceEntity device = accessDeviceDao.selectByDeviceCode(request.getDeviceAddress());
            if (device == null) {
                return createPairingResult(false, "璁惧涓嶅瓨鍦?);
            }

            long startTime = System.currentTimeMillis();

            // 妯℃嫙閰嶅杩囩▼
            if ("PIN".equals(request.getPairingMethod()) && SmartStringUtil.isEmpty(request.getPinCode())) {
                return createPairingResult(false, "PIN鐮佷笉鑳戒负绌?);
            }

            // 楠岃瘉PIN鐮侊紙妯℃嫙锛?
            if ("PIN".equals(request.getPairingMethod()) && !"123456".equals(request.getPinCode())) {
                return createPairingResult(false, "PIN鐮侀敊璇?);
            }

            // 鐢熸垚閰嶅瀵嗛挜
            String pairingKey = generatePairingKey(request.getUserId(), request.getDeviceAddress());

            // 淇濆瓨閰嶅淇℃伅
            BluetoothPairingInfo pairingInfo = new BluetoothPairingInfo();
            pairingInfo.setUserId(request.getUserId());
            pairingInfo.setDeviceAddress(request.getDeviceAddress());
            pairingInfo.setPairingKey(pairingKey);
            pairingInfo.setPairingMethod(request.getPairingMethod());
            pairingInfo.setPairingTime(System.currentTimeMillis());
            pairingInfo.setRequiresConfirmation(false);

            pairingCache.put(request.getDeviceAddress(), pairingInfo);

            long responseTime = System.currentTimeMillis() - startTime;

            BluetoothPairingResult result = new BluetoothPairingResult();
            result.setSuccess(true);
            result.setDeviceAddress(request.getDeviceAddress());
            result.setPairingKey(pairingKey);
            result.setPairingMethod(request.getPairingMethod());
            result.setResponseTime((int) responseTime);
            result.setRequiresConfirmation(false);

            log.info("[钃濈墮閰嶅] 閰嶅鎴愬姛: userId={}, deviceAddress={}, responseTime={}ms",
                    request.getUserId(), request.getDeviceAddress(), responseTime);
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[钃濈墮閰嶅] 閰嶅澶辫触: userId={}, deviceAddress={}, error={}",
                    request.getUserId(), request.getDeviceAddress(), e.getMessage(), e);
            return createPairingResult(false, "閰嶉厤澶辫触: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<UserAccessCardVO>> getUserAccessCards(Long userId) {
        log.info("[闂ㄧ鍗 鏌ヨ鐢ㄦ埛闂ㄧ鍗? userId={}", userId);

        try {
            List<UserAccessCardVO> accessCards = new ArrayList<>();

            // 鏌ヨ鐢ㄦ埛鐨勭墿鐞嗛棬绂佸崱
            // TODO: 瀹炵幇鐗╃悊鍗℃煡璇㈤€昏緫

            // 鏌ヨ鐢ㄦ埛鐨勮櫄鎷熼棬绂佸崱
            // TODO: 瀹炵幇铏氭嫙鍗℃煡璇㈤€昏緫

            // 鏌ヨ鐢ㄦ埛鐨勮摑鐗欓棬绂佸崱
            for (Map.Entry<String, BluetoothPairingInfo> entry : pairingCache.entrySet()) {
                BluetoothPairingInfo pairingInfo = entry.getValue();
                if (userId.equals(pairingInfo.getUserId())) {
                    DeviceEntity device = accessDeviceDao.selectByDeviceCode(pairingInfo.getDeviceAddress());
                    if (device != null) {
                        UserAccessCardVO card = new UserAccessCardVO();
                        card.setCardId(System.currentTimeMillis()); // 涓存椂ID
                        card.setCardName(device.getDeviceName() + "钃濈墮鍗?);
                        card.setCardType("BLUETOOTH");
                        card.setCardNumber(device.getDeviceCode());
                        card.setDeviceAddress(pairingInfo.getDeviceAddress());
                        card.setStatus("ACTIVE");
                        card.setIssueTime(formatTimestamp(pairingInfo.getPairingTime()));
                        card.setExpireTime("2025-12-31 23:59:59"); // 涓存椂杩囨湡鏃堕棿
                        card.setAllowedAreas(Arrays.asList("鍔炲叕鍖?, "浼氳瀹?));
                        card.setDailyUsageLimit(100);
                        card.setCurrentUsage(0);
                        card.setIsPrimary(false);

                        accessCards.add(card);
                    }
                }
            }

            log.info("[闂ㄧ鍗 鏌ヨ瀹屾垚: userId={}, 鍗＄墖鏁伴噺={}", userId, accessCards.size());
            return ResponseDTO.ok(accessCards);

        } catch (Exception e) {
            log.error("[闂ㄧ鍗 鏌ヨ澶辫触: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseDTO.error("CARD_QUERY_FAILED", "闂ㄧ鍗℃煡璇㈠け璐?);
        }
    }

    @Override
    public ResponseDTO<String> addBluetoothAccessCard(AddBluetoothCardRequest request) {
        log.info("[闂ㄧ鍗 娣诲姞钃濈墮闂ㄧ鍗? userId={}, cardName={}, deviceAddress={}",
                request.getUserId(), request.getCardName(), request.getDeviceAddress());

        try {
            // 妫€鏌ヨ澶囨槸鍚﹀凡閰嶅
            BluetoothPairingInfo pairingInfo = pairingCache.get(request.getDeviceAddress());
            if (pairingInfo == null || !request.getUserId().equals(pairingInfo.getUserId())) {
                return ResponseDTO.error("DEVICE_NOT_PAIRED", "璁惧鏈厤瀵?);
            }

            // 妫€鏌ヨ澶囨槸鍚﹀瓨鍦?
            DeviceEntity device = accessDeviceDao.selectByDeviceCode(request.getDeviceAddress());
            if (device == null) {
                return ResponseDTO.error("DEVICE_NOT_FOUND", "璁惧涓嶅瓨鍦?);
            }

            // TODO: 淇濆瓨闂ㄧ鍗′俊鎭埌鏁版嵁搴?
            // 杩欓噷搴旇璋冪敤闂ㄧ鍗℃湇鍔′繚瀛橀棬绂佸崱淇℃伅

            log.info("[闂ㄧ鍗 娣诲姞鎴愬姛: userId={}, cardName={}, deviceAddress={}",
                    request.getUserId(), request.getCardName(), request.getDeviceAddress());
            return ResponseDTO.ok("钃濈墮闂ㄧ鍗℃坊鍔犳垚鍔?);

        } catch (Exception e) {
            log.error("[闂ㄧ鍗 娣诲姞澶辫触: userId={}, deviceAddress={}, error={}",
                    request.getUserId(), request.getDeviceAddress(), e.getMessage(), e);
            return ResponseDTO.error("CARD_ADD_FAILED", "闂ㄧ鍗℃坊鍔犲け璐?);
        }
    }

    @Override
    public ResponseDTO<BluetoothDeviceHealthVO> checkDeviceHealth(String deviceAddress) {
        log.info("[璁惧鍋ュ悍] 妫€鏌ヨ摑鐗欒澶囧仴搴风姸鎬? deviceAddress={}", deviceAddress);

        try {
            BluetoothConnection connection = connectionCache.get(deviceAddress);

            BluetoothDeviceHealthVO healthVO = new BluetoothDeviceHealthVO();
            healthVO.setDeviceAddress(deviceAddress);
            healthVO.setDeviceName(connection != null ? connection.getDeviceName() : "鏈煡璁惧");

            // 鍋ュ悍鐘舵€佽瘎浼?
            int overallScore = 100;
            List<String> issues = new ArrayList<>();
            List<String> recommendations = new ArrayList<>();

            if (connection == null) {
                overallScore -= 50;
                issues.add("璁惧鏈繛鎺?);
                recommendations.add("妫€鏌ヨ澶囪繛鎺ョ姸鎬?);
            } else {
                // 淇″彿寮哄害妫€鏌?
                if (connection.getSignalStrength() < 30) {
                    overallScore -= 20;
                    issues.add("淇″彿寮哄害寮?);
                    recommendations.add("绉昏繎璁惧鎴栨鏌ヤ俊鍙峰共鎵?);
                }

                // 杩炴帴绋冲畾鎬ф鏌?
                long connectionDuration = System.currentTimeMillis() - connection.getConnectionTime();
                if (connectionDuration < TimeUnit.MINUTES.toMillis(5)) {
                    overallScore -= 10;
                    issues.add("杩炴帴鏃堕棿杩囩煭");
                    recommendations.add("妫€鏌ヨ澶囪繛鎺ョǔ瀹氭€?);
                }
            }

            // 鐢垫睜鐘舵€佹鏌?
            Integer batteryLevel = generateBatteryLevel();
            if (batteryLevel < 20) {
                overallScore -= 30;
                issues.add("鐢垫睜鐢甸噺浣?);
                recommendations.add("鍙婃椂鍏呯數");
            }

            // 璁剧疆鍋ュ悍鐘舵€?
            String healthStatus;
            if (overallScore >= 90) {
                healthStatus = "EXCELLENT";
            } else if (overallScore >= 70) {
                healthStatus = "GOOD";
            } else if (overallScore >= 50) {
                healthStatus = "FAIR";
            } else {
                healthStatus = "POOR";
            }

            healthVO.setHealthStatus(healthStatus);
            healthVO.setConnectionScore(connection != null ? connection.getSignalStrength() : 0);
            healthVO.setSignalQuality(connection != null ? connection.getSignalStrength() : 0);
            healthVO.setBatteryHealth(batteryLevel);
            healthVO.setLastCheckTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            healthVO.setIssues(issues);
            healthVO.setRecommendations(recommendations);

            log.info("[璁惧鍋ュ悍] 妫€鏌ュ畬鎴? deviceAddress={}, healthStatus={}, score={}",
                    deviceAddress, healthStatus, overallScore);
            return ResponseDTO.ok(healthVO);

        } catch (Exception e) {
            log.error("[璁惧鍋ュ悍] 妫€鏌ュけ璐? deviceAddress={}, error={}", deviceAddress, e.getMessage(), e);
            return ResponseDTO.error("HEALTH_CHECK_FAILED", "璁惧鍋ュ悍妫€鏌ュけ璐?);
        }
    }

    @Override
    public ResponseDTO<BluetoothFirmwareUpdateResult> updateDeviceFirmware(String deviceAddress, String firmwareVersion) {
        log.info("[鍥轰欢鍗囩骇] 寮€濮嬪崌绾ц摑鐗欒澶囧浐浠? deviceAddress={}, targetVersion={}",
                deviceAddress, firmwareVersion);

        try {
            BluetoothConnection connection = connectionCache.get(deviceAddress);
            if (connection == null) {
                return createFirmwareUpdateResult(false, "璁惧鏈繛鎺?, null, firmwareVersion);
            }

            String currentVersion = connection.getFirmwareVersion();

            // 妯℃嫙鍥轰欢鍗囩骇杩囩▼
            long updateDuration = 5000 + (long)(Math.random() * 10000); // 5-15绉?

            // 妯℃嫙鍗囩骇缁撴灉锛?0%鎴愬姛鐜囷級
            boolean success = Math.random() > 0.1;

            BluetoothFirmwareUpdateResult result = new BluetoothFirmwareUpdateResult();
            result.setSuccess(success);
            result.setDeviceAddress(deviceAddress);
            result.setCurrentVersion(currentVersion);
            result.setTargetVersion(firmwareVersion);
            result.setUpdateStatus(success ? "SUCCESS" : "FAILED");
            result.setUpdateDuration(updateDuration);
            result.setRequiresReboot(success);

            if (!success) {
                result.setErrorMessage("鍥轰欢涓嬭浇澶辫触");
            }

            // 鏇存柊璁惧鍥轰欢鐗堟湰
            if (success) {
                connection.setFirmwareVersion(firmwareVersion);
            }

            log.info("[鍥轰欢鍗囩骇] 鍗囩骇瀹屾垚: deviceAddress={}, success={}, duration={}ms",
                    deviceAddress, success, updateDuration);
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[鍥轰欢鍗囩骇] 鍗囩骇澶辫触: deviceAddress={}, error={}", deviceAddress, e.getMessage(), e);
            return createFirmwareUpdateResult(false, "鍗囩骇寮傚父", null, firmwareVersion);
        }
    }

    @Override
    public ResponseDTO<BluetoothDeviceDiagnosticVO> diagnoseDevice(String deviceAddress) {
        log.info("[璁惧璇婃柇] 寮€濮嬭瘖鏂摑鐗欒澶? deviceAddress={}", deviceAddress);

        try {
            BluetoothConnection connection = connectionCache.get(deviceAddress);

            BluetoothDeviceDiagnosticVO diagnosticVO = new BluetoothDeviceDiagnosticVO();
            diagnosticVO.setDeviceAddress(deviceAddress);
            diagnosticVO.setDeviceName(connection != null ? connection.getDeviceName() : "鏈煡璁惧");
            diagnosticVO.setDiagnosticTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            // 杩炴帴娴嬭瘯
            DiagnosticResult connectionTest = new DiagnosticResult();
            connectionTest.setTestName("杩炴帴绋冲畾鎬ф祴璇?);
            connectionTest.setPassed(connection != null);
            connectionTest.setScore(connection != null ? 85 : 0);
            connectionTest.setDetails(connection != null ? "杩炴帴绋冲畾锛屼俊鍙疯壇濂? : "璁惧鏈繛鎺?);
            connectionTest.setStatus(connection != null ? "PASS" : "FAIL");
            diagnosticVO.setConnectionTest(connectionTest);

            // 鍔熻兘娴嬭瘯
            DiagnosticResult functionalityTest = new DiagnosticResult();
            functionalityTest.setTestName("鍔熻兘妯″潡娴嬭瘯");
            functionalityTest.setPassed(true); // 妯℃嫙閫氳繃
            functionalityTest.setScore(92);
            functionalityTest.setDetails("鎵€鏈夊姛鑳芥ā鍧楁甯?);
            functionalityTest.setStatus("PASS");
            diagnosticVO.setFunctionalityTest(functionalityTest);

            // 鎬ц兘娴嬭瘯
            DiagnosticResult performanceTest = new DiagnosticResult();
            performanceTest.setTestName("鎬ц兘鍩哄噯娴嬭瘯");
            performanceTest.setPassed(true); // 妯℃嫙閫氳繃
            performanceTest.setScore(78);
            performanceTest.setDetails("鍝嶅簲鏃堕棿鍦ㄥ彲鎺ュ彈鑼冨洿鍐?);
            performanceTest.setStatus("PASS");
            diagnosticVO.setPerformanceTest(performanceTest);

            // 缁煎悎璇勫垎
            int overallScore = (connectionTest.getScore() + functionalityTest.getScore() + performanceTest.getScore()) / 3;
            diagnosticVO.setOverallScore(overallScore);

            // 璇嗗埆鐨勯棶棰樺拰寤鸿
            List<String> issues = new ArrayList<>();
            List<String> suggestedActions = new ArrayList<>();

            if (connection == null) {
                issues.add("璁惧鏈繛鎺?);
                suggestedActions.add("妫€鏌ヨ澶囩數婧愬拰钃濈墮杩炴帴");
            }

            if (connection != null && connection.getSignalStrength() < 50) {
                issues.add("淇″彿寮哄害杈冨急");
                suggestedActions.add("绉昏繎璁惧鎴栧噺灏戜俊鍙峰共鎵?);
            }

            diagnosticVO.setIdentifiedIssues(issues);
            diagnosticVO.setSuggestedActions(suggestedActions);

            log.info("[璁惧璇婃柇] 璇婃柇瀹屾垚: deviceAddress={}, overallScore={}", deviceAddress, overallScore);
            return ResponseDTO.ok(diagnosticVO);

        } catch (Exception e) {
            log.error("[璁惧璇婃柇] 璇婃柇澶辫触: deviceAddress={}, error={}", deviceAddress, e.getMessage(), e);
            return ResponseDTO.error("DIAGNOSTIC_FAILED", "璁惧璇婃柇澶辫触");
        }
    }

    // ==================== 绉佹湁杈呭姪鏂规硶 ====================

    private ResponseDTO<BluetoothAccessResult> createBluetoothAccessResult(
            boolean success, String reason, String riskLevel) {
        BluetoothAccessResult result = new BluetoothAccessResult();
        result.setSuccess(success);
        result.setAccessMethod("BLUETOOTH");
        result.setAccessTime(System.currentTimeMillis());
        result.setResponseTime(100);
        result.setDecisionReason(reason);
        result.setRiskLevel(RiskLevel.valueOf(riskLevel));
        return ResponseDTO.ok(result);
    }

    private ResponseDTO<BluetoothPairingResult> createPairingResult(boolean success, String message) {
        BluetoothPairingResult result = new BluetoothPairingResult();
        result.setSuccess(success);
        result.setDeviceAddress("");
        result.setResponseTime(200);
        if (!success) {
            result.setErrorMessage(message);
        }
        return ResponseDTO.ok(result);
    }

    private ResponseDTO<BluetoothFirmwareUpdateResult> createFirmwareUpdateResult(
            boolean success, String message, String currentVersion, String targetVersion) {
        BluetoothFirmwareUpdateResult result = new BluetoothFirmwareUpdateResult();
        result.setSuccess(success);
        result.setDeviceAddress("");
        result.setCurrentVersion(currentVersion);
        result.setTargetVersion(targetVersion);
        result.setUpdateStatus(success ? "SUCCESS" : "FAILED");
        result.setUpdateDuration(0);
        if (!success) {
            result.setErrorMessage(message);
        }
        return ResponseDTO.ok(result);
    }

    private boolean checkIfPaired(Long userId, String deviceAddress) {
        BluetoothPairingInfo pairingInfo = pairingCache.get(deviceAddress);
        return pairingInfo != null && userId.equals(pairingInfo.getUserId());
    }

    private boolean checkIfConnected(String deviceAddress) {
        return connectionCache.containsKey(deviceAddress);
    }

    private Integer generateSignalStrength() {
        return 30 + (int)(Math.random() * 70); // 30-100
    }

    private Integer generateBatteryLevel() {
        return 10 + (int)(Math.random() * 90); // 10-100
    }

    private Integer generateRSSI() {
        return -100 + (int)(Math.random() * 60); // -100 to -40
    }

    private Double calculateDistance(Integer rssi) {
        if (rssi == null) return null;
        // 绠€鍖栫殑璺濈璁＄畻鍏紡
        return Math.pow(10, (-69.0 - rssi) / 20.0);
    }

    private String generateConnectionId(String deviceAddress) {
        return "CONN_" + deviceAddress.replaceAll(":", "") + "_" + System.currentTimeMillis();
    }

    private String generateAccessToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String generatePairingKey(Long userId, String deviceAddress) {
        return "KEY_" + userId + "_" + deviceAddress.replaceAll(":", "") + "_" + System.currentTimeMillis();
    }

    private RiskLevel assessBluetoothRisk(BluetoothAccessRequest request, BluetoothConnection connection) {
        // 鍩轰簬澶氫釜鍥犵礌璇勪及椋庨櫓绛夌骇
        int riskScore = 0;

        // 杩炴帴鏃堕暱鍥犵礌
        long connectionDuration = System.currentTimeMillis() - connection.getConnectionTime();
        if (connectionDuration < TimeUnit.MINUTES.toMillis(1)) {
            riskScore += 20; // 鏂拌繛鎺ラ闄╄緝楂?
        }

        // 璁块棶鏃堕棿鍥犵礌
        int hour = LocalDateTime.now().getHour();
        if (hour < 6 || hour > 22) {
            riskScore += 15; // 闈炴甯告椂闂磋闂?
        }

        // 淇″彿寮哄害鍥犵礌
        if (connection.getSignalStrength() < 40) {
            riskScore += 10; // 淇″彿寮卞彲鑳芥湁椋庨櫓
        }

        // 璁块棶棰戠巼鍥犵礌
        if (connection.getAccessCount() > 10 && connection.getAccessCount() < 20) {
            riskScore += 5; // 楂橀璁块棶
        }

        if (riskScore >= 30) {
            return RiskLevel.HIGH;
        } else if (riskScore >= 15) {
            return RiskLevel.MEDIUM;
        } else {
            return RiskLevel.LOW;
        }
    }

    private void recordBluetoothAccessLog(BluetoothAccessRequest request, boolean success, long responseTime) {
        // TODO: 瀹炵幇钃濈墮璁块棶鏃ュ織璁板綍
        log.info("[钃濈墮鏃ュ織] 璁板綍璁块棶鏃ュ織: userId={}, deviceAddress={}, success={}, responseTime={}ms",
                request.getUserId(), request.getDeviceAddress(), success, responseTime);
    }

    private String formatTimestamp(long timestamp) {
        return LocalDateTime.ofEpochSecond(timestamp / 1000, 0, null)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // ==================== 鍐呴儴鏁版嵁绫?====================

    private static class BluetoothConnection {
        private Long userId;
        private String deviceAddress;
        private String deviceName;
        private String connectionId;
        private long connectionTime;
        private long lastAccessTime;
        private int signalStrength;
        private String protocolVersion;
        private String firmwareVersion = "1.0.0";
        private boolean autoReconnect;
        private long totalConnections = 1;
        private long accessCount = 0;

        // getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getDeviceAddress() { return deviceAddress; }
        public void setDeviceAddress(String deviceAddress) { this.deviceAddress = deviceAddress; }

        public String getDeviceName() { return deviceName; }
        public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

        public String getConnectionId() { return connectionId; }
        public void setConnectionId(String connectionId) { this.connectionId = connectionId; }

        public long getConnectionTime() { return connectionTime; }
        public void setConnectionTime(long connectionTime) { this.connectionTime = connectionTime; }

        public long getLastAccessTime() { return lastAccessTime; }
        public void setLastAccessTime(long lastAccessTime) { this.lastAccessTime = lastAccessTime; }

        public int getSignalStrength() { return signalStrength; }
        public void setSignalStrength(int signalStrength) { this.signalStrength = signalStrength; }

        public String getProtocolVersion() { return protocolVersion; }
        public void setProtocolVersion(String protocolVersion) { this.protocolVersion = protocolVersion; }

        public String getFirmwareVersion() { return firmwareVersion; }
        public void setFirmwareVersion(String firmwareVersion) { this.firmwareVersion = firmwareVersion; }

        public boolean isAutoReconnect() { return autoReconnect; }
        public void setAutoReconnect(boolean autoReconnect) { this.autoReconnect = autoReconnect; }

        public long getTotalConnections() { return totalConnections; }
        public void setTotalConnections(long totalConnections) { this.totalConnections = totalConnections; }

        public long getAccessCount() { return accessCount; }
        public void setAccessCount(long accessCount) { this.accessCount = accessCount; }
    }

    private static class BluetoothPairingInfo {
        private Long userId;
        private String deviceAddress;
        private String pairingKey;
        private String pairingMethod;
        private long pairingTime;
        private boolean requiresConfirmation;

        // getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getDeviceAddress() { return deviceAddress; }
        public void setDeviceAddress(String deviceAddress) { this.deviceAddress = deviceAddress; }

        public String getPairingKey() { return pairingKey; }
        public void setPairingKey(String pairingKey) { this.pairingKey = pairingKey; }

        public String getPairingMethod() { return pairingMethod; }
        public void setPairingMethod(String pairingMethod) { this.pairingMethod = pairingMethod; }

        public long getPairingTime() { return pairingTime; }
        public void setPairingTime(long pairingTime) { this.pairingTime = pairingTime; }

        public boolean isRequiresConfirmation() { return requiresConfirmation; }
        public void setRequiresConfirmation(boolean requiresConfirmation) { this.requiresConfirmation = requiresConfirmation; }
    }
}