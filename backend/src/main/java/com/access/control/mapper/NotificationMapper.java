package com.access.control.mapper;

import com.access.control.entity.Notification;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface NotificationMapper {

    @Insert("insert into notification(user_id, title, content, is_read, type, created_at) " +
            "values(#{userId}, #{title}, #{content}, 0, #{type}, now())")
    int insert(Notification n);

    @Select("select count(*) from notification where user_id = #{userId} and is_read = 0 and type in (1, 2)")
    int countUnreadReservationTypes(@Param("userId") Long userId);

    @Select("select * from notification where user_id = #{userId} and type in (1, 2) order by created_at desc limit 200")
    List<Notification> listReservationTypes(@Param("userId") Long userId);

    @Update("update notification set is_read = 1 where id = #{id} and user_id = #{userId}")
    int markRead(@Param("id") Long id, @Param("userId") Long userId);

    /** 管理员：新报修提醒（type=4） */
    @Select("select count(*) from notification where user_id = #{userId} and is_read = 0 and type = 4")
    int countUnreadAdminRepair(@Param("userId") Long userId);

    @Select("select * from notification where user_id = #{userId} and type = 4 and is_read = 0 order by created_at desc limit 30")
    List<Notification> listUnreadAdminRepair(@Param("userId") Long userId);
}
