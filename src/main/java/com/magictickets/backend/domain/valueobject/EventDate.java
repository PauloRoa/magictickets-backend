package com.magictickets.backend.domain.valueobject;

import java.time.LocalDate;
import com.magictickets.backend.domain.exception.InvalidEventDateException;

public record EventDate(LocalDate value) {
    public EventDate {
        if (value == null) {
            throw new InvalidEventDateException("Event date cannot be null");
        }
        if (!value.isAfter(LocalDate.now())) {
            throw new InvalidEventDateException(
                "Event date must be strictly after today: " + value + " is invalid");
        }
    }
}