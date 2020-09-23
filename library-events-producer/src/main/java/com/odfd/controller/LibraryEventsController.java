package com.odfd.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.odfd.domain.LibraryEvent;
import com.odfd.domain.LibraryEventType;
import com.odfd.producer.LibraryEventProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@Slf4j
public class LibraryEventsController {

    final LibraryEventProducer libraryEventProducer;

    public LibraryEventsController(LibraryEventProducer libraryEventProducer) {
        this.libraryEventProducer = libraryEventProducer;
    }

    @PostMapping("/v1/libraryevent")
    public ResponseEntity<LibraryEvent> postLibraryEvent(@RequestBody @Valid LibraryEvent libraryEvent) throws JsonProcessingException {
        libraryEvent.setLibraryEventType(LibraryEventType.NEW);
        log.info("before sendLibraryEvent");
//        libraryEventProducer.sendLibraryEvent(libraryEvent);
//        SendResult<Integer, String> sendResult = libraryEventProducer.sendLibraryEventSynchronous(libraryEvent);
        libraryEventProducer.sendLibraryEventWithProducerRecord(libraryEvent);
//        log.info("SendResult is : {}", sendResult.toString());
        log.info("after sendLibraryEvent");
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }

    @PutMapping("/v1/libraryevent")
    public ResponseEntity<?> putLibraryEvent(@RequestBody @Valid LibraryEvent libraryEvent) throws JsonProcessingException {

        if (libraryEvent.getLibraryEventId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please pass the LibraryEventId");
        }

        libraryEvent.setLibraryEventType(LibraryEventType.UPDATE);
        log.info("before sendLibraryEvent");
        libraryEventProducer.sendLibraryEventWithProducerRecord(libraryEvent);
        log.info("after sendLibraryEvent");
        return ResponseEntity.status(HttpStatus.OK).body(libraryEvent);
    }

}
