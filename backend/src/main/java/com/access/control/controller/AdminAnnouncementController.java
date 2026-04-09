package com.access.control.controller;

import com.access.control.common.BaseController;
import com.access.control.common.Result;
import com.access.control.entity.Announcement;
import com.access.control.entity.AnnouncementRead;
import com.access.control.entity.User;
import com.access.control.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/announcement")
public class AdminAnnouncementController extends BaseController {

    @Autowired
    private AnnouncementService announcementService;

    private boolean isAdminRole() {
        String role = getCurrentUserRole();
        return "admin".equals(role) || "super_admin".equals(role);
    }

    @GetMapping("/list")
    public Result list() {
        if (!isAdminRole()) return Result.error("无权限");
        return Result.success(announcementService.listAdmin());
    }

    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Long id) {
        if (!isAdminRole()) return Result.error("无权限");
        return Result.success(announcementService.detailAdmin(id));
    }

    @PostMapping("/create")
    public Result create(@RequestBody Announcement announcement) {
        if (!isAdminRole()) return Result.error("无权限");
        announcement.setPublisherId(getCurrentUserId());
        boolean ok = announcementService.create(announcement);
        return ok ? Result.success() : Result.error("发布失败");
    }

    @PostMapping("/update")
    public Result update(@RequestBody Announcement announcement) {
        if (!isAdminRole()) return Result.error("无权限");
        boolean ok = announcementService.update(announcement);
        return ok ? Result.success() : Result.error("更新失败");
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        if (!isAdminRole()) return Result.error("无权限");
        boolean ok = announcementService.delete(id);
        return ok ? Result.success() : Result.error("删除失败");
    }

    /**
     * 管理端：获取已读/未读明细
     * params: read=1(已读)/0(未读)
     */
    @GetMapping("/readers/{id}")
    public Result readers(@PathVariable Long id, @RequestParam(required = false) Integer read) {
        if (!isAdminRole()) return Result.error("无权限");
        if (read != null && read == 0) {
            List<User> unread = announcementService.listUnreadUsers(id);
            return Result.success(unread);
        }
        List<AnnouncementRead> readers = announcementService.listReaders(id);
        return Result.success(readers);
    }
}

