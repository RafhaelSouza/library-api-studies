package com.studies.libraryapi.api.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studies.libraryapi.api.dto.BookDTO;
import com.studies.libraryapi.exception.BusinessException;
import com.studies.libraryapi.model.entity.Book;
import com.studies.libraryapi.service.BookService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({SpringExtension.class})
@ActiveProfiles("test")
@WebMvcTest
@AutoConfigureMockMvc
public class BookControllerTest {

    static String BOOK_API = "/api/books";

    @Autowired
    MockMvc mvc;

    @MockBean
    BookService bookService;

    @Test
    @DisplayName("Must create a book with success")
    public void createBookTest() throws Exception {

        BookDTO book = BookDTO.builder()
                .title("Book Title")
                .author("Book Author")
                .isbn("123456")
                .build();

        Book savedBook = getASavedBook();

        BDDMockito
                .given(bookService.save(Mockito.any(Book.class)))
                .willReturn(savedBook);

        String json = new ObjectMapper().writeValueAsString(book);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform( request )
                .andExpect( status().isCreated() )
                .andExpect( jsonPath("id").isNotEmpty() )
                .andExpect( jsonPath("title").value("Book Title") )
                .andExpect( jsonPath("author").value("Book Author") )
                .andExpect( jsonPath("isbn").value("123456") );

    }

    @Test
    @DisplayName("Must throw an error when there is no enough data to create a book")
    public void createInvalidBookTest() throws Exception {

        String json = new ObjectMapper().writeValueAsString(new BookDTO());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform( request )
                .andExpect( status().isBadRequest() )
                .andExpect( jsonPath("errors", hasSize(3)));

    }

    @Test
    @DisplayName("Must throw an error when there is a duplicate isbn book")
    public void createBookWithDuplicateISBNTest() throws Exception {

        String json = new ObjectMapper().writeValueAsString(getASavedBook());

        String errorMessage = "ISBN already created";

        BDDMockito
                .given(bookService.save(Mockito.any(Book.class)))
                .willThrow(new BusinessException(errorMessage));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform( request )
                .andExpect( status().isBadRequest() )
                .andExpect( jsonPath("errors", hasSize(1)))
                .andExpect( jsonPath("errors[0]").value(errorMessage));

    }

    private Book getASavedBook() {
        return Book.builder()
                .title("Book Title")
                .author("Book Author")
                .isbn("123456")
                .build();
    }

}
