package com.aiinterview.score.mapper;

import com.aiinterview.score.entity.ScoreEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ScoreMapper extends BaseMapper<ScoreEntity> {

    @Select("select * from scores where session_id = #{sessionId} order by id asc")
    List<ScoreEntity> selectBySessionId(@Param("sessionId") Long sessionId);

    @Select("select * from scores where turn_id = #{turnId}")
    ScoreEntity selectByTurnId(@Param("turnId") Long turnId);
}
