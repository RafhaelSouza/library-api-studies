package com.studies.libraryapi.api.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studies.libraryapi.api.dto.LoanDTO;
import com.studies.libraryapi.api.dto.LoanFilterDTO;
import com.studies.libraryapi.api.dto.ReturnedLoanDTO;
import com.studies.libraryapi.exception.BusinessException;
import com.studies.libraryapi.model.entity.Book;
import com.studies.libraryapi.model.entity.Loan;
import com.studies.libraryapi.service.BookService;
import com.studies.libraryapi.service.LoanService;
import com.studies.libraryapi.service.LoanServiceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith({SpringExtension.class})
@ActiveProfiles("test")
@WebMvcTest(controllers = LoanController.class)
@AutoConfigureMockMvc
public class LoanControllerTest {

    static String LOAN_API = "/api/loans";

    @Autowired
    MockMvc mvc;

    @MockBean
    LoanService service;

    @MockBean
    BookService bookService;

    @Test
    @DisplayName("Must create a loan with success")
    public void mustCreateLoanTest() throws Exception {

        //given
        LoanDTO dto = LoanDTO.builder()
                .isbn("123")
                .customer("Someone")
                .email("customer@email.com")
                .build();

        String json = new ObjectMapper().writeValueAsString(dto);

        Book book = Book.builder().id(1L).isbn("123").build();

        BDDMockito
                .given(bookService.getBookByIsbn("123"))
                .willReturn(Optional.of( book ));

        Loan loan = Loan.builder().id(1L).customer("Someone").book(book).loanDate(LocalDateTime.now()).build();

        BDDMockito
                .given(service.save(Mockito.any(Loan.class)))
                .willReturn(loan);

        //when
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(LOAN_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        //then
        mvc.perform( request )
                .andExpect( status().isCreated() )
                .andExpect( content().string("1") );

    }

    @Test
    @DisplayName("Must return an error when to try to make a Loan of an existent book")
    public void mustNotCreateLoanTest() throws Exception {

        //given
        LoanDTO dto = LoanDTO.builder()
                .isbn("123")
                .customer("Someone")
                .build();

        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito
                .given(bookService.getBookByIsbn("123"))
                .willReturn(Optional.empty());

        //when
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(LOAN_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        //then
        mvc.perform( request )
                .andExpect( status().isBadRequest() )
                .andExpect( jsonPath("errors", hasSize(1)))
                .andExpect( jsonPath("errors[0]").value("Book not found for passed isbn"));

    }

    @Test
    @DisplayName("Must return an error when to try to make a Loan of a loaned book")
    public void mustNotSaveALoanOfAnAlredyloanedBookTest() throws Exception {

        //given
        LoanDTO dto = LoanDTO.builder()
                .isbn("123")
                .customer("Someone")
                .build();

        String json = new ObjectMapper().writeValueAsString(dto);

        Book book = Book.builder().id(1L).isbn("123").build();

        BDDMockito
                .given(bookService.getBookByIsbn("123"))
                .willReturn(Optional.of( book ));

        BDDMockito
                .given(service.save(Mockito.any(Loan.class)))
                .willThrow(new BusinessException("Book alredy loaned"));

        //when
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(LOAN_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        //then
        mvc.perform( request )
                .andExpect( status().isBadRequest() )
                .andExpect( jsonPath("errors", hasSize(1)))
                .andExpect( jsonPath("errors[0]").value("Book alredy loaned"));

    }

    @Test
    @DisplayName("Must return a book")
    public void mustReturnABookTest() throws Exception {
        //given
        ReturnedLoanDTO dto = ReturnedLoanDTO.builder().returned(true).build();

        String json = new ObjectMapper().writeValueAsString(dto);

        Loan loan = Loan.builder().id(1L).build();

        BDDMockito
                .given(service.getById(Mockito.anyLong()))
                .willReturn(Optional.of(loan));


        //when
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .patch(LOAN_API.concat("/1"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        //then
        mvc.perform( request )
                .andExpect( status().isOk() );

        Mockito.verify(service, Mockito.times(1)).update(loan);
    }

    @Test
    @DisplayName("Must return 404 when to try to return a inexistent book")
    public void mustReturnInexistentBookTest() throws Exception {
        //given
        ReturnedLoanDTO dto = ReturnedLoanDTO.builder().returned(true).build();

        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito
                .given(service.getById(Mockito.anyLong()))
                .willReturn(Optional.empty());


        //when
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .patch(LOAN_API.concat("/1"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        //then
        mvc.perform( request )
                .andExpect( status().isNotFound() );

    }

    @Test
    @DisplayName("Must filter loans")
    public void mustFindLoansTest() throws Exception {
        //given
        Long id = 1L;
        Loan loan = LoanServiceTest.createLoan();
        loan.setId(id);
        Book book = Book.builder().id(1L).isbn("123").build();
        loan.setBook(book);

        BDDMockito
                .given( service.find(any(LoanFilterDTO.class), any(Pageable.class)) )
                .willReturn( new PageImpl<>( Arrays.asList(loan), PageRequest.of(0, 10), 1 ));

        String queryString = String.format("?isbn=%s&customer=%s&page=0&size=10", book.getId(), loan.getCustomer());

        //when
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(LOAN_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        //then
        mvc.perform( request )
                .andExpect( status().isOk() )
                .andExpect( jsonPath("content", hasSize(1)))
                .andExpect( jsonPath("totalElements").value(1) )
                .andExpect( jsonPath("pageable.pageSize").value(10) )
                .andExpect( jsonPath("pageable.pageNumber").value(0) );
    }

}
