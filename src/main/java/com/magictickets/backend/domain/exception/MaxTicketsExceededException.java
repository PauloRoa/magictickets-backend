package com.magictickets.backend.domain.exception;

public class MaxTicketsExceededException extends RuntimeException {
    public MaxTicketsExceededException(String message) {
        super(message);
    }
}