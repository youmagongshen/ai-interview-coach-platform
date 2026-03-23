package com.aiinterview.user.mapper;

import com.aiinterview.user.entity.UserEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {

    @Update({
            "<script>",
            "update users",
            "<set>",
            "<if test='phone != null and phone != \"\"'>phone = #{phone},</if>",
            "<if test='email != null and email != \"\"'>email = #{email},</if>",
            "</set>",
            "where id = #{id}",
            "</script>"
    })
    int updateProfile(UserEntity entity);
}
