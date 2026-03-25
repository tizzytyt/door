package com.access.control.mapper;

import com.access.control.entity.SystemConfig;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface SystemConfigMapper {

    @Select("select config_value from system_config where config_key = #{key}")
    String getValueByKey(String key);

    // 管理员端：查询全部配置，用于展示/维护预约规则等
    @Select("select * from system_config order by id asc")
    List<SystemConfig> listAll();

    // 管理员端：根据 key 更新配置值
    @Update("update system_config set config_value = #{configValue} where config_key = #{configKey}")
    int updateByKey(SystemConfig config);
}
