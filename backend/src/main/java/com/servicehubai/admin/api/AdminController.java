package com.servicehubai.admin.api;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.servicehubai.admin.application.AdminService;
import com.servicehubai.request.api.RequestDtos.Response;
import com.servicehubai.request.domain.RequestPriority;
import com.servicehubai.request.domain.RequestStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/overview")
    AdminOverview overview() {
        return adminService.overview();
    }

    @GetMapping("/requests")
    List<Response> requests() {
        return adminService.requests();
    }

    @GetMapping("/support-agents")
    List<SupportAgent> supportAgents() {
        return adminService.supportAgents();
    }

    @PatchMapping("/requests/{reference}")
    Response updateRequest(Authentication authentication, @PathVariable String reference,
            @Valid @RequestBody AdminRequestAction action) {
        return adminService.updateRequest(authentication.getName(), reference, action);
    }

    public record AdminOverview(long totalUsers, long totalRequests, long openRequests, long highPriorityRequests,
            long totalAiCalls, long successfulAiCalls, long timedOutAiCalls) {
    }

    public record AdminRequestAction(@NotNull RequestStatus status, @NotNull RequestPriority priority,
            String assigneeEmail, String remarks, String resolutionNotes) {
    }

    public record SupportAgent(String email, String displayName) {
    }
}
