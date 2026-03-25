package com.access.control.service.impl;

import com.access.control.entity.Reservation;
import com.access.control.entity.VisitorReservation;
import com.access.control.entity.Report;
import com.access.control.entity.AccessRecord;
import com.access.control.mapper.*;
import com.access.control.service.StudentReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class StudentReservationServiceImpl implements StudentReservationService {

    @Autowired
    private ReservationMapper reservationMapper;
    @Autowired
    private VisitorReservationMapper visitorReservationMapper;
    @Autowired
    private ReportMapper reportMapper;
    @Autowired
    private SystemConfigMapper systemConfigMapper;
    @Autowired
    private AccessRecordMapper accessRecordMapper;
    @Autowired
    private DeviceMapper deviceMapper;

    @Override
    @Transactional
    public String submitReservation(Reservation reservation) {
        // 1. 检查每日预约次数限制
        String maxDailyStr = systemConfigMapper.getValueByKey("max_reservations_per_day");
        int maxDaily = maxDailyStr != null ? Integer.parseInt(maxDailyStr) : 3;
        int currentDaily = reservationMapper.countDailyReservations(reservation.getUserId(), reservation.getReservationDate());
        if (currentDaily >= maxDaily) {
            return "每日预约次数已达上限 (" + maxDaily + ")";
        }

        // 2. 检查提前预约时间限制
        String leadTimeStr = systemConfigMapper.getValueByKey("reservation_lead_time_minutes");
        int leadTime = leadTimeStr != null ? Integer.parseInt(leadTimeStr) : 60;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reservationStart = LocalDateTime.of(reservation.getReservationDate(), reservation.getStartTime());
        if (reservationStart.isBefore(now.plusMinutes(leadTime))) {
            return "请至少提前 " + leadTime + " 分钟预约";
        }

        // 3. 冲突检测 (同一时间段同一门禁是否已被预约)
        int conflictCount = reservationMapper.checkConflict(reservation.getDeviceId(), reservation.getReservationDate(), 
                                                           reservation.getStartTime(), reservation.getEndTime());
        if (conflictCount > 0) {
            return "该时间段门禁已被预约，请选择其他时段";
        }

        // 4. 保存预约 (初始状态 0-待审核)
        reservation.setStatus(0);
        reservationMapper.insert(reservation);
        return null; // 返回 null 表示成功
    }

    @Override
    public boolean cancelReservation(Long reservationId, Long userId) {
        Reservation r = reservationMapper.getById(reservationId);
        // 仅待审核(0)或已通过(1)的预约可以取消
        if (r != null && r.getUserId().equals(userId) && (r.getStatus() == 0 || r.getStatus() == 1)) {
            return reservationMapper.updateStatus(reservationId, userId, 4) > 0; // 4-已取消
        }
        return false;
    }

    @Override
    @Transactional
    public boolean confirmUsage(Long reservationId, Long userId) {
        Reservation r = reservationMapper.getById(reservationId);
        if (r != null && r.getUserId().equals(userId) && r.getStatus() == 1) { // 仅已通过(1)的预约可以确认使用
            LocalDateTime now = LocalDateTime.now();
            LocalTime nowTime = now.toLocalTime();
            // 若已过期：点击确认使用时自动标记为已失效(5)
            if (now.toLocalDate().isAfter(r.getReservationDate()) ||
                (now.toLocalDate().equals(r.getReservationDate()) && nowTime.isAfter(r.getEndTime()))) {
                reservationMapper.updateStatus(reservationId, userId, 5); // 5-已失效
                return false;
            }

            // 简单校验是否在预约时间段内
            if (now.toLocalDate().equals(r.getReservationDate()) &&
                !nowTime.isBefore(r.getStartTime()) && !nowTime.isAfter(r.getEndTime())) {
                
                // 1. 更新预约状态为已使用
                reservationMapper.confirmUsage(reservationId, userId);
                // 2. 记录进出流水
                AccessRecord record = new AccessRecord();
                record.setUserId(userId);
                record.setDeviceId(r.getDeviceId());
                record.setType(1); // 进入
                record.setReservationId(reservationId);
                record.setStatus(1); // 正常
                accessRecordMapper.insert(record);
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Reservation> getMyReservations(Long userId) {
        return reservationMapper.listByUserId(userId);
    }

    @Override
    public boolean submitVisitorReservation(VisitorReservation visitorReservation) {
        return visitorReservationMapper.insert(visitorReservation) > 0;
    }

    @Override
    public List<VisitorReservation> getMyVisitorReservations(Long userId) {
        return visitorReservationMapper.listByUserId(userId);
    }

    @Override
    public boolean cancelVisitorReservation(Long id, Long userId) {
        return visitorReservationMapper.cancel(id, userId) > 0;
    }

    @Override
    public boolean submitReport(Report report) {
        return reportMapper.insert(report) > 0;
    }

    @Override
    public List<Report> getMyReports(Long userId) {
        return reportMapper.listByUserId(userId);
    }

    @Override
    public boolean cancelReport(Long id, Long userId) {
        return reportMapper.cancel(id, userId) > 0;
    }

    @Override
    public List<AccessRecord> getMyAccessRecords(Long userId) {
        return accessRecordMapper.listByUserId(userId);
    }
}
