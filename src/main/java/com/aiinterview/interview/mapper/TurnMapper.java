package com.aiinterview.interview.mapper;

import com.aiinterview.interview.entity.TurnEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface TurnMapper extends BaseMapper<TurnEntity> {

    @Select("select * from turns where session_id = #{sessionId} order by round_no asc, id asc")
    List<TurnEntity> selectBySessionId(@Param("sessionId") Long sessionId);

    @Select("select * from turns where id = #{turnId} and session_id = #{sessionId}")
    TurnEntity selectById(@Param("turnId") Long turnId, @Param("sessionId") Long sessionId);

    @Update({
            "<script>",
            "update turns",
            "<set>",
            "<if test='answerMode != null and answerMode != \"\"'>answer_mode = #{answerMode},</if>",
            "<if test='answerText != null'>answer_text = #{answerText},</if>",
            "<if test='audioUrl != null'>audio_url = #{audioUrl},</if>",
            "<if test='asrText != null'>asr_text = #{asrText},</if>",
            "<if test='aiReplyText != null'>ai_reply_text = #{aiReplyText},</if>",
            "<if test='aiAdvice != null'>ai_advice = #{aiAdvice},</if>",
            "<if test='responseSec != null'>response_sec = #{responseSec},</if>",
            "<if test='evaluatedAt != null'>evaluated_at = #{evaluatedAt},</if>",
            "</set>",
            "where id = #{id}",
            "<if test='sessionId != null'>and session_id = #{sessionId}</if>",
            "</script>"
    })
    int updateAnswer(TurnEntity entity);
}
