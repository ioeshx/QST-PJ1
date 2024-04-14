package com.demo.controller.user;

import com.demo.entity.Order;
import com.demo.entity.User;
import com.demo.entity.Venue;
import com.demo.entity.vo.OrderVo;
import com.demo.exception.LoginException;
import com.demo.service.OrderService;
import com.demo.service.OrderVoService;
import com.demo.service.VenueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;
    @MockBean
    private OrderVoService orderVoService;
    @MockBean
    private VenueService venueService;

    private User getMockUser(String userId){
        User mockUser = mock(User.class);
        when(mockUser.getUserID()).thenReturn(userId);
        return mockUser;
    }
    private MockHttpSession getMockHttpSession(User user) {
        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute("user", user);
        return  mockHttpSession;
    }
    private List<Order> getMockOrderList(int size){
        List<Order> orderList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Order order = new Order(i, "userID", i, 1,LocalDateTime.now(),
                    LocalDateTime.now(), 1, 100);
            orderList.add(order);
        }
        return orderList;
    }
    private List<Venue> getMockVenueList(int size){
        List<Venue> venueList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Venue venue = new Venue(i, "venueName", "description",
                    100, "picture", "address", "open_time", "close_time");
            venueList.add(venue);
        }
        return venueList;
    }
    private List<OrderVo> getMockOrderVoList(List<Order> mockOrderList){
        List<OrderVo> orderVoList = new ArrayList<>();
        for (int i = 0; i < mockOrderList.size(); i++) {
            OrderVo orderVo = new OrderVo(mockOrderList.get(i).getOrderID(),
                    mockOrderList.get(i).getUserID(), mockOrderList.get(i).getVenueID(),
                    "venueName", mockOrderList.get(i).getState(),
                    mockOrderList.get(i).getOrderTime(), mockOrderList.get(i).getStartTime(),
                    mockOrderList.get(i).getHours(), mockOrderList.get(i).getTotal());
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }
    /**
     * 当用户未登录时，测试order_manage方法
     * @see OrderController#order_manage
     */
    @Test
    void order_manageTestWhenNotLogin() throws Exception{
        MockHttpSession session = getMockHttpSession(null);
        try{
            mockMvc.perform(get("/order_manage")
                    .session(session));
        } catch (Exception e) {
            assert e.getCause() instanceof LoginException;
            assert e.getCause().getMessage().equals("请登录！");
        }
    }

    /**
     * 当用户已登录时，并且没有订单数据。测试order_manage方法
     * @see OrderController#order_manage
     */
    @Test
    void order_manageTestWhenNoData() throws Exception{
        User mockUser = getMockUser("1");
        MockHttpSession session = getMockHttpSession(mockUser);

        when(orderService.findUserOrder(eq(mockUser.getUserID()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(new ArrayList<>()));

        mockMvc.perform(get("/order_manage")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("order_manage"))
                .andExpect(model().attribute("total", equalTo(1)))
                .andReturn();

        verify(orderService, times(1)).findUserOrder(eq(mockUser.getUserID()), any(Pageable.class));
    }

    /**
     * 当用户已登录时，并且有订单数据。测试order_manage方法
     * @see OrderController#order_manage
     */
    @Test
    void order_manageTestWhenHasData() throws Exception{
        User mockUser = getMockUser("1");
        MockHttpSession session = getMockHttpSession(mockUser);
        int size = 5;
        List<Order> orderList = getMockOrderList(size);
        Page<Order> page = new PageImpl<>(orderList);
        when(orderService.findUserOrder(eq(mockUser.getUserID()), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/order_manage").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("order_manage"))
                .andExpect(model().attribute("total", equalTo(page.getTotalPages())))
                .andReturn();

        verify(orderService, times(1)).findUserOrder(eq(mockUser.getUserID()), any(Pageable.class));
    }

    /**
     * 当venueID不存在时，测试order_place方法
     * @see OrderController#order_place(Model model,int venueID)
     */
    @Test
    void order_placeTestWhenIDNotExist() throws Exception{
        when(venueService.findByVenueID(any(Integer.class)))
                .thenReturn(null);

        mockMvc.perform(get("/order_place.do")
                        .param("venueID", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("order_place"))
                .andExpect(model().attribute("venue", nullValue()))
                .andReturn();

        verify(venueService, times(1)).findByVenueID(any(Integer.class));
    }

    /**
     * 当venueID存在时,测试order_place方法
     * @see OrderController#order_place(Model model,int venueID)
     */
    @Test
    void order_placeTEstWhenIDExist() throws Exception{
        Venue venue = new Venue(1, "venueName", "description", 100, "picture", "address", "open_time", "close_time");
        when(venueService.findByVenueID(any(Integer.class)))
                .thenReturn(venue);

        mockMvc.perform(get("/order_place.do")
                        .param("venueID", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("order_place"))
                .andExpect(model().attribute("venue", equalTo(venue)))
                .andReturn();

        verify(venueService, times(1)).findByVenueID(any(Integer.class));
    }

    /**
     * 测试不含venueID参数的order_place方法
     * @see OrderController#order_place(Model model)
     */
    @Test
    void order_placeTest() throws Exception{
        mockMvc.perform(get("/order_place"))
                .andExpect(status().isOk())
                .andExpect(view().name("order_place"))
                .andReturn();
    }

    /**
     * 当用户未登录时，测试order_list方法
     * @see OrderController#order_list
     */
    @Test
    void order_listTestWhenNotLogin() throws Exception{
        MockHttpSession session = getMockHttpSession(null);
        try{
            mockMvc.perform(get("/getOrderList.do")
                    .session(session));
        } catch (Exception e) {
            assert e.getCause() instanceof LoginException;
            assert e.getCause().getMessage().equals("请登录！");
        }
    }

    /**
     * 当用户已登录时，请求参数Page正确，并且没有订单数据。测试order_list方法
     * @see OrderController#order_list
     */
    @Test
    void order_placeTestWhenNoData() throws Exception{
        User mockUser = getMockUser("1");
        MockHttpSession session = getMockHttpSession(mockUser);

        when(orderService.findUserOrder(eq(mockUser.getUserID()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(new ArrayList<>()));
        when(orderVoService.returnVo(anyList()))
                .thenReturn(new ArrayList<>());

        mockMvc.perform(get("/getOrderList.do")
                        .param("page", "1")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)))
                .andReturn();

        verify(orderService, times(1)).findUserOrder(eq(mockUser.getUserID()), any(Pageable.class));
        verify(orderVoService, times(1)).returnVo(anyList());
    }


    /**
     * 当用户已登录时，请求参数Page正确，并且有订单数据。测试order_list方法
     * @see OrderController#order_list
     */
    @Test
    void order_listTestWhenHasData() throws Exception{
        User mockUser = getMockUser("1");
        MockHttpSession session = getMockHttpSession(mockUser);
        int size = 5;
        Page<Order> page = new PageImpl<>(getMockOrderList(size));
        when(orderService.findUserOrder(eq(mockUser.getUserID()), any(Pageable.class)))
                .thenReturn(page);

        when(orderVoService.returnVo(anyList()))
                .thenReturn(getMockOrderVoList(page.getContent()));


        mockMvc.perform(get("/getOrderList.do")
                        .param("page", "1")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(size)))
                .andReturn();

        verify(orderService, times(1)).findUserOrder(eq(mockUser.getUserID()), any(Pageable.class));
        verify(orderVoService, times(1)).returnVo(anyList());
    }

    /**
     * 当用户已登录，请求参数Page错误，测试order_list方法
     * 应该抛出异常失败
     * @see OrderController#order_list
     */
    @Test
    void order_listTestWhenArgumentPageError() throws Exception{
        User mockUser = getMockUser("1");
        MockHttpSession session = getMockHttpSession(mockUser);

        when(orderService.findUserOrder(eq(mockUser.getUserID()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(new ArrayList<>()));
        when(orderVoService.returnVo(anyList()))
                .thenReturn(new ArrayList<>());

        mockMvc.perform(get("/getOrderList.do")
                        .param("page", "0")     // page小于等于0，是一个非法等价类
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)))
                .andReturn();

        verify(orderService, times(0)).findUserOrder(eq(mockUser.getUserID()), any(Pageable.class));
        verify(orderVoService, times(0)).returnVo(anyList());
    }

    // TODO addOrder方法还要补充测试用例
    /**
     * 当用户已登录时，测试addOrder方法
     * @see OrderController#addOrder
     */
    @Test
    void add_orderTestWhenNotLogin() throws Exception{
        MockHttpSession session = getMockHttpSession(null);

        try{
            mockMvc.perform(get("/addOrder.do")
                    .session(session));
        } catch (Exception e) {
            assert e.getCause() instanceof LoginException;
            assert e.getCause().getMessage().equals("请登录！");
        }
    }
    /**
     * 当用户已登录时，测试addOrder方法
     * @see OrderController#addOrder
     */
    @Test
    void add_orderTestWhenLogin() throws Exception{
        User mockUser = getMockUser("1");
        MockHttpSession session = getMockHttpSession(mockUser);

//        mockMvc.perform(get("/addOrder.do")
//                .session(session))
//                .andExpect(status().isOk())
//                .andReturn();
    }

    /**
     * 当orderId在数据库存在时，测试finish_order方法
     * @see OrderController#finishOrder(int orderID)
     */
    @Test
    void finishOrderTestWhenIDExists() throws Exception{
        doNothing().when(orderService).finishOrder(anyInt());
        mockMvc.perform(post("/finishOrder.do")
                        .param("orderID", "1"))
                .andExpect(status().isOk())
                .andReturn();
    }

    /**
     * 当orderID在数据库不存在时，测试finish_order方法
     * 这个测试应该失败，会抛出ava.lang.RuntimeException: Order not found
     * @see OrderController#finishOrder(int orderID)
     */
    @Test
    void finishOrderTestWhenIDNotExist() throws Exception{
        doThrow(new RuntimeException("Order not found")).when(orderService).finishOrder(anyInt());
        mockMvc.perform(post("/finishOrder.do")
                        .param("orderID", "9999"))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    /**
     * 当orderID在数据库存在时，测试editOrder方法
     * @see OrderController#editOrder
     */
    @Test
    void editOrderTestWhenIDExist() throws Exception{
        int orderID = 1;
        Order mockOrder = getMockOrderList(1).get(0);
        Venue mockVenue = getMockVenueList(1).get(0);
        when(orderService.findById(eq(orderID)))
                .thenReturn(mockOrder);
        when(venueService.findByVenueID(anyInt()))
                .thenReturn(mockVenue);

        mockMvc.perform(get("/modifyOrder.do")
                        .param("orderID", String.valueOf(orderID)))
                .andExpect(status().isOk())
                .andExpect(view().name("order_edit"))
                .andExpect(model().attribute("order", equalTo(mockOrder)))
                .andExpect(model().attribute("venue", equalTo(mockVenue)))
                .andReturn();

        verify(orderService, times(1)).findById(eq(orderID));
        verify(venueService,times(1)).findByVenueID(anyInt());
    }

    /**
     * 当orderID在数据库不存在时，测试editOrder方法
     * @see OrderController#editOrder
     */
    @Test
    void editOrderTestWhenIDNotExist() throws Exception{
        int orderID = 1;
        when(orderService.findById(eq(orderID)))
                .thenReturn(mock(Order.class));
        when(venueService.findByVenueID(anyInt()))
                .thenReturn(mock(Venue.class));

        mockMvc.perform(get("/modifyOrder.do")
                        .param("orderID", String.valueOf(orderID)))
                .andExpect(status().isOk())
                .andExpect(view().name("order_edit"))
                .andReturn();

        verify(orderService, times(1)).findById(eq(orderID));
        verify(venueService,times(1)).findByVenueID(anyInt());
    }

    /**
     * 当请求参数中不带orderID时，测试editOrder方法
     * @see OrderController#editOrder
     */
    @Test
    void editOrderTestWhenIDNotExist2() throws Exception{
        Order mockOrder = getMockOrderList(1).get(0);
        Venue mockVenue = getMockVenueList(1).get(0);
        when(orderService.findById(anyInt()))
                .thenReturn(mockOrder);
        when(venueService.findByVenueID(anyInt()))
                .thenReturn(mockVenue);

        mockMvc.perform(get("/editOrder.do"))
                .andExpect(status().is4xxClientError());

    }
    // TODO modifyOrder方法还要补充测试用例
    /**
     * 测试modifyOrder方法
     * @see OrderController#modifyOrder
     */
    @Test
    void modifyOrderTest() throws Exception{

    }

    /**
     * 当OrderId参数在数据库中存在时，测试delOrder方法
     * @see OrderController#delOrder
     */
    @Test
    void delOrderTestWhenIDExists() throws Exception{
        doNothing().when(orderService).delOrder(anyInt());

        mockMvc.perform(post("/delOrder.do")
                        .param("orderID", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"))
                .andReturn();
    }
    /**
     * 当OrderId参数在数据库中不存在时，测试delOrder方法
     * TODO 修改这个测试用例，使其通过
     * @see OrderController#delOrder
     */
    @Test
    void delOrderTestWhenIDNotExist() throws Exception{
        doThrow(new IllegalArgumentException("Order not found"))
                .when(orderService).delOrder(anyInt());
        mockMvc.perform(post("/delOrder.do")
                        .param("orderID", "999")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andReturn();
    }
    /**
     * 当GET请求不带orderID参数时，测试delOrder方法
     * 因为不带OrderId参数，应该抛出如下异常（我没有捕获）
     * java.lang.IllegalStateException: Optional int parameter 'orderID' is present but cannot be translated into a null value due to being declared as a primitive type. Consider declaring it as object wrapper for the corresponding primitive type.
     * @see OrderController#delOrder
     */
    @Test
    void delOrderTestWhenNoArgument() throws Exception{
        doNothing().when(orderService).delOrder(anyInt());
        mockMvc.perform(post("/delOrder.do"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("true"))
                .andReturn();
    }

    // TODO getOrder方法还要补充测试用例
    /**
     * 测试getOrder方法
     * @see OrderController#getOrder
     */
    @Test
    void getOrderTest() throws Exception{

    }

}
