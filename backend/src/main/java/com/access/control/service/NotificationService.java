package com.access.control.service;

import com.access.control.entity.Feedback;
import com.access.control.entity.Notification;
import com.access.control.entity.Reservation;

import java.util.List;

public interface NotificationService {

    /** 学生提交设备报修后，通知所有在职管理员 */
    void notifyAdminsNewDeviceRepair(Feedback feedback, String submitterDisplayName);

    void notifyReservationAudited(Reservation reservation, int newStatus, String auditOpinion);

    void sendReservationReminder(Reservation reservation);

    int countUnreadReservationTypes(Long userId);

    List<Notification> listReservationTypes(Long userId);

    boolean markRead(Long id, Long userId);

    int countUnreadAdminRepair(Long userId);

    List<Notification> listUnreadAdminRepair(Long userId);
}
