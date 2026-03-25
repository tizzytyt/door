package com.access.control.service;

import com.access.control.entity.Feedback;
import java.util.List;

public interface StudentFeedbackService {
    /**
     * 提交反馈
     */
    boolean submitFeedback(Feedback feedback);

    /**
     * 获取个人反馈记录
     */
    List<Feedback> getMyFeedbacks(Long userId);
}
