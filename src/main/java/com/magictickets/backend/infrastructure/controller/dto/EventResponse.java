package com.magictickets.backend.infrastructure.controller.dto;

import com.magictickets.backend.domain.entity.Event;
import com.magictickets.backend.domain.enums.ShowCategory;
import com.magictickets.backend.domain.enums.ShowStatus;

public record EventResponse(
    String id,
    String name,
    int stock,
    String date,
    ShowStatus status,
    ShowCategory category,
    String imageUrl
) {
    public static EventResponse fromDomain(Event event) {
        return new EventResponse(
            event.getId(),
            event.getName(),
            event.getStock(),
            event.getDate().value().toString(),
            event.getStatus(),
            event.getCategory(),
            event.getImageUrl()
        );
    }
}