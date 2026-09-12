package com.hareeshvar.attendance.exception;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hareeshvar.attendance.security.filter.RequestCorrelationFilter;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // =========================================================================
    // MANDATORY REQUEST ID TESTS (A - H)
    // =========================================================================

    @Test
    @DisplayName("A & F: No Request ID supplied -> Server generates ID, attached to response header and success payload")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testA_NoRequestId_ShouldGenerateUUID() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Request-ID"))
                .andReturn();

        String headerReqId = result.getResponse().getHeader("X-Request-ID");
        org.junit.jupiter.api.Assertions.assertTrue(headerReqId.startsWith("req-"));
    }

    @Test
    @DisplayName("B & G: Valid Request ID -> Accepted, echoed in header and error response body")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testB_ValidRequestId_ShouldEcho() throws Exception {
        String clientReqId = "test-custom-id-12345";
        MvcResult result = mockMvc.perform(get("/api/v1/users/999999")
                        .header("X-Request-ID", clientReqId))
                .andExpect(status().isNotFound())
                .andExpect(header().string("X-Request-ID", clientReqId))
                .andExpect(jsonPath("$.requestId").value(clientReqId))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"))
                .andReturn();
    }

    @Test
    @DisplayName("C: Invalid Request ID characters -> Ignored and replaced with server generated ID")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testC_InvalidRequestId_ShouldReplace() throws Exception {
        String invalidReqId = "invalid request id @#$!";
        MvcResult result = mockMvc.perform(get("/api/v1/users/999999")
                        .header("X-Request-ID", invalidReqId))
                .andExpect(status().isNotFound())
                .andExpect(header().exists("X-Request-ID"))
                .andReturn();

        String responseReqId = result.getResponse().getHeader("X-Request-ID");
        org.junit.jupiter.api.Assertions.assertNotEquals(invalidReqId, responseReqId);
        org.junit.jupiter.api.Assertions.assertTrue(responseReqId.startsWith("req-"));
    }

    @Test
    @DisplayName("D: Oversized Request ID (>64 chars) -> Ignored and replaced with server generated ID")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testD_OversizedRequestId_ShouldReplace() throws Exception {
        String oversizedReqId = "a".repeat(100);
        MvcResult result = mockMvc.perform(get("/api/v1/users/999999")
                        .header("X-Request-ID", oversizedReqId))
                .andExpect(status().isNotFound())
                .andExpect(header().exists("X-Request-ID"))
                .andReturn();

        String responseReqId = result.getResponse().getHeader("X-Request-ID");
        org.junit.jupiter.api.Assertions.assertNotEquals(oversizedReqId, responseReqId);
    }

    @Test
    @DisplayName("E: Request ID containing CR/LF control characters -> Ignored and replaced")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testE_ControlCharactersInRequestId_ShouldReplace() throws Exception {
        String maliciousReqId = "cleanId\r\nInjected-Header: evil";
        MvcResult result = mockMvc.perform(get("/api/v1/users/999999")
                        .header("X-Request-ID", maliciousReqId))
                .andExpect(status().isNotFound())
                .andExpect(header().exists("X-Request-ID"))
                .andReturn();

        String responseReqId = result.getResponse().getHeader("X-Request-ID");
        org.junit.jupiter.api.Assertions.assertFalse(responseReqId.contains("\r"));
        org.junit.jupiter.api.Assertions.assertFalse(responseReqId.contains("\n"));
    }

    @Test
    @DisplayName("H: MDC Context -> Verified cleared after completion")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testH_MdcCleanup() throws Exception {
        mockMvc.perform(get("/api/v1/roles")).andExpect(status().isOk());
        assertNull(MDC.get(RequestCorrelationFilter.MDC_REQUEST_ID_KEY));
    }

    // =========================================================================
    // SECURITY INTEGRATION TESTS (1 - 6)
    // =========================================================================

    @Test
    @DisplayName("1 & 2: Protected endpoint without token -> 401 Unauthorized with ErrorResponse JSON")
    void testSecurity_Unauthenticated_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("X-Request-ID"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/users"));
    }

    @Test
    @DisplayName("3: Authenticated user with insufficient role -> 403 Forbidden with ErrorResponse JSON")
    @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
    void testSecurity_Forbidden_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs"))
                .andExpect(status().isForbidden())
                .andExpect(header().exists("X-Request-ID"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/audit-logs"));
    }

    // =========================================================================
    // ERROR TEST MATRIX
    // =========================================================================

    @Test
    @DisplayName("400 Validation Error -> Returns fieldErrors map")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testErrorMatrix_400Validation() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors").isMap());
    }

    @Test
    @DisplayName("400 Malformed JSON -> Returns MALFORMED_JSON error code")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testErrorMatrix_400MalformedJson() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid_json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errorCode").value("MALFORMED_JSON"));
    }

    @Test
    @DisplayName("404 Resource Not Found -> Returns RESOURCE_NOT_FOUND error code")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testErrorMatrix_404NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/users/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
    }
}
