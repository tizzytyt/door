package com.access.control.controller;

import com.access.control.common.BaseController;
import com.access.control.common.Result;
import com.access.control.entity.Device;
import com.access.control.entity.Reservation;
import com.access.control.entity.Blacklist;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

    /**
     * 预约记录导出（支持关键词/状态/日期/用户筛选）
     */
    @GetMapping("/reservation/export")
    public void exportReservations(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "userId", required = false) Long userId,
            HttpServletResponse response) throws Exception {
        String role = getCurrentUserRole();
        if (!"admin".equals(role) && !"super_admin".equals(role)) {
            response.setStatus(403);
            return;
        }

        List<Reservation> source = userId == null
                ? adminService.listAllReservations()
                : adminService.listReservationsByUser(userId);

        String kw = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        String targetDate = date == null ? "" : date.trim();

        List<Reservation> filtered = new ArrayList<>();
        for (Reservation it : source) {
            if (status != null && !status.equals(it.getStatus())) {
                continue;
            }
            String rowDate = it.getReservationDate() == null ? "" : it.getReservationDate().toString();
            if (!targetDate.isEmpty() && !targetDate.equals(rowDate)) {
                continue;
            }
            if (!kw.isEmpty()) {
                String realName = it.getRealName() == null ? "" : it.getRealName().toLowerCase(Locale.ROOT);
                String deviceName = it.getDeviceName() == null ? "" : it.getDeviceName().toLowerCase(Locale.ROOT);
                String reason = it.getReason() == null ? "" : it.getReason().toLowerCase(Locale.ROOT);
                if (!realName.contains(kw) && !deviceName.contains(kw) && !reason.contains(kw)) {
                    continue;
                }
            }
            filtered.add(it);
        }

        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "预约记录_" + ts + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + encoded);

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Reservations");
            int r = 0;
            Row h = sheet.createRow(r++);
            h.createCell(0).setCellValue("预约ID");
            h.createCell(1).setCellValue("用户ID");
            h.createCell(2).setCellValue("学生姓名");
            h.createCell(3).setCellValue("门禁位置");
            h.createCell(4).setCellValue("预约日期");
            h.createCell(5).setCellValue("开始时间");
            h.createCell(6).setCellValue("结束时间");
            h.createCell(7).setCellValue("状态");
            h.createCell(8).setCellValue("事由");
            h.createCell(9).setCellValue("审核意见");
            h.createCell(10).setCellValue("创建时间");

            for (Reservation it : filtered) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(it.getId() == null ? "" : String.valueOf(it.getId()));
                row.createCell(1).setCellValue(it.getUserId() == null ? "" : String.valueOf(it.getUserId()));
                row.createCell(2).setCellValue(it.getRealName() == null ? "" : it.getRealName());
                row.createCell(3).setCellValue(it.getDeviceName() == null ? "" : it.getDeviceName());
                row.createCell(4).setCellValue(it.getReservationDate() == null ? "" : it.getReservationDate().toString());
                row.createCell(5).setCellValue(formatTime(it.getStartTime()));
                row.createCell(6).setCellValue(formatTime(it.getEndTime()));
                row.createCell(7).setCellValue(statusText(it.getStatus()));
                row.createCell(8).setCellValue(it.getReason() == null ? "" : it.getReason());
                row.createCell(9).setCellValue(it.getAuditOpinion() == null ? "" : it.getAuditOpinion());
                row.createCell(10).setCellValue(it.getCreatedAt() == null ? "" : it.getCreatedAt().toString().replace('T', ' '));
            }

            wb.write(response.getOutputStream());
        }
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
        Object rawIds = params.get("ids");
        if (!(rawIds instanceof List)) {
            return Result.error("请选择要审核的预约");
        }
        List<Long> ids = new ArrayList<>();
        for (Object o : (List<?>) rawIds) {
            if (o != null) {
                ids.add(Long.valueOf(o.toString()));
            }
        }
        if (ids.isEmpty()) {
            return Result.error("请选择要审核的预约");
        }
        if (params.get("status") == null) {
            return Result.error("参数错误");
        }
        Integer status = Integer.valueOf(params.get("status").toString());
        String auditOpinion = params.get("auditOpinion") == null ? null : params.get("auditOpinion").toString();
        adminService.batchAudit(ids, status, auditOpinion);
        return Result.success();
    }

    // --- 晚归/外出报备审核（仅宿舍管理员，超级管理员不负责此项） ---

    @GetMapping("/report/pending")
    public Result listPendingReports() {
        if (!"admin".equals(getCurrentUserRole())) {
            return Result.error("无权限");
        }
        return Result.success(adminService.listPendingReports());
    }

    @GetMapping("/report/list")
    public Result listAllReports() {
        if (!"admin".equals(getCurrentUserRole())) {
            return Result.error("无权限");
        }
        return Result.success(adminService.listAllReports());
    }

    @PostMapping("/report/audit")
    public Result auditReport(@RequestBody Map<String, Object> params) {
        if (!"admin".equals(getCurrentUserRole())) {
            return Result.error("无权限");
        }
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        String auditOpinion = params.get("auditOpinion") == null ? null : params.get("auditOpinion").toString();
        boolean ok = adminService.auditReport(id, status, auditOpinion);
        return ok ? Result.success() : Result.error("审核失败，请确认记录仍为待审核状态");
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
        if (blacklist == null || blacklist.getUserId() == null) {
            return Result.error("用户ID不能为空");
        }
        boolean ok = adminService.addUserToBlacklist(blacklist);
        return ok ? Result.success() : Result.error("添加黑名单失败，请确认用户ID存在");
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

    /**
     * 超级管理员：创建管理员账号（不可通过公开注册获得）
     */
    @PostMapping("/user/create-admin")
    public Result<?> createAdmin(@RequestBody Map<String, Object> params) {
        if (!"super_admin".equals(getCurrentUserRole())) {
            return Result.error("无权限");
        }
        String username = params.get("username") == null ? "" : params.get("username").toString().trim();
        String password = params.get("password") == null ? "" : params.get("password").toString();
        String realName = params.get("realName") == null ? "" : params.get("realName").toString().trim();
        String phone = params.get("phone") == null ? "" : params.get("phone").toString().trim();

        if (username.isEmpty()) return Result.error("账号不能为空");
        if (password.isEmpty()) return Result.error("密码不能为空");
        if (password.length() < 6) return Result.error("密码至少6位");
        if (realName.isEmpty()) return Result.error("真实姓名不能为空");

        boolean ok = adminService.createAdminAccount(username, password, realName, phone);
        return ok ? Result.success() : Result.error("创建失败，账号可能已存在");
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
     * 管理员：导出“当前在学校内的人”为 Excel
     */
    @GetMapping("/dashboard/inside-people/export")
    public void exportInsidePeople(HttpServletResponse response) throws Exception {
        String role = getCurrentUserRole();
        if (!"admin".equals(role) && !"super_admin".equals(role)) {
            response.setStatus(403);
            return;
        }

        List<Reservation> list = adminService.listCurrentlyInSchool();

        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "inside_people_" + ts + ".xlsx";

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + encoded);

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("InsidePeople");

            int r = 0;
            Row h = sheet.createRow(r++);
            h.createCell(0).setCellValue("角色姓名");
            h.createCell(1).setCellValue("预约时间段");

            for (Reservation it : list) {
                Row row = sheet.createRow(r++);
                String roleText;
                if ("student".equals(it.getRole())) roleText = "学生";
                else if ("admin".equals(it.getRole())) roleText = "管理员";
                else if ("super_admin".equals(it.getRole())) roleText = "超级管理员";
                else roleText = "";

                String realName = it.getRealName() == null ? "" : it.getRealName();
                String date = it.getReservationDate() == null ? "" : it.getReservationDate().toString();
                String start = it.getStartTime() == null ? "" : it.getStartTime().toString().substring(0, 5);
                String end = it.getEndTime() == null ? "" : it.getEndTime().toString().substring(0, 5);
                String timeText = date + (!start.isEmpty() && !end.isEmpty() ? (" " + start + "-" + end) : "");

                row.createCell(0).setCellValue(roleText + realName);
                row.createCell(1).setCellValue(timeText);
            }

            wb.write(response.getOutputStream());
        }
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

    private String formatTime(Object time) {
        if (time == null) return "";
        String s = String.valueOf(time);
        return s.length() >= 5 ? s.substring(0, 5) : s;
    }

    private String statusText(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 0:
                return "待审核";
            case 1:
                return "已通过";
            case 2:
                return "已拒绝";
            case 3:
                return "已使用";
            case 4:
                return "已取消";
            case 5:
                return "已失效";
            default:
                return "未知";
        }
    }
}
