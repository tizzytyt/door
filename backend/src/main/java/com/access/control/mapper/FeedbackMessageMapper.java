package com.access.control.mapper;

import com.access.control.entity.FeedbackMessage;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FeedbackMessageMapper {

    @Insert("insert into feedback_message(feedback_id, sender_user_id, sender_role, content, created_at) " +
            "values(#{feedbackId}, #{senderUserId}, #{senderRole}, #{content}, now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(FeedbackMessage message);

    @Select("select m.*, u.real_name as sender_real_name from feedback_message m " +
            "left join user u on m.sender_user_id = u.id " +
            "where m.feedback_id = #{feedbackId} order by m.created_at asc")
    List<FeedbackMessage> listByFeedbackId(@Param("feedbackId") Long feedbackId);
}

