package net.lab1024.sa.common.monitor.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.lab1024.sa.common.monitor.dao.AlertRuleDao;
import net.lab1024.sa.common.monitor.dao.NotificationDao;
import net.lab1024.sa.common.monitor.domain.entity.AlertEntity;
import net.lab1024.sa.common.monitor.domain.entity.NotificationEntity;

public class NotificationManager {

    private final NotificationDao notificationDao;
    private final AlertRuleDao alertRuleDao;

    public NotificationManager(NotificationDao dao, AlertRuleDao ruleDao) {
        this.notificationDao = dao;
        this.alertRuleDao = ruleDao;
    }

    public void sendAlertNotification(AlertEntity alert) {
    }

    protected void sendNotification(NotificationEntity notification) {
    }

    protected boolean sendByChannel(NotificationEntity notification, Integer channelType) {
        return false;
    }

    public void processPendingNotifications() {
    }

    public void processRetryNotifications() {
    }

    public Map<String, Object> getNotificationStatistics(Integer days) {
        return new HashMap<>();
    }

    public boolean sendTestNotification(Integer channelType, List<String> targets) {
        return false;
    }
}
