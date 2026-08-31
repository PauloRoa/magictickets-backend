package com.magictickets.backend.infrastructure.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.magictickets.backend.application.usecase.TicketPurchaseService;
import com.magictickets.backend.domain.exception.EventNotFoundException;

@WebMvcTest(PurchaseController.class)
class PurchaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketPurchaseService purchaseService;

    @Test
    void should_return201AndDelegateToService_when_purchaseRequestIsValid() throws Exception {
        // Arrange
        doNothing().when(purchaseService).purchase("evt-1", 3);
        String body = """
                { "eventId": "evt-1", "quantity": 3 }
                """;

        // Act
        var result = mockMvc.perform(post("/api/v1/purchases")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

        // Assert
        result.andExpect(status().isCreated());

        var eventId = org.mockito.ArgumentCaptor.forClass(String.class);
        var quantity = org.mockito.ArgumentCaptor.forClass(Integer.class);
        verify(purchaseService).purchase(eventId.capture(), quantity.capture());
        assertEquals("evt-1", eventId.getValue());
        assertEquals(3, quantity.getValue());
    }

    @Test
    void should_return404_when_serviceThrowsEventNotFoundException() throws Exception {
        // Arrange
        doThrow(new EventNotFoundException("Event not found for id: missing"))
                .when(purchaseService).purchase("missing", 2);
        String body = """
                { "eventId": "missing", "quantity": 2 }
                """;

        // Act
        var result = mockMvc.perform(post("/api/v1/purchases")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

        // Assert
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Event not found for id: missing"));
    }
}
