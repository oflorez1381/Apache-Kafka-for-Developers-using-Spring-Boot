package com.odfd.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.odfd.service.LibraryEventsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LibraryEventsConsumer {

    private final LibraryEventsService libraryEventsService;

    public LibraryEventsConsumer(LibraryEventsService libraryEventsService) {
        this.libraryEventsService = libraryEventsService;
    }

    @KafkaListener(topics = {"library-events"})
    public void onMessage(ConsumerRecord<Integer,String> consumerRecord) throws JsonProcessingException {
        log.info("Consumer Record : {} ", consumerRecord );
        libraryEventsService.processLibraryEvent(consumerRecord);
    }

}
