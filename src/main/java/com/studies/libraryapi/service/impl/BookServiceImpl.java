package com.studies.libraryapi.service.impl;

import com.studies.libraryapi.exception.BusinessException;
import com.studies.libraryapi.model.entity.Book;
import com.studies.libraryapi.model.repository.BookRespository;
import com.studies.libraryapi.service.BookService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookServiceImpl implements BookService {

    private BookRespository bookRespository;

    public BookServiceImpl(BookRespository bookRespository) {
        this.bookRespository = bookRespository;
    }

    @Override
    public Optional<Book> getById(Long id) {
        return bookRespository.findById(id);
    }

    @Override
    public Page<Book> find(Book filter, Pageable pageRequest) {
        Example<Book> example = Example.of(filter,
                    ExampleMatcher
                            .matching()
                            .withIgnoreCase()
                            .withIgnoreNullValues()
                            .withStringMatcher( ExampleMatcher.StringMatcher.CONTAINING )
                );

        return bookRespository.findAll(example, pageRequest);
    }

    @Override
    public Book save(Book book) {
        if ( bookRespository.existsByIsbn(book.getIsbn()) )
            throw new BusinessException("ISBN already created");
        return bookRespository.save(book);
    }

    @Override
    public Book update(Book book) {
        if (checkBookNull(book))
            throw new IllegalArgumentException("Book cannot be null");
        return bookRespository.save(book);
    }

    @Override
    public void delete(Book book) {
        if (checkBookNull(book))
            throw new IllegalArgumentException("Book cannot be null");
        bookRespository.delete(book);
    }

    private boolean checkBookNull( Book book ) {
        return book == null || book.getId() == null;
    }

}
