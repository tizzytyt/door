package com.access.control.service.impl;

import com.access.control.entity.Feedback;
import com.access.control.entity.FeedbackMessage;
import com.access.control.mapper.FeedbackMapper;
import com.access.control.mapper.FeedbackMessageMapper;
import com.access.control.service.FeedbackChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class FeedbackChatServiceImpl implements FeedbackChatService {

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Autowired
    private FeedbackMessageMapper feedbackMessageMapper;

    private boolean canAccessFeedback(Feedback feedback, Long currentUserId, String currentUserRole) {
        if (feedback == null) return false;
        if ("student".equals(currentUserRole)) {
            return feedback.getUserId() != null && feedback.getUserId().equals(currentUserId);
        }
        // admin/super_admin：允许访问
        return "admin".equals(currentUserRole) || "super_admin".equals(currentUserRole);
    }

    @Override
    public List<FeedbackMessage> listMessages(Long feedbackId, Long currentUserId, String currentUserRole) {
        if (feedbackId == null || currentUserId == null || currentUserRole == null) {
            return Collections.emptyList();
        }

        Feedback feedback = feedbackMapper.getById(feedbackId);
        if (!canAccessFeedback(feedback, currentUserId, currentUserRole)) {
            return Collections.emptyList();
        }

        return feedbackMessageMapper.listByFeedbackId(feedbackId);
    }

    @Override
    @Transactional
    public boolean sendMessage(Long feedbackId, String content, Long currentUserId, String currentUserRole) {
        if (feedbackId == null || currentUserId == null || currentUserRole == null) {
            return false;
        }
        if (content == null) return false;
        String trimmed = content.trim();
        if (trimmed.isEmpty()) return false;

        Feedback feedback = feedbackMapper.getById(feedbackId);
        if (!canAccessFeedback(feedback, currentUserId, currentUserRole)) {
            return false;
        }

        FeedbackMessage message = new FeedbackMessage();
        message.setFeedbackId(feedbackId);
        message.setSenderUserId(currentUserId);
        message.setSenderRole(currentUserRole);
        message.setContent(trimmed);

        return feedbackMessageMapper.insert(message) > 0;
    }
}

