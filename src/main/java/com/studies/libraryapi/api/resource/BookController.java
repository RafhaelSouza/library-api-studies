package com.studies.libraryapi.api.resource;

import com.studies.libraryapi.api.dto.BookDTO;
import com.studies.libraryapi.api.dto.LoanDTO;
import com.studies.libraryapi.model.entity.Book;
import com.studies.libraryapi.model.entity.Loan;
import com.studies.libraryapi.service.BookService;
import com.studies.libraryapi.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService service;

    private final ModelMapper modelMapper;

    private final LoanService loanService;

    @GetMapping("{id}")
    public BookDTO get( @PathVariable Long id ) {
        return service
                .getById(id)
                .map(book -> modelMapper.map(book, BookDTO.class))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public Page<BookDTO> find(BookDTO dto, Pageable pageRequest) {
        Book filter = modelMapper.map(dto, Book.class);
        Page<Book> result = service.find(filter, pageRequest);
        List<BookDTO> list = result.getContent()
                .stream()
                .map(entity -> modelMapper.map(entity, BookDTO.class))
                .collect(Collectors.toList());

        return new PageImpl<>( list, pageRequest, result.getTotalElements() );
    }

    public Page<LoanDTO> loansByBook( @PathVariable Long id, Pageable pageable ) {
        Book book = service.getById(id)
                .orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND) );
        Page<Loan> result = loanService.getLoansByBook(book, pageable);
        List<LoanDTO> list = result.getContent()
                .stream()
                .map(loan -> {
                    Book loanBook = loan.getBook();
                    BookDTO bookDTO = modelMapper.map(loanBook, BookDTO.class);
                    LoanDTO loanDTO = modelMapper.map(loan, LoanDTO.class);
                    loanDTO.setBook(bookDTO);
                    return loanDTO;
                }).collect(Collectors.toList());
        return new PageImpl<>( list, pageable, result.getTotalElements());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookDTO create( @RequestBody @Valid BookDTO dto ) {
        Book book = modelMapper.map(dto, Book.class);

        book = service.save(book);

        return modelMapper.map(book, BookDTO.class);
    }

    @PutMapping("{id}")
    public BookDTO update( @PathVariable Long id, BookDTO dto ) {
        return service.getById(id)
                .map(book -> {
                            book.setTitle(dto.getTitle());
                            book.setAuthor(dto.getAuthor());
                            book = service.update(book);
                            return modelMapper.map(book, BookDTO.class);
                        }
                ).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete( @PathVariable Long id ) {
        Book book = service.getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        service.delete(book);
    }

}
