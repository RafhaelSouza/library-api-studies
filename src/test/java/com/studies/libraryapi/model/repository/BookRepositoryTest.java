package com.studies.libraryapi.model.repository;

import com.studies.libraryapi.model.entity.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith({SpringExtension.class})
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    BookRespository bookRespository;

    @Test
    @DisplayName("Must return true when exists a book with the given ISBN")
    public void mustReturnTrueWhenISBNExists() {

        //given
        String isbn = "123456";
        Book book = createNewBook(isbn);
        entityManager.persist(book);

        //when
        boolean bookExists = bookRespository.existsByIsbn(isbn);

        //then
        assertThat(bookExists).isTrue();

    }

    @Test
    @DisplayName("Must return false when does not exists a book with the given ISBN")
    public void mustReturnFalseWhenISBNDoesNotExists() {

        //given
        String isbn = "123456";

        //when
        boolean bookExists = bookRespository.existsByIsbn(isbn);

        //then
        assertThat(bookExists).isFalse();

    }

    @Test
    @DisplayName("Must get a book by id")
    public void mustFindABookByIdTest() {

        //given
        Book book = createNewBook("123");
        entityManager.persist(book);

        //when
        Optional<Book> foundBook = bookRespository.findById(book.getId());

        //then
        assertThat(foundBook.isPresent()).isTrue();

    }

    @Test
    @DisplayName("Must save a book")
    public void mustSaveABookTest() {

        //given
        Book book = createNewBook("123");

        //when
        Book savedBook = bookRespository.save(book);

        //then
        assertThat(savedBook.getId()).isNotNull();

    }

    @Test
    @DisplayName("Must delete a book")
    public void mustDeleteABookTest() {

        //given
        Book book = createNewBook("123");
        entityManager.persist(book);
        Book foundBook = entityManager.find( Book.class, book.getId() );

        //when
        bookRespository.delete(foundBook);
        Book deletedBook = entityManager.find( Book.class, book.getId() );

        //then
        assertThat(deletedBook).isNull();

    }

    private Book createNewBook(String isbn) {
        return Book.builder()
                .title("Book Title")
                .author("Book Author")
                .isbn(isbn)
                .build();
    }

}
