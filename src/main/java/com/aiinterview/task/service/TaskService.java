package com.aiinterview.task.service;

import com.aiinterview.common.response.PageResponse;
import com.aiinterview.task.dto.TaskCreateReq;
import com.aiinterview.task.dto.TaskVO;
import com.aiinterview.task.entity.TaskEntity;
import com.baomidou.mybatisplus.extension.service.IService;

public interface TaskService extends IService<TaskEntity> {

    PageResponse<TaskVO> pageByUser(Long userId, String status, int page, int pageSize);

    TaskVO create(Long userId, TaskCreateReq req);

    boolean updateStatus(Long userId, Long taskId, String status);

    boolean delete(Long userId, Long taskId);
}
