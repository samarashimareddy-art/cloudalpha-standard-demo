package com.cloudalpha.demo.order;

/** Only a NEW order can be confirmed or cancelled. */
public class IllegalOrderStateException extends RuntimeException {

    public IllegalOrderStateException(long id, OrderStatus current, OrderStatus target) {
        super("Order " + id + " is " + current + " and can't become " + target);
    }
}
