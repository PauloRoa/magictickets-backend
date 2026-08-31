package com.magictickets.backend.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.magictickets.backend.domain.entity.Event;
import com.magictickets.backend.domain.repository.EventRepository;

@Repository
public class EventRepositoryJpaAdapter implements EventRepository {

    private final EventJpaRepository jpaRepository;

    public EventRepositoryJpaAdapter(EventJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Event event) {
        jpaRepository.save(EventEntity.fromDomain(event));
    }

    @Override
    public Optional<Event> findById(String id) {
        return jpaRepository.findById(id)
                .map(entity -> entity.toDomain());
    }

    @Override
    public List<Event> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(entity -> entity.toDomain())
                .toList();
    }
}