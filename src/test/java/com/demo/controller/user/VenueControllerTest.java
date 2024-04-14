package com.demo.controller.user;

import com.demo.entity.Venue;
import com.demo.service.VenueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class VenueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VenueService venueService;

    private List<Venue> mockVenueList(int size) {
        List<Venue> venueList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Venue venue = new Venue(i, "Venue " + i, "Description " + i, i * 100, "image" + i + ".png", "Address " + i, "08:00", "22:00");
            venueList.add(venue);
        }
        return venueList;
    }

    /**
     * 测试场馆详情页面成功的场景
     */
    @Test
    public void testVenueDetail() throws Exception {
        Venue venue = new Venue(1, "Venue 1", "Description 1", 100, "image1.png", "Address 1", "08:00", "22:00");

        when(venueService.findByVenueID(1)).thenReturn(venue);

        mockMvc.perform(get("/venue").param("venueID", "1"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("venue", hasProperty("venueName", is("Venue 1"))))
                .andExpect(view().name("venue"))
                .andDo(print());

        verify(venueService).findByVenueID(1);
    }

    /**
     * 测试场馆详情页面失败的场景：场馆ID不存在
     */
    @Test
    public void testVenueDetail_NotFound() throws Exception {
        int nonExistentId = 999;
        when(venueService.findByVenueID(nonExistentId)).thenReturn(null);

        mockMvc.perform(get("/venue").param("venueID", String.valueOf(nonExistentId)))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("venue"))
                .andExpect(view().name("venue"))
                .andDo(print());

        verify(venueService).findByVenueID(nonExistentId);
    }

    /**
     * 测试分页查看场馆成功的场景
     */
    @Test
    public void testVenueListPaged() throws Exception {
        List<Venue> venueList = mockVenueList(5);
        Pageable pageable = PageRequest.of(0, 5, Sort.by("venueID").ascending());
        Page<Venue> page = new PageImpl<>(venueList, pageable, venueList.size());

        when(venueService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/venuelist/getVenueList").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.content[0].venueName", is("Venue 0")))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andDo(print());

        verify(venueService).findAll(any(Pageable.class));
    }

    /**
     * 测试查看场馆列表成功的场景
     */
    @Test
    public void testFullVenueList() throws Exception {
        List<Venue> venueList = mockVenueList(5);
        Pageable pageable = PageRequest.of(0, 5, Sort.by("venueID").ascending());
        Page<Venue> page = new PageImpl<>(venueList, pageable, 5);

        when(venueService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/venue_list"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("venue_list", hasSize(5)))
                .andExpect(model().attribute("total", is(1)))
                .andExpect(view().name("venue_list"))
                .andDo(print());

        verify(venueService, times(2)).findAll(any(Pageable.class)); // Called twice in the same method
    }
}
