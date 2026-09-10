package com.servicehubai.request.api;

import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.servicehubai.request.domain.RequestPriority;
import com.servicehubai.request.domain.RequestStatus;
import com.servicehubai.request.domain.RequestStatusHistoryEntity;
import com.servicehubai.request.domain.ServiceRequestEntity;
import com.servicehubai.request.domain.RequestCommentEntity;

public final class RequestDtos {

    private RequestDtos() {
    }

    public record CreateRequest(
            @NotBlank @Size(max = 160) String subject,
            @NotBlank @Size(max = 5000) String description,
            @NotBlank @Size(max = 80) String category,
            @NotBlank @Size(max = 100) String subCategory,
            @NotNull RequestPriority priority) {
    }

    public record UpdateRequest(
            @NotBlank @Size(max = 160) String subject,
            @NotBlank @Size(max = 5000) String description,
            @NotBlank @Size(max = 80) String category,
            @NotBlank @Size(max = 100) String subCategory,
            @NotNull RequestPriority priority) {
    }

    public record CommentRequest(@NotBlank @Size(max = 3000) String body) {
    }

    public record HistoryResponse(RequestStatus status, Instant occurredAt, String updatedBy, String remarks) {
        public static HistoryResponse from(RequestStatusHistoryEntity history) {
            return new HistoryResponse(history.getStatus(), history.getOccurredAt(), history.getUpdatedBy(), history.getRemarks());
        }
    }

    public record CommentResponse(String authorEmail, String body, Instant createdAt) {
        public static CommentResponse from(RequestCommentEntity comment) {
            return new CommentResponse(comment.getAuthorEmail(), comment.getBody(), comment.getCreatedAt());
        }
    }

    public record Response(String reference, String subject, String description, String category, String subCategory,
            RequestPriority priority, RequestStatus status, String ownerEmail, Instant createdAt, Instant updatedAt,
            String assigneeEmail, String resolutionNotes, List<CommentResponse> comments,
            List<HistoryResponse> history) {
        public static Response from(ServiceRequestEntity request, List<RequestCommentEntity> comments,
                List<RequestStatusHistoryEntity> history) {
                return new Response(request.getReference(), request.getSubject(), request.getDescription(),
                    request.getCategory(), request.getSubCategory(), request.getPriority(), request.getStatus(), request.getOwner().getEmail(),
                    request.getCreatedAt(), request.getUpdatedAt(),
                    request.getAssignee() == null ? null : request.getAssignee().getEmail(), request.getResolutionNotes(),
                    comments.stream().map(CommentResponse::from).toList(), history.stream().map(HistoryResponse::from).toList());
        }
    }
}
