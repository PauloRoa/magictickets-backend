package com.magictickets.backend.application;

import org.springframework.stereotype.Service;

import com.magictickets.backend.domain.entity.Event;
import com.magictickets.backend.domain.enums.ShowCategory;
import com.magictickets.backend.domain.repository.EventRepository;
import com.magictickets.backend.domain.valueobject.EventDate;

import java.time.LocalDate;

@Service
public class EventWriteService {

    private final EventRepository eventRepository;

    public EventWriteService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Event createEvent(String name, int stock, LocalDate date, ShowCategory category, String imageUrl) {
        Event event = new Event(name, stock, new EventDate(date), category, imageUrl);
        eventRepository.save(event);
        return event;
    }
}