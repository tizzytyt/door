package com.access.control.controller;

import com.access.control.common.BaseController;
import com.access.control.common.Result;
import com.access.control.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/student/announcement")
public class StudentAnnouncementController extends BaseController {

    @Autowired
    private AnnouncementService announcementService;

    @GetMapping("/list")
    public Result list() {
        return Result.success(announcementService.listForUser(getCurrentUserId()));
    }

    @GetMapping("/unread-count")
    public Result unreadCount() {
        return Result.success(announcementService.countUnreadForUser(getCurrentUserId()));
    }

    @PostMapping("/read/{id}")
    public Result read(@PathVariable Long id) {
        boolean ok = announcementService.markRead(id, getCurrentUserId());
        return ok ? Result.success() : Result.error("标记失败");
    }
}

