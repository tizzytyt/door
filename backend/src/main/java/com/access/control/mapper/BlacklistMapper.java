package com.access.control.mapper;

import com.access.control.entity.Blacklist;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface BlacklistMapper {

    @Insert("insert into blacklist(user_id, reason, expiry_date, created_at) values(#{userId}, #{reason}, #{expiryDate}, now())")
    int insert(Blacklist blacklist);

    @Delete("delete from blacklist where user_id = #{userId}")
    int deleteByUserId(Long userId);

    @Select("select b.*, u.username, u.real_name from blacklist b left join user u on b.user_id = u.id order by b.created_at desc")
    List<Blacklist> listAll();

    @Select("select * from blacklist where user_id = #{userId} and (expiry_date is null or expiry_date > now())")
    Blacklist getActiveByUserId(Long userId);
}
