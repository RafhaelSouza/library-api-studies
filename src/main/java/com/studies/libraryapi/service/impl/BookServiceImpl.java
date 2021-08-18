package com.studies.libraryapi.service.impl;

import com.studies.libraryapi.exception.BusinessException;
import com.studies.libraryapi.model.entity.Book;
import com.studies.libraryapi.model.repository.BookRespository;
import com.studies.libraryapi.service.BookService;
import org.springframework.stereotype.Service;

@Service
public class BookServiceImpl implements BookService {

    private BookRespository bookRespository;

    public BookServiceImpl(BookRespository bookRespository) {
        this.bookRespository = bookRespository;
    }

    @Override
    public Book save(Book book) {
        if ( bookRespository.existsByIsbn(book.getIsbn()) )
            throw new BusinessException("ISBN already created");
        return bookRespository.save(book);
    }

}
