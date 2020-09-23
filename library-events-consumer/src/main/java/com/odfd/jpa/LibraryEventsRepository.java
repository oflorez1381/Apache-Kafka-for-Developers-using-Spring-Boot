package com.odfd.jpa;

import com.odfd.entity.LibraryEvent;
import org.springframework.data.repository.CrudRepository;

public interface LibraryEventsRepository extends CrudRepository<LibraryEvent, Integer> {
}
