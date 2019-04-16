package com.github.evgeniymelnikov.piro.model.filter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.evgeniymelnikov.piro.exception.ResourceIllegalArgumentException;
import com.github.evgeniymelnikov.piro.model.Widget;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.function.Predicate;

@Getter
@Setter
public abstract class AbstractFilter<T extends Widget> {

    protected Long page;
    protected Long limit;
    protected String sort;
    protected SortDirection direction;

    @JsonIgnore
    protected String idFieldName;

    public AbstractFilter(Long page, Long limit, String sort, String direction) {

        if (page != null && page < 0) {
            throw new ResourceIllegalArgumentException("ОШИБКА: номер страницы меньше нуля");
        }

        if (limit != null && limit < 0) {
            throw new ResourceIllegalArgumentException("ОШИБКА: количество элементов на странице меньше нуля");
        }

        if (limit != null && limit > 500) {
            throw new ResourceIllegalArgumentException("ОШИБКА: количество элементов на странице не должно превышать 500");
        }

        this.page = page == null ? 0 : page;
        this.limit = limit == null ? 10 : limit;

        if (!StringUtils.hasText(sort) || "null".equals(sort) || "undefined".equals(sort)) {
            this.sort = getDefaultSort();
        } else {
            this.sort = sort;
        }

        if (StringUtils.hasText(direction)) {
            try {
                this.direction = SortDirection.valueOf(direction.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new ResourceIllegalArgumentException("ОШИБКА: направление сортировки может быть либо ASC, либо DESC");
            }
        } else {
            this.direction = SortDirection.ASC;
        }
    }

    /**
     * Позволяет производить сортировку по выбранному полю (логика по определению sort в конструкторе) и затем по id.
     * Контракт следующий: последний элемент имеет наибольший приоритет.
     * Метод может быть переопределён в реализациях AbstractFilter'а.
     * Метод вызывается при создании объекта Pageable {@link AbstractFilter#toPageable()}
     * @return массив полей, используемых для сортировки
     */
    protected String[] getSortFields() {
        return new String[]{getIdFieldName(), sort};
    }

    protected abstract String getDefaultSort();
    protected abstract String[] getFieldsFromMetamodel();

    protected abstract String getIdFieldName();

    public abstract Predicate<T> toPredicate();

    protected void checkSortFieldsExists() {
       if (!Arrays.asList(getFieldsFromMetamodel()).containsAll(Arrays.asList(getSortFields()))) {
           throw new ResourceIllegalArgumentException("ОШИБКА: для сортировки указано недопустимое поле");
        }
    }

    public Pageable toPageable() {
        checkSortFieldsExists();
        return new Pageable(page, limit, direction, getSortFields());
    }
}
