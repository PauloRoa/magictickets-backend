package com.magictickets.backend.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.magictickets.backend.domain.entity.Event;
import com.magictickets.backend.domain.enums.ShowCategory;
import com.magictickets.backend.domain.enums.ShowStatus;
import com.magictickets.backend.domain.exception.InvalidEventDateException;
import com.magictickets.backend.domain.repository.EventRepository;

@ExtendWith(MockitoExtension.class)
class EventWriteServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventWriteService eventWriteService;

    @Test
    void should_persistAndReturnNewEvent_when_createEventIsCalledWithValidData() {
        // Arrange
        String name = "Coldplay Live";
        int stock = 100;
        LocalDate date = LocalDate.now().plusDays(30);
        ShowCategory category = ShowCategory.MUSIC;

        // Act
        Event result = eventWriteService.createEvent(name, stock, date, category, "/events/test.jpg");

        // Assert
        ArgumentCaptor<Event> saved = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(saved.capture());
        assertEquals(result, saved.getValue());
        assertEquals(name, result.getName());
        assertEquals(stock, result.getStock());
        assertEquals(date, result.getDate().value());
        assertEquals(category, result.getCategory());
        assertEquals(ShowStatus.SCHEDULED, result.getStatus());
    }

    @Test
    void should_notPersistAnything_when_dateIsNotInTheFuture() {
        // Arrange
        LocalDate today = LocalDate.now();

        // Act
        Executable action = () -> eventWriteService.createEvent("Coldplay Live", 100, today, ShowCategory.MUSIC, "/events/test.jpg");

        // Assert
        assertThrows(InvalidEventDateException.class, action);
        verify(eventRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
