package com.github.evgeniymelnikov.piro.repository;

import com.github.evgeniymelnikov.piro.exception.ResourceIllegalArgumentException;
import com.github.evgeniymelnikov.piro.model.Widget;
import com.github.evgeniymelnikov.piro.model.filter.Pageable;
import com.github.evgeniymelnikov.piro.model.filter.WidgetFilter;
import com.github.evgeniymelnikov.piro.store.WidgetStore;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Кастомный, без спринг импелементаций (связано с недопустимостью использования БД)
 */
@Service
@RequiredArgsConstructor
public class WidgetRepositoryImpl implements CustomRepository<Widget, WidgetFilter> {

    private final WidgetStore widgetStore;

    @Override
    public Optional<Widget> findById(String id) {
        Assert.notNull(id, "для поиска передан null");
        return widgetStore.getStore().stream().filter(widget -> widget.getId().equals(id)).findFirst();
    }

    @Override
    public boolean removeByIds(List<String> ids) {
        Assert.notNull(ids, "для удаления передан null");
        return widgetStore.getStore().removeIf(widget -> ids.contains(widget.getId()));
    }

    @Override
    public boolean save(Widget widget) {
        Assert.notNull(widget, "для сохранения передан null");
        return widgetStore.getStore().add(widget);
    }

    /**
     * @param filter {@link WidgetFilter}
     * @return получение всех элементов без пагинации
     */
    @Override
    public List<Widget> findAll(WidgetFilter filter) {
        return widgetStore.getStore().stream().filter(filter.toPredicate()).collect(Collectors.toList());
    }

    /**
     * В фильтре осуществляется чейнинг предикатов, поэтому позхволяет применять множество условий для фильтрации.
     * Позволяет сортировать по нескольким полям.
     * @return получение элементов с пагинацией
     */
    @Override
    public List<Widget> findAll(WidgetFilter filter, Pageable pageable) {
        Stream<Widget> widgetStreamAfterFilter = widgetStore.getStore().stream().filter(filter.toPredicate());

        for (String sortFieldName : pageable.getSort()) {

            if (!StringUtils.hasText(sortFieldName)) {
                throw new ResourceIllegalArgumentException("ОШИБКА: для сортировки передано пустое поле");
            }

            PropertyDescriptor propertyDescriptor = BeanUtils.getPropertyDescriptor(Widget.class, sortFieldName);

            if (propertyDescriptor == null) {
                throw new ResourceIllegalArgumentException("ОШИБКА: виджет не содержит переданное для сортировки поле");
            }

            Method readMethod = propertyDescriptor.getReadMethod();

            if (readMethod == null) {
                throw new ResourceIllegalArgumentException(String.format("ОШИБКА: отсутствует метод чтения поля %s, необходимый для сортировки", sortFieldName));
            }

            Class<? extends Comparable> propertyType = (Class<? extends Comparable>) propertyDescriptor.getPropertyType();
            widgetStreamAfterFilter = widgetStreamAfterFilter.sorted((o1, o2) -> {
                try {
                    return propertyType.cast(readMethod.invoke(o1)).compareTo(propertyType.cast(readMethod.invoke(o2)));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                    throw new RuntimeException("внутренняя ошибка при сортирвоке");
                }
            });
        }

        return widgetStreamAfterFilter.skip(pageable.getLimit() * pageable.getPage()).limit(pageable.getLimit())
                .collect(Collectors.toList());
    }
}
