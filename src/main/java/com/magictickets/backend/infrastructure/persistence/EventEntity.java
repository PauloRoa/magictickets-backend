package com.magictickets.backend.infrastructure.persistence;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.magictickets.backend.domain.entity.Event;
import com.magictickets.backend.domain.enums.ShowCategory;
import com.magictickets.backend.domain.enums.ShowStatus;
import com.magictickets.backend.domain.valueobject.EventDate;

@Entity
@Table(name = "events")
public class EventEntity {

    @Id
    private String id;

    private String name;

    private int stock;

    private LocalDate eventDate;

    @Enumerated(EnumType.STRING)
    private ShowStatus status;

    @Enumerated(EnumType.STRING)
    private ShowCategory category;

    protected EventEntity() {
        // Constructor vacío requerido por JPA/Hibernate
    }

    public static EventEntity fromDomain(Event event) {
        EventEntity entity = new EventEntity();
        entity.id = event.getId();
        entity.name = event.getName();
        entity.stock = event.getStock();
        entity.eventDate = event.getDate().value();
        entity.status = event.getStatus();
        entity.category = event.getCategory();
        return entity;
    }

    public Event toDomain() {
        return new Event(
            id,
            name,
            stock,
            new EventDate(eventDate),
            status,
            category
        );
    }
}