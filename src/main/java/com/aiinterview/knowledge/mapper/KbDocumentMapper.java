package com.aiinterview.knowledge.mapper;

import com.aiinterview.knowledge.entity.KbDocumentEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface KbDocumentMapper extends BaseMapper<KbDocumentEntity> {

    @Select({
            "<script>",
            "select count(1) from kb_documents",
            "<where>",
            "<if test='roleId != null'>and role_id = #{roleId}</if>",
            "<if test='status != null and status != \"\"'>and status = #{status}</if>",
            "<if test='docType != null and docType != \"\"'>and doc_type = #{docType}</if>",
            "<if test='keyword != null and keyword != \"\"'>",
            "and (title like concat('%', #{keyword}, '%')",
            "or source_name like concat('%', #{keyword}, '%')",
            "or summary like concat('%', #{keyword}, '%'))",
            "</if>",
            "</where>",
            "</script>"
    })
    long count(
            @Param("roleId") Long roleId,
            @Param("status") String status,
            @Param("docType") String docType,
            @Param("keyword") String keyword);

    @Select({
            "<script>",
            "select * from kb_documents",
            "<where>",
            "<if test='roleId != null'>and role_id = #{roleId}</if>",
            "<if test='status != null and status != \"\"'>and status = #{status}</if>",
            "<if test='docType != null and docType != \"\"'>and doc_type = #{docType}</if>",
            "<if test='keyword != null and keyword != \"\"'>",
            "and (title like concat('%', #{keyword}, '%')",
            "or source_name like concat('%', #{keyword}, '%')",
            "or summary like concat('%', #{keyword}, '%'))",
            "</if>",
            "</where>",
            "order by updated_at desc",
            "limit #{offset}, #{limit}",
            "</script>"
    })
    List<KbDocumentEntity> selectPage(
            @Param("roleId") Long roleId,
            @Param("status") String status,
            @Param("docType") String docType,
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("limit") int limit);
}
