package com.magictickets.backend.infrastructure.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.magictickets.backend.domain.exception.EventNotFoundException;
import com.magictickets.backend.domain.exception.InvalidEventDateException;
import com.magictickets.backend.domain.exception.InvalidQuantityException;
import com.magictickets.backend.domain.exception.MaxTicketsExceededException;
import com.magictickets.backend.domain.exception.OutOfStockException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void should_return404_when_handlingEventNotFoundException() {
        // Arrange
        EventNotFoundException ex = new EventNotFoundException("event missing");

        // Act
        ResponseEntity<Map<String, String>> response = handler.handleEventNotFound(ex);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("event missing", response.getBody().get("error"));
    }

    @Test
    void should_return400_when_handlingInvalidQuantityException() {
        // Arrange
        InvalidQuantityException ex = new InvalidQuantityException("bad quantity");

        // Act
        ResponseEntity<Map<String, String>> response = handler.handleValidationErrors(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("bad quantity", response.getBody().get("error"));
    }

    @Test
    void should_return400_when_handlingMaxTicketsExceededException() {
        // Arrange
        MaxTicketsExceededException ex = new MaxTicketsExceededException("too many tickets");

        // Act
        ResponseEntity<Map<String, String>> response = handler.handleValidationErrors(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("too many tickets", response.getBody().get("error"));
    }

    @Test
    void should_return400_when_handlingInvalidEventDateException() {
        // Arrange
        InvalidEventDateException ex = new InvalidEventDateException("bad date");

        // Act
        ResponseEntity<Map<String, String>> response = handler.handleValidationErrors(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("bad date", response.getBody().get("error"));
    }

    @Test
    void should_return422UnprocessableContent_when_handlingOutOfStockException() {
        // Arrange
        OutOfStockException ex = new OutOfStockException("no stock");

        // Act
        ResponseEntity<Map<String, String>> response = handler.handleOutOfStock(ex);

        // Assert
        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, response.getStatusCode());
        assertEquals(422, response.getStatusCode().value());
        assertEquals("no stock", response.getBody().get("error"));
    }
}
