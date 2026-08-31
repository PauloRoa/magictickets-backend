package com.magictickets.backend.domain.repository;

import java.util.Optional;
import com.magictickets.backend.domain.entity.Event;
import java.util.List;

public interface EventRepository {
    void save(Event event);
    Optional<Event> findById(String id);
    List<Event> findAll();
}