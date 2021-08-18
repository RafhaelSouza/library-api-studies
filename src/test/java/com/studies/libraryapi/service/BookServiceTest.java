package com.studies.libraryapi.service;

import com.studies.libraryapi.exception.BusinessException;
import com.studies.libraryapi.model.entity.Book;
import com.studies.libraryapi.model.repository.BookRespository;
import com.studies.libraryapi.service.impl.BookServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class})
@ActiveProfiles("test")
public class BookServiceTest {

    BookService bookService;

    @MockBean
    BookRespository bookRespository;

    @BeforeEach
    public void setUp() {
        this.bookService = new BookServiceImpl( bookRespository );
    }

    @Test
    @DisplayName("Must persist a book")
    public void saveBookTest() {
        // given
        Book book = createAValidBook();

        when( bookRespository.existsByIsbn(anyString()) ).thenReturn(false);
        when( bookRespository.save(book) ).thenReturn(
                Book.builder()
                        .id(1L)
                        .title("Book Title")
                        .author("Book Author")
                        .isbn("123456")
                        .build()
        );

        // when
        Book savedBook = bookService.save(book);

        //then
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getIsbn()).isEqualTo("123456");
        assertThat(savedBook.getTitle()).isEqualTo("Book Title");
        assertThat(savedBook.getAuthor()).isEqualTo("Book Author");
    }

    @Test
    @DisplayName("Must throw a business exception when to try to save a book with duplicated ISBN")
    public void shouldNotSaveABookWithDuplicatedISBNTest() {
        // given
        Book book = createAValidBook();

        // when
        when( bookRespository.existsByIsbn(anyString()) ).thenReturn(true);
        Throwable exception = Assertions.catchThrowable(() -> bookService.save(book));

        //then
        String errorMessage = "ISBN already created";
        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage(errorMessage);

        verify(bookRespository, never()).save(book);
    }

    private Book createAValidBook() {
        return Book.builder()
                .title("Book Title")
                .author("Book Author")
                .isbn("123456")
                .build();
    }

}
