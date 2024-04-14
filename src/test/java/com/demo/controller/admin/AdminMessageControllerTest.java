package com.demo.controller.admin;


import com.demo.entity.Message;
import com.demo.entity.vo.MessageVo;
import com.demo.service.MessageService;
import com.demo.service.MessageVoService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
@SpringBootTest
@AutoConfigureMockMvc
public class AdminMessageControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private MessageService messageService;
    @MockBean
    private MessageVoService messageVoService;

    private List<Message> mockMessageList(int size){
        List<Message> messages = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            messages.add(new Message(i, "User " + i, "Content " + i, LocalDateTime.now(), 1));
        }
        return messages;
    }

    private List<MessageVo> mockMessageVoList(int size) {
        List<MessageVo> messageVos = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            messageVos.add(new MessageVo(
                    i,                       // messageID
                    "user" + i,              // userID
                    "This is message " + i,  // content
                    LocalDateTime.now(),     // time
                    "UserName" + i,          // userName
                    "picture" + i + ".png",  // picture
                    i % 2                    // state (alternating for variety)
            ));
        }
        return messageVos;
    }

    /**
     * 测试 /message_manage 的GET请求
     */
    @Test
    public void testMessageManage() throws Exception {
        PageImpl<Message> page = new PageImpl<>(mockMessageList(10));
        when(messageService.findWaitState(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/message_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/message_manage"))
                .andExpect(model().attribute("total", page.getTotalPages()))
                .andDo(print());

        verify(messageService).findWaitState(any(Pageable.class));
    }
    /**
     * 测试 /messageList.do 的GET请求
     * 当没有返回信息时
     */
    @Test
    public void testMessageNoList() throws Exception {
        List<Message> messages = mockMessageList(0);
        List<MessageVo> messageVos = mockMessageVoList(0);
        when(messageService.findWaitState(any(Pageable.class))).thenReturn(new PageImpl<>(messages));
        PageImpl<Message> page = new PageImpl<>(messages);
        when(messageService.findWaitState(any(Pageable.class)))
                .thenReturn(page);
        when(messageVoService.returnVo(messages))
                .thenReturn(messageVos);
        mockMvc.perform(get("/messageList.do").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)))
                .andDo(print());

        verify(messageService).findWaitState(any(Pageable.class));
        verify(messageVoService).returnVo(anyList());
    }
    /**
     * 测试 /messageList.do 的GET请求
     * 当有返回信息时：
     */
    @Test
    public void testMessageList() throws Exception {
        List<Message> messages = mockMessageList(10);
        List<MessageVo> messageVos = mockMessageVoList(10);
        when(messageService.findWaitState(any(Pageable.class))).thenReturn(new PageImpl<>(messages));
        PageImpl<Message> page = new PageImpl<>(messages);
        when(messageService.findWaitState(any(Pageable.class)))
                .thenReturn(page);
        when(messageVoService.returnVo(messages))
                .thenReturn(messageVos);
        mockMvc.perform(get("/messageList.do").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)))
                .andDo(print());

        verify(messageService).findWaitState(any(Pageable.class));
        verify(messageVoService).returnVo(anyList());
    }
    /**
     * 管理员通过消息
     * 消息id不存在
     */
    @Test
    public void test_passMessage_WhenMessageIDNotExistThenReturn404() throws Exception {
        int messageId = 9999;

        doThrow(new RuntimeException("消息不存在"))
                .when(messageService).confirmMessage(messageId);

        mockMvc.perform(post("/passMessage.do").param("messageID", String.valueOf(messageId)))
                .andExpect(status().isNotFound());

        verify(messageService, times(1)).confirmMessage(messageId);
    }

    /**
     * 管理员通过消息
     * 消息id存在
     */
    @Test
    public void test_passMessage_WhenMessageIDExistThenReturn200() throws Exception {
        int messageId = 1;

        doNothing()
                .when(messageService).confirmMessage(eq(messageId));

        mockMvc.perform(post("/passMessage.do").param("messageID", String.valueOf(messageId)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(messageService, times(1)).confirmMessage(messageId);
    }

    /**
     * 管理员拒绝消息-0
     * 消息id不存在
     * @see AdminMessageController#rejectMessage(int)
     */
    @Test
    public void test_rejectMessage_WhenMessageIDNotExistThenReturn404() throws Exception {
        int messageId = 9999;

        doThrow(new RuntimeException("消息不存在"))
                .when(messageService).rejectMessage(messageId);

        mockMvc.perform(post("/rejectMessage.do").param("messageID", String.valueOf(messageId)))
                .andExpect(status().isNotFound());

        verify(messageService).rejectMessage(messageId);
    }

    /**
     * 管理员拒绝消息-1
     * 消息id存在
     */
    @Test
    public void test_rejectMessage_WhenMessageIDExistThenReturn200() throws Exception {
        int messageId = 1;

        doNothing()
                .when(messageService).rejectMessage(eq(messageId));

        mockMvc.perform(post("/rejectMessage.do").param("messageID", String.valueOf(messageId)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(messageService, times(1)).rejectMessage(messageId);
    }

    /**
     * 管理员删除消息-0
     * 消息id不存在
     */
    /**
     * 管理员删除消息-0
     * 消息id不存在
     * @see AdminMessageController#delMessage(int)
     */
    @Test
    public void test_delMessage_WhenMessageIDNotExistThenReturn404() throws Exception {
        int messageId = 9999;

        doThrow(new RuntimeException("消息不存在"))
                .when(messageService).delById(messageId);

        mockMvc.perform(post("/delMessage.do").param("messageID", String.valueOf(messageId)))
                .andExpect(status().isNotFound());

        verify(messageService).delById(messageId);
    }

    /**
     * 管理员删除消息-1
     * 消息id存在
     */
    @Test
    public void test_delMessage_WhenMessageIDExistThenReturn200() throws Exception {
        int messageId = 1;

        doNothing()
                .when(messageService).delById(eq(messageId));

        mockMvc.perform(post("/delMessage.do").param("messageID", String.valueOf(messageId)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(messageService, times(1)).delById(messageId);
    }

}
