package com.access.control.mapper;

import com.access.control.entity.AccessRecord;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AccessRecordMapper {

    @Insert("insert into access_record(user_id, device_id, access_time, type, reservation_id, status) " +
            "values(#{userId}, #{deviceId}, now(), #{type}, #{reservationId}, #{status})")
    int insert(AccessRecord record);

    @Select("select ar.*, d.name as device_name, d.location from access_record ar " +
            "left join device d on ar.device_id = d.id " +
            "where ar.user_id = #{userId} order by ar.access_time desc")
    List<AccessRecord> listByUserId(Long userId);
}
