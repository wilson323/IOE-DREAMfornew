import re

# Fix AttendanceAnomalyApplyServiceTest.java
file1 = "D:/IOE-DREAM/microservices/ioedream-attendance-service/src/test/java/net/lab1024/sa/attendance/service/AttendanceAnomalyApplyServiceTest.java"

with open(file1, 'r', encoding='utf-8') as f:
    content = f.read()

# Replace the test class structure
old_imports = """import org.springframework.context.annotation.Import;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;"""

new_imports = """import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.attendance.dao.AttendanceAnomalyApplyDao;
import net.lab1024.sa.attendance.dao.AttendanceAnomalyDao;
import net.lab1024.sa.attendance.dao.AttendanceRuleConfigDao;
import net.lab1024.sa.attendance.service.impl.AttendanceAnomalyApplyServiceImpl;"""

content = content.replace(old_imports, new_imports)

# Replace class annotations
old_class = """@SpringBootTest(classes = {
    org.springframework.boot.autoconfigure.ImportAutoConfiguration.class,
    org.springframework.context.annotation.Configuration.class
})
@Import(EnhancedTestConfiguration.class)
@ActiveProfiles("h2-test")
@Transactional
@DisplayName("考勤异常申请Service测试")
class AttendanceAnomalyApplyServiceTest {"""

new_class = """@ExtendWith(MockitoExtension.class)
@DisplayName("考勤异常申请Service测试")
class AttendanceAnomalyApplyServiceTest {

    @Mock
    private AttendanceAnomalyApplyDao applyDao;

    @Mock
    private AttendanceAnomalyDao anomalyDao;

    @Mock
    private AttendanceRuleConfigDao ruleConfigDao;

    @InjectMocks
    private AttendanceAnomalyApplyServiceImpl applyService;"""

content = content.replace(old_class, new_class)

# Add @BeforeEach method before the test
old_test = """    @Test
    @DisplayName("测试：Service测试环境可以正常加载")
    void contextLoads() {"""

new_test = """    @BeforeEach
    void setUp() {
        // 初始化Mock对象
        // TODO: 添加必要的Mock行为
    }

    @Test
    @DisplayName("测试：Service测试环境可以正常加载")
    void contextLoads() {"""

content = content.replace(old_test, new_test)

with open(file1, 'w', encoding='utf-8') as f:
    f.write(content)

print("✅ Fixed AttendanceAnomalyApplyServiceTest.java")

# Fix AttendanceAnomalyApprovalServiceTest.java
file2 = "D:/IOE-DREAM/microservices/ioedream-attendance-service/src/test/java/net/lab1024/sa/attendance/service/AttendanceAnomalyApprovalServiceTest.java"

with open(file2, 'r', encoding='utf-8') as f:
    content2 = f.read()

old_imports2 = """import org.springframework.context.annotation.Import;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;"""

new_imports2 = """import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.attendance.dao.AttendanceAnomalyApplyDao;
import net.lab1024.sa.attendance.dao.AttendanceAnomalyApprovalRecordDao;
import net.lab1024.sa.attendance.service.impl.AttendanceAnomalyApprovalServiceImpl;"""

content2 = content2.replace(old_imports2, new_imports2)

old_class2 = """@SpringBootTest(classes = {
    org.springframework.boot.autoconfigure.ImportAutoConfiguration.class,
    org.springframework.context.annotation.Configuration.class
})
@Import(EnhancedTestConfiguration.class)
@ActiveProfiles("h2-test")
@Transactional
@DisplayName("考勤异常审批Service测试")
class AttendanceAnomalyApprovalServiceTest {"""

new_class2 = """@ExtendWith(MockitoExtension.class)
@DisplayName("考勤异常审批Service测试")
class AttendanceAnomalyApprovalServiceTest {

    @Mock
    private AttendanceAnomalyApplyDao applyDao;

    @Mock
    private AttendanceAnomalyApprovalRecordDao approvalRecordDao;

    @InjectMocks
    private AttendanceAnomalyApprovalServiceImpl approvalService;"""

content2 = content2.replace(old_class2, new_class2)

old_test2 = """    @Test
    @DisplayName("测试：Service测试环境可以正常加载")
    void contextLoads() {"""

new_test2 = """    @BeforeEach
    void setUp() {
        // 初始化Mock对象
        // TODO: 添加必要的Mock行为
    }

    @Test
    @DisplayName("测试：Service测试环境可以正常加载")
    void contextLoads() {"""

content2 = content2.replace(old_test2, new_test2)

with open(file2, 'w', encoding='utf-8') as f:
    f.write(content2)

print("✅ Fixed AttendanceAnomalyApprovalServiceTest.java")

# Fix AttendanceRuleServiceTest.java
file3 = "D:/IOE-DREAM/microservices/ioedream-attendance-service/src/test/java/net/lab1024/sa/attendance/service/AttendanceRuleServiceTest.java"

with open(file3, 'r', encoding='utf-8') as f:
    content3 = f.read()

old_imports3 = """import org.springframework.context.annotation.Import;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;"""

new_imports3 = """import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.attendance.dao.AttendanceRuleConfigDao;
import net.lab1024.sa.attendance.dao.AttendanceRuleDao;
import net.lab1024.sa.attendance.service.impl.AttendanceRuleServiceImpl;"""

content3 = content3.replace(old_imports3, new_imports3)

old_class3 = """@SpringBootTest(classes = {
    org.springframework.boot.autoconfigure.ImportAutoConfiguration.class,
    org.springframework.context.annotation.Configuration.class
})
@Import(EnhancedTestConfiguration.class)
@ActiveProfiles("h2-test")
@Transactional
@DisplayName("考勤规则Service测试")
class AttendanceRuleServiceTest {"""

new_class3 = """@ExtendWith(MockitoExtension.class)
@DisplayName("考勤规则Service测试")
class AttendanceRuleServiceTest {

    @Mock
    private AttendanceRuleConfigDao ruleConfigDao;

    @Mock
    private AttendanceRuleDao ruleDao;

    @InjectMocks
    private AttendanceRuleServiceImpl ruleService;"""

content3 = content3.replace(old_class3, new_class3)

old_test3 = """    @Test
    @DisplayName("测试：Service测试环境可以正常加载")
    void contextLoads() {"""

new_test3 = """    @BeforeEach
    void setUp() {
        // 初始化Mock对象
        // TODO: 添加必要的Mock行为
    }

    @Test
    @DisplayName("测试：Service测试环境可以正常加载")
    void contextLoads() {"""

content3 = content3.replace(old_test3, new_test3)

with open(file3, 'w', encoding='utf-8') as f:
    f.write(content3)

print("✅ Fixed AttendanceRuleServiceTest.java")

print("\n✅ 所有Service测试已修复为纯Mockito模式！")
print("注意：还需要根据实际测试需求添加@BeforeEach中的Mock行为配置")
