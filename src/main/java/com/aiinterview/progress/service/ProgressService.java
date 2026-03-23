package com.aiinterview.progress.service;

import com.aiinterview.progress.dto.TrendPointVO;
import com.aiinterview.progress.dto.WeakPointVO;
import java.util.List;

public interface ProgressService {

    List<TrendPointVO> trend(Long userId, Long roleId, int limit);

    List<WeakPointVO> latestWeakPoints(Long userId, Long roleId);
}
