package com.access.control.mapper;

import com.access.control.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import org.apache.ibatis.annotations.Insert;

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

    /** 接收报修类系统通知的管理员账号（启用状态） */
    @Select("select id from user where role in ('admin','super_admin') and ifnull(status,1) = 1")
    List<Long> listActiveAdminUserIds();

    @Insert("insert into user(username, password, real_name, role, phone, status) values(#{username}, #{password}, #{realName}, #{role}, #{phone}, #{status})")
    int insert(User user);
}
