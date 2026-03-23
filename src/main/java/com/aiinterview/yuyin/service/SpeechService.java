package com.aiinterview.yuyin.service;

import com.aiinterview.yuyin.dto.AsrResp;
import org.springframework.web.multipart.MultipartFile;

public interface SpeechService {

    AsrResp asr(MultipartFile file);
}
