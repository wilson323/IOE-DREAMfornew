package net.lab1024.sa.access.manager;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.access.dao.AccessUserPermissionDao;
import net.lab1024.sa.access.domain.entity.AccessUserPermissionEntity;

/**
 * 门禁设备权限管理器单元测试
 *
 * @author IOE-DREAM Team
 * @since 2025-12-21
 */
@ExtendWith(MockitoExtension.class)
class AccessUserPermissionManagerTest {

    @Mock
    private AccessUserPermissionDao accessUserPermissionDao;

    @InjectMocks
    private AccessUserPermissionManager accessUserPermissionManager;

    @Test
    void shouldRetuNullWhenPermissionMissing() {
        when(accessUserPermissionDao.selectByUserAndArea(1L, 2L)).thenReturn(null);
        assertNull(accessUserPermissionManager.getValidPermission(1L, 2L));
    }

    @Test
    void shouldRetuNullWhenPermissionDisabled() {
        AccessUserPermissionEntity permission = new AccessUserPermissionEntity();
        permission.setPermissionStatus(0);
        when(accessUserPermissionDao.selectByUserAndArea(1L, 2L)).thenReturn(permission);

        assertNull(accessUserPermissionManager.getValidPermission(1L, 2L));
    }

    @Test
    void shouldRetuPermissionWhenInTimeWindow() {
        AccessUserPermissionEntity permission = new AccessUserPermissionEntity();
        permission.setPermissionStatus(1);
        permission.setStartTime(LocalDateTime.now().minusMinutes(5));
        permission.setEndTime(LocalDateTime.now().plusMinutes(5));

        when(accessUserPermissionDao.selectByUserAndArea(1L, 2L)).thenReturn(permission);

        assertSame(permission, accessUserPermissionManager.getValidPermission(1L, 2L));
    }
}
