package com.magictickets.backend.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.magictickets.backend.domain.exception.InvalidEventDateException;

class EventDateTest {

    @Test
    void should_throwInvalidEventDateException_when_valueIsNull() {
        // Arrange
        LocalDate value = null;

        // Act
        Executable action = () -> new EventDate(value);

        // Assert
        assertThrows(InvalidEventDateException.class, action);
    }

    @Test
    void should_throwInvalidEventDateException_when_valueIsToday() {
        // Arrange
        LocalDate today = LocalDate.now();

        // Act
        Executable action = () -> new EventDate(today);

        // Assert
        assertThrows(InvalidEventDateException.class, action);
    }

    @Test
    void should_throwInvalidEventDateException_when_valueIsInThePast() {
        // Arrange
        LocalDate pastDate = LocalDate.now().minusDays(1);

        // Act
        Executable action = () -> new EventDate(pastDate);

        // Assert
        assertThrows(InvalidEventDateException.class, action);
    }

    @Test
    void should_createEventDate_when_valueIsInTheFuture() {
        // Arrange
        LocalDate futureDate = LocalDate.now().plusDays(10);

        // Act
        EventDate eventDate = new EventDate(futureDate);

        // Assert
        assertEquals(futureDate, eventDate.value());
    }
}
