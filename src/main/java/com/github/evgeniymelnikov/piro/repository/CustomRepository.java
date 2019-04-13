package com.github.evgeniymelnikov.piro.repository;

import com.github.evgeniymelnikov.piro.model.Widget;
import com.github.evgeniymelnikov.piro.model.filter.AbstractFilter;
import com.github.evgeniymelnikov.piro.model.filter.Pageable;

import java.util.List;
import java.util.Optional;

public interface CustomRepository<T extends Widget, K extends AbstractFilter> {
    
    Optional<T> findById(String id);
    boolean removeByIds(List<String> id);
    boolean save(T entity);
    List<T> findAll(K filter);
    List<T> findAll(K filter, Pageable pageable);
}
