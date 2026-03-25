package com.access.control.service.impl;

import com.access.control.entity.Device;
import com.access.control.entity.Reservation;
import com.access.control.entity.User;
import com.access.control.entity.Blacklist;
import com.access.control.entity.Feedback;
import com.access.control.entity.SystemConfig;
import com.access.control.mapper.DeviceMapper;
import com.access.control.mapper.ReservationMapper;
import com.access.control.mapper.UserMapper;
import com.access.control.mapper.BlacklistMapper;
import com.access.control.mapper.FeedbackMapper;
import com.access.control.mapper.SystemConfigMapper;
import com.access.control.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private DeviceMapper deviceMapper;
    @Autowired
    private ReservationMapper reservationMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private BlacklistMapper blacklistMapper;
    @Autowired
    private FeedbackMapper feedbackMapper;
    @Autowired
    private SystemConfigMapper systemConfigMapper;

    // 设备管理
    @Override
    public List<Device> listAllDevices() {
        return deviceMapper.listAll();
    }

    @Override
    public boolean addDevice(Device device) {
        return deviceMapper.insert(device) > 0;
    }

    @Override
    public boolean updateDevice(Device device) {
        return deviceMapper.update(device) > 0;
    }

    @Override
    public boolean deleteDevice(Long id) {
        return deviceMapper.delete(id) > 0;
    }

    // 预约审核
    @Override
    public List<Reservation> listPendingReservations() {
        return reservationMapper.listByStatus(0); // 0-待审核
    }

    @Override
    public List<Reservation> listAllReservations() {
        return reservationMapper.listAll();
    }

    @Override
    public List<Reservation> listReservationsByUser(Long userId) {
        return reservationMapper.listByUserId(userId);
    }

    @Override
    public boolean auditReservation(Long id, Integer status, String auditOpinion) {
        return reservationMapper.audit(id, status, auditOpinion) > 0;
    }

    @Override
    @Transactional
    public void batchAudit(List<Long> ids, Integer status, String auditOpinion) {
        for (Long id : ids) {
            reservationMapper.audit(id, status, auditOpinion);
        }
    }

    // 用户与黑名单管理
    @Override
    public List<User> listAllUsers() {
        return userMapper.listAll();
    }

    @Override
    @Transactional
    public boolean addUserToBlacklist(Blacklist blacklist) {
        // 1. 更新用户状态为禁用 (0)
        userMapper.updateStatus(blacklist.getUserId(), 0);
        // 2. 插入黑名单表
        return blacklistMapper.insert(blacklist) > 0;
    }

    @Override
    @Transactional
    public boolean removeUserFromBlacklist(Long userId) {
        // 1. 更新用户状态为正常 (1)
        userMapper.updateStatus(userId, 1);
        // 2. 从黑名单表删除
        return blacklistMapper.deleteByUserId(userId) > 0;
    }

    @Override
    public List<Blacklist> listBlacklist() {
        return blacklistMapper.listAll();
    }

    @Override
    @Transactional
    public boolean updateUserStatus(Long userId, Integer status) {
        boolean ok = userMapper.updateStatus(userId, status) > 0;
        // 启用账号时，为避免“启用但仍在黑名单表”的不一致，顺便清理黑名单记录
        if (ok && status != null && status == 1) {
            blacklistMapper.deleteByUserId(userId);
        }
        return ok;
    }

    @Override
    public boolean resetUserPassword(Long userId, String newPassword) {
        return userMapper.updatePassword(userId, newPassword) > 0;
    }

    // 设备维护 / 报修反馈管理
    @Override
    public List<Feedback> listAllFeedback() {
        return feedbackMapper.listAll();
    }

    @Override
    @Transactional
    public boolean updateFeedbackStatus(Long id, Integer status, String adminReply) {
        return feedbackMapper.updateStatusAndReply(id, status, adminReply) > 0;
    }

    // 预约规则 / 系统配置
    @Override
    public List<SystemConfig> listAllConfigs() {
        return systemConfigMapper.listAll();
    }

    @Override
    public boolean updateConfig(SystemConfig config) {
        return systemConfigMapper.updateByKey(config) > 0;
    }

    // 统计分析 (简单实现)
    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        List<Reservation> all = reservationMapper.listAll();
        stats.put("totalReservations", all.size());
        stats.put("pendingCount", all.stream().filter(r -> r.getStatus() == 0).count());
        stats.put("successCount", all.stream().filter(r -> r.getStatus() == 1 || r.getStatus() == 3).count());
        stats.put("deviceCount", deviceMapper.listAll().size());
        stats.put("userCount", userMapper.listAll().size());

        // 近7天预约量（按预约日期聚合）
        LocalDate today = LocalDate.now();
        Map<LocalDate, Long> countByDate = all.stream()
                .filter(r -> r.getReservationDate() != null)
                .collect(Collectors.groupingBy(Reservation::getReservationDate, Collectors.counting()));
        List<Map<String, Object>> trend7 = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            Map<String, Object> row = new HashMap<>();
            row.put("date", d.toString());
            row.put("count", countByDate.getOrDefault(d, 0L));
            trend7.add(row);
        }
        stats.put("trend7", trend7);

        // 状态分布（0-5）
        Map<Integer, Long> statusDist = all.stream()
                .collect(Collectors.groupingBy(Reservation::getStatus, Collectors.counting()));
        Map<String, Object> statusDistOut = new HashMap<>();
        for (int s = 0; s <= 5; s++) {
            statusDistOut.put(String.valueOf(s), statusDist.getOrDefault(s, 0L));
        }
        stats.put("statusDist", statusDistOut);

        // 门禁 Top5（按 deviceName 聚合）
        Map<String, Long> deviceCount = all.stream()
                .map(r -> r.getDeviceName() != null ? r.getDeviceName() : String.valueOf(r.getDeviceId()))
                .collect(Collectors.groupingBy(x -> x, Collectors.counting()));
        List<Map<String, Object>> topDevices = deviceCount.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .map(e -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("name", e.getKey());
                    row.put("count", e.getValue());
                    return row;
                })
                .collect(Collectors.toList());
        stats.put("topDevices", topDevices);

        return stats;
    }
}
