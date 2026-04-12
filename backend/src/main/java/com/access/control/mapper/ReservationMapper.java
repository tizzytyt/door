package com.access.control.mapper;

import com.access.control.entity.Reservation;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;

@Mapper
public interface ReservationMapper {

    @Insert("insert into reservation(user_id, device_id, reservation_date, start_time, end_time, reason, status, created_at, updated_at) " +
            "values(#{userId}, #{deviceId}, #{reservationDate}, #{startTime}, #{endTime}, #{reason}, #{status}, now(), now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Reservation reservation);

    @Select("select r.*, d.name as device_name, u.real_name as real_name from reservation r " +
            "left join device d on r.device_id = d.id " +
            "left join user u on r.user_id = u.id " +
            "where r.user_id = #{userId} order by r.created_at desc")
    List<Reservation> listByUserId(Long userId);

    @Select("select r.*, d.name as device_name, u.real_name as real_name from reservation r " +
            "left join device d on r.device_id = d.id " +
            "left join user u on r.user_id = u.id " +
            "order by r.created_at desc")
    List<Reservation> listAll();

    @Select("select r.*, d.name as device_name, u.real_name as real_name from reservation r " +
            "left join device d on r.device_id = d.id " +
            "left join user u on r.user_id = u.id " +
            "where r.status = #{status} order by r.created_at desc")
    List<Reservation> listByStatus(Integer status);

    @Update("update reservation set status = #{status}, audit_opinion = #{auditOpinion}, updated_at = now() " +
            "where id = #{id}")
    int audit(@Param("id") Long id, @Param("status") Integer status, @Param("auditOpinion") String auditOpinion);

    @Select("select * from reservation where id = #{id}")
    Reservation getById(Long id);

    @Update("update reservation set status = #{status}, updated_at = now() where id = #{id} and user_id = #{userId}")
    int updateStatus(@Param("id") Long id, @Param("userId") Long userId, @Param("status") Integer status);

    @Update("update reservation set status = 3, used_at = now(), updated_at = now() where id = #{id} and user_id = #{userId}")
    int confirmUsage(@Param("id") Long id, @Param("userId") Long userId);

    @Select("select count(*) from reservation where user_id = #{userId} and reservation_date = #{date} and status != 4")
    int countDailyReservations(@Param("userId") Long userId, @Param("date") java.time.LocalDate date);

    /**
     * 同一用户对同一门禁、同一日期的重叠时段是否已有待审/已通过预约（不同用户可共享同一时段）。
     */
    @Select("select count(*) from reservation where user_id = #{userId} and device_id = #{deviceId} and reservation_date = #{date} " +
            "and status in (0, 1) and ((start_time <= #{startTime} and end_time > #{startTime}) " +
            "or (start_time < #{endTime} and end_time >= #{endTime}) " +
            "or (start_time >= #{startTime} and end_time <= #{endTime}))")
    int countSameUserOverlapping(@Param("userId") Long userId, @Param("deviceId") Long deviceId, @Param("date") java.time.LocalDate date,
            @Param("startTime") java.time.LocalTime startTime, @Param("endTime") java.time.LocalTime endTime);

    /**
     * 获取“当前在学校内”的人员列表（基于预约时间段推断）
     * 口径：预约状态=3（已使用），且当前时间落在 start_time ~ end_time 内。
     */
    @Select("select r.*, d.name as device_name, u.real_name as real_name, u.role as role from reservation r " +
            "left join device d on r.device_id = d.id " +
            "left join user u on r.user_id = u.id " +
            "where r.status = 3 and r.reservation_date = #{date} " +
            "and r.start_time <= #{time} and r.end_time >= #{time} " +
            "order by r.start_time asc")
    List<Reservation> listCurrentlyInSchool(@Param("date") LocalDate date, @Param("time") LocalTime time);

    /**
     * 已通过且未发提醒，当前时间处于 [预约开始前30分钟, 预约开始时刻) 内。
     */
    @Select("select r.*, d.name as device_name, d.location as device_location from reservation r " +
            "left join device d on r.device_id = d.id " +
            "where r.status = 1 and ifnull(r.reminder_sent, 0) = 0 " +
            "and timestamp(r.reservation_date, r.start_time) > now() " +
            "and timestamp(r.reservation_date, r.start_time) - interval 30 minute <= now()")
    List<Reservation> listNeedingReminderSoon();

    @Update("update reservation set reminder_sent = 1, updated_at = now() where id = #{id}")
    int markReminderSent(@Param("id") Long id);
}
