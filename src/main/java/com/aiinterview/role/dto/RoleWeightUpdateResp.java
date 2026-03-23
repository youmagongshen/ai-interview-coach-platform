package com.aiinterview.role.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RoleWeightUpdateResp {

    private boolean updated;
    private BigDecimal weightTotal;
}