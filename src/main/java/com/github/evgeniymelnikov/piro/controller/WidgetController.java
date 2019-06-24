package com.github.evgeniymelnikov.piro.controller;

import com.github.evgeniymelnikov.piro.exception.ResourceIllegalArgumentException;
import com.github.evgeniymelnikov.piro.model.Widget;
import com.github.evgeniymelnikov.piro.model.filter.WidgetFilter;
import com.github.evgeniymelnikov.piro.repository.WidgetRepositoryImpl;
import com.github.evgeniymelnikov.piro.service.WidgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/common/widget")
@RequiredArgsConstructor
public class WidgetController {

    private final WidgetService widgetService;
    private final WidgetRepositoryImpl widgetRepository;

    @PostMapping("/add")
    public Widget addWidget(@RequestBody Widget widget) {
        System.out.println(widget);
        return widgetService.addWidget(widget);
    }

    @PutMapping("/{id}/widget")
    public Widget updateWidget(@PathVariable String id, @RequestBody Widget widget) {
        if (!id.equals(widget.getId())) {
            throw new ResourceIllegalArgumentException("ОШИБКА: id виджета из URL не совпадает с id в теле запроса");
        }
        return widgetService.update(widget);
    }

    @DeleteMapping("/delete")
    public void deleteWidget(@RequestBody List<String> ids) {
        widgetService.removeByIds(ids);
    }

    @PostMapping
    public List<Widget> findAll(@RequestBody WidgetFilter widgetFilter) {
        return widgetRepository.findAll(widgetFilter, widgetFilter.toPageable());
    }
}
