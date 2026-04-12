package com.access.control.service.impl;

import com.access.control.entity.Device;
import com.access.control.entity.Feedback;
import com.access.control.entity.User;
import com.access.control.mapper.DeviceMapper;
import com.access.control.mapper.FeedbackMapper;
import com.access.control.mapper.UserMapper;
import com.access.control.service.NotificationService;
import com.access.control.service.StudentFeedbackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j

@Service
public class StudentFeedbackServiceImpl implements StudentFeedbackService {

    @Autowired
    private FeedbackMapper feedbackMapper;
    @Autowired
    private DeviceMapper deviceMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private NotificationService notificationService;

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
        int rows = feedbackMapper.insert(feedback);
        if (rows > 0 && feedback.getType() != null && feedback.getType() == 1) {
            try {
                User u = userMapper.getById(feedback.getUserId());
                String display = "";
                if (u != null) {
                    if (u.getRealName() != null && !u.getRealName().trim().isEmpty()) {
                        display = u.getRealName().trim();
                    } else if (u.getUsername() != null) {
                        display = u.getUsername();
                    }
                }
                notificationService.notifyAdminsNewDeviceRepair(feedback, display);
            } catch (Exception e) {
                log.warn("报修已保存，但管理员通知发送失败 feedbackId={}", feedback.getId(), e);
            }
        }
        return rows > 0;
    }

    @Override
    public List<Feedback> getMyFeedbacks(Long userId) {
        return feedbackMapper.listByUserId(userId);
    }
}
