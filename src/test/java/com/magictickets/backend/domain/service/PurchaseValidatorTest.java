package com.magictickets.backend.domain.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.magictickets.backend.domain.entity.Event;
import com.magictickets.backend.domain.enums.ShowCategory;
import com.magictickets.backend.domain.exception.InvalidQuantityException;
import com.magictickets.backend.domain.exception.MaxTicketsExceededException;
import com.magictickets.backend.domain.exception.OutOfStockException;
import com.magictickets.backend.domain.valueobject.EventDate;

class PurchaseValidatorTest {

    private final PurchaseValidator validator = new PurchaseValidator();

    private Event eventWithStock(int stock) {
        return new Event("Coldplay Live", stock, new EventDate(LocalDate.now().plusDays(30)), ShowCategory.MUSIC, "/events/test.jpg");
    }

    // --- validateQuantity -------------------------------------------------

    @Test
    void should_pass_when_quantity_is_positive() {
        // Arrange
        int quantity = 3;

        // Act
        Executable action = () -> validator.validateQuantity(quantity);

        // Assert
        assertDoesNotThrow(action);
    }

    @Test
    void should_throwInvalidQuantityException_when_quantity_is_zero() {
        // Arrange
        int quantity = 0;

        // Act
        Executable action = () -> validator.validateQuantity(quantity);

        // Assert
        assertThrows(InvalidQuantityException.class, action);
    }

    @Test
    void should_throwInvalidQuantityException_when_quantity_is_negative() {
        // Arrange
        int quantity = -1;

        // Act
        Executable action = () -> validator.validateQuantity(quantity);

        // Assert
        assertThrows(InvalidQuantityException.class, action);
    }

    // --- validateMaxTickets ---------------------------------------------

    @Test
    void should_pass_when_quantity_equals_the_maximum_allowed() {
        // Arrange
        int quantity = 5;

        // Act
        Executable action = () -> validator.validateMaxTickets(quantity);

        // Assert
        assertDoesNotThrow(action);
    }

    @Test
    void should_throwMaxTicketsExceededException_when_quantity_is_above_the_limit() {
        // Arrange
        int quantity = 6;

        // Act
        Executable action = () -> validator.validateMaxTickets(quantity);

        // Assert
        assertThrows(MaxTicketsExceededException.class, action);
    }

    // --- validateStock ------------------------------------------------

    @Test
    void should_pass_when_requested_quantity_is_below_available_stock() {
        // Arrange
        Event event = eventWithStock(10);
        int quantity = 4;

        // Act
        Executable action = () -> validator.validateStock(event, quantity);

        // Assert
        assertDoesNotThrow(action);
    }

    @Test
    void should_pass_when_requested_quantity_equals_available_stock() {
        // Arrange
        Event event = eventWithStock(4);
        int quantity = 4;

        // Act
        Executable action = () -> validator.validateStock(event, quantity);

        // Assert
        assertDoesNotThrow(action);
    }

    @Test
    void should_throwOutOfStockException_when_requested_quantity_exceeds_available_stock() {
        // Arrange
        Event event = eventWithStock(2);
        int quantity = 3;

        // Act
        Executable action = () -> validator.validateStock(event, quantity);

        // Assert
        assertThrows(OutOfStockException.class, action);
    }
}
