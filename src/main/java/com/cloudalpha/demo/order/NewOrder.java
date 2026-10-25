package com.cloudalpha.demo.order;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** Request body for creating an order. */
public record NewOrder(
        @NotBlank @Size(max = 100) String item,
        @Min(1) @Max(1000) int quantity,
        @NotNull @DecimalMin(value = "0.01") BigDecimal unitPrice) {
}
