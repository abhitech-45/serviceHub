package com.servicehubai.admin.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.servicehubai.admin.api.AdminController.AdminOverview;
import com.servicehubai.admin.api.AdminController.AdminRequestAction;
import com.servicehubai.audit.application.AuditService;
import com.servicehubai.common.exception.ApiException;
import com.servicehubai.request.api.RequestDtos.Response;
import com.servicehubai.request.domain.RequestPriority;
import com.servicehubai.request.domain.RequestStatus;
import com.servicehubai.request.infrastructure.RequestCommentRepository;
import com.servicehubai.request.infrastructure.ServiceRequestRepository;
import com.servicehubai.request.infrastructure.RequestStatusHistoryRepository;
import com.servicehubai.request.domain.RequestStatusHistoryEntity;
import com.servicehubai.request.application.RequestLifecyclePublisher;
import com.servicehubai.user.infrastructure.UserRepository;
import com.servicehubai.user.domain.Role;
import com.servicehubai.admin.domain.AdminRequestActionEntity;
import com.servicehubai.admin.infrastructure.AdminRequestActionRepository;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final ServiceRequestRepository requestRepository;
    private final RequestCommentRepository commentRepository;
    private final AuditService auditService;
    private final RequestStatusHistoryRepository historyRepository;
    private final RequestLifecyclePublisher lifecyclePublisher;
    private final AdminRequestActionRepository adminActionRepository;

    public AdminService(UserRepository userRepository, ServiceRequestRepository requestRepository,
            RequestCommentRepository commentRepository, AuditService auditService,
            RequestStatusHistoryRepository historyRepository, RequestLifecyclePublisher lifecyclePublisher,
            AdminRequestActionRepository adminActionRepository) {
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.commentRepository = commentRepository;
        this.auditService = auditService;
        this.historyRepository = historyRepository;
        this.lifecyclePublisher = lifecyclePublisher;
        this.adminActionRepository = adminActionRepository;
    }

    @Transactional(readOnly = true)
    public AdminOverview overview() {
        return new AdminOverview(
                userRepository.count(),
                requestRepository.count(),
                requestRepository.countByStatusNot(RequestStatus.CLOSED),
                requestRepository.countByPriorityIn(java.util.List.of(RequestPriority.HIGH, RequestPriority.CRITICAL)));
    }

        @Transactional(readOnly = true)
        public java.util.List<Response> requests() {
        return requestRepository.findAll().stream()
            .sorted(java.util.Comparator.comparing(com.servicehubai.request.domain.ServiceRequestEntity::getCreatedAt).reversed())
            .map(request -> response(request))
            .toList();
        }

        @Transactional(readOnly = true)
        public java.util.List<com.servicehubai.admin.api.AdminController.SupportAgent> supportAgents() {
        return userRepository.findByActiveTrueAndRolesContainingOrderByDisplayNameAsc(Role.SUPPORT_AGENT).stream()
            .map(agent -> new com.servicehubai.admin.api.AdminController.SupportAgent(agent.getEmail(), agent.getDisplayName()))
            .toList();
        }

        @Transactional
        public Response updateRequest(String actorEmail, String reference, AdminRequestAction action) {
        var request = requestRepository.findByReference(reference)
            .orElseThrow(() -> new ApiException(org.springframework.http.HttpStatus.NOT_FOUND,
                "Request not found", "The request does not exist."));
        var assignee = action.assigneeEmail() == null || action.assigneeEmail().isBlank()
            ? null
            : userRepository.findByEmailIgnoreCase(action.assigneeEmail().trim())
                .filter(agent -> agent.isActive() && agent.getRoles().contains(Role.SUPPORT_AGENT))
                .orElseThrow(() -> new ApiException(org.springframework.http.HttpStatus.NOT_FOUND,
                "Assignee not found", "The selected support agent does not exist or is not an active support agent."));
        request.adminUpdate(action.status(), action.priority(), assignee,
            blankToNull(action.resolutionNotes()));
        var saved = requestRepository.save(request);
        adminActionRepository.save(new AdminRequestActionEntity(reference, actorEmail, action.status(), action.priority(),
            assignee == null ? null : assignee.getEmail(), blankToNull(action.remarks()), blankToNull(action.resolutionNotes())));
        historyRepository.save(new RequestStatusHistoryEntity(saved, action.status(), actorEmail,
            blankToNull(action.remarks())));
        auditService.record(actorEmail, "ADMIN_REQUEST_UPDATED:" + reference, "SUCCESS");
        Response response = response(saved);
        lifecyclePublisher.publish(reference, response);
        return response;
        }

        private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
        }

        private Response response(com.servicehubai.request.domain.ServiceRequestEntity request) {
        return Response.from(request, commentRepository.findAll().stream()
            .filter(comment -> comment.getRequestReference().equals(request.getReference())).toList(),
            historyRepository.findByRequestReferenceOrderByOccurredAtAsc(request.getReference()));
        }
}
