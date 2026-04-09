package com.access.control.service.impl;

import com.access.control.entity.Device;
import com.access.control.entity.Feedback;
import com.access.control.mapper.DeviceMapper;
import com.access.control.mapper.FeedbackMapper;
import com.access.control.service.StudentFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StudentFeedbackServiceImpl implements StudentFeedbackService {

    @Autowired
    private FeedbackMapper feedbackMapper;
    @Autowired
    private DeviceMapper deviceMapper;

    @Override
    public boolean submitFeedback(Feedback feedback) {
        if (feedback.getType() != null && feedback.getType() == 1) {
            if (feedback.getDeviceId() == null) {
                return false;
            }
            Device d = deviceMapper.getById(feedback.getDeviceId());
            if (d == null || d.getStatus() == null || d.getStatus() != 1) {
                return false;
            }
        } else {
            feedback.setDeviceId(null);
        }
        return feedbackMapper.insert(feedback) > 0;
    }

    @Override
    public List<Feedback> getMyFeedbacks(Long userId) {
        return feedbackMapper.listByUserId(userId);
    }
}
