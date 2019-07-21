package com.github.evgeniymelnikov.piro.controller;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ExceptionControllerTest {

    private final String controllerPath = "/api/v1/common/widget";

    @Autowired
    private MockMvc mockMvc;

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