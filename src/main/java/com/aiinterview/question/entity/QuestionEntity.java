package com.aiinterview.question.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("questions")
public class QuestionEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long roleId;
    private String questionType;
    private String difficulty;
    private String questionText;
    private String expectedPoints;
    private String keywords;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
