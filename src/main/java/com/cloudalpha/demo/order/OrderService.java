package com.cloudalpha.demo.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

/**
 * In-memory order store. Kept deliberately simple: the point of this demo is
 * the pipeline, not persistence.
 */
@Service
public class OrderService {

    /** Orders of this total or more get a 10% discount. */
    static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("1000.00");
    static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");

    private final Map<Long, Order> orders = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);
    private final Clock clock;

    public OrderService() {
        this(Clock.systemUTC());
    }

    OrderService(Clock clock) {
        this.clock = clock;
    }

    public Order create(NewOrder request) {
        BigDecimal total = total(request.quantity(), request.unitPrice());
        long id = nextId.getAndIncrement();
        Order order = new Order(id, request.item().trim(), request.quantity(), request.unitPrice(), total, OrderStatus.NEW, Instant.now(clock));
        orders.put(id, order);
        return order;
    }

    public Optional<Order> find(long id) {
        return Optional.ofNullable(orders.get(id));
    }

    public List<Order> list() {
        return orders.values().stream().sorted((a, b) -> Long.compare(a.id(), b.id())).toList();
    }

    public Order confirm(long id) {
        return changeStatus(id, OrderStatus.CONFIRMED);
    }

    public Order cancel(long id) {
        return changeStatus(id, OrderStatus.CANCELLED);
    }

    /** quantity × unit price, minus the discount at or above the threshold, rounded to cents. */
    static BigDecimal total(int quantity, BigDecimal unitPrice) {
        BigDecimal gross = unitPrice.multiply(BigDecimal.valueOf(quantity));
        if (gross.compareTo(DISCOUNT_THRESHOLD) >= 0) {
            gross = gross.subtract(gross.multiply(DISCOUNT_RATE));
        }
        return gross.setScale(2, RoundingMode.HALF_UP);
    }

    private Order changeStatus(long id, OrderStatus target) {
        Order current = find(id).orElseThrow(() -> new OrderNotFoundException(id));
        if (current.status() != OrderStatus.NEW) {
            throw new IllegalOrderStateException(id, current.status(), target);
        }
        Order updated = new Order(current.id(), current.item(), current.quantity(), current.unitPrice(), current.total(), target, current.createdAt());
        orders.put(id, updated);
        return updated;
    }
}
