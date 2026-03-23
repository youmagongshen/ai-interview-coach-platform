package com.aiinterview.report.mapper;

import com.aiinterview.report.entity.ReportEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ReportMapper extends BaseMapper<ReportEntity> {

    @Select("select * from reports where session_id = #{sessionId}")
    ReportEntity selectBySessionId(@Param("sessionId") Long sessionId);

    @Update("""
            update reports
               set summary = #{summary},
                   highlight_points = #{highlightPoints},
                   improvement_points = #{improvementPoints},
                   suggestions = #{suggestions},
                   next_plan = #{nextPlan}
             where session_id = #{sessionId}
            """)
    int updateBySessionId(ReportEntity entity);
}
