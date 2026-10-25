package com.cloudalpha.demo.order;

import java.math.BigDecimal;
import java.time.Instant;

/** An order as returned by the API. */
public record Order(long id, String item, int quantity, BigDecimal unitPrice, BigDecimal total, OrderStatus status, Instant createdAt) {
}
