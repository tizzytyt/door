package com.access.control.mapper;

import com.access.control.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserMapper {

    @Select("select * from user where username = #{username}")
    User getByUsername(String username);

    @Select("select * from user order by created_at desc")
    List<User> listAll();

    @Update("update user set status = #{status} where id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("update user set password = #{password} where id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    @Select("select * from user where id = #{id}")
    User getById(Long id);
}
