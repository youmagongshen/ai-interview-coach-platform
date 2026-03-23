package com.aiinterview.interview.mapper;

import com.aiinterview.interview.entity.SessionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SessionMapper extends BaseMapper<SessionEntity> {

    @Select({
            "<script>",
            "select count(1) from sessions",
            "where user_id = #{userId}",
            "<if test='status != null and status != \"\"'> and status = #{status}</if>",
            "<if test='interviewType != null and interviewType != \"\"'> and interview_type = #{interviewType}</if>",
            "<if test='keyword != null and keyword != \"\"'> and title like concat('%', #{keyword}, '%')</if>",
            "</script>"
    })
    long countByUser(
            @Param("userId") Long userId,
            @Param("status") String status,
            @Param("interviewType") String interviewType,
            @Param("keyword") String keyword);

    @Select({
            "<script>",
            "select * from sessions",
            "where user_id = #{userId}",
            "<if test='status != null and status != \"\"'> and status = #{status}</if>",
            "<if test='interviewType != null and interviewType != \"\"'> and interview_type = #{interviewType}</if>",
            "<if test='keyword != null and keyword != \"\"'> and title like concat('%', #{keyword}, '%')</if>",
            "order by created_at desc",
            "limit #{offset}, #{limit}",
            "</script>"
    })
    List<SessionEntity> selectPageByUser(
            @Param("userId") Long userId,
            @Param("status") String status,
            @Param("interviewType") String interviewType,
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("limit") int limit);

    @Select("select * from sessions where id = #{sessionId} and user_id = #{userId}")
    SessionEntity selectById(@Param("sessionId") Long sessionId, @Param("userId") Long userId);

    @Select({
            "<script>",
            "select count(1) from sessions",
            "where 1 = 1",
            "<if test='status != null and status != \"\"'> and status = #{status}</if>",
            "<if test='evaluationStatus != null and evaluationStatus != \"\"'> and evaluation_status = #{evaluationStatus}</if>",
            "<if test='keyword != null and keyword != \"\"'> and title like concat('%', #{keyword}, '%')</if>",
            "</script>"
    })
    long countAdmin(
            @Param("status") String status,
            @Param("evaluationStatus") String evaluationStatus,
            @Param("keyword") String keyword);

    @Select({
            "<script>",
            "select * from sessions",
            "where 1 = 1",
            "<if test='status != null and status != \"\"'> and status = #{status}</if>",
            "<if test='evaluationStatus != null and evaluationStatus != \"\"'> and evaluation_status = #{evaluationStatus}</if>",
            "<if test='keyword != null and keyword != \"\"'> and title like concat('%', #{keyword}, '%')</if>",
            "order by created_at desc",
            "limit #{offset}, #{limit}",
            "</script>"
    })
    List<SessionEntity> selectPageAdmin(
            @Param("status") String status,
            @Param("evaluationStatus") String evaluationStatus,
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("limit") int limit);

    @Select("select * from sessions where id = #{sessionId}")
    SessionEntity selectByIdAdmin(@Param("sessionId") Long sessionId);

    @Update("""
            update sessions
               set status = #{status},
                   finish_reason = #{finishReason},
                   ended_at = case when #{status} = 'FINISHED' and ended_at is null then now() else ended_at end,
                   last_active_at = now()
             where id = #{sessionId}
               and user_id = #{userId}
            """)
    int updateStatus(
            @Param("sessionId") Long sessionId,
            @Param("userId") Long userId,
            @Param("status") String status,
            @Param("finishReason") String finishReason);
}
