package com.odfd.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odfd.entity.LibraryEvent;
import com.odfd.jpa.LibraryEventsRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.Optional;

@Service
@Slf4j
public class LibraryEventsService {

    private final ObjectMapper objectMapper;
    private final LibraryEventsRepository libraryEventsRepository;
    private final KafkaTemplate<Integer,String> kafkaTemplate;

    public LibraryEventsService(ObjectMapper objectMapper, LibraryEventsRepository libraryEventsRepository, KafkaTemplate<Integer, String> kafkaTemplate) {
        this.objectMapper = objectMapper;
        this.libraryEventsRepository = libraryEventsRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void processLibraryEvent(ConsumerRecord<Integer,String> consumerRecord) throws JsonProcessingException {

        LibraryEvent libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);
        log.info("libraryEvent : {} ",libraryEvent);

        switch (libraryEvent.getLibraryEventType()){
            case NEW:
                save(libraryEvent);
                break;
            case UPDATE:
                validate(libraryEvent);
                save(libraryEvent);
                break;
            default:
                log.info("Invalid Library Event Type");
                break;
        }

    }

    private void validate(LibraryEvent libraryEvent) {
        Integer libraryEventId = libraryEvent.getLibraryEventId();
        if( libraryEventId == null) {
            throw new IllegalArgumentException("Library Even Id is missing");
        }

        Optional<LibraryEvent> optionalLibraryEvent = libraryEventsRepository.findById(libraryEventId);

        if (!optionalLibraryEvent.isPresent()) {
            throw new IllegalArgumentException("Not a valid Library Event");
        }

        log.info("Validation is successful for the Library Event : {} ", optionalLibraryEvent.get() );
    }

    private void save(LibraryEvent libraryEvent) {
        libraryEvent.getBook().setLibraryEvent(libraryEvent);
        libraryEventsRepository.save(libraryEvent);
        log.info("Successfully Persisted the library Event : {} ", libraryEvent);
    }

    public void handleRecovery(ConsumerRecord<Integer,String> record){

        Integer key = record.key();
        String message = record.value();

        ListenableFuture<SendResult<Integer,String>> listenableFuture = kafkaTemplate.sendDefault(key, message);
        listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
            @Override
            public void onFailure(Throwable ex) {
                handleFailure(key, message, ex);
            }

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                handleSuccess(key, message, result);
            }
        });
    }

    private void handleFailure(Integer key, String value, Throwable ex) {
        log.error("Error Sending the Message and the exception is {}", ex.getMessage());
        try {
            throw ex;
        } catch (Throwable throwable) {
            log.error("Error in OnFailure: {}", throwable.getMessage());
        }
    }

    private void handleSuccess(Integer key, String value, SendResult<Integer, String> result) {
        log.info("Message Sent SuccessFully for the key : {} and the value is {} , partition is {}", key, value, result.getRecordMetadata().partition());
    }
}
