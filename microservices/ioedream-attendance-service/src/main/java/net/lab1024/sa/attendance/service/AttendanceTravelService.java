package net.lab1024.sa.attendance.service;

import net.lab1024.sa.common.attendance.entity.AttendanceTravelEntity;
import net.lab1024.sa.attendance.domain.form.AttendanceTravelForm;

public interface AttendanceTravelService {
    AttendanceTravelEntity submitTravelApplication(AttendanceTravelForm form);
    void updateTravelStatus(String travelNo, String status, String approvalComment);
}



