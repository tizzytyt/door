package com.access.control.controller;

import com.access.control.common.BaseController;
import com.access.control.common.Result;
import com.access.control.entity.Notification;
import com.access.control.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/student/notification")
public class StudentNotificationController extends BaseController {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/list")
    public Result list() {
        List<Notification> list = notificationService.listReservationTypes(getCurrentUserId());
        for (Notification n : list) {
            if (n.getCreatedAt() != null) {
                n.setCreatedAtText(n.getCreatedAt().format(DT));
            }
            if (n.getType() != null) {
                n.setTypeText(n.getType() == 1 ? "预约提醒" : (n.getType() == 2 ? "审核结果" : "通知"));
            }
        }
        return Result.success(list);
    }

    @GetMapping("/unread-count")
    public Result unreadCount() {
        return Result.success(notificationService.countUnreadReservationTypes(getCurrentUserId()));
    }

    @PostMapping("/read/{id}")
    public Result read(@PathVariable Long id) {
        boolean ok = notificationService.markRead(id, getCurrentUserId());
        return ok ? Result.success() : Result.error("标记失败");
    }
}
