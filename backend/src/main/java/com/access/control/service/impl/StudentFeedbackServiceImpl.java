package com.access.control.service.impl;

import com.access.control.entity.Feedback;
import com.access.control.mapper.FeedbackMapper;
import com.access.control.service.StudentFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StudentFeedbackServiceImpl implements StudentFeedbackService {

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Override
    public boolean submitFeedback(Feedback feedback) {
        return feedbackMapper.insert(feedback) > 0;
    }

    @Override
    public List<Feedback> getMyFeedbacks(Long userId) {
        return feedbackMapper.listByUserId(userId);
    }
}
