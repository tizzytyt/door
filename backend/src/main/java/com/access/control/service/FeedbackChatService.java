package com.access.control.service;

import com.access.control.entity.FeedbackMessage;

import java.util.List;

public interface FeedbackChatService {
    List<FeedbackMessage> listMessages(Long feedbackId, Long currentUserId, String currentUserRole);

    boolean sendMessage(Long feedbackId, String content, Long currentUserId, String currentUserRole);
}

