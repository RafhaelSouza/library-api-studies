package com.studies.libraryapi.service;

import com.studies.libraryapi.model.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BookService {

    Optional<Book> getById(Long id);

    Page<Book> find(Book filter, Pageable pageRequest);

    Book save(Book any);

    Book update(Book book);

    void delete(Book book);
}
