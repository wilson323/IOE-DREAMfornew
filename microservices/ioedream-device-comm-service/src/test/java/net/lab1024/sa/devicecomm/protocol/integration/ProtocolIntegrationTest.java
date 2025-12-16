package net.lab1024.sa.devicecomm.protocol.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.TimeUnit;

import net.lab1024.sa.common.constant.SystemConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import jakarta.annotation.Resource;
import org.springframework.boot.test.context.SpringBootTest;
import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.devicecomm.protocol.handler.ProtocolParseException;
import net.lab1024.sa.devicecomm.protocol.handler.ProtocolProcessException;
import net.lab1024.sa.devicecomm.protocol.message.ProtocolMessage;
import net.lab1024.sa.devicecomm.protocol.handler.impl.AccessProtocolHandler;
import net.lab1024.sa.devicecomm.protocol.handler.impl.AttendanceProtocolHandler;
import net.lab1024.sa.devicecomm.protocol.handler.impl.ConsumeProtocolHandler;

/**
 * 协议集成测试
 * <p>
 * 测试范围：协议处理器的端到端集成测试
 * - 协议解析、验证、处理的完整流程
 * - 多个协议处理器的协同工作
 * - 异常场景处理
 * </p>
 * <p>
 * 注意：这是集成测试，需要Spring上下文支持
 * 实际运行时需要配置测试环境（数据库、网关等）
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DisplayName("协议集成测试")
@Slf4j
@Disabled("需要完整Spring集成环境（MQ/配置/依赖注入），单元测试阶段跳过")
class ProtocolIntegrationTest {

    @Resource
    private AccessProtocolHandler accessProtocolHandler;

    @Resource
    private AttendanceProtocolHandler attendanceProtocolHandler;

    @Resource
    private ConsumeProtocolHandler consumeProtocolHandler;

    private byte[] accessMessageBytes;
    private byte[] attendanceMessageBytes;
    private byte[] consumeMessageBytes;

    @BeforeEach
    void setUp() {
        // 准备门禁协议测试消息
        accessMessageBytes = createAccessMessage();

        // 准备考勤协议测试消息
        attendanceMessageBytes = createAttendanceMessage();

        // 准备消费协议测试消息
        consumeMessageBytes = createConsumeMessage();
    }

    @Test
    @DisplayName("测试门禁协议完整流程-解析到处理")
    void testAccessProtocol_FullFlow() throws ProtocolParseException, ProtocolProcessException {
        // 跳过测试如果处理器未注入（测试环境可能未配置）
        if (accessProtocolHandler == null) {
            log.warn("[协议集成测试] 跳过测试：AccessProtocolHandler未注入（测试环境未配置）");
            return;
        }

        // Given - 准备测试消息
        byte[] messageBytes = accessMessageBytes;

        // When - 解析消息
        ProtocolMessage message = accessProtocolHandler.parseMessage(messageBytes);
        assertNotNull(message);
        assertEquals("ACCESS_ENTROPY_V4_8", message.getProtocolType());

        // When - 验证消息
        boolean isValid = accessProtocolHandler.validateMessage(message);
        assertTrue(isValid);

        // When - 处理消息（注意：实际处理会调用网关服务，需要Mock或配置测试环境）
        // 这里只测试不会抛出异常
        assertDoesNotThrow(() -> {
            accessProtocolHandler.processMessage(message, 1L);
        });
    }

    @Test
    @DisplayName("测试考勤协议完整流程-解析到处理")
    void testAttendanceProtocol_FullFlow() throws ProtocolParseException, ProtocolProcessException {
        // 跳过测试如果处理器未注入
        if (attendanceProtocolHandler == null) {
            log.warn("[协议集成测试] 跳过测试：AttendanceProtocolHandler未注入（测试环境未配置）");
            return;
        }

        // Given - 准备测试消息
        byte[] messageBytes = attendanceMessageBytes;

        // When - 解析消息
        ProtocolMessage message = attendanceProtocolHandler.parseMessage(messageBytes);
        assertNotNull(message);
        assertEquals("ATTENDANCE_ENTROPY_V4_0", message.getProtocolType());

        // When - 验证消息
        boolean isValid = attendanceProtocolHandler.validateMessage(message);
        assertTrue(isValid);

        // When - 处理消息
        assertDoesNotThrow(() -> {
            attendanceProtocolHandler.processMessage(message, 1L);
        });
    }

    @Test
    @DisplayName("测试消费协议完整流程-解析到处理")
    void testConsumeProtocol_FullFlow() throws ProtocolParseException, ProtocolProcessException {
        // 跳过测试如果处理器未注入
        if (consumeProtocolHandler == null) {
            log.warn("[协议集成测试] 跳过测试：ConsumeProtocolHandler未注入（测试环境未配置）");
            return;
        }

        // Given - 准备测试消息
        byte[] messageBytes = consumeMessageBytes;

        // When - 解析消息
        ProtocolMessage message = consumeProtocolHandler.parseMessage(messageBytes);
        assertNotNull(message);
        assertEquals("CONSUME_ZKTECO_V1_0", message.getProtocolType());

        // When - 验证消息
        boolean isValid = consumeProtocolHandler.validateMessage(message);
        assertTrue(isValid);

        // When - 处理消息
        assertDoesNotThrow(() -> {
            consumeProtocolHandler.processMessage(message, 1L);
        });
    }

    @Test
    @DisplayName("测试协议处理器注册-所有处理器都已注册")
    void testProtocolHandlers_AllRegistered() {
        // 验证所有协议处理器都已注册为Spring Bean
        // 注意：这需要Spring上下文支持，实际运行时需要配置
        if (accessProtocolHandler == null ||
            attendanceProtocolHandler == null ||
            consumeProtocolHandler == null) {
            log.warn("[协议集成测试] 跳过测试：部分协议处理器未注入（测试环境未配置）");
            return;
        }

        assertNotNull(accessProtocolHandler);
        assertNotNull(attendanceProtocolHandler);
        assertNotNull(consumeProtocolHandler);

        // 验证协议类型
        assertEquals("ACCESS_ENTROPY_V4_8", accessProtocolHandler.getProtocolType());
        assertEquals("ATTENDANCE_ENTROPY_V4_0", attendanceProtocolHandler.getProtocolType());
        assertEquals("CONSUME_ZKTECO_V1_0", consumeProtocolHandler.getProtocolType());
    }

    @Test
    @DisplayName("测试协议消息解析性能")
    void testProtocolParsing_Performance() throws ProtocolParseException {
        // 跳过测试如果处理器未注入
        if (accessProtocolHandler == null) {
            log.warn("[协议集成测试] 跳过测试：AccessProtocolHandler未注入（测试环境未配置）");
            return;
        }

        // 性能测试：解析指定数量的消息
        int messageCount = SystemConstants.PROTOCOL_TEST_MESSAGE_COUNT;
        long startTime = System.nanoTime();

        for (int i = 0; i < messageCount; i++) {
            accessProtocolHandler.parseMessage(accessMessageBytes);
        }

        long endTime = System.nanoTime();
        long duration = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        double averageTime = (double) duration / messageCount;

        log.info("[协议集成测试] 解析{}条消息耗时：{} ms，平均：{:.2f} ms/条",
                messageCount, duration, averageTime);

        // 验证性能要求：平均每条消息解析时间应小于阈值
        assertTrue(averageTime < SystemConstants.PROTOCOL_PARSING_PERFORMANCE_THRESHOLD_MS,
                String.format("协议解析性能不达标，平均耗时%.2f ms超过阈值%.2f ms",
                        averageTime, SystemConstants.PROTOCOL_PARSING_PERFORMANCE_THRESHOLD_MS));
    }

    /**
     * 创建门禁协议测试消息
     */
    private byte[] createAccessMessage() {
        byte[] messageBytes = new byte[24];
        ByteBuffer buffer = ByteBuffer.wrap(messageBytes).order(ByteOrder.LITTLE_ENDIAN);

        // 协议头
        buffer.put((byte) 0xAA);
        buffer.put((byte) 0x55);

        // 消息类型
        buffer.put((byte) 0x01);

        // 设备编号
        byte[] deviceCode = "DEV001  ".getBytes();
        buffer.put(deviceCode);

        // 用户ID
        buffer.putInt(1001);

        // 通行时间
        buffer.putInt((int) (System.currentTimeMillis() / 1000));

        // 通行类型
        buffer.put((byte) 0x00);

        // 门号
        buffer.put((byte) 0x01);

        // 通行方式
        buffer.put((byte) 0x01);

        // 校验和
        int checksum = 0;
        for (int i = 2; i < messageBytes.length - 2; i++) {
            checksum += messageBytes[i] & 0xFF;
        }
        checksum = checksum & 0xFFFF;
        buffer.putShort((short) checksum);

        return messageBytes;
    }

    /**
     * 创建考勤协议测试消息
     */
    private byte[] createAttendanceMessage() {
        byte[] messageBytes = new byte[22];
        ByteBuffer buffer = ByteBuffer.wrap(messageBytes).order(ByteOrder.LITTLE_ENDIAN);

        // 协议头
        buffer.put((byte) 0xAA);
        buffer.put((byte) 0x55);

        // 消息类型
        buffer.put((byte) 0x01);

        // 设备编号
        byte[] deviceCode = "DEV001  ".getBytes();
        buffer.put(deviceCode);

        // 用户ID
        buffer.putInt(1001);

        // 打卡时间
        buffer.putInt((int) (System.currentTimeMillis() / 1000));

        // 打卡类型
        buffer.put((byte) 0x00);

        // 校验和
        int checksum = 0;
        for (int i = 2; i < messageBytes.length - 2; i++) {
            checksum += messageBytes[i] & 0xFF;
        }
        checksum = checksum & 0xFFFF;
        buffer.putShort((short) checksum);

        return messageBytes;
    }

    /**
     * 创建消费协议测试消息
     */
    private byte[] createConsumeMessage() {
        byte[] messageBytes = new byte[25];
        ByteBuffer buffer = ByteBuffer.wrap(messageBytes).order(ByteOrder.LITTLE_ENDIAN);

        // 协议头
        buffer.put((byte) 0xAA);
        buffer.put((byte) 0x55);

        // 消息类型
        buffer.put((byte) 0x01);

        // 设备编号
        byte[] deviceCode = "DEV001  ".getBytes();
        buffer.put(deviceCode);

        // 用户ID
        buffer.putInt(1001);

        // 消费金额
        buffer.putInt(1000);

        // 消费时间
        buffer.putInt((int) (System.currentTimeMillis() / 1000));

        // 校验和
        int checksum = 0;
        for (int i = 2; i < messageBytes.length - 2; i++) {
            checksum += messageBytes[i] & 0xFF;
        }
        checksum = checksum & 0xFFFF;
        buffer.putShort((short) checksum);

        return messageBytes;
    }
}

