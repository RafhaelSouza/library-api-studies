package com.studies.libraryapi.service;

import com.studies.libraryapi.api.dto.LoanFilterDTO;
import com.studies.libraryapi.exception.BusinessException;
import com.studies.libraryapi.model.entity.Book;
import com.studies.libraryapi.model.entity.Loan;
import com.studies.libraryapi.model.repository.LoanRepository;
import com.studies.libraryapi.service.impl.LoanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class})
@ActiveProfiles("test")
public class LoanServiceTest {

    @MockBean
    LoanRepository repository;

    LoanService service;

    @BeforeEach
    public void setUp() {
        this.service = new LoanServiceImpl(repository);
    }

    @Test
    @DisplayName("Must persist a loan")
    public void mustSaveLoanTest() {
        //given
        Book book = Book.builder().id(1L).build();
        String customer = "Someone";

        Loan savingLoan = Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(LocalDateTime.now())
                .build();

        Loan savedLoan = Loan.builder()
                .id(1L)
                .book(book)
                .customer(customer)
                .loanDate(LocalDateTime.now())
                .build();

        //when
        when( repository.existsByBookAndNotReturned(book) ).thenReturn(false);
        when( repository.save(savingLoan) ).thenReturn(savedLoan);

        Loan loan = service.save(savingLoan);

        //then
        assertThat(loan.getId()).isEqualTo(savedLoan.getId());
        assertThat(loan.getBook()).isEqualTo(savedLoan.getBook());
        assertThat(loan.getCustomer()).isEqualTo(savedLoan.getCustomer());
        assertThat(loan.getLoanDate()).isEqualTo(savedLoan.getLoanDate());

    }

    @Test
    @DisplayName("Must throw a business exception when to try to save a book already loaned")
    public void mustNotSaveLoanedBookTest() {
        //given
        Book book = Book.builder().id(1L).build();
        String customer = "Someone";

        Loan savingLoan = Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(LocalDateTime.now())
                .build();

        //when
        when( repository.existsByBookAndNotReturned(book) ).thenReturn(true);

        Throwable exception = catchThrowable(() -> service.save(savingLoan));

        //then
        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Book already loaned");

        verify(repository, never()).save(savingLoan);

    }

    @Test
    @DisplayName("Must get informations about a loan by id")
    public void mustGetLoanDetailsTest() {
        //given
        Long id = 1L;
        Loan loan = createLoan();
        loan.setId(id);

        //when
        Mockito.when( repository.findById(id) ).thenReturn( Optional.of(loan) );
        Optional<Loan> result = service.getById(id);

        //then
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getId()).isEqualTo(id);
        assertThat(result.get().getBook()).isEqualTo(loan.getBook());
        assertThat(result.get().getCustomer()).isEqualTo(loan.getCustomer());
        assertThat(result.get().getLoanDate()).isEqualTo(loan.getLoanDate());

        Mockito.verify( repository ).findById(id);
    }

    @Test
    @DisplayName("Must update a loan")
    public void mustUpdateLoanTest() {
        //given
        Loan loan = createLoan();
        loan.setId(1L);
        loan.setReturned(true);

        //when
        Mockito.when( repository.save(loan) ).thenReturn( loan );
        Loan updatedLoan = service.update(loan);

        //then
        assertThat(updatedLoan.getReturned()).isTrue();

        Mockito.verify( repository ).save(loan);
    }

    @Test
    @DisplayName("Must filter a loan by properties")
    public void mustFindByLoanTest() {

        //given
        LoanFilterDTO loanFilterDTO = LoanFilterDTO.builder().customer("Someone").isbn("123").build();

        Loan loan = createLoan();
        loan.setId(1L);

        PageRequest pageRequest = PageRequest.of(0 ,10);

        List<Loan> list = Arrays.asList(loan);

        Page<Loan> page = new PageImpl<>(list, pageRequest, 1);

        //when
        when( repository.findByBookIsbnOrCustomer(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.any(PageRequest.class)))
                .thenReturn(page);

        Page<Loan> result = service.find(loanFilterDTO, pageRequest);

        //then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(list);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);

    }

    public static Loan createLoan() {
        Book book = Book.builder().id(1L).build();
        String customer = "Someone";

        return Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(LocalDateTime.now())
                .build();
    }

}
