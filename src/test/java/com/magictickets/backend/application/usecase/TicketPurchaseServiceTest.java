package com.magictickets.backend.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.magictickets.backend.application.port.PurchaseNotifier;
import com.magictickets.backend.domain.entity.Event;
import com.magictickets.backend.domain.enums.ShowCategory;
import com.magictickets.backend.domain.exception.EventNotFoundException;
import com.magictickets.backend.domain.exception.InvalidQuantityException;
import com.magictickets.backend.domain.exception.MaxTicketsExceededException;
import com.magictickets.backend.domain.exception.OutOfStockException;
import com.magictickets.backend.domain.repository.EventRepository;
import com.magictickets.backend.domain.service.PurchaseValidator;
import com.magictickets.backend.domain.valueobject.EventDate;

@ExtendWith(MockitoExtension.class)
class TicketPurchaseServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private PurchaseNotifier notifier;

    // PurchaseValidator es lógica de negocio pura sin efectos secundarios: se usa real, no mockeada.
    private final PurchaseValidator validator = new PurchaseValidator();

    private TicketPurchaseService service;

    private static final String EVENT_ID = "evt-1";

    @BeforeEach
    void setUp() {
        service = new TicketPurchaseService(eventRepository, notifier, validator);
    }

    private Event eventWithStock(int stock) {
        return new Event("Coldplay Live", stock, new EventDate(LocalDate.now().plusDays(30)), ShowCategory.MUSIC, "/events/test.jpg");
    }

    @Test
    void should_reduceStockAndSaveAndNotify_when_purchaseIsValid() {
        // Arrange
        Event event = eventWithStock(10);
        when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));

        // Act
        service.purchase(EVENT_ID, 3);

        // Assert
        assertEquals(7, event.getStock());
        verify(eventRepository).save(event);
        verify(notifier).notifyPurchase("Coldplay Live", 3);
    }

    @Test
    void should_throwEventNotFoundException_when_eventDoesNotExist() {
        // Arrange
        when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.empty());

        // Act
        Executable action = () -> service.purchase(EVENT_ID, 3);

        // Assert
        assertThrows(EventNotFoundException.class, action);
        verify(eventRepository, never()).save(any());
        verify(notifier, never()).notifyPurchase(anyString(), anyInt());
    }

    @Test
    void should_propagateInvalidQuantityException_when_quantityIsNotPositive() {
        // Arrange
        when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(eventWithStock(10)));

        // Act
        Executable action = () -> service.purchase(EVENT_ID, 0);

        // Assert
        assertThrows(InvalidQuantityException.class, action);
        verify(eventRepository, never()).save(any());
        verify(notifier, never()).notifyPurchase(anyString(), anyInt());
    }

    @Test
    void should_propagateMaxTicketsExceededException_when_quantityIsAboveTheLimit() {
        // Arrange
        when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(eventWithStock(100)));

        // Act
        Executable action = () -> service.purchase(EVENT_ID, 6);

        // Assert
        assertThrows(MaxTicketsExceededException.class, action);
        verify(eventRepository, never()).save(any());
        verify(notifier, never()).notifyPurchase(anyString(), anyInt());
    }

    @Test
    void should_propagateOutOfStockException_when_requestedQuantityExceedsStock() {
        // Arrange
        when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(eventWithStock(2)));

        // Act
        Executable action = () -> service.purchase(EVENT_ID, 3);

        // Assert
        assertThrows(OutOfStockException.class, action);
        verify(eventRepository, never()).save(any());
        verify(notifier, never()).notifyPurchase(anyString(), anyInt());
    }
}
