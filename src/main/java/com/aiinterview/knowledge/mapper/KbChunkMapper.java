package com.aiinterview.knowledge.mapper;

import com.aiinterview.knowledge.entity.KbChunkEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface KbChunkMapper extends BaseMapper<KbChunkEntity> {

    @Select("select count(1) from kb_chunks where doc_id = #{docId}")
    long countByDocId(@Param("docId") Long docId);

    @Select("""
            select *
              from kb_chunks
             where doc_id = #{docId}
             order by chunk_index asc
             limit #{offset}, #{limit}
            """)
    List<KbChunkEntity> selectPageByDocId(
            @Param("docId") Long docId,
            @Param("offset") int offset,
            @Param("limit") int limit);

    @Select("select max(chunk_index) from kb_chunks where doc_id = #{docId}")
    Integer selectMaxChunkIndex(@Param("docId") Long docId);

    @Delete("delete from kb_chunks where doc_id = #{docId}")
    int deleteByDocId(@Param("docId") Long docId);
}
