package com.access.control.service.impl;

import com.access.control.entity.Device;
import com.access.control.entity.Favorite;
import com.access.control.mapper.DeviceMapper;
import com.access.control.mapper.FavoriteMapper;
import com.access.control.service.StudentDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentDeviceServiceImpl implements StudentDeviceService {

    @Autowired
    private DeviceMapper deviceMapper;
    @Autowired
    private FavoriteMapper favoriteMapper;

    @Override
    public List<Device> listActiveDevices() {
        return deviceMapper.listAllActive().stream()
                .filter(d -> d.getStatus() != null && d.getStatus() == 1)
                .collect(Collectors.toList());
    }

    @Override
    public List<Favorite> listMyFavorites(Long userId) {
        return favoriteMapper.listByUserId(userId);
    }

    @Override
    public boolean addFavorite(Long userId, Long deviceId) {
        if (deviceId == null) {
            return false;
        }
        Device d = deviceMapper.getById(deviceId);
        if (d == null || d.getStatus() == null || d.getStatus() != 1) {
            return false;
        }
        Favorite f = new Favorite();
        f.setUserId(userId);
        f.setDeviceId(deviceId);
        return favoriteMapper.insert(f) > 0;
    }

    @Override
    public boolean removeFavorite(Long userId, Long deviceId) {
        return favoriteMapper.delete(userId, deviceId) > 0;
    }
}
