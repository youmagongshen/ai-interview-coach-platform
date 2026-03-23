package com.aiinterview.role.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class RoleWeightUpdateReq {

    @Size(max = 500, message = "description too long")
    private String description;

    @NotNull(message = "weightCorrectness is required")
    @DecimalMin(value = "0", message = "weightCorrectness must be >= 0")
    @DecimalMax(value = "100", message = "weightCorrectness must be <= 100")
    private BigDecimal weightCorrectness;

    @NotNull(message = "weightDepth is required")
    @DecimalMin(value = "0", message = "weightDepth must be >= 0")
    @DecimalMax(value = "100", message = "weightDepth must be <= 100")
    private BigDecimal weightDepth;

    @NotNull(message = "weightLogic is required")
    @DecimalMin(value = "0", message = "weightLogic must be >= 0")
    @DecimalMax(value = "100", message = "weightLogic must be <= 100")
    private BigDecimal weightLogic;

    @NotNull(message = "weightMatch is required")
    @DecimalMin(value = "0", message = "weightMatch must be >= 0")
    @DecimalMax(value = "100", message = "weightMatch must be <= 100")
    private BigDecimal weightMatch;

    @NotNull(message = "weightExpression is required")
    @DecimalMin(value = "0", message = "weightExpression must be >= 0")
    @DecimalMax(value = "100", message = "weightExpression must be <= 100")
    private BigDecimal weightExpression;
}