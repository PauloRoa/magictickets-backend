package com.magictickets.backend.domain.service;

import org.springframework.stereotype.Component;

import com.magictickets.backend.domain.entity.Event;
import com.magictickets.backend.domain.exception.MaxTicketsExceededException;
import com.magictickets.backend.domain.exception.OutOfStockException;
import com.magictickets.backend.domain.exception.InvalidQuantityException;

@Component
public class PurchaseValidator {

    public void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new InvalidQuantityException("Quantity must be greater than 0, " + quantity + " is invalid");
        }
    }

    public void validateMaxTickets(int quantity) {
        if (quantity > 5) {
            throw new MaxTicketsExceededException("5 tickets is the maximum allowed per user, " + quantity + " tickets exceed the limit");
        }
    }

    public void validateStock(Event event, int quantity) {
        if (quantity > event.getStock()) {
            throw new OutOfStockException("Not enough tickets in stock, " + quantity + " tickets requested but only " + event.getStock() + " available");
        }
    }
}