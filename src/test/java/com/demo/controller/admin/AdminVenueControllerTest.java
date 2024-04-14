package com.demo.controller.admin;

import com.demo.entity.Venue;
import com.demo.service.VenueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminVenueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VenueService venueService;

    // Utility methods or setup data
    private Venue createTestVenue(int id) {
        return new Venue(
                id,                                // venueID
                "Venue " + id,                     // venueName
                "Description for Venue " + id,     // description
                id * 100,                          // price
                "Picture" + id + ".png",           // picture
                "Address " + id,                   // address
                "09:00",                           // open_time
                "21:00"                            // close_time
        );
    }
    @Test
    public void testVenueManage() throws Exception {
        List<Venue> venues = IntStream.range(1, 11).mapToObj(this::createTestVenue).collect(Collectors.toList());
        Page<Venue> page = new PageImpl<>(venues);
        when(venueService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/venue_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/venue_manage"))
                .andExpect(model().attribute("total", page.getTotalPages()))
                .andDo(print());

        verify(venueService).findAll(any(Pageable.class));
    }
    /**
     * 管理员编辑场馆成功
     */
    @Test
    public void testEditVenue() throws Exception {
        int venueID = 1;
        Venue venue = createTestVenue(venueID);
        when(venueService.findByVenueID(venueID)).thenReturn(venue);

        mockMvc.perform(get("/venue_edit").param("venueID", String.valueOf(venueID)))
                .andExpect(status().isOk())
                .andExpect(view().name("/admin/venue_edit"))
                .andExpect(model().attribute("venue", venue))
                .andDo(print());

        verify(venueService).findByVenueID(venueID);
    }
    /**
     * 管理员编辑场馆失败
     */
    @Test
    public void testEditVenue_WhenVenueIDNotExistThenReturn404() throws Exception {
        int venueID = 9999;

        // Assume the venue does not exist
        when(venueService.findByVenueID(venueID)).thenReturn(null);

        mockMvc.perform(get("/venue_edit").param("venueID", String.valueOf(venueID)))
                .andExpect(status().isNotFound())  // Expecting a 404 Not Found response for a non-existent venue
                .andDo(print());

        verify(venueService).findByVenueID(venueID);  // Verify the service was queried for the correct ID
    }

    /**
     * 管理员获取场馆列表
     */
    @Test
    public void testGetVenueList() throws Exception {
        List<Venue> venues = IntStream.range(1, 11).mapToObj(this::createTestVenue).collect(Collectors.toList());
        Page<Venue> page = new PageImpl<>(venues);
        when(venueService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/venueList.do").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)))
                .andDo(print());

        verify(venueService).findAll(any(Pageable.class));
    }
    /**
     * 管理员添加商品成功
     */
    @Test
    public void testAddVenue() throws Exception {
        MockMultipartFile file = new MockMultipartFile("picture", "filename.png", "image/png", "some image data".getBytes());

        Venue dummyVenue = new Venue(1, "New Venue", "A new venue description", 100, "filename.png", "123 Venue St", "09:00", "21:00");
        when(venueService.create(any(Venue.class))).thenReturn(1);  // Assume creation is successful and returns a positive ID

        mockMvc.perform(multipart("/addVenue.do")
                        .file(file)
                        .param("venueName", "New Venue")
                        .param("address", "123 Venue St")
                        .param("description", "A new venue")
                        .param("price", "100")
                        .param("open_time", "09:00")
                        .param("close_time", "21:00"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("venue_manage"));

        verify(venueService).create(any(Venue.class));
    }

    /**
     * 管理员添加商品失败
     * 服务层返回-1
     */
    @Test
    public void testAddVenueFailure() throws Exception {
        MockMultipartFile file = new MockMultipartFile("picture", "filename.png", "image/png", "some image data".getBytes());
        when(venueService.create(any(Venue.class))).thenReturn(0);  // Simulate failure

        mockMvc.perform(multipart("/addVenue.do")
                        .file(file)
                        .param("venueName", "New Venue")
                        .param("address", "123 Venue St")
                        .param("description", "A new venue")
                        .param("price", "100")
                        .param("open_time", "09:00")
                        .param("close_time", "21:00"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("venue_add"));

        verify(venueService).create(any(Venue.class));
    }
    /**
     * 管理员删除商品成功
     */
    @Test
    public void testDelVenueSuccess() throws Exception {
        int venueID = 1;
        doNothing().when(venueService).delById(venueID);

        mockMvc.perform(post("/delVenue.do")
                        .param("venueID", String.valueOf(venueID)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(venueService).delById(venueID);
    }

    /**
     * 管理员删除商品失败
     */
    @Test
    public void testDelVenue_WhenVenueIDNotExistThenReturn404() throws Exception {
        int venueID = 9999;

        // Setup the scenario where the ID does not exist
        doThrow(new RuntimeException("Venue does not exist")).when(venueService).delById(venueID);

        mockMvc.perform(post("/delVenue.do").param("venueID", String.valueOf(venueID)))
                .andExpect(status().isNotFound())  // We expect a 404 Not Found response if the venue does not exist
                .andDo(print());

        verify(venueService).delById(venueID);  // Ensure the method was called with the correct ID
    }

    /**
     * 管理员编辑商品内容成功
     */
    @Test
    public void testModifyVenue_Success() throws Exception {
        int venueID = 1;
        MockMultipartFile file = new MockMultipartFile("picture", "filename.png", "image/png", "some image data".getBytes());
        Venue updatedVenue = new Venue(venueID, "Updated Venue", "Updated Description", 200, "updatedPicture.png", "Updated Address", "10:00", "22:00");

        when(venueService.findByVenueID(venueID)).thenReturn(updatedVenue);
        doNothing().when(venueService).update(any(Venue.class));

        mockMvc.perform(multipart("/modifyVenue.do")
                        .file(file)
                        .param("venueID", String.valueOf(venueID))
                        .param("venueName", updatedVenue.getVenueName())
                        .param("address", updatedVenue.getAddress())
                        .param("description", updatedVenue.getDescription())
                        .param("price", String.valueOf(updatedVenue.getPrice()))
                        .param("open_time", updatedVenue.getOpen_time())
                        .param("close_time", updatedVenue.getClose_time()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("venue_manage"));

        verify(venueService).update(any(Venue.class));
    }
    /**
     * 管理员编辑商品内容失败
     * 有内容为空
     */
    @Test
    public void testModifyVenue_FailureDueToIncompleteData() throws Exception {
        int venueID = 1;
        Venue existingVenue = new Venue(venueID, "Existing Venue", "Existing Description", 100, "existingPicture.png", "Existing Address", "09:00", "21:00");

        // Ensuring that a valid Venue object is returned to avoid NullPointerException when attempting to set properties on it.
        when(venueService.findByVenueID(venueID)).thenReturn(existingVenue);

        MockMultipartFile file = new MockMultipartFile("picture", "", "image/png", new byte[0]);  // Simulating an empty file

        mockMvc.perform(multipart("/modifyVenue.do")
                        .file(file)
                        .param("venueID", String.valueOf(venueID))
                        .param("venueName", "")  // Missing venue name intentionally to simulate validation failure
                        .param("address", existingVenue.getAddress())
                        .param("description", existingVenue.getDescription())
                        .param("price", String.valueOf(existingVenue.getPrice()))
                        .param("open_time", existingVenue.getOpen_time())
                        .param("close_time", existingVenue.getClose_time()))
                .andExpect(status().isBadRequest());  // Expecting a bad request status due to validation failure

        verify(venueService, never()).update(any(Venue.class));  // Ensuring update is not called due to failure in validation
    }
    /**
     * 管理员添加商品失败
     * 有内容为空
     */
    @Test
    public void testAddVenue_FailureDueToIncompleteData() throws Exception {
        MockMultipartFile file = new MockMultipartFile("picture", "test-image.png", "image/png", new byte[0]);  // Simulating an empty picture file which is considered valid for demonstration

        mockMvc.perform(multipart("/addVenue.do")
                        .file(file)
                        .param("venueName", "")  // Missing venue name
                        .param("address", "123 Venue St")
                        .param("description", "")  // Missing description, critical for venue details
                        .param("price", "100")
                        .param("open_time", "10:00")
                        .param("close_time", "22:00"))
                .andExpect(status().isBadRequest());  // Expecting a bad request status due to validation failure

        verify(venueService, never()).create(any(Venue.class));  // No create operation should be called
    }


}
