package com.magictickets.backend.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.magictickets.backend.domain.entity.Event;
import com.magictickets.backend.domain.repository.EventRepository;

@Service
public class EventReadService {
    private final EventRepository eventRepository;

    public EventReadService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<Event> readEvents() {
        return eventRepository.findAll();
    }
}