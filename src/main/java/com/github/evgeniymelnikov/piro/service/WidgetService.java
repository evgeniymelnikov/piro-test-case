package com.github.evgeniymelnikov.piro.service;

import com.github.evgeniymelnikov.piro.exception.ResourceIllegalArgumentException;
import com.github.evgeniymelnikov.piro.exception.ResourceNotFoundException;
import com.github.evgeniymelnikov.piro.model.Widget;
import com.github.evgeniymelnikov.piro.model.validator.WidgetValidator;
import com.github.evgeniymelnikov.piro.repository.WidgetRepositoryImpl;
import com.github.evgeniymelnikov.piro.store.WidgetStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.DataBinder;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WidgetService {

    private final WidgetStore widgetStore;
    private final WidgetRepositoryImpl widgetRepository;
    private final WidgetValidator widgetValidator;

    public Widget addWidget(Widget widget) {
        if (widget == null) {
            throw new ResourceIllegalArgumentException("ОШИБКА: для сохранения передан пустой виджет");
        }

        widget.setId(UUID.randomUUID().toString());
        widget.setLastUpdate(LocalDate.now());
        validate(widget);

        // синхронизируeм - поскольку потребуется поменять z-index у некоторых элементов в хранилище,
        // на это время надо исключить доступ иных потоков к ресурсу
        synchronized (widgetStore.getStore()) {

            if (widget.getIndexZ() == null) {
                Optional<Widget> widgetWithMaxIndexZ = widgetStore.getStore().stream()
                        .max(Comparator.comparing(Widget::getIndexZ));
                if (widgetWithMaxIndexZ.isPresent()) {
                    widget.setIndexZ(widgetWithMaxIndexZ.get().getIndexZ() + 1L);
                } else {
                    widget.setIndexZ(0L);
                }
            } else {
                boolean isChosenIndexZExisted = widgetStore.getStore().stream().anyMatch(storedWidget ->
                        storedWidget.getIndexZ().equals(widget.getIndexZ()));
                if (isChosenIndexZExisted) {
                    widgetStore.getStore().forEach(storedWidget -> {
                        if (storedWidget.getIndexZ() >= widget.getIndexZ()) {
                            storedWidget.setIndexZ(storedWidget.getIndexZ() + 1L);
                        }
                    });
                }
            }

            widgetRepository.save(widget);
        }

        // возможно, что сохранялся один z-index, а в ответе будет уже изменившийся
        // (из-за выполнения данного метода другими потоками).
        // Если необходимо избежать этого, то в synchronize блоке надо создавать копию объекта и отдавать пользователю её.
        return widget;
    }

    public Widget update(Widget widget) {
        if (widget == null) {
            throw new ResourceIllegalArgumentException("ОШИБКА: для обновления передан пустой виджет");
        }

        widget.setLastUpdate(LocalDate.now());
        validateBeforeUpdate(widget);
        Widget existedWidget = widgetRepository.findById(widget.getId()).orElseThrow(
                () -> new ResourceNotFoundException(String.format("ОШИБКА: при обновлении не найден виджет с id %s", widget.getId())));

        existedWidget.updateWidget(widget);
        return widget;
    }

    public void removeByIds(List<String> ids){
        if (CollectionUtils.isEmpty(ids)) {
            throw new ResourceIllegalArgumentException("ОШИБКА: для удаления виджетов требуется передать их id");
        }

        // без проверки на exist
        widgetRepository.removeByIds(ids);
    }

    private void validate(Widget widget) {
        DataBinder dataBinder = new DataBinder(widget);
        dataBinder.addValidators(widgetValidator);
        dataBinder.validate();

        if (dataBinder.getBindingResult().hasErrors()) {
            String errorMessages = dataBinder.getBindingResult().getAllErrors().stream()
                    .map(el -> el.getCode()).collect(Collectors.joining(", "));
            throw new ResourceIllegalArgumentException(String.format("ОШИБКА: %s", errorMessages));
        }
    }

    private void validateBeforeUpdate(Widget widget) {
        validate(widget);
        if (widget.getIndexZ() == null) {
            throw new ResourceIllegalArgumentException(String.format("ОШИБКА: у виджета с id %s при обновлении не определён z-index", widget.getId()));
        }
    }
}
