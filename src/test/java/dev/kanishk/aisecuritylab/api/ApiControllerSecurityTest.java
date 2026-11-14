package dev.kanishk.aisecuritylab.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ApiControllerSecurityTest {
    @Autowired
    MockMvc mvc;

    @Test
    @WithMockUser(username = "lab-customer", roles = "USER")
    void customerCanReadOwnOrder() throws Exception {
        mvc.perform(get("/api/orders/1001")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "lab-customer", roles = "USER")
    void customerCannotReadAnotherCustomersOrder() throws Exception {
        mvc.perform(get("/api/orders/1002")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "lab-admin", roles = {"USER", "ADMIN"})
    void adminCanReadAnyOrder() throws Exception {
        mvc.perform(get("/api/orders/1002")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "lab-customer", roles = "USER")
    void customerCannotCreateProducts() throws Exception {
        mvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test\",\"priceInPaise\":100,\"stock\":1}"))
            .andExpect(status().isForbidden());
    }
}
