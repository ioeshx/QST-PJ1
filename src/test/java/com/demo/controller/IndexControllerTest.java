package com.demo.controller;

import com.demo.entity.Message;
import com.demo.entity.News;
import com.demo.entity.Venue;
import com.demo.entity.vo.MessageVo;
import com.demo.service.MessageService;
import com.demo.service.MessageVoService;
import com.demo.service.NewsService;
import com.demo.service.VenueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
     * 使用语句覆盖测试index函数。对底层service进行mock提供空的消息，场地信息和新闻
     * @see IndexController#index(Model)
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
        // 语句覆盖
        verify(newsService, times(1)).findAll(any(Pageable.class));
        verify(venueService, times(1)).findAll(any(Pageable.class));
        verify(messageService, times(1)).findPassState(any(Pageable.class));
        verify(messageVoService, times(1)).returnVo(any(List.class));
    }

    /**
     *  使用语句覆盖测试index函数。对底层service进行mock提供非空的消息，场地信息和新闻
     * @see IndexController#index(Model) 
     */
    @Test
    void indexTestWhenData() throws Exception {
        int size = 3;
        List<News> mockNewsList = mockNewsList(size);
        List<Venue> mockVenueList = mockVenueList(size);
        List<Message> mockMessageList = mockMessageList(size);
        List<MessageVo> mockMessageVoList = mockMessageVoList(size);

        when(newsService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(mockNewsList) );
        when(venueService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(mockVenueList) );
        when(messageService.findPassState(any(Pageable.class)))
                .thenReturn(new PageImpl<>(mockMessageList) );
        when(messageVoService.returnVo(any(List.class)))
                .thenReturn(mockMessageVoList);

        MvcResult mvcResult = mockMvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("user", nullValue()))
                .andExpect(model().attribute("news_list", hasSize(size)))
                .andExpect(model().attribute("venue_list", hasSize(size)))
                .andExpect(model().attribute("message_list", hasSize(size)))
                .andReturn();
        // 测试排列顺序
        ModelAndView modelAndView = mvcResult.getModelAndView();
        List<News> returnedNewsList = (List<News>) modelAndView.getModel().get("news_list");
        List<Venue> returnedVenueList = (List<Venue>) modelAndView.getModel().get("venue_list");
        List<MessageVo> returnedMessageList = (List<MessageVo>) modelAndView.getModel().get("message_list");
        mockVenueList.sort(Comparator.comparing(Venue::getVenueID));
        mockNewsList.sort(Comparator.comparing(News::getTime).reversed());
        mockMessageList.sort(Comparator.comparing(Message::getTime).reversed());

        assertEquals(mockNewsList, returnedNewsList);
        assertEquals(mockVenueList, returnedVenueList);
        assertEquals(mockMessageVoList, returnedMessageList);

        // 语句覆盖
        verify(newsService, times(1)).findAll(any(Pageable.class));
        verify(venueService, times(1)).findAll(any(Pageable.class));
        verify(messageService, times(1)).findPassState(any(Pageable.class));
        verify(messageVoService, times(1)).returnVo(any(List.class));
    }

    /**
     * 使用语句覆盖测试admin_index函数
     * @see IndexController#admin_index(Model)
     */
    @Test
    void adminIndexTest() throws Exception {
        mockMvc.perform(get("/admin_index"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/admin_index"));

    }
}
