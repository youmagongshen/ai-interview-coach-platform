package com.aiinterview.report.service;

import com.aiinterview.report.dto.ReportVO;
import com.aiinterview.report.entity.ReportEntity;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ReportService extends IService<ReportEntity> {

    ReportVO getBySession(Long userId, Long sessionId);

    ReportVO getBySessionAdmin(Long sessionId);
}
