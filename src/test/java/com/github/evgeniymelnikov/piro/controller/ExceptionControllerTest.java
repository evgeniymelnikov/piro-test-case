package com.github.evgeniymelnikov.piro.controller;

import com.github.evgeniymelnikov.piro.exception.ResourceIllegalArgumentException;
import com.github.evgeniymelnikov.piro.model.Widget;
import com.github.evgeniymelnikov.piro.service.WidgetService;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.ThrowsException;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@RunWith(MockitoJUnitRunner.class)
public class ExceptionControllerTest {

    private final String controllerPath = "/api/v1/common/widget";

    @InjectMocks
    private WidgetController widgetController;
    private MockMvc mockMvc;


    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(widgetController).setControllerAdvice(new ExceptionController()).build();
    }

    @Test
    public void handleResourceIllegalArgumentException() throws Exception {
        mockMvc.perform(post(controllerPath + "/add")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"sddsf\":\"dsfsdf\"}"))
                .andDo(print())
                .andExpect(jsonPath("$.status", Matchers.is(400)));
    }

    @Test
    public void handleResourceIllegalArgumentException_findAll() throws Exception {
        mockMvc.perform(post(controllerPath)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"direction\":\"dsfsdf\"}"))
                .andDo(print())
                .andExpect(jsonPath("$.status", Matchers.is(400)));
    }
}