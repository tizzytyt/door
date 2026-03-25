package com.access.control.mapper;

import com.access.control.entity.Reservation;
import org.apache.ibatis.annotations.*;
import java.util.List;

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

    @Select("select count(*) from reservation where device_id = #{deviceId} and reservation_date = #{date} " +
            "and status in (0, 1) and ((start_time <= #{startTime} and end_time > #{startTime}) " +
            "or (start_time < #{endTime} and end_time >= #{endTime}) " +
            "or (start_time >= #{startTime} and end_time <= #{endTime}))")
    int checkConflict(@Param("deviceId") Long deviceId, @Param("date") java.time.LocalDate date, 
                     @Param("startTime") java.time.LocalTime startTime, @Param("endTime") java.time.LocalTime endTime);
}
