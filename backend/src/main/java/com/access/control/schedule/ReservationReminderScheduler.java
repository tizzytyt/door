package com.access.control.schedule;

import com.access.control.entity.Reservation;
import com.access.control.mapper.ReservationMapper;
import com.access.control.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 对已通过的预约，在开始前 30 分钟至开始前发送一次「预约提醒」通知。
 */
@Component
public class ReservationReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReservationReminderScheduler.class);

    @Autowired
    private ReservationMapper reservationMapper;
    @Autowired
    private NotificationService notificationService;

    @Scheduled(cron = "0 * * * * ?")
    public void tick() {
        try {
            List<Reservation> list = reservationMapper.listNeedingReminderSoon();
            if (list == null || list.isEmpty()) {
                return;
            }
            for (Reservation r : list) {
                try {
                    notificationService.sendReservationReminder(r);
                    reservationMapper.markReminderSent(r.getId());
                } catch (Exception e) {
                    log.warn("预约提醒发送失败 reservationId={}", r.getId(), e);
                }
            }
        } catch (Exception e) {
            log.warn("预约提醒任务执行失败", e);
        }
    }
}
