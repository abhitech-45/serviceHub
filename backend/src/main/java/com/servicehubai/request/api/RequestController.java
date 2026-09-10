package com.servicehubai.request.api;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.servicehubai.request.api.RequestDtos.CommentRequest;
import com.servicehubai.request.api.RequestDtos.CreateRequest;
import com.servicehubai.request.api.RequestDtos.Response;
import com.servicehubai.request.api.RequestDtos.UpdateRequest;
import com.servicehubai.request.application.RequestService;

@RestController
@RequestMapping("/api/v1/requests")
public class RequestController {

    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    ResponseEntity<Response> create(Authentication authentication, @Valid @RequestBody CreateRequest input) {
        return ResponseEntity.status(HttpStatus.CREATED).body(requestService.create(authentication.getName(), input));
    }

    @GetMapping
    List<Response> list(Authentication authentication) {
        return requestService.list(authentication.getName());
    }

    @GetMapping("/{reference}")
    Response get(Authentication authentication, @PathVariable String reference) {
        return requestService.get(authentication.getName(), reference);
    }

    @PatchMapping("/{reference}")
    Response update(Authentication authentication, @PathVariable String reference, @Valid @RequestBody UpdateRequest input) {
        return requestService.update(authentication.getName(), reference, input);
    }

    @PostMapping("/{reference}/comments")
    Response comment(Authentication authentication, @PathVariable String reference, @Valid @RequestBody CommentRequest input) {
        return requestService.addComment(authentication.getName(), reference, input);
    }

    @GetMapping(value = "/{reference}/events", produces = "text/event-stream")
    SseEmitter events(Authentication authentication, @PathVariable String reference) {
        return requestService.subscribe(authentication.getName(), reference);
    }
}
