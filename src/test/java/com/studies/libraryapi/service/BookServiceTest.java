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
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class})
@ActiveProfiles("test")
public class BookServiceTest {

    BookService service;

    @MockBean
    BookRespository repository;

    @BeforeEach
    public void setUp() {
        this.service = new BookServiceImpl(repository);
    }

    @Test
    @DisplayName("Must persist a book")
    public void saveBookTest() {
        // given
        Book book = createAValidBook();

        when( repository.existsByIsbn(anyString()) ).thenReturn(false);
        when( repository.save(book) ).thenReturn(
                Book.builder()
                        .id(1L)
                        .title("Book Title")
                        .author("Book Author")
                        .isbn("123456")
                        .build()
        );

        // when
        Book savedBook = service.save(book);

        //then
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getIsbn()).isEqualTo("123456");
        assertThat(savedBook.getTitle()).isEqualTo("Book Title");
        assertThat(savedBook.getAuthor()).isEqualTo("Book Author");
    }

    @Test
    @DisplayName("Must throw a business exception when to try to save a book with duplicated ISBN")
    public void mustNotSaveABookWithDuplicatedISBNTest() {
        // given
        Book book = createAValidBook();

        // when
        when( repository.existsByIsbn(anyString()) ).thenReturn(true);
        Throwable exception = Assertions.catchThrowable(() -> service.save(book));

        //then
        String errorMessage = "ISBN already created";
        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage(errorMessage);

        verify(repository, never()).save(book);
    }

    @Test
    @DisplayName("Must return a book when to try to find it by id")
    public void mustGetABookByIdTest() {

        //given
        Long id = 1L;
        Book book = createAValidBook();
        book.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(book));

        //when
        Optional<Book> foundBook = service.getById(id);

        //then
        assertThat(foundBook.isPresent()).isTrue();
        assertThat(foundBook.get().getId()).isEqualTo(id);
        assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
        assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
        assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());

    }

    @Test
    @DisplayName("Must not return a book when to try to find it by id")
    public void mustNotGetABookByIdTest() {

        //given
        Long id = 1L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        //when
        Optional<Book> foundBook = service.getById(id);

        //then
        assertThat(foundBook.isPresent()).isFalse();

    }

    @Test
    @DisplayName("Must update a book")
    public void mustUpdateABookTest() {

        //given
        Long id = 1L;
        Book updatingBook = Book.builder().id(id).build();

        //when
        Book updatedBook = createAValidBook();
        updatedBook.setId(id);
        when(repository.save(updatingBook)).thenReturn(updatedBook);

        Book book = service.update(updatingBook);

        //then
        assertThat(book.getId()).isEqualTo(updatedBook.getId());
        assertThat(book.getTitle()).isEqualTo(updatedBook.getTitle());
        assertThat(book.getAuthor()).isEqualTo(updatedBook.getAuthor());
        assertThat(book.getIsbn()).isEqualTo(updatedBook.getIsbn());
    }

    @Test
    @DisplayName("Must throw an exception when to try to update a invalid book")
    public void mustNotUpdateAInvalidBookTest() {

        //given
        Book book = new Book();

        //when
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.update(book));

        //then
        verify(repository, never()).save(book);
    }

    @Test
    @DisplayName("Must delete a book")
    public void mustDeleteABookTest() {

        //given
        Book book = Book.builder().id(1L).build();

        //when
        org.junit.jupiter.api.Assertions.assertDoesNotThrow( () -> service.delete(book));

        //then
        verify(repository, times(1)).delete(book);
    }

    @Test
    @DisplayName("Must throw an exception when to try to delete a invalid book")
    public void mustNotDeleteAInvalidBookTest() {

        //given
        Book book = new Book();

        //when
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.delete(book));

        //then
        verify(repository, never()).delete(book);
    }

    @Test
    @DisplayName("Must filter a book by properties")
    public void mustFindBookTest() {

        //given
        Book book = createAValidBook();

        PageRequest pageRequest = PageRequest.of(0 ,10);

        List<Book> list = Arrays.asList(book);

        Page<Book> page = new PageImpl<>(list, pageRequest, 1);

        //when
        when( repository.findAll( any(Example.class), any(PageRequest.class) ))
                .thenReturn(page);

        Page<Book> result = service.find(book, pageRequest);

        //then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(list);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);

    }

    @Test
    @DisplayName("Must return a book when to try to find it by isbn")
    public void mustGetABookByIsbnTest() {

        //given
        String isbn = "123";
        when(repository.findByIsbn(isbn))
                .thenReturn(Optional.of(Book.builder().id(1L).isbn(isbn).build()));

        //when
        Optional<Book> book = service.getBookByIsbn(isbn);

        //then
        assertThat(book.isPresent()).isTrue();
        assertThat(book.get().getId()).isEqualTo(1L);
        assertThat(book.get().getIsbn()).isEqualTo(isbn);

        verify(repository, times(1)).findByIsbn(isbn);

    }

    private Book createAValidBook() {
        return Book.builder()
                .title("Book Title")
                .author("Book Author")
                .isbn("123456")
                .build();
    }

}
