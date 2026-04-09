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

    @Select("select * from report where id = #{id}")
    Report getById(Long id);

    @Select("select r.*, u.real_name as real_name, u.username as username from report r " +
            "left join user u on r.user_id = u.id where r.status = 0 order by r.created_at asc")
    List<Report> listPendingWithUser();

    @Select("select r.*, u.real_name as real_name, u.username as username from report r " +
            "left join user u on r.user_id = u.id order by r.created_at desc")
    List<Report> listAllWithUser();

    @Update("update report set status = #{status}, audit_opinion = #{auditOpinion} where id = #{id}")
    int audit(@Param("id") Long id, @Param("status") Integer status, @Param("auditOpinion") String auditOpinion);

    @Select("select count(*) from report where status = 0")
    int countPending();

    @Update("update report set status = 3 where id = #{id} and user_id = #{userId}") // 3-已撤销
    int cancel(@Param("id") Long id, @Param("userId") Long userId);
}
