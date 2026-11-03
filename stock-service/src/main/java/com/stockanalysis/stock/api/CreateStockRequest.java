package com.stockanalysis.stock.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateStockRequest(
        @NotBlank String symbol,
        @NotBlank String name,
        @NotNull @DecimalMin("0") BigDecimal price) {
}
