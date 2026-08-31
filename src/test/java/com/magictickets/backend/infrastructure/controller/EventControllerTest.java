package com.magictickets.backend.infrastructure.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.magictickets.backend.application.EventReadService;
import com.magictickets.backend.application.EventWriteService;
import com.magictickets.backend.domain.entity.Event;
import com.magictickets.backend.domain.enums.ShowCategory;
import com.magictickets.backend.domain.enums.ShowStatus;
import com.magictickets.backend.domain.valueobject.EventDate;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventReadService eventReadService;

    @MockitoBean
    private EventWriteService eventWriteService;

    private static final LocalDate FUTURE_DATE = LocalDate.now().plusDays(90);

    private Event sampleEvent() {
        return new Event("id-1", "Coldplay Live", 100, new EventDate(FUTURE_DATE),
                ShowStatus.SCHEDULED, ShowCategory.MUSIC);
    }

    @Test
    void should_return200WithEventList_when_getEventsIsCalled() throws Exception {
        // Arrange
        when(eventReadService.readEvents()).thenReturn(List.of(sampleEvent()));

        // Act
        var result = mockMvc.perform(get("/api/v1/events"));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].id").value("id-1"))
                .andExpect(jsonPath("$[0].name").value("Coldplay Live"))
                .andExpect(jsonPath("$[0].stock").value(100))
                .andExpect(jsonPath("$[0].status").value("SCHEDULED"))
                .andExpect(jsonPath("$[0].category").value("MUSIC"))
                .andExpect(jsonPath("$[0].date.value").value(FUTURE_DATE.toString()));
        verify(eventReadService).readEvents();
    }

    @Test
    void should_return201WithCreatedEvent_when_postEventIsCalledWithValidBody() throws Exception {
        // Arrange
        when(eventWriteService.createEvent(anyString(), anyInt(), any(LocalDate.class), any(ShowCategory.class)))
                .thenReturn(sampleEvent());
        String body = """
                {
                  "name": "Coldplay Live",
                  "stock": 100,
                  "date": "%s",
                  "category": "MUSIC"
                }
                """.formatted(FUTURE_DATE);

        // Act
        var result = mockMvc.perform(post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

        // Assert
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Coldplay Live"))
                .andExpect(jsonPath("$.stock").value(100))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.category").value("MUSIC"));

        var name = org.mockito.ArgumentCaptor.forClass(String.class);
        var stock = org.mockito.ArgumentCaptor.forClass(Integer.class);
        var date = org.mockito.ArgumentCaptor.forClass(LocalDate.class);
        var category = org.mockito.ArgumentCaptor.forClass(ShowCategory.class);
        verify(eventWriteService).createEvent(name.capture(), stock.capture(), date.capture(), category.capture());
        assertEquals("Coldplay Live", name.getValue());
        assertEquals(100, stock.getValue());
        assertEquals(FUTURE_DATE, date.getValue());
        assertEquals(ShowCategory.MUSIC, category.getValue());
    }
}
