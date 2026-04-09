package com.access.control.controller;

import com.access.control.common.BaseController;
import com.access.control.common.Result;
import com.access.control.entity.Feedback;
import com.access.control.service.StudentFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/student/feedback")
public class StudentFeedbackController extends BaseController {

    @Autowired
    private StudentFeedbackService feedbackService;

    /**
     * 提交报修或建议反馈
     */
    @PostMapping("/submit")
    public Result submit(@RequestBody Feedback feedback) {
        feedback.setUserId(getCurrentUserId());
        boolean ok = feedbackService.submitFeedback(feedback);
        return ok ? Result.success() : Result.error("提交失败：报修请选择正常状态的门禁；建议无需门禁");
    }

    /**
     * 获取个人反馈记录列表
     */
    @GetMapping("/list")
    public Result list() {
        List<Feedback> list = feedbackService.getMyFeedbacks(getCurrentUserId());
        return Result.success(list);
    }
}
