package com.github.evgeniymelnikov.piro.service;

import com.github.evgeniymelnikov.piro.exception.ResourceIllegalArgumentException;
import com.github.evgeniymelnikov.piro.model.Widget;
import com.github.evgeniymelnikov.piro.repository.WidgetRepositoryImpl;
import com.github.evgeniymelnikov.piro.store.WidgetStore;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WidgetServiceTest {

    @Autowired
    private WidgetService widgetService;

    @Autowired
    private WidgetStore widgetStore;

    @Autowired
    private WidgetRepositoryImpl widgetRepository;

    @Test
    public void addWidget() {
        widgetStore.getStore().clear();
        Widget widget = new Widget(null,
                0L, 0L, 10L, 10L,
                null, LocalDate.now());

        Widget savedWidget = widgetService.addWidget(widget);
        Assert.assertEquals(widget, savedWidget);

        Optional<Widget> byId = widgetRepository.findById(savedWidget.getId());
        Widget foundWidget = byId.get();
        Assert.assertNotNull(foundWidget);

        Assert.assertEquals(foundWidget, savedWidget);
        Assert.assertEquals(savedWidget.getId(), foundWidget.getId());
        Assert.assertEquals((Long) 0L, foundWidget.getPositionX());
        Assert.assertEquals((Long) 0L, foundWidget.getPositionY());
        Assert.assertEquals((Long) 10L, foundWidget.getWidth());
        Assert.assertEquals((Long) 10L, foundWidget.getHeight());
        Assert.assertEquals((Long) 0L, foundWidget.getIndexZ());
        Assert.assertEquals(LocalDate.now(), foundWidget.getLastUpdate());
    }

    @Test
    public void addWidgetInConcurrencyWithNullIndexZ() {
        widgetStore.getStore().clear();
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            Widget widget = new Widget(null,
                    0L, 0L, 10L, 10L,
                    null, LocalDate.now());
            futures.add(executorService.submit(() -> widgetService.addWidget(widget)));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        Assert.assertEquals(20, widgetStore.getStore().size());
        Assert.assertEquals(20, new HashSet<>(widgetStore.getStore().stream().map(Widget::getIndexZ)
                        .collect(Collectors.toList())).size());
    }

    @Test
    public void addWidgetCheckThatUserCannotSetCustomId() {

        String randomUUID = UUID.randomUUID().toString();

        Widget widget = new Widget(randomUUID,
                0L, 0L, 10L, 10L,
                null, LocalDate.now());

        Widget savedWidget = widgetService.addWidget(widget);

        Assert.assertNotEquals(randomUUID, savedWidget.getId());

        Optional<Widget> byId = widgetRepository.findById(randomUUID);
        Assert.assertFalse(byId.isPresent());
    }

    @Test
    public void validateWidget() {
        Widget widget = new Widget(UUID.randomUUID().toString(),
                0L, 0L, 10L, 10L,
                0L, LocalDate.now());

        ReflectionTestUtils.invokeMethod(widgetService, "validate", widget);
    }

    @Test(expected = ResourceIllegalArgumentException.class)
    public void validateWidgetWithoutIdFailTest() {
        Widget widget = new Widget(null,
                0L, 0L, 10L, 10L,
                0L, LocalDate.now());

        try {ReflectionTestUtils.invokeMethod(widgetService, "validate", widget);}
        catch (ResourceIllegalArgumentException ex) {
            MatcherAssert.assertThat(ex.getMessage(), Matchers.containsString("id должен быть определён"));
            throw ex;
        }
    }

    @Test(expected = ResourceIllegalArgumentException.class)
    public void validateWidgetWithoutPositionsFailTest() {
        Widget widget = new Widget(UUID.randomUUID().toString(),
                null, null, 10L, 10L,
                0L, LocalDate.now());

        try {ReflectionTestUtils.invokeMethod(widgetService, "validate", widget);}
        catch (ResourceIllegalArgumentException ex) {
            MatcherAssert.assertThat(ex.getMessage(), Matchers.containsString("координата по оси X должна быть определена, координата по оси Y должна быть определена"));
            throw ex;
        }
    }

    @Test(expected = ResourceIllegalArgumentException.class)
    public void validateWidgetWithoutWidthAndHeightFailTest() {
        Widget widget = new Widget(UUID.randomUUID().toString(),
                10L, 10L, null, null,
                0L, LocalDate.now());

        try {ReflectionTestUtils.invokeMethod(widgetService, "validate", widget);}
        catch (ResourceIllegalArgumentException ex) {
            MatcherAssert.assertThat(ex.getMessage(), Matchers.containsString("высота должна быть определена, ширина должна быть определена"));
            throw ex;
        }
    }

    @Test(expected = ResourceIllegalArgumentException.class)
    public void validateWidgetWithoutLastUpdateFailTest() {
        Widget widget = new Widget(UUID.randomUUID().toString(),
                10L, 10L, 8L, 8L,
                0L, null);

        try {ReflectionTestUtils.invokeMethod(widgetService, "validate", widget);}
        catch (ResourceIllegalArgumentException ex) {
            MatcherAssert.assertThat(ex.getMessage(), Matchers.containsString("дата модификации должна быть определена"));
            throw ex;
        }
    }

    @Test
    public void update() {
        Widget widget = new Widget(null,
                0L, 0L, 10L, 10L,
                null, null);
        String id = widgetService.addWidget(widget).getId();

        // поменяем дату последних изменений (ссылка на объект, который хранится в store, следовательно изменения отразятся и на нём)
        widget.setLastUpdate(LocalDate.ofYearDay(2018, 20));
        Widget afterChangedDate = widgetRepository.findById(id).get();
        Assert.assertEquals(LocalDate.ofYearDay(2018, 20), afterChangedDate.getLastUpdate());

        Widget updatedWidget = new Widget(id, 0L, 0L, 10L, 10L,
                10L, LocalDate.now());
        widgetService.update(updatedWidget);
        Widget afterUpdate = widgetRepository.findById(id).get();
        Assert.assertEquals((Long) 0L, afterUpdate.getPositionX());
        Assert.assertEquals((Long) 0L, afterUpdate.getPositionY());
        Assert.assertEquals((Long) 10L, afterUpdate.getWidth());
        Assert.assertEquals((Long) 10L, afterUpdate.getHeight());
        Assert.assertEquals((Long) 10L, afterUpdate.getIndexZ());

        Assert.assertEquals(LocalDate.now(), afterUpdate.getLastUpdate());
    }

    @Test(expected = ResourceIllegalArgumentException.class)
    public void updateWithoutIndexZFailTest() {
        Widget widget = new Widget(null,
                0L, 0L, 10L, 10L,
                null, null);
        String id = widgetService.addWidget(widget).getId();

        Widget updatedWidget = new Widget(id, 0L, 0L, 10L, 10L,
                null, LocalDate.now());
        try {widgetService.update(updatedWidget);}
        catch (ResourceIllegalArgumentException ex) {
            MatcherAssert.assertThat(ex.getMessage(), Matchers.containsString(
                    String.format("у виджета с id %s при обновлении не определён z-index", id)));
            throw ex;
        }
    }


    @Test
    public void updateWithConcurrency() {
        Widget widget = new Widget(null,
                0L, 0L, 10L, 10L,
                null, null);
        String id = widgetService.addWidget(widget).getId();

        ExecutorService executorService = Executors.newFixedThreadPool(8);
        List<Future<?>> futures = new ArrayList<>();

        List<Long> availableValues = new ArrayList<>();
        for (int i = 1; i < 11; i++) {
            availableValues.add(10L*i);
        }

        for (int i = 1; i < 11; i++) {

            Widget updatedWidget = new Widget(id, 10L*i, 10L*i, 10L, 10L,
                    10L, null);
            futures.add(executorService.submit(() -> widgetService.update(updatedWidget)));
            futures.add(executorService.submit(() ->
                    {
                        Widget widgetInRaceCondition = widgetRepository.findById(id).get();
                        MatcherAssert.assertThat(widgetInRaceCondition.getId(), Matchers.equalTo(id));
                        MatcherAssert.assertThat(widgetInRaceCondition.getPositionX(), Matchers.isIn(availableValues));
                        MatcherAssert.assertThat(widgetInRaceCondition.getPositionY(), Matchers.isIn(availableValues));
                        MatcherAssert.assertThat(widgetInRaceCondition.getWidth(), Matchers.equalTo(10L));
                        MatcherAssert.assertThat(widgetInRaceCondition.getHeight(), Matchers.equalTo(10L));
                        MatcherAssert.assertThat(widgetInRaceCondition.getLastUpdate(), Matchers.equalTo(LocalDate.now()));
                    }));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        Widget widgetInRaceCondition = widgetRepository.findById(id).get();
        MatcherAssert.assertThat(widgetInRaceCondition.getId(), Matchers.equalTo(id));
        MatcherAssert.assertThat(widgetInRaceCondition.getPositionX(), Matchers.isIn(availableValues));
        MatcherAssert.assertThat(widgetInRaceCondition.getPositionY(), Matchers.isIn(availableValues));
        MatcherAssert.assertThat(widgetInRaceCondition.getWidth(), Matchers.equalTo(10L));
        MatcherAssert.assertThat(widgetInRaceCondition.getHeight(), Matchers.equalTo(10L));
        MatcherAssert.assertThat(widgetInRaceCondition.getLastUpdate(), Matchers.equalTo(LocalDate.now()));
    }

    @Test
    public void removeByIds() {
    }

    @Test(expected = ResourceIllegalArgumentException.class)
    public void removeByIdsEmptyCollectionFailTest() {
        widgetService.removeByIds(new ArrayList<>());
    }

    @Test
    public void removeByIdsNotExistedId() {
        // тут возможны вариации, исходя из бизнес логики
        widgetService.removeByIds(Arrays.asList("1", "2"));
    }
}