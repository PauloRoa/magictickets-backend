package com.magictickets.backend.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.magictickets.backend.domain.enums.ShowCategory;
import com.magictickets.backend.domain.enums.ShowStatus;
import com.magictickets.backend.domain.valueobject.EventDate;

class EventTest {

    private final EventDate futureDate = new EventDate(LocalDate.now().plusDays(45));

    @Test
    void should_createEvent_when_allArgumentsAreValid() {
        // Arrange
        String name = "Hamilton";
        int stock = 120;
        ShowCategory category = ShowCategory.THEATER;

        // Act
        Event event = new Event(name, stock, futureDate, category);

        // Assert
        assertNotNull(event.getId());
        assertEquals(name, event.getName());
        assertEquals(stock, event.getStock());
        assertEquals(futureDate, event.getDate());
        assertEquals(category, event.getCategory());
    }

    @Test
    void should_haveScheduledStatus_when_eventIsCreatedWithShortConstructor() {
        // Arrange
        // (futureDate already prepared)

        // Act
        Event event = new Event("Hamilton", 120, futureDate, ShowCategory.THEATER);

        // Assert
        assertEquals(ShowStatus.SCHEDULED, event.getStatus());
    }

    @Test
    void should_throwIllegalArgumentException_when_categoryIsNull_onShortConstructor() {
        // Arrange
        ShowCategory category = null;

        // Act
        Executable action = () -> new Event("Hamilton", 120, futureDate, category);

        // Assert
        assertThrows(IllegalArgumentException.class, action);
    }

    @Test
    void should_throwIllegalArgumentException_when_categoryIsNull_onFullConstructor() {
        // Arrange
        ShowCategory category = null;

        // Act
        Executable action = () -> new Event("id-1", "Hamilton", 120, futureDate, ShowStatus.LIVE, category);

        // Assert
        assertThrows(IllegalArgumentException.class, action);
    }

    @Test
    void should_preserveProvidedState_when_eventIsCreatedWithFullConstructor() {
        // Arrange
        String id = "evt-42";

        // Act
        Event event = new Event(id, "Hamilton", 120, futureDate, ShowStatus.FINISHED, ShowCategory.THEATER);

        // Assert
        assertEquals(id, event.getId());
        assertEquals(ShowStatus.FINISHED, event.getStatus());
    }

    @Test
    void should_decreaseStock_when_reduceStockIsCalled() {
        // Arrange
        Event event = new Event("Hamilton", 100, futureDate, ShowCategory.THEATER);

        // Act
        event.reduceStock(30);

        // Assert
        assertEquals(70, event.getStock());
    }
}
