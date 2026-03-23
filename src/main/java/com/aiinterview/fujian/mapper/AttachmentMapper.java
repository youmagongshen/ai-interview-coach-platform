package com.aiinterview.fujian.mapper;

import com.aiinterview.fujian.entity.AttachmentEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AttachmentMapper extends BaseMapper<AttachmentEntity> {

    @Select({
            "<script>",
            "select * from attachments",
            "where user_id = #{userId}",
            "and session_id = #{sessionId}",
            "<if test='fileType != null and fileType != \"\"'>and file_type = #{fileType}</if>",
            "order by created_at desc",
            "</script>"
    })
    List<AttachmentEntity> selectBySession(
            @Param("userId") Long userId,
            @Param("sessionId") Long sessionId,
            @Param("fileType") String fileType);
}
