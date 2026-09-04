package com.magictickets.backend.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.magictickets.backend.domain.entity.Event;
import com.magictickets.backend.domain.enums.ShowCategory;
import com.magictickets.backend.domain.enums.ShowStatus;
import com.magictickets.backend.domain.valueobject.EventDate;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(EventRepositoryJpaAdapter.class)
class EventRepositoryJpaAdapterTest {

    @Autowired
    private EventRepositoryJpaAdapter adapter;

    private Event newEvent(String name, int stock, LocalDate date, ShowCategory category) {
        return new Event(name, stock, new EventDate(date), category, "/events/test.jpg");
    }

    @Test
    void should_persistAndReconstructAllFields_when_savingAndLoadingById() {
        // Arrange
        LocalDate date = LocalDate.now().plusDays(120);
        Event event = newEvent("Coldplay Live", 100, date, ShowCategory.MUSIC);

        // Act
        adapter.save(event);
        Optional<Event> loaded = adapter.findById(event.getId());

        // Assert
        assertTrue(loaded.isPresent());
        Event result = loaded.get();
        assertEquals(event.getId(), result.getId());
        assertEquals("Coldplay Live", result.getName());
        assertEquals(100, result.getStock());
        assertEquals(ShowStatus.SCHEDULED, result.getStatus());
        assertEquals(ShowCategory.MUSIC, result.getCategory());
        // EventDate (record con LocalDate) -> LocalDate plano en EventEntity -> EventDate de nuevo
        assertEquals(date, result.getDate().value());
    }

    @Test
    void should_returnEmpty_when_findByIdDoesNotMatchAnyRow() {
        // Arrange
        String unknownId = "does-not-exist";

        // Act
        Optional<Event> loaded = adapter.findById(unknownId);

        // Assert
        assertTrue(loaded.isEmpty());
    }

    @Test
    void should_returnAllPersistedEvents_when_findAllIsCalledWithMultipleEvents() {
        // Arrange
        adapter.save(newEvent("Coldplay Live", 100, LocalDate.now().plusDays(30), ShowCategory.MUSIC));
        adapter.save(newEvent("Hamilton", 50, LocalDate.now().plusDays(60), ShowCategory.THEATER));
        adapter.save(newEvent("Local Derby", 200, LocalDate.now().plusDays(15), ShowCategory.SPORTS));

        // Act
        List<Event> all = adapter.findAll();

        // Assert
        assertEquals(3, all.size());
        assertTrue(all.stream().anyMatch(e -> e.getName().equals("Coldplay Live")));
        assertTrue(all.stream().anyMatch(e -> e.getName().equals("Hamilton")));
        assertTrue(all.stream().anyMatch(e -> e.getName().equals("Local Derby")));
    }
}
