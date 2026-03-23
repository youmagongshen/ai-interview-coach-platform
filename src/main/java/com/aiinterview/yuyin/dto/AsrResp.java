package com.aiinterview.yuyin.dto;

import lombok.Data;

@Data
public class AsrResp {

    private String text;
    private Double confidence;
    private Integer durationSec;
}
