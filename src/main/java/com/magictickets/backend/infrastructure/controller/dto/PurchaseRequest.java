package com.magictickets.backend.infrastructure.controller.dto;

public record PurchaseRequest(String eventId, int quantity) {
}