package com.access.control.mapper;

import com.access.control.entity.Feedback;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface FeedbackMapper {

    @Insert("insert into feedback(user_id, device_id, type, content, images, status, created_at) " +
            "values(#{userId}, #{deviceId}, #{type}, #{content}, #{images}, 0, now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Feedback feedback);

    @Select("select f.*, d.name as device_name from feedback f " +
            "left join device d on f.device_id = d.id " +
            "where f.user_id = #{userId} order by f.created_at desc")
    List<Feedback> listByUserId(Long userId);

    @Select("select f.*, d.name as device_name from feedback f " +
            "left join device d on f.device_id = d.id " +
            "where f.id = #{id}")
    Feedback getById(@Param("id") Long id);

    // 管理员端：查看全部反馈/报修记录
    @Select("select f.*, d.name as device_name from feedback f " +
            "left join device d on f.device_id = d.id " +
            "order by f.created_at desc")
    List<Feedback> listAll();

    // 管理员端：更新处理状态与回复
    @Update("update feedback set status = #{status}, admin_reply = #{adminReply} where id = #{id}")
    int updateStatusAndReply(@Param("id") Long id,
                             @Param("status") Integer status,
                             @Param("adminReply") String adminReply);
}
