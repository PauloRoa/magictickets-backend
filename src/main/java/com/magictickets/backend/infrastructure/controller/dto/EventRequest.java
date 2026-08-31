package com.magictickets.backend.infrastructure.controller.dto;

import java.time.LocalDate;

import com.magictickets.backend.domain.enums.ShowCategory;

public record EventRequest(String name, int stock, LocalDate date, ShowCategory category) {
}