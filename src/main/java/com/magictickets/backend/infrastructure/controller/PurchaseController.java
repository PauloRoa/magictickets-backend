package com.magictickets.backend.infrastructure.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.magictickets.backend.application.usecase.TicketPurchaseService;
import com.magictickets.backend.infrastructure.controller.dto.PurchaseRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Purchases", description = "Operaciones de compra de tickets")
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/v1/purchases")
public class PurchaseController {

    private final TicketPurchaseService purchaseService;

    public PurchaseController(TicketPurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @Operation(summary = "Comprar tickets para un evento", description = "Valida cantidad, límite por compra y stock disponible antes de confirmar la compra")
    @PostMapping
    public ResponseEntity<Void> purchase(@RequestBody PurchaseRequest request) {
        purchaseService.purchase(request.eventId(), request.quantity());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}