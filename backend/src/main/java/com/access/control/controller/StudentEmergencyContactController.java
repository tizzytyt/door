package com.access.control.controller;

import com.access.control.common.BaseController;
import com.access.control.common.Result;
import com.access.control.entity.EmergencyContact;
import com.access.control.service.EmergencyContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/student/emergency-contact")
public class StudentEmergencyContactController extends BaseController {

    @Autowired
    private EmergencyContactService emergencyContactService;

    @GetMapping("/list")
    public Result list() {
        if (!"student".equals(getCurrentUserRole())) {
            return Result.error("无权限");
        }
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "NOT_LOGIN");
        }
        return Result.success(emergencyContactService.listMine(userId));
    }

    @PostMapping("/save")
    public Result save(@RequestBody EmergencyContact contact) {
        if (!"student".equals(getCurrentUserRole())) {
            return Result.error("无权限");
        }
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "NOT_LOGIN");
        }
        String err = emergencyContactService.save(userId, contact);
        return err == null ? Result.success() : Result.error(err);
    }

    @PostMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        if (!"student".equals(getCurrentUserRole())) {
            return Result.error("无权限");
        }
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "NOT_LOGIN");
        }
        boolean ok = emergencyContactService.delete(userId, id);
        return ok ? Result.success() : Result.error("删除失败");
    }
}
