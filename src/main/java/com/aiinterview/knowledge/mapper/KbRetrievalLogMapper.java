package com.aiinterview.knowledge.mapper;

import com.aiinterview.knowledge.entity.KbRetrievalLogEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface KbRetrievalLogMapper extends BaseMapper<KbRetrievalLogEntity> {

    @Select({
            "<script>",
            "select count(1) from kb_retrieval_logs",
            "<where>",
            "<if test='sessionId != null'>and session_id = #{sessionId}</if>",
            "<if test='roleId != null'>and role_id = #{roleId}</if>",
            "</where>",
            "</script>"
    })
    long count(@Param("sessionId") Long sessionId, @Param("roleId") Long roleId);

    @Select({
            "<script>",
            "select * from kb_retrieval_logs",
            "<where>",
            "<if test='sessionId != null'>and session_id = #{sessionId}</if>",
            "<if test='roleId != null'>and role_id = #{roleId}</if>",
            "</where>",
            "order by created_at desc",
            "limit #{offset}, #{limit}",
            "</script>"
    })
    List<KbRetrievalLogEntity> selectPage(
            @Param("sessionId") Long sessionId,
            @Param("roleId") Long roleId,
            @Param("offset") int offset,
            @Param("limit") int limit);
}
