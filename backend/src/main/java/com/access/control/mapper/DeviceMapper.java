package com.access.control.mapper;

import com.access.control.entity.Device;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;
import java.util.List;

@Mapper
public interface DeviceMapper {

    /** 仅正常门禁（1）；维护中(2)、故障(0)不包含 */
    @Select("select * from device where status = 1")
    List<Device> listAllActive();

    @Select("select * from device order by created_at desc")
    List<Device> listAll();

    @Insert("insert into device(name, location, status, description, created_at, updated_at) " +
            "values(#{name}, #{location}, #{status}, #{description}, now(), now())")
    int insert(Device device);

    @Update("update device set name=#{name}, location=#{location}, status=#{status}, " +
            "description=#{description}, updated_at=now() where id=#{id}")
    int update(Device device);

    @Delete("delete from device where id=#{id}")
    int delete(Long id);

    @Select("select * from device where id = #{id}")
    Device getById(Long id);

    @Select("select d.* from device d inner join favorite f on d.id = f.device_id " +
            "where f.user_id = #{userId} and d.status = 1")
    List<Device> listFavoritesByUserId(Long userId);
}
