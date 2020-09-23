package com.odfd.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.odfd.domain.Book;
import com.odfd.domain.LibraryEvent;
import com.odfd.producer.LibraryEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.concurrent.SettableListenableFuture;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LibraryEventsController.class)
@AutoConfigureMockMvc
class LibraryEventsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    LibraryEventProducer libraryEventProducer;

    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void postLibraryEvent() throws Exception {

        Book book = Book.builder()
                .bookId(123)
                .bookAuthor("Oscar")
                .bookName("Kafka using SpringBoot")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .book(book)
                .build();

        String json = objectMapper.writeValueAsString(libraryEvent);

        when(libraryEventProducer.sendLibraryEventWithProducerRecord(isA(LibraryEvent.class))).thenReturn(new SettableListenableFuture());

        mockMvc.perform(post("/v1/libraryevent")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void postLibraryEvent_400_passing_null_Book() throws Exception {

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .book(null)
                .build();

        String json = objectMapper.writeValueAsString(libraryEvent);

        when(libraryEventProducer.sendLibraryEventWithProducerRecord(isA(LibraryEvent.class))).thenReturn(null);

        String expectedErrorMessage = "book - must not be null";
        mockMvc.perform(post("/v1/libraryevent")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(content().string(expectedErrorMessage));
    }

    @Test
    void postLibraryEvent_400_passing_null_any_mandatory_fields_book() throws Exception {

        Book book = Book.builder()
                .bookId(null)
                .bookAuthor(null)
                .bookName("Kafka using SpringBoot")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .book(book)
                .build();

        String json = objectMapper.writeValueAsString(libraryEvent);

        when(libraryEventProducer.sendLibraryEventWithProducerRecord(isA(LibraryEvent.class))).thenReturn(null);

        String expectedErrorMessage = "book.bookAuthor - must not be blank,book.bookId - must not be null";
        mockMvc.perform(post("/v1/libraryevent")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(content().string(expectedErrorMessage));
    }

    @Test
    void updateLibraryEvent() throws Exception {

        //given
        Book book = new Book().builder()
                .bookId(123)
                .bookAuthor("Oscar")
                .bookName("Kafka Using Spring Boot")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(123)
                .book(book)
                .build();
        String json = objectMapper.writeValueAsString(libraryEvent);
        when(libraryEventProducer.sendLibraryEventWithProducerRecord(isA(LibraryEvent.class))).thenReturn(null);

        //expect
        mockMvc.perform(
                put("/v1/libraryevent")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    void updateLibraryEvent_withNullLibraryEventId() throws Exception {

        //given
        Book book = new Book().builder()
                .bookId(123)
                .bookAuthor("Oscar")
                .bookName("Kafka Using Spring Boot")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .book(book)
                .build();
        String json = objectMapper.writeValueAsString(libraryEvent);
        when(libraryEventProducer.sendLibraryEventWithProducerRecord(isA(LibraryEvent.class))).thenReturn(null);

        //expect
        mockMvc.perform(
                put("/v1/libraryevent")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Please pass the LibraryEventId"));
    }
}
