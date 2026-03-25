package com.access.control.mapper;

import com.access.control.entity.VisitorReservation;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface VisitorReservationMapper {

    @Insert("insert into visitor_reservation(user_id, visitor_name, visitor_phone, visit_reason, visit_date, start_time, end_time, status, created_at) " +
            "values(#{userId}, #{visitorName}, #{visitorPhone}, #{visitReason}, #{visitDate}, #{startTime}, #{endTime}, 0, now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(VisitorReservation visitorReservation);

    @Select("select * from visitor_reservation where user_id = #{userId} order by created_at desc")
    List<VisitorReservation> listByUserId(Long userId);

    @Update("update visitor_reservation set status = 3 where id = #{id} and user_id = #{userId}") // 3-已取消
    int cancel(@Param("id") Long id, @Param("userId") Long userId);
}
