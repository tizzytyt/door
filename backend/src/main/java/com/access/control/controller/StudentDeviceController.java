package com.access.control.controller;

import com.access.control.common.BaseController;
import com.access.control.common.Result;
import com.access.control.entity.Device;
import com.access.control.entity.Favorite;
import com.access.control.service.StudentDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/student/device")
public class StudentDeviceController extends BaseController {

    @Autowired
    private StudentDeviceService deviceService;

    /**
     * 获取所有可预约的门禁设备列表
     */
    @GetMapping("/list")
    public Result list() {
        List<Device> list = deviceService.listActiveDevices();
        return Result.success(list);
    }

    /**
     * 获取个人收藏的门禁列表
     */
    @GetMapping("/favorites")
    public Result favorites() {
        List<Favorite> list = deviceService.listMyFavorites(getCurrentUserId());
        return Result.success(list);
    }

    /**
     * 添加常用门禁收藏
     */
    @PostMapping("/favorite/add/{deviceId}")
    public Result addFavorite(@PathVariable Long deviceId) {
        boolean ok = deviceService.addFavorite(getCurrentUserId(), deviceId);
        return ok ? Result.success() : Result.error("收藏失败，门禁不可用或已在收藏中");
    }

    /**
     * 移除门禁收藏
     */
    @PostMapping("/favorite/remove/{deviceId}")
    public Result removeFavorite(@PathVariable Long deviceId) {
        boolean ok = deviceService.removeFavorite(getCurrentUserId(), deviceId);
        return ok ? Result.success() : Result.error("移除收藏失败");
    }
}
