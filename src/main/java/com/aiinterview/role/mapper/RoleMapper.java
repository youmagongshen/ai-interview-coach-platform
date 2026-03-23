package com.aiinterview.role.mapper;

import com.aiinterview.role.entity.RoleEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RoleMapper extends BaseMapper<RoleEntity> {

    @Select("select * from roles order by id asc")
    List<RoleEntity> selectAll();
}
