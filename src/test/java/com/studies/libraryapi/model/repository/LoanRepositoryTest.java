package com.studies.libraryapi.model.repository;

import com.studies.libraryapi.model.entity.Book;
import com.studies.libraryapi.model.entity.Loan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;

import static com.studies.libraryapi.model.repository.BookRepositoryTest.createNewBook;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({SpringExtension.class})
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    LoanRepository repository;

    @Test
    @DisplayName("Must verify if exists a loan not returned")
    public void checkMethodExistsByBookAndNotReturned() {
        //given
        Book book = createNewBook("123");

        Loan loan = Loan.builder()
                .book(book)
                .customer("Someone")
                .loanDate(LocalDateTime.now())
                .build();

        //when
        entityManager.persist(book);
        entityManager.persist(loan);

        //then
        boolean exists = repository.existsByBookAndNotReturned(book);
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Must find loan by book or customer")
    public void checkMethodFindByBookIsbnOrCustomer() {

        //given
        Book book = createNewBook("123");

        Loan loan = Loan.builder()
                .book(book)
                .customer("Someone")
                .loanDate(LocalDateTime.now())
                .build();

        //when
        entityManager.persist(book);
        entityManager.persist(loan);
        Page<Loan> result = repository.findByBookIsbnOrCustomer("123", "Someone", PageRequest.of(0, 10));

        //then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()).contains(loan);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getTotalElements()).isEqualTo(1);

    }

}
