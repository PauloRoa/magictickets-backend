package com.magictickets.backend.infrastructure.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import com.magictickets.backend.application.EventReadService;
import com.magictickets.backend.application.EventWriteService;
import com.magictickets.backend.domain.entity.Event;
import com.magictickets.backend.infrastructure.controller.dto.EventRequest;
import com.magictickets.backend.infrastructure.controller.dto.EventResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Events", description = "Operaciones de consulta sobre la cartelera de eventos")
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventReadService eventReadService;
    private final EventWriteService eventWriteService;

    public EventController(EventReadService eventReadService, EventWriteService eventWriteService) {
        this.eventReadService = eventReadService;
        this.eventWriteService = eventWriteService;
    }

    @Operation(summary = "Obtener la cartelera de eventos", description = "Retorna la lista completa de eventos registrados en el sistema")
    @GetMapping
    public List<EventResponse> getEvents() {
        return eventReadService.readEvents()
                .stream()
                .map(EventResponse::fromDomain)
                .toList();
    }

    @Operation(summary = "Crear un nuevo evento", description = "Registra un evento nuevo en estado SCHEDULED")
    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@RequestBody EventRequest request) {
        Event event = eventWriteService.createEvent(request.name(), request.stock(), request.date(), request.category(), request.imageUrl());
        return ResponseEntity.status(HttpStatus.CREATED).body(EventResponse.fromDomain(event));
    }
}