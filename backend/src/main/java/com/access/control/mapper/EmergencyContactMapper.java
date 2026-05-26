package com.access.control.mapper;

import com.access.control.entity.EmergencyContact;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface EmergencyContactMapper {

    @Select("select * from emergency_contact where user_id = #{userId} order by id asc")
    List<EmergencyContact> listByUserId(Long userId);

    @Select("select count(*) from emergency_contact where user_id = #{userId}")
    int countByUserId(Long userId);

    @Select("select * from emergency_contact where id = #{id} and user_id = #{userId}")
    EmergencyContact getByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Insert("insert into emergency_contact(user_id, name, phone, relation, created_at, updated_at) " +
            "values(#{userId}, #{name}, #{phone}, #{relation}, now(), now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(EmergencyContact contact);

    @Update("update emergency_contact set name = #{name}, phone = #{phone}, relation = #{relation}, updated_at = now() " +
            "where id = #{id} and user_id = #{userId}")
    int update(@Param("id") Long id, @Param("userId") Long userId,
               @Param("name") String name, @Param("phone") String phone, @Param("relation") String relation);

    @Delete("delete from emergency_contact where id = #{id} and user_id = #{userId}")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
