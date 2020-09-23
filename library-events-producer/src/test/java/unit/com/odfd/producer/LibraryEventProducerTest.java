package com.odfd.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odfd.domain.Book;
import com.odfd.domain.LibraryEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibraryEventProducerTest {

    @Mock
    KafkaTemplate<Integer,String> kafkaTemplate;

    @Spy
    ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    LibraryEventProducer eventProducer;

    @Test
    void sendLibraryEventWithProducerRecord_failure() throws JsonProcessingException, ExecutionException, InterruptedException {

        Book book = Book.builder()
                .bookId(null)
                .bookAuthor(null)
                .bookName("Kafka using SpringBoot")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .book(book)
                .build();

        SettableListenableFuture future = new SettableListenableFuture();
        future.setException(new RuntimeException("Exception Calling Kafka"));

        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);

        assertThrows(Exception.class, () -> eventProducer.sendLibraryEventWithProducerRecord(libraryEvent).get());
    }

    @Test
    void sendLibraryEventWithProducerRecord_success() throws JsonProcessingException, ExecutionException, InterruptedException {

        Book book = Book.builder()
                .bookId(null)
                .bookAuthor(null)
                .bookName("Kafka using SpringBoot")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .book(book)
                .build();

        String recordString = objectMapper.writeValueAsString(libraryEvent);
        SettableListenableFuture future = new SettableListenableFuture();


        ProducerRecord<Integer,String> producerRecord =
                new ProducerRecord("library-events",libraryEvent.getLibraryEventId(), recordString);
        RecordMetadata recordMetadata =
                new RecordMetadata(new TopicPartition("library-events", 1),1,1,342,System.currentTimeMillis(),1,1);
        SendResult<Integer,String> sendResult = new SendResult<>(producerRecord, recordMetadata);

        future.set(sendResult);

        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);

        ListenableFuture<SendResult<Integer,String>> listenableFuture = eventProducer.sendLibraryEventWithProducerRecord(libraryEvent);

        assertNotNull(listenableFuture.get());

    }
}
