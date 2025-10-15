package com.scorelens.DTOs.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageableResponseDto<T> {
    List<T> content; // List of items
    int currentPage; // Current page number
    int pageSizes; // Page size
    long totalItems; // Total number of elements
    int totalPages; // Total number of pages
    boolean empty; // Is empty page
    String sortBy; // Sort field
    String sortDirection; // Sort direction
}
