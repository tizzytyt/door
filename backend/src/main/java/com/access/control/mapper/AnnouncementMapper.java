package com.access.control.mapper;

import com.access.control.entity.Announcement;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AnnouncementMapper {

    @Insert("insert into announcement(title, content, publisher_id, created_at, updated_at) " +
            "values(#{title}, #{content}, #{publisherId}, now(), now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Announcement announcement);

    @Update("update announcement set title = #{title}, content = #{content}, updated_at = now() where id = #{id}")
    int update(Announcement announcement);

    @Delete("delete from announcement where id = #{id}")
    int deleteById(@Param("id") Long id);

    @Select("select a.*, u.real_name as publisher_name " +
            "from announcement a left join user u on a.publisher_id = u.id " +
            "where a.id = #{id}")
    Announcement getById(@Param("id") Long id);

    /** 管理端：公告详情（带已读数与学生总数） */
    @Select("select a.*, u.real_name as publisher_name, " +
            "(select count(*) from announcement_read ar where ar.announcement_id = a.id) as read_count, " +
            "(select count(*) from user where role = 'student') as total_students " +
            "from announcement a left join user u on a.publisher_id = u.id " +
            "where a.id = #{id}")
    Announcement getAdminDetail(@Param("id") Long id);

    /** 管理端：公告列表（带已读数与学生总数） */
    @Select("select a.*, u.real_name as publisher_name, " +
            "(select count(*) from announcement_read ar where ar.announcement_id = a.id) as read_count, " +
            "(select count(*) from user where role = 'student') as total_students " +
            "from announcement a left join user u on a.publisher_id = u.id " +
            "order by a.created_at desc")
    List<Announcement> listAdmin();

    /** 学生端：公告列表（带当前用户已读状态） */
    @Select("select a.*, " +
            "case when ar.id is null then 0 else 1 end as is_read, " +
            "ar.read_at as read_at " +
            "from announcement a " +
            "left join announcement_read ar on a.id = ar.announcement_id and ar.user_id = #{userId} " +
            "order by a.created_at desc")
    List<Announcement> listForUser(@Param("userId") Long userId);

    @Select("select count(*) from announcement a " +
            "where not exists (select 1 from announcement_read ar where ar.announcement_id = a.id and ar.user_id = #{userId})")
    long countUnreadForUser(@Param("userId") Long userId);
}

