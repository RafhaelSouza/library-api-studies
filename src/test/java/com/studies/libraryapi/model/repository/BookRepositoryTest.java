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
    public void returnTrueWhenISBNExists() {

        //given
        String isbn = "123456";
        Book book = Book.builder()
                .title("Book Title")
                .author("Book Author")
                .isbn(isbn)
                .build();
        entityManager.persist(book);

        //when
        boolean bookExists = bookRespository.existsByIsbn(isbn);

        //then
        assertThat(bookExists).isTrue();

    }

    @Test
    @DisplayName("Must return false when does not exists a book with the given ISBN")
    public void returnFalseWhenISBNDoesNotExists() {

        //given
        String isbn = "123456";

        //when
        boolean bookExists = bookRespository.existsByIsbn(isbn);

        //then
        assertThat(bookExists).isFalse();

    }

}
