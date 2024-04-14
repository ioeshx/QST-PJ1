package com.demo.controller.admin;

import com.demo.entity.News;
import com.demo.service.NewsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminNewsControllerTest {
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
     * 测试 /news_manage 的GET请求
     */
    @Test
    public void testNewsManage() throws Exception {
        PageImpl<News> page = new PageImpl<>(mockNewsList(10));
        when(newsService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/news_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/news_manage"))
                .andExpect(model().attribute("total", page.getTotalPages()))
                .andDo(print());

        verify(newsService).findAll(any(Pageable.class));
    }

    /**
     * 测试 /news_add 的GET请求
     */
    @Test
    public void testNewsAdd() throws Exception {
        mockMvc.perform(get("/news_add"))
                .andExpect(status().isOk())
                .andExpect(view().name("/admin/news_add"));
    }

    /**
     * 测试 /news_edit 的GET请求
     */
    @Test
    public void testNewsEdit() throws Exception {
        int newsID = 1;
        News news = new News();
        news.setTitle("Test News");
        news.setContent("Test content");
        news.setTime(LocalDateTime.now());

        when(newsService.findById(newsID)).thenReturn(news);

        mockMvc.perform(get("/news_edit").param("newsID", String.valueOf(newsID)))
                .andExpect(status().isOk())
                .andExpect(view().name("/admin/news_edit"))
                .andExpect(model().attribute("news", news))
                .andDo(print());

        verify(newsService).findById(newsID);
    }
    /**
     * 测试 /news_edit 的GET请求失败
     * id未知
     */
    @Test
    public void testEditNews_WhenNewsIDNotExistThenReturn404() throws Exception {
        int newsID = 9999;

        // Assume the news does not exist
        when(newsService.findById(newsID)).thenReturn(null);

        mockMvc.perform(get("/news_edit").param("newsID", String.valueOf(newsID)))
                .andExpect(status().isNotFound())  // Expecting a 404 Not Found response for a non-existent news item
                .andDo(print());

        verify(newsService).findById(newsID);  // Verify the service was queried for the correct ID
    }

    /**
     * 测试 /newsList.do 的GET请求
     */
    @Test
    public void testNewsList() throws Exception {
        List<News> news = mockNewsList(10);
        PageImpl<News> page = new PageImpl<>(news);
        when(newsService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/newsList.do").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)))
                .andDo(print());

        verify(newsService).findAll(any(Pageable.class));
    }

    /**
     * 测试 /delNews.do 的POST请求
     */
    @Test
    public void testDelNews() throws Exception {
        int newsID = 1;

        doNothing().when(newsService).delById(newsID);

        mockMvc.perform(post("/delNews.do").param("newsID", String.valueOf(newsID)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(newsService).delById(newsID);
    }
    /**
     * 测试 /delNews.do 的POST请求失败
     * id未知
     */
    @Test
    public void testDelNews_WhenNewsIDNotExistThenReturn404() throws Exception {
        int newsID = 9999;

        // Setup the scenario where the ID does not exist
        doThrow(new RuntimeException("News does not exist")).when(newsService).delById(newsID);

        mockMvc.perform(post("/delNews.do").param("newsID", String.valueOf(newsID)))
                .andExpect(status().isNotFound())  // We expect a 404 Not Found response if the news does not exist
                .andDo(print());

        verify(newsService).delById(newsID);  // Ensure the method was called with the correct ID
    }

    /**
     * 测试 /modifyNews.do 的POST请求
     */
    @Test
    public void testModifyNews() throws Exception {
        int newsID = 1;
        String title = "Updated Title";
        String content = "Updated Content";

        News news = new News();
        news.setTitle(title);
        news.setContent(content);
        news.setTime(LocalDateTime.now());

        when(newsService.findById(newsID)).thenReturn(news);
        doNothing().when(newsService).update(any(News.class));

        mockMvc.perform(post("/modifyNews.do")
                        .param("newsID", String.valueOf(newsID))
                        .param("title", title)
                        .param("content", content))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("news_manage"));

        verify(newsService).findById(newsID);
        verify(newsService).update(news);
    }
    /**
     * 测试 /modifyNews.do 的POST请求失败
     * 字段为空
     */
    @Test
    public void testModifyNews_FailureDueToIncompleteData() throws Exception {
        int newsID = 1;

        News existingNews = new News(newsID, "Old Title", "Old content", LocalDateTime.now());
        when(newsService.findById(newsID)).thenReturn(existingNews);

        mockMvc.perform(post("/modifyNews.do")
                        .param("newsID", String.valueOf(newsID))
                        .param("title", "")  // Missing title
                        .param("content", "Updated Content"))
                .andExpect(status().isBadRequest());  // Expecting a bad request status due to validation failure

        verify(newsService, never()).update(any(News.class));  // Ensuring update is not called due to failure in validation
    }


    /**
     * 测试 /addNews.do 的POST请求
     */
    @Test
    public void testAddNews() throws Exception {
        String title = "New Title";
        String content = "New content";
        when(newsService.create(any(News.class))).thenReturn(anyInt());

        mockMvc.perform(post("/addNews.do")
                        .param("title", title)
                        .param("content", content))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("news_manage"));

        verify(newsService).create(any(News.class)); // 验证create方法被正确调用
    }

    /**
     * 测试 /addNews.do 的POST请求失败
     * 有字段为空
     */
    @Test
    public void testAddNews_FailureDueToIncompleteData() throws Exception {
        mockMvc.perform(post("/addNews.do")
                        .param("title", "")  // Missing title
                        .param("content", "Some content"))
                .andExpect(status().isBadRequest());  // Expecting a bad request status due to validation failure

        verify(newsService, never()).create(any(News.class));  // No create operation should be called
    }

}
