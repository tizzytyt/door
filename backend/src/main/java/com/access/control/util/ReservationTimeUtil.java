package com.access.control.util;

import com.access.control.entity.Reservation;

import java.time.LocalDateTime;

/**
 * 预约时段判断（与确认使用、管理员审核等口径一致：当前时刻晚于预约结束时刻视为已过期）。
 */
public final class ReservationTimeUtil {

    private ReservationTimeUtil() {
    }

    public static boolean isExpired(Reservation reservation) {
        if (reservation == null || reservation.getReservationDate() == null || reservation.getEndTime() == null) {
            return false;
        }
        LocalDateTime end = LocalDateTime.of(reservation.getReservationDate(), reservation.getEndTime());
        return LocalDateTime.now().isAfter(end);
    }
}
