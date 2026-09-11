package com.hareeshvar.attendance.controller;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hareeshvar.attendance.dto.response.AuditLogResponseDTO;
import com.hareeshvar.attendance.dto.response.PageResponse;
import com.hareeshvar.attendance.enums.AuditAction;
import com.hareeshvar.attendance.enums.AuditResult;
import com.hareeshvar.attendance.service.AuditLogService;

@SpringBootTest
@AutoConfigureMockMvc
class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditLogService auditLogService;

    @org.junit.jupiter.api.Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAuditLogs_AsAdmin_ShouldReturn200() throws Exception {
        AuditLogResponseDTO dto = AuditLogResponseDTO.builder()
                .id(1L)
                .actorUsername("admin")
                .actorRole("ADMIN")
                .action(AuditAction.USER_CREATED)
                .entityType("USER")
                .entityId("42")
                .description("Created user")
                .timestamp(LocalDateTime.now())
                .result(AuditResult.SUCCESS)
                .build();

        PageResponse<AuditLogResponseDTO> pageResponse = PageResponse.<AuditLogResponseDTO>builder()
                .content(List.of(dto))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();

        when(auditLogService.getAuditLogs(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/audit-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].actorUsername").value("admin"))
                .andExpect(jsonPath("$.content[0].action").value("USER_CREATED"));
    }

    @org.junit.jupiter.api.Test
    @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
    void getAuditLogs_AsEmployee_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs"))
                .andExpect(status().isForbidden());
    }

    @org.junit.jupiter.api.Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void getAuditLogs_AsManager_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs"))
                .andExpect(status().isForbidden());
    }
}
