package com.magictickets.backend.domain.entity;

import java.util.UUID;
import com.magictickets.backend.domain.valueobject.EventDate;
import com.magictickets.backend.domain.enums.ShowCategory;
import com.magictickets.backend.domain.enums.ShowStatus;


public class Event {
    private final String id;
    private final String name;
    private int stock;
    private final EventDate date;
    private ShowStatus status;
    private final ShowCategory category;
    private final String imageUrl;

    public Event(String name, int stock, EventDate date, ShowCategory category, String imageUrl) {
        if (category == null) {
            throw new IllegalArgumentException("Event category cannot be null");
        }
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.stock = stock;
        this.date = date;
        this.status = ShowStatus.SCHEDULED;
        this.category = category;
        this.imageUrl = imageUrl;
    }

    public Event(String id, String name, int stock, EventDate date, ShowStatus status, ShowCategory category, String imageUrl) {
    if (category == null) {
        throw new IllegalArgumentException("Event category cannot be null");
    }
    this.id = id;
    this.name = name;
    this.stock = stock;
    this.date = date;
    this.status = status;
    this.category = category;
    this.imageUrl = imageUrl;
}

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getStock() {
        return stock;
    }

    public EventDate getDate() {
        return date;
    }

    public ShowStatus getStatus() {
        return status;
    }

    public ShowCategory getCategory() {
        return category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void reduceStock(int quantity) {
        stock -= quantity;
    }
}