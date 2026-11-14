package dev.kanishk.aisecuritylab.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.kanishk.aisecuritylab.security.ClamAvScanner;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ApiControllerSecurityTest {
    @Autowired
    MockMvc mvc;

    @MockBean
    ClamAvScanner clamAvScanner;

    @Test
    void dashboardIsPublicButApiRequiresAuthentication() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
        mvc.perform(get("/api/products")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "lab-customer", roles = "USER")
    void customerCanReadOwnOrder() throws Exception {
        mvc.perform(get("/api/orders/1001")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "lab-customer", roles = "USER")
    void customerCanReadAnotherCustomersOrderInVulnerableVersion() throws Exception {
        mvc.perform(get("/api/orders/1002")).andExpect(status().isOk());
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

    @Test
    @WithMockUser(username = "lab-customer", roles = "USER")
    void cleanUploadIsAcceptedAfterClamAvScan() throws Exception {
        when(clamAvScanner.scan(any(byte[].class)))
            .thenReturn(new ClamAvScanner.ScanResult(true, "CLEAN"));
        var file = new MockMultipartFile("file", "notes.txt", "text/plain", "safe notes".getBytes());
        mvc.perform(multipart("/api/uploads").file(file)).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "lab-customer", roles = "USER")
    void infectedUploadIsRejected() throws Exception {
        when(clamAvScanner.scan(any(byte[].class)))
            .thenReturn(new ClamAvScanner.ScanResult(false, "INFECTED"));
        var file = new MockMultipartFile("file", "notes.txt", "text/plain", "test payload".getBytes());
        mvc.perform(multipart("/api/uploads").file(file)).andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser(username = "lab-customer", roles = "USER")
    void uploadFailsClosedWhenClamAvIsUnavailable() throws Exception {
        when(clamAvScanner.scan(any(byte[].class))).thenThrow(new IOException("offline"));
        var file = new MockMultipartFile("file", "notes.txt", "text/plain", "safe notes".getBytes());
        mvc.perform(multipart("/api/uploads").file(file)).andExpect(status().isServiceUnavailable());
    }
}
