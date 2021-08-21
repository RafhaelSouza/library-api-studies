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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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

        Book savedBook = createABook();

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

        String json = new ObjectMapper().writeValueAsString(createABook());

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

    @Test
    @DisplayName("Must get book details")
    public void mustGetDetailsBookTest() throws Exception {

        //given
        Long id = 1L;

        Book book = Book.builder()
                .id(id)
                .title(createABook().getTitle())
                .author(createABook().getAuthor())
                .isbn(createABook().getIsbn())
                .build();

        BDDMockito
                .given(bookService.getById(id))
                .willReturn(Optional.of(book));

        //when
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API + "/" + id)
                .accept(MediaType.APPLICATION_JSON);

        //then
        mvc.perform( request )
                .andExpect( status().isOk() )
                .andExpect( jsonPath("id").value(id) )
                .andExpect( jsonPath("title").value(createABook().getTitle()) )
                .andExpect( jsonPath("author").value(createABook().getAuthor()) )
                .andExpect( jsonPath("isbn").value(createABook().getIsbn()) );


    }

    @Test
    @DisplayName("Must return resource not found when a book does not exists")
    public void mustNotFoundAbookTest() throws Exception {

        // given
        BDDMockito
                .given( bookService.getById(anyLong()) )
                .willReturn( Optional.empty() );

        // when
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API + "/" + anyLong())
                .accept(MediaType.APPLICATION_JSON);

        // then
        mvc.perform( request )
                .andExpect( status().isNotFound() );

    }

    @Test
    @DisplayName("Must update a book")
    public void mustUpdateABookTest() throws Exception {

        //given
        Long id = 1L;

        String json = new ObjectMapper().writeValueAsString( createABook() );

        Book updatingBook = Book.builder()
                .id(1L)
                .title("Some Title")
                .author("Some Author")
                .isbn("654")
                .build();

        BDDMockito
                .given(bookService.getById(id))
                .willReturn(Optional.of( updatingBook ));

        Book updatedBook = Book.builder()
                .id(1L)
                .title("Title Book")
                .author("Author Book")
                .isbn("654")
                .build();

        BDDMockito
                .given(bookService.update(updatingBook))
                .willReturn(updatedBook);

        //when
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API + "/" + id)
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        //then
        mvc.perform( request )
                .andExpect( status().isOk() )
                .andExpect( jsonPath("id").value(id) )
                .andExpect( jsonPath("title").value(updatedBook.getTitle()) )
                .andExpect( jsonPath("author").value(updatedBook.getAuthor()) )
                .andExpect( jsonPath("isbn").value("654") );

    }

    @Test
    @DisplayName("Must return resource not found when to try to update an inexistent book")
    public void mustNotUpdateAInexistentBookTest() throws Exception {

        //given
        String json = new ObjectMapper().writeValueAsString( createABook() );

        BDDMockito
                .given(bookService.getById(anyLong()))
                .willReturn(Optional.empty());

        //when
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API + "/" + anyLong())
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        //then
        mvc.perform( request )
                .andExpect( status().isNotFound() );

    }

    @Test
    @DisplayName("Must delete a book")
    public void mustDeleteABookTest() throws Exception {

        // given
        BDDMockito
                .given( bookService.getById(anyLong()) )
                .willReturn( Optional.of( Book.builder().id(1L).build() ) );

        // when
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API + "/" + 1);

        // then
        mvc.perform( request )
                .andExpect( status().isNoContent() );

    }

    @Test
    @DisplayName("Must return resource not found when to try to delete an inexistent book")
    public void mustNotDeleteAInexistentBookTest() throws Exception {

        // given
        BDDMockito
                .given( bookService.getById(anyLong()) )
                .willReturn( Optional.empty() );

        // when
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API + "/" + anyLong());

        // then
        mvc.perform( request )
                .andExpect( status().isNotFound() );

    }

    @Test
    @DisplayName("Must filter books")
    public void mustFindBooksTest() throws Exception {
        //given
        Long id = 1L;
        Book book = Book.builder()
                .id(id)
                .title(createABook().getTitle())
                .author(createABook().getAuthor())
                .isbn(createABook().getIsbn())
                .build();

        BDDMockito
                .given( bookService.find(any(Book.class), any(Pageable.class)) )
                .willReturn( new PageImpl<Book>( Arrays.asList(book), PageRequest.of(0, 100), 1 ) );

        String queryString = String.format("?title=%s&author=%s&page=0&size=100", book.getTitle(), book.getAuthor());

        //when
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        //then
        mvc.perform( request )
                .andExpect( status().isOk() )
                .andExpect( jsonPath("content", hasSize(1)))
                .andExpect( jsonPath("totalElements").value(1) )
                .andExpect( jsonPath("pageable.pageSize").value(100) )
                .andExpect( jsonPath("pageable.pageNumber").value(0) );
    }

    private Book createABook() {
        return Book.builder()
                .id(1L)
                .title("Book Title")
                .author("Book Author")
                .isbn("123456")
                .build();
    }

}
