package com.servicehubai.request.application;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.servicehubai.request.api.RequestDtos.Response;

@Component
public class RequestLifecyclePublisher {

    private final Map<String, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String reference) {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        emitters.computeIfAbsent(reference, ignored -> new CopyOnWriteArrayList<>()).add(emitter);
        Runnable cleanup = () -> remove(reference, emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(ignored -> cleanup.run());
        return emitter;
    }

    public void publish(String reference, Response response) {
        for (SseEmitter emitter : emitters.getOrDefault(reference, new CopyOnWriteArrayList<>())) {
            try {
                emitter.send(SseEmitter.event().name("request-updated").data(response));
            } catch (IOException exception) {
                emitter.completeWithError(exception);
            }
        }
    }

    private void remove(String reference, SseEmitter emitter) {
        var listeners = emitters.get(reference);
        if (listeners != null) {
            listeners.remove(emitter);
            if (listeners.isEmpty()) emitters.remove(reference);
        }
    }
}