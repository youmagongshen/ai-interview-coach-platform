package com.aiinterview.fujian.service.impl;

import com.aiinterview.fujian.dto.AttachmentVO;
import com.aiinterview.fujian.entity.AttachmentEntity;
import com.aiinterview.fujian.mapper.AttachmentMapper;
import com.aiinterview.fujian.service.AttachmentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AttachmentServiceImpl extends ServiceImpl<AttachmentMapper, AttachmentEntity> implements AttachmentService {

    @Override
    public AttachmentVO upload(Long userId, Long sessionId, Long turnId, String fileType, MultipartFile file) {
        AttachmentEntity entity = new AttachmentEntity();
        entity.setUserId(userId);
        entity.setSessionId(sessionId);
        entity.setTurnId(turnId);
        entity.setFileType(fileType);
        entity.setFileName(file.getOriginalFilename());
        entity.setMimeType(file.getContentType());
        entity.setFileSize(file.getSize());
        entity.setFileUrl("/uploads/" + file.getOriginalFilename());
        baseMapper.insert(entity);
        return toVO(entity);
    }

    @Override
    public List<AttachmentVO> listBySession(Long userId, Long sessionId, String fileType) {
        return baseMapper.selectBySession(userId, sessionId, fileType)
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    private AttachmentVO toVO(AttachmentEntity entity) {
        AttachmentVO vo = new AttachmentVO();
        vo.setId(entity.getId());
        vo.setUserId(entity.getUserId());
        vo.setSessionId(entity.getSessionId());
        vo.setTurnId(entity.getTurnId());
        vo.setFileType(entity.getFileType());
        vo.setFileName(entity.getFileName());
        vo.setFileUrl(entity.getFileUrl());
        vo.setMimeType(entity.getMimeType());
        vo.setFileSize(entity.getFileSize());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }
}
