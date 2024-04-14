package com.demo.controller.user;

import com.demo.entity.Message;
import com.demo.entity.News;
import com.demo.entity.User;
import com.demo.entity.vo.MessageVo;
import com.demo.exception.LoginException;
import com.demo.service.MessageService;
import com.demo.service.MessageVoService;
import com.demo.service.NewsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class NewsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NewsService newsService;

    private List<News> mockNewsList(int size) {
        List<News> newsList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            News news = new News();
            news.setTitle("News Title " + i);
            news.setContent("Content " + i);
            news.setTime(LocalDateTime.now().minusDays(i));
            newsList.add(news);
        }
        return newsList;
    }

    /**
     * 用户查看新闻详情成功
     */
    @Test
    public void testNewsDetail() throws Exception {
        News news = new News(1,"News Title 1","Content 1",LocalDateTime.now());

        when(newsService.findById(1)).thenReturn(news);

        mockMvc.perform(get("/news").param("newsID", "1"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("news", hasProperty("title", is("News Title 1"))))
                .andExpect(model().attribute("news", hasProperty("content", is("Content 1"))))
                .andExpect(view().name("news"))
                .andDo(print());

        verify(newsService).findById(1);
    }
    /**
     * 用户查看新闻详情失败
     * id不存在
     */
    @Test
    public void testNewsDetail_NewsIdNotFound() throws Exception {
        int nonExistentId = 999;  // Use an ID that you know does not exist
        when(newsService.findById(nonExistentId)).thenReturn(null);

        mockMvc.perform(get("/news").param("newsID", String.valueOf(nonExistentId)))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("news"))
                .andExpect(view().name("news"))  // Assuming you still return the "news" view even if no news is found
                .andDo(print());

        verify(newsService).findById(nonExistentId);
    }

    /**
     * 用户查询新闻列表成功
     */
    @Test
    public void testNewsListPaged() throws Exception {
        List<News> newsList = mockNewsList(5);
        Pageable pageable = PageRequest.of(0, 5, Sort.by("time").descending());
        Page<News> page = new PageImpl<>(newsList, pageable, newsList.size());

        when(newsService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/news/getNewsList").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.content[0].title", is("News Title 0")))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andDo(print());

        verify(newsService).findAll(any(Pageable.class));
    }
    /**
     * 用户查看新闻列表成功
     */
    @Test
    public void testFullNewsList() throws Exception {
        List<News> newsList = mockNewsList(5);
        Pageable pageable = PageRequest.of(0, 5, Sort.by("time").descending());
        Page<News> page = new PageImpl<>(newsList, pageable, 5);

        when(newsService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/news_list"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("news_list", hasSize(5)))
                .andExpect(model().attribute("total", is(1)))
                .andExpect(view().name("news_list"))
                .andDo(print());

        verify(newsService, times(2)).findAll(any(Pageable.class)); // Called twice in the same method
    }
}
