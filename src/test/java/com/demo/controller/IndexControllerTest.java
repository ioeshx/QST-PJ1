package com.demo.controller;

import com.demo.entity.Message;
import com.demo.entity.News;
import com.demo.entity.Venue;
import com.demo.entity.vo.MessageVo;
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

import java.time.LocalDateTime;
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

    private List<Message> mockMessageList(int size){
        List<Message> messageList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Message message = new Message(i, "user_test "+i, "test message "+i, LocalDateTime.now(), 2);
            messageList.add(message);
        }
        return messageList;
    }

    private List<News> mockNewsList(int size){
        List<News> newsList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            News news = new News(i, "title "+i, "content "+i, LocalDateTime.now());
            newsList.add(news);
        }
        return newsList;
    }

    private List<Venue> mockVenueList(int size){
        List<Venue> venueList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Venue venue = new Venue(i, "venue "+i, "description "+i, i*100, "image "+i, "address "+i, "08:00", "22:00");
            venueList.add(venue);
        }
        return venueList;
    }

    private List<MessageVo> mockMessageVoList(int size){
        List<MessageVo> messageVoList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            MessageVo messageVo = new MessageVo(i, "user_test "+i, "content "+i, LocalDateTime.now(), "user_name_"+i, "image "+i, 2);
            messageVoList.add(messageVo);
        }
        return messageVoList;
    }


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
        int size = 3;
        when(newsService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(mockNewsList(size)) );
        when(venueService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(mockVenueList(size)) );
        when(messageService.findPassState(any(Pageable.class)))
                .thenReturn(new PageImpl<>(mockMessageList(size)) );
        when(messageVoService.returnVo(any(List.class)))
                .thenReturn(mockMessageVoList(size));


        mockMvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("user", nullValue()))
                .andExpect(model().attribute("news_list", hasSize(size)))
                .andExpect(model().attribute("venue_list", hasSize(size)))
                .andExpect(model().attribute("message_list", hasSize(size)))
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
