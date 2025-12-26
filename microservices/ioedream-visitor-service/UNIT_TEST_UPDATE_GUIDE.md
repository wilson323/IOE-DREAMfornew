# 单元测试更新指南 - SelfServiceRegistrationEntity拆分

**更新日期**: 2025-12-26
**适用模块**: ioedream-visitor-service
**测试框架**: JUnit 5 + Mockito

---

## 📋 测试更新概览

### Entity拆分对测试的影响

**原测试**:
- 测试单个Entity（SelfServiceRegistrationEntity）
- 验证36个字段

**新测试**:
- 测试6个Entity
- 验证Entity间的关联关系
- 验证事务一致性
- 验证JOIN查询正确性

---

## 🧪 测试策略

### 测试层级

```
1. DAO层测试（单元测试）
   └─ 测试每个DAO的CRUD操作
   └─ 测试外键关联查询

2. Manager层测试（集成测试）
   └─ 测试多表事务操作
   └─ 测试JOIN查询数据组装

3. Service层测试（集成测试）
   └─ 测试业务流程
   └─ 测试向后兼容性
```

---

## 📝 DAO层测试示例

### VisitorBiometricDao测试

```java
package net.lab1024.sa.visitor.dao;

import net.lab1024.sa.visitor.entity.VisitorBiometricEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VisitorBiometricDao 单元测试
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional // 测试后自动回滚
class VisitorBiometricDaoTest {

    @Autowired
    private VisitorBiometricDao visitorBiometricDao;

    @Test
    void testInsert() {
        // given
        VisitorBiometricEntity biometric = VisitorBiometricEntity.builder()
                .registrationId(1L)
                .facePhotoUrl("http://example.com/face.jpg")
                .faceFeature("base64_feature_string")
                .idCardPhotoUrl("http://example.com/id_card.jpg")
                .build();

        // when
        int result = visitorBiometricDao.insert(biometric);

        // then
        assertEquals(1, result);
        assertNotNull(biometric.getBiometricId());
    }

    @Test
    void testSelectByRegistrationId() {
        // given
        Long registrationId = 1L;

        // when
        VisitorBiometricEntity result = visitorBiometricDao.selectByRegistrationId(registrationId);

        // then
        if (result != null) {
            assertEquals(registrationId, result.getRegistrationId());
        }
    }

    @Test
    void testUpdate() {
        // given
        VisitorBiometricEntity biometric = visitorBiometricDao.selectById(1L);
        if (biometric != null) {
            biometric.setFacePhotoUrl("http://example.com/new_face.jpg");

            // when
            int result = visitorBiometricDao.updateById(biometric);

            // then
            assertEquals(1, result);
        }
    }

    @Test
    void testDelete() {
        // given
        VisitorBiometricEntity biometric = visitorBiometricDao.selectById(1L);
        if (biometric != null) {
            // when
            int result = visitorBiometricDao.deleteById(biometric.getBiometricId());

            // then
            assertEquals(1, result);
        }
    }
}
```

### 批量DAO测试模板

```java
/**
 * DAO层批量测试模板
 * 适用于所有5个新DAO
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EntityDaoTestTemplate {

    /**
     * 测试DAO: VisitorBiometricDao, VisitorApprovalDao, VisitRecordDao,
     *         TerminalInfoDao, VisitorAdditionalInfoDao
     */
    @ParameterizedTest
    @ValueSource(classes = {
        VisitorBiometricDao.class,
        VisitorApprovalDao.class,
        VisitRecordDao.class,
        TerminalInfoDao.class,
        VisitorAdditionalInfoDao.class
    })
    void testAllDaoOperations(Class<?> daoClass) {
        // 测试所有DAO的基本CRUD操作
        // 1. insert
        // 2. selectById
        // 3. selectByRegistrationId
        // 4. updateById
        // 5. deleteById
    }
}
```

---

## 🔧 Manager层测试示例

### SelfServiceRegistrationManager测试

```java
package net.lab1024.sa.visitor.manager;

import net.lab1024.sa.visitor.entity.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * SelfServiceRegistrationManager 单元测试
 */
@ExtendWith(MockitoExtension.class)
class SelfServiceRegistrationManagerTest {

    @Mock
    private SelfServiceRegistrationDao registrationDao;

    @Mock
    private VisitorBiometricDao biometricDao;

    @Mock
    private VisitorApprovalDao approvalDao;

    @Mock
    private VisitRecordDao recordDao;

    @Mock
    private TerminalInfoDao terminalInfoDao;

    @Mock
    private VisitorAdditionalInfoDao additionalInfoDao;

    @InjectMocks
    private SelfServiceRegistrationManager manager;

    private SelfServiceRegistrationEntity registration;

    @BeforeEach
    void setUp() {
        registration = SelfServiceRegistrationEntity.builder()
                .visitorName("张三")
                .idCard("110101199001011234")
                .phone("13800138000")
                .visitPurpose("商务洽谈")
                .visitorCode("VC202512261430001234")
                .registrationStatus(0)
                .build();
    }

    @Test
    void testCreateRegistration_AllFields() {
        // given
        when(registrationDao.insert(any())).thenReturn(1);
        when(biometricDao.insert(any())).thenReturn(1);
        when(terminalInfoDao.insert(any())).thenReturn(1);
        when(additionalInfoDao.insert(any())).thenReturn(1);

        // 设置生物识别信息
        registration.setFacePhotoUrl("http://example.com/face.jpg");
        registration.setTerminalId("TERMINAL_001");
        registration.setBelongings("{\"笔记本电脑\": 1}");

        // when
        SelfServiceRegistrationEntity result = manager.createRegistration(registration);

        // then
        assertNotNull(result);
        assertEquals(1, result.getRegistrationId());

        // 验证所有DAO都调用了insert
        verify(registrationDao, times(1)).insert(any());
        verify(biometricDao, times(1)).insert(any());
        verify(terminalInfoDao, times(1)).insert(any());
        verify(additionalInfoDao, times(1)).insert(any());
    }

    @Test
    void testCreateRegistration_CoreOnly() {
        // given
        when(registrationDao.insert(any())).thenReturn(1);

        // when
        SelfServiceRegistrationEntity result = manager.createRegistration(registration);

        // then
        assertNotNull(result);
        assertEquals(1, result.getRegistrationId());

        // 验证只调用了核心DAO的insert
        verify(registrationDao, times(1)).insert(any());
        verify(biometricDao, never()).insert(any());
        verify(terminalInfoDao, never()).insert(any());
    }

    @Test
    void testGetRegistrationByVisitorCode_JoinQuery() {
        // given
        String visitorCode = "VC202512261430001234";
        Long registrationId = 1L;

        // Mock核心登记信息
        SelfServiceRegistrationEntity coreRegistration = SelfServiceRegistrationEntity.builder()
                .registrationId(registrationId)
                .visitorCode(visitorCode)
                .visitorName("张三")
                .build();
        when(registrationDao.selectByVisitorCode(visitorCode)).thenReturn(coreRegistration);

        // Mock生物识别信息
        VisitorBiometricEntity biometric = VisitorBiometricEntity.builder()
                .registrationId(registrationId)
                .facePhotoUrl("http://example.com/face.jpg")
                .build();
        when(biometricDao.selectByRegistrationId(registrationId)).thenReturn(biometric);

        // Mock审批信息
        VisitorApprovalEntity approval = VisitorApprovalEntity.builder()
                .registrationId(registrationId)
                .approverName("李四")
                .build();
        when(approvalDao.selectByRegistrationId(registrationId)).thenReturn(approval);

        // when
        SelfServiceRegistrationEntity result = manager.getRegistrationByVisitorCode(visitorCode);

        // then
        assertNotNull(result);
        assertEquals(registrationId, result.getRegistrationId());
        assertEquals("张三", result.getVisitorName());
        assertEquals("http://example.com/face.jpg", result.getFacePhotoUrl());
        assertEquals("李四", result.getApproverName());

        // 验证调用了所有相关DAO
        verify(registrationDao, times(1)).selectByVisitorCode(visitorCode);
        verify(biometricDao, times(1)).selectByRegistrationId(registrationId);
        verify(approvalDao, times(1)).selectByRegistrationId(registrationId);
    }

    @Test
    void testApproveRegistration() {
        // given
        Long registrationId = 1L;
        Long approverId = 100L;
        String approverName = "李四";
        Boolean approved = true;
        String approvalComment = "同意";

        SelfServiceRegistrationEntity registration = SelfServiceRegistrationEntity.builder()
                .registrationId(registrationId)
                .visitorCode("VC202512261430001234")
                .registrationStatus(0)
                .build();

        when(registrationDao.selectById(registrationId)).thenReturn(registration);
        when(registrationDao.updateById(any())).thenReturn(1);
        when(approvalDao.selectByRegistrationId(registrationId)).thenReturn(null);
        when(approvalDao.insert(any())).thenReturn(1);
        when(biometricDao.selectByRegistrationId(registrationId)).thenReturn(null);
        when(recordDao.selectByRegistrationId(registrationId)).thenReturn(null);
        when(terminalInfoDao.selectByRegistrationId(registrationId)).thenReturn(null);
        when(additionalInfoDao.selectByRegistrationId(registrationId)).thenReturn(null);

        // when
        SelfServiceRegistrationEntity result = manager.approveRegistration(
                registrationId, approverId, approverName, approved, approvalComment);

        // then
        assertNotNull(result);
        assertEquals(1, result.getRegistrationStatus()); // 1-审批通过

        // 验证更新了核心表
        verify(registrationDao, times(1)).updateById(any());

        // 验证插入了审批记录
        verify(approvalDao, times(1)).insert(any());
    }

    @Test
    void testCheckIn() {
        // given
        String visitorCode = "VC202512261430001234";
        Long registrationId = 1L;

        SelfServiceRegistrationEntity registration = SelfServiceRegistrationEntity.builder()
                .registrationId(registrationId)
                .visitorCode(visitorCode)
                .registrationStatus(1) // 1-审批通过
                .build();

        when(registrationDao.selectByVisitorCode(visitorCode)).thenReturn(registration);
        when(registrationDao.updateById(any())).thenReturn(1);
        when(recordDao.selectByRegistrationId(registrationId)).thenReturn(null);
        when(recordDao.insert(any())).thenReturn(1);
        when(biometricDao.selectByRegistrationId(registrationId)).thenReturn(null);
        when(approvalDao.selectByRegistrationId(registrationId)).thenReturn(null);
        when(terminalInfoDao.selectByRegistrationId(registrationId)).thenReturn(null);
        when(additionalInfoDao.selectByRegistrationId(registrationId)).thenReturn(null);

        // when
        SelfServiceRegistrationEntity result = manager.checkIn(visitorCode);

        // then
        assertNotNull(result);
        assertEquals(3, result.getRegistrationStatus()); // 3-已签到
        assertNotNull(result.getCheckInTime());

        // 验证创建了访问记录
        verify(recordDao, times(1)).insert(any());
    }
}
```

---

## 🎯 Service层测试示例

### SelfServiceRegistrationService测试

```java
package net.lab1024.sa.visitor.service;

import net.lab1024.sa.visitor.entity.SelfServiceRegistrationEntity;
import net.lab1024.sa.visitor.manager.SelfServiceRegistrationManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * SelfServiceRegistrationService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class SelfServiceRegistrationServiceTest {

    @Mock
    private SelfServiceRegistrationManager manager;

    @InjectMocks
    private SelfServiceRegistrationServiceImpl service;

    @Test
    void testCreateRegistration_Success() {
        // given
        SelfServiceRegistrationEntity registration = SelfServiceRegistrationEntity.builder()
                .visitorName("张三")
                .idCard("110101199001011234")
                .phone("13800138000")
                .visitPurpose("商务洽谈")
                .build();

        when(manager.createRegistration(any())).thenReturn(registration);

        // when
        SelfServiceRegistrationEntity result = service.createRegistration(registration);

        // then
        assertNotNull(result);
        assertEquals("张三", result.getVisitorName());
        verify(manager, times(1)).createRegistration(any());
    }

    @Test
    void testGetRegistrationByVisitorCode_Found() {
        // given
        String visitorCode = "VC202512261430001234";
        SelfServiceRegistrationEntity registration = SelfServiceRegistrationEntity.builder()
                .registrationId(1L)
                .visitorCode(visitorCode)
                .visitorName("张三")
                .build();

        when(manager.getRegistrationByVisitorCode(visitorCode)).thenReturn(registration);

        // when
        SelfServiceRegistrationEntity result = service.getRegistrationByVisitorCode(visitorCode);

        // then
        assertNotNull(result);
        assertEquals("张三", result.getVisitorName());
        verify(manager, times(1)).getRegistrationByVisitorCode(visitorCode);
    }

    @Test
    void testGetRegistrationByVisitorCode_NotFound() {
        // given
        String visitorCode = "INVALID_CODE";
        when(manager.getRegistrationByVisitorCode(visitorCode)).thenReturn(null);

        // when
        SelfServiceRegistrationEntity result = service.getRegistrationByVisitorCode(visitorCode);

        // then
        assertNull(result);
        verify(manager, times(1)).getRegistrationByVisitorCode(visitorCode);
    }

    @Test
    void testApproveRegistration_Success() {
        // given
        Long registrationId = 1L;
        Long approverId = 100L;
        String approverName = "李四";
        Boolean approved = true;
        String approvalComment = "同意";

        SelfServiceRegistrationEntity registration = SelfServiceRegistrationEntity.builder()
                .registrationId(registrationId)
                .registrationStatus(1)
                .build();

        when(manager.approveRegistration(
                eq(registrationId), eq(approverId), eq(approverName),
                eq(approved), eq(approvalComment))).thenReturn(registration);

        // when
        SelfServiceRegistrationEntity result = service.approveRegistration(
                registrationId, approverId, approverName, approved, approvalComment);

        // then
        assertNotNull(result);
        assertEquals(1, result.getRegistrationStatus());
        verify(manager, times(1)).approveRegistration(
                eq(registrationId), eq(approverId), eq(approverName),
                eq(approved), eq(approvalComment));
    }
}
```

---

## 📊 集成测试示例

### 完整流程测试

```java
/**
 * 完整业务流程集成测试
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SelfServiceRegistrationIntegrationTest {

    @Autowired
    private SelfServiceRegistrationService service;

    @Test
    void testCompleteRegistrationWorkflow() {
        // Step 1: 创建登记
        SelfServiceRegistrationEntity registration = new SelfServiceRegistrationEntity();
        registration.setVisitorName("张三");
        registration.setIdCard("110101199001011234");
        registration.setPhone("13800138000");
        registration.setVisitPurpose("商务洽谈");
        registration.setFacePhotoUrl("http://example.com/face.jpg");

        SelfServiceRegistrationEntity created = service.createRegistration(registration);
        assertNotNull(created);
        assertNotNull(created.getRegistrationId());
        assertNotNull(created.getVisitorCode());

        // Step 2: 查询登记
        SelfServiceRegistrationEntity found = service.getRegistrationByVisitorCode(created.getVisitorCode());
        assertNotNull(found);
        assertEquals("张三", found.getVisitorName());
        assertEquals("http://example.com/face.jpg", found.getFacePhotoUrl());

        // Step 3: 审批登记
        SelfServiceRegistrationEntity approved = service.approveRegistration(
                created.getRegistrationId(),
                100L,
                "李四",
                true,
                "同意"
        );
        assertNotNull(approved);
        assertEquals(1, approved.getRegistrationStatus());

        // Step 4: 访客签到
        SelfServiceRegistrationEntity checkedIn = service.checkIn(created.getVisitorCode());
        assertNotNull(checkedIn);
        assertEquals(3, checkedIn.getRegistrationStatus());
        assertNotNull(checkedIn.getCheckInTime());

        // Step 5: 访客签离
        SelfServiceRegistrationEntity checkedOut = service.checkOut(created.getVisitorCode());
        assertNotNull(checkedOut);
        assertEquals(4, checkedOut.getRegistrationStatus());
        assertNotNull(checkedOut.getCheckOutTime());
    }
}
```

---

## ✅ 测试完成检查清单

### DAO层测试

- [ ] VisitorBiometricDao测试
- [ ] VisitorApprovalDao测试
- [ ] VisitRecordDao测试
- [ ] TerminalInfoDao测试
- [ ] VisitorAdditionalInfoDao测试

### Manager层测试

- [ ] createRegistration测试（6个表插入）
- [ ] getRegistrationByVisitorCode测试（6个表JOIN）
- [ ] approveRegistration测试（事务处理）
- [ ] checkIn测试（事务处理）
- [ ] checkOut测试（事务处理）

### Service层测试

- [ ] createRegistration测试
- [ ] getRegistrationByVisitorCode测试
- [ ] approveRegistration测试
- [ ] checkIn测试
- [ ] checkOut测试
- [ ] queryPage测试

### 集成测试

- [ ] 完整业务流程测试
- [ ] 事务回滚测试
- [ ] 并发测试（可选）

---

## 🚀 测试执行步骤

### Step 1: 编写测试（4小时）

1. 创建5个DAO测试类
2. 更新Manager测试类
3. 更新Service测试类
4. 创建集成测试类

### Step 2: 执行测试（1小时）

```bash
# 运行所有测试
mvn test -pl ioedream-visitor-service

# 运行特定测试类
mvn test -Dtest=VisitorBiometricDaoTest
mvn test -Dtest=SelfServiceRegistrationManagerTest
mvn test -Dtest=SelfServiceRegistrationServiceTest

# 生成测试报告
mvn test -pl ioedream-visitor-service jacoco:report
```

### Step 3: 验证覆盖率（30分钟）

```bash
# 查看测试覆盖率
mvn jacoco:report

# 目标覆盖率
# - DAO层: ≥80%
# - Manager层: ≥75%
# - Service层: ≥60%
```

**总预计时间**: 5.5小时

---

## 📞 支持信息

**测试框架**: JUnit 5 + Mockito + Spring Boot Test
**代码覆盖率**: JaCoCo
**测试问题反馈**: 提交GitHub Issue或联系测试团队

---

**文档版本**: v1.0.0
**生成时间**: 2025-12-26
**维护人**: IOE-DREAM架构团队
**状态**: ✅ 测试指南已就绪，待执行
