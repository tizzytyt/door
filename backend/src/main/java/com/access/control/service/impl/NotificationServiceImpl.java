package com.access.control.service.impl;

import com.access.control.entity.Device;
import com.access.control.entity.Feedback;
import com.access.control.entity.Notification;
import com.access.control.entity.Reservation;
import com.access.control.mapper.DeviceMapper;
import com.access.control.mapper.NotificationMapper;
import com.access.control.mapper.UserMapper;
import com.access.control.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    public static final int TYPE_REMINDER = 1;
    public static final int TYPE_AUDIT = 2;
    /** 管理员收到的「学生新报修」提醒 */
    public static final int TYPE_ADMIN_NEW_REPAIR = 4;

    @Autowired
    private NotificationMapper notificationMapper;
    @Autowired
    private DeviceMapper deviceMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public void notifyAdminsNewDeviceRepair(Feedback feedback, String submitterDisplayName) {
        if (feedback == null || feedback.getId() == null) {
            return;
        }
        List<Long> adminIds = userMapper.listActiveAdminUserIds();
        if (adminIds == null || adminIds.isEmpty()) {
            return;
        }
        String deviceName = feedback.getDeviceName();
        if (deviceName == null || deviceName.isEmpty()) {
            if (feedback.getDeviceId() != null) {
                Device d = deviceMapper.getById(feedback.getDeviceId());
                deviceName = d != null && d.getName() != null ? d.getName() : "门禁";
            } else {
                deviceName = "门禁";
            }
        }
        String who = submitterDisplayName != null && !submitterDisplayName.isEmpty() ? submitterDisplayName : "学生用户";
        String title = "新设备报修【单号:" + feedback.getId() + "】";
        StringBuilder body = new StringBuilder();
        body.append(who).append(" 提交了设备报修，请及时处理。\n\n");
        body.append("【门禁】").append(deviceName).append("\n");
        body.append("【问题描述】\n");
        String c = feedback.getContent() != null ? feedback.getContent().trim() : "";
        if (c.length() > 500) {
            body.append(c.substring(0, 500)).append("…");
        } else {
            body.append(c.isEmpty() ? "（无文字描述）" : c);
        }
        body.append("\n\n请在「设备维护」中查看并处理该报修单。");

        for (Long adminId : adminIds) {
            if (adminId == null) {
                continue;
            }
            Notification n = new Notification();
            n.setUserId(adminId);
            n.setTitle(title);
            n.setContent(body.toString());
            n.setType(TYPE_ADMIN_NEW_REPAIR);
            notificationMapper.insert(n);
        }
    }

    @Override
    public void notifyReservationAudited(Reservation reservation, int newStatus, String auditOpinion) {
        if (reservation == null || reservation.getUserId() == null) {
            return;
        }
        if (newStatus != 1 && newStatus != 2) {
            return;
        }
        Device device = reservation.getDeviceId() != null ? deviceMapper.getById(reservation.getDeviceId()) : null;
        String deviceName = device != null && device.getName() != null ? device.getName() : "门禁";
        String location = device != null && device.getLocation() != null ? device.getLocation() : "—";

        String title = newStatus == 1 ? "预约审核：已通过" : "预约审核：未通过";
        StringBuilder body = new StringBuilder();
        body.append("【预约信息】\n");
        body.append("门禁：").append(deviceName).append("\n");
        body.append("位置：").append(location).append("\n");
        if (reservation.getReservationDate() != null) {
            body.append("日期：").append(reservation.getReservationDate()).append("\n");
        }
        if (reservation.getStartTime() != null && reservation.getEndTime() != null) {
            body.append("时段：").append(reservation.getStartTime()).append(" - ").append(reservation.getEndTime()).append("\n");
        }
        if (reservation.getReason() != null && !reservation.getReason().isEmpty()) {
            body.append("事由：").append(reservation.getReason()).append("\n");
        }
        body.append("\n【审核意见】\n");
        if (auditOpinion != null && !auditOpinion.trim().isEmpty()) {
            body.append(auditOpinion.trim());
        } else {
            body.append(newStatus == 1 ? "无" : "无");
        }

        Notification n = new Notification();
        n.setUserId(reservation.getUserId());
        n.setTitle(title);
        n.setContent(body.toString());
        n.setType(TYPE_AUDIT);
        notificationMapper.insert(n);
    }

    @Override
    public void sendReservationReminder(Reservation reservation) {
        if (reservation == null || reservation.getUserId() == null) {
            return;
        }
        String deviceName = reservation.getDeviceName() != null ? reservation.getDeviceName() : "门禁";
        String location = reservation.getDeviceLocation() != null ? reservation.getDeviceLocation() : "—";

        String title = "预约即将开始";
        StringBuilder body = new StringBuilder();
        body.append("您的预约将在约 30 分钟内开始，请提前做好准备。\n\n");
        body.append("【预约时间】\n");
        if (reservation.getReservationDate() != null) {
            body.append("日期：").append(reservation.getReservationDate()).append("\n");
        }
        if (reservation.getStartTime() != null && reservation.getEndTime() != null) {
            body.append("时段：").append(reservation.getStartTime()).append(" - ").append(reservation.getEndTime()).append("\n");
        }
        body.append("\n【门禁位置】\n");
        body.append(deviceName);
        if (!"—".equals(location)) {
            body.append("（").append(location).append("）");
        }

        Notification n = new Notification();
        n.setUserId(reservation.getUserId());
        n.setTitle(title);
        n.setContent(body.toString());
        n.setType(TYPE_REMINDER);
        notificationMapper.insert(n);
    }

    @Override
    public int countUnreadReservationTypes(Long userId) {
        if (userId == null) {
            return 0;
        }
        return notificationMapper.countUnreadReservationTypes(userId);
    }

    @Override
    public List<Notification> listReservationTypes(Long userId) {
        return notificationMapper.listReservationTypes(userId);
    }

    @Override
    public boolean markRead(Long id, Long userId) {
        if (id == null || userId == null) {
            return false;
        }
        return notificationMapper.markRead(id, userId) > 0;
    }

    @Override
    public int countUnreadAdminRepair(Long userId) {
        if (userId == null) {
            return 0;
        }
        return notificationMapper.countUnreadAdminRepair(userId);
    }

    @Override
    public List<Notification> listUnreadAdminRepair(Long userId) {
        return notificationMapper.listUnreadAdminRepair(userId);
    }
}
