package com.studies.libraryapi.api.dto;

import lombok.*;

import javax.validation.constraints.NotEmpty;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {

    private Long id;

    @NotEmpty
    private String title;

    @NotEmpty
    private String author;

    @NotEmpty
    private String isbn;

}
