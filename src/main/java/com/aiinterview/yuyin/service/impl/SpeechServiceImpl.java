package com.aiinterview.yuyin.service.impl;

import com.aiinterview.yuyin.dto.AsrResp;
import com.aiinterview.yuyin.service.SpeechService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SpeechServiceImpl implements SpeechService {

    @Override
    public AsrResp asr(MultipartFile file) {
        AsrResp resp = new AsrResp();
        String name = file == null ? "audio" : file.getOriginalFilename();
        resp.setText("Simulated ASR text from " + (name == null ? "audio" : name));
        resp.setConfidence(0.92);
        resp.setDurationSec(18);
        return resp;
    }
}
