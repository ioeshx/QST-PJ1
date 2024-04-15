package com.demo.controller.admin;


import com.demo.entity.Order;
import com.demo.entity.vo.OrderVo;
import com.demo.service.OrderService;
import com.demo.service.OrderVoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
public class AdminOrderControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockBean
    private OrderService orderService;
    @MockBean
    private OrderVoService orderVoService;

    private List<Order> getMockOrderList(int size){
        List<Order> orderList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Order order = new Order(i, "userID", i, 1, LocalDateTime.now(),
                    LocalDateTime.now(), 1, 100);
            orderList.add(order);
        }
        return orderList;
    }
    private List<OrderVo> getMockOrderVoList(List<Order> mockOrderList){
        List<OrderVo> orderVoList = new ArrayList<>();
        for (Order order : mockOrderList) {
            OrderVo orderVo = new OrderVo(order.getOrderID(),
                    order.getUserID(), order.getVenueID(),
                    "venueName", order.getState(),
                    order.getOrderTime(), order.getStartTime(),
                    order.getHours(), order.getTotal());
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }
    // reservation_manage 等价类划分应该有四种，
    // 1. 未审查order和已审查order都存在
    // 2. 未审查order存在，已审查order不存在
    // 3. 未审查order不存在，已审查order存在
    // 4. 未审查order和已审查order都不存在
    // 为了简化测试，合并成两种
    /**
     * 当未审查order和已审查order都存在时
     * 测试reservation_manage方法
     * @see AdminOrderController#reservation_manage
     */
    @Test
    void reservation_manageTestWhenDataExists() throws Exception {
        int size = 10;
        List<Order> mockOrderList = getMockOrderList(size);
        Page<Order> mockOrderPage = new PageImpl<>(mockOrderList);
        List<OrderVo> mockOrderVoList = getMockOrderVoList(mockOrderList);
        when(orderService.findAuditOrder())
                .thenReturn(mockOrderList);
        when(orderVoService.returnVo(any(List.class)))
                .thenReturn(mockOrderVoList);
        when(orderService.findNoAuditOrder(any(Pageable.class)))
                .thenReturn(mockOrderPage);

        mockMvc.perform(get("/reservation_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/reservation_manage"))
                .andExpect(model().attribute("order_list", mockOrderVoList))
                .andExpect(model().attribute("total", mockOrderPage.getTotalPages()));

        verify(orderService,times(1)).findAuditOrder();
        verify(orderVoService,times(1)).returnVo(any(List.class));
        verify(orderService,times(1)).findNoAuditOrder(any(Pageable.class));
    }

    /**
     * 当未审查order和已审查order都不存在时
     * 测试reservation_manage方法
     * @see AdminOrderController#reservation_manage
     */
    @Test
    void reservation_manageTestWhenNoData() throws Exception {
        List<Order> mockOrderList = new ArrayList<>();
        Page<Order> mockOrderPage = new PageImpl<>(mockOrderList);
        List<OrderVo> mockOrderVoList = getMockOrderVoList(new ArrayList<>());
        when(orderService.findAuditOrder())
                .thenReturn(mockOrderList);
        when(orderVoService.returnVo(any(List.class)))
                .thenReturn(mockOrderVoList);
        when(orderService.findNoAuditOrder(any(Pageable.class)))
                .thenReturn(mockOrderPage);

        mockMvc.perform(get("/reservation_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/reservation_manage"))
                .andExpect(model().attribute("order_list", mockOrderVoList))
                .andExpect(model().attribute("total", mockOrderPage.getTotalPages()));

        verify(orderService,times(1)).findAuditOrder();
        verify(orderVoService,times(1)).returnVo(any(List.class));
        verify(orderService,times(1)).findNoAuditOrder(any(Pageable.class));
    }

    /**
     * 当未审查order数据存在并且参数Page正确时，
     * 测试getNoAuditOrder方法
     * @see AdminOrderController#getNoAuditOrder
     */
    @Test
    void getNoAuditOrderTestWhenDataExists() throws Exception {
        int size = 10;
        List<Order> mockOrderList = getMockOrderList(size);
        Page<Order> mockOrderPage = new PageImpl<>(mockOrderList);
        List<OrderVo> mockOrderVoList = getMockOrderVoList(mockOrderList);
        when(orderService.findNoAuditOrder(any(Pageable.class)))
                .thenReturn(mockOrderPage);
        when(orderVoService.returnVo(any(List.class)))
                .thenReturn(mockOrderVoList);

        mockMvc.perform(get("/admin/getOrderList.do")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(size)));

        verify(orderService,times(1)).findNoAuditOrder(any(Pageable.class));
        verify(orderVoService,times(1)).returnVo(any(List.class));
    }
    /**
     * 当未审查order数据存在，并且参数Page超过页数上限时
     * 测时getNoAuditOrder方法
     * @see AdminOrderController#getNoAuditOrder
     */
    @Test
    void getNoAuditOrderTestWhenPageOverLimit() throws Exception {
        int size = 0;
        List<Order> mockOrderList = getMockOrderList(size);
        Page<Order> mockOrderPage = new PageImpl<>(mockOrderList);
        List<OrderVo> mockOrderVoList = getMockOrderVoList(mockOrderList);
        when(orderService.findNoAuditOrder(any(Pageable.class)))
                .thenReturn(mockOrderPage);
        when(orderVoService.returnVo(any(List.class)))
                .thenReturn(mockOrderVoList);

        mockMvc.perform(get("/admin/getOrderList.do")
                        .param("page", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(orderService,times(1)).findNoAuditOrder(any(Pageable.class));
        verify(orderVoService,times(1)).returnVo(any(List.class));
    }

    /**
     * 当未审查order数据不存在并且参数Page正确时，测试getNoAuditOrder方法
     * @see AdminOrderController#getNoAuditOrder
     */
    @Test
    void getNoAuditOrderTestWhenNoData() throws Exception {
        List<Order> mockOrderList = new ArrayList<>();
        Page<Order> mockOrderPage = new PageImpl<>(mockOrderList);
        List<OrderVo> mockOrderVoList = getMockOrderVoList(mockOrderList);
        when(orderService.findNoAuditOrder(any(Pageable.class)))
                .thenReturn(mockOrderPage);
        when(orderVoService.returnVo(any(List.class)))
                .thenReturn(mockOrderVoList);

        mockMvc.perform(get("/admin/getOrderList.do")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));

        verify(orderService,times(1)).findNoAuditOrder(any(Pageable.class));
        verify(orderVoService,times(1)).returnVo(any(List.class));
    }

    /**
     * 当参数Page错误(Page<=0)时，测试getNoAuditOrder方法
     * 这个测试应该失败并抛出异常 java.lang.IllegalArgumentException: Page index must not be less than zero!
     * @see AdminOrderController#getNoAuditOrder
     */
    @Test
    void getNoAuditOrderTestWhenPageArgumentWrong() throws Exception {
        List<Order> mockOrderList = new ArrayList<>();
        Page<Order> mockOrderPage = new PageImpl<>(mockOrderList);
        List<OrderVo> mockOrderVoList = getMockOrderVoList(mockOrderList);
        when(orderService.findNoAuditOrder(any(Pageable.class)))
                .thenReturn(mockOrderPage);
        when(orderVoService.returnVo(any(List.class)))
                .thenReturn(mockOrderVoList);
        //Page参数小于或等于0会出错，是一个非法等价类
        mockMvc.perform(get("/admin/getOrderList.do")
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(content().json(mockOrderList.toString()));

        verify(orderService,times(1)).findNoAuditOrder(any(Pageable.class));
        verify(orderVoService,times(1)).returnVo(any(List.class));
    }

    /**
     * 当orderId在数据库存在时，测试confirmOrder方法
     * @see AdminOrderController#confirmOrder
     */
    @Test
    void confirmOrderTestWhenIdExists() throws Exception {
        doNothing().when(orderService).confirmOrder(anyInt());

        mockMvc.perform(post("/passOrder.do")
                        .param("orderID", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    /**
     * 当orderId在数据库不存在时，测试confirmOrder方法
     * 这个测试应该抛出异常失败
     * @see AdminOrderController#confirmOrder
     */
    @Test
    void confirmOrderTestWhenIdNotExist() throws Exception {
        doThrow(new IllegalArgumentException("Order ID Not Exists")).when(orderService).confirmOrder(anyInt());

        mockMvc.perform(post("/passOrder.do")
                        .param("orderID", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    /**
     * 当orderId在数据库存在时，测试rejectOrder方法
     * @see AdminOrderController#rejectOrder
     */
    @Test
    void rejectOrderTestWhenIDExists() throws Exception {
        doNothing().when(orderService).rejectOrder(anyInt());

        mockMvc.perform(post("/rejectOrder.do")
                        .param("orderID", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    /**
     * 当orderId在数据库不存在时，测试rejectOrder方法
     * 这个测试应该失败并抛出异常
     * @see AdminOrderController#rejectOrder
     */
    @Test
    void rejectOrderTestWhenIDNotExist() throws Exception {
        doThrow(new IllegalArgumentException("Order ID Not Exists"))
                .when(orderService).rejectOrder(anyInt());

        mockMvc.perform(post("/rejectOrder.do")
                        .param("orderID", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}
