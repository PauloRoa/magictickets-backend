package com.magictickets.backend.application.usecase;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.magictickets.backend.application.port.PurchaseNotifier;
import com.magictickets.backend.domain.entity.Event;
import com.magictickets.backend.domain.repository.EventRepository;
import com.magictickets.backend.domain.service.PurchaseValidator;
import com.magictickets.backend.domain.exception.EventNotFoundException;

@Service
public class TicketPurchaseService {

    private final EventRepository eventRepository;
    private final PurchaseNotifier notifier;
    private final PurchaseValidator validator;

    public TicketPurchaseService(EventRepository eventRepository, PurchaseNotifier notifier, PurchaseValidator validator) {
        this.eventRepository = eventRepository;
        this.notifier = notifier;
        this.validator = validator;
    }

    public void purchase(String eventId, int quantity) {
        Event event = findEvent(eventId);
        validator.validateQuantity(quantity);
        validator.validateMaxTickets(quantity);
        validator.validateStock(event, quantity);
        event.reduceStock(quantity);
        eventRepository.save(event);
        notifier.notifyPurchase(event.getName(), quantity);
    }

    private Event findEvent(String eventId) {
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty()) {
            throw new EventNotFoundException("Event not found for id: " + eventId);
        }
        return event.get();
    }
}