package com.access.control.mapper;

import com.access.control.entity.Report;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ReportMapper {

    @Insert("insert into report(user_id, type, expected_time, reason, status, created_at) " +
            "values(#{userId}, #{type}, #{expectedTime}, #{reason}, 0, now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Report report);

    @Select("select * from report where user_id = #{userId} order by created_at desc")
    List<Report> listByUserId(Long userId);

    @Update("update report set status = 3 where id = #{id} and user_id = #{userId}") // 3-已撤销
    int cancel(@Param("id") Long id, @Param("userId") Long userId);
}
