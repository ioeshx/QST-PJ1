package com.demo.controller;

import com.demo.entity.Message;
import com.demo.service.MessageService;
import com.demo.service.MessageVoService;
import com.demo.service.NewsService;
import com.demo.service.VenueService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IndexControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NewsService newsService;
    @MockBean
    private VenueService venueService;
    @MockBean
    private MessageVoService messageVoService;
    @MockBean
    private MessageService messageService;

    /**
     * 测试访问index方法访问
     * 当没有任何新闻、场馆、消息时
     */
    @Test
    void indexTestWhenNoData() throws Exception {
        when(newsService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(new ArrayList<>()) );
        when(venueService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(new ArrayList<>()) );
        when(messageService.findPassState(any(Pageable.class))).thenReturn(new PageImpl<>(new ArrayList<>()) );
        when(messageVoService.returnVo(any(List.class))).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("user", nullValue()))
                .andExpect(model().attribute("news_list", hasSize(0)))
                .andExpect(model().attribute("venue_list", hasSize(0)))
                .andExpect(model().attribute("message_list", hasSize(0)))
                .andReturn();

        verify(newsService, times(1)).findAll(any(Pageable.class));
        verify(venueService, times(1)).findAll(any(Pageable.class));
        verify(messageService, times(1)).findPassState(any(Pageable.class));
        verify(messageVoService, times(1)).returnVo(any(List.class));
    }

    /**
     * 测试访问index方法
     * 当有新闻、场馆、消息时
     */
    @Test
    void indexTestWhenData() throws Exception {
        when(newsService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(new ArrayList<>()) );
        when(venueService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(new ArrayList<>()) );
        when(messageService.findPassState(any(Pageable.class)))
                .thenReturn(new PageImpl<>(new ArrayList<>()) );
        when(messageVoService.returnVo(any(List.class)))
                .thenReturn(new ArrayList<>());

        mockMvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("user", nullValue()))
                .andExpect(model().attribute("news_list", hasSize(0)))
                .andExpect(model().attribute("venue_list", hasSize(0)))
                .andExpect(model().attribute("message_list", hasSize(0)))
                .andReturn();

        verify(newsService, times(1)).findAll(any(Pageable.class));
        verify(venueService, times(1)).findAll(any(Pageable.class));
        verify(messageService, times(1)).findPassState(any(Pageable.class));
        verify(messageVoService, times(1)).returnVo(any(List.class));

    }

    /**
     * 测试访问adminIndex方法
     */
    @Test
    void adminIndexTest() throws Exception {
        mockMvc.perform(get("/admin_index"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/admin_index"));

    }
}
