package net.lab1024.sa.oa.workflow.config.wrapper;

import org.flowable.engine.IdentityService;
import org.flowable.idm.api.Group;
import org.flowable.idm.api.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 身份服务包装器
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-17
 */
public class FlowableIdentityService {

    private static final Logger log = LoggerFactory.getLogger(FlowableIdentityService.class);
    private final IdentityService identityService;

    public FlowableIdentityService(IdentityService identityService) {
        this.identityService = identityService;
    }

    public User newUser(String userId) {
        return identityService.newUser(userId);
    }

    public void saveUser(User user) {
        log.info("[身份服务] 保存用户: {}", user.getId());
        identityService.saveUser(user);
    }

    public Group newGroup(String groupId) {
        return identityService.newGroup(groupId);
    }

    public void saveGroup(Group group) {
        log.info("[身份服务] 保存组: {}", group.getId());
        identityService.saveGroup(group);
    }

    public void setAuthenticatedUserId(String userId) {
        identityService.setAuthenticatedUserId(userId);
    }

    public IdentityService getNativeService() {
        return identityService;
    }
}
