package com.cloudalpha.demo.order;

/** No order with this id. */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(long id) {
        super("Order " + id + " not found");
    }
}
