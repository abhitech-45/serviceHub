package com.servicehubai.request.application;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.servicehubai.audit.application.AuditService;
import com.servicehubai.common.exception.ApiException;
import com.servicehubai.request.api.RequestDtos.CommentRequest;
import com.servicehubai.request.api.RequestDtos.CreateRequest;
import com.servicehubai.request.api.RequestDtos.Response;
import com.servicehubai.request.api.RequestDtos.UpdateRequest;
import com.servicehubai.request.domain.RequestCommentEntity;
import com.servicehubai.request.domain.ServiceRequestEntity;
import com.servicehubai.request.infrastructure.RequestCommentRepository;
import com.servicehubai.request.infrastructure.ServiceRequestRepository;
import com.servicehubai.request.infrastructure.RequestStatusHistoryRepository;
import com.servicehubai.request.domain.RequestStatusHistoryEntity;
import com.servicehubai.request.domain.CampusServiceCatalog;
import com.servicehubai.user.domain.UserEntity;
import com.servicehubai.user.infrastructure.UserRepository;

@Service
public class RequestService {

    private final ServiceRequestRepository requestRepository;
    private final RequestCommentRepository commentRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final RequestStatusHistoryRepository historyRepository;
    private final RequestLifecyclePublisher lifecyclePublisher;

    public RequestService(ServiceRequestRepository requestRepository, RequestCommentRepository commentRepository,
            UserRepository userRepository, AuditService auditService, RequestStatusHistoryRepository historyRepository,
            RequestLifecyclePublisher lifecyclePublisher) {
        this.requestRepository = requestRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.historyRepository = historyRepository;
        this.lifecyclePublisher = lifecyclePublisher;
    }

    @Transactional
    public Response create(String email, CreateRequest input) {
        UserEntity owner = user(email);
        String reference = "SR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        validateCategory(input.category(), input.subCategory());
        ServiceRequestEntity request = requestRepository.save(new ServiceRequestEntity(reference, input.subject().trim(),
            input.description().trim(), input.category().trim(), input.subCategory().trim(), input.priority(), owner));
        historyRepository.save(new RequestStatusHistoryEntity(request, request.getStatus(), email, "Request submitted"));
        auditService.record(email, "REQUEST_CREATED:" + reference, "SUCCESS");
        return response(request);
    }

    @Transactional(readOnly = true)
    public List<Response> list(String email) {
        return requestRepository.findByOwnerEmailIgnoreCaseOrderByCreatedAtDesc(email).stream()
                .map(this::response)
                .toList();
    }

    @Transactional(readOnly = true)
    public Response get(String email, String reference) {
        return response(request(email, reference));
    }

    @Transactional
    public Response update(String email, String reference, UpdateRequest input) {
        ServiceRequestEntity request = request(email, reference);
        if (request.getStatus() != com.servicehubai.request.domain.RequestStatus.OPEN
                && request.getStatus() != com.servicehubai.request.domain.RequestStatus.PENDING_CUSTOMER) {
            throw new ApiException(HttpStatus.CONFLICT, "Request is not editable", "Only open or pending-customer requests can be updated.");
        }
        validateCategory(input.category(), input.subCategory());
        request.update(input.subject().trim(), input.description().trim(), input.category().trim(), input.subCategory().trim(), input.priority());
        auditService.record(email, "REQUEST_UPDATED:" + reference, "SUCCESS");
        Response response = response(requestRepository.save(request));
        lifecyclePublisher.publish(reference, response);
        return response;
    }

    @Transactional
    public Response addComment(String email, String reference, CommentRequest input) {
        ServiceRequestEntity request = request(email, reference);
        UserEntity author = user(email);
        commentRepository.save(new RequestCommentEntity(request, author, input.body().trim()));
        auditService.record(email, "REQUEST_COMMENTED:" + reference, "SUCCESS");
        Response response = response(request);
        lifecyclePublisher.publish(reference, response);
        return response;
    }

    private ServiceRequestEntity request(String email, String reference) {
        return requestRepository.findByReferenceAndOwnerEmailIgnoreCase(reference, email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Request not found", "The request does not exist or is not accessible."));
    }

    private UserEntity user(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found", "The authenticated user no longer exists."));
    }

    private List<RequestCommentEntity> comments(ServiceRequestEntity request) {
        return commentRepository.findAll().stream()
                .filter(comment -> comment.getRequestReference().equals(request.getReference()))
                .toList();
    }

    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter subscribe(String email, String reference) {
        request(email, reference);
        return lifecyclePublisher.subscribe(reference);
    }

    public Response response(ServiceRequestEntity request) {
        return Response.from(request, comments(request), historyRepository.findByRequestReferenceOrderByOccurredAtAsc(request.getReference()));
    }

    private void validateCategory(String category, String subCategory) {
        if (!CampusServiceCatalog.contains(category.trim(), subCategory.trim())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid service category",
                    "Choose a valid campus service category and sub-category.");
        }
    }
}
