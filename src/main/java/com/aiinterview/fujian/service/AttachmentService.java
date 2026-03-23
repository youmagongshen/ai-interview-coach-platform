package com.aiinterview.fujian.service;

import com.aiinterview.fujian.dto.AttachmentVO;
import com.aiinterview.fujian.entity.AttachmentEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface AttachmentService extends IService<AttachmentEntity> {

    AttachmentVO upload(Long userId, Long sessionId, Long turnId, String fileType, MultipartFile file);

    List<AttachmentVO> listBySession(Long userId, Long sessionId, String fileType);
}
