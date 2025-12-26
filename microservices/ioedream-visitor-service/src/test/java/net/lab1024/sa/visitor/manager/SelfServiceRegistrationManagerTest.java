package net.lab1024.sa.visitor.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.visitor.dao.SelfServiceRegistrationDao;
import net.lab1024.sa.common.entity.visitor.SelfServiceRegistrationEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 自助登记管理器单元测试
 *
 * 测试范围：
 * 1. 访客码生成
 * 2. 登记码生成
 * 3. 身份证验证
 * 4. 手机号验证
 * 5. 审批流程
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("自助登记管理器测试")
class SelfServiceRegistrationManagerTest {

    @Mock
    private SelfServiceRegistrationDao selfServiceRegistrationDao;

    @InjectMocks
    private SelfServiceRegistrationManager selfServiceRegistrationManager;

    private SelfServiceRegistrationEntity testRegistration;

    @BeforeEach
    void setUp() {
        testRegistration = SelfServiceRegistrationEntity.builder()
                .registrationId(1L)
                .registrationCode("SSRG20251226000001")
                .visitorName("张三")
                .idCardType(1)
                .idCard("110101199001011234")
                .phone("13800138000")
                .visitorType(1)
                .visitPurpose("商务洽谈")
                .intervieweeId(2001L)
                .intervieweeName("李四")
                .intervieweeDepartment("技术部")
                .visitDate(LocalDate.now())
                .registrationStatus(0) // 待审批
                .build();
    }

    @Test
    @DisplayName("测试生成访客码")
    void testGenerateVisitorCode() {
        log.info("[单元测试] 测试生成访客码");

        // When
        String visitorCode = selfServiceRegistrationManager.generateVisitorCode();

        // Then
        assertNotNull(visitorCode, "访客码不应为null");
        assertTrue(visitorCode.startsWith("VC"), "访客码应以VC开头");
        assertEquals(20, visitorCode.length(), "访客码长度应为20位");

        log.info("[单元测试] 测试通过: 访客码生成成功，visitorCode={}", visitorCode);
    }

    @Test
    @DisplayName("测试生成登记码")
    void testGenerateRegistrationCode() {
        log.info("[单元测试] 测试生成登记码");

        // When
        String registrationCode = selfServiceRegistrationManager.generateRegistrationCode();

        // Then
        assertNotNull(registrationCode, "登记码不应为null");
        assertTrue(registrationCode.startsWith("SSRG"), "登记码应以SSRG开头");
        assertEquals(21, registrationCode.length(), "登记码长度应为21位");

        log.info("[单元测试] 测试通过: 登记码生成成功，registrationCode={}", registrationCode);
    }

    @Test
    @DisplayName("测试验证身份证号 - 有效身份证")
    void testValidateIdCard_Valid() {
        log.info("[单元测试] 测试验证身份证号 - 有效身份证");

        // Given
        String validIdCard = "110101199001011234";

        // When
        boolean isValid = selfServiceRegistrationManager.validateIdCard(validIdCard);

        // Then
        assertTrue(isValid, "身份证号应验证通过");

        log.info("[单元测试] 测试通过: 身份证号验证通过，idCard={}", validIdCard);
    }

    @Test
    @DisplayName("测试验证身份证号 - 无效身份证")
    void testValidateIdCard_Invalid() {
        log.info("[单元测试] 测试验证身份证号 - 无效身份证");

        // Test cases
        String[] invalidIdCards = {
                "123456789012",           // 长度不足
                "123456789012345678",     // 长度超长
                "11010119900101123X",     // 校验位错误
                "AA0101199001011234",     // 包含字母
                ""                         // 空字符串
        };

        for (String invalidIdCard : invalidIdCards) {
            boolean isValid = selfServiceRegistrationManager.validateIdCard(invalidIdCard);

            assertFalse(isValid, "身份证号应验证失败: " + invalidIdCard);

            log.info("[单元测试] 身份证号验证失败（预期）: idCard={}", invalidIdCard);
        }

        log.info("[单元测试] 测试通过: 身份证号验证逻辑正确");
    }

    @Test
    @DisplayName("测试验证手机号 - 有效手机号")
    void testValidatePhone_Valid() {
        log.info("[单元测试] 测试验证手机号 - 有效手机号");

        // Given
        String validPhone = "13800138000";

        // When
        boolean isValid = selfServiceRegistrationManager.validatePhone(validPhone);

        // Then
        assertTrue(isValid, "手机号应验证通过");

        log.info("[单元测试] 测试通过: 手机号验证通过，phone={}", validPhone);
    }

    @Test
    @DisplayName("测试验证手机号 - 无效手机号")
    void testValidatePhone_Invalid() {
        log.info("[单元测试] 测试验证手机号 - 无效手机号");

        // Test cases
        String[] invalidPhones = {
                "12345678901",            // 不是1开头
                "1380013800",             // 长度不足
                "138001380000",           // 长度超长
                "1380013800a",            // 包含字母
                ""                         // 空字符串
        };

        for (String invalidPhone : invalidPhones) {
            boolean isValid = selfServiceRegistrationManager.validatePhone(invalidPhone);

            assertFalse(isValid, "手机号应验证失败: " + invalidPhone);

            log.info("[单元测试] 手机号验证失败（预期）: phone={}", invalidPhone);
        }

        log.info("[单元测试] 测试通过: 手机号验证逻辑正确");
    }

    @Test
    @DisplayName("测试审批登记申请 - 通过")
    void testApproveRegistration_Approved() {
        log.info("[单元测试] 测试审批登记申请 - 通过");

        // Given
        Long approverId = 3001L;
        String approverName = "王经理";
        Boolean approved = true;
        String approvalComment = "同意接待";

        when(selfServiceRegistrationDao.selectById(1L)).thenReturn(testRegistration);
        when(selfServiceRegistrationDao.updateById(any(SelfServiceRegistrationEntity.class))).thenReturn(1);

        // When
        SelfServiceRegistrationEntity result = selfServiceRegistrationManager.approveRegistration(
                1L, approverId, approverName, approved, approvalComment
        );

        // Then
        assertNotNull(result, "审批结果不应为null");
        assertEquals(approverId, result.getApproverId(), "审批人ID应匹配");
        assertEquals(approverName, result.getApproverName(), "审批人姓名应匹配");
        assertEquals(approvalComment, result.getApprovalComment(), "审批意见应匹配");
        assertEquals(1, result.getRegistrationStatus(), "状态应为1（审批通过）");
        assertNotNull(result.getApprovalTime(), "审批时间应设置");

        log.info("[单元测试] 测试通过: 审批通过，registrationId={}, status={}",
                result.getRegistrationId(), result.getRegistrationStatus());

        verify(selfServiceRegistrationDao, times(1)).selectById(1L);
        verify(selfServiceRegistrationDao, times(1)).updateById(any(SelfServiceRegistrationEntity.class));
    }

    @Test
    @DisplayName("测试审批登记申请 - 拒绝")
    void testApproveRegistration_Rejected() {
        log.info("[单元测试] 测试审批登记申请 - 拒绝");

        // Given
        Long approverId = 3001L;
        String approverName = "王经理";
        Boolean approved = false;
        String approvalComment = "时间不合适";

        when(selfServiceRegistrationDao.selectById(1L)).thenReturn(testRegistration);
        when(selfServiceRegistrationDao.updateById(any(SelfServiceRegistrationEntity.class))).thenReturn(1);

        // When
        SelfServiceRegistrationEntity result = selfServiceRegistrationManager.approveRegistration(
                1L, approverId, approverName, approved, approvalComment
        );

        // Then
        assertNotNull(result, "审批结果不应为null");
        assertEquals(2, result.getRegistrationStatus(), "状态应为2（审批拒绝）");

        log.info("[单元测试] 测试通过: 审批拒绝，registrationId={}, status={}",
                result.getRegistrationId(), result.getRegistrationStatus());

        verify(selfServiceRegistrationDao, times(1)).selectById(1L);
        verify(selfServiceRegistrationDao, times(1)).updateById(any(SelfServiceRegistrationEntity.class));
    }

    @Test
    @DisplayName("测试访客签到")
    void testCheckIn_Success() {
        log.info("[单元测试] 测试访客签到");

        // Given
        String visitorCode = "VC20251226100001001";
        testRegistration.setRegistrationStatus(1); // 审批通过
        testRegistration.setVisitorCode(visitorCode);

        when(selfServiceRegistrationDao.selectByVisitorCode(visitorCode)).thenReturn(testRegistration);
        when(selfServiceRegistrationDao.updateById(any(SelfServiceRegistrationEntity.class))).thenReturn(1);

        // When
        SelfServiceRegistrationEntity result = selfServiceRegistrationManager.checkIn(visitorCode);

        // Then
        assertNotNull(result, "签到结果不应为null");
        assertEquals(3, result.getRegistrationStatus(), "状态应为3（已签到）");
        assertNotNull(result.getCheckInTime(), "签到时间应设置");

        log.info("[单元测试] 测试通过: 访客签到成功，visitorCode={}, checkInTime={}",
                visitorCode, result.getCheckInTime());

        verify(selfServiceRegistrationDao, times(1)).selectByVisitorCode(visitorCode);
        verify(selfServiceRegistrationDao, times(1)).updateById(any(SelfServiceRegistrationEntity.class));
    }

    @Test
    @DisplayName("测试访客签离")
    void testCheckOut_Success() {
        log.info("[单元测试] 测试访客签离");

        // Given
        String visitorCode = "VC20251226100001001";
        testRegistration.setRegistrationStatus(3); // 已签到
        testRegistration.setVisitorCode(visitorCode);
        testRegistration.setCheckInTime(LocalDateTime.now().minusHours(2));

        when(selfServiceRegistrationDao.selectByVisitorCode(visitorCode)).thenReturn(testRegistration);
        when(selfServiceRegistrationDao.updateById(any(SelfServiceRegistrationEntity.class))).thenReturn(1);

        // When
        SelfServiceRegistrationEntity result = selfServiceRegistrationManager.checkOut(visitorCode);

        // Then
        assertNotNull(result, "签离结果不应为null");
        assertEquals(4, result.getRegistrationStatus(), "状态应为4（已完成）");
        assertNotNull(result.getCheckOutTime(), "签离时间应设置");

        log.info("[单元测试] 测试通过: 访客签离成功，visitorCode={}, checkOutTime={}",
                visitorCode, result.getCheckOutTime());

        verify(selfServiceRegistrationDao, times(1)).selectByVisitorCode(visitorCode);
        verify(selfServiceRegistrationDao, times(1)).updateById(any(SelfServiceRegistrationEntity.class));
    }

    @Test
    @DisplayName("测试提取人脸特征")
    void testExtractFaceFeature() {
        log.info("[单元测试] 测试提取人脸特征");

        // Given
        String photoUrl = "http://example.com/face/photo.jpg";

        // When
        String faceFeature = selfServiceRegistrationManager.extractFaceFeature(photoUrl);

        // Then
        assertNotNull(faceFeature, "人脸特征不应为null");
        assertFalse(faceFeature.isEmpty(), "人脸特征不应为空");

        // Base64编码的特征向量
        assertTrue(faceFeature.length() > 0, "特征向量应大于0");

        log.info("[单元测试] 测试通过: 人脸特征提取成功，featureLength={}", faceFeature.length());
    }
}
