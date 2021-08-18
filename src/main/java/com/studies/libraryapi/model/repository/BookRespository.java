package com.studies.libraryapi.model.repository;

import com.studies.libraryapi.model.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRespository extends JpaRepository<Book, Long> {
    boolean existsByIsbn(String isbn);
}
