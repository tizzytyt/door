package com.access.control.mapper;

import com.access.control.entity.AnnouncementRead;
import com.access.control.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AnnouncementReadMapper {

    /** 学生端：标记已读（幂等，已读过不会重复插入） */
    @Insert("insert into announcement_read(announcement_id, user_id, read_at) " +
            "values(#{announcementId}, #{userId}, now()) " +
            "on duplicate key update read_at = now()")
    int insertOrIgnore(@Param("announcementId") Long announcementId, @Param("userId") Long userId);

    /** 管理端：已读明细 */
    @Select("select ar.*, u.username as username, u.real_name as real_name " +
            "from announcement_read ar join user u on ar.user_id = u.id " +
            "where ar.announcement_id = #{announcementId} " +
            "order by ar.read_at desc")
    List<AnnouncementRead> listReaders(@Param("announcementId") Long announcementId);

    /** 管理端：未读明细（学生列表中排除已读用户） */
    @Select("select u.* from user u " +
            "where u.role = 'student' and not exists (" +
            "  select 1 from announcement_read ar " +
            "  where ar.announcement_id = #{announcementId} and ar.user_id = u.id" +
            ") " +
            "order by u.created_at desc")
    List<User> listUnreadUsers(@Param("announcementId") Long announcementId);
}

