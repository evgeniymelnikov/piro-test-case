package com.github.evgeniymelnikov.piro.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.evgeniymelnikov.piro.model.Widget;
import com.github.evgeniymelnikov.piro.repository.WidgetRepositoryImpl;
import com.github.evgeniymelnikov.piro.store.WidgetStore;
import com.github.evgeniymelnikov.piro.utils.LoadFileUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class WidgetControllerTest {

    private final String controllerPath = "/api/v1/common/widget";
    private final String filePath = "controller";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WidgetStore widgetStore;

    @Autowired
    private WidgetRepositoryImpl widgetRepository;

    @Test
    public void addWidget() throws Exception {
        String newContent = LoadFileUtils.readFile(filePath + "/add_widget_50_50.json");
        String responseBody = mockMvc.perform(post(controllerPath + "/add")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(newContent))
//                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", Matchers.notNullValue()))
                .andExpect(jsonPath("indexZ", Matchers.is(0)))
                .andExpect(jsonPath("height", Matchers.is(100)))
                .andExpect(jsonPath("width", Matchers.is(100)))
                .andExpect(jsonPath("positionX", Matchers.is(50)))
                .andExpect(jsonPath("positionY", Matchers.is(50)))
                .andExpect(jsonPath("lastUpdate", Matchers.is(LocalDate.now().toString())))
                .andReturn().getResponse().getContentAsString();

        String addedWidgetId = objectMapper.readTree(responseBody).path("id").asText();

        Optional<Widget> widgetOptional = widgetRepository.findById(addedWidgetId);
        Assert.assertTrue(widgetOptional.isPresent());
        Widget widget = widgetOptional.get();
        Assert.assertEquals((Long) 0L, widget.getIndexZ());
        Assert.assertEquals((Long) 100L, widget.getHeight());
        Assert.assertEquals((Long) 100L, widget.getWidth());
        Assert.assertEquals((Long) 50L, widget.getPositionX());
        Assert.assertEquals((Long) 50L, widget.getPositionY());
        Assert.assertEquals(LocalDate.now(), widget.getLastUpdate());
    }


    @Test
    public void addWidget_withConcurrency() throws Exception {

        String newContent = LoadFileUtils.readFile(filePath + "/add_widget_50_50.json");
        String oneMoreContent = LoadFileUtils.readFile(filePath + "/add_widget_100_100.json");
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        List<Future<?>> futures = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < 20; i++) {
            //z-index = 0
            futures.add(executorService.submit(() -> {
                try {
                    mockMvc.perform(post(controllerPath + "/add")
                            .accept(MediaType.APPLICATION_JSON_UTF8)
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .content(newContent))
//                .andDo(print())
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("id", Matchers.notNullValue()))
                            .andExpect(jsonPath("height", Matchers.is(100)))
                            .andExpect(jsonPath("width", Matchers.is(100)))
                            .andExpect(jsonPath("positionX", Matchers.is(50)))
                            .andExpect(jsonPath("positionY", Matchers.is(50)));

                    /** не факт, что то значение z-index'а, который мы передавали, нам возвратится
                     * другой поток после освобождения монитора на хранилище, до того, как сформируется json ответ
                     * и он будет направлен пользователю, уже может успеть накатить свои изменения
                     * см. {@link com.github.evgeniymelnikov.piro.service.WidgetService#addWidget(Widget)} и комментарий в нём
                     **/

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));

            //z-index = 1
            futures.add(executorService.submit(() -> {
                try {
                    mockMvc.perform(post(controllerPath + "/add")
                            .accept(MediaType.APPLICATION_JSON_UTF8)
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .content(oneMoreContent))
//                        .andDo(print())
                            .andExpect(status().isOk());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
        }

        for (Future<?> future : futures) {
            future.get();
        }
        executorService.shutdown();

        HashSet<Long> hashSet = new HashSet<>();
        widgetStore.getStore().forEach(widget -> hashSet.add(widget.getIndexZ()));
        // по бизнес-логике z-index у виджетов не может повторяться
        System.out.println(hashSet);
        Assert.assertEquals(40, hashSet.size());
    }


    @Test
    public void updateWidget() throws Exception {
        String newContent = LoadFileUtils.readFile(filePath + "/add_widget_50_50.json");
        String responseBody = mockMvc.perform(post(controllerPath + "/add")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(newContent))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", Matchers.notNullValue()))
                .andExpect(jsonPath("indexZ", Matchers.is(0)))
                .andExpect(jsonPath("height", Matchers.is(100)))
                .andExpect(jsonPath("width", Matchers.is(100)))
                .andExpect(jsonPath("positionX", Matchers.is(50)))
                .andExpect(jsonPath("positionY", Matchers.is(50)))
                .andExpect(jsonPath("lastUpdate", Matchers.is(LocalDate.now().toString())))
                .andReturn().getResponse().getContentAsString();

        String addedWidgetId = objectMapper.readTree(responseBody).path("id").asText();
        String updateContent = LoadFileUtils.readFile(filePath + "/update_widget.json");

        updateContent = updateContent.replace("!%%%!", addedWidgetId);
        mockMvc.perform(put(controllerPath + String.format("/%s/widget", addedWidgetId))
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(updateContent))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", Matchers.notNullValue()))
                .andExpect(jsonPath("indexZ", Matchers.is(0)))
                .andExpect(jsonPath("height", Matchers.is(0)))
                .andExpect(jsonPath("width", Matchers.is(0)))
                .andExpect(jsonPath("positionX", Matchers.is(0)))
                .andExpect(jsonPath("positionY", Matchers.is(0)))
                .andExpect(jsonPath("lastUpdate", Matchers.is(LocalDate.now().toString())));

        Optional<Widget> widgetOptional = widgetRepository.findById(addedWidgetId);
        Assert.assertTrue(widgetOptional.isPresent());
        Widget widget = widgetOptional.get();
        Assert.assertEquals((Long) 0L, widget.getIndexZ());
        Assert.assertEquals((Long) 0L, widget.getHeight());
        Assert.assertEquals((Long) 0L, widget.getWidth());
        Assert.assertEquals((Long) 0L, widget.getPositionX());
        Assert.assertEquals((Long) 0L, widget.getPositionY());
        Assert.assertEquals(LocalDate.now(), widget.getLastUpdate());

    }

    @Test
    public void removeWidget() throws Exception {
        String newContent = LoadFileUtils.readFile(filePath + "/add_widget_50_50.json");
        String responseBody = mockMvc.perform(post(controllerPath + "/add")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(newContent))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", Matchers.notNullValue()))
                .andExpect(jsonPath("indexZ", Matchers.is(0)))
                .andExpect(jsonPath("height", Matchers.is(100)))
                .andExpect(jsonPath("width", Matchers.is(100)))
                .andExpect(jsonPath("positionX", Matchers.is(50)))
                .andExpect(jsonPath("positionY", Matchers.is(50)))
                .andExpect(jsonPath("lastUpdate", Matchers.is(LocalDate.now().toString())))
                .andReturn().getResponse().getContentAsString();

        String addedWidgetId = objectMapper.readTree(responseBody).path("id").asText();

        Optional<Widget> widgetOptional = widgetRepository.findById(addedWidgetId);
        Assert.assertTrue(widgetOptional.isPresent());

        mockMvc.perform(delete(controllerPath + "/delete")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(String.format("[\"%s\"]}", addedWidgetId)))
                .andDo(print())
                .andExpect(status().isOk());

        Optional<Widget> widgetOptionalAfterDelete = widgetRepository.findById(addedWidgetId);
        Assert.assertFalse(widgetOptionalAfterDelete.isPresent());
    }

    @Test
    public void findAll() throws Exception {
        widgetStore.getStore().clear();
        for (int i = 0; i < 25; i++) {
            addWidget();
        }

        mockMvc.perform(post(controllerPath)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"limit\":100}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(25)));
    }

    @Test
    public void findAll_filterByPoints() throws Exception {
        widgetStore.getStore().clear();
        mockMvc.perform(post(controllerPath + "/add")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(LoadFileUtils.readFile(filePath + "/add_widget_50_50.json")));

        mockMvc.perform(post(controllerPath + "/add")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(LoadFileUtils.readFile(filePath + "/add_widget_50_100.json")));

        mockMvc.perform(post(controllerPath + "/add")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(LoadFileUtils.readFile(filePath + "/add_widget_100_100.json")));

        mockMvc.perform(post(controllerPath)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(LoadFileUtils.readFile(filePath + "/find_all_filter_rectangle.json")))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(2)));
    }
}