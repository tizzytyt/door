package com.access.control.service;

import com.access.control.entity.Device;
import com.access.control.entity.Favorite;
import java.util.List;

public interface StudentDeviceService {
    /**
     * 获取可预约的设备列表
     */
    List<Device> listActiveDevices();

    /**
     * 获取收藏的设备列表
     */
    List<Favorite> listMyFavorites(Long userId);

    /**
     * 添加收藏
     */
    boolean addFavorite(Long userId, Long deviceId);

    /**
     * 移除收藏
     */
    boolean removeFavorite(Long userId, Long deviceId);
}
