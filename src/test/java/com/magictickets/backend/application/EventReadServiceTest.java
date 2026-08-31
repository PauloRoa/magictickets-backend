package com.magictickets.backend.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.magictickets.backend.domain.entity.Event;
import com.magictickets.backend.domain.enums.ShowCategory;
import com.magictickets.backend.domain.repository.EventRepository;
import com.magictickets.backend.domain.valueobject.EventDate;

@ExtendWith(MockitoExtension.class)
class EventReadServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventReadService eventReadService;

    @Test
    void should_returnEventsFromRepository_when_readEventsIsCalled() {
        // Arrange
        List<Event> stored = List.of(
                new Event("Coldplay Live", 100, new EventDate(LocalDate.now().plusDays(30)), ShowCategory.MUSIC),
                new Event("Hamilton", 50, new EventDate(LocalDate.now().plusDays(60)), ShowCategory.THEATER));
        when(eventRepository.findAll()).thenReturn(stored);

        // Act
        List<Event> result = eventReadService.readEvents();

        // Assert
        assertSame(stored, result);
        assertEquals(2, result.size());
        verify(eventRepository).findAll();
    }

    @Test
    void should_returnEmptyList_when_repositoryHasNoEvents() {
        // Arrange
        when(eventRepository.findAll()).thenReturn(List.of());

        // Act
        List<Event> result = eventReadService.readEvents();

        // Assert
        assertEquals(0, result.size());
        verify(eventRepository).findAll();
    }
}
