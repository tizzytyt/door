package com.access.control.mapper;

import com.access.control.entity.Favorite;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface FavoriteMapper {

    @Insert("insert ignore into favorite(user_id, device_id, created_at) values(#{userId}, #{deviceId}, now())")
    int insert(Favorite favorite);

    @Delete("delete from favorite where user_id = #{userId} and device_id = #{deviceId}")
    int delete(@Param("userId") Long userId, @Param("deviceId") Long deviceId);

    /** 仅展示当前状态为「正常」的门禁；维护中/故障门禁不展示 */
    @Select("select f.*, d.name as device_name, d.location from favorite f " +
            "inner join device d on f.device_id = d.id and d.status = 1 " +
            "where f.user_id = #{userId}")
    List<Favorite> listByUserId(Long userId);
}
