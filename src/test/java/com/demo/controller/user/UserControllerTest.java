package com.demo.controller.user;

import com.demo.entity.User;
import com.demo.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private User  getMockUser(boolean isLegal, boolean isUser){
        if(!isLegal)
            return null;
        User mockUser = mock(User.class);
        if(isUser)
            when(mockUser.getIsadmin()).thenReturn(0);
        else
            when(mockUser.getIsadmin()).thenReturn(1);
        return mockUser;
    }

    @Test
    void signUpTest() throws Exception{
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"));
    }

    @Test
    void loginTest() throws Exception{
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }
    @Test
    void loginCheckTestForUser() throws Exception{
        when(userService.checkLogin(any(String.class), any(String.class)) )
                .thenReturn(getMockUser(true,true));
        mockMvc.perform(post("/loginCheck.do"))
                .andExpect(status().isOk());
    }

    @Test
    void loginCheckTestForAdmin() throws Exception{

    }

    @Test
    void loginCheckForIllegalUser() throws Exception{

    }

    @Test
    void loginCheckForIllegalAdmin() throws Exception{

    }



}
