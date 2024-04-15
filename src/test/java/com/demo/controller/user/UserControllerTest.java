package com.demo.controller.user;

import com.demo.entity.User;
import com.demo.service.UserService;
import com.demo.utils.FileUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.exceptions.TemplateInputException;

import java.util.Objects;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


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

    private User getRealUser(int id, String userID, String userName, String password, String email, String phone, int isAdmin,MultipartFile picture) throws Exception {
        User user = new User();
        user.setId(id);
        user.setUserID(userID);
        user.setUserName(userName);
        user.setPassword(password);
        user.setEmail(email);
        user.setPhone(phone);
        user.setIsadmin(isAdmin); // 0代表是User，1代表是Admin
        if(picture!=null && !Objects.equals(picture.getOriginalFilename(), "")){
            user.setPicture(FileUtil.saveUserFile(picture));
        }
        return user;
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
     * 使用语句覆盖测试signup函数
     */
    @Test
    void signUpTest() throws Exception{
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"));
    }

    /**
     * 使用语句覆盖测试login函数
     */
    @Test
    void loginTest() throws Exception{
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    /**
     * （分支覆盖）测试用户正确登录的分支，使用mock提供的用户
     */
    @Test
    void loginCheckTestForUser() throws Exception{
        User mockUser = getMockUser(true,true);
        when(userService.checkLogin(any(String.class), any(String.class)) )
                .thenReturn(mockUser);

        mockMvc.perform(post("/loginCheck.do")
                        .param("userID","User")
                        .param("password","123456")
                        .session(getMockHttpSession(mockUser, true)))
                .andExpect(status().isOk())
                .andExpect(content().string("/index"))
                .andExpect(request -> assertEquals(
                        "User objects are not equal",
                        mockUser,
                        request.getRequest().getSession().getAttribute("user")));
    }

    /**
     * （分支覆盖）测试管理员正确登录的分支，使用mock提供的管理员
     */
    @Test
    void loginCheckTestForAdmin() throws Exception{
        User mockUser = getMockUser(true,false);
        when(userService.checkLogin(any(String.class), any(String.class)) )
                .thenReturn(mockUser);

        mockMvc.perform(post("/loginCheck.do")
                        .param("userID","Admin")
                        .param("password","123456")
                        .session(getMockHttpSession(mockUser, false)))
                .andExpect(status().isOk())
                .andExpect(content().string("/admin_index"))
                .andExpect(request -> assertEquals(
                        "User objects are not equal",
                        mockUser,
                        request.getRequest().getSession().getAttribute("admin")));
    }

    /**
     * （分支覆盖）测试登录失败的分支，使用mock模拟登录失败
     */
    @Test
    void loginCheckWhenFail() throws Exception{
        when(userService.checkLogin(any(String.class), any(String.class)) )
                .thenReturn(null);

        mockMvc.perform(post("/loginCheck.do")
                        .param("userID","wrongUser")
                        .param("password","123456"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

    }

    /**
     *  （等价类划分）测试register函数，输入的用户注册参数是合法的
     */
    @Test
    void registerTest_Success() throws Exception{
        when(userService.create(any(User.class)))
                .thenReturn(0);

        mockMvc.perform(post("/register.do")
                        .param("userID","test_random")
                        .param("userName","User")
                        .param("password","123456")
                        .param("email","")
                        .param("phone",""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("login"));
    }
    /**
     * （等价类划分）测试register函数，输入的任意字符串参数超过长度限制
     *  应该失败
     */
    @Test
    void registerTestShouldFailWhenStringExceedsMaxLength() throws Exception{
        doThrow(new IllegalArgumentException("String exceeds max length"))
                .when(userService).create(any(User.class));
        try {
            String longString = new String(new char[256]).replace("\0", "a");
            mockMvc.perform(post("/register.do")
                            .param("userID",longString) // longString长度是256，超过了数据库中userID的长度255
                            .param("userName","User")
                            .param("password","123456")
                            .param("email","")
                            .param("phone",""))
                    .andExpect(status().is3xxRedirection());
        }catch(Exception e){
            assert e.getCause() instanceof IllegalArgumentException;
            assert e.getCause().getMessage().equals("String exceeds max length");
        }
    }

    /**
     * （等价类划分）测试register函数，输入的用户名参数是已经存在的
     * 应该失败
     */
    @Test
    void registerTestShouldFailWhenUserExists() throws Exception{
        when(userService.create(any(User.class)))   //确保不会修改数据库
                .thenReturn(0);

        mockMvc.perform(post("/register.do")
                        .param("userID","test") // username test already exists
                        .param("userName","User")
                        .param("password","123456")
                        .param("email","")
                        .param("phone",""))
                .andExpect(status().is3xxRedirection());
    }

    /**
     * （等价类划分）测试logout函数，用户已经登录（session中有user）
     */
    @Test
    void logOutTestWhenLogin() throws Exception{
        MockHttpSession mockHttpSession = getMockHttpSession(getMockUser(true,true), true);
        mockMvc.perform(get("/logout.do")
                        .session(mockHttpSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"))
                .andExpect(request().sessionAttribute("user", nullValue()));
    }

    /**
     * （等价类划分）测试logout函数，用户未登录（session中没有user）
     */
    @Test
    void logOutTestWhenNotLogin() throws Exception{
        mockMvc.perform(get("/logout.do"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"));
    }

    /**
     * （等价类划分）测试quit函数，管理员已经登录（session中有admin）
     */
    @Test
    void quitTestWhenLogin() throws Exception{
        MockHttpSession mockHttpSession = getMockHttpSession(getMockUser(true,false), false);
        mockMvc.perform(get("/quit.do")
                        .session(mockHttpSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"))
                .andExpect(request().sessionAttribute("user", nullValue()));
    }
    /**
     * （等价类划分）测试quit函数，管理员未登录（session中没有admin）
     */
    @Test
    void quitTestWhenNotLogin() throws Exception{
        mockMvc.perform(get("/quit.do"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"));
    }

    /**
     * userID存在，使用合法参数，测试updateUser函数
     */
    @Test
    void updateUserTestSuccess() throws Exception{
        MockMultipartFile mockFile = new MockMultipartFile("picture", "", "", "".getBytes());
        User mockUser = getRealUser(0,"test_user", "test", "123", "test@qq.com", "12345",  0, null);
        User expectedUser = new User(0, "test_user", "username_update", "test", "123456789@qq.com", "12345678910", 0, null);

        when(userService.findByUserID(eq("test_user")))
                .thenReturn(mockUser);
        doNothing().when(userService).updateUser(any(User.class));

        mockMvc.perform(multipart("/updateUser.do")
                        .file(mockFile)
                        .param("userName","username_update")
                        .param("userID","test_user")
                        .param("passwordNew","test")
                        .param("email","123456789@qq.com")
                        .param("phone","12345678910")//.param("picture","")
                        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("user_info"))
                .andExpect(request().sessionAttribute("user", samePropertyValuesAs(expectedUser)));

        verify(userService, times(1)).findByUserID(eq("test_user"));
        verify(userService, times(1)).updateUser(any(User.class));
    }

    /**
     * 当userID在数据库不存在（找不到对于User），测试updateUser函数
     */
    @Test
    void updateUserWhenUserNotExists() {
        when(userService.findByUserID(eq("test_user")))
                .thenReturn(null);
        try{
            mockMvc.perform(post("/updateUser.do")
                            .param("userName","username_update")
                            .param("userID","notExists")
                            .param("passwordNew","test")
                            .param("email",""))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("user_info"));
        }catch (Exception e){
            assert e.getCause() instanceof NullPointerException;
        }
    }

    /**
     * userID存在，
     * 但是输入参数userName,PasswordNew,email,phone，其中任意一个不合法时（为空，长度超过限制或者不符合格式）
     * 测试updateUser
     */
    @Test
    void updateUserWhenArgumentInvalid() throws Exception {
        String longString = new String(new char[256]).replace("\0", "a");
        MockMultipartFile mockFile = new MockMultipartFile("picture", "", "", "".getBytes());
        User mockUser = getRealUser(0,"test_user", "test", "123", "", "",  0, null);
        when(userService.findByUserID(eq("test_user")))
                .thenReturn(mockUser);
        doThrow(new IllegalArgumentException("Invalid arguments")).
                when(userService).updateUser(any(User.class));
        try{
            mockMvc.perform(multipart("/updateUser.do")
                            .file(mockFile)
                            .param("userName","")   //为空
                            .param("userID","test_user")
                            .param("passwordNew",longString)    //长度超过限制
                            .param("email","not valid")     //格式不合法
                            .param("phone","12345678910")
                    )
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("user_info"));
        }catch (Exception e){
            assert e.getCause() instanceof IllegalArgumentException;
            assert e.getCause().getMessage().equals("Invalid arguments");
        }

    }

    /**
     * password参数为空或null，测试updateUser函数
     */
    @Test
    void updateUserTestWhenPasswordEmptyOrNull() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile("picture", "", "", "".getBytes());
        User mockUser = getRealUser(0,"test_user", "test", "123", "test@qq.com", "12345",  0, null);
        User expectedUser = new User(0, "test_user", "username_update", "123", "123456789@qq.com", "12345678910", 0, null);

        when(userService.findByUserID(eq("test_user")))
                .thenReturn(mockUser);
        doNothing().when(userService).updateUser(any(User.class));

        // PasswordNew为空
        mockMvc.perform(multipart("/updateUser.do")
                        .file(mockFile)
                        .param("userName","username_update")
                        .param("userID","test_user")
                        .param("passwordNew","")
                        .param("email","123456789@qq.com")
                        .param("phone","12345678910")//.param("picture","")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("user_info"))
                .andExpect(request().sessionAttribute("user", samePropertyValuesAs(expectedUser)));

        verify(userService, times(1)).findByUserID(eq("test_user"));
        verify(userService, times(1)).updateUser(any(User.class));

        // password为null，请求不带password
        mockMvc.perform(multipart("/updateUser.do")
                        .file(mockFile)
                        .param("userName","username_update")
                        .param("userID","test_user")
                        .param("email","123456789@qq.com")
                        .param("phone","12345678910")//.param("picture","")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("user_info"))
                .andExpect(request().sessionAttribute("user", samePropertyValuesAs(expectedUser)));

    }

    /**
     * TODO 关于Picture参数的测试还要补充
     * picture参数的OriginalFilename为空时，测试updateUser函数
     */
    @Test
    void updateUserTestWhenPictureEmpty() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile("picture", "", "", "".getBytes());
        User mockUser = getRealUser(0,"test_user", "test", "123", "test@qq.com", "12345",  0, null);
        mockUser.setPicture("test.jpg");
        User expectedUser = new User(0, "test_user", "username_update", "test", "123456789@qq.com", "12345678910", 0, null);
        expectedUser.setPicture("test.jpg");

        when(userService.findByUserID(eq("test_user")))
                .thenReturn(mockUser);
        doNothing().when(userService).updateUser(any(User.class));

        mockMvc.perform(multipart("/updateUser.do")
                        .file(mockFile)
                        .param("userName","username_update")
                        .param("userID","test_user")
                        .param("passwordNew","test")
                        .param("email","123456789@qq.com")
                        .param("phone","12345678910")//.param("picture","")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("user_info"))
                .andExpect(request().sessionAttribute("user", samePropertyValuesAs(expectedUser)));

        verify(userService, times(1)).findByUserID(eq("test_user"));
        verify(userService, times(1)).updateUser(any(User.class));

    }

    /**
     * 当输入的userID已存在，密码正确时，测试checkPassword函数
     */
    @Test
    void checkPasswordTestWhenTrue() throws Exception {
        when(userService.findByUserID(eq("test_user")))
                .thenReturn(getRealUser(0,"test_user", "test", "123456", "", "",  0, null));

        mockMvc.perform(get("/checkPassword.do")
                        .param("userID","test_user")
                        .param("password","123456"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
    /**
     * 当输入的userID已存在且密码错误时，测试checkPassword函数
     */
    @Test
    void checkPasswordTestWhenFalse() throws Exception {
        when(userService.findByUserID(eq("test_user")))
                .thenReturn(getRealUser(0,"test_user", "test", "wrongPassword", "", "",  0, null));

        mockMvc.perform(get("/checkPassword.do")
                        .param("userID","test_user")
                        .param("password","123456"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    /**
     * 当输入userID不存在时，测试checkPassword函数
     * 测试应该失败
     */
    @Test
    void checkPasswordTestWhenUserNotExists(){
        when(userService.findByUserID(eq("test_user")))
                .thenReturn(null);
        try{
            mockMvc.perform(get("/checkPassword.do")
                            .param("userID","test_user")
                            .param("password","123456"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("false"));
        }catch(Exception e){
            assert e.getCause() instanceof NullPointerException;
        }
    }

    /**
     * 用户未登录时(session中没有用户)，测试user_info函数
     * 应该会失败
     */
    @Test
    void user_infoTestWhenSessionHasNoUser(){
        try{
            mockMvc.perform(get("/user_info"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("user_info"));
        }catch(Exception e){
            assert e.getCause() instanceof TemplateInputException;
        }
    }

    /**
     * 用户登录时(session中有用户)，测试user_info函数
     */
    @Test
    void user_infoTestWhenUserLogin() throws Exception{
        MockHttpSession mockHttpSession = getMockHttpSession(
                getRealUser(0,"test_user", "test", "123456", "", "",  0, null), true);

        mockMvc.perform(get("/user_info")
                        .session(mockHttpSession))
                .andExpect(status().isOk())
                .andExpect(view().name("user_info"));
    }
}
