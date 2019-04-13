package com.github.evgeniymelnikov.piro.model.filter;

import lombok.Data;

@Data
public class Pageable {
    private Long page;
    private Long limit;
    private SortDirection direction;
    private String[] sort;

    public Pageable(Long page, Long limit, SortDirection direction, String[] sort) {
        this.page = page;
        this.limit = limit;
        this.direction = direction;
        this.sort = sort;
    }
}
