package com.access.control.service;

import com.access.control.entity.Reservation;
import com.access.control.entity.VisitorReservation;
import com.access.control.entity.Report;
import java.util.List;

public interface StudentReservationService {
    /**
     * 提交预约申请
     */
    String submitReservation(Reservation reservation);

    /**
     * 取消预约
     */
    boolean cancelReservation(Long reservationId, Long userId);

    /**
     * 确认使用门禁
     */
    boolean confirmUsage(Long reservationId, Long userId);

    /**
     * 获取个人预约记录
     */
    List<Reservation> getMyReservations(Long userId);

    /**
     * 提交访客预约
     */
    boolean submitVisitorReservation(VisitorReservation visitorReservation);

    /**
     * 获取访客预约记录
     */
    List<VisitorReservation> getMyVisitorReservations(Long userId);

    /**
     * 取消访客预约
     */
    boolean cancelVisitorReservation(Long id, Long userId);

    /**
     * 提交晚归/外出报备
     */
    boolean submitReport(Report report);

    /**
     * 获取报备记录
     */
    List<Report> getMyReports(Long userId);

    /**
     * 撤销报备
     */
    boolean cancelReport(Long id, Long userId);

    /**
     * 获取个人出入记录
     */
    List<com.access.control.entity.AccessRecord> getMyAccessRecords(Long userId);
}
