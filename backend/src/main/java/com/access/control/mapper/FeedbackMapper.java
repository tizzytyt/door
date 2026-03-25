package com.access.control.mapper;

import com.access.control.entity.Feedback;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface FeedbackMapper {

    @Insert("insert into feedback(user_id, type, content, images, status, created_at) " +
            "values(#{userId}, #{type}, #{content}, #{images}, 0, now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Feedback feedback);

    @Select("select * from feedback where user_id = #{userId} order by created_at desc")
    List<Feedback> listByUserId(Long userId);

    // 管理员端：查看全部反馈/报修记录
    @Select("select * from feedback order by created_at desc")
    List<Feedback> listAll();

    // 管理员端：更新处理状态与回复
    @Update("update feedback set status = #{status}, admin_reply = #{adminReply} where id = #{id}")
    int updateStatusAndReply(@Param("id") Long id,
                             @Param("status") Integer status,
                             @Param("adminReply") String adminReply);
}
