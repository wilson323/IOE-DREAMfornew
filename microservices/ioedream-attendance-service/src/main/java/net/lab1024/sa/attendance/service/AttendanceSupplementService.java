package net.lab1024.sa.attendance.service;

import net.lab1024.sa.attendance.domain.entity.AttendanceSupplementEntity;
import net.lab1024.sa.attendance.domain.form.AttendanceSupplementForm;

public interface AttendanceSupplementService {
    AttendanceSupplementEntity submitSupplementApplication(AttendanceSupplementForm form);
    void updateSupplementStatus(String supplementNo, String status, String approvalComment);
}



