package com.aiinterview.yuyin.controller;

import com.aiinterview.common.response.ApiResponse;
import com.aiinterview.yuyin.dto.AsrResp;
import com.aiinterview.yuyin.service.SpeechService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inv/speech")
public class SpeechController {

    private final SpeechService speechService;

    @PostMapping(value = "/asr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<AsrResp> asr(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(speechService.asr(file));
    }
}

