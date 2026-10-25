package com.cloudalpha.demo.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

/** Stage 5, unit test: the order rules. */
class OrderServiceTest {

    private final OrderService service = new OrderService(Clock.fixed(Instant.parse("2026-10-25T10:00:00Z"), ZoneOffset.UTC));

    @Test
    void totalIsQuantityTimesPrice() {
        assertThat(OrderService.total(3, new BigDecimal("19.99"))).isEqualByComparingTo("59.97");
    }

    @Test
    void discountAppliesAtTheThreshold() {
        assertThat(OrderService.total(10, new BigDecimal("100.00"))).isEqualByComparingTo("900.00");
        assertThat(OrderService.total(1, new BigDecimal("999.99"))).isEqualByComparingTo("999.99");
    }

    @Test
    void createStoresANewOrder() {
        Order o = service.create(new NewOrder("  Laptop ", 2, new BigDecimal("450.00")));
        assertThat(o.id()).isEqualTo(1);
        assertThat(o.item()).isEqualTo("Laptop");
        assertThat(o.status()).isEqualTo(OrderStatus.NEW);
        assertThat(o.createdAt()).isEqualTo(Instant.parse("2026-10-25T10:00:00Z"));
        assertThat(service.find(1)).contains(o);
        assertThat(service.list()).containsExactly(o);
    }

    @Test
    void confirmAndCancelOnlyFromNew() {
        long id = service.create(new NewOrder("Pen", 1, new BigDecimal("1.50"))).id();
        assertThat(service.confirm(id).status()).isEqualTo(OrderStatus.CONFIRMED);
        assertThatThrownBy(() -> service.cancel(id)).isInstanceOf(IllegalOrderStateException.class);

        long other = service.create(new NewOrder("Ink", 1, new BigDecimal("3.00"))).id();
        assertThat(service.cancel(other).status()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void unknownOrder() {
        assertThat(service.find(42)).isEmpty();
        assertThatThrownBy(() -> service.confirm(42)).isInstanceOf(OrderNotFoundException.class).hasMessageContaining("42");
    }

    @Test
    void listIsOrderedById() {
        service.create(new NewOrder("A", 1, BigDecimal.ONE));
        service.create(new NewOrder("B", 1, BigDecimal.ONE));
        assertThat(service.list()).extracting(Order::item).containsExactly("A", "B");
    }
}
