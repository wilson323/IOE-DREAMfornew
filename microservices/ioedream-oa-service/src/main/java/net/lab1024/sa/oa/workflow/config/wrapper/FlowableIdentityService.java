package net.lab1024.sa.oa.workflow.config.wrapper;

import lombok.extern.slf4j.Slf4j;

import org.flowable.engine.IdentityService;
import org.flowable.idm.api.Group;
import org.flowable.idm.api.User;
/**
 * 身份服务包装器
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-17
 */
@Slf4j
public class FlowableIdentityService {

    private final IdentityService identityService;

    public FlowableIdentityService (IdentityService identityService) {
        this.identityService = identityService;
    }

    public User newUser (String userId) {
        return identityService.newUser (userId);
    }

    public void saveUser (User user) {
        log.info ("[身份服务] 保存用户: {}", user.getId ());
        identityService.saveUser (user);
    }

    public Group newGroup (String groupId) {
        return identityService.newGroup (groupId);
    }

    public void saveGroup (Group group) {
        log.info ("[身份服务] 保存组: {}", group.getId ());
        identityService.saveGroup (group);
    }

    public void setAuthenticatedUserId (String userId) {
        identityService.setAuthenticatedUserId (userId);
    }

    public IdentityService getNativeService () {
        return identityService;
    }
}
