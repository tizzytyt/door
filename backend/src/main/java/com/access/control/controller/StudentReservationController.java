package com.access.control.controller;

import com.access.control.common.BaseController;
import com.access.control.common.Result;
import com.access.control.entity.Reservation;
import com.access.control.entity.VisitorReservation;
import com.access.control.entity.Report;
import com.access.control.entity.AccessRecord;
import com.access.control.service.StudentReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/student/reservation")
public class StudentReservationController extends BaseController {

    @Autowired
    private StudentReservationService reservationService;

    /**
     * 提交门禁预约申请
     */
    @PostMapping("/submit")
    public Result submit(@RequestBody Reservation reservation) {
        reservation.setUserId(getCurrentUserId());
        String error = reservationService.submitReservation(reservation);
        if (error != null) {
            return Result.error(error);
        }
        return Result.success();
    }

    /**
     * 获取个人预约记录列表
     */
    @GetMapping("/list")
    public Result list() {
        List<Reservation> list = reservationService.getMyReservations(getCurrentUserId());
        return Result.success(list);
    }

    /**
     * 取消预约
     */
    @PostMapping("/cancel/{id}")
    public Result cancel(@PathVariable Long id) {
        boolean ok = reservationService.cancelReservation(id, getCurrentUserId());
        return ok ? Result.success() : Result.error("取消预约失败，可能预约已在使用或不可取消");
    }

    /**
     * 确认使用门禁
     */
    @PostMapping("/confirm/{id}")
    public Result confirm(@PathVariable Long id) {
        boolean ok = reservationService.confirmUsage(id, getCurrentUserId());
        return ok ? Result.success() : Result.error("确认使用失败：不在预约时间段内或预约已过期（已自动标记为已失效）");
    }

    /**
     * 提交访客预约
     */
    @PostMapping("/visitor/submit")
    public Result submitVisitor(@RequestBody VisitorReservation visitorReservation) {
        visitorReservation.setUserId(getCurrentUserId());
        boolean ok = reservationService.submitVisitorReservation(visitorReservation);
        return ok ? Result.success() : Result.error("提交访客预约失败");
    }

    /**
     * 获取访客预约列表
     */
    @GetMapping("/visitor/list")
    public Result listVisitor() {
        List<VisitorReservation> list = reservationService.getMyVisitorReservations(getCurrentUserId());
        return Result.success(list);
    }

    /**
     * 取消访客预约
     */
    @PostMapping("/visitor/cancel/{id}")
    public Result cancelVisitor(@PathVariable Long id) {
        boolean ok = reservationService.cancelVisitorReservation(id, getCurrentUserId());
        return ok ? Result.success() : Result.error("取消访客预约失败");
    }

    /**
     * 提交报备 (晚归/外出)
     */
    @PostMapping("/report/submit")
    public Result submitReport(@RequestBody Report report) {
        report.setUserId(getCurrentUserId());
        boolean ok = reservationService.submitReport(report);
        return ok ? Result.success() : Result.error("提交报备失败");
    }

    /**
     * 获取报备记录
     */
    @GetMapping("/report/list")
    public Result listReport() {
        List<Report> list = reservationService.getMyReports(getCurrentUserId());
        return Result.success(list);
    }

    /**
     * 撤销报备
     */
    @PostMapping("/report/cancel/{id}")
    public Result cancelReport(@PathVariable Long id) {
        boolean ok = reservationService.cancelReport(id, getCurrentUserId());
        return ok ? Result.success() : Result.error("撤销报备失败");
    }

    /**
     * 获取个人出入流水记录
     */
    @GetMapping("/access-records")
    public Result accessRecords() {
        List<AccessRecord> list = reservationService.getMyAccessRecords(getCurrentUserId());
        return Result.success(list);
    }
}
