package com.access.control.controller;

import com.access.control.common.BaseController;
import com.access.control.common.Result;
import com.access.control.entity.Feedback;
import com.access.control.entity.FeedbackMessage;
import com.access.control.service.FeedbackChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/feedback/chat")
public class FeedbackChatController extends BaseController {

    @Autowired
    private FeedbackChatService feedbackChatService;

    @Autowired
    private com.access.control.mapper.FeedbackMapper feedbackMapper;

    /**
     * 获取某条报修的基础信息（用于聊天页头部展示）
     */
    @GetMapping("/{feedbackId}")
    public Result getFeedbackDetail(@PathVariable Long feedbackId) {
        Feedback f = feedbackMapper.getById(feedbackId);
        if (f == null) return Result.error("反馈不存在");

        String role = getCurrentUserRole();
        Long uid = getCurrentUserId();
        boolean ok;
        if ("student".equals(role)) {
            ok = f.getUserId() != null && f.getUserId().equals(uid);
        } else {
            ok = "admin".equals(role) || "super_admin".equals(role);
        }
        if (!ok) return Result.error("无权限");
        return Result.success(f);
    }

    /**
     * 获取对话消息列表
     */
    @GetMapping("/messages/{feedbackId}")
    public Result listMessages(@PathVariable Long feedbackId) {
        List<FeedbackMessage> list = feedbackChatService.listMessages(
                feedbackId,
                getCurrentUserId(),
                getCurrentUserRole()
        );
        return Result.success(list);
    }

    /**
     * 发送消息
     */
    @PostMapping("/send/{feedbackId}")
    public Result sendMessage(@PathVariable Long feedbackId, @RequestBody Map<String, Object> params) {
        String content = params.get("content") == null ? "" : params.get("content").toString();
        boolean ok = feedbackChatService.sendMessage(
                feedbackId,
                content,
                getCurrentUserId(),
                getCurrentUserRole()
        );
        return ok ? Result.success() : Result.error("发送失败");
    }
}

