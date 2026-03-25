package com.access.control.controller;

import com.access.control.common.BaseController;
import com.access.control.common.Result;
import com.access.control.entity.Device;
import com.access.control.entity.Reservation;
import com.access.control.entity.User;
import com.access.control.entity.Blacklist;
import com.access.control.entity.Feedback;
import com.access.control.entity.SystemConfig;
import com.access.control.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController extends BaseController {

    @Autowired
    private AdminService adminService;

    // --- 设备管理 ---

    @GetMapping("/device/list")
    public Result listDevices() {
        return Result.success(adminService.listAllDevices());
    }

    @PostMapping("/device/add")
    public Result addDevice(@RequestBody Device device) {
        boolean ok = adminService.addDevice(device);
        return ok ? Result.success() : Result.error("添加设备失败");
    }

    @PostMapping("/device/update")
    public Result updateDevice(@RequestBody Device device) {
        boolean ok = adminService.updateDevice(device);
        return ok ? Result.success() : Result.error("更新设备失败");
    }

    @DeleteMapping("/device/delete/{id}")
    public Result deleteDevice(@PathVariable Long id) {
        boolean ok = adminService.deleteDevice(id);
        return ok ? Result.success() : Result.error("删除设备失败");
    }

    // --- 预约审核 ---

    @GetMapping("/reservation/pending")
    public Result listPendingReservations() {
        return Result.success(adminService.listPendingReservations());
    }

    @GetMapping("/reservation/list")
    public Result listAllReservations() {
        return Result.success(adminService.listAllReservations());
    }

    @GetMapping("/reservation/user/{userId}")
    public Result listReservationsByUser(@PathVariable Long userId) {
        return Result.success(adminService.listReservationsByUser(userId));
    }

    @PostMapping("/reservation/audit")
    public Result auditReservation(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        String auditOpinion = (String) params.get("auditOpinion");
        boolean ok = adminService.auditReservation(id, status, auditOpinion);
        return ok ? Result.success() : Result.error("审核操作失败");
    }

    @PostMapping("/reservation/batch-audit")
    public Result batchAudit(@RequestBody Map<String, Object> params) {
        List<Long> ids = (List<Long>) params.get("ids");
        Integer status = Integer.valueOf(params.get("status").toString());
        String auditOpinion = (String) params.get("auditOpinion");
        adminService.batchAudit(ids, status, auditOpinion);
        return Result.success();
    }

    // --- 用户与黑名单管理 ---

    @GetMapping("/user/list")
    public Result listUsers() {
        return Result.success(adminService.listAllUsers());
    }

    @GetMapping("/blacklist/list")
    public Result listBlacklist() {
        return Result.success(adminService.listBlacklist());
    }

    @PostMapping("/blacklist/add")
    public Result addBlacklist(@RequestBody Blacklist blacklist) {
        boolean ok = adminService.addUserToBlacklist(blacklist);
        return ok ? Result.success() : Result.error("添加黑名单失败");
    }

    @DeleteMapping("/blacklist/remove/{userId}")
    public Result removeBlacklist(@PathVariable Long userId) {
        boolean ok = adminService.removeUserFromBlacklist(userId);
        return ok ? Result.success() : Result.error("移出黑名单失败");
    }

    // --- 超级管理员：账号控制 ---

    @PostMapping("/user/status")
    public Result updateUserStatus(@RequestBody Map<String, Object> params) {
        if (!"super_admin".equals(getCurrentUserRole())) {
            return Result.error("无权限");
        }
        Long userId = Long.valueOf(params.get("userId").toString());
        Integer status = Integer.valueOf(params.get("status").toString()); // 1启用 0封禁
        boolean ok = adminService.updateUserStatus(userId, status);
        return ok ? Result.success() : Result.error("更新用户状态失败");
    }

    @PostMapping("/user/reset-password")
    public Result resetUserPassword(@RequestBody Map<String, Object> params) {
        if (!"super_admin".equals(getCurrentUserRole())) {
            return Result.error("无权限");
        }
        Long userId = Long.valueOf(params.get("userId").toString());
        String newPassword = String.valueOf(params.get("newPassword"));
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return Result.error("新密码不能为空");
        }
        boolean ok = adminService.resetUserPassword(userId, newPassword);
        return ok ? Result.success() : Result.error("重置密码失败");
    }

    // --- 设备维护 / 报修反馈管理 ---

    @GetMapping("/feedback/list")
    public Result listFeedback() {
        return Result.success(adminService.listAllFeedback());
    }

    @PostMapping("/feedback/update")
    public Result updateFeedback(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        String adminReply = (String) params.getOrDefault("adminReply", "");
        boolean ok = adminService.updateFeedbackStatus(id, status, adminReply);
        return ok ? Result.success() : Result.error("更新反馈处理状态失败");
    }

    // --- 预约规则 / 系统配置 ---

    @GetMapping("/rules/list")
    public Result listRules() {
        return Result.success(adminService.listAllConfigs());
    }

    @PostMapping("/rules/update")
    public Result updateRule(@RequestBody SystemConfig config) {
        boolean ok = adminService.updateConfig(config);
        return ok ? Result.success() : Result.error("更新配置失败");
    }

    // --- 首页统计 ---

    @GetMapping("/dashboard/stats")
    public Result getStats() {
        return Result.success(adminService.getDashboardStats());
    }

    /**
     * 管理员：筛选当前在学校内的人
     * 口径：预约状态=已使用(status=3)且当前时间落在 start_time ~ end_time 内。
     */
    @GetMapping("/dashboard/inside-people")
    public Result insidePeople() {
        return Result.success(adminService.listCurrentlyInSchool());
    }

    /**
     * 导出统计数据为 Excel（管理员/超级管理员）
     */
    @GetMapping("/dashboard/stats/export")
    public void exportDashboardStats(HttpServletResponse response) throws Exception {
        String role = getCurrentUserRole();
        if (!"admin".equals(role) && !"super_admin".equals(role)) {
            response.setStatus(403);
            return;
        }

        Map<String, Object> stats = adminService.getDashboardStats();
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "dashboard_stats_" + ts + ".xlsx";

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + encoded);

        try (Workbook wb = new XSSFWorkbook()) {
            // Sheet 1: Summary
            Sheet summary = wb.createSheet("Summary");
            int r = 0;
            Row h = summary.createRow(r++);
            h.createCell(0).setCellValue("指标");
            h.createCell(1).setCellValue("数值");
            r = writeKV(summary, r, "总预约", stats.get("totalReservations"));
            r = writeKV(summary, r, "待审核", stats.get("pendingCount"));
            r = writeKV(summary, r, "成功(通过+已用)", stats.get("successCount"));
            r = writeKV(summary, r, "设备数", stats.get("deviceCount"));
            r = writeKV(summary, r, "用户数", stats.get("userCount"));

            // Sheet 2: Trend7
            Sheet trend = wb.createSheet("Trend7");
            Row th = trend.createRow(0);
            th.createCell(0).setCellValue("日期");
            th.createCell(1).setCellValue("预约量");
            Object trend7Obj = stats.get("trend7");
            if (trend7Obj instanceof List) {
                List<?> trend7 = (List<?>) trend7Obj;
                int i = 1;
                for (Object o : trend7) {
                    if (!(o instanceof Map)) continue;
                    Map<?, ?> m = (Map<?, ?>) o;
                    Row row = trend.createRow(i++);
                    row.createCell(0).setCellValue(String.valueOf(m.get("date")));
                    row.createCell(1).setCellValue(Long.parseLong(String.valueOf(m.get("count"))));
                }
            }

            // Sheet 3: StatusDist
            Sheet dist = wb.createSheet("StatusDist");
            Row dh = dist.createRow(0);
            dh.createCell(0).setCellValue("状态码");
            dh.createCell(1).setCellValue("数量");
            Object distObj = stats.get("statusDist");
            if (distObj instanceof Map) {
                Map<?, ?> m = (Map<?, ?>) distObj;
                int i = 1;
                for (int s = 0; s <= 5; s++) {
                    Row row = dist.createRow(i++);
                    row.createCell(0).setCellValue(s);
                    row.createCell(1).setCellValue(Long.parseLong(String.valueOf(m.get(String.valueOf(s)))));
                }
            }

            // Sheet 4: TopDevices
            Sheet top = wb.createSheet("TopDevices");
            Row topH = top.createRow(0);
            topH.createCell(0).setCellValue("门禁");
            topH.createCell(1).setCellValue("次数");
            Object topObj = stats.get("topDevices");
            if (topObj instanceof List) {
                List<?> topDevices = (List<?>) topObj;
                int i = 1;
                for (Object o : topDevices) {
                    if (!(o instanceof Map)) continue;
                    Map<?, ?> m = (Map<?, ?>) o;
                    Row row = top.createRow(i++);
                    row.createCell(0).setCellValue(String.valueOf(m.get("name")));
                    row.createCell(1).setCellValue(Long.parseLong(String.valueOf(m.get("count"))));
                }
            }

            wb.write(response.getOutputStream());
        }
    }

    private int writeKV(Sheet sheet, int rowIdx, String k, Object v) {
        Row row = sheet.createRow(rowIdx);
        row.createCell(0).setCellValue(k);
        row.createCell(1).setCellValue(v == null ? "" : String.valueOf(v));
        return rowIdx + 1;
    }
}
