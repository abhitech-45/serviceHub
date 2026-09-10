package com.servicehubai.chat.application;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.servicehubai.chat.api.ChatDtos.Message;
import com.servicehubai.chat.api.ChatDtos.Reply;
import com.servicehubai.chat.domain.ChatMessageEntity;
import com.servicehubai.chat.domain.ChatSessionEntity;
import com.servicehubai.chat.infrastructure.ChatMessageRepository;
import com.servicehubai.chat.infrastructure.ChatSessionRepository;
import com.servicehubai.request.api.RequestDtos.CreateRequest;
import com.servicehubai.request.api.RequestDtos.Response;
import com.servicehubai.request.application.RequestService;
import com.servicehubai.request.domain.RequestPriority;
import com.servicehubai.user.domain.UserEntity;
import com.servicehubai.user.infrastructure.UserRepository;

@Service
public class StudentChatService {

    private static final String CONFIRM_PREFIX = "confirm create:";
    private static final DateTimeFormatter CHAT_DATE = DateTimeFormatter.ofPattern("dd MMM yyyy")
            .withZone(ZoneId.systemDefault());
    private final RequestService requestService;
    private final ObjectProvider<ChatClient> chatClient;
    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final Map<UUID, PendingCreate> pendingCreates = new ConcurrentHashMap<>();

    public StudentChatService(RequestService requestService, ObjectProvider<ChatClient> chatClient,
            ChatSessionRepository sessionRepository, ChatMessageRepository messageRepository,
            UserRepository userRepository) {
        this.requestService = requestService;
        this.chatClient = chatClient;
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Reply respond(String email, Message input) {
        ChatSessionEntity session = session(input.sessionId(), email);
        String message = input.message().trim();
        String normalized = message.toLowerCase(Locale.ROOT);
        messageRepository.save(new ChatMessageEntity(session, "STUDENT", message));

        if (normalized.startsWith(CONFIRM_PREFIX) || normalized.matches("^(yes|confirm|confirmed|create it|please create it)(:)?( .*)?$")) {
            String details = normalized.startsWith(CONFIRM_PREFIX)
                    ? message.substring(CONFIRM_PREFIX.length()).trim()
                    : message.replaceFirst("(?i)^(yes|confirm|confirmed|create it|please create it)(:)?\\s*", "").trim();
            PendingCreate pending = pendingCreates.remove(session.getId());
            if (pending == null) {
                return finish(session, "GENERAL_ASSISTANCE", null, RequestPriority.MEDIUM,
                        "There is no pending ticket creation to confirm.", false, null, List.of());
            }
                String subject = details.isBlank() ? pending.category() + " support request" : details;
                Response created = requestService.create(email, new CreateRequest(subject, pending.description(),
                    pending.category(), pending.subCategory(), pending.priority()));
                return finish(session, "CREATE_SERVICE_REQUEST", pending.category(), pending.priority(),
                    "Your support case " + created.reference() + " was created.", false, created, List.of());
        }

        String intent = intent(normalized);
        String category = category(normalized);
        RequestPriority priority = priority(normalized);
        if ("CHECK_TICKET_STATUS".equals(intent)) {
            String reference = message.toUpperCase(Locale.ROOT).replaceAll(".*?(SR-[A-Z0-9-]+).*", "$1");
            if (!reference.startsWith("SR-")) {
                List<Response> matching = requestService.list(email).stream()
                        .filter(request -> request.status() != com.servicehubai.request.domain.RequestStatus.CLOSED)
                        .toList();
                return finish(session, intent, category, priority,
                    matching.isEmpty() ? "You have no open support cases." : formatOpenRequests(matching),
                        false, null, matching);
            }
            Response request = requestService.get(email, reference);
                return finish(session, intent, category, priority, formatOpenRequests(List.of(request)),
                    false, request, List.of());
        }
        if ("CREATE_SERVICE_REQUEST".equals(intent)) {
            pendingCreates.put(session.getId(), new PendingCreate(message, category, subCategory(category), priority));
            return finish(session, intent, category, priority,
                    "I can create a " + category + " support case with " + priority + " priority. Reply with `confirm create:` followed by a short subject to submit it.",
                    true, null, List.of());
        }

        String aiAnswer = openAiAnswer(email, message, session);
        return finish(session, intent, category, priority, aiAnswer == null ? fallback(normalized) : aiAnswer,
                false, null, List.of());
    }

    private String openAiAnswer(String email, String message, ChatSessionEntity session) {
        ChatClient client = chatClient.getIfAvailable();
        if (client == null) return null;
        try {
            return client.prompt()
                    .system("You are Campus Services Hub student support. Answer concisely, use the conversation context, never invent ticket actions, protect private data, and recommend a support case when self-service is insufficient.")
                    .user(history(session) + "\nStudent: " + message)
                    .call()
                    .content();
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private String intent(String message) {
        if (message.matches(".*(create|raise|submit|register|log).*(ticket|request|case|complaint).*")) return "CREATE_SERVICE_REQUEST";
            if (message.matches(".*(sr-[a-z0-9-]+|ticket status|case status|track ticket|track my ticket|my requests|show my requests|open requests|active complaints|active requests).*")) return "CHECK_TICKET_STATUS";
        if (message.matches(".*(not working|cannot|can't|unable|error|issue|problem|incorrect).*")) return "TROUBLESHOOTING";
        if (message.matches(".*(how|when|where|what|documents|timing|apply|download).*")) return "FAQ_QUERY";
        return "GENERAL_ASSISTANCE";
    }

    private ChatSessionEntity session(UUID sessionId, String email) {
        if (sessionId != null) {
            return sessionRepository.findByIdAndUserEmailIgnoreCase(sessionId, email)
                    .orElseThrow(() -> new IllegalArgumentException("Chat session is not accessible."));
        }
        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));
        return sessionRepository.save(new ChatSessionEntity(user));
    }

    private String history(ChatSessionEntity session) {
        return messageRepository.findBySessionIdOrderByTimestampAsc(session.getId()).stream()
                .map(item -> item.getSender() + ": " + item.getMessage())
                .reduce("Conversation so far:", (left, right) -> left + "\n" + right);
    }

    private String category(String message) {
        if (message.contains("admission")) return "Admission Support";
        if (message.contains("scholarship")) return "Scholarship Support";
        if (message.contains("library")) return "Library Services";
        if (message.contains("hostel")) return "Hostel Services";
        if (message.contains("transport") || message.contains("bus")) return "Transport Services";
        if (message.contains("gym") || message.contains("sport")) return "Gym & Sports Center";
        if (message.contains("placement") || message.contains("resume")) return "Placement Cell";
        if (message.contains("fee") || message.contains("payment") || message.contains("refund")) return "Finance & Fees";
        if (message.contains("exam") || message.contains("hall ticket") || message.contains("revaluation")) return "Examination Cell";
        if (message.contains("login") || message.contains("password") || message.contains("wifi") || message.contains("portal")) return "IT Helpdesk";
        if (message.contains("attendance") || message.contains("subject") || message.contains("marks") || message.contains("academic")) return "Academic Support";
        return "General Administration";
    }

    private RequestPriority priority(String message) {
        if (message.contains("urgent") || message.contains("blocked") || message.contains("critical")) return RequestPriority.HIGH;
        return RequestPriority.MEDIUM;
    }

    private String subCategory(String category) {
        return switch (category) {
            case "Admission Support" -> "Admission Status";
            case "Scholarship Support" -> "Scholarship Application";
            case "Library Services" -> "Book Availability Request";
            case "Hostel Services" -> "Maintenance Request";
            case "Transport Services" -> "Transport Complaint";
            case "Gym & Sports Center" -> "Facility Booking";
            case "Placement Cell" -> "Resume Assistance";
            case "Finance & Fees" -> "Fee Payment Issues";
            case "Examination Cell" -> "Exam Schedule Queries";
            case "IT Helpdesk" -> "Student Portal Access";
            case "Academic Support" -> "Course Registration";
            default -> "General Enquiries";
        };
    }

    private String fallback(String message) {
        if (message.contains("scholarship")) return "For scholarship applications, status, renewal, or payment delays, keep your enrollment and bank documents ready. I can help you create a Scholarship Support case if needed.";
        if (message.contains("library")) return "Library timing and digital-resource access depend on campus schedules. Check the Library Services desk, or create a Library Services case for a specific issue.";
        if (message.contains("hall ticket") || message.contains("exam")) return "For hall-ticket and exam schedule questions, check the Examination Cell announcements first. I can help you raise an Examination Cell case.";
        if (message.contains("hostel") || message.contains("wifi")) return "Try reconnecting to the campus network and restarting your device. If the problem persists, submit a Hostel Services or IT Helpdesk case with your building and room number.";
        if (message.contains("login") || message.contains("password")) return "Verify your student username, reset your password, clear browser cache, and try again. If it still fails, I can help you create an IT Helpdesk case.";
        if (message.contains("academic") || message.contains("attendance") || message.contains("marks") || message.contains("subject")) return "For academic support, I can help with course registration, subject changes, attendance questions, marks verification, exam queries, and academic certificates. Include your course and semester when you create a case.";
        if (message.contains("placement") || message.contains("resume") || message.contains("interview")) return "For Placement Cell support, I can help with placement registration, interview schedules, resume guidance, and company applications. Include the company or placement drive name for faster support.";
        return "I can answer campus-service questions, guide troubleshooting, check an owned ticket, or help create a support case. What do you need help with?";
    }

    private String formatOpenRequests(List<Response> requests) {
        StringBuilder answer = new StringBuilder("Here are your active support cases:\n");
        for (int index = 0; index < requests.size(); index++) {
            Response request = requests.get(index);
            String latestRemark = request.history().stream()
                    .map(com.servicehubai.request.api.RequestDtos.HistoryResponse::remarks)
                    .filter(remark -> remark != null && !remark.isBlank())
                    .reduce((first, second) -> second)
                    .orElse("No timeline update yet.");
            answer.append(index + 1).append(". ").append(request.reference()).append("\n")
                    .append("   Category: ").append(request.category()).append("\n")
                    .append("   Subject: ").append(request.subject()).append("\n")
                    .append("   Status: ").append(request.status()).append("\n")
                    .append("   Created: ").append(CHAT_DATE.format(request.createdAt())).append("\n")
                    .append("   Latest update: ").append(latestRemark);
            if (index < requests.size() - 1) answer.append("\n\n");
        }
        return answer.toString();
    }

    private Reply finish(ChatSessionEntity session, String intent, String category, RequestPriority priority,
            String answer, boolean confirmationRequired, Response created, List<Response> matching) {
        messageRepository.save(new ChatMessageEntity(session, "ASSISTANT", answer));
        return new Reply(session.getId(), intent, category, priority.name(), answer, confirmationRequired, created, matching, Instant.now());
    }

    private record PendingCreate(String description, String category, String subCategory, RequestPriority priority) {
    }
}