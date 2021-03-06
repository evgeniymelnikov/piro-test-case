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
        widgetStore.getStore().clear();
        String newContent = LoadFileUtils.readFile(filePath + "/add_widget_50_50.json");
        String oneMoreContent = LoadFileUtils.readFile(filePath + "/add_widget_100_100.json");
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        List<Future<?>> futures = new ArrayList<>();

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
    public void addWidget_withNullInIndexZ() throws Exception {
        widgetStore.getStore().clear();
        addWidget();
        addWidget();
        addWidget();
        String responseBody = mockMvc.perform(post(controllerPath + "/add")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(LoadFileUtils.readFile(filePath + "/add_widget_null_in_index_z.json")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(responseBody).path("id").asText();
        Assert.assertEquals((Long) 3L, widgetRepository.findById(id).get().getIndexZ());
    }

    @Test
    public void addWidget_notCorrect() throws Exception {
        addNotCorrect("widget_without_positionX.json");
        addNotCorrect("widget_without_positionY.json");
        addNotCorrect("widget_without_height.json");
        addNotCorrect("widget_without_width.json");
    }


    @Test
    public void updateWidget() throws Exception {
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

    /**
     * проверяем на то, что не получим расщеплённое состояние
     */
    @Test
    public void updateWidget_withConcurrency() throws Exception {
        widgetStore.getStore().clear();
        String newContent = LoadFileUtils.readFile(filePath + "/add_widget_50_50.json");
        String responseBody = mockMvc.perform(post(controllerPath + "/add")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(newContent))
//                .andDo(print())
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        String addedWidgetId = objectMapper.readTree(responseBody).path("id").asText();

        final String updateContent = LoadFileUtils.readFile(filePath + "/update_widget_with_concurrency.json");
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 1; i < 26; i++) {

            final int iFin = i;

            futures.add(executorService.submit(() -> {
                final int iTemp = iFin;

                String preparedContent = updateContent.replace("!%%%!", addedWidgetId)
                        .replace("1n", String.valueOf(10 * iTemp))
                        .replace("2n", String.valueOf(10 * iTemp));

                try {
                    System.out.println(mockMvc.perform(put(controllerPath + String.format("/%s/widget", addedWidgetId))
                            .accept(MediaType.APPLICATION_JSON_UTF8)
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .content(preparedContent))
//                            .andDo(print())
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("id", Matchers.notNullValue()))
                            .andExpect(jsonPath("indexZ", Matchers.is(0)))
                            .andExpect(jsonPath("height", Matchers.is(0)))
                            .andExpect(jsonPath("width", Matchers.is(0)))
                            .andExpect(jsonPath("positionX", Matchers.is(10 * iTemp)))
                            .andExpect(jsonPath("positionY", Matchers.is(10 * iTemp)))
                            .andExpect(jsonPath("lastUpdate", Matchers.is(LocalDate.now().toString())))
                            .andReturn().getResponse().getContentAsString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));

            futures.add(executorService.submit(() -> {
                final double iTemp = iFin * 1.5D;

                String preparedContent = updateContent.replace("!%%%!", addedWidgetId)
                        .replace("1n", String.valueOf(10 * iTemp)).replace("2n", String.valueOf(10 * iTemp));

                try {
                    System.out.println(mockMvc.perform(put(controllerPath + String.format("/%s/widget", addedWidgetId))
                            .accept(MediaType.APPLICATION_JSON_UTF8)
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .content(preparedContent))
//                            .andDo(print())
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("id", Matchers.notNullValue()))
                            .andExpect(jsonPath("indexZ", Matchers.is(0)))
                            .andExpect(jsonPath("height", Matchers.is(0)))
                            .andExpect(jsonPath("width", Matchers.is(0)))
                            .andExpect(jsonPath("positionX", Matchers.is((int) (10 * iTemp))))
                            .andExpect(jsonPath("positionY", Matchers.is((int) (10 * iTemp))))
                            .andExpect(jsonPath("lastUpdate", Matchers.is(LocalDate.now().toString())))
                            .andReturn().getResponse().getContentAsString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
        }

        for (Future<?> future : futures) {
            future.get();
        }

        executorService.shutdown();

        widgetStore.getStore().forEach(System.out::println);
    }


    @Test
    public void updateWidget_notCorrect() throws Exception {
        String newContent = LoadFileUtils.readFile(filePath + "/add_widget_50_50.json");

        String responseBody = mockMvc.perform(post(controllerPath + "/add")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(newContent))
//                .andDo(print())
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        String addedWidgetId = objectMapper.readTree(responseBody).path("id").asText();

        updateNotCorrect("widget_without_positionX.json", addedWidgetId);
        updateNotCorrect("widget_without_positionY.json", addedWidgetId);
        updateNotCorrect("widget_without_height.json", addedWidgetId);
        updateNotCorrect("widget_without_width.json", addedWidgetId);
        updateNotCorrect("widget_without_index_z_only_for_upd.json", addedWidgetId);

        //корректный контент, но несовпадающий id в url и в json
        mockMvc.perform(put(controllerPath + String.format("/%s/widget", UUID.randomUUID().toString()))
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(LoadFileUtils.readFile(filePath + "/update_widget.json")))
                .andDo(print())
                .andExpect(status().isBadRequest());

        //корректный контент, но несуществующий id
        String notExistedId = UUID.randomUUID().toString();
        String content = LoadFileUtils.readFile(filePath + "/update_widget.json").replace("!%%%!", notExistedId);
        mockMvc.perform(put(controllerPath + String.format("/%s/widget", notExistedId))
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(content))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deleteWidget() throws Exception {
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
    public void findAll_notCorrect() throws Exception {
        widgetStore.getStore().clear();
        for (int i = 0; i < 25; i++) {
            addWidget();
        }

        mockMvc.perform(post(controllerPath)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"limit\":600}"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        mockMvc.perform(post(controllerPath)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"limit\":-4}"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        mockMvc.perform(post(controllerPath)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"page\":-4}"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        mockMvc.perform(post(controllerPath)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"sort\": \"ldsfsdfsd\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void findAll_defaultLimitPageSort() throws Exception {
        widgetStore.getStore().clear();
        for (int i = 0; i < 25; i++) {
            addWidget();
        }

        mockMvc.perform(post(controllerPath)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(10)))
                .andExpect(jsonPath("$[0].indexZ", Matchers.is(0)))
                .andExpect(jsonPath("$[9].indexZ", Matchers.is(9)));
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

    private void addNotCorrect(String fileName) throws Exception {
        String newContent = LoadFileUtils.readFile(filePath + String.format("/not_correct/%s", fileName));
        mockMvc.perform(post(controllerPath + "/add")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(newContent))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    private void updateNotCorrect(String fileName, String id) throws Exception {
        String newContent = LoadFileUtils.readFile(filePath + String.format("/not_correct/%s", fileName));
        String preparedContent = newContent.substring(0,1) + String.format("\"id\":\"%s\", ", id) + newContent.substring(1);
        mockMvc.perform(put(controllerPath + String.format("/%s/widget", id))
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(preparedContent))
                .andDo(print())
                .andExpect(status().isBadRequest());

    }
}