package com.magictickets.backend.infrastructure.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.magictickets.backend.domain.exception.EventNotFoundException;
import com.magictickets.backend.domain.exception.InvalidQuantityException;
import com.magictickets.backend.domain.exception.MaxTicketsExceededException;
import com.magictickets.backend.domain.exception.OutOfStockException;
import com.magictickets.backend.domain.exception.InvalidEventDateException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEventNotFound(EventNotFoundException ex) {
        Map<String, String> body = Map.of("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler({InvalidQuantityException.class, MaxTicketsExceededException.class, InvalidEventDateException.class})
    public ResponseEntity<Map<String, String>> handleValidationErrors(RuntimeException ex) {
        Map<String, String> body = Map.of("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(OutOfStockException.class)
    public ResponseEntity<Map<String, String>> handleOutOfStock(OutOfStockException ex) {
        Map<String, String> body = Map.of("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(body);
    }
}