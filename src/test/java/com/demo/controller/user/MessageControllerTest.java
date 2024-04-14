package com.demo.controller.user;

import com.demo.entity.Message;
import com.demo.entity.User;
import com.demo.entity.vo.MessageVo;
import com.demo.exception.LoginException;
import com.demo.service.MessageService;
import com.demo.service.MessageVoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.hasSize;
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
public class MessageControllerTest {
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
     * 用户获取消息列表成功
     * 用户已登录
     */
    @Test
    public void testMessageList_WithUserLoggedIn() throws Exception {
        // Prepare pageable and message list
        Pageable pageable = PageRequest.of(0, 5, Sort.by("time").descending());
        List<Message> messageList = Collections.singletonList(new Message(1, "user1", "Content here", LocalDateTime.now(), 1));
        Page<Message> page = new PageImpl<>(messageList, pageable, messageList.size());

        // Mocking the service method to return a non-null Page object
        when(messageService.findPassState(pageable)).thenReturn(page);
        when(messageService.findByUser(anyString(), any(Pageable.class))).thenReturn(page);  // Mock this too if it's used similarly

        // Setting up a mock user session
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/message_list")
                .sessionAttr("user",    new User(1,"1","1","1","1","1",0,"1"));  // Assuming User class has a constructor User(String userID, String username)

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("total"))
                .andExpect(model().attributeExists("user_total"))
                .andExpect(view().name("message_list"));

        verify(messageService).findPassState(any(Pageable.class));
        verify(messageVoService).returnVo(anyList());
        verify(messageService).findByUser(eq("1"), any(Pageable.class));
    }
    /**
     * 用户获取消息列表失败
     * 用户未登录
     */
    @Test
    public void testMessageList_WithUserNotLoggedIn() throws Exception {
        mockMvc.perform(get("/message_list"))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof LoginException))
                .andExpect(result -> assertEquals("请登录！", result.getResolvedException().getMessage()));
    }
    /**
     * 用户查看消息列表
     */
    @Test
    public void testGetMessageList_ValidRequest() throws Exception {
        int size = 5;
        Pageable pageable = PageRequest.of(0, 5, Sort.by("time").descending());
        List<Message> messages = mockMessageList(size);
        List<MessageVo> messageVos = mockMessageVoList(size);

        when(messageService.findPassState(any(Pageable.class))).thenReturn(new PageImpl<>(messages));
        when(messageVoService.returnVo(messages)).thenReturn(messageVos);

        mockMvc.perform(get("/message/getMessageList").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(size)))
                .andDo(print());

        verify(messageService).findPassState(any(Pageable.class));
        verify(messageVoService).returnVo(any(List.class));
    }

    /**
     * 用户查看用户列表成功
     * 用户已登录
     */
    @Test
    public void testFindUserList_WithUser() throws Exception {
        List<Message> messages = mockMessageList(10);
        List<MessageVo> messageVos = mockMessageVoList(10);
        Pageable pageable = PageRequest.of(0, 5, Sort.by("time").descending());
        when(messageService.findByUser(eq("1"), any(Pageable.class))).thenReturn(new PageImpl<>(messages));
        when(messageVoService.returnVo(messages)).thenReturn(messageVos);

        mockMvc.perform(get("/message/findUserList").param("page", "1")
                        .sessionAttr("user", new User(1,"1","1","1","1","1",0,"1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)))
                .andDo(print());

        verify(messageService).findByUser(eq("1"), any(Pageable.class));
        verify(messageVoService).returnVo(messages);
    }
    /**
     * 用户查看用户列表失败
     * 用户未登录
     */
    @Test
    public void testFindUserList_WithoutUser() throws Exception {
        mockMvc.perform(get("/message/findUserList").param("page", "1"))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    /**
     * 用户发布信息成功
     * 信息内容非空
     */
    @Test
    public void testSendMessage_Valid() throws Exception {
        mockMvc.perform(post("/sendMessage")
                        .param("userID", "1")
                        .param("content", "Hello, World!")
                        .sessionAttr("user", new User(1,"1","1","1","1","1",0,"1")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/message_list"))
                .andDo(print());

        verify(messageService).create(any(Message.class));
    }
    /**
     * 用户发布信息失败
     * 信息内容为空
     */
    @Test
    public void testSendMessage_EmptyContent() throws Exception {
        mockMvc.perform(post("/sendMessage")
                        .param("userID", "1")
                        .param("content", "")
                        .sessionAttr("user", new User(1,"1","1","1","1","1",0,"1")))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }
    /**
     * 用户编辑信息成功
     * 信息内容不为空
     */
    @Test
    public void testModifyMessage_Valid() throws Exception {
        when(messageService.findById(1)).thenReturn(new Message(1, "1", "Original content", LocalDateTime.now(), 1));

        mockMvc.perform(post("/modifyMessage.do")
                        .param("messageID", "1")
                        .param("content", "Updated content here.")
                        .sessionAttr("user", new User(1,"1","1","1","1","1",0,"1")))
                .andExpect(status().isOk())
                .andExpect(content().string("true"))
                .andDo(print());

        verify(messageService).findById(1);
        verify(messageService).update(any(Message.class));
    }

    /**
     * 用户编辑信息失败
     * 信息内容为空
     */
    @Test
    public void testModifyMessage_EmptyContent() throws Exception {
        when(messageService.findById(1)).thenReturn(new Message(1, "1", "Original content", LocalDateTime.now(), 1));

        mockMvc.perform(post("/modifyMessage.do")
                        .param("messageID", "1")
                        .param("content", "")
                        .sessionAttr("user", new User(1,"1","1","1","1","1",0,"1")))
                .andExpect(status().isBadRequest())
                .andDo(print());

        verify(messageService).findById(1);
    }

    /**
     * 用户编辑信息失败
     * 信息id不存在
     */
    @Test
    public void testModifyMessage_NonExistentMessage() throws Exception {
        when(messageService.findById(999)).thenThrow(new RuntimeException("Message not found"));

        mockMvc.perform(post("/modifyMessage.do")
                        .param("messageID", "999")
                        .param("content", "This should fail.")
                        .sessionAttr("user", new User(1,"1","1","1","1","1",0,"1")))
                .andExpect(status().isNotFound())
                .andDo(print());

        verify(messageService).findById(999);
    }

    /**
     * 用户删除信息成功
     */
    @Test
    public void testDelMessage_Valid() throws Exception {
        doNothing().when(messageService).delById(1);

        mockMvc.perform(post("/delMessage.do")
                        .param("messageID", "1")
                        .sessionAttr("user", new User(1,"1","1","1","1","1",0,"1")))
                .andExpect(status().isOk())
                .andExpect(content().string("true"))
                .andDo(print());

        verify(messageService).delById(1);
    }

    /**
     * 用户删除信息失败
     * 信息id不存在
     */
    @Test
    public void testDelMessage_NonExistentMessage() throws Exception {
        doThrow(new RuntimeException("Message not found")).when(messageService).delById(999);

        mockMvc.perform(post("/delMessage.do")
                        .param("messageID", "999")
                        .sessionAttr("user", new User(1,"1","1","1","1","1",0,"1")))
                .andExpect(status().isNotFound())
                .andDo(print());

        verify(messageService).delById(999);
    }
}
