package net.lab1024.sa.admin.module.smart.access.domain.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * AccessEventEntity 单元测试
 * <p>
 * 测试门禁通行事件实体的字段验证、业务方法和数据完整性
 * 严格遵循repowiki规范：单元测试覆盖率≥90%
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("门禁通行事件实体测试")
class AccessEventEntityTest {

    private AccessEventEntity accessEvent;

    @BeforeEach
    void setUp() {
        accessEvent = new AccessEventEntity();

        // 基础测试数据
        accessEvent.setEventId(1L);
        accessEvent.setEventNo("EVENT_20251116_001");
        accessEvent.setUserId(1001L);
        accessEvent.setUserCode("EMP001");
        accessEvent.setUserName("张三");
        accessEvent.setDepartmentId(10L);
        accessEvent.setDepartmentName("研发部");
        accessEvent.setDeviceId(2001L);
        accessEvent.setDeviceCode("DEV001");
        accessEvent.setDeviceName("主门禁机");
        accessEvent.setAreaId(1L);
        accessEvent.setAreaName("主园区");
        accessEvent.setVerifyMethod(1);
        accessEvent.setVerifyResult(0);
        accessEvent.setCardNo("CARD123456");
        accessEvent.setBiometricId(3001L);
        accessEvent.setFingerprintId("FP001");
        accessEvent.setFaceId("FACE001");
        accessEvent.setDirection(0);
        accessEvent.setEventTime(LocalDateTime.now());
        accessEvent.setDoorOpenTime(LocalDateTime.now().plusSeconds(2));
        accessEvent.setDoorCloseTime(LocalDateTime.now().plusSeconds(10));
        accessEvent.setDuration(8);
        accessEvent.setDoorOpened(1);
        accessEvent.setPhotoPath("/photos/events/20251116_001.jpg");
        accessEvent.setThumbnailPath("/photos/events/20251116_001_thumb.jpg");
        accessEvent.setTemperature(36.5);
        accessEvent.setTemperatureAbnormal(0);
        accessEvent.setMaskStatus(1);
        accessEvent.setVisitorId(4001L);
        accessEvent.setVisitorName("李四");
        accessEvent.setVisitorIdCard("110101199001011234");
        accessEvent.setVisiteeId(2001L);
        accessEvent.setVisiteeName("王五");
        accessEvent.setValidStartTime(LocalDateTime.now().minusDays(1));
        accessEvent.setValidEndTime(LocalDateTime.now().plusDays(1));
        accessEvent.setEventLevel(0);
        accessEvent.setBlacklistEvent(0);
        accessEvent.setAlarmType(0);
        accessEvent.setAlarmDesc("");
        accessEvent.setHandleStatus(0);
        accessEvent.setHandlerId(5001L);
        accessEvent.setHandlerName("管理员");
        accessEvent.setHandleTime(LocalDateTime.now());
        accessEvent.setHandleRemark("已处理");
        accessEvent.setDataSource(0);
        accessEvent.setThirdPartyId("SYS001");
        accessEvent.setRawMessage("{\"status\":\"success\"}");
        accessEvent.setRemark("测试事件");
    }

    @Test
    @DisplayName("测试基础字段设置和获取")
    void testBasicFields() {
        // 验证基础字段
        assertEquals(1L, accessEvent.getEventId());
        assertEquals("EVENT_20251116_001", accessEvent.getEventNo());
        assertEquals(1001L, accessEvent.getUserId());
        assertEquals("EMP001", accessEvent.getUserCode());
        assertEquals("张三", accessEvent.getUserName());
        assertEquals(10L, accessEvent.getDepartmentId());
        assertEquals("研发部", accessEvent.getDepartmentName());
        assertEquals(2001L, accessEvent.getDeviceId());
        assertEquals("DEV001", accessEvent.getDeviceCode());
        assertEquals("主门禁机", accessEvent.getDeviceName());
        assertEquals(1L, accessEvent.getAreaId());
        assertEquals("主园区", accessEvent.getAreaName());
    }

    @Test
    @DisplayName("测试验证方式字段")
    void testVerifyMethodField() {
        // 测试所有有效的验证方式
        Integer[] validMethods = {1, 2, 3, 4, 5, 6, 7}; // 1:刷卡 2:密码 3:指纹 4:人脸 5:二维码 6:IC卡+密码 7:其他

        for (Integer method : validMethods) {
            accessEvent.setVerifyMethod(method);
            assertEquals(method, accessEvent.getVerifyMethod(),
                "验证方式 " + method + " 应该有效");
        }
    }

    @Test
    @DisplayName("测试验证结果字段")
    void testVerifyResultField() {
        // 测试所有有效的验证结果
        Integer[] validResults = {0, 1, 2, 3, 4}; // 0:成功 1:失败 2:超时 3:取消 4:其他

        for (Integer result : validResults) {
            accessEvent.setVerifyResult(result);
            assertEquals(result, accessEvent.getVerifyResult(),
                "验证结果 " + result + " 应该有效");
        }
    }

    @Test
    @DisplayName("测试通行方向字段")
    void testDirectionField() {
        // 测试所有有效的通行方向
        Integer[] validDirections = {0, 1, 2}; // 0:进入 1:外出 2:未知

        for (Integer direction : validDirections) {
            accessEvent.setDirection(direction);
            assertEquals(direction, accessEvent.getDirection(),
                "通行方向 " + direction + " 应该有效");
        }
    }

    @Test
    @DisplayName("测试事件级别字段")
    void testEventLevelField() {
        // 测试所有有效的事件级别
        Integer[] validLevels = {0, 1, 2, 3}; // 0:普通 1:重要 2:紧急 3:异常

        for (Integer level : validLevels) {
            accessEvent.setEventLevel(level);
            assertEquals(level, accessEvent.getEventLevel(),
                "事件级别 " + level + " 应该有效");
        }
    }

    @Test
    @DisplayName("测试报警类型字段")
    void testAlarmTypeField() {
        // 测试所有有效的报警类型
        Integer[] validAlarmTypes = {0, 1, 2, 3, 4}; // 0:无报警 1:非法闯入 2:尾随 3:胁迫 4:其他

        for (Integer alarmType : validAlarmTypes) {
            accessEvent.setAlarmType(alarmType);
            assertEquals(alarmType, accessEvent.getAlarmType(),
                "报警类型 " + alarmType + " 应该有效");
        }
    }

    @Test
    @DisplayName("测试处理状态字段")
    void testHandleStatusField() {
        // 测试所有有效的处理状态
        Integer[] validStatuses = {0, 1, 2}; // 0:未处理 1:已处理 2:忽略

        for (Integer status : validStatuses) {
            accessEvent.setHandleStatus(status);
            assertEquals(status, accessEvent.getHandleStatus(),
                "处理状态 " + status + " 应该有效");
        }
    }

    @Test
    @DisplayName("测试数据来源字段")
    void testDataSourceField() {
        // 测试所有有效的数据来源
        Integer[] validSources = {0, 1, 2, 3}; // 0:设备上报 1:手动录入 2:第三方系统 3:其他

        for (Integer source : validSources) {
            accessEvent.setDataSource(source);
            assertEquals(source, accessEvent.getDataSource(),
                "数据来源 " + source + " 应该有效");
        }
    }

    @Test
    @DisplayName("测试生物特征相关字段")
    void testBiometricFields() {
        // 测试生物特征ID
        accessEvent.setBiometricId(3001L);
        assertEquals(3001L, accessEvent.getBiometricId());

        // 测试指纹模板ID
        accessEvent.setFingerprintId("FP123456");
        assertEquals("FP123456", accessEvent.getFingerprintId());

        // 测试人脸特征ID
        accessEvent.setFaceId("FACE123456");
        assertEquals("FACE123456", accessEvent.getFaceId());
    }

    @Test
    @DisplayName("测试访客相关字段")
    void testVisitorFields() {
        // 测试访客信息
        accessEvent.setVisitorId(4001L);
        accessEvent.setVisitorName("李四");
        accessEvent.setVisitorIdCard("110101199001011234");
        accessEvent.setVisiteeId(2001L);
        accessEvent.setVisiteeName("王五");

        assertEquals(4001L, accessEvent.getVisitorId());
        assertEquals("李四", accessEvent.getVisitorName());
        assertEquals("110101199001011234", accessEvent.getVisitorIdCard());
        assertEquals(2001L, accessEvent.getVisiteeId());
        assertEquals("王五", accessEvent.getVisiteeName());
    }

    @Test
    @DisplayName("测试时间相关字段")
    void testTimeFields() {
        LocalDateTime now = LocalDateTime.now();

        // 测试通行时间
        accessEvent.setEventTime(now);
        assertEquals(now, accessEvent.getEventTime());

        // 测试门锁动作时间
        LocalDateTime doorOpenTime = now.plusSeconds(2);
        accessEvent.setDoorOpenTime(doorOpenTime);
        assertEquals(doorOpenTime, accessEvent.getDoorOpenTime());

        // 测试门锁关闭时间
        LocalDateTime doorCloseTime = now.plusSeconds(10);
        accessEvent.setDoorCloseTime(doorCloseTime);
        assertEquals(doorCloseTime, accessEvent.getDoorCloseTime());

        // 测试有效期时间
        accessEvent.setValidStartTime(now.minusDays(1));
        assertEquals(now.minusDays(1), accessEvent.getValidStartTime());

        accessEvent.setValidEndTime(now.plusDays(1));
        assertEquals(now.plusDays(1), accessEvent.getValidEndTime());

        // 测试处理时间
        accessEvent.setHandleTime(now);
        assertEquals(now, accessEvent.getHandleTime());
    }

    @Test
    @DisplayName("测试持续时间字段")
    void testDurationField() {
        // 测试持续时间范围
        accessEvent.setDuration(0);
        assertEquals(0, accessEvent.getDuration());

        accessEvent.setDuration(3600); // 1小时
        assertEquals(3600, accessEvent.getDuration());

        accessEvent.setDuration(86400); // 24小时
        assertEquals(86400, accessEvent.getDuration());
    }

    @Test
    @DisplayName("测试体温检测字段")
    void testTemperatureFields() {
        // 测试正常体温
        accessEvent.setTemperature(36.5);
        assertEquals(36.5, accessEvent.getTemperature());

        accessEvent.setTemperatureAbnormal(0);
        assertEquals(0, accessEvent.getTemperatureAbnormal());

        // 测试异常体温
        accessEvent.setTemperature(37.8);
        assertEquals(37.8, accessEvent.getTemperature());

        accessEvent.setTemperatureAbnormal(1);
        assertEquals(1, accessEvent.getTemperatureAbnormal());
    }

    @Test
    @DisplayName("测试口罩状态字段")
    void testMaskStatusField() {
        // 测试所有有效的口罩状态
        Integer[] validMaskStatuses = {0, 1, 2}; // 0:未佩戴 1:佩戴 2:未检测

        for (Integer maskStatus : validMaskStatuses) {
            accessEvent.setMaskStatus(maskStatus);
            assertEquals(maskStatus, accessEvent.getMaskStatus(),
                "口罩状态 " + maskStatus + " 应该有效");
        }
    }

    @Test
    @DisplayName("测试照片路径字段")
    void testPhotoFields() {
        // 测试照片路径
        String photoPath = "/photos/events/20251116_001.jpg";
        String thumbnailPath = "/photos/events/20251116_001_thumb.jpg";

        accessEvent.setPhotoPath(photoPath);
        accessEvent.setThumbnailPath(thumbnailPath);

        assertEquals(photoPath, accessEvent.getPhotoPath());
        assertEquals(thumbnailPath, accessEvent.getThumbnailPath());
    }

    @Test
    @DisplayName("测试开门状态字段")
    void testDoorOpenedField() {
        // 测试开门状态
        accessEvent.setDoorOpened(0); // 失败
        assertEquals(0, accessEvent.getDoorOpened());

        accessEvent.setDoorOpened(1); // 成功
        assertEquals(1, accessEvent.getDoorOpened());
    }

    @Test
    @DisplayName("测试开关标志字段")
    void testFlagFields() {
        // 测试各种开关标志
        accessEvent.setBlacklistEvent(0);
        assertEquals(0, accessEvent.getBlacklistEvent());

        accessEvent.setBlacklistEvent(1);
        assertEquals(1, accessEvent.getBlacklistEvent());

        accessEvent.setTemperatureAbnormal(1);
        assertEquals(1, accessEvent.getTemperatureAbnormal());

        accessEvent.setDoorOpened(1);
        assertEquals(1, accessEvent.getDoorOpened());
    }

    @Test
    @DisplayName("测试处理信息字段")
    void testHandleFields() {
        // 测试处理相关信息
        accessEvent.setHandleStatus(1);
        accessEvent.setHandlerId(5001L);
        accessEvent.setHandlerName("管理员");
        accessEvent.setHandleTime(LocalDateTime.now());
        accessEvent.setHandleRemark("已处理");

        assertEquals(1, accessEvent.getHandleStatus());
        assertEquals(5001L, accessEvent.getHandlerId());
        assertEquals("管理员", accessEvent.getHandlerName());
        assertEquals("已处理", accessEvent.getHandleRemark());
    }

    @Test
    @DisplayName("测试原始报文字段")
    void testRawMessageField() {
        // 测试原始报文
        String rawMessage = "{\"status\":\"success\",\"data\":{\"code\":200}}";
        accessEvent.setRawMessage(rawMessage);
        assertEquals(rawMessage, accessEvent.getRawMessage());

        // 测试空报文
        accessEvent.setRawMessage("");
        assertEquals("", accessEvent.getRawMessage());

        accessEvent.setRawMessage(null);
        assertNull(accessEvent.getRawMessage());
    }

    @Test
    @DisplayName("测试非数据库字段")
    void testTransientFields() {
        // 测试展示用字段
        accessEvent.setVerifyMethodName("刷卡");
        assertEquals("刷卡", accessEvent.getVerifyMethodName());

        accessEvent.setVerifyResultName("成功");
        assertEquals("成功", accessEvent.getVerifyResultName());

        accessEvent.setEventLevelName("普通");
        assertEquals("普通", accessEvent.getEventLevelName());

        accessEvent.setNeedHandle(false);
        assertFalse(accessEvent.getNeedHandle());
    }

    @Test
    @DisplayName("测试toString方法")
    void testToString() {
        // 验证toString方法不会抛出异常
        String toString = accessEvent.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("AccessEventEntity"));
        assertTrue(toString.contains("eventNo=EVENT_20251116_001"));
    }

    @Test
    @DisplayName("测试equals和hashCode方法")
    void testEqualsAndHashCode() {
        AccessEventEntity event1 = new AccessEventEntity();
        event1.setEventId(1L);
        event1.setEventNo("EVENT_001");

        AccessEventEntity event2 = new AccessEventEntity();
        event2.setEventId(1L);
        event2.setEventNo("EVENT_001");

        AccessEventEntity event3 = new AccessEventEntity();
        event3.setEventId(2L);
        event3.setEventNo("EVENT_002");

        // 测试相等性
        assertEquals(event1, event2, "相同ID的事件应该相等");
        assertEquals(event1.hashCode(), event2.hashCode(), "相等对象的hashCode应该相同");

        assertNotEquals(event1, event3, "不同ID的事件应该不相等");
        assertNotEquals(event1.hashCode(), event3.hashCode(), "不相等对象的hashCode可能不同");
    }

    @Test
    @DisplayName("测试事件编号格式")
    void testEventNoFormat() {
        // 测试事件编号格式
        accessEvent.setEventNo("EVENT_20251116_001");
        assertEquals("EVENT_20251116_001", accessEvent.getEventNo());

        accessEvent.setEventNo("20251116120000_1001");
        assertEquals("20251116120000_1001", accessEvent.getEventNo());
    }

    @Test
    @DisplayName("测试证件号码格式")
    void testIdCardFormat() {
        // 测试身份证号格式
        accessEvent.setVisitorIdCard("110101199001011234");
        assertEquals("110101199001011234", accessEvent.getVisitorIdCard());

        accessEvent.setVisitorIdCard("PASS123456");
        assertEquals("PASS123456", accessEvent.getVisitorIdCard());
    }
}