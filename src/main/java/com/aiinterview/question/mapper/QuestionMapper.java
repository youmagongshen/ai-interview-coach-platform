package com.aiinterview.question.mapper;

import com.aiinterview.question.entity.QuestionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface QuestionMapper extends BaseMapper<QuestionEntity> {

    @Select({
            "<script>",
            "select count(1) from questions",
            "<where>",
            "<if test='roleId != null'>and role_id = #{roleId}</if>",
            "<if test='questionType != null and questionType != \"\"'>and question_type = #{questionType}</if>",
            "<if test='difficulty != null and difficulty != \"\"'>and difficulty = #{difficulty}</if>",
            "<if test='active != null'>and active = #{active}</if>",
            "<if test='keyword != null and keyword != \"\"'>",
            "and (question_text like concat('%', #{keyword}, '%')",
            "or expected_points like concat('%', #{keyword}, '%')",
            "or keywords like concat('%', #{keyword}, '%'))",
            "</if>",
            "</where>",
            "</script>"
    })
    long count(
            @Param("roleId") Long roleId,
            @Param("questionType") String questionType,
            @Param("difficulty") String difficulty,
            @Param("active") Boolean active,
            @Param("keyword") String keyword);

    @Select({
            "<script>",
            "select * from questions",
            "<where>",
            "<if test='roleId != null'>and role_id = #{roleId}</if>",
            "<if test='questionType != null and questionType != \"\"'>and question_type = #{questionType}</if>",
            "<if test='difficulty != null and difficulty != \"\"'>and difficulty = #{difficulty}</if>",
            "<if test='active != null'>and active = #{active}</if>",
            "<if test='keyword != null and keyword != \"\"'>",
            "and (question_text like concat('%', #{keyword}, '%')",
            "or expected_points like concat('%', #{keyword}, '%')",
            "or keywords like concat('%', #{keyword}, '%'))",
            "</if>",
            "</where>",
            "order by id desc",
            "limit #{offset}, #{limit}",
            "</script>"
    })
    List<QuestionEntity> selectPage(
            @Param("roleId") Long roleId,
            @Param("questionType") String questionType,
            @Param("difficulty") String difficulty,
            @Param("active") Boolean active,
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("limit") int limit);

    @Update("update questions set active = #{active} where id = #{id}")
    int updateActive(@Param("id") Long id, @Param("active") Boolean active);
}
