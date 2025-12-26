package net.lab1024.sa.visitor.config;

import net.lab1024.sa.visitor.dao.SelfServiceRegistrationDao;
import net.lab1024.sa.visitor.dao.VisitorBiometricDao;
import net.lab1024.sa.visitor.dao.VisitorApprovalDao;
import net.lab1024.sa.visitor.dao.VisitRecordDao;
import net.lab1024.sa.visitor.dao.TerminalInfoDao;
import net.lab1024.sa.visitor.dao.VisitorAdditionalInfoDao;
import net.lab1024.sa.visitor.manager.SelfServiceRegistrationManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自助登记配置类
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Configuration
public class SelfServiceRegistrationConfiguration {

    @Bean
    public SelfServiceRegistrationManager selfServiceRegistrationManager(
            SelfServiceRegistrationDao dao,
            VisitorBiometricDao biometricDao,
            VisitorApprovalDao approvalDao,
            VisitRecordDao recordDao,
            TerminalInfoDao terminalDao,
            VisitorAdditionalInfoDao additionalInfoDao) {
        return new SelfServiceRegistrationManager(dao, biometricDao, approvalDao, recordDao, terminalDao, additionalInfoDao);
    }
}
