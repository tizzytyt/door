package com.access.control.service;

import com.access.control.entity.Device;
import com.access.control.entity.Reservation;
import com.access.control.entity.User;
import com.access.control.entity.Blacklist;
import com.access.control.entity.Feedback;
import com.access.control.entity.SystemConfig;
import com.access.control.entity.Report;
import java.util.List;
import java.util.Map;

public interface AdminService {
    // 设备管理
    List<Device> listAllDevices();
    boolean addDevice(Device device);
    boolean updateDevice(Device device);
    boolean deleteDevice(Long id);

    // 预约审核
    List<Reservation> listPendingReservations();
    List<Reservation> listAllReservations();
    List<Reservation> listReservationsByUser(Long userId);
    boolean auditReservation(Long id, Integer status, String auditOpinion);
    void batchAudit(List<Long> ids, Integer status, String auditOpinion);

    // 晚归/外出报备审核
    List<Report> listPendingReports();
    List<Report> listAllReports();
    boolean auditReport(Long id, Integer status, String auditOpinion);

    // 用户与黑名单管理
    List<User> listAllUsers();
    boolean addUserToBlacklist(Blacklist blacklist);
    boolean removeUserFromBlacklist(Long userId);
    List<Blacklist> listBlacklist();
    boolean updateUserStatus(Long userId, Integer status);
    boolean resetUserPassword(Long userId, String newPassword);

    /** 超级管理员创建管理员账号（角色固定为 admin） */
    boolean createAdminAccount(String username, String password, String realName, String phone);

    // 设备维护 / 报修反馈管理
    List<Feedback> listAllFeedback();
    boolean updateFeedbackStatus(Long id, Integer status, String adminReply);

    // 预约规则 / 系统配置
    List<SystemConfig> listAllConfigs();
    boolean updateConfig(SystemConfig config);

    // 统计分析
    Map<String, Object> getDashboardStats();

    /**
     * 获取“当前在学校内”的人员列表（管理员统计筛选）
     */
    List<Reservation> listCurrentlyInSchool();
}
