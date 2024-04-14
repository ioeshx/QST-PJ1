package com.demo.controller.admin;


import com.demo.entity.User;
import com.demo.service.UserService;
import com.demo.utils.FileUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminUserControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    private UserService userService;


    private User getRealUser(int id, String userID, String userName, String password, String email, String phone, int isAdmin) throws Exception {
        User user = new User();
        user.setId(id);
        user.setUserID(userID);
        user.setUserName(userName);
        user.setPassword(password);
        user.setEmail(email);
        user.setPhone(phone);
        user.setIsadmin(isAdmin); // 0代表是User，1代表是Admin
        return user;
    }
    private List<User> getMockUsers(int size) {
        List<User> users = new ArrayList<>();
        for(int i=0; i<size; i++) {
            User user = new User();
            user.setId(i);
            user.setUserID("user"+i);
            user.setUserName("user"+i);
            user.setPassword("password"+i);
            user.setEmail("user");
            users.add(user);
        }
        return users;
    }
    private MockHttpSession getMockHttpSession(User user, boolean isUser){
        MockHttpSession session = new MockHttpSession();
        if(isUser)
            session.setAttribute("user", user);
        else
            session.setAttribute("admin", user);
        return session;
    }

    /**
     * 没有用户数据时，测试user_manage
     */
    @Test
    void user_manageTestWhenNoUser() throws Exception {
        Page<User> mockUsers = new PageImpl<>(new ArrayList<>());
        when(userService.findByUserID(any(Pageable.class)))
                .thenReturn(mockUsers);

        mockMvc.perform(get("/user_manage"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("total", mockUsers.getTotalPages()))
                .andExpect(view().name("admin/user_manage"));

        verify(userService,times(1)).findByUserID(any(Pageable.class));
    }

    /**
     * 用户数据存在时，测试user_manage
     */
    @Test
    void user_manageTestWhenUserExists() throws Exception {
        int size = 12;
        Page<User> mockUsers = new PageImpl<>(getMockUsers(size));
        when(userService.findByUserID(any(Pageable.class)))
                .thenReturn(mockUsers);

        mockMvc.perform(get("/user_manage"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("total", mockUsers.getTotalPages()))
                .andExpect(view().name("admin/user_manage"));

        verify(userService,times(1)).findByUserID(any(Pageable.class));
    }

    /**
     * 测试访问user_add
     */
    @Test
    void user_addTest() throws Exception {
        mockMvc.perform(get("/user_add"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user_add"));
    }

    /**
     * 测试userList函数成功执行的情况
     */
    @Test
    void userListTestSuccess() throws Exception {
        when(userService.findByUserID(any(Pageable.class)))
                .thenReturn(new PageImpl<>(getMockUsers(10)));

        mockMvc.perform(get("/userList.do"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"id\":0,\"userID\":\"user0\",\"userName\":\"user0\",\"password\":\"password0\",\"email\":\"user\"},"
                        + "{\"id\":1,\"userID\":\"user1\",\"userName\":\"user1\",\"password\":\"password1\",\"email\":\"user\"},"
                        + "{\"id\":2,\"userID\":\"user2\",\"userName\":\"user2\",\"password\":\"password2\",\"email\":\"user\"},"
                        + "{\"id\":3,\"userID\":\"user3\",\"userName\":\"user3\",\"password\":\"password3\",\"email\":\"user\"},"
                        + "{\"id\":4,\"userID\":\"user4\",\"userName\":\"user4\",\"password\":\"password4\",\"email\":\"user\"},"
                        + "{\"id\":5,\"userID\":\"user5\",\"userName\":\"user5\",\"password\":\"password5\",\"email\":\"user\"},"
                        + "{\"id\":6,\"userID\":\"user6\",\"userName\":\"user6\",\"password\":\"password6\",\"email\":\"user\"},"
                        + "{\"id\":7,\"userID\":\"user7\",\"userName\":\"user7\",\"password\":\"password7\",\"email\":\"user\"},"
                        + "{\"id\":8,\"userID\":\"user8\",\"userName\":\"user8\",\"password\":\"password8\",\"email\":\"user\"},"
                        + "{\"id\":9,\"userID\":\"user9\",\"userName\":\"user9\",\"password\":\"password9\",\"email\":\"user\"}]"));
    }

    /**
     * 测试GET请求不带参数page，userList返回结果
     */
    @Test
    void userListTestWhenArgumentPageEmpty() throws Exception {
        when(userService.findByUserID(any(Pageable.class)))
                .thenReturn(new PageImpl<>(getMockUsers(0)));
        mockMvc.perform(get("/userList.do"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        when(userService.findByUserID(any(Pageable.class)))
                .thenReturn(new PageImpl<>(getMockUsers(10)));
        mockMvc.perform(get("/userList.do"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"id\":0,\"userID\":\"user0\",\"userName\":\"user0\",\"password\":\"password0\",\"email\":\"user\"},"
                        + "{\"id\":1,\"userID\":\"user1\",\"userName\":\"user1\",\"password\":\"password1\",\"email\":\"user\"},"
                        + "{\"id\":2,\"userID\":\"user2\",\"userName\":\"user2\",\"password\":\"password2\",\"email\":\"user\"},"
                        + "{\"id\":3,\"userID\":\"user3\",\"userName\":\"user3\",\"password\":\"password3\",\"email\":\"user\"},"
                        + "{\"id\":4,\"userID\":\"user4\",\"userName\":\"user4\",\"password\":\"password4\",\"email\":\"user\"},"
                        + "{\"id\":5,\"userID\":\"user5\",\"userName\":\"user5\",\"password\":\"password5\",\"email\":\"user\"},"
                        + "{\"id\":6,\"userID\":\"user6\",\"userName\":\"user6\",\"password\":\"password6\",\"email\":\"user\"},"
                        + "{\"id\":7,\"userID\":\"user7\",\"userName\":\"user7\",\"password\":\"password7\",\"email\":\"user\"},"
                        + "{\"id\":8,\"userID\":\"user8\",\"userName\":\"user8\",\"password\":\"password8\",\"email\":\"user\"},"
                        + "{\"id\":9,\"userID\":\"user9\",\"userName\":\"user9\",\"password\":\"password9\",\"email\":\"user\"}]"));
    }

    /**
     * 当Page参数小于等于0时，测试userList
     */
    @Test
    void userListTestWhenArgumentPageLEZero() throws Exception {
        when(userService.findByUserID(any(Pageable.class)))
                .thenReturn(new PageImpl<>(getMockUsers(5)));
        mockMvc.perform(get("/userList.do")
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        mockMvc.perform(get("/userList.do")
                        .param("page", "-1"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }


    /**
     * 当参数Page超过总页数时，测试userList
     * 返回结果应该为空
     */
    @Test
    void userListTestWhenArgumentPageExceedsMax() throws Exception {
        when(userService.findByUserID(any(Pageable.class)))
                .thenReturn(new PageImpl<>(getMockUsers(0)));


        mockMvc.perform(get("/userList.do")
                        .param("page", "999"))   // 参数999,超过mock数据的10/10=1页
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    /**
     * 当Id存在时，测试user_edit函数
     */
    @Test
    void user_editTestWhenSuccess() throws Exception {
        User mockUser = getRealUser(1, "user1", "user1", "password1", "123@qq.com", "12345678901", 0);
        when(userService.findById(any(Integer.class)))
                .thenReturn(mockUser);

        mockMvc.perform(get("/user_edit")
                        .param("id", "1")
                        .session(new MockHttpSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user_edit"))
                .andExpect(model().attribute("user", samePropertyValuesAs(mockUser)));

        verify(userService, times(1)).findById(any(Integer.class));
    }

    /**
     * 当Id不存在时，测试user_edit函数
     */
    @Test
    void user_editTestWhenIDNotExists() throws Exception {
        when(userService.findById(any(Integer.class)))
                .thenReturn(null);

        mockMvc.perform(get("/user_edit")
                        .param("id", "1")
                        .session(new MockHttpSession()))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).findById(any(Integer.class));
    }

    // TODO modifyUser函数测试



    // TODO addUser函数测试

    /**
     * 当userID已被使用，测试checkUserID函数
     */
    @Test
    void checkUserIDTestWhenUserIDExists() throws Exception {
        when(userService.countUserID(any(String.class)))
                .thenReturn(1);

        mockMvc.perform(post("/checkUserID.do")
                        .param("userID", "usertest"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(userService, times(1)).countUserID(any(String.class));
    }

    /**
     * 当userID未被使用，测试checkUserID函数
     */
    @Test
    void checkUserIDTestWhenUserIDNotExists() throws Exception {
        when(userService.countUserID(any(String.class)))
                .thenReturn(0);

        mockMvc.perform(post("/checkUserID.do")
                        .param("userID", "usertest"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(userService, times(1)).countUserID(any(String.class));
    }

    /**
     * userID存在时，测试delUser函数
     */
    @Test
    void delUserIDTestWhenUserIDExists() throws Exception {
        doNothing().when(userService).delByID(any(Integer.class));

        mockMvc.perform(post("/delUser.do")
                        .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(userService, times(1)).delByID(any(Integer.class));
    }
    /**
     * userID不存在时，测试delUser函数
     * 应该失败
     */
    @Test
    void delUserIDTestWhenUserIDNotExists() throws Exception {
        doThrow(new IllegalArgumentException("User not found"))
                .when(userService).delByID(any(Integer.class));

        mockMvc.perform(post("/delUser.do")
                        .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(userService, times(1)).delByID(any(Integer.class));
    }
}
