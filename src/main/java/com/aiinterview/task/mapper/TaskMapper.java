package com.aiinterview.task.mapper;

import com.aiinterview.task.entity.TaskEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface TaskMapper extends BaseMapper<TaskEntity> {

    @Select({
            "<script>",
            "select count(1) from tasks",
            "where user_id = #{userId}",
            "<if test='status != null and status != \"\"'>and status = #{status}</if>",
            "</script>"
    })
    long countByUser(@Param("userId") Long userId, @Param("status") String status);

    @Select({
            "<script>",
            "select * from tasks",
            "where user_id = #{userId}",
            "<if test='status != null and status != \"\"'>and status = #{status}</if>",
            "order by created_at desc",
            "limit #{offset}, #{limit}",
            "</script>"
    })
    List<TaskEntity> selectPageByUser(
            @Param("userId") Long userId,
            @Param("status") String status,
            @Param("offset") int offset,
            @Param("limit") int limit);

    @Select("select * from tasks where id = #{taskId} and user_id = #{userId}")
    TaskEntity selectById(@Param("taskId") Long taskId, @Param("userId") Long userId);

    @Update("update tasks set status = #{status} where id = #{taskId} and user_id = #{userId}")
    int updateStatus(@Param("taskId") Long taskId, @Param("userId") Long userId, @Param("status") String status);

    @Delete("delete from tasks where id = #{taskId} and user_id = #{userId}")
    int deleteById(@Param("taskId") Long taskId, @Param("userId") Long userId);
}
