package com.scorelens.Service.Filter;


import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class SpecificationBuilder<T> {
    private final List<Specification<T>> specs = new ArrayList<>();

    public SpecificationBuilder<T> with(Specification<T> spec) {
        if (spec != null) specs.add(spec);
        return this;
    }

    public Specification<T> build() {
        return specs.stream()
                .reduce(Specification::and)
                .orElse(null);
    }
}

