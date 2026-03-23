package com.aiinterview.task.service.impl;

import com.aiinterview.common.response.PageResponse;
import com.aiinterview.task.dto.TaskCreateReq;
import com.aiinterview.task.dto.TaskVO;
import com.aiinterview.task.entity.TaskEntity;
import com.aiinterview.task.mapper.TaskMapper;
import com.aiinterview.task.service.TaskService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, TaskEntity> implements TaskService {

    @Override
    public PageResponse<TaskVO> pageByUser(Long userId, String status, int page, int pageSize) {
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, pageSize);
        int offset = (safePage - 1) * safeSize;
        long total = baseMapper.countByUser(userId, status);
        List<TaskVO> list = baseMapper.selectPageByUser(userId, status, offset, safeSize)
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return PageResponse.of(list, safePage, safeSize, total);
    }

    @Override
    public TaskVO create(Long userId, TaskCreateReq req) {
        TaskEntity entity = new TaskEntity();
        entity.setUserId(userId);
        entity.setSessionId(req.getSessionId());
        entity.setTitle(req.getTitle());
        entity.setTaskType(req.getTaskType());
        entity.setContent(req.getContent());
        entity.setStatus("TODO");
        entity.setDueDate(req.getDueDate());
        baseMapper.insert(entity);
        TaskEntity latest = baseMapper.selectById(entity.getId(), userId);
        return toVO(latest == null ? entity : latest);
    }

    @Override
    public boolean updateStatus(Long userId, Long taskId, String status) {
        return baseMapper.updateStatus(taskId, userId, status) > 0;
    }

    @Override
    public boolean delete(Long userId, Long taskId) {
        return baseMapper.deleteById(taskId, userId) > 0;
    }

    private TaskVO toVO(TaskEntity entity) {
        TaskVO vo = new TaskVO();
        vo.setId(entity.getId());
        vo.setUserId(entity.getUserId());
        vo.setSessionId(entity.getSessionId());
        vo.setTitle(entity.getTitle());
        vo.setTaskType(entity.getTaskType());
        vo.setContent(entity.getContent());
        vo.setStatus(entity.getStatus());
        vo.setDueDate(entity.getDueDate());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}
