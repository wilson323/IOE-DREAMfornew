package net.lab1024.sa.attendance.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.attendance.service.AttendanceLocationService;
import net.lab1024.sa.attendance.service.AttendanceMobileService;

/**
 * 移动端考勤控制器单元测试
 *
 * @author IOE-DREAM Team
 * @since 2025-12-04
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("移动端考勤控制器单元测试")
@SuppressWarnings("null")
class AttendanceMobileControllerTest {

    @Mock
    private AttendanceMobileService attendanceMobileService;

    @Mock
    private AttendanceLocationService attendanceLocationService;

    @InjectMocks
    private AttendanceMobileController attendanceMobileController;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("测试GPS定位打卡")
    void testGpsPunch() throws Exception {
        AttendanceMobileController.GpsPunchRequest request = new AttendanceMobileController.GpsPunchRequest();
        request.setEmployeeId(1001L);
        request.setLatitude(39.9042);
        request.setLongitude(116.4074);
        request.setPhotoUrl("https://example.com/photo.jpg");
        request.setAddress("北京市朝阳区");

        ResponseDTO<String> response = attendanceMobileController.gpsPunch(request);
        org.junit.jupiter.api.Assertions.assertNotNull(response);
        org.junit.jupiter.api.Assertions.assertEquals(200, response.getCode());
    }

    @Test
    @DisplayName("测试位置验证")
    void testValidateLocation() throws Exception {
        AttendanceMobileController.LocationValidationRequest request = new AttendanceMobileController.LocationValidationRequest();
        request.setEmployeeId(1001L);
        request.setLatitude(39.9042);
        request.setLongitude(116.4074);

        ResponseDTO<Boolean> response = attendanceMobileController.validateLocation(request);
        org.junit.jupiter.api.Assertions.assertNotNull(response);
        org.junit.jupiter.api.Assertions.assertEquals(200, response.getCode());
    }

    @Test
    @DisplayName("测试离线打卡数据缓存")
    void testCacheOfflinePunch() throws Exception {
        AttendanceMobileController.OfflinePunchRequest request = new AttendanceMobileController.OfflinePunchRequest();
        request.setEmployeeId(1001L);

        AttendanceMobileController.OfflinePunchData data = new AttendanceMobileController.OfflinePunchData();
        data.setPunchType("上班");
        data.setPunchTime(java.time.LocalDateTime.parse("2025-12-04T09:00:00"));
        data.setLatitude(39.9042);
        data.setLongitude(116.4074);
        data.setPhotoUrl("https://example.com/photo.jpg");
        request.setPunchDataList(java.util.List.of(data));

        ResponseDTO<String> response = attendanceMobileController.cacheOfflinePunch(request);
        org.junit.jupiter.api.Assertions.assertNotNull(response);
        org.junit.jupiter.api.Assertions.assertEquals(200, response.getCode());
    }

    @Test
    @DisplayName("测试离线数据同步")
    void testSyncOfflinePunches() throws Exception {
        ResponseDTO<String> response = attendanceMobileController.syncOfflinePunches(1001L);
        org.junit.jupiter.api.Assertions.assertNotNull(response);
        org.junit.jupiter.api.Assertions.assertEquals(200, response.getCode());
    }
}

