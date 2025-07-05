package com.gogidix.socialcommerce.${SERVICE}.controller;

import com.gogidix.socialcommerce.${SERVICE}.service.${SERVICE_CLASS}Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ${SERVICE_CLASS}ControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ${SERVICE_CLASS}Service ${SERVICE_VAR}Service;

    @InjectMocks
    private ${SERVICE_CLASS}Controller ${SERVICE_VAR}Controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(${SERVICE_VAR}Controller).build();
    }

    @Test
    void testGetAll${SERVICE_CLASS}s() throws Exception {
        mockMvc.perform(get("/api/${SERVICE_PATH}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        
        verify(${SERVICE_VAR}Service, times(1)).findAll();
    }

    @Test
    void testGet${SERVICE_CLASS}ById() throws Exception {
        Long id = 1L;
        
        mockMvc.perform(get("/api/${SERVICE_PATH}/{id}", id))
                .andExpect(status().isOk());
        
        verify(${SERVICE_VAR}Service, times(1)).findById(id);
    }

    @Test
    void testCreate${SERVICE_CLASS}() throws Exception {
        String json = "{}";
        
        mockMvc.perform(post("/api/${SERVICE_PATH}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    void testUpdate${SERVICE_CLASS}() throws Exception {
        Long id = 1L;
        String json = "{}";
        
        mockMvc.perform(put("/api/${SERVICE_PATH}/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void testDelete${SERVICE_CLASS}() throws Exception {
        Long id = 1L;
        
        mockMvc.perform(delete("/api/${SERVICE_PATH}/{id}", id))
                .andExpect(status().isNoContent());
        
        verify(${SERVICE_VAR}Service, times(1)).deleteById(id);
    }
}
