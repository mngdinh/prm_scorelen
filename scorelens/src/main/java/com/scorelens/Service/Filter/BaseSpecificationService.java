package com.scorelens.Service.Filter;

import com.scorelens.DTOs.Request.PageableRequestDto;
import com.scorelens.DTOs.Response.PageableResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class BaseSpecificationService<T, R> {

    protected abstract JpaSpecificationExecutor<T> getRepository();

    protected abstract Function<T, R> getMapper();

    /**
     * Mỗi service con sẽ implement logic filter riêng.
     */
    protected abstract Specification<T> buildSpecification(Map<String, Object> filters);

    public PageableResponseDto<R> getAll(PageableRequestDto req, Map<String, Object> filters) {

        Specification<T> spec = buildSpecification(filters);

        Sort sort = Sort.by(req.getSortDirection().equalsIgnoreCase("desc") ?
                        Sort.Direction.DESC : Sort.Direction.ASC,
                req.getSortBy());

        Pageable pageable = PageRequest.of(req.getPage() - 1, req.getSize(), sort);

        Page<T> page = getRepository().findAll(spec, pageable);

        List<R> responses = page.getContent()
                .stream()
                .map(getMapper())
                .toList();

        return PageableResponseDto.<R>builder()
                .content(responses)
                .currentPage(page.getNumber() + 1)
                .pageSizes(page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}
