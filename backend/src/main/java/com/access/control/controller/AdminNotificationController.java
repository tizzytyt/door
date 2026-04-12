package com.access.control.controller;

import com.access.control.common.BaseController;
import com.access.control.common.Result;
import com.access.control.entity.Notification;
import com.access.control.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 管理员站内通知（如新报修提醒，notification.type = 4）
 */
@RestController
@RequestMapping("/admin/notification")
public class AdminNotificationController extends BaseController {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Autowired
    private NotificationService notificationService;

    private boolean isAdminRole() {
        String role = getCurrentUserRole();
        return "admin".equals(role) || "super_admin".equals(role);
    }

    @GetMapping("/repair-unread-list")
    public Result repairUnreadList() {
        if (!isAdminRole()) {
            return Result.error("无权限");
        }
        List<Notification> list = notificationService.listUnreadAdminRepair(getCurrentUserId());
        for (Notification n : list) {
            if (n.getCreatedAt() != null) {
                n.setCreatedAtText(n.getCreatedAt().format(DT));
            }
        }
        return Result.success(list);
    }

    @GetMapping("/repair-unread-count")
    public Result repairUnreadCount() {
        if (!isAdminRole()) {
            return Result.error("无权限");
        }
        return Result.success(notificationService.countUnreadAdminRepair(getCurrentUserId()));
    }

    @PostMapping("/read/{id}")
    public Result read(@PathVariable Long id) {
        if (!isAdminRole()) {
            return Result.error("无权限");
        }
        boolean ok = notificationService.markRead(id, getCurrentUserId());
        return ok ? Result.success() : Result.error("标记失败");
    }
}
